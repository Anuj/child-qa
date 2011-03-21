package qa.childes.transcripts.parser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class TranscriptOldCha {

	HashMap<String, String> fileProperties = new HashMap<String, String>();
	ArrayList<Line> allLines = new ArrayList<Line>();
	double childAgeYear; //assume child is known as CHI, only one child
	double childAgeMonth;
	ArrayList<String> adults; // KEEPING IT SIMPLE
	String path;
	int transcriptID;
	
	public TranscriptOldCha(String path, int transcriptId) {
		this.path = path;
		this.transcriptID = transcriptId;
		File transcriptFile = new File(path);

		allLines = new ArrayList<Line>(4000);
	    try {
	      //use buffering, reading one line at a time
	      //FileReader always assumes default encoding is OK!
	      BufferedReader input =  new BufferedReader(new FileReader(transcriptFile));
	      try {
	        String line = null; //not declared within while loop
	        adults = new ArrayList<String>();
	        String workingLine = input.readLine();
	        while (( line = input.readLine()) != null && (line.charAt(0) == '@' || line.charAt(0) == '\t')) {
	        	if (line.charAt(0) != '\t') {
		        	if (workingLine.length() > 3 && workingLine.substring(0,3).equals("@ID")) {
		        		//System.out.println(path);
		        		parseIDLine(workingLine);
		        	} else if (workingLine.length() > 10 && workingLine.substring(0,10).toLowerCase().equals("@situation")) {
		        		fileProperties.put("situation", workingLine);
		        	}
		        	workingLine = line;
	        		// assume only one child
	        	//} else if (line.length() > 3 && line.substring(0,3).equals("@situation")) {
	        	} else {
	        		workingLine += line.substring(1);
	        	}
	        }
	        
	        // do something with the last workingLine
	        
	        ArrayList<String> lines = new ArrayList<String>();
	        workingLine = line;
	        line = input.readLine();
	        if (line.charAt(0)== '\t') {
	        	workingLine += line.substring(1);
	        	lines.add(workingLine);
	        	workingLine = input.readLine();
	        } else {
	        	lines.add(workingLine);
	        	workingLine = line;
	        }
	        
	        while (( line = input.readLine()) != null){
	        	if (workingLine.equals("@End")) break;
	        	if (line.charAt(0) != '\t') {
	        		if ( workingLine.charAt(0) == '*') {
		        	//allLines.add(new Line(lines, transcriptId));
		        		lines = new ArrayList<String>();
		        		lines.add(workingLine);
		        	} else {
		        		lines.add(workingLine);
		        	}
	        		workingLine = line;
	        	} else {
	        		workingLine += line.substring(1);
	        	}
	        	
	          //contents.append(line);
	          //contents.append(System.getProperty("line.separator"));
	        }
	      }
	      finally {
	        input.close();
	      }
	    }
	    catch (IOException ex){
	      ex.printStackTrace();
	    }
	    allLines.trimToSize();
	    
	}
	
	public TranscriptOldCha(String path, int transcriptID, String age, String situation) {
		this.path = path;
		this.childAgeYear = Double.parseDouble(age.substring(0,age.indexOf(';')));
		this.childAgeMonth = Double.parseDouble(age.substring(age.indexOf(';')+1));
		if (situation != null)
			fileProperties.put("situation", situation);
		this.transcriptID = transcriptID;
	}

	private void parseIDLine(String line) {
		int pipe1 = line.indexOf('|');
		int pipe2 = line.indexOf('|', pipe1+1);
		int pipe3 = line.indexOf('|', pipe2+1);
		int pipe4 = line.indexOf('|', pipe3+1);
		if (line.substring(pipe2+1, pipe3).equals("CHI")) {
			childAgeYear = Double.parseDouble(line.substring(pipe3+1, pipe3+2)); // assume years are in single digits
			childAgeMonth = Double.parseDouble(line.substring(pipe3+3, pipe4));
		} else {
			adults.add(line.substring(pipe2+1, pipe3));
		}
	}
	
	public int getTotalLines() {
		return allLines.size();
	}
	
	public String getChiAge() {
		return childAgeYear + ";" + childAgeMonth;
	}
	
}
