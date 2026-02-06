Отказоустойчивая микросервисная система на событийной архитектуре (Event-Driven). Проект реализует асинхронный конвейер обработки заявок с использованием Java 21, Spring Boot 3.5.10 и Apache Kafka.

Технологический стек
Spring WebFlux: Полностью реактивное взаимодействие без блокировок.
Apache Kafka: Брокер сообщений для асинхронной связи между сервисами.
Redis: Обеспечение идемпотентности (дедупликация) и хранение кэша профилей.
Micrometer & Tracing: Сквозная трассировка запросов (Trace ID).
Docker: Контейнеризация всей инфраструктуры.

Инструкция по запуску
Убедитесь, что запущен Docker Desktop.
В корневой директории проекта выполните команду для запуска инфраструктуры: docker-compose up -d.
Запустите микросервисы в следующем порядке: сначала Enrichment Service, затем Moderation Service.
При старте Enrichment Service автоматически инициализирует тестовые данные клиентов в Redis.

Точки доступа и порты
Moderation API: http://localhost:8080/api/appeals/send — основной вход для заявок.
Swagger UI (Enrichment Service): http://localhost:8081/webjars/swagger-ui/index.html. Здесь через эндпоинт getDetails можно проверить данные клиента в кэше.

Тестирование бизнес-кейсов
1. Проверка через Swagger Используйте веб-интерфейс сервиса обогащения для подтверждения, что профиль клиента (например, ID "1") корректно загружен в реактивный кэш и доступен для системы.
2. Эмуляция входящего события (Postman)
Метод: POST
URL: http://localhost:8080/api/appeals/send
Body (JSON):
JSON
{"clientId": "1", "category": "CREDIT", "text": "Заявка на кредит"} 
Результат: Сообщение мгновенно уходит в Topic-1, проходит через воркеров и модерацию.

3. Демонстрация логики модерации
Идемпотентность: При быстрой отправке двух одинаковых запросов Redis отсекает повтор по eventId, предотвращая дублирование.
Правила по времени: Заявки категории CREDIT отклоняются (REJECTED_BY_TIME), если время сервера не входит в рабочий интервал (09:00 - 18:00).
Отказоустойчивость: Реализован механизм Retry (3 попытки с задержкой 2 сек) для запросов к сервису обогащения, что исключает сбои при временной нестабильности сети.

Нагрузочное тестирование (PowerShell)
Для проверки системы в условиях высокого трафика используются следующие скрипты:
Скрипт №1: Базовая нагрузка Простая отправка 100 запросов для визуального контроля логов:
PowerShell
1..100 | ForEach-Object { Invoke-RestMethod -Uri "http://localhost:8080/api/appeals/send" -Method Post -Body '{"clientId":"3","text":"Test Load","category":"CREDIT"}' -ContentType "application/json"; Write-Host "Запрос №$_ отправлен" -ForegroundColor Cyan }
Скрипт №2: Анализ производительности (RPS) Скрипт замеряет время выполнения параллельных запросов и рассчитывает итоговый показатель:

PowerShell
$url = "http://localhost:8080/api/appeals/send"
$body = '{"clientId":"3","text":"RPS Test","category":"CREDIT"}'
$totalRequests = 100
$timer = [System.Diagnostics.Stopwatch]::StartNew()

1..$totalRequests | ForEach-Object -Parallel {
    try {
        Invoke-RestMethod -Uri $using:url -Method Post -Body $using:body -ContentType "application/json" -ErrorAction Stop > $null
    } catch { Write-Host "Ошибка!" -ForegroundColor Red }
} -ThrottleLimit 50

$timer.Stop()
$rps = [math]::Round($totalRequests / $timer.Elapsed.TotalSeconds, 2)
Write-Host "ИТОГОВЫЙ RPS: $rps" -ForegroundColor Green -BackgroundColor Black
В ходе тестов система показала результат 355.0 RPS, что подтверждает эффективность реактивного подхода.

Внутренняя логика
Система работает по принципу неблокирующей обработки. После публикации сообщения в Kafka контроллер сразу возвращает ответ. Вся цепочка (модерация, запрос к Redis, обогащение через WebClient) выполняется асинхронно. Сквозная трассировка (Trace ID) позволяет отслеживать путь каждой заявки между микросервисами даже при высокой нагрузке.
