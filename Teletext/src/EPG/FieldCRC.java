package EPG;
class FieldCRC {

	final static int CRC_SIZE = 4;
	
	private static byte[] buffer = new byte[CRC_SIZE];

	static boolean read() {
		return Reader.readFromPackets(buffer, 0);
	}

}