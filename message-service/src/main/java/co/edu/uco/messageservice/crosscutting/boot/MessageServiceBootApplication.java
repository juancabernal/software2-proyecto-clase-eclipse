package co.edu.uco.messageservice.crosscutting.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Message Service application. Kept within the crosscutting
 * layer to avoid leaking infrastructure concerns into the domain or
 * application layers.
 */
@SpringBootApplication(scanBasePackages = "co.edu.uco.messageservice")
public class MessageServiceBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessageServiceBootApplication.class, args);
    }
}
