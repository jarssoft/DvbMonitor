import java.io.IOException;

public class Reader {
	
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
  		if(c =='{') {
  			return 'ä';
    	}else if(c =='[') {
    		return 'Ä';
    	}else if(c =='}') {
    		return 'å';
    	}else if(c ==']') {
    		return 'Å';
    	}else if(c =='|') {
    		return 'ö';
    	}else if(c =='\\') { 
    		return 'Ö';
    	}else {
    		return c;
    	}
  	}else{
  		return ' ';
  	}
  }
	  
  public static boolean read(byte[] buffer) {
	  try {
		  return System.in.read(buffer) > 0;
      } catch (IOException e) {
	     e.printStackTrace();
	     return false;
      }	  
  }
  
  public static String toHex(byte[] buffer) {
	  StringBuilder result = new StringBuilder();
      for(byte aByte : buffer) {
   	      result.append(String.format("%02x", aByte)+" ");
      }
      return result.toString(); 
  }
  
  /*****************************/
  
  final private static int ID_SIZE = 4;
  final private static int PREFIX_SIZE = 6;
  final private static int ADDRESS_SIZE = 8;
  final private static int DATA_SIZE = 40;
  final private static int PAYLOAD_SIZE = (PREFIX_SIZE + DATA_SIZE)*4;
  
  private static int packet = 0;
  
  public static boolean readPacket() {
	  
	  if(packet==0) {
		  
		  int pid=0;
		  byte[] bufferPayload = new byte[PAYLOAD_SIZE];
		  byte[] bufferShortPayload = new byte[PAYLOAD_SIZE-16];
		  
		  while(true) {
			  
			  if(!readId()) {
				  return false;
			  }
			  
			  pid = ((0b00011111 & (byte)bufferId[1])<<8) | (0xFF & bufferId[2]);
			  //System.out.println(Reader.getIdAsHex() + " (pid=" + pid + ")");
			  
			  if(pid==5000) {
				  break;
			  }
			  
			  if(bufferId[0] != 0x47) {				  
				  
				  // Sometimes it becomes shorted (-16 bytes) payload 
				  // (or 20 bytes extension for previous). 
				  // I don't know why?
				  if(!read(bufferShortPayload)) {
					  return false;
				  }
				  
		      }else {
		    	  
				  if(!read(bufferPayload)) {
					  return false;
				  }
				  
		      }			  
		  }
	  }
	  
	  if(!readPrefix()) {
		  return false;
	  }
	  
	  packet = (packet + 1) % 4;
	  
	  return true;
	  
  }
  
  /**********************/
  
  private static byte[] bufferId = new byte[ID_SIZE];
  
  public static boolean readId() {
	  return read(bufferId);
  }
  
  public static String getIdAsHex() {
	  return toHex(bufferId);
  }
  
  /**********************/
  
  private static byte[] bufferPrefix = new byte[PREFIX_SIZE];
  
  public static boolean readPrefix() {
	  bufferData = new byte[DATA_SIZE];
	  return read(bufferPrefix);
  }
  
  public static String getPrefixAsHex() {
	  return toHex(bufferPrefix);
  }
  
  public static int getY() {
	  return (decodeHamming(bufferPrefix[5])<<1) | (((byte)bufferPrefix[4] & (byte)0b00000001));
  }
  
  public static int getMagazine() {
	  int magazine = decodeHamming(bufferPrefix[4]) & 0b0111;
	  if(magazine==0) {
		  magazine=8;
	  } 
	  return magazine;
  }
  
  public static boolean isVisible() {
	  return bufferPrefix[3] == (byte)0xe4 && getY() <= 24;
  }
  
  /**********************/
  
  private static byte[] bufferAddress = new byte[ADDRESS_SIZE];
  
  public static boolean readAddress() {
	  assert(getY()==0);
	  bufferData = new byte[DATA_SIZE - ADDRESS_SIZE];
	  return read(bufferAddress);
  }
  
  public static int getPageNumber() {
	  assert(getY()==0);
	  return decodeHamming(bufferAddress[0]) + decodeHamming(bufferAddress[1]) * 10;
  }
  
  public static String getAddressAsHex() {
	  assert(getY()==0);
	  return toHex(bufferAddress);
  }
  
  /**********************/
  
  private static byte[] bufferData = null;
  
  public static boolean readData() {
	  assert(bufferData!=null);
	  return read(bufferData);
  }
    
  public static String getDataAsText() {
	  StringBuilder result = new StringBuilder();
      for (byte aByte : bufferData) {
   	      result.append(decodechar(aByte));
      }
      return result.toString();
  }
  
}