import java.time.Duration;
import java.time.LocalDateTime;

public class EPGMonitor {

	private static boolean isAsciiPrintable(char ch) {

		return ch >= 32 && ch < 127;
	}

	private static char decodechar(byte aByte) {

		char c = (char)aByte;

		if(isAsciiPrintable(c)) {
			return c;
		}else{
			return '.';
		}
	}

	/**********************/
	
	public static int getServiceId(byte[] bufferSection) {
		return (((bufferSection[3] & (byte)0xFF)) << 8) + (bufferSection[4] & 0xFF);
	}
	
	byte[] bufferEventHeader;	

	static int timeunit(byte b) {

		return ((b & 0xF0) >> 4) * 10 + (b & 0x0F);
	}

	private String getEventStart() {

		int MJD = (((bufferEventHeader[2] & 0xFF) << 8) |(bufferEventHeader[3] & 0xFF));

		if (MJD==0xFFFF) {
			return null;
		}

		int Yh=(int)((MJD-15078.2)/365.25);

		int Mh = (int)(( MJD - 14956.1 - (int) (Yh * 365.25) ) / 30.6001 );

		int D = MJD - 14956 - (int) (Yh * 365.25) - (int) (Mh * 30.6001 );

		int K = (Mh == 14 || Mh == 15 ? 1 : 0);

		int Y = Yh + K;

		int M = Mh - 1 - K * 12;

		if ((bufferEventHeader[4] & 0xFF) == 0xFF) {
			return null;
		}

		return LocalDateTime.of(Y + 1900, M, D,
				timeunit(bufferEventHeader[4]), 
				timeunit(bufferEventHeader[5]),  
				timeunit(bufferEventHeader[6])
				).toString();
	}


	public int getParentalRating(byte[] bufferData) {
		return (bufferData[3]>=0x01 && bufferData[3]<=0x0F ? bufferData[3] + 3 : 0);
	}

	private String getEventDuration() {

		return formatDuration(Duration.ofSeconds(timeunit(bufferEventHeader[7]) * 3600 +
				timeunit(bufferEventHeader[8]) * 60 +
				timeunit(bufferEventHeader[9])
				));
	}

	public static String formatDuration(Duration duration) {

		long seconds = duration.getSeconds();
		long absSeconds = Math.abs(seconds);
		String positive = String.format(
				"%d:%02d:%02d",
				absSeconds / 3600,
				(absSeconds % 3600) / 60,
				absSeconds % 60);
		return seconds < 0 ? "-" + positive : positive;
	}

	private static final String[] nibbles = {
			"undefined", 
			"Movie/Drama", 
			"News/Current affairs",
			"Show/Game show", 
			"Sports",
			"Children's/Youth programmes",
			"Music/Ballet/Dance",
			"Arts/Culture (without music)",
			"Social/Political issues/Economics",
			"Education/ Science/Factual topics",
			"Leisure hobbies" ,
			"Leisure hobbies",
			"", "", "", ""
	};
	
	public static byte getContentNibble(byte[] bufferData) {
		return bufferData[0];
	}
	  
	public static String getDataAsText(byte[] bufferData) {
		StringBuilder result = new StringBuilder();
		for (byte aByte : bufferData) {
			result.append(decodechar(aByte));
		}
		return result.toString();
	}
	
	/*********************************/

	public void section(byte[] bufferSection) {
		
		System.out.println("  Service: " + getServiceId(bufferSection));
		
	}
	  
	public void event(byte[] bufferEventHeader) {
		
		this.bufferEventHeader = bufferEventHeader;
		System.out.println("    Starts: " + getEventStart() + ", Duration: "+getEventDuration());
		
	}
	  
	public void descriptor(int tag, byte[] data) {
		
		  if(tag == 0x54) {

			  System.err.print(DvbReader.byteBuffertoHex(data)+"  ");
			  System.out.print(nibbles[(getContentNibble(data) & 0xF0) >> 4]);
			  System.out.println();

		  }else if(tag == 0x55) {

			  //System.out.print(getDataAsText()+"  "+getParentalRating());
			  System.out.println(getDataAsText(data));

		  }else {

			  System.out.println(getDataAsText(data));

		  }
	}

	public static void main(String[] args) {

		EPGMonitor monitor = new EPGMonitor();
		
		EPGReader.readEPG(monitor);

	}


}
