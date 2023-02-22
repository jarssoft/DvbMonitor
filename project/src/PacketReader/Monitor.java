package PacketReader;

public class Monitor {

	/** Transport Stream Monitor */
	public static void maind(String[] args) {
		
		int packets=0;
		int[] lastpn=new int[8191+1];
		
		int dataleft=Integer.MAX_VALUE;
		while(Id.read() && packets<500000) {
			
			if(Id.hasSyncByte() && Id.getPid()==0x12) {
					
				System.out.print((packets++) + "  ");						
				System.out.print(Id.getIdAsHex());
				System.out.print(Id.hasSyncByte() ? " OK" : "!!");
										
				if(Id.hasSyncByte()) {
					
					int pid=Id.getPid();
					
					System.out.print("  pid:" + pid + " ");
					System.out.print(Id.getPacketNumber()==((lastpn[pid]+1) % 0x10) ? "": " *");
					
					lastpn[pid] = Id.getPacketNumber(); 

				}
				
				System.out.println();
			
			}
			
			if(DvbReader.readPayload()==false) {
				return;
			}
			
		}
		
	}
	
	/** Transport Stream Monitor */
	public static void main(String[] args) {
		
		int packets=0;
		SeekPID.setFilter(new int[] {0x12});
		
		while(SeekPID.seekPid() == 0x12) {
			
			System.out.print((packets++)+" ");						
			
			System.out.print(Id.getIdAsHex());
			
			System.out.print(Id.hasSyncByte()?"OK":"!!");
									
			if(Id.hasSyncByte()) {
				
				int pid=Id.getPid();
				
				System.out.print("  pid:"+pid+" ");
				
				if(Id.containsNewUnit()) {
					System.out.print(", payload:"+DvbReader.getPayloadPointer()+" ");
				}
								
				if(DvbReader.readLeft()==false) {
				//if(read(bufferPayload)==false) {
					return;
				}
				
			}
			System.out.println();
			
		}
		
	}
	
}
