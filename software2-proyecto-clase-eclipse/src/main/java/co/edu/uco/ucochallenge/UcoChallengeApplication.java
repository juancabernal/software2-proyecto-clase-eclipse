package co.edu.uco.ucochallenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

// Main application class for the UcoChallenge Spring Boot application.
@SpringBootApplication
@EnableCaching// Asegura que escanee todos los subpaquetes.
@EnableScheduling
public class UcoChallengeApplication {

	public static void main(String[] args) {
		SpringApplication.run(UcoChallengeApplication.class, args);
	}

}
