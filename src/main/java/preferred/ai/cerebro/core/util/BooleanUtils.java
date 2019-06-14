package preferred.ai.cerebro.core.util;
/**
 * BooleanUtils
 * @author ddle.2015
 *
 */
public class BooleanUtils {
	private static final boolean DEFAULT_BOOLEAN = false;

	public static boolean getDefaultBoolean() {
		return DEFAULT_BOOLEAN;
	}

	public static Boolean getObject(boolean value) {
		return new Boolean(value);
	}

	public static boolean parseBoolean(Object value) {
		return parseBoolean(value, DEFAULT_BOOLEAN);
	}

	public static boolean parseBoolean(Object value, boolean defaultValue) {
		if (value == null)
			return defaultValue;

		if (value instanceof Boolean)
			return ((Boolean) value).booleanValue();

		try {
			return new Boolean(value.toString()).booleanValue();
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public static boolean areEqual(Boolean value1, Boolean value2) {
		return parseBoolean(value1) == parseBoolean(value2);
	}

}
