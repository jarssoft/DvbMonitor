package PacketReader;

import java.nio.charset.StandardCharsets;

/** Meters amount of data left in current packet. */
public class DataLeft {
	
	private static int dataleft = 0;
	  
	/** Returns amount of data left in packet in bytes. */
	public static final int getAmount() {
		return dataleft;
	}
	
	public static void reduce(final int dl) {
		dataleft -= dl;
		assert(dataleft >= 0): "dataleft must be zero or positive, but dataleft = " + dataleft;
	}
	
	public static void reset() {
		dataleft = DvbReader.TS_PACKET_SIZE;
	}

	public static boolean readAll() {
		
		if(dataleft==0) {
			return true;
		}
		
		assert(dataleft >= 0): "dataleft must be zero or positive, but dataleft = " + dataleft;
		assert(dataleft < DvbReader.TS_PACKET_SIZE);
		
		byte[] left = new byte[dataleft];
		boolean ok = Field.read(left);
		
		String s = new String(left, StandardCharsets.UTF_8);
		//System.out.println((left[0]!=0xFF ? "" : "") 
		//		+ s + "");
		
		return ok;
		
	}
}
