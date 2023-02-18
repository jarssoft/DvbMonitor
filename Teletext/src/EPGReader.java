import java.io.IOException;
import Descriptions.Descriptor;

public class EPGReader {

	final private static int PAYLOADPOINTER_SIZE = 1;
	
	final private static int DATA_SIZE = DvbReader.PAYLOAD_SIZE - PAYLOADPOINTER_SIZE - EPGSection.BYTESIZE - EPGEvent.BYTESIZE;	

	final static int epgpids[] = {0x12};

	static EPGMonitor monitor;

	/** 
	 * Read byte-buffer, which continues on next packet. 
	 * Return true if succeed, otherwise false. 
	 **/
	public static boolean readFromPackets(byte[] buffer, int start) {

		final int toRead = buffer.length - start;
		final int packetDataLeft = DvbReader.getDataleft();
		int readNow = Math.min(toRead, packetDataLeft);

		//System.out.println("\n >readFromPackets(" + buffer.length + ", " + start + ", " + readNow + ")");

		try {

			//assert(start + readNow < buffer.length);
			assert(start >= 0);
			assert(buffer.length > 0);

			assert(System.in.read(buffer, start, readNow) == readNow) : "End of source!";		  
			DvbReader.reduceDataleft(readNow);

			// If 0xFF read then there is stuffing and no buffer read in this packet
			// (ETSI EN 300 468, 5.1.2)
			if((buffer[start] & 0xFF) == 0xFF && buffer.length == EPGSection.BYTESIZE) {

				//All bytes are 0xFF
				assert((buffer[start+readNow-1] & 0xFF) == 0xFF);

				//Jump end of packet
				assert(DvbReader.readLeft());

				readNow=0;
			}

		} catch (IOException e) {

			e.printStackTrace();
			return false;

		}

		//Go to next packet
		if(readNow < toRead) {

			assert(DvbReader.getDataleft()==0) : "Whole packet not read.";

			//Changes the packet between reading.
			assert(nextPacket()) : "Next packet not found!";

			assert(start + readNow < buffer.length);
			readFromPackets(buffer, start + readNow);

		}

		return true;
	}
	
	public static boolean readBuffer(byte[] buffer) {
		assert(buffer!=null);

		return EPGReader.readFromPackets(buffer,0);
	}

	public static boolean nextPacket() {

		if(DvbReader.seekPid(epgpids) == 0) {		  
			return false;
		}

		return true;
	}

	static int SECTIONZERO = 15;
	private static boolean SAFEMODE = true; 

	public static void readEPG(EPGMonitor monitor) {

		EPGReader.monitor = monitor;

		assert(DvbReader.HEADER_SIZE + PAYLOADPOINTER_SIZE + EPGSection.BYTESIZE 
				+ EPGEvent.BYTESIZE + DATA_SIZE == DvbReader.TS_PACKET_SIZE);

		// TS loop
		while (true) {

			int section_length = EPGSection.next();

			// Iterate events
			while(section_length > SECTIONZERO) {

				int eventLenght = EPGEvent.next();
				section_length -= EPGEvent.BYTESIZE;

				if(eventLenght == 0) {
					section_length = SECTIONZERO;
				}

				//assert(eventLenght>=2) : "eventLenght must be >=2. eventLenght="+eventLenght;

				// Iterate descriotors
				while (eventLenght>0){

					assert(readFromPackets(DescriptorTL.buffer, 0 ));					  

					int descLenght = DescriptorTL.getLenght();

					assert(SAFEMODE || descLenght>0);
					Descriptor.buffer = new byte[descLenght];

					eventLenght -= (DescriptorTL.BYTESIZE + Descriptor.buffer.length);
					section_length -= (DescriptorTL.BYTESIZE + Descriptor.buffer.length);

					assert(readBuffer(Descriptor.buffer));

					// Print data of descriptor.

					monitor.descriptor(DescriptorTL.getTag(), Descriptor.buffer);

				}
			} 

			assert(CRC.read());

		}
	}

}