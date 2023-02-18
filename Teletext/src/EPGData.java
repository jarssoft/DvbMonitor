import java.io.UnsupportedEncodingException;

public class EPGData {

	public static final int COUNTRY_FIN = 0x46494E;
	public static final int COUNTRY_fin = 0x66696e;

	public static byte[] buffer;

	public static boolean read() {
		assert(buffer!=null);

		return EPGReader.readFromPackets(buffer,0);
	}


	public int getParentalRating(byte[] bufferData) {

		return (bufferData[3]>=0x01 && bufferData[3]<=0x0F ? bufferData[3] + 3 : 0);

	}

	public static final String[] nibbles = {

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


	// packing an array of 4 bytes to an int, big endian, clean code
	private static int fromByteArray(byte[] bytes, int i) {			
		return ((bytes[i+0] & 0xFF) << 16) | 
				((bytes[i+1] & 0xFF) << 8) | 
				((bytes[i+2] & 0xFF) << 0 ) ;
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * */
	
	/** Returns amount of countries of rating values. */
	public static int getParentalRatingDescriptorCountries() {
		return buffer.length / 4;
	}

	/** Returns 24-bit field identifies a country using the 3-character code as specified in ISO 3166 as string.*/
	public static String getParentalRatingDescriptorLang(int n) {
		assert(n<getParentalRatingDescriptorCountries());
		int i = n * 4;
		if(buffer.length>=3) {
			return new String(buffer, i, 3);
		}else {
			return null;
		}
	}

	/** Returns 24-bit field identifies a country using the 3-character code as specified in ISO 3166 as integer. */
	public static int getParentalRatingDescriptorCountry(int n) {
		assert(n<getParentalRatingDescriptorCountries());

		return fromByteArray(buffer, n*4);

	}

	/** Returns age by country number. */
	public static int getParentalRatingDescriptorAge(int n) {
		assert(n<getParentalRatingDescriptorCountries());

		int rating = buffer[n*4+3] & 0xFF;
		if(rating >= 0x01 && rating <= 0x0F) {
			return rating+3;
		}else {
			return 0;
		}
	}
	
	/** Returns age by country number. */
	public static int getParentalRatingDescriptorByBountry(int country_n) {
	
		for(int i=0;i<getParentalRatingDescriptorCountries();i++){
			
			final int countryiso8859 = getParentalRatingDescriptorCountry(i);
			
			if(countryiso8859 == country_n) {
				
				return getParentalRatingDescriptorAge(i);
				
			}
			
		}
		
		return 0;
	}
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * */
	
	/** Returns ISO_639_language_code of short event descriptor as string. */
	public static String getShortEventDescriptorLang() {

		return getParentalRatingDescriptorLang(0);

	}
	
	/** Returns ISO_639_language_code of short event descriptor as integer. */
	public static int getShortEventDescriptorLangCode() {

		return fromByteArray(buffer, 0);

	}

	public static String getShortEventDescriptorName() {

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

	public static String getShortEventDescriptorText() {

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
