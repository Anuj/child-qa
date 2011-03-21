package qa.childes.transcripts.parser;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import qa.util.*;
public class Lines {

	
	public ArrayList<Line> allLines;
	public ArrayList<Integer> filteredLinesIdx;
	public ArrayList<Transcript> transcripts;
	
	public Lines() {
		allLines = new ArrayList<Line>();
		filteredLinesIdx = new ArrayList<Integer>();
		transcripts = new ArrayList<Transcript>();
	}
	
	public Lines(ArrayList<Directory> directories) {
		int nLine = 0;
		for (Directory d : directories) {
			for (Transcript t : d.getWorkingTranscripts()) {
				nLine += t.allLines.size();
			}
		}
		allLines = new ArrayList<Line>(nLine);
		filteredLinesIdx = new ArrayList<Integer>(nLine);
		transcripts = new ArrayList<Transcript>();
		
		for (int i = 0; i < nLine; ++i) filteredLinesIdx.add(i);
		
		int transcriptI = 0;
		for (Directory d : directories) {
			for (Transcript t : d.getWorkingTranscripts()) {
				allLines.addAll(t.allLines);
				transcripts.add(t);
				++transcriptI;
			}
		}
	}
	
	public Lines(ArrayList<Line> lines, ArrayList<Transcript> transcripts) {
		filteredLinesIdx = new ArrayList<Integer>(lines.size());
		for (int i = 0; i < lines.size(); ++i) filteredLinesIdx.add(i);
		allLines = lines;
		this.transcripts = transcripts;
	}
	
	public Lines(ArrayList<Line> lines, ArrayList<Transcript> transcripts, ArrayList<Integer> filteredLinesIdx) {
		this.filteredLinesIdx = filteredLinesIdx;
		allLines = lines;
		this.transcripts = transcripts;
	}
	
