package co.edu.uco.ucochallenge.crosscuting.helper;

public final class TextHelper {

	public static final String EMPTY = "";

	private TextHelper() {
	}

	public static String getDefault() {
		return EMPTY;
	}

       public static String getDefault(final String value) {
               return ObjectHelper.getDefault(value, getDefault());
       }

       public static String getDefault(final String value, final String defaultValue) {
               final String safeDefault = ObjectHelper.getDefault(defaultValue, getDefault());
               final String safeValue = getDefault(value);
               return safeValue.isEmpty() ? safeDefault : safeValue;
       }

	public static String getDefaultWithTrim(final String value) {
		return getDefault(value).trim();
	}

	public static boolean isEmpty(final String value) {
		return getDefaultWithTrim(value).isEmpty();
	}

}
