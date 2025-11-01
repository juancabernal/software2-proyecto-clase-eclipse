package co.edu.uco.ucochallenge.crosscuting.security;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import co.edu.uco.ucochallenge.crosscuting.helper.ObjectHelper;

public final class SanitizationHelper {

    private static final Safelist SAFE_LIST = Safelist.none();

    private SanitizationHelper() {
    }

    public static String sanitize(final String value) {
        final String safeValue = ObjectHelper.getDefault(value, "");
        return Jsoup.clean(safeValue, SAFE_LIST).replaceAll("\\s+", " ").trim();
    }
}