	public Lines(String fileName) {
		allLines = new ArrayList<Line>();
		filteredLinesIdx = new ArrayList<Integer>();
		transcripts = new ArrayList<Transcript>();
		try {
		      //use buffering, reading one line at a time
		      //FileReader always assumes default encoding is OK!
		      BufferedReader input =  new BufferedReader(new FileReader(fileName));
		      try {
		        String line = null; //not declared within while loop
		        String situation = null;
		        String workingLine = input.readLine(); // read the first blank line
		        //workingLine = input.readLine();	// read the next line (assume it's there...)
		        int transcriptID = 0;
		        int currLineIdx = 0;
		        while ( ! (workingLine = input.readLine()).equals("ENDALL") ) { // if the workingLine is blank, then it is a new transcript
		        	// assume first line is transcript path
		        	String path = workingLine.substring(workingLine.indexOf('\t')+1);
		        	workingLine = input.readLine();
		        	String chiAge = workingLine.substring(workingLine.indexOf('\t')+1);
		        	workingLine = input.readLine();
		        	if (workingLine.charAt(0) == '@') {
		        		situation = workingLine.substring(workingLine.indexOf('\t')+1);
		        		workingLine = input.readLine();
		        	}
		        	transcripts.add(new Transcript(path, transcriptID, chiAge, situation));
		        	
		        	
		        	
		        	//workingLine = input.readLine();
		        	//while (! (workingLine = input.readLine()).equals("") ) {
		        	while (! workingLine.equals("") ) {
		        		// first line is a spoken line  
		        		if (workingLine.substring(0,2).equals("**") ) {
		        			filteredLinesIdx.add(currLineIdx);
		        			workingLine = workingLine.substring(1);
		        		}
		        		ArrayList<String> lines = new ArrayList<String>(5);
		        		lines.add(workingLine);
		        		while( ! (workingLine = input.readLine()).equals("") && workingLine.charAt(0) != '*') 
		        			lines.add(workingLine);
		        		allLines.add(new Line(lines, transcriptID, 0));
		        		++currLineIdx;
		        	}
		        	situation = null;
		        	++transcriptID;
		        	
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

	public Lines copy() {
		return new Lines(this.allLines, this.transcripts, this.filteredLinesIdx);
	}
	
	public void addLines(Directory d) {
		int nLines = allLines.size();
		for (Transcript t : d.getWorkingTranscripts()) {
			nLines += t.allLines.size();
		}
		
		for (Transcript t : d.getWorkingTranscripts()) {
			allLines.addAll(t.allLines);
			transcripts.add(t);
			int oldFilteredLinesSize = filteredLinesIdx.size(); 
			for (int i = oldFilteredLinesSize; i < oldFilteredLinesSize  + t.allLines.size(); ++i)
				filteredLinesIdx.add(i);
		}
	}
	
	public void addLine(Line l) {
		allLines.add(l);
	}
	
	public void synchronizeTranscriptIDs() {
		HashMap<Integer, Integer> transcriptIDMap= new HashMap<Integer, Integer>();
		int iID = 0;
		for (Transcript t : transcripts) {
			if (! transcriptIDMap.containsKey(t.transcriptID)) {
				transcriptIDMap.put(t.transcriptID, iID);
				t.transcriptID = iID;
				++iID;
			}
		}
		for (Line l : allLines) {
			l.transcriptId = transcriptIDMap.get(l.transcriptId);
		}
			
	}
	
	public Line getLine(int idx) {
		return allLines.get(filteredLinesIdx.get(idx));
	}
	
	public Line getLineFromAll(int idx) {
		return allLines.get(idx);
	}
	/* METHODS FOR K MEANS CLUSTERING */
	public Line getCentroid() {
		Line maxLine = null;
		int maxSimCount = -1;
		for (Line lineCand : allLines) {
			int tmpSimCount = 0; 
			for (Line otherLine : allLines) {
				tmpSimCount += lineCand.getSimilarity(otherLine);
			}
			tmpSimCount -= lineCand.getSimilarity(lineCand);
			if (tmpSimCount > maxSimCount) {
				maxSimCount = tmpSimCount;
				maxLine = lineCand;
			}
		}
		return maxLine;
	}

	// return the index of the most similar line
	public int getMaxSimLineIdx(Line l) {
		int maxIdx = -1;
		double maxSim = -1;
		for (int i = 0; i < allLines.size(); ++i) {
			double tmpSim = l.getSimilarity(allLines.get(i));
			if (tmpSim > maxSim) {
				maxSim = tmpSim;
				maxIdx = i;
			}
		}
		return maxIdx;
	}
	
	public void kMeansClustering(int nClusters, int nIterations, Lines centroids, ArrayList<Lines> lineGroups) {
		
		nClusters = 40;
		nIterations = 20;
		int nLines = allLines.size();
		
		Random r = new Random();
		
		System.out.println("Beginning K Means Clustering");
		System.out.println("Initializing clusters");
		
		// for each cluster, randomly choose a line and make it the centroid 
		// possibility of centroid doubles... oh well
		for (int i = 0; i < nClusters; ++i) {
			Line l = allLines.get(r.nextInt(nLines));//linesOrder.removeFirst();
			centroids.addLine(l);
			Lines lineGroup = new Lines();
			lineGroup.addLine(l);
			lineGroups.add(lineGroup);
		}
		
		// for each line, assign it to the cluster with the most similar centroid
		for (Line l : allLines) {
			int maxIdx = centroids.getMaxSimLineIdx(l);
			lineGroups.get(maxIdx).addLine(l);
		}
		
		System.out.print("Iteration: ");
		for (int i = 0; i < nIterations; ++i) {
			System.out.print(i + " ");
			
			centroids = new Lines();
			ArrayList<Lines> oldLineGroups = lineGroups;
			lineGroups = new ArrayList<Lines>();
			
			// find new centroids;
			for (int iGroup = 0; iGroup < oldLineGroups.size(); ++iGroup) {
				Lines lineGroup = new Lines();
				Line centroid = oldLineGroups.get(iGroup).getCentroid();
				centroids.addLine(centroid);
				lineGroups.add(lineGroup);
			}
			
			// create new groups
			for (Line l : allLines) {
				int maxIdx = centroids.getMaxSimLineIdx(l);
				lineGroups.get(maxIdx).addLine(l);
			}
			
			// Merge groups that are too small
			int j = 0;
			while (j < lineGroups.size()) {
				Lines ls = lineGroups.get(j);
				if (ls.allLines.size() < 4) {
					lineGroups.remove(j);
					centroids.allLines.remove(j);
					for (Line l : ls.allLines) {
						int maxIdx = centroids.getMaxSimLineIdx(l);
						lineGroups.get(maxIdx).addLine(l);
					}
				} else {
					++j;
				}
			}
		}
	}
	
	/* METHODS FOR PRUNING LINES */
	
	// BUGGY use pruning method in Directory class
	public void pruneByAge(int minYear, double minMonth, int maxYear, double maxMonth) {
		Counter<Integer> indicesToRemove = new Counter<Integer>();
		for (Transcript t : transcripts) {
			if (t.childAgeYear < minYear || (t.childAgeYear == minYear && t.childAgeMonth < minMonth)
					|| t.childAgeYear > maxYear || (t.childAgeYear == maxYear && t.childAgeMonth > maxMonth)) {
				indicesToRemove.incrementCount(t.transcriptID, 1.0);
			}
		}	
		ArrayList<Integer> newFilteredLinesIdx = new ArrayList<Integer>();
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			if (! indicesToRemove.containsKey(allLines.get(filteredLinesIdx.get(filterI)).transcriptId)) {
				//Line l = allLines.get(filteredLinesIdx.get(filterI));
				newFilteredLinesIdx.add(filteredLinesIdx.get(filterI));
			}
		}
		
		filteredLinesIdx = newFilteredLinesIdx;
	}

	// BUGGY use pruning method in Directory class
	public Lines pruneAllLinesByAge(int minYear, double minMonth, int maxYear, double maxMonth) {
		Counter<Integer> indicesToRemove = new Counter<Integer>();
		for (Transcript t : transcripts) {
			if (t.childAgeYear < minYear || (t.childAgeYear == minYear && t.childAgeMonth < minMonth)
					|| t.childAgeYear > maxYear || (t.childAgeYear == maxYear && t.childAgeMonth > maxMonth)) {
				indicesToRemove.incrementCount(t.transcriptID, 1.0);
			}
		}	
		ArrayList<Line> prunedLines = new ArrayList<Line>();
		
		//ArrayList<Integer> newFilteredLinesIdx = new ArrayList<Integer>();
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			if (! indicesToRemove.containsKey(allLines.get(filteredLinesIdx.get(filterI)).transcriptId)) {
				//Line l = allLines.get(filteredLinesIdx.get(filterI));
				prunedLines.add(allLines.get(filteredLinesIdx.get(filterI)));
				//newFilteredLinesIdx.add(filteredLinesIdx.get(filterI));
			}
		}
		
		return new Lines(prunedLines, transcripts);
		
	}

	// remove lines < minLength
	public int pruneByLength(int minLength) {
		ArrayList<Integer> newFilteredLinesIdx = new ArrayList<Integer>();
		int pruned = 0;
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			
			if (allLines.get(filteredLinesIdx.get(filterI)).grammarLine.size() >= minLength) {
				newFilteredLinesIdx.add(filteredLinesIdx.get(filterI));
			} else {
				++pruned;
			}
		}
		filteredLinesIdx = newFilteredLinesIdx;
		return pruned;
	}
	
	public int pruneByLengthOfResponse(int minLength) {
		ArrayList<Integer> newFilteredLinesIdx = new ArrayList<Integer>();
		int pruned = 0;
		for (int filterI = 0; filterI < filteredLinesIdx.size()-1; ++filterI) {
			
			if (allLines.get(filteredLinesIdx.get(filterI)+1).grammarLine.size() >= minLength) {
				newFilteredLinesIdx.add(filteredLinesIdx.get(filterI));
			} else {
				++pruned;
			}
		}
		filteredLinesIdx = newFilteredLinesIdx;
		return pruned;
	}
	
	public int pruneByWords(String words) {
		int pruned = 0;
		String[] keywords = words.split("%");
		for (String keyword : keywords) {
			pruned += pruneByWord(keyword);
		}
		return pruned;
	}
	// remove lines that contain the word 
	public int pruneByWord(String word) {
		ArrayList<Integer> newFilteredLinesIdx = new ArrayList<Integer>();
		int pruned = 0;
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			if (! allLines.get(filteredLinesIdx.get(filterI)).containsKeyword(word)) {
				newFilteredLinesIdx.add(filteredLinesIdx.get(filterI));
			} else {
				++pruned;
			}
		}
		filteredLinesIdx = newFilteredLinesIdx;
		return pruned;
	}
	
