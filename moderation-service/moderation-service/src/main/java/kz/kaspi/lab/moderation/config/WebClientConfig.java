package kz.kaspi.lab.moderation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    // Считываем URL сервиса обогащения из файла application.properties
    @Value("${app.enrichment-service-url}")
    private String enrichmentUrl;

    //Создаем Bean WebClient — реактивного HTTP-клиента.
    //Он будет использоваться для асинхронных запросов к Service-2
    @Bean
    public WebClient enrichmentWebClient(WebClient.Builder builder){
        return builder
                .baseUrl(enrichmentUrl) // Базовый адрес (например, http://localhost:8081/api/enrichment)
                .build();
    }
}