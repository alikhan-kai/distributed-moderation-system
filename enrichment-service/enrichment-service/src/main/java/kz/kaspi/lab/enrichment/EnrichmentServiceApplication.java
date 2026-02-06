package kz.kaspi.lab.enrichment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class EnrichmentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnrichmentServiceApplication.class, args);
		//заставляет контекст трассировки летать между потоками
		Hooks.enableAutomaticContextPropagation();
	}

}
