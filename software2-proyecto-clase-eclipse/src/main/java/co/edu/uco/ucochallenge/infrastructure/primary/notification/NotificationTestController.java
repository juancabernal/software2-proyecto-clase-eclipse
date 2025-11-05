package co.edu.uco.ucochallenge.infrastructure.primary.notification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;

import co.edu.uco.ucochallenge.application.notification.DuplicateRegistrationNotificationService;
import co.edu.uco.ucochallenge.application.notification.RegistrationAttempt;

@RestController
@RequestMapping("/notifications/test")
public class NotificationTestController {

    private final DuplicateRegistrationNotificationService notificationService;

    public NotificationTestController(DuplicateRegistrationNotificationService notificationService) {
        this.notificationService = notificationService;
    }
    @PostConstruct
    public void init() {
        System.out.println("✅ NotificationTestController loaded successfully!");
    }


    @PostMapping("/duplicate/email")
    public ResponseEntity<String> testDuplicateEmail(@RequestBody RegistrationAttempt body) {
        notificationService.notifyEmailConflict(body);
        return ResponseEntity.ok("✅ Email duplicate notification sent.");
    }

    @PostMapping("/duplicate/mobile")
    public ResponseEntity<String> testDuplicateMobile(@RequestBody RegistrationAttempt body) {
        notificationService.notifyMobileConflict(body);
        return ResponseEntity.ok("✅ Mobile duplicate notification sent.");
    }

    @PostMapping("/confirm/email")
    public ResponseEntity<String> testConfirmEmail(@RequestBody RegistrationAttempt body) {
        notificationService.notifyEmailConfirmation(body, "123456", 5, 3);

        return ResponseEntity.ok("✅ Email confirmation notification sent.");
    }

    @PostMapping("/confirm/mobile")
    public ResponseEntity<String> testConfirmMobile(@RequestBody RegistrationAttempt body) {
        notificationService.notifyMobileConfirmation(body, "123456", 5, 3);
        return ResponseEntity.ok("✅ Mobile confirmation notification sent.");
    }
}
