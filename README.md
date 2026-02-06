Отказоустойчивая микросервисная система на событийной архитектуре (Event-Driven). Проект реализует асинхронный конвейер обработки заявок с использованием Java 21, Spring Boot 3.5.10 и Apache Kafka.

Технологический стек
Spring WebFlux: Полностью реактивное взаимодействие без блокировок.
Apache Kafka: Брокер сообщений для асинхронной связи между сервисами.
Redis: Обеспечение идемпотентности (дедупликация) и хранение кэша профилей.
Micrometer & Tracing: Сквозная трассировка запросов (Trace ID).
Docker: Контейнеризация всей инфраструктуры.

Инструкция по запуску
Убедитесь, что запущен Docker Desktop.
В корневой директории проекта выполните команду для запуска инфраструктуры:
Bash: docker-compose up -d
Запустите микросервисы в следующем порядке:
Сначала Enrichment Service
Затем Moderation Service
При старте Enrichment Service автоматически инициализирует тестовые данные клиентов в Redis.

Точки доступа и порты
Moderation API: http://localhost:8080/api/appeals/send — основной вход для заявок.
Swagger UI (Enrichment Service): http://localhost:8081/webjars/swagger-ui/index.html — проверка данных в кэше через эндпоинт getDetails.

Тестирование бизнес-кейсов
1. Проверка через Swagger
Используйте веб-интерфейс сервиса обогащения для подтверждения, что профиль клиента (например, ID "1") корректно загружен в реактивный кэш.
2. Эмуляция входящего события (Postman)
Метод: POST
URL: http://localhost:8080/api/appeals/send
Body (JSON):
JSON
{"clientId": "1", "category": "CREDIT", "text": "Заявка на кредит"}
Результат: Статус 200 OK, сообщение мгновенно уходит в Topic-1 и проходит через конвейер.

4. Демонстрация логики модерации
Идемпотентность: При быстрой отправке двух одинаковых запросов Redis отсекает повтор по eventId.
Правила по времени: Заявки категории CREDIT отклоняются (REJECTED_BY_TIME), если время сервера не входит в рабочий интервал (09:00 - 18:00).
Отказоустойчивость: Реализован механизм Retry (3 попытки с задержкой 2 сек) для запросов к сервису обогащения.

Нагрузочное тестирование (PowerShell)
Скрипт №1: Базовая нагрузка (визуальный контроль логов)
PowerShell

1..100 | ForEach-Object { Invoke-RestMethod -Uri "http://localhost:8080/api/appeals/send" -Method Post -Body '{"clientId":"3","text":"Test Load","category":"CREDIT"}' -ContentType "application/json"; Write-Host "Запрос №$_ отправлен" -ForegroundColor Cyan }

Скрипт №2: Анализ производительности (RPS)
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
Итог: В ходе тестов система показала результат 355.0 RPS, что подтверждает высокую эффективность реактивного стека.

Внутренняя логика
Система работает по принципу неблокирующей обработки. После публикации сообщения в Kafka контроллер сразу возвращает ответ. Вся цепочка (модерация, запрос к Redis, обогащение через WebClient) выполняется асинхронно в пуле потоков boundedElastic. Trace ID сохраняется на всех этапах, что позволяет отслеживать путь каждой заявки в логах обоих сервисов.
