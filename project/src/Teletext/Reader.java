package Teletext;
import PacketReader.SeekPID;
import PacketReader.DataLeft;
import PacketReader.Field;

class Reader {
	
  private static boolean isAsciiPrintable(char ch) {
	  
      return ch >= 32 && ch < 127;
  }
  
  private static int decodeHamming(byte aByte) {
	  
	  int bit1 = aByte & (byte)0b01000000;
	  int bit2 = aByte & (byte)0b00010000;
	  int bit3 = aByte & (byte)0b00000100;
	  int bit4 = aByte & (byte)0b00000001;
	  
	  return (bit4<<3) | bit3 | (bit2>>3) | (bit1>>6);
  }
    
  private static char decodechar(byte aByte) {
	  
  	char c = (char)(Integer.reverse(aByte & 0b11111110) >> 24);

  	if(isAsciiPrintable(c)) {
  		if(c =='{') {return 'ä';}
    	else if(c =='[') {return 'Ä';}
    	else if(c =='}') {return 'å';}
    	else if(c ==']') {return 'Å';}
    	else if(c =='|') {return 'ö';}
    	else if(c =='\\'){return 'Ö';}
    	else if(c =='~') {return 'ü';}
    	else {return c;}
  	}else{
  		return ' ';
  	}
  }

  /**********************/
    
  final private static int STUFF_SIZE = 1;
  final private static int PREFIX_SIZE = 5; 
  final private static int ADDRESS_SIZE = 8;
  final private static int DATA_SIZE = 40;
  
  private static int packet = 0;
  private static int currentPid = 0;
  
  public static boolean readPacket() {
	  
	  if(packet==0) {
		  currentPid = SeekPID.nextPacket();
		  if(currentPid == 0) {
			  return false;
		  }	  
	  }
	  
	  if(!readPrefix()) {
		  return false;
	  }
	  
	  packet = (packet + 1) % 4; 
	  
	  return true;
	  
  }
  
  /**********************/
  
  private static byte[] bufferStuff = new byte[STUFF_SIZE];
  private static byte[] bufferPrefix = new byte[PREFIX_SIZE];
  
  public static boolean readPrefix() {
	  
	  if(DataLeft.getAmount()!=183) {
		  Field.read(bufferStuff);
	  }
	  
	  bufferData = new byte[DATA_SIZE];
	  return Field.read(bufferPrefix);
  }
  
  public static String getPrefixAsHex() {
	  return Field.byteBuffertoHex(bufferPrefix);
  }
  
  public static int getY() {
	  return (decodeHamming(bufferPrefix[4])<<1) | (((byte)bufferPrefix[3] & (byte)0b00000001));
  }
  
  public static int getMagazine() {
	  int magazine = decodeHamming(bufferPrefix[3]) & 0b0111;
	  if(magazine==0) {
		  magazine=8;
	  } 
	  assert(magazine<9);
	  return magazine;
  }
  
  public static boolean isVisible() {
	  return bufferPrefix[2] == (byte)0xe4 && getY() <= 24;
  }
  
  /**********************/
  
  private static byte[] bufferAddress = new byte[ADDRESS_SIZE];
  
  public static boolean readAddress() {
	  assert(getY()==0);
	  bufferData = new byte[DATA_SIZE - ADDRESS_SIZE];
	  return Field.read(bufferAddress);
  }
  
  public static int getPageNumber() {
	  assert(getY()==0);
	  int page = decodeHamming(bufferAddress[0]) + decodeHamming(bufferAddress[1]) * 10;
	  //assert(page<100);
	  return page;
  }
  
  public static String getAddressAsHex() {
	  assert(getY()==0);
	  return Field.byteBuffertoHex(bufferAddress);
  }
  
  /**********************/
  
  private static byte[] bufferData = null;
  
  public static boolean readData() {
	  assert(bufferData != null);
	  return Field.read(bufferData);
  }
    
  public static String getDataAsText() {
	  StringBuilder result = new StringBuilder();
      for (byte aByte : bufferData) {
   	      result.append(decodechar(aByte));
      }
      return result.toString();
  }

  public static int getCurrentPID() {
	  return currentPid;
  }

}