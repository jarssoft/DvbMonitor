import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.params.shadow.com.univocity.parsers.conversions.ToStringConversion;

public class EPGReader {

	private static boolean isAsciiPrintable(char ch) {

		return ch >= 32 && ch < 127;
	}

	private static char decodechar(byte aByte) {

		char c = (char)aByte;

		if(isAsciiPrintable(c)) {
			return c;
		}else{
			return '*';
		}
	}

  /**********************/
    
  final private static int PREFIX_SIZE = 6 + 8 + 1;
  final private static int EVENT_HEADER_SIZE = 12;
  final private static int DESCRIPTOR_TAG_AND_LENGHT_SIZE = 2;
  final private static int DATA_SIZE = DvbReader.PAYLOAD_SIZE - PREFIX_SIZE - EVENT_HEADER_SIZE;
  final private static int CRC_SIZE = 4;
  
  private final static int epgpids[] = {0x12};
  
  /** Read byte-buffer, which exists in two packet. 
   * Return true if succeed, otherwise false. */
  public static boolean readFromPackets(byte[] buffer, int start) {

	  final int toRead = buffer.length - start;
	  final int packetDataLeft = DvbReader.getDataleft();
	  final int readNow = Math.min(toRead, packetDataLeft);
	  
	  //System.out.println("\n >readFromPackets(" + buffer.length + ", " + start + ", " + readNow + ")");
	  
	  try {

		  assert(System.in.read(buffer, start, readNow) == readNow);
		  DvbReader.reduceDataleft(readNow);
		  
	  } catch (IOException e) {

		  e.printStackTrace();
		  return false;

	  }
	  
	  if(toRead > packetDataLeft) {
		  
		  //Changes the packet between reading.
		  assert(nextPacket());
		   
		  assert(start + readNow < buffer.length);
		  readFromPackets(buffer, start + readNow);

	  }
	  
	  return true;
  }
	
  public static boolean nextPacket() {
	  
	  System.out.println("\n----[new packet]-----");
	  
	  if(DvbReader.seekPid(epgpids) == 0) {		  
		  return false;
	  }
	  return true;
  }
  
  /**********************/

  private static byte[] bufferSection = new byte[PREFIX_SIZE];

  public static boolean readSection() {
	  return readFromPackets(bufferSection,0 );
  }

  public static String getPrefixAsHex() {
	  return DvbReader.byteBuffertoHex(bufferSection);
  }

  public static int getTableID() {
	  return bufferSection[1];//(decodeHamming(bufferPrefix[5])<<1) | (((byte)bufferPrefix[4] & (byte)0b00000001));
  }

  public static int getSectionLenght() {
	  //System.out.println(Integer.toBinaryString(0x0F & (int)bufferPrefix[2]));
	  //System.out.println(Integer.toBinaryString((byte)0x0F));
	  //System.out.println(Integer.toBinaryString(0xFF & bufferPrefix[3]));
	  return (((bufferSection[2] & (byte)0x0F)) << 8) | (bufferSection[3] & 0xFF);
  }
  
  public static boolean correctSection() {
	  int ti = bufferSection[1];	
	  
	  return (bufferSection[0]==0 && 
			  (ti==0x4e || ti==0x4f || (ti & 0xF0)==0x50 || (ti & 0xF0)==0x60)
			  && getSectionLenght()>SECTIONZERO);
  }

  /**********************/

  private static byte[] bufferEventHeader = new byte[EVENT_HEADER_SIZE];

  public static boolean readEventHeader() {
	  return readFromPackets(bufferEventHeader,0 );
  }

  public static String getEventHeaderAsHex() {
	  return DvbReader.byteBuffertoHex(bufferEventHeader);
  }

  public static int getStartTime() {
	  return bufferEventHeader[1];//(decodeHamming(bufferPrefix[5])<<1) | (((byte)bufferPrefix[4] & (byte)0b00000001));
  }

  public static int getDuration() {
	  return ((bufferEventHeader[2] & 0x0F) << 8) + bufferEventHeader[3];
  }
  
  private static int getDescriptorLoopLenght() {
	  return ((bufferEventHeader[10] & 0x0F) << 8) + (bufferEventHeader[11] & 0xFF);
  }
  
  static int timeunit(byte b) {
	  
	  return ((b & 0xF0) >> 4) * 10 + (b & 0x0F);
  }
  
