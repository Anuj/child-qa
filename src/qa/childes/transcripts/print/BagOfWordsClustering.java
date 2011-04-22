package qa.childes.transcripts.print;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import danbikel.wordnet.Morphy;
import danbikel.wordnet.WordNet;

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

import java.io.*;
import java.util.Scanner;

public class BagOfWordsClustering {

	public static void main(String[] args) {
		String dirRoot = "/home/pomegranate/frosting/QA/childes/";
		String outputDir = "./";
		String dirStr = "Brown/Adam Brown/Sarah Kuczaj MacWhinney Sachs Suppes Warren";////"Kuczaj";//"Brown/Sarah";// Brown/Sarah Kuczaj MacWhinney Sachs Suppes Warren";
		String wnhome = "/home/pomegranate/workspace/WordNet-3.0";
		
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

	    WordNet wn = new WordNet(wnhome);
		Morphy m = new Morphy(wn);
		
		/* PROCESS CHAT TRANSCRIBED FILES */
		Lines lines = new Lines();
		System.out.println("Using base path: " + dirRoot);
	    System.out.println("Using directories: " + dirStr);
	    processChatFiles(3, 4, directories, dirRoot, lines);
		
	    // set memoizedlines
		long startTime = System.currentTimeMillis();
		HashMap<String, ArrayList<String>> memoizedExpansions = new HashMap<String, ArrayList<String>>();
		lines.setStemmedLines(m, wn, memoizedExpansions);
		lines.setMaps();
		long endTime = System.currentTimeMillis();
		System.out.println("Total execution time: " + (endTime-startTime));

		
		System.out.println("Total Lines: " + lines.totalLines());
		
		Lines orig = lines.copy();
		
		/* APPLY FILTERS */
		 lines.keepOnlyPerson("CHI");
		 System.out.println("Total Lines by Child: " + lines.totalFilteredLines());
		 System.out.println("Total Questions by Child: " + lines.keepOnlyLinesWithKeywords("?"));
		 System.out.println("Total Questions by Child of length > 3: " + lines.pruneByLength(3));
		 
		 
		 /* GET STOPWORDS FOR RESPONSES */
		 //Lines notChild = orig.copy();
		 //notChild.keepOnlyNotPerson("CHI");
		 
		 ArrayList<Integer> idxFromAllLinesUsed = new ArrayList<Integer>();
		 Lines responses = orig.copy();
		 responses.filteredLinesIdx = getResponseIdx(lines,idxFromAllLinesUsed);
		 lines.filteredLinesIdx = idxFromAllLinesUsed;
		 
		 
		// these should be command line arguments
		 String stopResponseWordsFilename = "/home/pomegranate/workspace/child-qa/stopWordsResponses3-4.txt";
		 String stopQuesWordsFilename = "/home/pomegranate/workspace/child-qa/stopWordsQues3-withResponse.txt";
		 String tfidfQuestUnigramFilename = "/home/pomegranate/workspace/child-qa/tfidfQuestUnigrams3-4-withResponse.txt";
		 String tfidfResponseUnigramFilename = "/home/pomegranate/workspace/child-qa/tfidfResponseUnigrams3-4.txt";
		 boolean getStopWordsFromFile = true;
		 boolean getTFIDFQuestUnigramFromFile= true;
		 boolean getTFIDFResponseUnigramFromFile= true;
		  
		 
		 int nStopWords = 25;
		 
		 
		 /* print tfidf and stopwords to file
		  */
		 Counter<String> stopQuesWords = getStopWords(lines, nStopWords, true);
		 Counter<String> stopResponseWords = getStopWords(responses, nStopWords, true);
		 PriorityQueue stopWordQuesOrder = new FastPriorityQueue<String>(stopQuesWords);
		 PriorityQueue stopWordResponseOrder = new FastPriorityQueue<String>(stopResponseWords);
		 Counter<String> tfidfResponseUnigrams = BasicDataTransformations.counterStrArrToCounterStr(responses.tfidfNGramWeights(1));
		 Counter<String> tfidfQuestUnigrams = BasicDataTransformations.counterStrArrToCounterStr(lines.tfidfNGramWeights(1));
	     PriorityQueue tfidfResponseOrder = new FastPriorityQueue<String>(tfidfResponseUnigrams);
		 PriorityQueue tfidfQuestOrder = new FastPriorityQueue<String>(tfidfQuestUnigrams);
	     AnalyzeMerged.printFrequencies("tfidfQuestUnigrams3-4-withResponse.txt", tfidfQuestOrder, (int)tfidfQuestUnigrams.totalCount());
		 AnalyzeMerged.printFrequencies("tfidfResponseUnigrams3-4.txt", tfidfResponseOrder, (int)tfidfResponseUnigrams.totalCount());
		 AnalyzeMerged.printFrequencies("stopWordsQues3-withResponse.txt", stopWordQuesOrder, stopQuesWords.totalCount());
		 AnalyzeMerged.printFrequencies("stopWordsResponses3-4.txt", stopWordResponseOrder, stopResponseWords.totalCount());
		 /*
		 Counter<String> stopQuesWords = null; // = getStopWords(lines, nStopWords, true);
		 Counter<String> stopResponseWords = null; // = getStopWords(responses, nStopWords, true);
		 Counter<String> tfidfResponseUnigrams = null;
		 Counter<String> tfidfQuestUnigrams = null;
		 stopQuesWords = readFrequencyFile(stopQuesWordsFilename);
		 stopResponseWords = readFrequencyFile(stopResponseWordsFilename);
		 tfidfResponseUnigrams = readFrequencyFile(tfidfResponseUnigramFilename);
	     tfidfQuestUnigrams =  readFrequencyFile(tfidfQuestUnigramFilename);
		 */
		 
		 
		 
	     
		 
		 /* COLLECT LINE FEATURES HERE */
	     
	     
	     /*
		 for (int i = 0; i < lines.filteredLinesIdx.size(); ++i) {
			 Line l = lines.getFilteredLine(i);
			 Line response = responses.getFilteredLine(i);
			 l.setTFIDFFeatures(tfidfQuestUnigrams, stopQuesWords);
			 l.setTFIDFResponseFeatures(tfidfResponseUnigrams, stopResponseWords, response, 1.0);
		 }		 
		 
		 String[] whs = {"what", "why", "how", "where"};
		 for (int i = 0; i < whs.length; ++i) {
			 //String word = wordOrder.removeFirst();
			String word = whs[i];	
			
			Lines linesSubset = lines.copy();
			linesSubset.keepOnlyLinesWithKeywords(word);
			
			int nClusters = 40, nIterations = 100;
			Lines centroids = new Lines();
			ArrayList<Lines> lineGroups = new ArrayList<Lines>();
			linesSubset.kMeansClustering(nClusters, nIterations, centroids, lineGroups);
			//for (Line l : centroids.allLines) {
			//	System.out.println(l.spokenLineStr);
			//}
			AnalyzeMerged.printSentenceClusters(lineGroups, centroids, whs[i] + "_clusters.txt", centroids, true);
		 }*/
		 
	}	 
	
