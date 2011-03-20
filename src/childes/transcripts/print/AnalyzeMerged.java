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

public class AnalyzeMerged {

	
	public static void main(String[] args) {
	
		
		// Set up default parameters and settings
		String dirRoot = "/home/frosting/windows/childes/";
		String outputDir = "./";
		String dirStr = "Brown/Adam Brown/Sarah Kuczaj MacWhinney Sachs Suppes Warren";//"Brown/Sarah";//"Kuczaj";//"Brown/Sarah";// Brown/Sarah Kuczaj MacWhinney Sachs Suppes Warren";
		
		// 
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
			Lines tmpLines = new Lines();
			tmpLines.addLines(d);
			System.out.println("Total Lines: " + tmpLines.totalLines());
			tmpLines.keepOnlyPerson("CHI");
			System.out.println("Total Lines by Child: " + tmpLines.totalFilteredLines());
			tmpLines.keepOnlyLinesWithKeywords("?");
			System.out.println("Total Questions by Child: " + tmpLines.totalFilteredLines());
			lines.addLines(d);			// we pruneByAge using the Directory rather than the Lines class to speed up processing
			transcriptIDBegin += d.getTranscriptPaths().size();
			System.out.println(d.getTotalLines() + " processed");
		}
		lines.synchronizeTranscriptIDs();	// hackish. necessary to make things work for some reason that
											// i don't care to take the time to figure out
		System.out.println("Total Lines: " + lines.totalLines());
		
		
		/* APPLY FILTERS */
		
		// filters used in cs281a project
		 lines.keepOnlyPerson("CHI");
		 System.out.println("Total Lines by Child: " + lines.totalFilteredLines());
		 //printAllLines(outputDir + "all_chi4-5.txt", lines);
		 lines.keepOnlyLinesWithKeywords("?");
		// lines.pruneByLength(3);
		// lines.pruneByLengthOfResponse(3);
		// lines.keepOnlyLinesThatAlternate();
		
		/* PRINT TO TEXT FILES */
		//printLines(outputDir + "all_chi3-4_ques.txt", lines);
		 
		// print statement used in cs281a project
		//printLines(outputDir + "all_chi4-5_ques_KWAL10-2.txt", lines.getKWAL(10,2), lines);
		
		// example print statements
		//printAllLines("all_alllines_4-5.txt", lines);
		 //if (lines.filteredLinesIdx.size() >= 50) {
		//	 printLines(outputDir + "brownsarah" + "_chi_3-4_KWAL4-4.txt", lines.getKWAL(4, 4), lines, 50);
		 //} else {
		//	 System.out.println("Not enough lines");
		 //}
		System.out.println("Total Questions by Child: " + lines.totalFilteredLines());
		
		
		
		
		/*System.out.println(tmp.pruneByLength(3) + " pruned by size < 3");
		int totalLines = tmp.totalFilteredLines();
		System.out.println(totalLines - tmp.keepOnlyLinesWithPattern("* n| *") + " pruned if a noun doesn't exist");  // keep only lines that contain a noun
		System.out.println(tmp.pruneByWords("you%we%I%your%my%me")+ " pruned by you/we/I/your/my/me");
		System.out.println(tmp.pruneByWords("he%she%they%it%his%her")+ " pruned by he/she/his/herthey/it");
		System.out.println(tmp.pruneByWords("det|that%det|this%det|these%det|those%here%there")+ " pruned by this/that/these/those");
		//System.out.println(tmp.pruneByPattern("v| *")+ " pruned by verb ___");
		totalLines = tmp.totalFilteredLines();
		System.out.println(totalLines - tmp.keepOnlyLinesWithPattern("* v:| *") + " pruned by no verb");
		printLines("all_quesKWAL4-4-n-nomeyou-no3rd-nodet-nobegv-hasv.txt", tmp.getKWAL(4, 4), lines);
		//tmp.keeyOnlyLinesWithPattern("what be&3S *");
		//printLines("all_KWAL2-2-whatis.txt", tmp.getKWAL(2,2), tmp);*/
		