  private static String getEventStart() {

	  return LocalDateTime.of(2023, 1, 30,
			  timeunit(bufferEventHeader[4]), 
			  timeunit(bufferEventHeader[5]),  
			  timeunit(bufferEventHeader[6])).toString();

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

  private static String getEventDuration() {

	  return formatDuration(Duration.ofSeconds(timeunit(bufferEventHeader[7]) * 3600 +
			  timeunit(bufferEventHeader[8]) * 60 +
			  timeunit(bufferEventHeader[9])
			  ));
  }

  /**********************/

  private static byte[] bufferDescriptorTL = new byte[DESCRIPTOR_TAG_AND_LENGHT_SIZE];

  public static boolean readDescriptorTL() {
	  return readFromPackets(bufferDescriptorTL,0 );
  }

  public static String getDescriptorTLAsHex() {
	  return DvbReader.byteBuffertoHex(bufferDescriptorTL);
  }

  public static int getDescriptorTag() {
	  return bufferDescriptorTL[0];
  }

  public static int getDescriptorLenght() {
	  return bufferDescriptorTL[1] & 0xFF;
  }
  
  /**********************/
  
  private static byte[] bufferData = new byte[DATA_SIZE];

  public static boolean readData() {
	  assert(bufferData!=null);
	  
	  return readFromPackets(bufferData,0);
  }

  public static String getDataAsText() {
	  StringBuilder result = new StringBuilder();
	  for (byte aByte : bufferData) {
		  result.append(decodechar(aByte));
	  }
	  return result.toString();
  }

  /*********************/
  
  private static byte[] bufferCRC = new byte[CRC_SIZE];
  
  private static boolean readCRC() {
	  return readFromPackets(bufferCRC,0);
  }

  /*********************/
  
  private static int SECTIONZERO = 15;
  
  public static int nextSection() {

	  //Section starts on zeropoint of packet
	  assert(DvbReader.getDataleft()==0);
	  
	  //assert(section_length == SECTIONZERO):"section_length must be "+SECTIONZERO+", section_length == "+section_length;
	  
	  assert(readSection());

	  //System.out.println("  s"+section_length);
	  System.out.println("Section: "+getPrefixAsHex());

	  if(correctSection()) {
		  return getSectionLenght();
	  }else {
		  return 0;
	  }

  }
  
  private static int nextEvent() {
	  
	  //assert(DvbReader.getDataleft()>=2);
	  //assert(readCRC());
	  
	  assert(readEventHeader());
	  System.out.println("  Event: " + getEventHeaderAsHex());
	  System.out.println("    Starts: " + getEventStart() + ", Duration: "+getEventDuration());
	  //System.out.println("  Event: (s" + section_length + ") " + getEventHeaderAsHex());
	  
	  //eventLenght = getDescriptorLoopLenght();	  
	  
	  assert((bufferEventHeader[2] & 0xFF) == 0xEA): "Error in EventHeader.";
	  
	  return getDescriptorLoopLenght();
	  
  }


public static void main(String[] args) {

	  assert(DvbReader.HEADER_SIZE + PREFIX_SIZE + EVENT_HEADER_SIZE + DATA_SIZE == DvbReader.TS_PACKET_SIZE);

	  boolean first_packet_detected = false;
	  
	  // TS loop
	  while (true) {

		  int section_length = nextSection();

			  // Iterate events
			  while(section_length > SECTIONZERO) {
				  
				  first_packet_detected = true;

				  int eventLenght=nextEvent();
				  section_length -= EVENT_HEADER_SIZE;
				  
				  //assert(eventLenght>=2):"eventLenght must be >=2. eventLenght="+eventLenght;
				  
				  //Iterate descriotors
				  while (eventLenght>0){
					  
					  assert(readDescriptorTL());					  
	
					  System.out.print("    Desc: (e"+eventLenght+") "+getDescriptorTLAsHex()+"  ");
	
					  int descLenght = getDescriptorLenght();
					  assert(descLenght>0);
					  bufferData = new byte[descLenght];
	
					  eventLenght -= (DESCRIPTOR_TAG_AND_LENGHT_SIZE + bufferData.length);
					  section_length -= (DESCRIPTOR_TAG_AND_LENGHT_SIZE + bufferData.length);
					  
					  assert(readData());
					  
					  //Print data of descriptor.
					  
					  System.out.println(getDataAsText());
					  
				  }

			  } 

		  assert(readCRC());
		  assert(DvbReader.readLeft());		  
		  assert(first_packet_detected==false 
				  || DvbReader.getLeft().length==0 
				  || (DvbReader.getLeft()[0] & 0xFF) == 0xFF);
	  }
  }
}