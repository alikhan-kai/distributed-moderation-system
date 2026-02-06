package kz.kaspi.lab.moderation.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservationConfig {
    //Этот бин создает реестр для сбора метрик и трассировки
    @Bean
    ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }

    //Этот аспект позволяет автоматически перехватывать вызовы и вешать на них Trace ID
    @Bean
    ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}