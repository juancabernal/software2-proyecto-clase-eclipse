package co.edu.uco.ucochallenge.application.user.searchUsers;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;
import org.springframework.util.DigestUtils;

public final class CacheKeyUtil {

    private CacheKeyUtil() {}

    public static String canonicalKey(
            UUID idType,
            String idNumber,
            String firstName,
            String firstSurname,
            UUID homeCity,
            String email,
            String mobileNumber,
            String q,
            int page,
            int size
    ) {
        // Normaliza nulos y espacios para que "  " == "".
        String k = String.join("|",
                safe(idType),
                safe(idNumber),
                safe(firstName),
                safe(firstSurname),
                safe(homeCity),
                safe(email),
                safe(mobileNumber),
                "p=" + page,
                "s=" + size,
                safe(q)
        );
        // MD5 para que la key en Redis sea corta y estable
        return DigestUtils.md5DigestAsHex(k.getBytes(StandardCharsets.UTF_8));
    }

    private static String safe(String s) {
        return TextHelper.getDefaultWithTrim(s);
    }

    private static String safe(UUID u) {
        return UUIDHelper.getDefault(u).toString();
    }
}
