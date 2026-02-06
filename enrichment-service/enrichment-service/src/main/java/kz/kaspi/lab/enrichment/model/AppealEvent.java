package kz.kaspi.lab.enrichment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

//Модель события обращения, которая передается через Kafka.
//Поля должны строго совпадать с моделью в Moderation Service для корректной десериализации.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppealEvent {
    private String eventId; // Уникальный ID события (используется для идемпотентности)
    private String clientId; // ID клиента для поиска профиля в Redis
    private String text; // Текст обращения клиента
    private String category; // Категория (например, CREDIT, DEBIT, SUPPORT)
    private LocalDateTime timestamp; // Время создания (используется для проверки "свежести")
}