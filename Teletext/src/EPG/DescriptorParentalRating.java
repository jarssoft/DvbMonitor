package EPG;

public class DescriptorParentalRating extends Descriptor {
	
	static final public int TAG = 0x55;
	
	/** Returns amount of countries of rating values. */
	public static int getCountries() {
		return buffer.length / 4;
	}

	/** Returns 24-bit field identifies a country using the 3-character code as specified in ISO 3166 as string.*/
	public static String getLang(int n) {
		assert(n<getCountries());
		int i = n * 4;
		if(buffer.length>=3) {
			return new String(buffer, i, 3);
		}else {
			return null;
		}
	}

	public int getParentalRating(byte[] bufferData) {

		return (bufferData[3]>=0x01 && bufferData[3]<=0x0F ? bufferData[3] + 3 : 0);
	}
	
	/** Returns 24-bit field identifies a country using the 3-character code as specified in ISO 3166 as integer. */
	public static int getParentalRatingDescriptorCountry(int n) {
		assert(n<getCountries());

		return fromByteArray(buffer, n*4);

	}

	/** Returns age by country number. */
	public static int getAge(int n) {
		assert(n<getCountries());

		int rating = buffer[n*4+3] & 0xFF;
		if(rating >= 0x01 && rating <= 0x0F) {
			return rating+3;
		}else {
			return 0;
		}
	}
	
	/** Returns age by country number. */
	public static int getByBountry(int country_n) {
	
		for(int i=0;i<getCountries();i++){
			
			final int countryiso8859 = getParentalRatingDescriptorCountry(i);
			
			if(countryiso8859 == country_n) {
				
				return getAge(i);
				
			}
			
		}
		
		return 0;
	}	
}
