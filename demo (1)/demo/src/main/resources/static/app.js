const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
const numberFormat = new Intl.NumberFormat();
const state = { yearly: [], selectedFile: null };

const elements = {
  connectionDot: document.querySelector('#connection-dot'),
  connectionLabel: document.querySelector('#connection-label'),
  totalSales: document.querySelector('#total-sales'),
  bestYear: document.querySelector('#best-year'),
  bestYearNote: document.querySelector('#best-year-note'),
  yearSales: document.querySelector('#year-sales'),
  yearSalesNote: document.querySelector('#year-sales-note'),
  yearSelect: document.querySelector('#year-select'),
  chartTotal: document.querySelector('#chart-total'),
  chart: document.querySelector('#bar-chart'),
  chartCaption: document.querySelector('#chart-caption'),
  dropZone: document.querySelector('#drop-zone'),
  fileInput: document.querySelector('#csv-file'),
  fileName: document.querySelector('#file-name'),
  filePrompt: document.querySelector('#file-prompt'),
  uploadButton: document.querySelector('#upload-button'),
  uploadNotice: document.querySelector('#upload-notice'),
  questionForm: document.querySelector('#question-form'),
  questionInput: document.querySelector('#question-input'),
  chatLog: document.querySelector('#chat-log'),
  sendButton: document.querySelector('.send-button'),
  aiStatus: document.querySelector('#ai-status')
};

document.querySelector('#today-label').textContent = new Intl.DateTimeFormat(undefined, {
  month: 'short', day: 'numeric', year: 'numeric'
}).format(new Date());

function setConnection(online) {
  elements.connectionDot.classList.toggle('is-online', online);
  elements.connectionLabel.textContent = online ? 'Sales data connected' : 'Data service unavailable';
}

async function loadSalesSummary() {
  try {
    const response = await fetch('/api/car-sales/yearly-count');
    if (!response.ok) throw new Error('Sales data could not be loaded.');
    const payload = await response.json();
    state.yearly = Array.isArray(payload.data) ? payload.data : [];
    setConnection(true);
    renderYearOptions();
    renderYearlySummary();
    await loadMonthlySummary();
  } catch (error) {
    setConnection(false);
    elements.totalSales.textContent = '—';
    elements.bestYearNote.textContent = 'Could not connect to the sales database';
    renderEmptyChart('Sales data is unavailable');
  }
}

function renderYearOptions() {
  const years = state.yearly.map((item) => Number(item.year)).filter(Number.isFinite);
  const currentYear = new Date().getFullYear();
  const options = [...new Set([...years, currentYear])].sort((a, b) => b - a);
  elements.yearSelect.replaceChildren(...options.map((year) => {
    const option = document.createElement('option');
    option.value = String(year);
    option.textContent = String(year);
    return option;
  }));
  elements.yearSelect.value = String(years.includes(currentYear) ? currentYear : (years[0] ?? currentYear));
}

function renderYearlySummary() {
  const total = state.yearly.reduce((sum, item) => sum + Number(item.count || 0), 0);
  const best = [...state.yearly].sort((a, b) => Number(b.count) - Number(a.count))[0];
  elements.totalSales.textContent = numberFormat.format(total);
  elements.bestYear.textContent = best ? String(best.year) : '—';
  elements.bestYearNote.textContent = best ? `${numberFormat.format(best.count)} vehicles sold` : 'No sales recorded yet';
}

async function loadMonthlySummary() {
  const year = elements.yearSelect.value;
  if (!year) return;
  elements.chartCaption.textContent = `Purchase date · ${year}`;
  try {
    const response = await fetch(`/api/car-sales/monthly-count?year=${encodeURIComponent(year)}`);
    if (!response.ok) throw new Error('Monthly sales could not be loaded.');
    const payload = await response.json();
    renderMonthlyChart(Array.isArray(payload.data) ? payload.data : []);
  } catch (error) {
    renderEmptyChart('Monthly sales are unavailable');
  }
}

function renderMonthlyChart(data) {
  const counts = Array.from({ length: 12 }, () => 0);
  data.forEach((item) => {
    const monthIndex = Number(item.month) - 1;
    if (monthIndex >= 0 && monthIndex < 12) counts[monthIndex] = Number(item.count) || 0;
  });
  const max = Math.max(...counts, 1);
  const total = counts.reduce((sum, count) => sum + count, 0);
  elements.chartTotal.textContent = numberFormat.format(total);
  elements.yearSales.textContent = numberFormat.format(total);
  elements.yearSalesNote.textContent = `Vehicles sold in ${elements.yearSelect.value}`;
  elements.chart.setAttribute('aria-label', `${total} vehicles sold in ${elements.yearSelect.value}, by month`);
  elements.chart.replaceChildren(...counts.map((count, index) => {
    const item = document.createElement('div');
    item.className = 'bar-item';
    item.title = `${monthNames[index]}: ${numberFormat.format(count)} vehicles`;
    const value = document.createElement('span');
    value.className = 'bar-value';
    value.textContent = count ? numberFormat.format(count) : '';
    const track = document.createElement('div');
    track.className = 'bar-track';
    const fill = document.createElement('div');
    fill.className = 'bar-fill';
    fill.style.height = `${count ? Math.max(5, (count / max) * 100) : 0}%`;
    track.append(fill);
    const label = document.createElement('span');
    label.className = 'bar-label';
    label.textContent = monthNames[index];
    item.append(value, track, label);
    return item;
  }));
}

