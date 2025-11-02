package co.edu.uco.ucochallenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Main application class for the UcoChallenge Spring Boot application.
@SpringBootApplication(scanBasePackages = "co.edu.uco.ucochallenge")
public class UcoChallengeApplication {

        public static void main(String[] args) {
                SpringApplication.run(UcoChallengeApplication.class, args);
        }

}
