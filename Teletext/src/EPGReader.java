import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.LocalDateTime;

public class EPGReader {

	final private static int PAYLOADPOINTER_SIZE = 1;
	final private static int SECTION_HEADER_SIZE = 14;  
	final private static int EVENT_HEADER_SIZE = 12;
	final private static int DESCRIPTOR_TAG_AND_LENGHT_SIZE = 2;
	final private static int DATA_SIZE = DvbReader.PAYLOAD_SIZE - PAYLOADPOINTER_SIZE - SECTION_HEADER_SIZE - EVENT_HEADER_SIZE;
	final private static int CRC_SIZE = 4;

	final private static int epgpids[] = {0x12};

	private static EPGMonitor monitor;
 
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
			if((buffer[start] & 0xFF) == 0xFF && buffer.length == SECTION_HEADER_SIZE) {

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

	public static boolean nextPacket() {

		if(DvbReader.seekPid(epgpids) == 0) {		  
			return false;
		}

		return true;
	}

	static class Section {

		public static byte[] buffer = new byte[SECTION_HEADER_SIZE];

		public static int getTableID() {
			return buffer[0];
		}

		public static int getLenght() {
			return (((buffer[1] & (byte)0x0F)) << 8) | (buffer[2] & 0xFF);
		}

		public static boolean isValid() {
			int ti = getTableID();	

			return ((ti==0x4e || ti==0x4f || (ti & 0xF0)==0x50 || (ti & 0xF0)==0x60)
					&& getLenght()>=SECTIONZERO);
		}

		public static int getServiceId() {  
			return (((buffer[3] & (byte)0xFF)) << 8) + (buffer[4] & 0xFF); 
		}

		public static int next() {

			if(DvbReader.getDataleft()==0) {

				//Find place of section

				do {
					DvbReader.seekPid(epgpids);
				} while(!DvbReader.containsNewUnit());

				DvbReader.toPayloadStart();

			}

			//Read section

			assert(readFromPackets(buffer, 0));

			monitor.section();

			assert(isValid());

			return getLenght();
		}
	}

	static class Event {

		static byte[] buffer = new byte[EVENT_HEADER_SIZE];

		static int getDescriptorLoopLenght() {
			return ((buffer[10] & 0x0F) << 8) + (buffer[11] & 0xFF);
		}

		static boolean isValid() {
			//Rude way to check is event is valid.
			return ((buffer[2] & 0xFF) >= 0xD6 && (buffer[2] & 0xFF) <= 0xF0);
		}

		static int next() {

			//assert(DvbReader.getDataleft()>=2);
			//assert(readCRC());

			assert(readFromPackets(buffer, 0));

			//assert(SAFEMODE || (bufferEventHeader[2] & 0xFF) == 0xEA): "Error in EventHeader.";

			assert(isValid());

			monitor.event();

			//System.out.println("  Event: (s" + section_length + ") " + getEventHeaderAsHex());

			//eventLenght = getDescriptorLoopLenght();	  

			return getDescriptorLoopLenght();

		}

		static int timeunit(byte b) {

			return ((b & 0xF0) >> 4) * 10 + (b & 0x0F);
		}

		public static String getEventStart() {

			int MJD = (((buffer[2] & 0xFF) << 8) |(buffer[3] & 0xFF));

			if (MJD==0xFFFF) {
				return null;
			}

			int Yh=(int)((MJD-15078.2)/365.25);

			int Mh = (int)(( MJD - 14956.1 - (int) (Yh * 365.25) ) / 30.6001 );

			int D = MJD - 14956 - (int) (Yh * 365.25) - (int) (Mh * 30.6001 );

			int K = (Mh == 14 || Mh == 15 ? 1 : 0);

			int Y = Yh + K;

			int M = Mh - 1 - K * 12;

			if ((buffer[4] & 0xFF) == 0xFF) {
				return null;
			}

			return LocalDateTime.of(Y + 1900, M, D,
					timeunit(buffer[4]), 
					timeunit(buffer[5]),  
					timeunit(buffer[6])
					).toString();
		}

		public static String formatDuration(Duration duration) {

			long seconds = duration.getSeconds();
			long absSeconds = Math.abs(seconds);
			String positive = String.format(
					"%d:%02d:%02d",
					absSeconds / 3600,
					(absSeconds % 3600) / 60,
					absSeconds % 60);
			return seconds < 0 ? "-" + positive : positive;
		}

		public static String getEventDuration() {

			return formatDuration(Duration.ofSeconds(timeunit(buffer[7]) * 3600 +
					timeunit(buffer[8]) * 60 +
					timeunit(buffer[9])
					));
		}

	}

	static class DescriptorTL {

		public static byte[] buffer = new byte[DESCRIPTOR_TAG_AND_LENGHT_SIZE];

		public static int getTag() {
			return buffer[0];
		}

		public static int getLenght() {
			return buffer[1] & 0xFF;
		}
	}

  static class CRC {

	  private static byte[] buffer = new byte[CRC_SIZE];

	  private static boolean read() {
		  return readFromPackets(buffer, 0);
	  }

  }

  private static int SECTIONZERO = 15;
  private static boolean SAFEMODE = true; 

  public static void readEPG(EPGMonitor monitor) {

	  EPGReader.monitor = monitor;

	  assert(DvbReader.HEADER_SIZE + PAYLOADPOINTER_SIZE + SECTION_HEADER_SIZE 
			  + EVENT_HEADER_SIZE + DATA_SIZE == DvbReader.TS_PACKET_SIZE);

	  // TS loop
	  while (true) {

		  int section_length = Section.next();

		  // Iterate events
		  while(section_length > SECTIONZERO) {

			  int eventLenght = Event.next();
			  section_length -= EVENT_HEADER_SIZE;

			  if(eventLenght == 0) {
				  section_length = SECTIONZERO;
			  }

			  //assert(eventLenght>=2) : "eventLenght must be >=2. eventLenght="+eventLenght;

			  // Iterate descriotors
			  while (eventLenght>0){

				  assert(readFromPackets(DescriptorTL.buffer, 0 ));					  

				  int descLenght = DescriptorTL.getLenght();

				  assert(SAFEMODE || descLenght>0);
				  EPGData.buffer = new byte[descLenght];

				  eventLenght -= (DESCRIPTOR_TAG_AND_LENGHT_SIZE + EPGData.buffer.length);
				  section_length -= (DESCRIPTOR_TAG_AND_LENGHT_SIZE + EPGData.buffer.length);

				  assert(EPGData.read());

				  // Print data of descriptor.

				  monitor.descriptor(DescriptorTL.getTag(), EPGData.buffer);

			  }
		  } 

		  assert(CRC.read());

	  }
  }

}