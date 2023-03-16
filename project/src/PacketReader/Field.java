package PacketReader;

import java.io.IOException;
import java.io.InputStream;


public class Field {
	
	static public InputStream input = System.in;
	
	/** Read byte-buffer. Return true if succeed, otherwise false. */
	public static boolean read(byte[] buffer) {
		
		assert(buffer.length > 0): "buffer.length must be greater than zero, but buffer.length = " + buffer.length;
		assert(buffer.length <= DataLeft.getAmount()): "No enought data in this packet. getDataleft() = " + DataLeft.getAmount();
		
		try {
			
			DataLeft.reduce(buffer.length);
			
			int readed = input.read(buffer);
			//System.out.println(readed);
			//buffer = System.in.readNBytes(buffer.length);
			//assert(readed==buffer.length):"Only "+readed+" read. Must read "+buffer.length;
			if(readed<buffer.length) {
				//System.out.println("Only "+readed+" readed.");
				assert(readed>=0) : "End of data!";
				if(readed==-1) {
					System.exit(0);
				}
				  
				int second = input.read(buffer, readed, buffer.length-readed);
				//System.out.println("Second try: "+second);
				readed+=second;
				//System.out.println("Together "+readed+" readed.");
			}
			return readed==buffer.length;
			
		} catch (IOException e) {
			
			e.printStackTrace();
			return false;
			
		}
		
	}
	
	/** Returns hexadecimal presentation of byte-buffer @buffer. */
	public static String byteBuffertoHex(byte[] buffer) {
		StringBuilder result = new StringBuilder();
		for(byte aByte : buffer) {
			result.append(String.format("%02x", aByte)+" ");
		}
		return result.toString(); 
	}
	
}