	public static void processChatFiles(int minAge, int maxAge, String[] directories, String dirRoot, Lines lines) {
		
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
	}
	public static ArrayList<Integer> getResponseIdx(Lines questionLines, ArrayList<Integer> idxUsed) {
		ArrayList<Integer> responseIndices = new ArrayList<Integer>();
		HashMap<Integer, Integer> lineIDtoAllLinesMap = questionLines.lineIDtoAllLinesIDMap;
		HashMap<Integer, Integer> allLinestoLineIDMap = questionLines.allLinesIDtoLineIDMap;
		boolean proceed = true;
		for (int i = 0; i < questionLines.filteredLinesIdx.size(); ++i) { 
			proceed = true;
			Line filteredLine = questionLines.getFilteredLine(i);
			int lineID = filteredLine.ID;
			// heuristic 1: we say the parent's response is the first line the parent speaks after a child asks a question
			int nResponseLines = questionLines.findNumLinesUntilOtherSpeaker(lineIDtoAllLinesMap.get(lineID), filteredLine.participant);
			int allLinesIdx = lineIDtoAllLinesMap.get(lineID);
			int responseID = questionLines.getLineFromAll(allLinesIdx + nResponseLines).ID;
			//int responseID = lineID + nResponseLines;
			// heuristic 2: if the child asks another question before the parent answers, then the parent did not response to the first question
			if (i < questionLines.filteredLinesIdx.size()-1) {
				int nextLineID = questionLines.getFilteredLine(i+1).ID;
				if (nextLineID < responseID) proceed = false; 
			}
			if (proceed) {
				// heuristic 3: the parent should have answered the question within 3 lines
				if (nResponseLines < 4 && nResponseLines != -1) {
					int allLineIdx = lineIDtoAllLinesMap.get(responseID);
					responseIndices.add(lineIDtoAllLinesMap.get(responseID));
					idxUsed.add(questionLines.filteredLinesIdx.get(i));
					
				}
			}
		}
		return responseIndices;
	}
	
