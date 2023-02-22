package PacketReader;
import java.nio.charset.StandardCharsets;

/** Reads Transport Stream. */
public class DvbReader {
		
	final public static int TS_PACKET_SIZE = 188;	
	
	// Payload pointer
	
	final private static int PAYLOADPOINTER_SIZE = 1;
	
	private static byte[] bufferPayloadPointer = new byte[PAYLOADPOINTER_SIZE];
	
	public static int getPayloadPointer() {
		return bufferPayloadPointer[0] & 0xFF;
	}
	
	public static void readPayloadPointer() {
		assert(Field.read(bufferPayloadPointer));
	}
	
	public static void toPayloadStart() {
		//System.out.println("(jump " + getPayloadPointer() + ", " + continues + ")");

		if(Id.containsNewUnit()) {
			if(getPayloadPointer()>0) {
				byte hopp[] = new byte[getPayloadPointer()];
				assert(Field.read(hopp));		  
	
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



	//Data left
	
	private static int dataleft = 0;
	  
	/** Returns amount of data left in packet in bytes. */
	public static int getDataleft() {
		return dataleft;
	}
	
	public static void reduceDataleft(int dl) {
		dataleft -= dl;
	}
	
	public static void resetLeft() {
		dataleft = TS_PACKET_SIZE;
	}

	public static boolean readLeft() {
		
		if(dataleft==0) {
			return true;
		}
		
		assert(dataleft >= 0): "dataleft must be zero or positive, but dataleft = " + dataleft;
		assert(dataleft < TS_PACKET_SIZE);
		
		byte[] left = new byte[dataleft];
		boolean ok = Field.read(left);
		
		String s = new String(left, StandardCharsets.UTF_8);
		//System.out.println((left[0]!=0xFF ? "" : "") 
		//		+ s + "");
		
		return ok;
		
	}

}