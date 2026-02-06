package kz.kaspi.lab.moderation.consumer;

import kz.kaspi.lab.moderation.model.AppealEvent;
import kz.kaspi.lab.moderation.service.ModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppealConsumer {
    private final ModerationService moderationService;
    //Слушатель входного топика Kafka.
    //groupId изменен на v3 для сброса оффсетов и начала чтения "чистой" очереди.
    @KafkaListener(topics = "Topic-1", groupId = "moderation-group-v3")
    public void consume(AppealEvent event){
        log.info("===> Считано из Kafka (Topic-1): {}", event.getEventId());

        // Запуск реактивной обработки события.
        // .subscribe() необходим, так как без подписки Project Reactor не начнет выполнение.
        moderationService.processAppeal(event)
                .doOnError(e -> log.error("Критическая ошибка при обработке события {}: {}",
                        event.getEventId(), e.getMessage()))
                .subscribe();
    }
}