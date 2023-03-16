package EPG;
import java.io.IOException;

import PacketReader.DataLeft;
import PacketReader.SeekPID;

public class Reader {

	final private static int PAYLOADPOINTER_SIZE = 1;	
	//final private static int DATA_SIZE = DvbReader.PAYLOAD_SIZE - PAYLOADPOINTER_SIZE - FieldSection.BYTESIZE - FieldEvent.BYTESIZE;	

	static Client monitor;

	/** 
	 * Read byte-buffer, which continues on next packet. 
	 * Return true if succeed, otherwise false. 
	 **/
	public static boolean readFromPackets(byte[] buffer, int start) {

		final int toRead = buffer.length - start;
		final int packetDataLeft = DataLeft.getAmount();
		int readNow = Math.min(toRead, packetDataLeft);

		//System.out.println("\n >readFromPackets(" + buffer.length + ", " + start + ", " + readNow + ")");

		try {

			//assert(start + readNow < buffer.length);
			assert(start >= 0);
			assert(buffer.length > 0);

			int readed = System.in.readNBytes(buffer, start, readNow);
			if(readed==-1) {
				System.exit(0);
			}
			//assert( == readNow) : "End of source!";		  
			DataLeft.reduce(readNow);

			// If 0xFF read then there is stuffing and no buffer read in this packet
			// (ETSI EN 300 468, 5.1.2)
			if((buffer[start] & 0xFF) == 0xFF && buffer.length == FieldSection.BYTESIZE) {

				//All bytes are 0xFF
				assert((buffer[start+readNow-1] & 0xFF) == 0xFF);

				//Jump end of packet
				DataLeft.readAll();
				//assert();

				readNow=0;
			}

		} catch (IOException e) {

			e.printStackTrace();
			return false;

		}

		//Go to next packet
		if(readNow < toRead) {

			assert(DataLeft.getAmount()==0) : "Whole packet not read.";

			//Changes the packet between reading.
			nextPacket();
			//assert() : "Next packet not found!";

			assert(start + readNow < buffer.length);
			readFromPackets(buffer, start + readNow);

		}

		return true;
	}
	
	public static boolean readBuffer(byte[] buffer) {
		assert(buffer!=null);

		return Reader.readFromPackets(buffer,0);
	}

	public static boolean nextPacket() {

		if(SeekPID.nextPacket() == 0) {		  
			return false;
		}

		return true;
	}

	static int SECTIONZERO = 15;
	private static boolean SAFEMODE = true; 

	public static void readEPG(Client monitor) {

		PacketReader.SeekPID.setFilter(new int [] {0x12});
		Reader.monitor = monitor;

		//assert(Id.BYTESIZE + PAYLOADPOINTER_SIZE + FieldSection.BYTESIZE 
		//		+ FieldEvent.BYTESIZE + DATA_SIZE == DvbReader.TS_PACKET_SIZE);

		// TS loop
		while (true) {

			int section_length = FieldSection.next();

			// Iterate events
			while(section_length > SECTIONZERO) {

				int eventLenght = FieldEvent.next();
				section_length -= FieldEvent.BYTESIZE;

				if(eventLenght == 0) {
					section_length = SECTIONZERO;
				}

				//assert(eventLenght>=2) : "eventLenght must be >=2. eventLenght="+eventLenght;

				// Iterate descriotors
				while (eventLenght>0){
					
					readFromPackets(FieldDescriptorTL.buffer, 0);
					//assert();					  

					int descLenght = FieldDescriptorTL.getLenght();

					assert(SAFEMODE || descLenght>0);
					Descriptor.buffer = new byte[descLenght];

					eventLenght -= (FieldDescriptorTL.BYTESIZE + Descriptor.buffer.length);
					section_length -= (FieldDescriptorTL.BYTESIZE + Descriptor.buffer.length);

					readBuffer(Descriptor.buffer);
					//assert();

					// Print data of descriptor.

					monitor.descriptor(FieldDescriptorTL.getTag());

				}
			} 

			FieldCRC.read();
			//assert();

		}
	}

}