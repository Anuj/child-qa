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

public class RegexFinder {

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
		 
		 
		 ArrayList<Tree> questionTrees = new ArrayList<Tree>(lines.filteredLinesIdx.size());
		 LexicalizedParser lp = new LexicalizedParser("/home/pomegranate/workspace/child-qa/parser/englishPCFG.ser.gz");
		 int sentenceIdx = 0;		 
		 
		 CounterMap<ArrayList<String>, Line> allConnections = findConnections(3, questionTrees, lines);
		 
		 Counter<String> tfidfUnigrams = BasicDataTransformations.counterStrArrToCounterStr(lines.tfidfNGramWeights(1));
		 PriorityQueue<String> wordOrder = new FastPriorityQueue<String>(tfidfUnigrams);
		 
		 
		 //for (int i = 0; i < 25; ++i) {
		//	 String word = wordOrder.removeFirst();
		//	 System.out.println("Total Questions " + word + " by Child (of the ones remaining): " + lines.keepOnlyLinesWithKeywords(word));
		 //}
		 String[] whs = {"what", "why", "how", "where"};
		 
		 
		 //for (int i = 0; i < 25; ++i) {
		 for (int i = 0; i < whs.length; ++i) {
			 //String word = wordOrder.removeFirst();
			 String word = whs[i];
			 lines.removeAllLineFeatures();
			 
			 Lines copy = lines.copy();
			 System.out.println("Total Questions " + word + " by Child (of the ones remaining): " + copy.keepOnlyLinesWithKeywords(word));
			 lines.pruneByWord(word.substring(0,word.length()-1));
			 //lines.pruneByPattern(word);
			 //filteredLines = copy.getFilteredLinesArr();
			 int nFilteredLines = copy.filteredLinesIdx.size();
			 questionTrees = new ArrayList<Tree>(nFilteredLines);
			 for (int j = 0; j < nFilteredLines; ++j) {
				 Line l = copy.getFilteredLine(j);
				 Tree parse = (Tree) lp.apply(Arrays.asList(l.spokenLine));
				 questionTrees.add(parse);
				// System.out.println(j + "parse done");
			 }

			 allConnections = findConnections(3, questionTrees, copy);
				
			 // CODE FOR K MEANS
			 Counter<ArrayList<String>> compressedCounter = allConnections.getCounterCompressed();
			 compressedCounter.normalize();
			 double allConnectionsAvg = compressedCounter.totalCount()/compressedCounter.size();
			 compressedCounter.incrementAll(1-allConnectionsAvg);

			 
			 /* K MEANS CLUSTERING ALGORITHM */
			
			// add connectionsFeatures. this is so tedious.			 
			ArrayList<Integer> lineIDs = new ArrayList<Integer>();
			for (ArrayList<String> p : allConnections.keySet()) {
				Counter<Line> linesWithP = allConnections.getCounter(p);
				for (Line l : linesWithP.keySet()) {
					l.features.incrementCount(p.toString(), compressedCounter.getCount(p));
					//lineIDMap.put(l.ID, l.ID);
				}
			}
			
			
			ArrayList<Line> linesSubsetList = new ArrayList<Line>(questionTrees.size());
			//for (int j = 0; j < questionTrees.size(); ++j) {
			//	linesSubsetList.add(copy.getLine(j));
			//}
			Lines linesSubset = new Lines(copy.getFilteredLinesArr(), lines.transcripts);
			
			int nClusters = 40, nIterations = 500;
			Lines centroids = new Lines();
			centroids.allLines = copy.allLines;
			centroids.transcripts = copy.transcripts;
			ArrayList<Lines> lineGroups = new ArrayList<Lines>();
			copy.kMeansClustering(nClusters, nIterations, centroids, lineGroups);
			//linesSubset.kMeansClustering(nClusters, nIterations, centroids, lineGroups);
			System.out.println();
			
			AnalyzeMerged.printSentenceClusters(lineGroups, centroids, i + word + "_clusters.txt", copy);
		 }
		 
		 
		 
