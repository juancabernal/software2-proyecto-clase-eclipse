package co.edu.uco.ucochallenge.user.registeruser.application.interactor.usecase.rules.impl;

import co.edu.uco.ucochallenge.application.interactor.usecase.rules.Rule;
import co.edu.uco.ucochallenge.user.registeruser.application.interactor.usecase.rules.RegisterUserContext;

public class UniqueEmail implements Rule<RegisterUserContext> {
    @Override
    public String getName() {
        return "UniqueEmail";
    }

    @Override
    public boolean evaluate(RegisterUserContext context) {

        return false;
    }
}
