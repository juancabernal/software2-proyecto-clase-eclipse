package co.edu.uco.ucochallenge.application.user.contactvalidation.usecase.impl; // ✅ FIX: Provide implementation for public verification flow

import java.util.UUID; // ✅ FIX: Accept UUID-based verification identifiers

import org.springframework.stereotype.Service; // ✅ FIX: Register use case as Spring service

import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO; // ✅ FIX: Reuse notification attempt response for frontend status
import co.edu.uco.ucochallenge.application.notification.VerificationTokenService; // ✅ FIX: Delegate verification to shared token service
import co.edu.uco.ucochallenge.application.user.contactvalidation.interactor.ValidateTokenViaPublicIdInteractor; // ✅ FIX: Implement new interactor contract

@Service // ✅ FIX: Enable dependency injection for the new use case
public class ValidateTokenViaPublicIdUseCaseImpl implements ValidateTokenViaPublicIdInteractor { // ✅ FIX: Connect controller with verification service via new use case

    private final VerificationTokenService verificationTokenService; // ✅ FIX: Hold reference to shared token operations

    public ValidateTokenViaPublicIdUseCaseImpl(final VerificationTokenService verificationTokenService) { // ✅ FIX: Inject shared token service into use case
        this.verificationTokenService = verificationTokenService; // ✅ FIX: Assign injected service for later reuse
    }

    @Override
    public VerificationAttemptResponseDTO execute(final UUID tokenId) { // ✅ FIX: Implement link-based verification orchestration
        return verificationTokenService.validateTokenViaPublicId(tokenId); // ✅ FIX: Delegate verification logic to shared service method
    }
}
