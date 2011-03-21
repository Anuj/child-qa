// this class only works on CHAI files. CHAI was the modified CHA file created using the script sent out a few weeks ago.
package qa.childes.transcripts.parser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Transcript {

	public HashMap<String, String> fileProperties = new HashMap<String, String>(); // this overrides IDs, because there can be multiple IDs
	public ArrayList<String> filePropertiesArr = new ArrayList<String>();
	public ArrayList<Line> allLines = new ArrayList<Line>();
	double childAgeYear; //assume child is known as CHI, only one child
	double childAgeMonth;
	ArrayList<String> adults; // label the other participants as children (this may not be the case)
	public String path;
	public int transcriptID;
	
	public Transcript(String path, int transcriptId) {
		this.path = path;
		this.transcriptID = transcriptId;
		File transcriptFile = new File(path);

		allLines = new ArrayList<Line>(4000);
		int spokenLineIdx = 0;
		
	    try {
	      //use buffering, reading one line at a time
	      //FileReader always assumes default encoding is OK!
	      BufferedReader input =  new BufferedReader(new FileReader(transcriptFile));
	      try {
	        String line = null; //not declared within while loop
	        adults = new ArrayList<String>();
	        
	        
	        String workingLine = input.readLine();
	        workingLine = workingLine.substring(1);
	        while (( line = input.readLine()) != null) {
	        	int equalIdx = line.indexOf('=');
		        	if ((line.charAt(1) == '@' || line.charAt(1) == '\t')) {
		        		line = line.substring(equalIdx+1);
			        	if (line.charAt(0) != '\t') {
				        	if (workingLine.length() > 3 && workingLine.substring(0,3).equals("@ID")) 
				        		parseIDLine(workingLine); 
			        		int colon = workingLine.indexOf(':');
			        		if (colon != -1) { 
			        			fileProperties.put(workingLine.substring(0,colon), workingLine);
			        			filePropertiesArr.add(workingLine);
			        		}
				        	
				        	workingLine = line;
			        		// assume only one child
			        	//} else if (line.length() > 3 && line.substring(0,3).equals("@situation")) {
			        	} else {
			        		workingLine += " " + line.substring(1);
			        	}
		        	} else { 
		        		// add the last working line
		        		int colon = workingLine.indexOf(':');
		        		if (colon != -1) {
		        			fileProperties.put(workingLine.substring(0,colon), workingLine);
		        			filePropertiesArr.add(workingLine);
		        		}
			        	break;
		        	}
	        }
	        ArrayList<String> lines = new ArrayList<String>();
	        workingLine = line;
	        int equalIdx = workingLine.indexOf('=');
	        int spokenLineIdxNew = 0;
        	if (equalIdx != 0) {
        		spokenLineIdx = Integer.parseInt(workingLine.substring(0,equalIdx));
        		spokenLineIdxNew = spokenLineIdx;
        	}
        	workingLine = line.substring(equalIdx+1);
        	
	        line = input.readLine();
	        if (line.charAt(0)== '\t') {	// if the current line extends two lines, add the next line to workingLine 
	        	workingLine += line.substring(2);
	        	lines.add(workingLine);
	        	workingLine = input.readLine();
	        } else {
	        	lines.add(workingLine);
	        	workingLine = line.substring(1);
	        }
	        
	        // we have workingLines, because one line in the transcript can take up two lines
	        // if it takes up two lines, the second line will continue after a tab
	        
	        // at each iteration, I have a line that I haven't added yet: workingLine, because lines in a transcript can take up two lines
	        while (( line = input.readLine()) != null){
	        	equalIdx = line.indexOf('=');
	        	int startIdx = equalIdx + 1;
	        	if (equalIdx != 0) {	// if there is a line number, then it is a spoken line
	        		spokenLineIdx = spokenLineIdxNew;
	        		spokenLineIdxNew = Integer.parseInt(line.substring(0,equalIdx));
	        	}
	        	line = line.substring(startIdx);
	        	
	        	if (workingLine.equals("@End")) break;
	        	
	        	if (line.charAt(0) != '\t') { // if this line is not extending the previous line, we need to add the previous working line
	        		if ( workingLine.charAt(0) == '*') {	// if the workingLine is a spoken line
		        		allLines.add(new Line(lines, transcriptId, spokenLineIdx));
		        		lines = new ArrayList<String>();
		        		lines.add(workingLine);
		        	} else {
		        		lines.add(workingLine);
		        	}
	        		workingLine = line;
	        	} else {
	        		workingLine += " " + line.substring(1); // remove the tab and add a space
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
	
	public Transcript(String path, int transcriptID, String age, String situation) {
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
