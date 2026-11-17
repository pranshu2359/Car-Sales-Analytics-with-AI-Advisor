package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AIQueryServiceImpl implements AIQueryService {

    private final ChatClient chatClient;
    private final JdbcTemplate jdbcTemplate;

    public AIQueryServiceImpl(ChatClient.Builder builder, JdbcTemplate jdbcTemplate) {
        this.chatClient = builder.build();
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String process(String question) {
        String sql = generateSQL(question);
        if ("INVALID".equalsIgnoreCase(sql)) {
            return "only table related question allowed";
        }
        if (!isSafe(sql)) {
            return "unsafe query";
        }
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);

            if (result.isEmpty()) {
                return "no data found";
            }

            return toNaturalLanguage(question, result);

        } catch (Exception e) {
            System.out.println("Query execution failed: " + e.getMessage());
            return "Query failed";
        }
    }

    private boolean isSafe(String sql) {
        String lower = sql.toLowerCase();
        return lower.startsWith("select")
                && !lower.contains("drop")
                && !lower.contains("delete")
                && !lower.contains("update")
                && !lower.contains("insert");
    }

    private String generateSQL(String question) {
        String prompt = """
               You are a SQL generator.
               Table: car_sales
               Columns: id, brand, car_number, city, color, contact_number, customer_name, date_of_purchase, email, engine, fuel_type, mileage, model, payment_mode, state, time_of_purchase, warranty_period, year
               
               Rules:
               - Only SELECT queries
               - Use only given columns
               - If not related, return: INVALID
               - Return only SQL
               
               Question:
               """ + question;

        String response = chatClient.prompt().user(prompt).call().content();
        return response != null ? response.trim() : "INVALID";
    }

    private String toNaturalLanguage(String question, List<Map<String, Object>> result) {
        String prompt = """
                Convert database result into a human readable answer.
                
                User Question:
                """ + question + """
                
                DB Result:
                """ + result.toString() + """
                
                Rules:
                - Answer clearly (don’t write too much)
                - Do not show JSON
                - Do not explain SQL
                """;

        String response = chatClient.prompt().user(prompt).call().content();
        return response != null ? response.trim() : "no data found";
    }
}
