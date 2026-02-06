package kz.kaspi.lab.enrichment.consumer;

import kz.kaspi.lab.enrichment.model.AppealEvent;
import kz.kaspi.lab.enrichment.service.EnrichmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnrichmentConsumer {
    private final EnrichmentService enrichmentService;
    //Слушатель Topic-2. Сюда попадают события, успешно прошедшие модерацию.
    @KafkaListener(topics = "Topic-2", groupId = "enrichment-group")
    public void consume(AppealEvent event) {
        log.info("Enrichment: Получено событие из Topic-2: {}", event.getEventId());

        // Запуск процесса финального обогащения и сохранения статуса в Redis
        enrichmentService.enrichAppeal(event)
                .doOnError(e -> log.error("Ошибка при финальном обогащении события {}: {}",
                        event.getEventId(), e.getMessage()))
                .subscribe(enriched ->
                        log.info("Обращение успешно обогащено для клиента: {}", enriched.getClientId())
                );
    }
}