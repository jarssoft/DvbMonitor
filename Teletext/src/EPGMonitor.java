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
	
	public void descriptor(int tag, byte[] data) {
		
		  System.out.println("    Desc: (e"+EPGReader.Event.getDescriptorLoopLenght()+") " + DvbReader.byteBuffertoHex(EPGReader.DescriptorTL.buffer) + "  ");
		
		  if(tag == 0x54) {

			  System.err.print(DESCIDENT+DvbReader.byteBuffertoHex(data)+"  ");
			  System.out.print(EPGReader.Data.nibbles[(EPGReader.Data.getContentNibble(data) & 0xF0) >> 4]);
			  System.out.println();

		  }else if(tag == 0x55) {

			  System.out.println(DESCIDENT+EPGReader.Data.getDataAsText(data));
			  
		  }else if(tag == 0x4d) {

			  String asString="";
			  try {
				  asString = new String(data, "ISO-8859-9");
			  } catch (UnsupportedEncodingException e) {
				  e.printStackTrace();
			  }
			  
			  if(data.length>=3) {

				  String lang = asString.substring(0,3);
				  System.out.println(DESCIDENT+"Lang:  "+lang);
				  
				  int start = 4;
				  if(data.length > start) {					  
					  int codepage = 0;
					  if((data[start] & 0xFF) < 0x20) {
						  codepage=data[4];
						  start+=1;						  
						  System.out.println(DESCIDENT+"Codepage.Title: "+codepage);
					  }
		
					  String title = asString.substring(start, 4+data[3]);
					  System.out.println(DESCIDENT+"Title: "+title);
					  
					  int dstart = 4+data[3]+1;
					  if(data.length > dstart) {								  						  
						  int dcodepage = 0;
						  if((data[dstart] & 0xFF) < 0x20) {
							  dcodepage=data[dstart];
							  dstart+=1;							  
							  System.out.println(DESCIDENT+"Codepage.Desc: "+dcodepage);
						  }
						  
						  String desc = asString.substring(dstart, data.length);
						  System.out.println(DESCIDENT+"Desc:  "+desc);
					  }
					  
				  }			  
			  
			  }
			  
			  //System.out.println(asString);
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