	// return the stopwords counter
	public static Counter<String> getStopWords(Lines lines, int nStopWords, boolean ignoreTopFilteredTFIDF) {
		Counter<String> stopWords = new Counter<String>();
		Counter<String> wordFrequencies = lines.frequencies();	// get frequencies from all lines
		 PriorityQueue<String> wordOrder = new FastPriorityQueue<String>(wordFrequencies);
		 int total = (int)lines.filteredLinesIdx.size();
		 Counter<String> topQWords = new Counter<String>(); 
		 if (ignoreTopFilteredTFIDF) {
			 Counter<String> tfidfUnigrams = BasicDataTransformations.counterStrArrToCounterStr(lines.tfidfNGramWeights(1));
			 PriorityQueue<String> filteredWordOrderTFIDF = new FastPriorityQueue<String>(tfidfUnigrams);
			 
			 //Counter<String> stopWords = new Counter<String>();
			 
			 for (int i = 0; i < nStopWords; ++i) {
				 double firstPriority = filteredWordOrderTFIDF.getPriority();
				 topQWords.incrementCount(filteredWordOrderTFIDF.removeFirst(), firstPriority);
			 }
		 }
		 int iStopWord = 0;
		 while (! wordOrder.isEmpty() && iStopWord != nStopWords) {
			 double firstPriority = wordOrder.getPriority();
			 String word = wordOrder.removeFirst();
			 if (ignoreTopFilteredTFIDF) {
				 if (! topQWords.containsKey(word)) {
					 stopWords.incrementCount(word, firstPriority);
					 iStopWord++;
				 }
			 } else {
				 stopWords.incrementCount(word, firstPriority);
				 iStopWord++;
			 }
		 }
		 //printFrequencies("top25stopwords.txt", wordOrder, total);
		 return stopWords;
	}
	
	public static Counter<String> readFrequencyFile(String fileName) {
		Counter<String> freq = new Counter<String>();
		File f = new File(fileName);
		Scanner scanner = null;
		try {
			scanner = new Scanner(new FileReader(f));
			String s = scanner.nextLine(); // total count
			s = scanner.nextLine();	// total distinct count
			while ( scanner.hasNextLine() ){
				s = scanner.nextLine();
		    	String parts[] = s.split("\t");
		    	freq.incrementCount(parts[0], Double.parseDouble(parts[1]));
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    finally {
	      //ensure the underlying stream is always closed
	      //this only has any effect if the item passed to the Scanner
	      //constructor implements Closeable (which it does in this case).
	    	if (scanner != null)
	    		scanner.close();
	    }
	    return freq;

	}
	
}
