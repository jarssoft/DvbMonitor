
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

	public void descriptor(int tag, byte[] data) {
		
		  System.out.print("    Desc: (e"+EPGReader.Event.getDescriptorLoopLenght()+") " + DvbReader.byteBuffertoHex(EPGReader.DescriptorTL.buffer) + "  ");
		
		  if(tag == 0x54) {

			  System.err.print(DvbReader.byteBuffertoHex(data)+"  ");
			  System.out.print(EPGReader.Data.nibbles[(EPGReader.Data.getContentNibble(data) & 0xF0) >> 4]);
			  System.out.println();

		  }else if(tag == 0x55) {

			  //System.out.print(getDataAsText()+"  "+getParentalRating());
			  System.out.println(EPGReader.Data.getDataAsText(data));

		  }else {

			  System.out.println(EPGReader.Data.getDataAsText(data));

		  }
	}

	public static void main(String[] args) {

		EPGMonitor monitor = new EPGMonitor();		
		EPGReader.readEPG(monitor);

	}

}