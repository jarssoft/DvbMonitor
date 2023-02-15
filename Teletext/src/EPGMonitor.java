import java.io.UnsupportedEncodingException;

public class EPGMonitor {

	public void section() {
		
		System.out.println("Section: " + DvbReader.byteBuffertoHex(EPGReader.Section.buffer)
				+ ", correct: " +EPGReader.Section.isValid()
				+ ", lenght: "+EPGReader.Section.getLenght());
		System.out.println("  Service: " + EPGReader.Section.getServiceId());
		
	}
	
	public void event() {
		
		System.out.println("  Event: " + DvbReader.byteBuffertoHex(EPGReader.Section.buffer));
		System.out.println("    Starts: " + EPGReader.Event.getEventStart() 
							+ ", Duration: " + EPGReader.Event.getEventDuration());
		
	}

	final private String DESCIDENT = "      ";
	
	String getShortEventDescriptorLang() {

		if(EPGReader.Data.buffer.length>=3) {
			return  new String(EPGReader.Data.buffer, 0, 3);
		}else {
			return null;
		}

	}
	
	String getShortEventDescriptorName() {

		  int enStart = 4;
		  if(EPGReader.Data.buffer.length > enStart) {					  
			  
			  int enCharTable = 0;
			  
			  if((EPGReader.Data.buffer[enStart] & 0xFF) < 0x20) {
				  enCharTable=EPGReader.Data.buffer[4];
				  enStart+=1;
			  }
			  
			  try {
				  return new String(EPGReader.Data.buffer, enStart, 4+EPGReader.Data.buffer[3]-enStart, "ISO-8859-9");
			  } catch (UnsupportedEncodingException e) {
				  return null;
			  }

		  }else {
			  
			  return null;
			  
		  }

	}
	
	String getShortEventDescriptorText() {
		
		  int tStart = 4 + EPGReader.Data.buffer[3] + 1;
		  
		  if(EPGReader.Data.buffer.length > tStart) {		
			  
			  int tCharTable = 0;
			  
			  if((EPGReader.Data.buffer[tStart] & 0xFF) < 0x20) {
				  tCharTable=EPGReader.Data.buffer[tStart];
				  tStart+=1;
			  }
			  
			  try {
				  return new String(EPGReader.Data.buffer, tStart, EPGReader.Data.buffer.length - tStart, "ISO-8859-9");
			  } catch (UnsupportedEncodingException e) {
				  e.printStackTrace();
				  return null;
			  }
			  
		  }else {
			  
			  return null;
			  
		  }
	}
	
	 void descriptor(int tag, byte[] data) {
		
		  System.out.println("    Desc: (e"+EPGReader.Event.getDescriptorLoopLenght() + ") " + DvbReader.byteBuffertoHex(EPGReader.DescriptorTL.buffer) + "  ");
		
		  if(tag == 0x54) {

			  System.err.print(DESCIDENT + DvbReader.byteBuffertoHex(data) + "  ");
			  System.out.print(EPGReader.Data.nibbles[(EPGReader.Data.getContentNibble(data) & 0xF0) >> 4]);
			  System.out.println();

		  }else if(tag == 0x55) {

			  System.out.println(DESCIDENT+EPGReader.Data.getDataAsText(data));
			  
		  }else if(tag == 0x4d) {

			  if(getShortEventDescriptorLang()!=null) {
				  System.out.println(DESCIDENT + "Lang: "+getShortEventDescriptorLang());
			  }
			  
			  if(getShortEventDescriptorName()!=null) {
				  System.out.println(DESCIDENT + "EventName: "+getShortEventDescriptorName());
			  }
			  
			  if(getShortEventDescriptorText()!=null) {
				  System.out.println(DESCIDENT + "Text: "+getShortEventDescriptorText());
			  }			  

		  }else if(tag == 0x4e) {

			  String asString = EPGReader.Data.getDataAsText(data);
			  /*
			  if(data.length>=3) {
				  System.out.println(asString);
				  
				  String lang = asString.substring(0,3);
				  System.out.println("      lang:  "+lang);
				  
				  if(data.length>4) {
		
					  String title = asString.substring(4, 4+data[3]);
					  System.out.println("      text: "+title);
					  
				  }			  
			  
			  }
			  */
			  System.out.println(DESCIDENT+asString);

		  }else {
			  
			  System.out.println(DESCIDENT+EPGReader.Data.getDataAsText(data));
			  
		  }
	}

	public static void main(String[] args) {

		EPGMonitor monitor = new EPGMonitor();		
		EPGReader.readEPG(monitor);

	}

}