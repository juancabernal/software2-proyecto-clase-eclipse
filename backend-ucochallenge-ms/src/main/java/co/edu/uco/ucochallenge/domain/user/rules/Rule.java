package co.edu.uco.ucochallenge.domain.user.rules;

import co.edu.uco.ucochallenge.application.Void;

public interface Rule {
    String getName();
    boolean evaluate(Void context);
}
