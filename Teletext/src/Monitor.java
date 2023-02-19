
public class Monitor {
	  
	  public static void main(String[] args) {
		  
		  //TeletextReader.teletextpids = new int[]{5000};
		  //TeletextReader.teletextpids = new int[]{5010};
		  TeletextReader.teletextpids = new int[]{4372};

		  int last_y=-1;
		  
	      while (TeletextReader.readPacket()) {
						   
			   StringBuilder result = new StringBuilder();
			   
		       result.append(TeletextReader.getPrefixAsHex() + " ");
		       		
		       int y = TeletextReader.getY();
		       
		       if(y==0) {
 
				   if(!TeletextReader.readAddress()) {
					   break;
				   }
				   
				   result.append(" "+TeletextReader.getAddressAsHex() + "   ");
			       
			       int page_number = TeletextReader.getPageNumber() + TeletextReader.getMagazine() * 100;
			       
			       result.append("           P" + page_number);
			       
			       result.append("\n                    ");
	  
				   } 
					   	
			   if(!TeletextReader.readData()) {
				   break;
			   }
		       
		       result.append(TeletextReader.getDataAsText() + " " + String.format("%02d", y) + ((last_y+1)==y ? " " : "*")); 
		       
		       
		       
		       //if(DvbReader.getCurrentPID()==4372) {
		    	   
			       if(TeletextReader.isVisible()) {
			    	   
			    	   System.out.println(result.toString());
			    	   last_y=y;

			       }

		       //}
		       
	       }
	     
	   }
}
