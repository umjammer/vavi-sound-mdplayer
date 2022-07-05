package mdplayer.driver.sid.libsidplayfp.utils.md5;

/**
 * bit twiddling functions
 *
 * @author christopherbare@cbare.org
 * @date 2006.03.26
 */
public class Bits {


	/**
	 * Convert the given array of bytes to an int using the
	 * first byte as the most significant.
	 * @param b byte array
	 * @param offset offset into byte array
	 */
	public static int toInt(byte[] b, int offset) {
		return ((b[offset] & 0xFF) << 24)
		| ((b[offset+1] & 0xFF) << 16)
		| ((b[offset+2] & 0xFF) << 8)
		| (b[offset+3] & 0xFF);
	}

	/**
	 * convert a byte array to an int in least significant byte
	 * first order. {0x11, 0x22, 0x33, 0x44} becomes 0x44332211.
	 * @param offset offset into byte array
	 */
	public static int toBigEndianInt(byte[] bytes, int offset) {
		return ((bytes[offset+3] & 0xFF) << 24)
		| ((bytes[offset+2] & 0xFF) << 16)
		| ((bytes[offset+1] & 0xFF) << 8)
		| (bytes[offset] & 0xFF);
	}

	/**
	 * Convert the given long to an array of 8 bytes with the
	 * most significant byte first in the array.
	 */
	public static byte[] toBytes(long l) {
		byte[] b = new byte[8];
		for (int i=7; i>=0; i--) {
			// grab the least significant byte
			b[i] = (byte)(l & 0xFFL);
			// shift right by one byte
			l = l >> 8;
		}
		return b;
	}

	/**
	 * Convert the given long to an array of 8 bytes with the
	 * most significant byte first in the array.
	 */
	public static void toBytes(long l, byte[] b, int offset) {
		for (int i=7; i>=0; i--) {
			// grab the least significant byte
			b[offset + i] = (byte)(l & 0xFFL);
			// shift right by one byte
			l = l >> 8;
		}
	}

	/**
	 * Convert the int to 4 bytes with the most significant
	 * byte first.
	 */
	public static byte[] toBytes(int a) {
		byte[] b = new byte[4];
		for (int i=3; i>=0; i--) {
			b[i] = (byte)(a & 0x000000FF);
			a = a >>> 8;
		}
		return b;
	}

	/**
	 * convert an int to a byte array.
	 * @param b byte array to receive the value
	 * @param offset in b to receive the value
	 */
	public static void toBytes(int a, byte[] b, int offset) {
		for (int i=3; i>=0; i--) {
			b[offset+i] = (byte)(a & 0x000000FF);
			a = a >>> 8;
		}
	}

	/**
	 * converts an int to a 4 element byte array such that
	 * the most significant byte comes first.
	 * @param a value to convert
	 * @param bytes byte array to receive the value
	 * @param offset where to put the value in the byte array
	 */
	public static void toBytesBigEndian(int a, byte[] bytes, int offset) {
		bytes[offset+3]   = (byte)((a >>> 24) & 0x000000FF);
		bytes[offset+2] = (byte)((a >>> 16) & 0x000000FF);
		bytes[offset+1] = (byte)((a >>> 8) & 0x000000FF);
		bytes[offset] = (byte)((a) & 0x000000FF);
	}

	/**
	 * converts a long to a 8 element byte array such that
	 * the most significant byte comes first.
	 * @param a value to convert
	 * @param bytes destination of value
	 * @param offset where to put the value in the byte array
	 */
	public static void toBytesBigEndian(long a, byte[] bytes, int offset) {
		for (int i=0; i<8; i++) {
			bytes[offset+i] = (byte)(a & 0x000000FF);
			a = a >>> 8;
		}
	}

	/**
	 * Rotate the bits of the given int a by s positions.
	 */
    public static int leftRotate(int a, int s) {
    	s %= 32;
        return (a << s) | (a >>> (32-s));
    }

    /**
     * Reverses the order of the bytes in the given int.
     * Given 0x11223344 will return 0x44332211.
     */
    public static int rev(int x) {
        return (x>>>24) | ((x>>>8) &0x0000FF00) | ((x<<8) & 0x00FF0000) | (x<<24);
    }


	public static byte[] hexStringToByteArray(String hex) {
		int len = hex.length() / 2;
		byte[] bytes = new byte[len];

		for (int i=0; i<len; i++) {
			bytes[i] = Byte.parseByte(hex.substring(i*2,i*2+2));
		}
		return bytes;
	}

	public static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<b.length; i++) {
			sb.append(String.format("%02x",b[i]));
		}
		return sb.toString();
	}

    public static Object toHexString(int[] data) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<data.length; i++) {
            sb.append(String.format("%08x",data[i]));
        }
        return sb.toString();
    }

    public static String arrayToString(int[] a) {
        StringBuilder sb = new StringBuilder("[");
        if (a.length > 0) {
            sb.append(Integer.toHexString(rev(a[0])));
        }
        for (int i=1; i<a.length; i++) {
            sb.append(", ").append(Integer.toHexString(rev(a[i])));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String arrayToStringBigEndian(int[] a) {
        StringBuilder sb = new StringBuilder("[");
        if (a.length > 0) {
            sb.append(Integer.toHexString(a[0]));
        }
        for (int i=1; i<a.length; i++) {
            sb.append(", ").append(Integer.toHexString(a[i]));
        }
        sb.append("]");
        return sb.toString();
    }

	public static String arrayToString(byte[] a) {
		StringBuilder sb = new StringBuilder("[");
		if (a.length > 0) {
			sb.append(Integer.toHexString(a[0]));
		}
		for (int i=1; i<a.length; i++) {
			sb.append(", ").append(Integer.toHexString(a[i]));
		}
		sb.append("]");
		return sb.toString();
	}

}