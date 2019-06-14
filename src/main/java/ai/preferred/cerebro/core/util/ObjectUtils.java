package ai.preferred.cerebro.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
/** 
 * ObjectUtils
 * @author ddle.2015
 *
 */
public class ObjectUtils {
	public static Object coalesce(Object... objArray) {
		for (Object obj : objArray)
			if (!isNull(obj))
				return obj;
		return null;
	}

	public static boolean isAnyNull(Object... objArray) {
		for (Object obj : objArray)
			if (isNull(obj))
				return true;
		return false;
	}

	public static boolean isAllNull(Object... objArray) {
		for (Object obj : objArray)
			if (!isNull(obj))
				return false;
		return true;
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

	public static Object getObject(int value) {
		return new Integer(value);
	}

	public static Object getObject(long value) {
		return new Long(value);
	}

	public static Object getObject(float value) {
		return new Float(value);
	}

	public static Object getObject(double value) {
		return new Double(value);
	}

	public static Object getObject(boolean value) {
		return new Boolean(value);
	}

	public static Object getObject(Object object) {
		return object;
	}

	public static Object getObject(Object object, Object defaultObject) {
		return object != null ? object : defaultObject;
	}	

	@SuppressWarnings("rawtypes")
	public static String normalizeClassName(Class type) {
		if (!type.isArray())
			return type.getName();

		StringBuffer className = new StringBuffer();
		try {
			className.append(getArrayBaseType(type).getName() + " ");
			for (int i = 0; i < getArrayDimensions(type); i++)
				className.append("[]");
		} catch (Exception e) { /* shouldn't happen */
		}

		return className.toString();
	}

	@SuppressWarnings("rawtypes")
	public static int getArrayDimensions(Class arrayClass) {
		if (!arrayClass.isArray())
			return 0;

		return arrayClass.getName().lastIndexOf('[') + 1;
	}

	@SuppressWarnings("rawtypes")
	public static Class getArrayBaseType(Class arrayClass) throws Exception {
		if (!arrayClass.isArray())
			throw new Exception("The class is not an array.");

		return arrayClass.getComponentType();
	}

	public static void save(Object obj, String fileName) throws IOException {
		if (!(obj instanceof Serializable)) {
			throw new IllegalArgumentException("Type " + obj.getClass()
					+ " is not serializable");
		}

		OutputStream out = null;
		ObjectOutputStream oout = null;
		try {
			out = new FileOutputStream(fileName);
			oout = new ObjectOutputStream(out);
			oout.writeObject(obj);
		} finally {
			oout.close();
		}
	}

	public static Object load(String fileName) throws IOException,
			ClassNotFoundException {
		Object obj = null;
		FileInputStream in = null;
		try {
			in = new FileInputStream(fileName);
			obj = deserialize(in);
		} finally {
			in.close();
		}

		return obj;
	}

	public static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bytes);
		out.writeObject(obj);
		out.close();
		byte[] ret = bytes.toByteArray();
		return ret;
	}

	public static Object deserialize(InputStream in) throws IOException,
			ClassNotFoundException {
		Object obj = null;
		ObjectInputStream oin = null;
		try {
			oin = new ObjectInputStream(in);
			obj = oin.readObject();
		} finally {
			oin.close();
		}

		return obj;
	}

	public static Object deserialize(byte[] bytes) throws Exception {
		if (bytes == null || bytes.length <= 0)
			return null;
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		Object obj = deserialize(is);
		is.close();
		return obj;
	}
}
