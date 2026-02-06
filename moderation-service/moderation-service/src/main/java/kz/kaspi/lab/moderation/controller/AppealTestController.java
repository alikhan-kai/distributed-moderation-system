package kz.kaspi.lab.moderation.controller;

import kz.kaspi.lab.moderation.model.AppealEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/appeals")
@RequiredArgsConstructor
public class AppealTestController {
    private final KafkaTemplate<String, AppealEvent> kafkaTemplate;
    // Эндпоинт для ручной имитации входящих событий через REST (Postman)
    @PostMapping("/send")
    public String sendToKafka(@RequestBody AppealEvent event){
        // Автогенерация EventId, если клиент его не передал
        if(event.getEventId() == null){
            event.setEventId(UUID.randomUUID().toString());
        }

        // Если время не указано, ставим текущее (важно для правил модерации по времени)
        if(event.getTimestamp() == null){
            event.setTimestamp(LocalDateTime.now());
        }

        // Публикация события во входной топик Kafka (Topic-1)
        // Ключом сообщения делаем EventId для обеспечения порядка обработки
        kafkaTemplate.send("Topic-1", event.getEventId(), event);

        return "Сообщение отправлено в Topic-1! EventId: " + event.getEventId();
    }
}