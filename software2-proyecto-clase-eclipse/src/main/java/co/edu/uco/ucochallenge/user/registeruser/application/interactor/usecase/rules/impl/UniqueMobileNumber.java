package co.edu.uco.ucochallenge.user.registeruser.application.interactor.usecase.rules.impl;

import co.edu.uco.ucochallenge.application.interactor.usecase.rules.Rule;
import co.edu.uco.ucochallenge.user.registeruser.application.interactor.usecase.rules.RegisterUserContext;

public class UniqueMobileNumber implements Rule<RegisterUserContext> {
    @Override
    public String getName() {
        return "UniqueMobileNumber";
    }

    @Override
    public boolean evaluate(RegisterUserContext context) {
        return false;
    }
}
