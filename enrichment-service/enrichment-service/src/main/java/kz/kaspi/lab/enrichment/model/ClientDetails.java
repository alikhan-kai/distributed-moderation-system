package kz.kaspi.lab.enrichment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientDetails implements Serializable {
    private String clientId;
    private String category;
    private boolean hasActiveAppeals; // Для правила: "Наличие активных обращений"
}