function renderEmptyChart(message) {
  elements.chartTotal.textContent = '—';
  elements.yearSales.textContent = '—';
  elements.yearSalesNote.textContent = message;
  elements.chart.setAttribute('aria-label', message);
  elements.chart.replaceChildren(...monthNames.map((month) => {
    const item = document.createElement('div');
    item.className = 'bar-item';
    const track = document.createElement('div');
    track.className = 'bar-track';
    const label = document.createElement('span');
    label.className = 'bar-label';
    label.textContent = month;
    item.append(track, label);
    return item;
  }));
}

function selectFile(file) {
  if (!file) return;
  elements.uploadNotice.hidden = true;
  if (!file.name.toLowerCase().endsWith('.csv') || file.size > 5 * 1024 * 1024) {
    state.selectedFile = null;
    elements.uploadButton.disabled = true;
    showUploadNotice(file.size > 5 * 1024 * 1024 ? 'Choose a CSV file smaller than 5 MB.' : 'Please choose a .csv file.', true);
    return;
  }
  state.selectedFile = file;
  elements.filePrompt.textContent = 'Ready to import';
  elements.fileName.textContent = file.name;
  elements.uploadButton.disabled = false;
}

function showUploadNotice(message, isError = false) {
  elements.uploadNotice.textContent = message;
  elements.uploadNotice.classList.toggle('is-error', isError);
  elements.uploadNotice.hidden = false;
}

elements.dropZone.addEventListener('click', (event) => {
  if (event.target.closest('button')) return;
  elements.fileInput.click();
});
elements.dropZone.addEventListener('keydown', (event) => {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault();
    elements.fileInput.click();
  }
});
elements.fileInput.addEventListener('change', () => selectFile(elements.fileInput.files[0]));
['dragenter', 'dragover'].forEach((name) => elements.dropZone.addEventListener(name, (event) => {
  event.preventDefault();
  elements.dropZone.classList.add('is-dragging');
}));
['dragleave', 'drop'].forEach((name) => elements.dropZone.addEventListener(name, (event) => {
  event.preventDefault();
  elements.dropZone.classList.remove('is-dragging');
}));
elements.dropZone.addEventListener('drop', (event) => selectFile(event.dataTransfer.files[0]));

elements.uploadButton.addEventListener('click', async () => {
  if (!state.selectedFile) return;
  const formData = new FormData();
  formData.append('file', state.selectedFile);
  elements.uploadButton.disabled = true;
  elements.uploadButton.textContent = 'Uploading…';
  try {
    const response = await fetch('/api/car-sales/upload-csv', { method: 'POST', body: formData });
    const payload = await response.json();
    if (!response.ok) throw new Error(payload.message || 'Upload failed.');
    const result = payload.data || {};
    const message = `${numberFormat.format(result.successCount || 0)} of ${numberFormat.format(result.totalRecords || 0)} rows imported. ${numberFormat.format(result.failureCount || 0)} rows skipped.`;
    showUploadNotice(message, !payload.success);
    state.selectedFile = null;
    elements.filePrompt.textContent = 'Drop your CSV here';
    elements.fileName.textContent = 'or choose a file from your device';
    elements.fileInput.value = '';
    await loadSalesSummary();
  } catch (error) {
    showUploadNotice(error.message || 'Upload failed. Check your connection and try again.', true);
  } finally {
    elements.uploadButton.innerHTML = 'Upload sales <span aria-hidden="true">→</span>';
    elements.uploadButton.disabled = !state.selectedFile;
  }
});

document.querySelector('#template-download').addEventListener('click', (event) => {
  event.preventDefault();
  const headers = ['car number', 'brand', 'model', 'color', 'year', 'time of purchase', 'date of purchase', 'price', 'mileage', 'engine(cc)', 'fuel type', 'payment mode', 'state', 'city', 'customer name', 'contact number', 'email', 'warranty period'];
  const blob = new Blob([`${headers.join(',')}\r\n`], { type: 'text/csv;charset=utf-8' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = 'car-sales-template.csv';
  link.click();
  URL.revokeObjectURL(link.href);
});

elements.yearSelect.addEventListener('change', loadMonthlySummary);

function addMessage(text, sender, isError = false) {
  const message = document.createElement('div');
  message.className = `chat-message ${sender}-message${isError ? ' is-error' : ''}`;
  if (sender === 'assistant') {
    const avatar = document.createElement('span');
    avatar.className = 'message-avatar';
    avatar.textContent = 'M';
    message.append(avatar);
  }
  const paragraph = document.createElement('p');
  paragraph.textContent = text;
  message.append(paragraph);
  elements.chatLog.append(message);
  elements.chatLog.scrollTop = elements.chatLog.scrollHeight;
  return paragraph;
}

elements.questionForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  const question = elements.questionInput.value.trim();
  if (!question) return;
  addMessage(question, 'user');
  elements.questionInput.value = '';
  elements.sendButton.disabled = true;
  elements.aiStatus.textContent = 'Checking your sales data…';
  const answer = addMessage('Thinking…', 'assistant');
  try {
    const response = await fetch('/api/ai/ask', {
      method: 'POST',
      headers: { 'Content-Type': 'text/plain;charset=UTF-8' },
      body: question
    });
    if (!response.ok) throw new Error('The AI service could not answer right now.');
    answer.textContent = await response.text();
    elements.aiStatus.textContent = 'Your question is answered using your sales data.';
  } catch (error) {
    answer.textContent = error.message || 'Could not reach the AI service. Check that Ollama is running.';
    answer.parentElement.classList.add('is-error');
    elements.aiStatus.textContent = 'Check that the application and Ollama are running.';
  } finally {
    elements.sendButton.disabled = false;
    elements.questionInput.focus();
  }
});

loadSalesSummary();