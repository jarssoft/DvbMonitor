package EPG;

public class Descriptor {

	public static final int COUNTRY_FIN = 0x46494E;
	public static final int COUNTRY_fin = 0x66696e;

	public static byte[] buffer;

	private static boolean isAsciiPrintable(char ch) {

		return ch >= 32 && ch < 127;
	}

	private static char decodechar(byte aByte) {

		char c = (char)aByte;

		if(isAsciiPrintable(c)) {
			return c;
		}else{
			return '.';
		}
	}

	public static String getDataAsText(byte[] bufferData) {

		StringBuilder result = new StringBuilder();
		for (byte aByte : bufferData) {
			result.append(decodechar(aByte));
		}
		return result.toString();

	}

	// packing an array of 4 bytes to an int, big endian, clean code
	protected static int fromByteArray(byte[] bytes, int i) {			
		return ((bytes[i+0] & 0xFF) << 16) | 
				((bytes[i+1] & 0xFF) << 8) | 
				((bytes[i+2] & 0xFF) << 0 ) ;
	}

}
