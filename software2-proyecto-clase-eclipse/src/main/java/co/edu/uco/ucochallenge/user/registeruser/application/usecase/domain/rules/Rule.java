package co.edu.uco.ucochallenge.user.registeruser.application.usecase.domain.rules;

import co.edu.uco.ucochallenge.application.Void;

public interface Rule {
    String getName();
    boolean evaluate(Void context);
}
