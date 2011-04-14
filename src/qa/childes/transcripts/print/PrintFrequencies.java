package qa.childes.transcripts.print;
/** 
 * Main method for 
 * 	reading in a list of CHAT transcribed text files
 * 	outputting text files
 */
import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import qa.util.*;
import qa.childes.transcripts.parser.*;

public class PrintFrequencies {

	
	public static void main(String[] args) {
	
		
		String dirRoot = "/home/pomegranate/frosting/QA/childes/";
		String outputDir = "./";
		String dirStr = "Brown/Adam"; //Brown/Sarah Kuczaj MacWhinney Sachs Suppes Warren";//"Brown/Sarah";//"Kuczaj";//"Brown/Sarah";// Brown/Sarah Kuczaj MacWhinney Sachs Suppes Warren";
		
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
		 
		Counter<String> tfidfUnigrams = BasicDataTransformations.counterStrArrToCounterStr(lines.tfidfNGramWeights(1));
		
		int total = (int)lines.filteredLinesIdx.size();
		PriorityQueue<String> wordOrder = new FastPriorityQueue<String>(tfidfUnigrams);
		
		printFrequencies("tfidfunigrams.txt", wordOrder, total);
		
				
	}
	
	

	
	/** 
	 * print frequencies of words to a text file
	 * Note: we use the frequencies of the expanded conjunctions 
	 * (i.e. 'do not' instead of 'don't') and of 
	 * the actual words (i.e. 'them' instead of 'dem')
	 * 
	 * @param outputName : output file name
	 * @param freq : the priority queue of word frequencies
	 * @param total : total number of unique words
	 */
	// 
	public static void printFrequencies(String outputName, PriorityQueue<String> freq, int total) {
		
		FileOutputStream out; // declare a file output object
		PrintStream p; // declare a print stream object
		
		try
		{
			// Create a new file output stream
			// connected to "myfile.txt"
			out = new FileOutputStream(outputName);

			// Connect print stream to the output stream	
			p = new PrintStream( out );
			
			p.println("Total Count: " + total);
			p.println("Total Distinct Words: " + freq.size());
			while (! freq.isEmpty()) {
				double count = freq.getPriority();
				p.println(freq.removeFirst() + '\t' + count);
			}
			p.close();
		}
		catch (Exception e)
		{
			System.err.println ("Error writing to file");
		}
	}
	

}
