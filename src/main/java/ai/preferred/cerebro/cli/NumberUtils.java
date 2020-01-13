package ai.preferred.cerebro.cli;

import java.text.DecimalFormat;
import java.text.NumberFormat;
/**
 * NumberUtils
 * @author ddle.2015
 *
 */
public class NumberUtils {
	private static final int DEFAULT_INT = 0;
	private static final long DEFAULT_LONG = 0L;
	private static final float DEFAULT_FLOAT = 0F;
	private static final double DEFAULT_DOUBLE = 0D;

	public static boolean isZero(double value) {
		return Math.round(value) == 0;
	}

	public static boolean isZero(double value, int d) {
		return d != 0 ? Math.round(value * Math.pow(10, d)) == 0
				: isZero(value);
	}

	public static int getDefaultInt() {
		return DEFAULT_INT;
	}

	public static long getDefaultLong() {
		return DEFAULT_LONG;
	}

	public static float getDefaultFloat() {
		return DEFAULT_FLOAT;
	}

	public static double getDefaultDouble() {
		return DEFAULT_DOUBLE;
	}

	public static Integer getObject(int value) {
		return new Integer(value);
	}

	public static Long getObject(long value) {
		return new Long(value);
	}

	public static Float getObject(float value) {
		return new Float(value);
	}

	public static Double getObject(double value) {
		return new Double(value);
	}

	public static Boolean getObject(boolean value) {
		return new Boolean(value);
	}

	public static String formatNumber(String pattern, double value) {
		return new DecimalFormat(pattern).format(value);
	}

	public static String formatNumber(String pattern, long value) {
		return new DecimalFormat(pattern).format(value);
	}

	public static String formatNumber(String pattern, Object value) {
		return new DecimalFormat(pattern).format(value);
	}

	public static int parseInt(Object value) {
		return parseInt(value, DEFAULT_INT);
	}

	public static int parseInt(Object value, int defaultValue) {
		if (value == null)
			return defaultValue;

		if (value instanceof Integer)
			return ((Integer) value).intValue();

		try {
			return NumberFormat.getIntegerInstance().parse(value.toString())
					.intValue();
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public static long parseLong(Object value) {
		return parseLong(value, DEFAULT_LONG);
	}

	public static long parseLong(Object value, long defaultValue) {
		if (value == null)
			return defaultValue;

		if (value instanceof Long)
			return ((Long) value).longValue();

		try {
			return NumberFormat.getNumberInstance().parse(value.toString())
					.longValue();
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public static float parseFloat(Object value) {
		return parseFloat(value, DEFAULT_FLOAT);
	}

	public static float parseFloat(Object value, float defaultValue) {
		if (value == null)
			return defaultValue;

		if (value instanceof Float)
			return ((Float) value).floatValue();

		try {
			return Float.parseFloat(value.toString());
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public static double parseDouble(Object value) {
		return parseDouble(value, DEFAULT_DOUBLE);
	}

	public static double parseDouble(Object value, double defaultValue) {
		if (value == null)
			return defaultValue;

		if (value instanceof Double)
			return ((Double) value).doubleValue();

		try {
			return Double.parseDouble(value.toString());
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public static boolean areEqual(Integer value1, Integer value2) {
		return parseInt(value1) == parseInt(value2);
	}

	public static int[] parseAsIntArray(Object[] objArray){
		int[] intArray = new int[objArray.length];

		for(int i = 0; i < intArray.length; i++){
			intArray[i] = parseInt(objArray[i]);
		}
		return intArray;
	}

	public static double[] parseAsDoubleArray(Object[] objArray){
		double[] doubleArray = new double[objArray.length];

		for(int i = 0; i < objArray.length; i++){
			doubleArray[i] = parseDouble(objArray[i]);
		}
		return doubleArray;
	}

	public static long[] parseAsLongArray(Object[] objArray){
		long[] longArray = new long[objArray.length];

		for(int i = 0; i < objArray.length; i++){
			longArray[i] = parseLong(objArray[i]);
		}
		return longArray;
	}

	public static double roundDouble(Double value, int numDecimal){
		if(value == null) return 0;
		if(numDecimal == -1) return value;
		double de = Math.pow(10, numDecimal);
		return (double)Math.round(value*de)/de;
	}

	public static double logb(double a, double b) {
		return Math.log(a) / Math.log(b);
	}

	public static double log2(double value){
		return logb(value, 2);
	}

	public static boolean isNumber(String s){
		for(int i = 0; i < s.length(); i++){
			char c = s.charAt(i);
			if((int) c < 48 || (int) c > 57 ) return false;
		}
		return true;
	}
}
