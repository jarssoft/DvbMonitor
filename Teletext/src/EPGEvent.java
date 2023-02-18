import java.time.Duration;
import java.time.LocalDateTime;

class EPGEvent {

	final static int BYTESIZE = 12;
	
	static byte[] buffer = new byte[BYTESIZE];

	static int getDescriptorLoopLenght() {
		return ((buffer[10] & 0x0F) << 8) + (buffer[11] & 0xFF);
	}

	static boolean isValid() {
		//Rude way to check is event is valid.
		return ((buffer[2] & 0xFF) >= 0xD6 && (buffer[2] & 0xFF) <= 0xF0); 
	}

	static int next() {

		//assert(DvbReader.getDataleft()>=2);
		//assert(readCRC());

		assert(EPGReader.readFromPackets(buffer, 0));

		//assert(SAFEMODE || (bufferEventHeader[2] & 0xFF) == 0xEA): "Error in EventHeader.";

		assert(isValid());

		EPGReader.monitor.event();

		//System.out.println("  Event: (s" + section_length + ") " + getEventHeaderAsHex());

		//eventLenght = getDescriptorLoopLenght();	  

		return getDescriptorLoopLenght();

	}

	static int timeunit(byte b) {

		return ((b & 0xF0) >> 4) * 10 + (b & 0x0F);
	}

	public static String getEventStart() {

		int MJD = (((buffer[2] & 0xFF) << 8) |(buffer[3] & 0xFF));

		if (MJD==0xFFFF) {
			return null;
		}

		int Yh=(int)((MJD-15078.2)/365.25);

		int Mh = (int)(( MJD - 14956.1 - (int) (Yh * 365.25) ) / 30.6001 );

		int D = MJD - 14956 - (int) (Yh * 365.25) - (int) (Mh * 30.6001 );

		int K = (Mh == 14 || Mh == 15 ? 1 : 0);

		int Y = Yh + K;

		int M = Mh - 1 - K * 12;

		if ((buffer[4] & 0xFF) == 0xFF) {
			return null;
		}

		return LocalDateTime.of(Y + 1900, M, D,
				timeunit(buffer[4]), 
				timeunit(buffer[5]),  
				timeunit(buffer[6])
				).toString();
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

	public static String getEventDuration() {

		return formatDuration(Duration.ofSeconds(timeunit(buffer[7]) * 3600 +
				timeunit(buffer[8]) * 60 +
				timeunit(buffer[9])
				));
	}

}