import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Reads Transport Stream. */
public class DvbReader {

	final public static int TS_PACKET_SIZE = 188;
	final public static int HEADER_SIZE = 4;
	final public static int PAYLOAD_SIZE = TS_PACKET_SIZE - HEADER_SIZE;
	final public static int PAYLOADPOINTER_SIZE = 1;
	
	private static int currentPID = 0;

	public static int getCurrentPID() {
		assert currentPID>0 && currentPID<10000;
		return currentPID;
	}

	private static int dataleft=0;
	  
	public static int getDataleft() {
		return dataleft;
	}
	
	public static void reduceDataleft(int dl) {
		dataleft -= dl;
	}

	/** Read byte-buffer. Return true if succeed, otherwise false. */
	public static boolean read(byte[] buffer) {
		
		assert(buffer.length > 0): "buffer.length must be greater than zero, but buffer.length = " + buffer.length;
		assert(buffer.length <= getDataleft()): "No enought data in this packet. getDataleft() = " + getDataleft();
		
		try {
			
			dataleft -= buffer.length;
			
			int readed=System.in.read(buffer);
			//System.out.println(readed);
			//buffer = System.in.readNBytes(buffer.length);
			//assert(readed==buffer.length):"Only "+readed+" read. Must read "+buffer.length;
			if(readed<buffer.length) {
				//System.out.println("Only "+readed+" readed.");
				  assert(readed>=0) : "End of data!";
				  
				int second = System.in.read(buffer, readed, buffer.length-readed);
				//System.out.println("Second try: "+second);
				readed+=second;
				//System.out.println("Together "+readed+" readed.");
			}
			return readed==buffer.length;
			
		} catch (IOException e) {
			
			e.printStackTrace();
			return false;
			
		}
		
	}

	/** Returns hexadecimal presentation of byte-buffer @buffer. */
	public static String byteBuffertoHex(byte[] buffer) {
		StringBuilder result = new StringBuilder();
		for(byte aByte : buffer) {
			result.append(String.format("%02x", aByte)+" ");
		}
		return result.toString(); 
	}

	/** Buffers for using read transport stream. */
	private static byte[] bufferId = new byte[HEADER_SIZE];
	private static byte[] bufferPayload = new byte[PAYLOAD_SIZE];
	private static byte[] bufferPayloadPointer = new byte[PAYLOADPOINTER_SIZE];
	
	/** Read next id.*/
	public static boolean readId() {

		return read(bufferId);
	}

	/** Get id as hexadecimal string.*/
	public static String getIdAsHex() {
		return byteBuffertoHex(bufferId);
	}
	
	private static boolean contains(final int[] arr, final int key) {
	    return Arrays.stream(arr).anyMatch(i -> i == key);
	}

	private static boolean hasSyncByte() {
		return bufferId[0] == 0x47;
	}
	
	private static int getPacketNumber() {
		return bufferId[3] & 0xf;
	}
	
	private static int getPid() {
		return ((0b00011111 & (byte)bufferId[1])<<8) | (0xFF & bufferId[2]);
	};
	
	private static boolean hasPayloadPointer() {
		return (bufferId[1] & 0b01000000) != 0;
	}
	
	private static int getPayloadPointer() {
		return bufferPayloadPointer[0] & 0xFF;
	}

	/** Reads transport stream to begin of the first packet,
	 *  thats PID founds on array @pidfilter.*/  
	public static int seekPid(final int[] pidfilter) {

		//assert(dataleft==0): "dataleft must be zero, but dataleft = " + dataleft;
		boolean continues=true;
		
		while(true) {
			
			dataleft = DvbReader.TS_PACKET_SIZE;
			
			if(!readId()) {
				return 0;
			}	
			
			if(hasSyncByte()){	
				
				currentPID = getPid();
				
				if(contains(pidfilter, currentPID)) {
					//System.out.println(getIdAsHex() + " (pid=" + pid + ")");
					if(hasPayloadPointer()) {
						
						assert(read(bufferPayloadPointer));
					}					
					break;
				}
				
				continues=false;
			}

		}

		return currentPID;
	}

	/** Transport Stream Monitor */
	public static void maind(String[] args) {
		
		int packets=0;
		int[] lastpn=new int[8191+1];
		
		dataleft=Integer.MAX_VALUE;
		while(readId() && packets<500000) {
			
			if(hasSyncByte() && getPid()==0x12) {
					
				System.out.print((packets++) + "  ");						
				System.out.print(getIdAsHex());
				System.out.print(hasSyncByte() ? " OK" : "!!");
										
				if(hasSyncByte()) {
					
					int pid=getPid();
					
					System.out.print("  pid:" + pid + " ");
					System.out.print(getPacketNumber()==((lastpn[pid]+1) % 0x10) ? "": " *");
					
					lastpn[pid] = getPacketNumber(); 

				}
				
				System.out.println();
			
			}
			
			if(read(bufferPayload)==false) {
				return;
			}
			
		}
		
	}
	
	/** Transport Stream Monitor */
	public static void main(String[] args) {
		
		int packets=0;
		int[] pidfilter= {0x12};
		
		while(seekPid(pidfilter) == 0x12) {
						
			
			System.out.print((packets++)+" ");						
			
			System.out.print(getIdAsHex());
			
			System.out.print(hasSyncByte()?"OK":"!!");
									
			if(hasSyncByte()) {
				
				int pid=getPid();
				
				System.out.print("  pid:"+pid+" ");
				
				if(hasPayloadPointer()) {
					System.out.print(", payload:"+getPayloadPointer()+" ");
				}
								
				if(readLeft()==false) {
				//if(read(bufferPayload)==false) {
					return;
				}
				
			}
			System.out.println();
			
		}
		
	}
	
	private static byte[] left = new byte[HEADER_SIZE];

	public static boolean readLeft() {
		
		if(dataleft==0) {
			return true;
		}
		
		assert(dataleft >= 0): "dataleft must be zero or positive, but dataleft = " + dataleft;
		assert(dataleft < TS_PACKET_SIZE);
		
		left = new byte[dataleft];
		boolean ok = read(left);
		
		
		String s = new String(left, StandardCharsets.UTF_8);
		System.out.println((left[0]!=0xFF ? SubtitleMonitor.ANSI_LRED : "")+s+SubtitleMonitor.ANSI_RESET);
		
		return ok;
		
	}
	
	public static byte[] getLeft() {
		return left;
	}

}