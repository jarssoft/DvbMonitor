package PacketReader;

public class Id {
	
	final public static int BYTESIZE = 4;
	
	private static byte[] buffer = new byte[BYTESIZE];
	
	/** Read next id. */
	public static boolean read() {
		return Field.read(buffer);
	}

	/** Get id as hexadecimal string. */
	final public static String getIdAsHex() {
		return Field.byteBuffertoHex(buffer);
	}
	
	final public static int getContinCounter() {
		return buffer[3] & 0x0F;
	}
	
	final public static boolean hasSyncByte() {
		return buffer[0] == 0x47;
	}
	
	public static int getPacketNumber() {
		return buffer[3] & 0xf;
	}
	
	final public static int getPid() {
		return ((0b00011111 & (byte)buffer[1])<<8) | (0xFF & buffer[2]);
	};
	
	/** Set when this packet contains the first byte of a new payload unit. */
	public static boolean containsNewUnit() {
		return (buffer[1] & 0b01000000) != 0;
	}
	
}