		//tmp = lines.copy();
		//tmp.keeyOnlyLinesWithPattern("do you *");
		//printLines("all_KWAL2-2-doyou.txt", tmp.getKWAL(2,2), tmp);
		
		//tmp = lines.copy();
		//tmp.keeyOnlyLinesWithPatterns("be&3S this *%be&3S that *");
		//printLines("all_KWAL2-2-isthat-isthis.txt", tmp.getKWAL(2,2), tmp);
		
		//Lines.printLines("blah.txt", lines);
		//lines = new Lines("blah.txt");
		
		//System.out.println("Total Questions by Child: " + lines.totalFilteredLines());
		
		/*
		// ** Examples where only lines that have a certain pattern are kept **
		
		// == keeyOnlyLinesWithPattern ==
		//   keep only lines with a certain pattern of keywords
		//   - keywords are delimited by spaces 
		//   - keywords in the pattern can take the forms: *, POS|, POS|word, word
		//     where * stands for anything and POS stands for part of speech
		
		lines.keeyOnlyLinesWithPattern("* n| *");  // keep only lines that contain a noun
		System.out.println("Total Questions by Child with Nouns: " + lines.totalFilteredLines());
		//printLines("all_quesKWAL0-0-n.txt", lines.getKWAL(0, 0), lines);
		
		Lines linesWithPatternExp = lines.copy();
		linesWithPatternExp.keeyOnlyLinesWithPattern("v| *"); // keep only lines that begin with a verb 
		
		
		// == keeyOnlyLinesWithPatterns ==
		//   keep only lines that contain at least one of the patterns in the argument 
		//   - patterns are delimited by % 
		
		Lines linesWithPatternExp3 = lines.copy();
		linesWithPatternExp3.keeyOnlyLinesWithPattern("what *%why *"); // keep only lines that start with what OR lines that start with why  
		
		// == keepOnlyLinesWithKeywords ==
		//   keep only lines that contain at least one of the keywords in the argument 
		//   - keywords are delimited by spaces
		//   - keywords can take the forms: POS|, POS|word, word
		 
		Lines linesWithPatternExp4 = lines.copy();
		linesWithPatternExp4.keepOnlyLinesWithKeywords("det|that det|this det|these det|those"); //(keep only lines that have a determinant)

		
		// ** Examples where lines are pruned by various attributes **
		
		// == pruneByLength ==
		//   remove lines that are < argument in length
		//   - note: punctuation counts as part of the length (i.e. "huh ." is length 2)
		
		Lines linesPrunedExp = lines.copy();
		linesPrunedExp.pruneByLength(3); // prune lines that are of length < 3 
		
		// == pruneByWord ==
		//   keep only lines that have the argument as a word
		lines.pruneByWord("you"); // prune lines that have the word "you" in them
		lines.pruneByWord("we"); 
		//printLines("all_quesKWAL0-0-n-noyouwe.txt", lines.getKWAL(0, 0), lines);
		lines.pruneByWord("he");
		lines.pruneByWord("she");
		lines.pruneByWord("it");
		lines.pruneByWord("they");
		//printLines("all_quesKWAL0-0-n-noyouwe-no3rd.txt", lines.getKWAL(0, 0), lines);
		lines.pruneByWord("this");
		lines.pruneByWord("that");
		lines.pruneByWord("these");
		lines.pruneByWord("those");
		lines.pruneByWord("here");
		lines.pruneByWord("there");
		//printLines("all_quesKWAL0-0-n-noyou-no3rd-nodet.txt", lines.getKWAL(0, 0), lines);
		System.out.println("Total Questions by Child with Nouns without you, we, 3rd person, determiners: " + lines.totalFilteredLines());
		
		
		/* FREQUENCY CALCULATIONS */
		
