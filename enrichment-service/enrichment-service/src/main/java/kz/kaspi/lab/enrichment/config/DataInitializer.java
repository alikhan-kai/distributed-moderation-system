package kz.kaspi.lab.enrichment.config;

import kz.kaspi.lab.enrichment.model.ClientDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    private final ReactiveRedisTemplate<String, ClientDetails> redisTemplate;

    @EventListener(ApplicationReadyEvent.class) //Сработает сразу после запуска приложения
    public void initData(){
        ClientDetails client1 = new ClientDetails("1", "CREDIT", false); //Нет активных заявок
        ClientDetails client2 = new ClientDetails("2", "DEBIT", true); //Есть активная заявка (должен быть отклонён правилами)

        redisTemplate.opsForValue().set("1", client1)
                .then(redisTemplate.opsForValue().set("2", client2))
                .subscribe(success -> log.info("Тестовые данные загружены в Redis!"));
    }
}
