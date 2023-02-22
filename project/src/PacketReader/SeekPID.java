package PacketReader;

import java.util.Arrays;

public class SeekPID {

	//Seeking packets
	
	private static int[] pidfilter = new int[] {};
	
	public static void setFilter(int[] pids) {
		pidfilter = pids;
	}
	
	private static boolean contains(final int key) {
	    return Arrays.stream(pidfilter).anyMatch(i -> i == key);
	}
	
	/** Reads transport stream to begin of the first packet,
	 *  thats PID founds on array @pidfilter.
	 *  Returns PID. */  
	public static int nextPacket() {

		//assert(dataleft==0): "dataleft must be zero, but dataleft = " + dataleft;
		
		while(true) {
			
			DataLeft.reset();
			
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
						DvbReader.readPayloadPointer();
						//System.out.print(", Paystart " + getPayloadPointer());
					}
					
					//System.out.println("] ");
					
					return currentPID;
				}
				
				
				//Read to end of packet
				//assert(readLeft());
				
				assert(DvbReader.readPayload());

			}
			
			//assert(dataleft==PAYLOAD_SIZE): "dataleft must be zero, but dataleft = " + dataleft;

		}

	}
	
	public static void nextPayloadStart() {
		do {
			SeekPID.nextPacket();
		} while(!Id.containsNewUnit());

		DvbReader.toPayloadStart();
	}
	
}
