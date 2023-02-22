package PacketReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Reads Transport Stream. */
public class DvbReader {
	
	// Packet data counter
	private static int dataleft = 0;
	  
	/** Returns amount of data left in packet in bytes. */
	public static int getDataleft() {
		return dataleft;
	}
	
	public static void reduceDataleft(int dl) {
		dataleft -= dl;
	}
	
	final public static int TS_PACKET_SIZE = 188;	
	
	// Payload pointer
	
	final public static int PAYLOADPOINTER_SIZE = 1;
	
	private static byte[] bufferPayloadPointer = new byte[PAYLOADPOINTER_SIZE];
	
	public static int getPayloadPointer() {
		return bufferPayloadPointer[0] & 0xFF;
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

	//Seeking packets
	
	static int[] pidfilter = new int[] {};
	
	public static void setFilter(int[] pids) {
		pidfilter = pids;
	}
	
	private static boolean contains(final int key) {
	    return Arrays.stream(pidfilter).anyMatch(i -> i == key);
	}
	
	/** Reads transport stream to begin of the first packet,
	 *  thats PID founds on array @pidfilter.
	 *  Returns PID. */  
	public static int seekPid() {

		//assert(dataleft==0): "dataleft must be zero, but dataleft = " + dataleft;
		
		while(true) {
			
			dataleft = DvbReader.TS_PACKET_SIZE;
			
			if(!Id.read()) {
				return 0;
			}	
			
			if(Id.hasSyncByte()){	
				
				int currentPID = Id.getPid();
								
				if(contains(currentPID)) {
					
					/*
					System.out.print("[Packet in "
							+ "0x" + Integer.toHexString(DvbReader.getReadOffset()-HEADER_SIZE)
							+ " - "+DvbReader.getIdAsHex()
							+ ", pid 0x" + Integer.toHexString(currentPID)							
							);
							*/
					
					//System.out.println(getIdAsHex() + " (pid=" + pid + ")");
					
					//Jump to Payload Pointer
					if(Id.containsNewUnit()) {
						assert(Field.read(bufferPayloadPointer));
						//System.out.print(", Paystart " + getPayloadPointer());
					}
					
					//System.out.println("] ");
					
					return currentPID;
				}
				
				
				//Read to end of packet
				//assert(readLeft());
				
				assert(Field.read(bufferPayload));

			}
			
			//assert(dataleft==PAYLOAD_SIZE): "dataleft must be zero, but dataleft = " + dataleft;

		}

	}

	//Data left
	
	private static byte[] left;

	public static boolean readLeft() {
		
		if(dataleft==0) {
			return true;
		}
		
		assert(dataleft >= 0): "dataleft must be zero or positive, but dataleft = " + dataleft;
		assert(dataleft < TS_PACKET_SIZE);
		
		left = new byte[dataleft];
		boolean ok = Field.read(left);
		
		String s = new String(left, StandardCharsets.UTF_8);
		//System.out.println((left[0]!=0xFF ? "" : "") 
		//		+ s + "");
		
		return ok;
		
	}

}