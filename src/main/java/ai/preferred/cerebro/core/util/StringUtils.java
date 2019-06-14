package ai.preferred.cerebro.core.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
/**
 * StringUtils
 * @author ddle.2015
 *
 */
public class StringUtils {
	private static final String DEFAULT_STRING = "";
	private static final int STRING_BUFFER_SIZE = 1024;

	// Null or Empty -Coalescing
	public static Object coalesce(Object... objArray) {
		for (Object obj : objArray)
			if (!isNullOrEmpty(obj))
				return obj;
		return DEFAULT_STRING;
	}

	public static int indexOf(Object container, Object token) {
		return parseString(container).indexOf(parseString(token));
	}

	public static boolean contains(Object container, Object token) {
		return indexOf(container, token) >= 0;
	}

	public static String concat(Object... values) {
		StringBuilder builder = new StringBuilder();
		for (Object val : values)
			builder.append(parseString(val));
		return builder.toString();
	}

	public static boolean in(Object obj, Object[] values) {
		String str = parseString(obj);
		for (Object val : values)
			if (str.equals(parseString(val)))
				return true;
		return false;
	}

	public static boolean inIgnoreCase(Object obj, Object[] values) {
		String str = parseString(obj);
		for (Object val : values)
			if (str.equalsIgnoreCase(parseString(val)))
				return true;
		return false;
	}

	public static boolean areEqual(Object... vals) {
		if (vals.length < 2)
			return true;
		String reference = parseString(vals[0]);
		for (Object val : vals)
			if (!parseString(val).equals(reference))
				return false;
		return true;
	}

	public static boolean areEqualIgnoreCase(Object... vals) {
		if (vals.length < 2)
			return true;
		String reference = parseString(vals[0]);
		for (Object val : vals)
			if (!parseString(val).equalsIgnoreCase(reference))
				return false;
		return true;
	}

	public static int indexOf(String str, int strStartIndex, String search,
			boolean caseSensitivity) {
		final int endIndex = str.length() - search.length();
		if (endIndex >= strStartIndex) {
			for (int i = strStartIndex; i <= endIndex; i++) {
				if (regionMatches(str, i, search, caseSensitivity)) {
					return i;
				}
			}
		}
		return -1;
	}

	public static boolean regionMatches(String str, int strStartIndex,
			String search, boolean caseSensitivity) {
		return str.regionMatches(!caseSensitivity, strStartIndex, search, 0,
				search.length());
	}

	public static boolean isNull(Object value) {
		return (value == null);
	}

	public static boolean isNullOrEmpty(Object value) {
		if (value == null)
			return true;

		if (value instanceof String)
			return "".equals(value);

		return "".equals(value.toString());
	}

	public static boolean isNullOrWhiteSpace(Object value) {
		if (value == null)
			return true;

		if (value instanceof String)
			return "".equals(((String) value).trim());

		return value.toString() == null || "".equals(value.toString().trim());
	}

	public static String getDefaultString() {
		return DEFAULT_STRING;
	}

	public static String parseString(Object value) {
		return parseString(value, DEFAULT_STRING);
	}

