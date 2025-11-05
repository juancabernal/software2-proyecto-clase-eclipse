package co.edu.uco.ucochallenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
<<<<<<< HEAD
import org.springframework.scheduling.annotation.EnableScheduling;
=======
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
>>>>>>> a2fa3830db9d55b2b837f44eb0200001876f8ed7

@EnableAsync
@SpringBootApplication
@EnableCaching// Asegura que escanee todos los subpaquetes.
@EnableScheduling
public class UcoChallengeApplication {

	public static void main(String[] args) {
		SpringApplication.run(UcoChallengeApplication.class, args);
	}

}