	// takes a string of words delimited by %
	// if the word begins with !, then prune by
	// otherwise, keep
	public int keepPruneByWords(String words) {
		int pruned = 0;
		String[] keywords = words.split("%");
		boolean[] keep = new boolean[keywords.length];
		int keywordIdx = 0;
		for (String keyword: keywords) {
			if (keyword.charAt(0) == '!')
				keywords[keywordIdx] = keywords[keywordIdx].substring(1);
			else 
				keep[keywordIdx] = true;
			++keywordIdx;
		}
		
		ArrayList<Integer> newFilteredLinesIdx = new ArrayList<Integer>();
		
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			Line l = allLines.get(filteredLinesIdx.get(filterI));
			boolean keepLine = true;
			for (keywordIdx = 0; keywordIdx < keywords.length; ++keywordIdx) {
				if (keep[keywordIdx]) {
					if (! l.containsKeyword(keywords[keywordIdx])) {
						keepLine = false;
						break;
					}
				} else {
					if (l.containsKeyword(keywords[keywordIdx])) {
						keepLine = false;
						break;
					}
				}
			
			}
			if (keepLine) {
				newFilteredLinesIdx.add(filteredLinesIdx.get(filterI));
			} else {
				++pruned;
			}
		}
		filteredLinesIdx = newFilteredLinesIdx;
		return pruned;
	}
	
	public int pruneByPattern(String pattern) {
		ArrayList<String> patternArrList = new ArrayList<String>();
		String[] patternArr = pattern.split(" ");
		for (String p : patternArr) patternArrList.add(p);		
		return pruneByPattern(patternArrList);
	}
	
	public int pruneByPattern(ArrayList<String> pattern) {	
		ArrayList<Integer> newFilteredLinesIdx = new ArrayList<Integer>();
		int pruned = 0;
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			if (! allLines.get(filteredLinesIdx.get(filterI)).containsPattern(pattern)) {
				newFilteredLinesIdx.add(filteredLinesIdx.get(filterI));
			} else {
				++pruned;
			}
		}
		filteredLinesIdx = newFilteredLinesIdx;
		return pruned;
	}

	// keep only lines with the person
	public void keepOnlyPerson(String person) {
		ArrayList<Integer> newFilteredLinesIdx = new ArrayList<Integer>();
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			if (allLines.get(filteredLinesIdx.get(filterI)).participant.equals(person)) {
				newFilteredLinesIdx.add(filteredLinesIdx.get(filterI));
			}
		}
		filteredLinesIdx = newFilteredLinesIdx;
	}

	// keep only lines that contain the pos
	public void keeyOnlyLinesWithPOS(String pos) {
		ArrayList<Integer> newFilteredLinesIdx = new ArrayList<Integer>();
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			if (allLines.get(filteredLinesIdx.get(filterI)).containsPOS(pos)) {
				newFilteredLinesIdx.add(filteredLinesIdx.get(filterI));
			}
		}
		filteredLinesIdx = newFilteredLinesIdx;
	}
	
	// keep only lines with the pattern, delimited by spaces
	// keywords within pattern can be of the form: *, pos|, pos|word, word
	public int keepOnlyLinesWithPattern(String pattern) {
		ArrayList<String> patternArrList = new ArrayList<String>();
		String[] patternArr = pattern.split(" ");
		for (String p : patternArr) patternArrList.add(p);		
		return keepOnlyLinesWithPattern(patternArrList);
	}
	
	public int keepOnlyLinesWithPattern(ArrayList<String> pattern) {	
		ArrayList<Integer> newFilteredLinesIdx = new ArrayList<Integer>();
		int kept = 0;
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			if (allLines.get(filteredLinesIdx.get(filterI)).containsPattern(pattern)) {
				newFilteredLinesIdx.add(filteredLinesIdx.get(filterI));
				++kept;
			}
		}
		filteredLinesIdx = newFilteredLinesIdx;
		return kept;
	}
	
	// keep only lines that contain any of the patterns, delimited by %
	// read description of keeyOnlyLinesWithPattern(String pattern), for what patterns are valid
	public void keeyOnlyLinesWithPatterns(String patternStr) {
		ArrayList<ArrayList<String>> patterns = new ArrayList<ArrayList<String>>();
		String[] patternsArr = patternStr.split("%");
		for (String pattern : patternsArr) {//List) { 
			ArrayList<String> patternArrList = new ArrayList<String>();
			String[] patternArr = pattern.split(" ");
			for (String p : patternArr) patternArrList.add(p);
			patterns.add(patternArrList);
		}
		keeyOnlyLinesWithPatterns(patterns);
	}
	
	public void keeyOnlyLinesWithPatterns(ArrayList<ArrayList<String>> patterns) {
		ArrayList<Integer> newFilteredLinesIdx = new ArrayList<Integer>();
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			boolean containsAPattern = false;
			Line line = allLines.get(filteredLinesIdx.get(filterI));
			for (ArrayList<String> pattern : patterns)
				if (line.containsPattern(pattern)) {
					containsAPattern = true;
					break;
				}
			if (containsAPattern)
				newFilteredLinesIdx.add(filteredLinesIdx.get(filterI));
			
		}
		filteredLinesIdx = newFilteredLinesIdx;
	}

	
	
	// keep only lines with any of the keywords, delimited by spaces
	// keywords can be of the forms: pos|, pos|word, word
	public int keepOnlyLinesWithKeywords(String keywords) {
		ArrayList<String> keywordsArrList = new ArrayList<String>();
		String[] keywordsArr = keywords.split(" ");
		for (String p : keywordsArr) keywordsArrList.add(p);
		return keepOnlyLinesWithKeywords(keywordsArrList);
	}
	
	// keep only lines with any of the keywords, delimited by spaces
	// keywords can be of the forms: pos|, pos|word, word
	public int keepOnlyLinesThatAlternate() {
		ArrayList<Integer> newFilteredLinesIdx = new ArrayList<Integer>();
		int kept = 0;
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			String filteredLinesSpeaker = allLines.get(filteredLinesIdx.get(filterI)).participant;
			//String blah = allLines.get(filteredLinesIdx.get(filterI)).spokenLineStr;
			String responseSpeaker = allLines.get(filteredLinesIdx.get(filterI)+1).participant;
			//String blah2 = allLines.get(filteredLinesIdx.get(filterI)+1).spokenLineStr;
			if (! filteredLinesSpeaker.equals(responseSpeaker)) {
				newFilteredLinesIdx.add(filteredLinesIdx.get(filterI));
				++kept;
			}
		}
		filteredLinesIdx = newFilteredLinesIdx;
		return kept;
	}
	
	public int keepOnlyLinesWithKeywords(ArrayList<String> keywords) {
		ArrayList<Integer> newFilteredLinesIdx = new ArrayList<Integer>();
		int kept = 0;
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			if (allLines.get(filteredLinesIdx.get(filterI)).containsOneKeyword(keywords)) {
				newFilteredLinesIdx.add(filteredLinesIdx.get(filterI));
				++kept;
			}
		}
		filteredLinesIdx = newFilteredLinesIdx;
		return kept;
	}
		
	/* STATS METHODS */
	// return frequencies of all words from all lines  (unfiltered)
	public Counter<String> frequencies() {
		Counter<String> freq = new Counter<String>();
		for (Line line : allLines) {
			if (line.grammaticalLine != null) {
				for (String word : line.grammaticalLine)
				freq.incrementCount(word, 1.0);
			}
		}
		return freq;
	}
	
	// return frequencies of all words from filtered lines
	public Counter<String> filteredFrequencies() {
		Counter<String> freq = new Counter<String>();
		
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			Line line = allLines.get(filteredLinesIdx.get(filterI));
			if (line.grammaticalLine != null) {
				for (String word : line.grammaticalLine)
				freq.incrementCount(word, 1.0);
			}
		}
		return freq;
	}
	
	// return nGram frequencies of all words from filtered lines
	public Counter<ArrayList<String>> filteredNGramFrequencies(int n) {
		Counter<ArrayList<String>> freq = new Counter<ArrayList<String>>();
		
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			Line line = allLines.get(filteredLinesIdx.get(filterI)); 
			if (line.grammaticalLine != null) {
				// puncutation is included in these counts
				for (int i = n-1; i < line.grammaticalLine.size(); ++i) {
					ArrayList<String> tuple = new ArrayList<String>(n);
					for (int j = i+1-n; j <= i; ++j)
						tuple.add(line.grammaticalLine.get(j));
					freq.incrementCount(tuple, 1.0);
				}	
			}
		}
		return freq;
	}
	
	public Counter<ArrayList<String>> allNGramFrequencies(int n) {
		Counter<ArrayList<String>> freq = new Counter<ArrayList<String>>();
		
		for (Line line : allLines) {
			if (line.grammaticalLine != null) {
				// puncutation is included in these counts
				for (int i = n-1; i < line.grammaticalLine.size(); ++i) {
					ArrayList<String> tuple = new ArrayList<String>(n);
					for (int j = i+1-n; j <= i; ++j)
						tuple.add(line.grammaticalLine.get(j));
					freq.incrementCount(tuple, 1.0);
				}	
			}
		}
		return freq;
	}
	
	// return nGram frequencies of all words from filtered lines
	public Counter<ArrayList<String>> filteredNGramPOSFrequencies(int n) {
		Counter<ArrayList<String>> freq = new Counter<ArrayList<String>>();
		
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			Line line = allLines.get(filteredLinesIdx.get(filterI)); 
			if (line.grammarLine != null) {
				// puncutation is included in these counts
				for (int i = n-1; i < line.grammarLine.size(); ++i) {
					ArrayList<String> tuple = new ArrayList<String>(n);
					for (int j = i+1-n; j <= i; ++j)
						tuple.add(line.grammarLine.get(j));
					freq.incrementCount(tuple, 1.0);
				}	
			}
		}
		return freq;
	}
	
	public Counter<ArrayList<String>> allNGramPOSFrequencies(int n) {
		Counter<ArrayList<String>> freq = new Counter<ArrayList<String>>();
		
		
		for (Line line : allLines) {
			if (line.grammarLine != null) {
				// puncutation is included in these counts
				for (int i = n-1; i < line.grammarLine.size(); ++i) {
					ArrayList<String> tuple = new ArrayList<String>(n);
					for (int j = i+1-n; j <= i; ++j)
						tuple.add(line.grammarLine.get(j));
					freq.incrementCount(tuple, 1.0);
				}	
			}
		}
		return freq;
	}
	
	/*public Counter<ArrayList<String>> allNGramLineFrequencies(int n) {
		Counter<ArrayList<String>> freq = new Counter<ArrayList<String>>();
		
		for (Line line : allLines) {
			if (line.grammaticalLine != null) {
				// puncutation is included in these counts
				for (int i = n-1; i < line.grammaticalLine.size(); ++i) {
					ArrayList<String> tuple = new ArrayList<String>(n);
					for (int j = i+1-n; j <= i; ++j)
						tuple.add(line.grammaticalLine.get(j));
					freq.incrementCount(tuple, 1.0);
				}	
			}
		}
		return freq;
	}*/
	
	
	// return frequencies of words of the POS from filtered lines
	public Counter<String> filteredFrequenciesPOS(String pos) {
		Counter<String> freq = new Counter<String>();
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			Line line = allLines.get(filteredLinesIdx.get(filterI));
			if (line.grammarLine != null) {
				for (int i = 0; i < line.grammarLine.size(); ++i) {
					if (line.grammarLine.get(i).equals(pos))
						freq.incrementCount(line.grammaticalLine.get(i), 1.0);
				}
			}
		}
		return freq;
	}
	
	// return frequencies of words of the POS from filtered lines
	public Counter<String> filteredTranscriptFrequencies() {
		Counter<String> freq = new Counter<String>();
		Counter<String> freqForTranscript = new Counter<String>();
		int transcriptID = -1;
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			Line line = allLines.get(filteredLinesIdx.get(filterI));
			if (line.transcriptId != transcriptID) {
				freq.incrementAll(freqForTranscript);
				transcriptID = line.transcriptId;
				freqForTranscript = new Counter<String>();
			}
			if (line.grammarLine != null) {
				for (int i = 0; i < line.grammarLine.size(); ++i) {
						freqForTranscript.setCount(line.grammaticalLine.get(i), 1.0);
				}
			}
		}
		return freq;
	}
	
	// return frequencies of words of the POS from filtered lines
	public Counter<String> filteredTranscriptFrequenciesPOS(String pos) {
		Counter<String> freq = new Counter<String>();
		Counter<String> freqForTranscript = new Counter<String>();
		int transcriptID = -1;
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			Line line = allLines.get(filteredLinesIdx.get(filterI));
			if (line.transcriptId != transcriptID) {
				freq.incrementAll(freqForTranscript);
				transcriptID = line.transcriptId;
				freqForTranscript = new Counter<String>();
			}
			if (line.grammarLine != null) {
				for (int i = 0; i < line.grammarLine.size(); ++i) {
					if (line.grammarLine.get(i).equals(pos))
						freqForTranscript.setCount(line.grammaticalLine.get(i), 1.0);
				}
			}
		}
		return freq;
	}
	// return frequencies of bigrams from all lines (unfiltered)
	public Counter<String> bigramFrequency() {
		Counter<String> freq = new Counter<String>();
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			Line line = allLines.get(filteredLinesIdx.get(filterI));
			// ignore punctuation mark at the end
			if (line.grammaticalLine != null) {
				for (int i = 1; i < line.grammaticalLine.size()-1; ++i) {
					freq.incrementCount(line.grammaticalLine.get(i-1) + " " + line.grammaticalLine.get(i), 1.0);
				}	
			}
		}
		return freq;
	}
	
	// return frequencies of bigrams from filtered lines
	public Counter<String> filteredBigramFrequency() {
		Counter<String> freq = new Counter<String>();
		
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			Line line = allLines.get(filteredLinesIdx.get(filterI)); 
			if (line.grammaticalLine != null) {
				// ignore punctuation mark at the end
				for (int i = 1; i < line.grammaticalLine.size()-1; ++i) {
					freq.incrementCount(line.grammaticalLine.get(i-1) + " " + line.grammaticalLine.get(i), 1.0);
				}	
			}
		}
		return freq;
	}
	
	public Lines getFilteredLines() {
		ArrayList<Line> lines = new ArrayList<Line>();
		for (Iterator<Integer> it = filteredLinesIdx.iterator(); it.hasNext(); )
			lines.add(allLines.get(it.next()));
		return new Lines(lines, transcripts);
	}
	
	
	public ArrayList<Line> getKWAL(int nBefore, int nAfter, int lineIdx) {
		//ArrayList<Lines> lineGroups = new ArrayList<Lines>();
		
		//int filterI = lineIdxToAnalyze;
		//for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			//int lineIdx = filteredLinesIdx.get(filterI);
			
			int start = lineIdx-nBefore;
			int end = lineIdx + nAfter;
			
			// make sure that the before, after lines do not go outside the indices of allLines
			// and furthermore, that the cluster of lines all belong to the same transcript
			if (lineIdx - nBefore < 0)  {
				start = 0;
			} else if (allLines.get(lineIdx - nBefore).transcriptId != allLines.get(lineIdx).transcriptId) {
				for (int i = lineIdx-nBefore+1; i <= lineIdx; ++i) {
					if (allLines.get(i).transcriptId == allLines.get(lineIdx).transcriptId) {
						start = i;
						break;
					}
				}
			}
			
			if (lineIdx + nAfter >= allLines.size()) {// 0 + 1 >= size(2)
				end = allLines.size() - 1;
			} else if (allLines.get(lineIdx + nAfter).transcriptId != allLines.get(lineIdx).transcriptId) {
				for (int i = lineIdx+nAfter-1; i >= lineIdx; --i) {
					if (allLines.get(i).transcriptId == allLines.get(lineIdx).transcriptId) {
						end = i;
						break;
					}
				}
			}
			
			ArrayList<Line> lineGroup = new ArrayList<Line>();
			for (int i = start; i <= end; ++i)
				lineGroup.add(allLines.get(i));
		
			return lineGroup;
			//ArrayList<Transcript> transcript = new ArrayList<Transcript>(1);
			//transcript.add(transcripts.get(allLines.get(lineIdx).transcriptId));
			//lineGroups.add(new Lines(lineGroup, transcript));	
		//}	
		//return lineGroups;
	}
	
	public ArrayList<Lines> getKWAL(int nBefore, int nAfter) {
		ArrayList<Lines> lineGroups = new ArrayList<Lines>();
		
		
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			int lineIdx = filteredLinesIdx.get(filterI);
			
			int start = lineIdx-nBefore;
			int end = lineIdx + nAfter;
			
			// make sure that the before, after lines do not go outside the indices of allLines
			// and furthermore, that the cluster of lines all belong to the same transcript
			if (lineIdx - nBefore < 0)  {
				start = 0;
			} else if (allLines.get(lineIdx - nBefore).transcriptId != allLines.get(lineIdx).transcriptId) {
				for (int i = lineIdx-nBefore+1; i <= lineIdx; ++i) {
					if (allLines.get(i).transcriptId == allLines.get(lineIdx).transcriptId) {
						start = i;
						break;
					}
				}
			}
			
			if (lineIdx + nAfter >= allLines.size()) {// 0 + 1 >= size(2)
				end = allLines.size() - 1;
			} else if (allLines.get(lineIdx + nAfter).transcriptId != allLines.get(lineIdx).transcriptId) {
				for (int i = lineIdx+nAfter-1; i >= lineIdx; --i) {
					if (allLines.get(i).transcriptId == allLines.get(lineIdx).transcriptId) {
						end = i;
						break;
					}
				}
			}
			
			ArrayList<Line> lineGroup = new ArrayList<Line>();
			for (int i = start; i <= end; ++i)
				lineGroup.add(allLines.get(i));
		
			ArrayList<Transcript> transcript = new ArrayList<Transcript>(1);
			transcript.add(transcripts.get(allLines.get(lineIdx).transcriptId));
			lineGroups.add(new Lines(lineGroup, transcript));	
		}	
		return lineGroups;
	}
	
	public int totalLines() {
		int count = 0;
		for (Line l : allLines)
			if (l.grammarLine.size() > 0)
				++count;
		return count;
	}
	
	public int totalFilteredLines() {
		int count = 0;
		for (Integer idx : filteredLinesIdx) {
			if ( allLines.get(idx).grammarLine.size() > 0)
				++count;
		}
		return count;
	}
	
	public ArrayList<String> strArrayToArrayList(String[] strArray) {
		ArrayList<String> l = new ArrayList<String>();
		for (String s : strArray)
			l.add(s);
		return l;
			
	}
	
	// change all CHI: I/me/my/you/your to chi/chi/chi-pos/adu/adu-pos
	// change all OTHER: I/me/my/you/your to adu/adu/adu-pos/chi/chi-pos
	public void simpleCoferenceResolution() {
		for (Line l : allLines) {
			String participant = l.participant;
			if (participant.equals("CHI")) {
				for (int i = 0; i < l.grammaticalLine.size(); ++i) {
					String word = l.grammaticalLine.get(i);
					 if (word.equals("I")) {
						l.grammaticalLine.set(i, "chi"); 
					 } else if (word.equals("me")) {
						 l.grammaticalLine.set(i, "chi"); 
					 } else if (word.equals("my")) {
						 l.grammaticalLine.set(i, "chi-pos");
					 } else if (word.equals("you")) {
						 l.grammaticalLine.set(i, "adu");
					 } else if (word.equals("your")) {
						 l.grammaticalLine.set(i, "adu-pos");
					 }
				}
			} else {
				for (int i = 0; i < l.grammaticalLine.size(); ++i) {
					String word = l.grammaticalLine.get(i);
					 if (word.equals("I")) {
						l.grammaticalLine.set(i, "adu"); 
					 } else if (word.equals("me")) {
						 l.grammaticalLine.set(i, "adu"); 
					 } else if (word.equals("my")) {
						 l.grammaticalLine.set(i, "adu-pos");
					 } else if (word.equals("you")) {
						 l.grammaticalLine.set(i, "chi");
					 } else if (word.equals("your")) {
						 l.grammaticalLine.set(i, "chi-pos");
					 }
				}
			}
		}
	}
	
	// NEW FILE FORMAT
	/*
	 * @FilePath: filePath
	 * @ChiAge: 4;1.15
	 * @Situation: situation (opt)
	 * *CHI:	see
	 * %mor:	co|see
	 * %gra:	1|0|ROOT 2|1|PUNCT
	 * *CHI:	it's Pucile@c .
	 * *%mor:	pro|it~v:cop|be&3S n:prop|Pucile .
	 * %gra:	1|2|SUBJ 2|0|ROOT 3|2|PRED 4|2|PUNCT
	 * %com:	Cecelia
	 * ....
	 *  
	 *  @FilePath: filePath
	 *  ...
	 * ENDALL
	 */
	
	public static void printLines(String outputName, Lines filteredLines) {
		
		FileOutputStream out; // declare a file output object
		PrintStream p; // declare a print stream object
		//ArrayList<String> keywordLines = lines.allLines; 
		//int totalLines = allLines.totalLines();//allLines.allLines.size();
		
		try
		{
			// Create a new file output stream
			out = new FileOutputStream(outputName);

			// Connect print stream to the output stream	
			p = new PrintStream( out );
			
			//p.println("Total Lines: " + totalLines);
			
			int transcriptID = -1;
			//int i = 0;
			int currLineIdx = 0;
			int nextUnreadFilteredLineIdx = 0;
			
			for (Line line : filteredLines.allLines) {
				
				Transcript t = filteredLines.transcripts.get(line.transcriptId); 
				if (t.transcriptID != transcriptID) {
					p.println("");
					p.println("@FilePath:\t" + t.path);
					p.println("@ChiAge:\t" + t.getChiAge());
					if (t.fileProperties.containsKey("situation"))
						p.println(t.fileProperties.get("situation"));
					transcriptID = t.transcriptID;
				}
				if (nextUnreadFilteredLineIdx < filteredLines.filteredLinesIdx.size() && filteredLines.filteredLinesIdx.get(nextUnreadFilteredLineIdx) == currLineIdx) {
					p.print("*");
					++nextUnreadFilteredLineIdx;
				}
				p.println(line.spokenLineStr);
				for (String lineType : line.otherLines.keySet()) {
					p.println("@" + lineType + ":\t" + line.otherLines.get(lineType));
				}
				
				++currLineIdx;
				//++i;
			}
			p.println();
			p.println("ENDALL");
			p.close();
		}
		catch (Exception e)
		{
			System.err.println ("Error writing to file");
		}
	}

	public void replace(String oldWord, String newWord) {
		for (Line line : allLines) {
			for (int i = 0; i < line.grammaticalLine.size(); ++i) {
				if (line.grammaticalLine.get(i).equals(oldWord))
					line.grammaticalLine.set(i,newWord);
			}
		}
	}
	public ArrayList<LinesKWAL> createLinesKWALArray(Lines lines, int nBefore, int nAfter) {
		//%ArrayList<Lines> lineGroups = new ArrayList<Lines>();
		ArrayList<LinesKWAL> lineGroups = new ArrayList<LinesKWAL>(lines.filteredLinesIdx.size());
		
		
		for (int filterI = 0; filterI < lines.filteredLinesIdx.size(); ++filterI) {
			int lineIdx = lines.filteredLinesIdx.get(filterI);
			
			int start = lineIdx-nBefore;
			int end = lineIdx + nAfter;
			
			// make sure that the before, after lines do not go outside the indices of allLines
			// and furthermore, that the cluster of lines all belong to the same transcript
			if (lineIdx - nBefore < 0)  {
				start = 0;
			} else if (lines.allLines.get(lineIdx - nBefore).transcriptId != lines.allLines.get(lineIdx).transcriptId) {
				for (int i = lineIdx-nBefore+1; i <= lineIdx; ++i) {
					if (lines.allLines.get(i).transcriptId == lines.allLines.get(lineIdx).transcriptId) {
						start = i;
						break;
					}
				}
			}
			
			ArrayList<Line> linesStart =new ArrayList<Line>();
			for (int i = start; i < lineIdx; ++i) 
				linesStart.add(lines.allLines.get(i));
			
			if (lineIdx + nAfter >= lines.allLines.size()) {// 0 + 1 >= size(2)
				end = lines.allLines.size() - 1;
			} else if (lines.allLines.get(lineIdx + nAfter).transcriptId != lines.allLines.get(lineIdx).transcriptId) {
				for (int i = lineIdx+nAfter-1; i >= lineIdx; --i) {
					if (lines.allLines.get(i).transcriptId == lines.allLines.get(lineIdx).transcriptId) {
						end = i;
						break;
					}
				}
			}
			ArrayList<Line> linesEnd = new ArrayList<Line>();
			for (int i = lineIdx+1; i <= end; ++i) 
				linesEnd.add(lines.allLines.get(i));
			
			if(linesStart.size() != 0 && linesEnd.size() != 0)
				lineGroups.add(new LinesKWAL(linesStart,linesEnd,lines.allLines.get(lineIdx)));	
		}	
		return lineGroups;
	
}
	
}
