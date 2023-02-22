package Teletext;

import PacketReader.SeekPID;

class Monitor {
	  
	  public static void main(String[] args) {
		  
		  //TeletextReader.teletextpids = new int[]{5000};
		  //TeletextReader.teletextpids = new int[]{5010};

		  SeekPID.setFilter(new int[] {4372});

		  int last_y=-1;
		  
	      while (Reader.readPacket()) {
						   
			   StringBuilder result = new StringBuilder();
			   
		       result.append(Reader.getPrefixAsHex() + " ");
		       		
		       int y = Reader.getY();
		       
		       if(y==0) {
 
				   if(!Reader.readAddress()) {
					   break;
				   }
				   
				   result.append(" "+Reader.getAddressAsHex() + "   ");
			       
			       int page_number = Reader.getPageNumber() + Reader.getMagazine() * 100;
			       
			       result.append("           P" + page_number);
			       
			       result.append("\n                    ");
	  
				   } 
					   	
			   if(!Reader.readData()) {
				   break;
			   }
		       
		       result.append(Reader.getDataAsText() + " " + String.format("%02d", y) + ((last_y+1)==y ? " " : "*")); 
		       
		       
		       
		       //if(DvbReader.getCurrentPID()==4372) {
		    	   
			       if(Reader.isVisible()) {
			    	   
			    	   System.out.println(result.toString());
			    	   last_y=y;

			       }

		       //}
		       
	       }
	     
	   }
}
