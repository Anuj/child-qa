package qa.childes.transcripts.parser;
import java.util.ArrayList;

// Assumption: conversations are focused on nouns
public class ContextState {
	public ArrayList<String> nouns;
	public ArrayList<Line> lines;
	public ContextState(ArrayList<String> nouns, ArrayList<Line> lines) {
		this.nouns = nouns;
		this.lines = lines;
	}
	
	// collapse the context states between start and end and make this a contextState
	public ContextState(ArrayList<ContextState> c, int start, int end) {
		nouns = c.get(start).nouns;
		lines = new ArrayList<Line>();
		for (int i = start; i <= end; ++i) {
			lines.addAll(c.get(i).lines);
		}
	}
	public ContextState(String noun, ArrayList<Line> lines) {
		nouns = new ArrayList<String>(1);
		nouns.add(noun);
		this.lines = lines;
		
	}
	
	
	

}
