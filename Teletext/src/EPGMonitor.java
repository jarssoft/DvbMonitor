
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

	void descriptor(int tag, byte[] data) {

		System.out.println("    Desc: (e"+EPGReader.Event.getDescriptorLoopLenght() + ") " + DvbReader.byteBuffertoHex(EPGReader.DescriptorTL.buffer) + "  ");

		if(tag == 0x54) {

			System.out.print(DESCIDENT + DvbReader.byteBuffertoHex(data) + "  ");
			System.out.print(EPGReader.Data.nibbles[(EPGReader.Data.getContentNibble(data) & 0xF0) >> 4]);
			System.out.println();

		}else if(tag == 0x55) {
			
			System.out.println(DESCIDENT + DvbReader.byteBuffertoHex(data));
			
			System.out.println(DESCIDENT + "Countries: " + EPGReader.Data.getParentalRatingDescriptorCountries());
			
			for(int i=0;i<EPGReader.Data.getParentalRatingDescriptorCountries();i++){					
				System.out.println(DESCIDENT + "  Country: " + EPGReader.Data.getParentalRatingDescriptorLang(i));
				System.out.println(DESCIDENT + "  Age: " + EPGReader.Data.getParentalRatingDescriptorAge(i));				
			}
			
			System.out.println(DESCIDENT + "  Age(FIN): " + EPGReader.Data.getParentalRatingDescriptorByBountry(EPGReader.Data.COUNTRY_FIN));
			

		}else if(tag == 0x4d) {

			//if(EPGReader.Data.getShortEventDescriptorLangCode()==EPGReader.Data.COUNTRY_fin) {
				if(EPGReader.Data.getShortEventDescriptorLang()!=null) {
					System.out.println(DESCIDENT + "Lang: " + EPGReader.Data.getShortEventDescriptorLang());
				}
	
				if(EPGReader.Data.getShortEventDescriptorName()!=null) {
					System.out.println(DESCIDENT + "EventName: " + EPGReader.Data.getShortEventDescriptorName());
				}
	
				if(EPGReader.Data.getShortEventDescriptorText()!=null) {
					System.out.println(DESCIDENT + "Text: " + EPGReader.Data.getShortEventDescriptorText());
				}			  
			//}

		}else if(tag == 0x4e) {

			String asString = EPGReader.Data.getDataAsText(data);
			System.out.println(DESCIDENT + asString);

		}else {

			System.out.println(DESCIDENT + EPGReader.Data.getDataAsText(data));

		}
	}

	public static void main(String[] args) {

		EPGMonitor monitor = new EPGMonitor();		
		EPGReader.readEPG(monitor);

	}

}