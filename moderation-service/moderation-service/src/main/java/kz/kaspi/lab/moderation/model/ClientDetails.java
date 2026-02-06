package kz.kaspi.lab.moderation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO (Data Transfer Object) для получения расширенных данных о клиенте из Service-2
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientDetails {
    private String clientId; // Уникальный идентификатор клиента
    private String category; // Категория обслуживания (например, CREDIT, DEBIT)
    private boolean hasActiveAppeals; // Флаг наличия открытых обращений (для правил модерации)
}