package PacketReader;
import java.nio.charset.StandardCharsets;

/** Reads Transport Stream. */
class DvbReader {
		
	final public static int TS_PACKET_SIZE = 188;		
	
	// Payload pointer
	
	final private static int PAYLOADPOINTER_SIZE = 1;
	
	private static byte[] bufferPayloadPointer = new byte[PAYLOADPOINTER_SIZE];
	
	public static int getPayloadPointer() {
		return bufferPayloadPointer[0] & 0xFF;
	}
	
	public static void readPayloadPointer() {
		Field.read(bufferPayloadPointer);
		//assert();
	}
	
	public static void toPayloadStart() {
		//System.out.println("(jump " + getPayloadPointer() + ", " + continues + ")");

		if(Id.containsNewUnit()) {
			if(getPayloadPointer()>0) {
				byte hopp[] = new byte[getPayloadPointer()];
				Field.read(hopp);
				//assert();		  
	
				String s = new String(hopp, StandardCharsets.UTF_8);
				System.out.println(s);
			}
		}
	}
	
	
	//Payload 
	
	final public static int PAYLOAD_SIZE = TS_PACKET_SIZE - Id.BYTESIZE;
	private static byte[] bufferPayload = new byte[PAYLOAD_SIZE];	
	
	/** Read next id. */
	public static boolean readPayload() {
		return Field.read(bufferPayload);
	}




}