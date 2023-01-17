import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SubtitleMonitor {

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

	public static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("HH:mm");
	
	public static void main(String[] args) {
		
		ChangeMonitor.followedPackets[320] = 0b01000001000001000000000000000000;		
		
		int subtextFilter = (0x1 << (32-20) | (0x1 << (32-22)));
		
		ChangeMonitor.followedPackets[333] = subtextFilter;
		ChangeMonitor.followedPackets[334] = subtextFilter;
		ChangeMonitor.followedPackets[336] = subtextFilter;
		
		/*
		followedPackets[771] = subtextFilter;
		followedPackets[772] = subtextFilter;
		followedPackets[773] = subtextFilter;
		*/
		
		ChangeMonitor.followedPackets[451] = subtextFilter;
		ChangeMonitor.followedPackets[452] = subtextFilter;
		ChangeMonitor.followedPackets[453] = subtextFilter;
		
		/*
		followedPackets[455] = subtextFilter;
		followedPackets[456] = subtextFilter;
		followedPackets[457] = subtextFilter;
		 */	

		// Check only followed packets
		while (true) {
			
			LineChange lc = ChangeMonitor.readChange();
			
			if(lc==null) {
				return;
			}

			String kanavas = "";

			if(lc.page == 320) {
				kanavas = ANSI_LYELLOW+"***";
			}
			if(lc.page == 333 || lc.page == 771 || lc.page == 451 || lc.page == 455) {
				kanavas = ANSI_LBLUE + "<TV1>";
			}
			if(lc.page == 334 || lc.page == 772 || lc.page == 452 || lc.page == 456) {
				kanavas = ANSI_LMAGENTA + "<TV2>";
			}
			if(lc.page == 336 || lc.page == 773 || lc.page == 453 || lc.page == 457) {
				kanavas = ANSI_WHITE + "<Teema & Fem>";
			}
		
			LocalDateTime date = LocalDateTime.now();
			
			System.out.print(ANSI_WHITE + date.format(dateFormat) + " ");
			
			System.out.print(kanavas);
			
			System.out.println(lc.newline);
							
		}		
	}
}
