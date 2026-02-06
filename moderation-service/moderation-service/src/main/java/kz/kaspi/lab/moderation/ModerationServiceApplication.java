package kz.kaspi.lab.moderation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class ModerationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModerationServiceApplication.class, args);
		//заставляет контекст трассировки летать между потоками
		Hooks.enableAutomaticContextPropagation();
	}

}
