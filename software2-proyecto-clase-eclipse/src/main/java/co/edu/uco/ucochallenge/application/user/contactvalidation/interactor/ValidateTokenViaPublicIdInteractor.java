package co.edu.uco.ucochallenge.application.user.contactvalidation.interactor; // ✅ FIX: Define contract for public token validation flow

import java.util.UUID; // ✅ FIX: Support UUID identifiers for public verification

import co.edu.uco.ucochallenge.application.notification.VerificationAttemptResponseDTO; // ✅ FIX: Reuse attempt response structure for frontend feedback

public interface ValidateTokenViaPublicIdInteractor { // ✅ FIX: Expose interaction boundary for link-based verification

    VerificationAttemptResponseDTO execute(UUID tokenId); // ✅ FIX: Allow application layer to trigger verification via identifier
}