		Counter<String> freqAllLines = lines.frequencies();
		Counter<String> freqFilteredLines = lines.filteredTranscriptFrequencies();
		Counter<String> freqNounFilteredLines = lines.filteredTranscriptFrequenciesPOS("n");
		Counter<String> freqVerbFilteredLines = lines.filteredTranscriptFrequenciesPOS("v");
		//Counter<String> biFreqFilteredLines = lines.filteredBigramFrequency();
		int total = (int)freqFilteredLines.totalCount();
		PriorityQueue<String> wordOrder = new FastPriorityQueue<String>(freqFilteredLines);
		PriorityQueue<String> wordVOrder = new FastPriorityQueue<String>(freqVerbFilteredLines);
		PriorityQueue<String> wordNOrder = new FastPriorityQueue<String>(freqNounFilteredLines);
		
		//printFrequencies("all4-5_quesTotalCount.txt", wordOrder, total);
		//printFrequencies("all4-5_quesTotalNounCount.txt", wordNOrder, total);
		//printFrequencies("all4-5_quesTotalVerbCount.txt", wordVOrder, total);
		
		//wordOrder = new FastPriorityQueue<String>(biFreqFilteredLines);
		//printFrequencies("all_quesTotalBigramCount.txt", wordOrder, total);
		
		// choose the top 5% frequently occurring as the common words
		//int minCount = (int)(wordOrder.size()*.05);
		//Counter<String> commonWords = new Counter<String>();
		//for (int i = 0; i < minCount; ++i) {
		//	commonWords.incrementCount(wordOrder.removeFirst(), 1.0);
		//}
		
		
	
	
		/* K MEANS CLUSTERING ALGORITHM */
		
