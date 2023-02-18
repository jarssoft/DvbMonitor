
class DescriptorTL {

	final static int BYTESIZE = 2;
	
	public static byte[] buffer = new byte[BYTESIZE];

	public static int getTag() {
		return buffer[0];
	}

	public static int getLenght() {
		return buffer[1] & 0xFF;
	}
}