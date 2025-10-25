package co.edu.uco.ucochallenge.user.registeruser.application.interactor.usecase.rules.impl;

import co.edu.uco.ucochallenge.application.interactor.usecase.rules.Rule;
import co.edu.uco.ucochallenge.user.registeruser.application.interactor.usecase.rules.RegisterUserContext;

public class UniqueIDNumberIDType implements Rule<RegisterUserContext> {
    @Override
    public String getName() {
        return "UniqueIDNumberIDType";
    }

    @Override
    public boolean evaluate(RegisterUserContext context) {
        return false;
    }
}
