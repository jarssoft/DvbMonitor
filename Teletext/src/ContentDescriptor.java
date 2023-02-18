
public class ContentDescriptor extends Descriptor {

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
	
	public static byte getContentNibble(byte[] bufferData) {

		return bufferData[0];

	}
	
}