		/*for (Line l : lines.allLines) {
			l.setFeatures(commonWords);
		}
		int nClusters = 40, nIterations = 50;
		Lines centroids = new Lines();
		ArrayList<Lines> lineGroups = new ArrayList<Lines>();
		lines.kMeansClustering(nClusters, nIterations, centroids, lineGroups);
		printSentenceClusters(lineGroups, centroids, names[dirI] + "_" + filter + "_clusters.txt");
		*/

				
	}
	
	

	/**
	 * prints all lines given to a text file
	 * @param outputName
	 * @param lines
	 */
	public static void printLines(String outputName, Lines lines) {
		
		FileOutputStream out; // declare a file output object
		PrintStream p; // declare a print stream object 
		int totalLines = lines.totalLines();//lines.allLines.size();
		
		// AGE LINE
		try
		{
			// Create a new file output stream
			out = new FileOutputStream(outputName);
			

			// Connect print stream to the output stream	
			p = new PrintStream( out );
			
			p.println("Total Lines: " + totalLines);
			p.println("Total Filtered Lines: " + lines.filteredLinesIdx.size());
			
			for (Integer i : lines.filteredLinesIdx) {
				p.println(lines.allLines.get(i).spokenLineStr);
				
			}
			//for (Line line : lines.allLines) 
			//	p.println(line.spokenLineStr);
			p.close();
		}
		catch (Exception e)
		{
			System.err.println ("Error writing to file");
		}
	}
	
	/**
	 * prints a text file of clusters of lines. 
	 * each cluster includes a line of interest and a certain number of lines before and after the line of interest.
	 * the clusters are separated by the text file from which they come from
	 *   
	 * @param outputName : output file name
	 * @param filteredLines : an ArrayList of Lines. Each Lines object contains a cluster of lines (lines before and after a line of interest)
	 * @param allLines : all lines
	 */
	public static void printLines(String outputName, ArrayList<Lines> filteredLines, Lines allLines) {
		
		FileOutputStream out; // declare a file output object
		PrintStream p; // declare a print stream object
		//ArrayList<String> keywordLines = lines.allLines; 
		int totalLines = allLines.totalLines();//allLines.allLines.size();
		
		int totalFilteredLines = filteredLines.size();
		
		try
		{
			// Create a new file output stream
			out = new FileOutputStream(outputName);

			// Connect print stream to the output stream	
			p = new PrintStream( out );
			
			p.println("Total Lines: " + totalLines);
			p.println("Total Filtered Lines: " + totalFilteredLines);
			
			int transcriptID = -1;
			for (Lines lines : filteredLines) {
				Transcript t = lines.transcripts.get(0); 
				if (t.transcriptID != transcriptID) {
					p.println("============");
					p.println(t.path);
					if (t.fileProperties.containsKey("situation"))
						p.println(t.fileProperties.get("situation"));
					p.println("============");
					transcriptID = t.transcriptID;
				}
				for (Line line : lines.allLines) 
					p.println(line.spokenLineStr);
				p.println();
			}
			p.close();
		}
		catch (Exception e)
		{
			System.err.println ("Error writing to file");
		}
	}
	
	
	/**
	 * prints a text file of k randomly selected clusters of lines. 
	 * each cluster includes a line of interest and a certain number of lines before and after the line of interest.
	 * the clusters are separated by the text file from which they come from
	 *   
	 * @param outputName : output file name
	 * @param filteredLines : an ArrayList of Lines. Each Lines object contains a cluster of lines (lines before and after a line of interest)
	 * @param allLines : all lines
	 * @param k : number of random clusters to print
	 */
	public static void printLines(String outputName, ArrayList<Lines> filteredLines, Lines allLines, int k) {
		
		FileOutputStream out; // declare a file output object
		PrintStream p; // declare a print stream object
		//ArrayList<String> keywordLines = lines.allLines; 
		int totalLines = allLines.totalLines();//allLines.allLines.size();
		
		int totalFilteredLines = filteredLines.size();
		Random r = new Random();
		
		ArrayList<Integer> idx = new ArrayList<Integer>(totalFilteredLines);
		Counter<Integer> idxChosen = new Counter<Integer>(k);
		for (int i = 0; i < totalFilteredLines; ++i) idx.add(i);
		
		for (int i = 0; i < k; ++i) {
			int tmp = r.nextInt(totalFilteredLines - i);
			tmp = idx.remove(tmp);
			idxChosen.setCount(tmp, -tmp);
		}
		PriorityQueue<Integer> idxChosenQueue = new FastPriorityQueue<Integer>(idxChosen);
		
		try
		{
			// Create a new file output stream
			out = new FileOutputStream(outputName);

			// Connect print stream to the output stream	
			p = new PrintStream( out );
			
			p.println("Total Lines: " + totalLines);
			p.println("Total Filtered Lines: " + totalFilteredLines);
			//p.println("Total Random Lines Printed: " + k);
			
			int transcriptID = -1;
			//for (Lines lines : filteredLines) {
			//for (Integer i : idxChosenQueue)
			while (idxChosenQueue.hasNext()) {
				int idxTmp = idxChosenQueue.next();//.remove();//.removeFirst();
				//System.out.println(idxTmp);
				Lines lines = filteredLines.get(idxTmp);
				Transcript t = lines.transcripts.get(0);//allLines.transcripts.get(allLines.getLine(idxTmp).transcriptId); 
				if (t.transcriptID != transcriptID) {
					p.println("============");
					p.println(t.path);
					if (t.fileProperties.containsKey("situation"))
						p.println(t.fileProperties.get("situation"));
					p.println("============");
					transcriptID = t.transcriptID;
				}
				p.println("Line ID: " + lines.allLines.get(0).ID);
				for (Line line : lines.allLines) { 
					p.println(line.spokenLineStr);
					if (line.otherLines.containsKey("act"))
						p.println("%act:\t" + line.otherLines.get("act"));
				}
				//p.println(idxTmp));
				p.println();
			}
			p.close();
		}
		catch (Exception e)
		{
			System.err.println ("Error writing to file\n" + e);
		}
	}
	
	/**
	 * print all lines to a text file
	 * 
	 * @param outputName : output file name
	 * @param allLines : all lines
	 */
	public static void printAllLines(String outputName, Lines allLines) {
		
		FileOutputStream out; // declare a file output object
		PrintStream p; // declare a print stream object
		//ArrayList<String> keywordLines = lines.allLines; 
		int totalLines = allLines.totalLines();//allLines.allLines.size();
	
		
		try
		{
			// Create a new file output stream
			out = new FileOutputStream(outputName);

			// Connect print stream to the output stream	
			p = new PrintStream( out );
			
			p.println("Total Lines: " + totalLines);
			
			int transcriptID = -1;
			
			for (Line line : allLines.allLines) {
				Transcript t = allLines.transcripts.get(line.transcriptId); 
				if (t.transcriptID != transcriptID) {
					p.println("============");
					p.println(t.path);
					if (t.fileProperties.containsKey("situation"))
						p.println(t.fileProperties.get("situation"));
					p.println("============");
					transcriptID = t.transcriptID;
				}
				 
				p.println(line.spokenLineStr);
				for (String keyword : line.otherLines.keySet()) {
					if (!keyword.equals("mor") && !keyword.equals("gra") && !keyword.equals("spa")) {
						p.println("%" + keyword +":\t" + line.otherLines.get(keyword));
					}
				}
				//p.println();
			}
			p.close();
		}
		catch (Exception e)
		{
			System.err.println ("Error writing to file");
		}
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
				p.println(freq.removeFirst() + '\t' + (int)count);
			}
			p.close();
		}
		catch (Exception e)
		{
			System.err.println ("Error writing to file");
		}
	}
	
	/**
	 * print the sentence clusters found from k means clustering to a text file
	 * @param allLines : an ArrayList of Lines. Each Lines object contains the lines closest to a centroid
	 * @param centroids : Lines object of centroids
	 * @param outputName : output file name
	 */
	public static void printSentenceClusters(ArrayList<Lines> allLines, Lines centroids, String outputName) {
		FileOutputStream out; // declare a file output object
		PrintStream p; // declare a print stream object
		try {
			out = new FileOutputStream(outputName);
			// Connect print stream to the output stream	
			p = new PrintStream( out );

			for (int i = 0; i < allLines.size(); ++i) {
				p.println("===Centroid " + i + "===");
				p.println(centroids.allLines.get(i).spokenLineStr);
				p.println("===============");
				for (int j = 0; j < allLines.get(i).allLines.size(); ++j)
					p.println(allLines.get(i).allLines.get(j));
				p.println();
				
			}
			p.close();
		} catch (Exception e)
		{
			System.err.println ("Error writing to file");
		}
	}
	
	public static void printLinesBlah(String outputName, Lines filteredLines) {
		
		FileOutputStream out; // declare a file output object
		PrintStream p; // declare a print stream object
		//ArrayList<String> keywordLines = lines.allLines; 
		int totalLines = filteredLines.totalLines();//allLines.allLines.size();
	
		
		try
		{
			// Create a new file output stream
			out = new FileOutputStream(outputName);

			// Connect print stream to the output stream	
			p = new PrintStream( out );
			
			p.println("Total Lines: " + totalLines);
			
			int transcriptID = -1;
			for (int fLineIdx : filteredLines.filteredLinesIdx) {
				Line line = filteredLines.getLineFromAll(fLineIdx);
				Line nextLine = filteredLines.getLineFromAll(fLineIdx+1);
				if (! nextLine.participant.equals("CHI")) {
					Transcript t = filteredLines.transcripts.get(line.transcriptId); 
					if (t.transcriptID != transcriptID) {
						p.println("============");
						p.println(t.path);
						if (t.fileProperties.containsKey("situation"))
							p.println(t.fileProperties.get("situation"));
						p.println("============");
						transcriptID = t.transcriptID;
					}
					p.println(line.spokenLineStr);
					p.println(nextLine.spokenLineStr);
				}
			}
			p.close();
		}
		catch (Exception e)
		{
			System.err.println ("Error writing to file");
		}
	}
}
