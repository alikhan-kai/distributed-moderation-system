package kz.kaspi.lab.moderation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppealEvent {
    private String eventId; //Уникальный ID события (для идемпотентности)
    private String clientId; //Id клиента
    private String text; //Текст обращения
    private String category; //Категория (например, КРЕДИТ)
    private LocalDateTime timestamp;
}
