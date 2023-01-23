
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
  
  private final static int epgpids[] = {0x12};
  
  private static int section_length = 0;

  public static boolean readPacket() {

	  if(DvbReader.seekPid(epgpids) == 0) {
		  return false;
	  }

	  if(dLenght==0) {	  
		  
		  if(!readPrefix()) {
			  return false;
		  }
		  
		  System.out.println("-----------------");
		  System.out.println(getPrefixAsHex());
		  
		  //if(section_length<=0) {
		  if(bufferPrefix[0]==0) {

			  if(!readEventHeader()) {
				  return false;
			  }

			  int table_id = getTableID();
			  System.out.print(table_id + "   ");
	
			  section_length = getSectionLenght();
			  System.out.println(section_length);
	
			  dlLenght = getDescriptorLoopLenght();
			  System.out.println(getEventHeaderAsHex()+" "+dlLenght);

		  }
	  }
	  
	  section_length -= DvbReader.TS_PACKET_SIZE;

	  return true;
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
  
  private static int dLenght = 0;
  private static int dlLenght = 0;

  public static void main(String[] args) {

	  assert(DvbReader.HEADER_SIZE + PREFIX_SIZE + EVENT_HEADER_SIZE + DATA_SIZE == DvbReader.TS_PACKET_SIZE);

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

			  //while(readDescriptorTL()) {
			  while (true) {

				  // Starting to read new descriptor.
				  if(dLenght==0) {

					  assert(readDescriptorTL());					  
					  
					  System.out.print(dlLenght+" "+getDescriptorTLAsHex()+"  ");
					  dlLenght-=DESCRIPTOR_TAG_AND_LENGHT_SIZE;
					  
					  if(getDescriptorTag()==0x4d || getDescriptorTag()==0x54 
							  || getDescriptorTag()==0x55) {
						  
					  } else {
						  //short_event_desc = 0x4D
						  //content_descriptor = 0x54
						  //parental_rating_descriptor = 0x55
	
						  //assert(getDescriptorLenght()<=DvbReader.getDataleft());
					  
						  break;
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
				  
				  // Print data of descriptor.
				  if(DvbReader.getDataleft()>0) {
					  assert(readData());
				  }
				  System.out.print(getDataAsText());
				  if(dLenght>0) {
					  System.out.print("[new packet]");
					  break;
				  }
				  if(DvbReader.getDataleft()<2) {
					  break;
				  }
				  
				  System.out.println();
			  }
		  }
		  assert(DvbReader.readLeft());
	  }
   }
  
}