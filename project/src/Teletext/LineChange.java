package Teletext;
/**
	Change of teletext line.
*/
class LineChange {

	String content;	
	int page;
	
	public LineChange(String aNewline, int aPage) {
		content=aNewline;
		page=aPage;
	}
	
}
