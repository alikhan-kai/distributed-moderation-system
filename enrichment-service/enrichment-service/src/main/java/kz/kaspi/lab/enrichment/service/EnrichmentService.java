package kz.kaspi.lab.enrichment.service;

import kz.kaspi.lab.enrichment.model.AppealEvent;
import kz.kaspi.lab.enrichment.model.ClientDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrichmentService {
    // Шаблон для работы со строками (статусы обработки)
    private final ReactiveRedisTemplate<String, String> stringRedisTemplate;
    // Шаблон для работы с объектами ClientDetails (профили клиентов)
    private final ReactiveRedisTemplate<String, ClientDetails> clientDetailsRedisTemplate;

    //Обогащение события данными из Redis.
    // Даже если данных нет, процесс не прерывается (fallback на дефолт).
    public Mono<AppealEvent> enrichAppeal(AppealEvent event) {
        String eventKey = "event:" + event.getEventId();

        // 1. Пытаемся достать детали клиента из Redis по его clientId
        return clientDetailsRedisTemplate.opsForValue().get(event.getClientId())
                .flatMap(details -> {
                    // Если данные найдены:
                    log.info("[ID: {}] Данные клиента {} найдены. Категория: {}",
                            event.getEventId(), event.getClientId(), details.getCategory());

                    // Помечаем в Redis, что событие успешно обогащено
                    return stringRedisTemplate.opsForValue()
                            .set(eventKey, "FULLY_ENRICHED", Duration.ofHours(24))
                            .thenReturn(event);
                })
                // 2. Если данных в Redis нет (пустой результат):
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("[ID: {}] Данные клиента {} не найдены, используем дефолт.",
                            event.getEventId(), event.getClientId());

                    // Все равно помечаем событие как готовое, чтобы цепочка не обрывалась
                    return stringRedisTemplate.opsForValue()
                            .set(eventKey, "FULLY_ENRICHED", Duration.ofHours(24))
                            .thenReturn(event);
                }));
    }
}