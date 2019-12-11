package ai.preferred.cerebro.core.jpa.utils;

import java.util.UUID;

public class UUIDUtils {
	private static final String DEFAULT_UUID_STRING = "00000000-0000-0000-0000-000000000000";
	private static final UUID DEFAULT_UUID = UUID
			.fromString("00000000-0000-0000-0000-000000000000");

	public static String getDefaultUUIDString() {
		return DEFAULT_UUID_STRING;
	}

	public static UUID getDefaultUUID() {
		return DEFAULT_UUID;
	}

	public static UUID newUUID() {
		return UUID.randomUUID();
	}

	public static String newUUIDString() {
		return String.valueOf(newUUID().toString());
	}

	public static String newPlainUUIDString() {
		return newUUIDString().replace("-", "");
	}

	public static byte[] newUUIDByteArray() {
		return toBytes(newUUID());
	}

	public static UUID parseUUID(Object value) {
		return parseUUID(value, DEFAULT_UUID);
	}

	public static UUID parseUUID(Object value, UUID defaultValue) {
		if (value == null)
			return defaultValue;

		if (value instanceof UUID)
			return (UUID) value;

		try {
			return UUID.fromString(value.toString());
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public static byte[] toBytes(UUID uuid) {
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();

		return toBytes(msb, lsb);
	}

	private static byte[] toBytes(long msb, long lsb) {

		byte[] buffer = new byte[16];

		for (int i = 0; i < 8; i++) {
			buffer[i] = (byte) (msb >>> 8 * (7 - i));
		}
		for (int i = 8; i < 16; i++) {
			buffer[i] = (byte) (lsb >>> 8 * (7 - i));
		}

		return buffer;
	}

}