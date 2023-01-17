import java.io.IOException;
import java.util.Arrays;

/** Reads Transport Stream. */
public class DvbReader {

	final private static int TS_PACKET_SIZE = 188;
	final private static int HEADER_SIZE = 4;
	final private static int PAYLOAD_SIZE = TS_PACKET_SIZE - HEADER_SIZE;

	private static int currentPID = 0;

	public static int getCurrentPID() {
		assert currentPID>0 && currentPID<10000;
		return currentPID;
	}

	/** Read byte-buffer. Return true if succeed, otherwise false. */
	public static boolean read(byte[] buffer) {
		try {
			return System.in.read(buffer) > 0;
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

	/** Reads transport stream to begin of the first packet,
	 *  thats PID founds on array @pidfilter.*/  
	public static int seekPid(final int[] pidfilter) {

		while(true) {

			if(!readId()) {
				return 0;
			}	
			
			if(hasSyncByte()){
				
				currentPID = getPid();
				
				if(contains(pidfilter, currentPID)) {
					//System.out.println(getIdAsHex() + " (pid=" + pid + ")");
					break;
				}
			}

		}

		return currentPID;
	}

	/** Transport Stream Monitor */
	public static void main(String[] args) {
		
		int packets=0;
		int[] lastpn=new int[8191+1];
		
		while(readId() && packets<5000) {
			
			System.out.print((packets++)+" ");						
			
			System.out.print(getIdAsHex());
			
			System.out.print(hasSyncByte()?"OK":"!!");
									
			if(hasSyncByte()) {
				
				int pid=getPid();
				
				System.out.print("  "+pid+" ");
				
				System.out.print(getPacketNumber()==((lastpn[pid]+1) % 0x10)?"":" *");
				lastpn[pid] = getPacketNumber(); 
				
				if(read(bufferPayload)==false) {
					return;
				}
				
			}
			
			System.out.println();
			
		}
		
	}



}