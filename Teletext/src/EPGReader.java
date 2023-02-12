import java.io.IOException;

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
		  
		  System.out.println("Section: " + DvbReader.byteBuffertoHex(buffer)
			  		+ ", correct: " +isValid()
			  		+ ", lenght: "+getLenght());
		  
		  monitor.section(buffer);
		  
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
		  return ((buffer[2] & 0xFF) < 0xD6 || (buffer[2] & 0xFF) > 0xF0);
	  }
	  
	  static int next() {

		  //assert(DvbReader.getDataleft()>=2);
		  //assert(readCRC());

		  assert(readFromPackets(buffer, 0));
		  System.out.println("  Event: " + DvbReader.byteBuffertoHex(buffer));

		  //assert(SAFEMODE || (bufferEventHeader[2] & 0xFF) == 0xEA): "Error in EventHeader.";

		  if(isValid()) {
			  return 0;
		  }

		  monitor.event(buffer);

		  //System.out.println("  Event: (s" + section_length + ") " + getEventHeaderAsHex());

		  //eventLenght = getDescriptorLoopLenght();	  

		  return getDescriptorLoopLenght();

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

  static class Data {
	  
	  private static byte[] buffer;

	  public static boolean read() {
		  assert(buffer!=null);

		  return readFromPackets(buffer,0);
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

				  System.out.print("    Desc: (e"+eventLenght+") " + DvbReader.byteBuffertoHex(DescriptorTL.buffer) + "  ");

				  int descLenght = DescriptorTL.getLenght();

				  assert(SAFEMODE || descLenght>0);
				  Data.buffer = new byte[descLenght];

				  eventLenght -= (DESCRIPTOR_TAG_AND_LENGHT_SIZE + Data.buffer.length);
				  section_length -= (DESCRIPTOR_TAG_AND_LENGHT_SIZE + Data.buffer.length);

				  assert(Data.read());

				  // Print data of descriptor.

				  monitor.descriptor(DescriptorTL.getTag(), Data.buffer);

			  }
		  } 

		  assert(CRC.read());

	  }
  }

}