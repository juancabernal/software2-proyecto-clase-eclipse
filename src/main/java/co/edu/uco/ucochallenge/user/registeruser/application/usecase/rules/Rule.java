package co.edu.uco.ucochallenge.user.registeruser.application.usecase.rules;

import co.edu.uco.ucochallenge.user.registeruser.application.usecase.rules.context.ContextRegisterUser;

public interface Rule {
    String getName();
    boolean evaluate(ContextRegisterUser context);
}
