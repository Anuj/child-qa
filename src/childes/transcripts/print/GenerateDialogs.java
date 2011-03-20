package qa.childes.transcripts.print;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;

import qa.childes.transcripts.parser.*;
import qa.util.*;

public class GenerateDialogs {

	public static void main(String[] args) {
		// Set up default parameters and settings
		String dirRoot = "C:/Users/frosting/QA/childes";
		String outputDir = "./";
		//String dirStr = "Brown/Adam Brown/Sarah Kuczaj MacWhinney Sachs Suppes Warren";//"Brown/Sarah";//"Kuczaj";//"Brown/Sarah";// Brown/Sarah Kuczaj MacWhinney Sachs Suppes Warren";
		String dirStr = "Brown/Adam";
		String outputNameSuffix = "_dialog.txt";
		String lineListFileName = "lines_4-5.csv";
		String[] directories; 
		
		
	    Map<String, String> argMap = CommandLineUtils.simpleCommandLineParser(args);

	    // Update defaults using command line specifications
	    if (argMap.containsKey("-dirRoot")) {
	      dirRoot = argMap.get("-dirRoot");
	    }
	    if (argMap.containsKey("-directories")) {
	    	dirStr = argMap.get("-directories");
	    }
	    if (argMap.containsKey("-outputDir")) {
	    	outputDir = argMap.get("-outputDir");
	    }
	    directories = dirStr.split(" ");
	    
	    System.out.println("Using base path: " + dirRoot);
	    System.out.println("Using directories: " + dirStr);
		 
		/* PROCESS CHAT TRANSCRIBED FILES */
		Lines lines = new Lines();
		int transcriptIDBegin = 0;
		for (String dStr : directories) {
			String outputPath = dirRoot + "/" + dStr;
			System.out.println("Processing directory: " + outputPath);
			Directory d = new Directory(dirRoot + "/" + dStr, transcriptIDBegin);	
			d.pruneByAge(4, 0, 5, 0);	// note: d.pruneByAge removes all transcripts that do not fit the age constraint
			
			String outputLinesFileName = dirRoot + "/" + dStr + "/" + lineListFileName;
			FileOutputStream out2 = null; // declare a file output object
			PrintStream p2 = null; // declare a print stream object
			
			try {
				out2 = new FileOutputStream(outputLinesFileName);
				p2 = new PrintStream( out2 );
			
				for (Transcript t : d.getWorkingTranscripts()) {
					FileOutputStream out = null; // declare a file output object
					PrintStream p = null; // declare a print stream object
					// Create a new file output stream
					String tmpPath = t.path;
					String outputName = tmpPath.substring(0,tmpPath.indexOf('.')) + outputNameSuffix;
					
					try {
						out = new FileOutputStream(outputName);
					
						// Connect print stream to the output stream	
						p = new PrintStream( out );
					
					
					//for (String prop : t.fileProperties.keySet()) {
					//	p.println(t.fileProperties.get(prop));
					//}
					for (String prop : t.filePropertiesArr) {
						p.println(prop);
					}
					
					for (Line l : t.allLines) {
						if (l.otherLines.containsKey("sit"))
							p.println("%sit:\t" + l.otherLines.get("sit"));
						
						if (l.containsKeyword("?") && l.participant.equals("CHI")) {
							p.println("***\t" + l.ID + "\t" + l.spokenLineStr);
							p2.println(l.ID + "\t" + l.spokenLineStr + "\t");
						} else {
							p.println("\t" + l.ID + "\t" + l.spokenLineStr);
						}
					}
					
					
					System.out.println("Printed " +  outputName);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} finally {
						if ( out != null) { try { out.close();	} catch (IOException e) { e.printStackTrace(); } }
						if (p != null) p.close();
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				if ( out2 != null) { try { out2.close(); } catch (IOException e) { e.printStackTrace();	} }
				if (p2 != null) { p2.close(); }
			}
			
			//Lines tmpLines = new Lines();
			//tmpLines.addLines(d);
			//System.out.println("Total Lines: " + tmpLines.totalLines());
			//tmpLines.keepOnlyPerson("CHI");
			//System.out.println("Total Lines by Child: " + tmpLines.totalFilteredLines());
			//tmpLines.keepOnlyLinesWithKeywords("?");
			//System.out.println("Total Questions by Child: " + tmpLines.totalFilteredLines());
			//lines.addLines(d);			// we pruneByAge using the Directory rather than the Lines class to speed up processing
			//transcriptIDBegin += d.getTranscriptPaths().size();
			//System.out.println(d.getTotalLines() + " processed");
		}
		//lines.synchronizeTranscriptIDs();	// hackish. necessary to make things work for some reason that
											// i don't care to take the time to figure out
		//System.out.println("Total Lines: " + lines.totalLines());
		
		
	}
}
