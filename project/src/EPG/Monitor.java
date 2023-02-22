package EPG;
import PacketReader.DvbReader;
import PacketReader.Field;

public class Monitor implements Client {

	public void section() {

		System.out.println("Section: " + Field.byteBuffertoHex(FieldSection.buffer)
				+ ", correct: " +FieldSection.isValid()
				+ ", lenght: "+FieldSection.getLenght());
		System.out.println("  Service: " + FieldSection.getServiceId());

	}

	public void event() {

		System.out.println("  Event: " + Field.byteBuffertoHex(FieldSection.buffer));
		System.out.println("    Starts: " + FieldEvent.getEventStart() 
				+ ", Duration: " + FieldEvent.getEventDuration());

	}

	final private String DESCIDENT = "      ";

	public void descriptor(int tag) {

		System.out.println("    Desc: (e"+FieldEvent.getDescriptorLoopLenght() + ") " + Field.byteBuffertoHex(FieldDescriptorTL.buffer) + "  ");

		switch (tag) {
		
		  case DescriptorContent.TAG:
			  
			  System.out.print(DESCIDENT + Field.byteBuffertoHex(Descriptor.buffer) + "  ");
				System.out.print(DescriptorContent.nibbles[(DescriptorContent.getContentNibble(Descriptor.buffer) & 0xF0) >> 4]);
				System.out.println();
				
		    break;
		    
		  case DescriptorParentalRating.TAG:
			  
				System.out.println(DESCIDENT + Field.byteBuffertoHex(Descriptor.buffer));
				
				System.out.println(DESCIDENT + "Countries: " + DescriptorParentalRating.getCountries());
				
				for(int i=0;i<DescriptorParentalRating.getCountries();i++){					
					System.out.println(DESCIDENT + "  Country: " + DescriptorParentalRating.getLang(i));
					System.out.println(DESCIDENT + "  Age: " + DescriptorParentalRating.getAge(i));				
				}
				
				System.out.println(DESCIDENT + "  Age(FIN): " + DescriptorParentalRating.getByBountry(Descriptor.COUNTRY_FIN));
				
		    break;
		    
		  case DescriptorShortEvent.TAG:
		    
				//if(ShortEventDescriptor.getLangCode()==EPGData.COUNTRY_fin) {
				if(DescriptorShortEvent.getLang()!=null) {
					System.out.println(DESCIDENT 
							+ "Lang: " + DescriptorShortEvent.getLang());
				}
	
				if(DescriptorShortEvent.getName()!=null) {
					System.out.println(DESCIDENT 
							+ "EventName: " + DescriptorShortEvent.getName());
				}
	
				if(DescriptorShortEvent.getText()!=null) {
					System.out.println(DESCIDENT 
							+ "Text: " + DescriptorShortEvent.getText());
				}			  
			//}
				
		    break;

		  default:
			  System.out.println(DESCIDENT + Descriptor.getDataAsText(Descriptor.buffer));
			  
		    break;
		}

	}

	public static void main(String[] args) {

		Monitor monitor = new Monitor();		
		Reader.readEPG(monitor);

	}

}