package EPG;

import java.time.format.DateTimeFormatter;

class SimpleMonitor implements Client {

	boolean printOn = false;
	String oldDate = "";

	//49 - mtv3 
	//81 - teema
	public void section() {

		printOn = (FieldSection.getServiceId() != 49 && FieldSection.getTableID() == 0x4e ? true: false);
		if(printOn) {
			System.out.println("  Service: " + FieldSection.getServiceId()+", Id:"+FieldSection.getTableID());
		}

	}

	public void event() {
		
		if(printOn) {
			String datestr = FieldEvent.getEventStart().format(DateTimeFormatter.ISO_LOCAL_DATE);
			
			if(!datestr.equals(oldDate)) {
				System.out.println("   " + datestr);
				oldDate = datestr;
			}
		
		
			System.out.print("     " + FieldEvent.getEventStart().format(DateTimeFormatter.ISO_LOCAL_TIME));
		}

	}

	final private String DESCIDENT = "      ";

	public void descriptor(int tag) {

		if(printOn) {
				
			
			switch (tag) {
			
			  case DescriptorShortEvent.TAG:
				    
					if(DescriptorShortEvent.getLangCode()==Descriptor.COUNTRY_fin) {
			
						if(DescriptorShortEvent.getName()!=null) {
							System.out.print(" " + DescriptorShortEvent.getName());
						}
			
						if(DescriptorShortEvent.getText()!=null) {
							//System.out.println(DESCIDENT 
							//		+ "Text: " + DescriptorShortEvent.getText());
						}			  
					}
					
			    break;
			    
			  case DescriptorContent.TAG:
				  
					//System.out.print(" "+DescriptorContent.nibbles[(DescriptorContent.getContentNibble(Descriptor.buffer) & 0xF0) >> 4]);
					System.out.println();
					
			    break;
			    
			  case DescriptorParentalRating.TAG:
					int age = DescriptorParentalRating.getByBountry(Descriptor.COUNTRY_FIN);
					if(age>0) {
						System.out.println(DESCIDENT + "  Ikä: " + age);
					}
					
			    break;
			    
	
	
			  default:
				  //System.out.println(DESCIDENT + Descriptor.getDataAsText(Descriptor.buffer));
				  
			    break;
			}
		}

	}

	public static void main(String[] args) {

		SimpleMonitor monitor = new SimpleMonitor();		
		Reader.readEPG(monitor);

	}

}