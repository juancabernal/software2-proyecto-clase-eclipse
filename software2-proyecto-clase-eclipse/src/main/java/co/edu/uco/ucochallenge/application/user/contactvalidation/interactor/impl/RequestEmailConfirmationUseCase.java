package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.impl;

import java.util.UUID;

import co.edu.uco.ucochallenge.application.Void;

public interface RequestEmailConfirmationUseCase {

    Void execute(UUID userId);
}