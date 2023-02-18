
public class EPGMonitor {

	public void section() {

		System.out.println("Section: " + DvbReader.byteBuffertoHex(EPGSection.buffer)
				+ ", correct: " +EPGSection.isValid()
				+ ", lenght: "+EPGSection.getLenght());
		System.out.println("  Service: " + EPGSection.getServiceId());

	}

	public void event() {

		System.out.println("  Event: " + DvbReader.byteBuffertoHex(EPGSection.buffer));
		System.out.println("    Starts: " + EPGEvent.getEventStart() 
				+ ", Duration: " + EPGEvent.getEventDuration());

	}

	final private String DESCIDENT = "      ";

	void descriptor(int tag, byte[] data) {

		System.out.println("    Desc: (e"+EPGEvent.getDescriptorLoopLenght() + ") " + DvbReader.byteBuffertoHex(EPGReader.DescriptorTL.buffer) + "  ");

		if(tag == 0x54) {

			System.out.print(DESCIDENT + DvbReader.byteBuffertoHex(data) + "  ");
			System.out.print(ContentDescriptor.nibbles[(ContentDescriptor.getContentNibble(data) & 0xF0) >> 4]);
			System.out.println();

		}else if(tag == 0x55) {
			
			System.out.println(DESCIDENT + DvbReader.byteBuffertoHex(data));
			
			System.out.println(DESCIDENT + "Countries: " + ParentalRatingDescriptor.getCountries());
			
			for(int i=0;i<ParentalRatingDescriptor.getCountries();i++){					
				System.out.println(DESCIDENT + "  Country: " + ParentalRatingDescriptor.getLang(i));
				System.out.println(DESCIDENT + "  Age: " + ParentalRatingDescriptor.getAge(i));				
			}
			
			System.out.println(DESCIDENT + "  Age(FIN): " + ParentalRatingDescriptor.getByBountry(Descriptor.COUNTRY_FIN));
			
		}else if(tag == 0x4d) {

			//if(ShortEventDescriptor.getLangCode()==EPGData.COUNTRY_fin) {
				if(ShortEventDescriptor.getLang()!=null) {
					System.out.println(DESCIDENT 
							+ "Lang: " + ShortEventDescriptor.getLang());
				}
	
				if(ShortEventDescriptor.getName()!=null) {
					System.out.println(DESCIDENT 
							+ "EventName: " + ShortEventDescriptor.getName());
				}
	
				if(ShortEventDescriptor.getText()!=null) {
					System.out.println(DESCIDENT 
							+ "Text: " + ShortEventDescriptor.getText());
				}			  
			//}

		}else if(tag == 0x4e) {

			String asString = Descriptor.getDataAsText(data);
			System.out.println(DESCIDENT + asString);

		}else {

			System.out.println(DESCIDENT + Descriptor.getDataAsText(data));

		}
	}

	public static void main(String[] args) {

		EPGMonitor monitor = new EPGMonitor();		
		EPGReader.readEPG(monitor);

	}

}