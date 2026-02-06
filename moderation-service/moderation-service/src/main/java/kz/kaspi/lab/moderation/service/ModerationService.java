package kz.kaspi.lab.moderation.service;

import kz.kaspi.lab.moderation.model.AppealEvent;
import kz.kaspi.lab.moderation.model.ClientDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModerationService {
    private final WebClient enrichmentWebClient;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, AppealEvent> kafkaTemplate;
    private final ModerationRules rules;

    public Mono<Void> processAppeal(AppealEvent event) {
        String eventKey = "event:" + event.getEventId();
        log.info("[TRACE ID: {}] --- [START] Обработка события ---", event.getEventId());

        // Защита от дублей (идемпотентность) через Redis
        return redisTemplate.opsForValue()
                .setIfAbsent(eventKey, "PROCESSING", Duration.ofHours(24))
                .flatMap(isNew -> {
                    if (Boolean.FALSE.equals(isNew)) {
                        log.info("Дубликат: событие {} уже обрабатывалось.", event.getEventId());
                        return Mono.empty(); // Прерываем цепочку для повторных событий
                    }

                    return handleModeration(event)
                            .flatMap(status -> redisTemplate.opsForValue()
                                    .set(eventKey, status, Duration.ofHours(24)) // Фиксируем финальный статус
                                    .doOnSuccess(v -> log.info("--- [FINISH] Событие {} завершено: {} ---", event.getEventId(), status))
                            );
                })
                .then();
    }

    private Mono<String> handleModeration(AppealEvent event) {
        // Проверка бизнес-правил: актуальность по времени и категориям
        if (!rules.isFresh(event)) {
            return Mono.just("REJECTED_BY_AGE");
        }
        if (!rules.isAllowedByTime(event)) {
            return Mono.just("REJECTED_BY_TIME");
        }

        // Обогащение данных через внешний микросервис
        return fetchClientDetails(event.getClientId())
                .flatMap(details -> {
                    // Проверка на наличие открытых тикетов у клиента
                    if (!rules.hasNoActiveAppeals(details)) {
                        return Mono.just("REJECTED_HAS_ACTIVE");
                    }
                    // Если все проверки пройдены — шлем в Kafka
                    return sendToNextTopic(event).thenReturn("SENT_TO_NEXT_STEP");
                });
    }

    private Mono<ClientDetails> fetchClientDetails(String clientId) {
        return enrichmentWebClient.get()
                .uri("/{clientId}", clientId)
                .retrieve()
                .bodyToMono(ClientDetails.class)
                .timeout(Duration.ofSeconds(2)) // Ограничение времени ожидания
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)) // Требование ТЗ: ретраи при сбоях
                        .doBeforeRetry(sig -> log.warn("Retry Service-2, попытка: {}", sig.totalRetries() + 1))
                )
                .onErrorResume(e -> {
                    // Обработка отсутствия данных или сбоя (Fallback)
                    log.error("Сбой Service-2: {}. Применяем дефолт.", e.getMessage());
                    return Mono.just(new ClientDetails(clientId, "UNKNOWN", false));
                });
    }

    private Mono<Void> sendToNextTopic(AppealEvent event) {
        // Асинхронная отправка результата в следующий топик Kafka
        return Mono.fromFuture(kafkaTemplate.send("Topic-2", event.getEventId(), event))
                .doOnSuccess(res -> log.info("===> Отправлено в Topic-2: {}", event.getEventId()))
                .then();
    }
}