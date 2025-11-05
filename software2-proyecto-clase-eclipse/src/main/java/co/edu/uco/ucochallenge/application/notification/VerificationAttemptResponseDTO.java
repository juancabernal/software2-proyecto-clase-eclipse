package co.edu.uco.ucochallenge.application.notification; // ✅ FIX: Extend response to share verification identifier

import java.util.UUID; // ✅ FIX: Bring UUID support for verification identifiers

public record VerificationAttemptResponseDTO( // ✅ FIX: Provide verification identifier to client attempts
        boolean success, // ✅ FIX: Maintain success flag semantics
        boolean expired, // ✅ FIX: Preserve expiration state feedback
        int attemptsRemaining, // ✅ FIX: Keep attempt counter for retry logic
        boolean contactConfirmed, // ✅ FIX: Maintain individual contact confirmation status
        boolean allContactsConfirmed, // ✅ FIX: Preserve aggregated confirmation state
        String message, // ✅ FIX: Keep localized feedback for the user
        UUID verificationId) { // ✅ FIX: Surface verification identifier for follow-up actions
}
