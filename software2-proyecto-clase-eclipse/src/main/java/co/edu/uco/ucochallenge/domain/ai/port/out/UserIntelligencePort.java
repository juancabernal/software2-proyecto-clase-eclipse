package co.edu.uco.ucochallenge.domain.ai.port.out;

import co.edu.uco.ucochallenge.domain.user.model.User;

public interface UserIntelligencePort {

    void publishUserRegistrationInsight(User user);
}
