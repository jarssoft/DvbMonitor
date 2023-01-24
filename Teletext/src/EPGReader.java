
public class EPGReader {

	private static boolean isAsciiPrintable(char ch) {

		return ch >= 32 && ch < 127;
	}

	private static char decodechar(byte aByte) {

		char c = (char)aByte;

		if(isAsciiPrintable(c)) {
			if(c =='￤') {return 'ä';}
			else if(c =='[') {return 'Ä';}
			else if(c =='}') {return 'å';}
			else if(c ==']') {return 'Å';}
			else if(c =='|') {return 'ö';}
			else if(c =='\\'){return 'Ö';}
			else if(c =='~') {return 'ü';} 
			else {return c;}
		}else{
			return '*';
		}
	}

  /**********************/
    
  final private static int PREFIX_SIZE = 6+8+1;
  final private static int EVENT_HEADER_SIZE = 12;
  final private static int DESCRIPTOR_TAG_AND_LENGHT_SIZE = 2;
  final private static int DATA_SIZE = DvbReader.PAYLOAD_SIZE - PREFIX_SIZE - EVENT_HEADER_SIZE; //40-8;
  final private static int CRC_SIZE = 4;
  
  private final static int epgpids[] = {0x12};
  
  private static int section_length = 0;

  public static boolean readPacket() {

	  if(DvbReader.seekPid(epgpids) == 0) {
		  return false;
	  }

	  if(dLenght==0) {	  
		  
		  //if(dlLenght<=0) {
		  //if(section_length <= 27+EVENT_HEADER_SIZE){

			  if(!readPrefix()) {
				  return false;
			  }
			  
			  System.out.println("  "+section_length+": ");
			  System.out.println("--- Section --------------------------------");
			  
			  System.out.println(getPrefixAsHex());

			  int table_id = getTableID();
			  section_length = getSectionLenght();
			  
		  //}
		  //System.out.println(table_id + "   " + section_length);
		  //}
		  
		  //if(section_length<=0) {
					  
		  int ti=bufferPrefix[1];	  
		  if(bufferPrefix[0]==0 && (ti==0x4e || ti==0x4f 
				  || (ti & 0xF0)==0x50 || (ti & 0xF0)==0x60)) {
			  nextEvent();
		  }
		  
	  }

	  return true;
  }
  
  private static void nextEvent() {
	  
	  //assert(DvbReader.getDataleft()>=2);
	  //assert(readCRC());
	  
	  assert(readEventHeader());
	  dlLenght = getDescriptorLoopLenght();
	  System.out.println("  "+section_length + ": " + getEventHeaderAsHex());
  }

  /**********************/

  private static byte[] bufferPrefix = new byte[PREFIX_SIZE];

  public static boolean readPrefix() {
	  return DvbReader.read(bufferPrefix);
  }

  public static String getPrefixAsHex() {
	  return DvbReader.byteBuffertoHex(bufferPrefix);
  }

  public static int getTableID() {
	  return bufferPrefix[1];//(decodeHamming(bufferPrefix[5])<<1) | (((byte)bufferPrefix[4] & (byte)0b00000001));
  }

  public static int getSectionLenght() {
	  //System.out.println(Integer.toBinaryString(0x0F & (int)bufferPrefix[2]));
	  //System.out.println(Integer.toBinaryString((byte)0x0F));
	  //System.out.println(Integer.toBinaryString(0xFF & bufferPrefix[3]));
	  return (((bufferPrefix[2] & (byte)0x0F)) << 8) | (bufferPrefix[3] & 0xFF);
  }

  /**********************/

  private static byte[] bufferEventHeader = new byte[EVENT_HEADER_SIZE];

  public static boolean readEventHeader() {
	  return DvbReader.read(bufferEventHeader);
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

  /**********************/

  private static byte[] bufferDescriptorTL = new byte[DESCRIPTOR_TAG_AND_LENGHT_SIZE];

  public static boolean readDescriptorTL() {
	  return DvbReader.read(bufferDescriptorTL);
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
	  
	  return DvbReader.read(bufferData);
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
	  return DvbReader.read(bufferCRC);
  }
  
  private static int dLenght = 0;
  private static int dlLenght = 0;

  public static void main(String[] args) {

	  assert(DvbReader.HEADER_SIZE + PREFIX_SIZE + EVENT_HEADER_SIZE + DATA_SIZE == DvbReader.TS_PACKET_SIZE);

	  // TS loop
	  while (true) {

		  assert(readPacket());

		  //StringBuilder result = new StringBuilder();

		  /*
		   if(!readData()) {
			   break;
		   }
		   */

		  //result.append(getDataAsText()); 	      

		  if(bufferPrefix[0]==0 || dLenght>0) {

			  // Descriptor loop
			  do {

				  // Starting to read new descriptor.
				  if(dLenght==0) {

					  if(dlLenght==0) {
						  //No enough data left in packet.
						  if(DvbReader.getDataleft() <= EVENT_HEADER_SIZE + 1) {
							  break;
						  }
						  nextEvent();
						  section_length -= EVENT_HEADER_SIZE;
						  assert(dlLenght>=2):"dlLenght must be >=2. dlLenght="+dlLenght;
					  }
					  assert(dlLenght>=2):"dlLenght must be >=2. dlLenght="+dlLenght;
					  
					  assert(readDescriptorTL());					  
					  
					  System.out.print("    "+dlLenght+": "+getDescriptorTLAsHex()+"  ");
					  dlLenght-=DESCRIPTOR_TAG_AND_LENGHT_SIZE;
					  section_length -= DESCRIPTOR_TAG_AND_LENGHT_SIZE;
					  
					  if(getDescriptorTag()==0x4d || getDescriptorTag()==0x54 
							  || getDescriptorTag()==0x55
							  || (getDescriptorTag() & 0xF0) == 0x50) {
						  
					  } else {
						  //short_event_desc = 0x4D
						  //content_descriptor = 0x54
						  //parental_rating_descriptor = 0x55
	
						  //assert(getDescriptorLenght()<=DvbReader.getDataleft());
					  
						  //break;
					  }

					  dLenght = getDescriptorLenght();

				  }
				  
				  assert(dLenght>0);
				  //assert(DvbReader.getDataleft()>0);
				  
				  // Section continues in next packet.
				  if(dLenght > DvbReader.getDataleft()) {
					  bufferData = new byte[DvbReader.getDataleft()];
				  }else {
					  bufferData = new byte[dLenght];
				  }
				  
				  dLenght -= bufferData.length;
				  dlLenght -= bufferData.length;
				  section_length -= bufferData.length;
				  ///assert(dlLenght>=0);
				  
				  // Print data of descriptor.
				  if(DvbReader.getDataleft()>0) {
					  assert(readData());
				  }
				  System.out.print(getDataAsText());
				  
				  //Descriptor continues on next packet
				  if(dLenght>0) {
					  System.out.print("[new packet]");
					  break;
				  }
				  
				  System.out.println();
				  
			  } while(DvbReader.getDataleft() >= 2 && section_length > 27);
		  }

		  assert(DvbReader.readLeft());
		  
	  }
  }

}