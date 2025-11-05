package co.edu.uco.ucochallenge.application.notification;

import java.util.UUID; // ✅ FIX: Keep UUID dependency for verification identifiers

public record ConfirmationResponseDTO( // ✅ FIX: Expand response contract with verification identifier
        int remainingSeconds, // ✅ FIX: Preserve remaining time for frontend countdown
        UUID tokenId, // ✅ FIX: Maintain legacy token identifier for backward compatibility
        UUID verificationId) { // ✅ FIX: Expose verification identifier to the frontend
}

