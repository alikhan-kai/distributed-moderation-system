# Скрипт для нагрузочного теста (100 запросов)
$url = "http://localhost:8080/api/appeals/send"
$body = @{
    clientId = "3"
    text = "Нагрузочный тест из PowerShell"
    category = "CREDIT"
    timestamp = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
} | ConvertTo-Json

Write-Host "Начинаю отправку 100 запросов..." -ForegroundColor Cyan

# Запускаем 100 запросов
for ($i=1; $i -le 100; $i++) {
    Invoke-RestMethod -Uri $url -Method Post -Body $body -ContentType "application/json"
    Write-Host "Запрос №$i отправлен"
}

Write-Host "Тест завершен успешно!" -ForegroundColor Green