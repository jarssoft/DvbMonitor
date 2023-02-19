package Teletext;
import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class SubtitleMonitor {

	public static class LineCache{
		
		public String content[] = new String[15];
		
		public int index = 0;
		
		public LocalDateTime time = LocalDateTime.now();
		
		public void add(String content) {
			assert(index<content.length()):"LineCache is full.";

			time=LocalDateTime.now();
			
			if(!phraseEnds(content)) {
				time=time.plusSeconds(8);
			}else {
				if(index<7) {
					time=time.plusSeconds(2);
				}
			}
				
			this.content[index++] = content;
		}
		
		public void clear() {
			index=0;
		}
		
	}
	
	public static boolean phraseEnds(String phrase) {
		return phrase.contains(".") || phrase.contains("?") || phrase.contains("!");
	}
	
	/** Constants for coloring text. */
	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	public static final String ANSI_GRAY = "\u001B[37m";
	public static final String ANSI_LRED = "\u001B[91m";
	public static final String ANSI_LGREEN = "\u001B[92m";
	public static final String ANSI_LYELLOW = "\u001B[92m";
	public static final String ANSI_LBLUE = "\u001B[94m";
	public static final String ANSI_LMAGENTA = "\u001B[95m";
	public static final String ANSI_LCYAN = "\u001B[97m";


	public static final String[] channels = {
			ANSI_LYELLOW + "***",
			ANSI_LBLUE + "<TV1",
			ANSI_LMAGENTA + "<TV2",
			ANSI_WHITE + "<Teema"
			};
	
	public static void main(String[] args) {
		
		Reader.teletextpids = new int[]{5000, 5010};
		
		//final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("HH:mm");
		
		LineCache caches[] = {new LineCache(), new LineCache(), new LineCache(), new LineCache()};
		
		String programnames[] = {"---", "---", "---", "---"};
		
		
		/** Filter for current programs. */
		ChangeMonitor.followedPackets[320] = 0b01000001000001000000000000000000;		
		
		/** Filter for subtitle locations. */
		int subtextFilter = (0x1 << (32-20) | (0x1 << (32-22)));
		
		// TV1, TV2 and Teema & Fem in finnish
		ChangeMonitor.followedPackets[333] = subtextFilter;
		ChangeMonitor.followedPackets[334] = subtextFilter;
		ChangeMonitor.followedPackets[336] = subtextFilter;
		
		/*
		// TV1, TV2 and Teema & Fem in swedish
		followedPackets[771] = subtextFilter;
		followedPackets[772] = subtextFilter;
		followedPackets[773] = subtextFilter;
		*/
		
		// TV1, TV2 and Teema & Fem in finnish
		ChangeMonitor.followedPackets[451] = subtextFilter;
		ChangeMonitor.followedPackets[452] = subtextFilter;
		ChangeMonitor.followedPackets[453] = subtextFilter;
		
		/*
		// TV1, TV2 and Teema & Fem in swedish
		followedPackets[455] = subtextFilter;
		followedPackets[456] = subtextFilter;
		followedPackets[457] = subtextFilter;
		 */	

		
		
		/** Checks only followed packets */
		while (true) {
			
			LineChange lc = ChangeMonitor.readChange();
			LocalDateTime date = LocalDateTime.now();
			
			if(lc==null) {
				return;
			}
			
			{
				int id=0;
	
				if(lc.page == 333 || lc.page == 771 || lc.page == 451 || lc.page == 455) {				
					id=1;
				}
				if(lc.page == 334 || lc.page == 772 || lc.page == 452 || lc.page == 456) {
					id=2;
				}
				if(lc.page == 336 || lc.page == 773 || lc.page == 453 || lc.page == 457) {
					id=3;
				}
				
				if(lc.page == 320) {
					if(lc.content.contains("TV1")) {
						programnames[1] = lc.content.substring(14, lc.content.length()).trim();
					}
					if(lc.content.contains("TV2")) {
						programnames[2] = lc.content.substring(14, lc.content.length()).trim();
					}
					if(lc.content.contains("Teema")) {
						programnames[3] = lc.content.substring(14, lc.content.length()).trim();
					}
				}
			
				//System.out.println(ANSI_WHITE + date.format(dateFormat) + " " + channels[id] + ">" + lc.content);
				
				caches[id].add(lc.content);
				
			}
								
			for(int id: new int[] {0, 1, 2, 3}) {

				if(caches[id].index >= 10 || 
						(caches[id].time.until(date, ChronoUnit.SECONDS) >= 0 
						&& caches[id].index>0)) {

					System.out.println(ANSI_WHITE + //date.format(dateFormat) + 
							"   " + channels[id] + " / " + programnames[id] + ">" 
							//+ ANSI_GRAY + date.format(dateFormat) 
							+ ANSI_RESET);
					
					for(int i=0;i<caches[id].index;i++) {
						System.out.println("    "+caches[id].content[i]);
					}
					
					caches[id].clear();
					System.out.println();
				}

			}
	
		}		
		
	}
	
}
