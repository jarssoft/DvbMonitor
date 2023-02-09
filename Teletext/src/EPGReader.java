import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class EPGReader {

	private static boolean isAsciiPrintable(char ch) {

		return ch >= 32 && ch < 127;
	}

	private static char decodechar(byte aByte) {

		char c = (char)aByte;

		if(isAsciiPrintable(c)) {
			return c;
		}else{
			return '.';
		}
	}

  /**********************/
	
  final private static int PAYLOADPOINTER_SIZE = 1;
  final private static int SECTION_HEADER_SIZE = 14;  
  final private static int EVENT_HEADER_SIZE = 12;
  final private static int DESCRIPTOR_TAG_AND_LENGHT_SIZE = 2;
  final private static int DATA_SIZE = DvbReader.PAYLOAD_SIZE - PAYLOADPOINTER_SIZE - SECTION_HEADER_SIZE - EVENT_HEADER_SIZE;
  final private static int CRC_SIZE = 4;
  
  private final static int epgpids[] = {0x12};
  
  /** 
   * Read byte-buffer, which exists in two packet. 
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
	  
	  //System.out.println("\n----[new packet]-----");
	  
	  if(DvbReader.seekPid(epgpids) == 0) {		  
		  return false;
	  }
	  
	  return true;
  }
  
  /**********************/

  private static byte[] bufferSection = new byte[SECTION_HEADER_SIZE];

  public static boolean readSection() {	  
	  return readFromPackets(bufferSection, 0);
  }

  public static String getPrefixAsHex() {
	  return DvbReader.byteBuffertoHex(bufferSection);
  }

  public static int getTableID() {
	  return bufferSection[0]; //(decodeHamming(bufferPrefix[5])<<1) | (((byte)bufferPrefix[4] & (byte)0b00000001));
  }

  public static int getSectionLenght() {
	  //System.out.println(Integer.toBinaryString(0x0F & (int)bufferPrefix[2]));
	  //System.out.println(Integer.toBinaryString((byte)0x0F));
	  //System.out.println(Integer.toBinaryString(0xFF & bufferPrefix[3]));
	  return (((bufferSection[1] & (byte)0x0F)) << 8) | (bufferSection[2] & 0xFF);
  }
  
  public static int getServiceId() {
	  return (((bufferSection[3] & (byte)0xFF)) << 8) + (bufferSection[4] & 0xFF);
  }
  
  public static boolean validSection() {
	  int ti = getTableID();	
	  
	  return ((ti==0x4e || ti==0x4f || (ti & 0xF0)==0x50 || (ti & 0xF0)==0x60)
			  && getSectionLenght()>=SECTIONZERO);
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

	  int MJD = (((bufferEventHeader[2] & 0xFF) << 8) |(bufferEventHeader[3] & 0xFF));
	  
	  if (MJD==0xFFFF) {
		  return null;
	  }
	  
	  int Yh=(int)((MJD-15078.2)/365.25);
	  	  
	  int Mh = (int)(( MJD - 14956.1 - (int) (Yh * 365.25) ) / 30.6001 );
			  
	  int D = MJD - 14956 - (int) (Yh * 365.25) - (int) (Mh * 30.6001 );
	  
	  int K = (Mh == 14 || Mh == 15 ? 1 : 0);
			  
	  int Y = Yh + K;
			  
	  int M = Mh - 1 - K * 12;
	  
	  if ((bufferEventHeader[4] & 0xFF) == 0xFF) {
		  return null;
	  }
	  
	  
	  return LocalDateTime.of(Y + 1900, M, D,
			  timeunit(bufferEventHeader[4]), 
			  timeunit(bufferEventHeader[5]),  
			  timeunit(bufferEventHeader[6])
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

  private static String getEventDuration() {

	  return formatDuration(Duration.ofSeconds(timeunit(bufferEventHeader[7]) * 3600 +
			  timeunit(bufferEventHeader[8]) * 60 +
			  timeunit(bufferEventHeader[9])
			  ));
  }
  
  private static boolean validEvent() {
		//Rude way to check is event is valid.
		return ((bufferEventHeader[2] & 0xFF) < 0xD6 || (bufferEventHeader[2] & 0xFF) > 0xF0);
	}


  /**********************/

  private static byte[] bufferDescriptorTL = new byte[DESCRIPTOR_TAG_AND_LENGHT_SIZE];

  public static boolean readDescriptorTL() {
	  return readFromPackets(bufferDescriptorTL, 0 );
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
  
  private static byte[] bufferData;
  
  private static final String[] nibbles = {
		  "undefined", 
		  "Movie/Drama", 
		  "News/Current affairs",
		  "Show/Game show", 
		  "Sports",
		  "Children's/Youth programmes",
		  "Music/Ballet/Dance",
		  "Arts/Culture (without music)",
		  "Social/Political issues/Economics",
		  "Education/ Science/Factual topics",
		  "Leisure hobbies" ,
		  "Leisure hobbies",
		  "","","",""
  };

  public static boolean readData() {
	  assert(bufferData!=null);
	  
	  return readFromPackets(bufferData,0);
  }
  
  public static String getDataAsHex() {
	  return DvbReader.byteBuffertoHex(bufferData);
  }

  public static byte getContentNibble() {
	  return bufferData[0];
  }
  
  public static int getParentalRating() {
	  return (bufferData[3]>=0x01 && bufferData[3]<=0x0F ? bufferData[3] + 3 : 0);
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
	  return readFromPackets(bufferCRC, 0);
  }

  /*********************/
  
  private static int SECTIONZERO = 15;
  private static boolean SAFEMODE = true; 
  
  public static int nextSection() {

	  if(DvbReader.getDataleft()==0) {
		  
		  //Find place of section
		  
		  do {
			  DvbReader.seekPid(epgpids);
		  }while(!DvbReader.containsNewUnit());
		  
		  DvbReader.toPayloadStart();
		  
	  }
	  
	  //Read section
	  //do {		  
		  assert(readSection());
		  System.out.println("Section: " + getPrefixAsHex()+", correct: " +validSection()+", lenght: "+getSectionLenght());
		  System.out.println("  Service: " + getServiceId());

	  //} while(!validSection());
		  
		  assert(validSection());
	  
	  return getSectionLenght();
  }
  
  private static int nextEvent() {
	  
	  //assert(DvbReader.getDataleft()>=2);
	  //assert(readCRC());
	  
	  assert(readEventHeader());
	  System.out.println("  Event: " + getEventHeaderAsHex());

	  //assert(SAFEMODE || (bufferEventHeader[2] & 0xFF) == 0xEA): "Error in EventHeader.";

	  if(validEvent()) {
		  return 0;
	  }
	  
	  System.out.println("    Starts: " + getEventStart() + ", Duration: "+getEventDuration());
	  
	  //System.out.println("  Event: (s" + section_length + ") " + getEventHeaderAsHex());
	  
	  //eventLenght = getDescriptorLoopLenght();	  
	  
	  return getDescriptorLoopLenght();
	  
  }

  public static void main(String[] args) {

	  assert(DvbReader.HEADER_SIZE + PAYLOADPOINTER_SIZE + SECTION_HEADER_SIZE 
			  + EVENT_HEADER_SIZE + DATA_SIZE == DvbReader.TS_PACKET_SIZE);
	  

	  
	  // TS loop
	  while (true) {

		  int section_length = nextSection();

		  // Iterate events
		  while(section_length > SECTIONZERO) {

			  int eventLenght=nextEvent();
			  section_length -= EVENT_HEADER_SIZE;

			  if(eventLenght==0) {
				  section_length=SECTIONZERO;
			  }

			  //assert(eventLenght>=2) : "eventLenght must be >=2. eventLenght="+eventLenght;

			  //Iterate descriotors
			  while (eventLenght>0){

				  assert(readDescriptorTL());					  

				  System.out.print("    Desc: (e"+eventLenght+") "+getDescriptorTLAsHex()+"  ");

				  int descLenght = getDescriptorLenght();

				  assert(SAFEMODE || descLenght>0);
				  bufferData = new byte[descLenght];

				  eventLenght -= (DESCRIPTOR_TAG_AND_LENGHT_SIZE + bufferData.length);
				  section_length -= (DESCRIPTOR_TAG_AND_LENGHT_SIZE + bufferData.length);

				  assert(readData());

				  //Print data of descriptor.

				  if(getDescriptorTag() == 0x54) {

					  System.out.print(getDataAsHex()+"  ");
					  System.out.print(nibbles[(getContentNibble() & 0xF0) >> 4]);
					  System.out.println();

				  }else if(getDescriptorTag() == 0x55) {

					  //System.out.print(getDataAsText()+"  "+getParentalRating());
					  System.out.println(getDataAsText());

				  }else {

					  System.out.println(getDataAsText());

				  }
			  }
		  } 

		  assert(readCRC());

	  }
  }
}