	public static String parseString(Object value, String defaultValue) {
		if (value == null)
			return defaultValue;

		if (value instanceof String)
			return (String) value;

		try {
			return value.toString();
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public static String trim(Object value) {
		return parseString(value).trim();
	}

	public static String trimToSize(String s, int length) {
		return s != null && s.length() > length ? s.substring(0, length) : s;
	}

	public static String quote(String s) {
		int i = s.indexOf("\\E");
		if (i == -1)
			return (new StringBuffer()).append("\\Q").append(s).append("\\E")
					.toString();
		StringBuffer stringbuilder = new StringBuffer(s.length() * 2);
		stringbuilder.append("\\Q");
		i = 0;
		int j = 0;
		while ((i = s.indexOf("\\E", j)) != -1) {
			stringbuilder.append(s.substring(j, i));
			j = i + 2;
			stringbuilder.append("\\E\\\\E\\Q");
		}
		stringbuilder.append(s.substring(j, s.length()));
		stringbuilder.append("\\E");
		return stringbuilder.toString();
	}

	public static String implode(Object[] elements, String delimiter) {
		if (elements.length <= 0)
			return "";

		StringBuffer buffer = new StringBuffer("");
		for (int i = 0; i < elements.length - 1; i++) {
			buffer.append(parseString(elements[i]) + delimiter);
		}
		buffer.append(parseString(elements[elements.length - 1]));
		return buffer.toString();
	}

	public static String implode(Object[] elements) {
		return implode(elements, ", ");
	}

	public static String capitalize(String str) {
		return capitalize(str, null);
	}

	public static String capitalize(String str, char[] delimiters) {
		int delimLen = (delimiters == null ? -1 : delimiters.length);
		if (str == null || str.length() == 0 || delimLen == 0) {
			return str;
		}
		int strLen = str.length();
		StringBuffer buffer = new StringBuffer(strLen);
		boolean capitalizeNext = true;
		for (int i = 0; i < strLen; i++) {
			char ch = str.charAt(i);

			if (isDelimiter(ch, delimiters)) {
				buffer.append(ch);
				capitalizeNext = true;
			} else if (capitalizeNext) {
				buffer.append(Character.toTitleCase(ch));
				capitalizeNext = false;
			} else {
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}

	public static String capitalizeFully(String str) {
		return capitalizeFully(str, null);
	}

	public static String capitalizeFully(String str, char[] delimiters) {
		int delimLen = (delimiters == null ? -1 : delimiters.length);
		if (str == null || str.length() == 0 || delimLen == 0) {
			return str;
		}
		str = str.toLowerCase();
		return capitalize(str, delimiters);
	}

	public static String uncapitalize(String str) {
		return uncapitalize(str, null);
	}

	public static String uncapitalize(String str, char[] delimiters) {
		int delimLen = (delimiters == null ? -1 : delimiters.length);
		if (str == null || str.length() == 0 || delimLen == 0) {
			return str;
		}
		int strLen = str.length();
		StringBuffer buffer = new StringBuffer(strLen);
		boolean uncapitalizeNext = true;
		for (int i = 0; i < strLen; i++) {
			char ch = str.charAt(i);

			if (isDelimiter(ch, delimiters)) {
				buffer.append(ch);
				uncapitalizeNext = true;
			} else if (uncapitalizeNext) {
				buffer.append(Character.toLowerCase(ch));
				uncapitalizeNext = false;
			} else {
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}

	private static boolean isDelimiter(char ch, char[] delimiters) {
		if (delimiters == null) {
			return Character.isWhitespace(ch);
		}
		for (int i = 0, isize = delimiters.length; i < isize; i++) {
			if (ch == delimiters[i]) {
				return true;
			}
		}
		return false;
	}

	public static String[] explode(String str, String regex){
		if (str == null || str.length() == 0) {
			return null;
		}
		ArrayList<String> strings = new ArrayList<String>();

		String[] els = str.split(regex);
		for(String el: els){
			el = el.trim();
			if(!isNullOrWhiteSpace(el)) 
				strings.add(el);
		}

		return strings.toArray(new String[strings.size()]);
	}

	public static String[] explode(String str, int ch) {
		return explode(str, ch, false);
	}

	public static String[] explode(String str, int ch, boolean respectEmpty) {
		if (str == null || str.length() == 0) {
			return new String[0];
		}

		ArrayList<String> strings = new ArrayList<String>();
		int pos;
		int lastpos = 0;

		// add snipples
		while ((pos = str.indexOf(ch, lastpos)) >= 0) {
			if (pos - lastpos > 0 || respectEmpty) {
				strings.add(str.substring(lastpos, pos));
			}
			lastpos = pos + 1;
		}
		// add rest
		if (lastpos < str.length()) {
			strings.add(str.substring(lastpos));
		} else if (respectEmpty && lastpos == str.length()) {
			strings.add("");
		}

		// return string array
		return strings.toArray(new String[strings.size()]);
	}

	public static String[] tokenize(String str) {
		if (str == null)
			return new String[0];

		String[] tokens = str.split("[ \\t{}():;._,\\-! \"?\n]");
		ArrayList<String> list = new ArrayList<String>();

		for (String token : tokens) {
			if (!isNullOrWhiteSpace(token) && !"rrb".equalsIgnoreCase(token))
				list.add(token);
		}

		return list.toArray(new String[0]);
	}

	public static String subTokens(String[] tokens, int beginIndex, int endIndex) {
		return subTokens(tokens, beginIndex, endIndex, " ");
	}

	public static String subTokens(String[] tokens, int beginIndex,
			int endIndex, String separator) {
		if (tokens == null || beginIndex < 0 || endIndex < 0)
			return "";

		StringBuilder builder = new StringBuilder();

		for (int i = beginIndex; i < tokens.length && i < endIndex; i++) {
			builder.append(tokens[i]);
			if (i != tokens.length - 1 && i != endIndex - 1) {
				builder.append(separator);
			}
		}

		return builder.toString().trim();
	}

	public static void save(Object obj, String fileName) throws IOException {
		OutputStream out = null;
		OutputStreamWriter owriter = null;
		String str = parseString(obj);
		try {
			out = new FileOutputStream(fileName);
			owriter = new OutputStreamWriter(out, "UTF8");
			owriter.write(str, 0, str.length());
		} finally {
			owriter.close();
		}
	}

	public static String load(String fileName) throws IOException {
		char[] buffer = new char[STRING_BUFFER_SIZE];
		InputStream in = null;
		InputStreamReader ireader = null;
		StringBuilder str = new StringBuilder();
		int len = 0;
		try {
			in = new FileInputStream(fileName);
			ireader = new InputStreamReader(in, "UTF8");
			while ((len = ireader.read(buffer, 0, STRING_BUFFER_SIZE)) != -1) {
				str.append(buffer, 0, len);
			}
		} finally {
			ireader.close();
		}
		return str.toString();
	}

	public static List<String> toList(String str, String separator){
		String[] strs = str.split(separator);
		return toList(strs);
	}

	public static List<String> toList(String ...strs){
		if(strs == null || strs.length == 0) return null;
		List<String> holder = new ArrayList<String>();
		for(String sel: strs) holder.add(sel);
		return holder;
	}

	public static String indent(String prefix, int maxNumSpace){
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < maxNumSpace - prefix.length(); i++)
			buffer.append(" ");
		return buffer.toString();
	}
}
