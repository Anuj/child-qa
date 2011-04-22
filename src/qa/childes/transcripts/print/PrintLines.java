package qa.childes.transcripts.print;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import qa.util.BasicDataTransformations;
import qa.util.Counter;
import qa.util.CounterMap;
import qa.util.FastPriorityQueue;
import qa.util.PriorityQueue;
import edu.stanford.nlp.trees.Tree;

import qa.childes.transcripts.parser.Directory;
import qa.childes.transcripts.parser.Line;
import qa.childes.transcripts.parser.Lines;
import qa.util.CommandLineUtils;

public class PrintLines {

	public static void main(String[] args) {
		String dirRoot = "/home/pomegranate/frosting/QA/childes/";
		String outputDir = "./";
		String dirStr = "Brown/Adam Brown/Sarah Kuczaj MacWhinney Sachs Suppes Warren";//"Brown/Sarah";//"Kuczaj";//"Brown/Sarah";// Brown/Sarah Kuczaj MacWhinney Sachs Suppes Warren";
		
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
			System.out.println("Processing directory: " + dStr);
			Directory d = new Directory(dirRoot + dStr, transcriptIDBegin);	
			d.pruneByAge(3, 0, 4, 0);	// note: d.pruneByAge removes all transcripts that do not fit the age constraint
			lines.addLines(d);			// we pruneByAge using the Directory rather than the Lines class to speed up processing
			transcriptIDBegin += d.getTranscriptPaths().size();
			System.out.println(d.getTotalLines() + " processed");
		}
		lines.synchronizeTranscriptIDs();	// hackish. necessary to make things work for some reason that
											// i don't care to take the time to figure out
		System.out.println("Total Lines: " + lines.totalLines());
		
		
		/* APPLY FILTERS */
		 lines.keepOnlyPerson("CHI");
		 System.out.println("Total Lines by Child: " + lines.totalFilteredLines());
		 System.out.println("Total Questions by Child: " + lines.keepOnlyLinesWithKeywords("?"));
		 System.out.println("Total Questions by Child of length > 3: " + lines.pruneByLength(3));
		 
		 String outputName = "ques3-4response.txt";
		 String notes = "Brown/Adam Brown/Sarah Kuczaj MacWhinney Sachs Suppes Warren\n";
		 notes += "3-4\n";
		 notes += "length > 3";
		 notes += "with response";
		 AnalyzeMerged.printFilteredLines(outputName, lines, notes, true);		 
	}	 
	
	
}
