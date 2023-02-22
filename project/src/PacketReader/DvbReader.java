package PacketReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Reads Transport Stream. */
public class DvbReader {

	final public static int TS_PACKET_SIZE = 188;
	final public static int PAYLOAD_SIZE = TS_PACKET_SIZE - Id.BYTESIZE;
	final public static int PAYLOADPOINTER_SIZE = 1;
	
	private static int dataleft = 0;
	  
	public static int getDataleft() {
		return dataleft;
	}
	
	public static void reduceDataleft(int dl) {
		dataleft -= dl;
	}

	/** Buffers for using read transport stream. */
	
	private static byte[] bufferPayload = new byte[PAYLOAD_SIZE];
	private static byte[] bufferPayloadPointer = new byte[PAYLOADPOINTER_SIZE];
	
	private static boolean contains(final int[] arr, final int key) {
	    return Arrays.stream(arr).anyMatch(i -> i == key);
	}
	
	public static int getPayloadPointer() {
		return bufferPayloadPointer[0] & 0xFF;
	}

	/** Reads transport stream to begin of the first packet,
	 *  thats PID founds on array @pidfilter.*/  
	public static int seekPid(final int[] pidfilter) {

		//assert(dataleft==0): "dataleft must be zero, but dataleft = " + dataleft;
		
		while(true) {
			
			dataleft = DvbReader.TS_PACKET_SIZE;
			
			if(!Id.readId()) {
				return 0;
			}	
			
			if(Id.hasSyncByte()){	
				
				int currentPID = Id.getPid();
								
				if(contains(pidfilter, currentPID)) {
					
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

	private static byte[] left = new byte[Id.BYTESIZE];

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
	
	public static byte[] getLeft() {
		return left;
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
	
	/** Read next id. */
	public static boolean readPayload() {
		return Field.read(bufferPayload);
	}

}