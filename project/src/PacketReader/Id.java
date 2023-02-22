package PacketReader;

public class Id{
	
	final public static int BYTESIZE = 4;
	
	private static byte[] bufferId = new byte[BYTESIZE];
	
	/** Read next id. */
	public static boolean readId() {
		return Field.read(bufferId);
	}

	/** Get id as hexadecimal string. */
	final public static String getIdAsHex() {
		return Field.byteBuffertoHex(bufferId);
	}
	
	final public static int getContinCounter() {
		return bufferId[3] & 0x0F;
	}
	
	final public static boolean hasSyncByte() {
		return bufferId[0] == 0x47;
	}
	
	public static int getPacketNumber() {
		return bufferId[3] & 0xf;
	}
	
	final public static int getPid() {
		return ((0b00011111 & (byte)bufferId[1])<<8) | (0xFF & bufferId[2]);
	};
	
	/** Set when this packet contains the first byte of a new payload unit. */
	public static boolean containsNewUnit() {
		return (bufferId[1] & 0b01000000) != 0;
	}
	
}