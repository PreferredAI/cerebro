package ai.preferred.cerebro.core.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
/**
 * DateUtils
 * 
 */
public class DateUtils {

	private static final Date DEFAULT_DATE = new Date(0);
	private static final String DEFAULT_DATE_MASK = "EEE HH:mm:ss dd MMM yyyy";
	private static final String SIMPLE_DATE_MASK = "yyyyMMdd";
	private static final String COMPACT_DATE_MASK = "yyyyMMddHHmmss";
	
	public static Date getDefaultDate() {
		return DEFAULT_DATE;
	}

	public static String getGMTString(Date value) {
		SimpleDateFormat formatter = new SimpleDateFormat(DEFAULT_DATE_MASK);
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return formatter.format(value);
	}

	public static String getString(Date value) {
		SimpleDateFormat formatter = new SimpleDateFormat(DEFAULT_DATE_MASK);
		return formatter.format(value);
	}
	
	public static String getString(Date value, String mask) {
		SimpleDateFormat formatter = new SimpleDateFormat(mask);
		return formatter.format(value);
	}
	
	public static String getString(Date value, String mask, TimeZone tz) {
		SimpleDateFormat formatter = new SimpleDateFormat(mask);
		formatter.setTimeZone(tz);
		return formatter.format(value);
	}
	
	public static String getSimpleString(Date value) {
		SimpleDateFormat formatter = new SimpleDateFormat(SIMPLE_DATE_MASK);
		return formatter.format(value);
	}
	
	public static String getCompactString(Date value) {
		SimpleDateFormat formatter = new SimpleDateFormat(COMPACT_DATE_MASK);
		return formatter.format(value);
	}

	public static Date parseDate(Object value) {
		return parseDate(value, DEFAULT_DATE);
	}

	public static Date parseDate(Object value, Date defaultValue) {
		return parseDate(value, DEFAULT_DATE_MASK, defaultValue);
	}

	public static Date parseDate(Object value, String mask) {
		return parseDate(value, mask, DEFAULT_DATE);
	}
	
	public static Date parseDate(Object value, String mask, Date defaultValue) {

		if (value == null)
			return defaultValue;

		if (value instanceof Date)
			return (Date) value;

		try {
			SimpleDateFormat formatter = new SimpleDateFormat(mask);
			return (Date) formatter.parse(StringUtils.parseString(value));
		} catch (Exception ex) {
			return defaultValue;
		}
	}
	
	public static Date parseDate(Object value, String mask, TimeZone tz) {
		return parseDate(value, mask, DEFAULT_DATE);
	}
	
	public static Date parseDate(Object value, String mask, TimeZone tz, Date defaultValue) {

		if (value == null)
			return defaultValue;

		if (value instanceof Date)
			return (Date) value;

		try {
			SimpleDateFormat formatter = new SimpleDateFormat(mask);
			formatter.setTimeZone(tz);
			return (Date) formatter.parse(StringUtils.parseString(value));
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public static Date addSeconds(Date date, int seconds) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.SECOND, seconds);
		return calendar.getTime();
	}
	
	public static double diffDays(Date d1, Date d2){
		double diff = d2.getTime() - d1.getTime();
		return NumberUtils.roundDouble(diff / (24 * 60 * 60 * 1000),0);
	}
}
