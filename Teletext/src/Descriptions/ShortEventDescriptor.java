package Descriptions;
import java.io.UnsupportedEncodingException;

public class ShortEventDescriptor extends Descriptor {
	
	/** Returns ISO_639_language_code of short event descriptor as string. */
	public static String getLang() {
		return ParentalRatingDescriptor.getLang(0);
	}
	
	/** Returns ISO_639_language_code of short event descriptor as integer. */
	public static int getLangCode() {

		return fromByteArray(buffer, 0);

	}

	public static String getName() {

		int enStart = 4;
		if(buffer.length > enStart) {					  

			int enCharTable = 0;

			if((buffer[enStart] & 0xFF) < 0x20) {
				enCharTable=buffer[4];
				enStart+=1;
			}

			try {
				return new String(buffer, enStart, 4+buffer[3]-enStart, "ISO-8859-9");
			} catch (UnsupportedEncodingException e) {
				return null;
			}

		}else {
			return null;
		}

	}

	public static String getText() {

		int tStart = 4 + buffer[3] + 1;

		if(buffer.length > tStart) {		

			int tCharTable = 0;

			if((buffer[tStart] & 0xFF) < 0x20) {
				tCharTable=buffer[tStart];
				tStart+=1;
			}

			try {
				return new String(buffer, tStart, buffer.length - tStart, "ISO-8859-9");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}

		}else {
			return null;
		}
	}
}
