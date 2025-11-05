package co.edu.uco.ucochallenge.application.user.contactvalidation.dto; // ✅ FIX: Introduce DTO for link-based verification payloads

import java.util.UUID; // ✅ FIX: Represent verification identifiers with UUID

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper; // ✅ FIX: Reuse helper to sanitize incoming token values
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper; // ✅ FIX: Normalize token identifiers before use

public record VerificationLinkRequestDTO(String token) { // ✅ FIX: Model incoming token from verification link

    public UUID sanitizedTokenId() { // ✅ FIX: Safely transform raw token into UUID
        final String normalized = TextHelper.getDefaultWithTrim(token); // ✅ FIX: Trim whitespace from raw token
        if (TextHelper.isEmpty(normalized)) { // ✅ FIX: Handle missing token safely
            return null; // ✅ FIX: Represent absence of token with null identifier
        }
        try { // ✅ FIX: Guard against malformed UUID input
            final UUID parsed = UUIDHelper.getFromString(normalized); // ✅ FIX: Parse token string into UUID instance
            return UUIDHelper.getDefault().equals(parsed) ? null : parsed; // ✅ FIX: Reject default UUID placeholders
        } catch (final IllegalArgumentException exception) { // ✅ FIX: Catch invalid UUID formatting
            return null; // ✅ FIX: Fallback to null when parsing fails
        }
    }
}
