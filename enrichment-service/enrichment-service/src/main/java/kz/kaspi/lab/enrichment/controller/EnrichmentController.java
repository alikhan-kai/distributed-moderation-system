package kz.kaspi.lab.enrichment.controller;

import kz.kaspi.lab.enrichment.model.ClientDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/enrichment")
@RequiredArgsConstructor
@Slf4j
public class EnrichmentController {
    private final ReactiveRedisTemplate<String, ClientDetails> redisTemplate;
    //REST-эндпоинт для Service-1 (Moderation).
    //Возвращает расширенную информацию о клиенте из кэша Redis.
    @GetMapping("/{clientId}")
    public Mono<ClientDetails> getDetails(@PathVariable String clientId) {
        log.info("Запрос данных клиента через HTTP: {}", clientId);

        return redisTemplate.opsForValue().get(clientId)
                .doOnNext(details -> log.info("Клиент найден в Redis: {}", clientId))
                // Kорректная обработка отсутствия данных.
                // switchIfEmpty гарантирует, что Service-1 всегда получит валидный объект.
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Клиент {} не найден в Redis, возвращаем дефолт", clientId);
                    // Возвращаем "чистый" профиль: обычная категория, без активных жалоб.
                    return Mono.just(new ClientDetails(clientId, "COMMON", false));
                }));
    }
}