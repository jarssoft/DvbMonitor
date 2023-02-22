package Teletext;
import java.util.LinkedHashMap;

class ChangeMonitor {

	public static int followedPackets[] = new int[1900];
	private static int current_page[] = {0, 0};
	
	private static LinkedHashMap<Integer, String> prevdata
			= new LinkedHashMap<Integer, String>();
	
	public static LineChange readChange() {

		while (Reader.readPacket()) {
			if(Reader.isVisible()) { 

				int kanava = (Reader.getCurrentPID()==5000 ? 0 : 1);
				//assert(DvbReader.getCurrentPID()==5000);

				int y = Reader.getY();

				// Read page number from header packet?
				if(y==0) {

					if(!Reader.readAddress()) {
						break;
					}

					current_page[kanava] = kanava * 1000 + Reader.getMagazine() * 100 + Reader.getPageNumber();

				}

				if(!Reader.readData()) {
					break;
				}

				// Check only followed packets

				if(current_page[kanava] < 1900 && followedPackets[current_page[kanava]]!=0) {

					if((followedPackets[current_page[kanava]] & (1 << (32-y))) != 0) {	
						
						String data = Reader.getDataAsText();
						
						if(!data.equals(prevdata.get(current_page[kanava] * 25 + y))) {
							prevdata.put(current_page[kanava]*25+y, data);
						
							return new LineChange(data, current_page[kanava]);
						}
						
					}
				}
			}else {
				if(!Reader.readData()) {
					break;
				}
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		
		followedPackets[100] = 0b00000111111110000000000000000000;
		followedPackets[320] = 0b01000001000001000000000000000000;		
		followedPackets[406] = 0x1 << 16;
		followedPackets[1320] = (0x1 << (32-5) | (0x1 << (32-7)));

		while (true) {
			
			LineChange lc = readChange();
			
			if(lc==null) {
				return;
			}
			
			System.out.println(String.format("%04d", lc.page) + "> " + lc.content);
			
		}
		
	}
}
