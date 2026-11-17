# Motorboard

Responsive car-sales dashboard using the existing Spring Boot, Java 17, MySQL, Spring Data JPA, Spring AI/Ollama, and browser HTML/CSS/JavaScript stack.

## Requirements

- JDK 17 or newer
- MySQL with a `carsales` database
- Ollama with the configured model available for AI questions

Create the database once in MySQL:

```sql
CREATE DATABASE carsales;
```

The application creates or updates the `car_sales` table on startup. Configure database access with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Defaults target `jdbc:mysql://localhost:3306/carsales` and user `root` with an empty password.

## Run

Start Ollama and download the default model in another terminal:

```powershell
ollama pull llama2
ollama serve
```

From the project directory, set `DB_PASSWORD` if needed and run:

```powershell
.\mvnw.cmd spring-boot:run
```

Open <http://localhost:9000>. Set `OLLAMA_BASE_URL` or `OLLAMA_MODEL` to use another local Ollama endpoint or model. The dashboard, CSV import, and sales analytics need MySQL; AI questions additionally need Ollama.

## CSV format

Use the **Get template** link in the dashboard. Header names are case-insensitive. Purchase dates use `dd-MM-yyyy`, and purchase times use `HH:mm:ss`. Duplicate car numbers are skipped.

## Workflows

- Import CSV sales with drag-and-drop and see imported/skipped row totals
- View total sales by purchase year and monthly sales for a selected year
- Ask natural-language questions about sales through the configured Ollama model