		 /*
		 ArrayList<Tree> questionTrees = new ArrayList<Tree>(lines.filteredLinesIdx.size());
		 LexicalizedParser lp = new LexicalizedParser("/home/pomegranate/workspace/child-qa/parser/englishPCFG.ser.gz");
		 int sentenceIdx = 0;
		 //for (Line l : lines.getFilteredLinesArr()) {
		 ArrayList<String> spokenSentences = new ArrayList<String>(lines.filteredLinesIdx.size());
		 ArrayList<Line> filteredLines = lines.getFilteredLinesArr();
		 for (int i = 0; i < 526; ++i) {
			 Line l = filteredLines.get(i);
			 System.out.println("Parsing sentence" + sentenceIdx++);
			 Tree parse = (Tree) lp.apply(Arrays.asList(l.spokenLine));
			 questionTrees.add(parse);
			 //spokenSentences.add(lines.getLine(i).spokenLineStr);
		 }
		 
	
		 CounterMap<ArrayList<String>, Line> allConnections = findConnections(3, questionTrees, lines);
	
		 // CODE FOR K MEANS
		 Counter<ArrayList<String>> compressedCounter = allConnections.getCounterCompressed();
		 compressedCounter.normalize();
		 double allConnectionsAvg = compressedCounter.totalCount()/compressedCounter.size();
		 compressedCounter.incrementAll(1-allConnectionsAvg);
		 // calculate stats for the connections
		 PriorityQueue<ArrayList<String>> dependencyOrder  = new FastPriorityQueue<ArrayList<String>>(compressedCounter);
		 // calculate stats for unigram weights
		 Counter<ArrayList<String>> unigramWeights = lines.tfidfNGramWeights(1);//lines.filteredNGramFrequencies(1);
		 Counter<ArrayList<String>> bigramWeights = lines.tfidfNGramWeights(2);//lines.filteredNGramFrequencies(2);
		 unigramWeights.normalize();
		 bigramWeights.normalize();
		 double unigramWeightsAvg = unigramWeights.totalCount()/unigramWeights.size();
		 unigramWeights.incrementAll(1-unigramWeightsAvg);
		 double bigramWeightsAvg = bigramWeights.totalCount()/bigramWeights.size();
		 bigramWeights.incrementAll(1-bigramWeightsAvg);
		 unigramWeights.scale(.5);
		 bigramWeights.scale(.5);
		 */
		 
		 
		 /* K MEANS CLUSTERING ALGORITHM */
		 /*
			ArrayList<Line> linesSubsetList = new ArrayList<Line>(questionTrees.size());
			for (int i = 0; i < questionTrees.size(); ++i) {
				linesSubsetList.add(lines.getLine(i));
			}
			
			// add connectionsFeatures. this is so tedious.			 
			ArrayList<Integer> lineIDs = new ArrayList<Integer>();
			for (ArrayList<String> p : allConnections.keySet()) {
				Counter<Line> linesWithP = allConnections.getCounter(p);
				for (Line l : linesWithP.keySet()) {
					l.features.incrementCount(p.toString(), compressedCounter.getCount(p));
					//lineIDMap.put(l.ID, l.ID);
				}
			}
			Lines linesSubset = new Lines(linesSubsetList, lines.transcripts);
			// add unigram features
			//for (Line l : linesSubset.getFilteredLines().allLines) {
			//	l.incrementFeaturesWithNgrams(unigramWeights, 1);
			//	l.incrementFeaturesWithNgrams(bigramWeights, 2);
			//}
			
			
			
			int nClusters = 40, nIterations = 100;
			Lines centroids = new Lines();
			ArrayList<Lines> lineGroups = new ArrayList<Lines>();
			linesSubset.kMeansClustering(nClusters, nIterations, centroids, lineGroups);
			for (Line l : centroids.allLines) {
				System.out.println(l.spokenLineStr);
			}
			AnalyzeMerged.printSentenceClusters(lineGroups, centroids, "_clusters.txt");
		
			*/
		  
		 
	}	 
	
	public static CounterMap<ArrayList<String>, Line> findConnections(int connLength, ArrayList<Tree> questionTrees, Lines lines) {//ArrayList<String> spokenSentences) {
		// we only look at parent -> child connections (for now); sibling dependencies will have to wait
		// assume connLength >= 1;

		
		CounterMap<ArrayList<String>, Line> allConnections = new CounterMap<ArrayList<String>, Line>();
		
		int sentenceIdx = 0;
		for (Tree t : questionTrees) {
			
			ArrayList<ArrayList<String>> dep = new ArrayList<ArrayList<String>>(1);
			Tree currTree = t;//t.children()[0];
			Line currLine = lines.getFilteredLine(sentenceIdx);
			Counter<String> linePrefixFeatures = new Counter<String>();
			
			ArrayList<String> oneDep = new ArrayList<String>(1);
			oneDep.add(currTree.label().toString()); // should be "ROOT";
			
			LinkedList<ArrayList<String>> prefixes = findConnectionsTree(oneDep, currTree.children(), connLength);
			
			
			for (ArrayList<String> p : prefixes) {
				allConnections.incrementCount(p, currLine, 1.0);
				//linePrefixFeatures.incrementCount(p.toString(), 1.0);
			}
			//ArrayList<String> spokenLineCopy = new ArrayList<String>(); 
			//for (int i = 0; i < currLine.spokenLine.length; ++i) {
			//	spokenLineCopy.add(currLine.spokenLine[i]);
			//}
			
			// PROBABLY AN INAPPROPRIATE PLACE TO SET FEATURES 
			//currLine.features = linePrefixFeatures;
			//currLine.incrementFeaturesWithWords();
			++sentenceIdx;
			
		}
		
		
		/*
		Counter<ArrayList<String>> compressedCounter = allConnections2.getCounterCompressed();
		PriorityQueue<ArrayList<String>> dependencyOrder  = new FastPriorityQueue<ArrayList<String>>(compressedCounter);
		
		
		for (int i = 0; i < 10; ++i) {
			double p = dependencyOrder.getPriority();
			ArrayList<String> prefix = dependencyOrder.removeFirst();
			System.out.println(p + " : " + prefix);
			Counter<Line> sentences = allConnections2.getCounter(prefix);
			for (Line key : sentences.keySet()) {
				//if (key.spokenLineStr.substring(0,9).equals("*CHI:	why"))
					System.out.println("\t" + key.spokenLineStr);
			}
		}*/
		
		return allConnections;
		
		
	}
	public static LinkedList<ArrayList<String>> findConnectionsTree(ArrayList<String> prefix, Tree[] treeChildren, int connLength) {
		
		if (connLength == 0) {
			LinkedList<ArrayList<String>> onePrefix = new LinkedList<ArrayList<String>>();
			onePrefix.add(prefix);
			
			return onePrefix;
		}
		LinkedList<ArrayList<String>> prefixes = new LinkedList<ArrayList<String>>();
		
		for (Tree t : treeChildren) {
			if (! t.isLeaf()) {
				ArrayList<String> tmp = new ArrayList<String>(prefix.size()+1);
				for (int i = 0; i < prefix.size(); ++i) {
					tmp.add(prefix.get(i));
				}
				
				tmp.add(t.label().toString());
				prefixes.addAll(findConnectionsTree(tmp, t.children(), connLength-1));
				
				tmp = new ArrayList<String>(prefix.size());
				for (int i = 1; i < prefix.size(); ++i) {
					tmp.add(prefix.get(i));
				}
				tmp.add(t.label().toString());
				prefixes.addAll(findConnectionsTree(tmp, t.children(), connLength));
			}
		}
		return prefixes;
	}
}
