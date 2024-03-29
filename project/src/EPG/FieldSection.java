package EPG;
import PacketReader.DataLeft;
import PacketReader.SeekPID;

class FieldSection {

	final static int BYTESIZE = 14;  
	
	public static byte[] buffer = new byte[BYTESIZE];

	public static int getTableID() {
		return buffer[0];
	}

	public static int getLenght() {
		return (((buffer[1] & (byte)0x0F)) << 8) | (buffer[2] & 0xFF);
	}

	public static boolean isValid() {
		int ti = getTableID();	

		return ((ti==0x4e || ti==0x4f || (ti & 0xF0)==0x50 || (ti & 0xF0)==0x60)
				&& getLenght()>=Reader.SECTIONZERO);
	}

	public static int getServiceId() {  
		return (((buffer[3] & (byte)0xFF)) << 8) + (buffer[4] & 0xFF); 
	}

	public static int next() {

		if(DataLeft.getAmount()==0) {
			SeekPID.nextPayloadStart();
		}

		//Read section

		Reader.readFromPackets(buffer, 0);
		//assert();

		Reader.monitor.section();

		assert(isValid());

		return getLenght();
	}
}