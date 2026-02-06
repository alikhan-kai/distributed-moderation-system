package kz.kaspi.lab.enrichment.config;

import kz.kaspi.lab.enrichment.model.ClientDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    //Настройка реактивного шаблона для работы с Redis.
    //Определяет, как ключи и значения будут превращаться в байты для хранения.
    @Bean
    public ReactiveRedisTemplate<String, ClientDetails> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory){
        // Настраиваем сериализатор для объектов ClientDetails (превращает их в JSON-строку)
        Jackson2JsonRedisSerializer<ClientDetails> serializer = new Jackson2JsonRedisSerializer<>(ClientDetails.class);

        // Создаем контекст сериализации:
        // Ключи будут обычными строками (String), а значения - объектами в формате JSON
        RedisSerializationContext.RedisSerializationContextBuilder<String, ClientDetails> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, ClientDetails> context = builder.value(serializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }
}
