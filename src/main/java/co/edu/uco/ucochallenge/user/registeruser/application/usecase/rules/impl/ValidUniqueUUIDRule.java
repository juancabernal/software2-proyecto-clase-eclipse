package co.edu.uco.ucochallenge.user.registeruser.application.usecase.rules.impl;

import co.edu.uco.ucochallenge.user.registeruser.application.usecase.rules.Rule;
import co.edu.uco.ucochallenge.user.registeruser.application.usecase.rules.context.ContextRegisterUser;

public class ValidUniqueUUIDRule implements Rule {

    @Override
    public String getName() {
        return "ValidUUIDFormat";
    }

    @Override
    public boolean evaluate(ContextRegisterUser context) {
            //Verificación con el repository
        return true;
    }
}
