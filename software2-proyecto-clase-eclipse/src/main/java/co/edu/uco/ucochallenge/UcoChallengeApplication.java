package co.edu.uco.ucochallenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

// Main application class for the UcoChallenge Spring Boot application.
@SpringBootApplication

@ComponentScan(basePackages = "co.edu.uco.ucochallenge") // Asegura que escanee todos los subpaquetes.
public class UcoChallengeApplication {

	public static void main(String[] args) {
		SpringApplication.run(UcoChallengeApplication.class, args);
	}

}
