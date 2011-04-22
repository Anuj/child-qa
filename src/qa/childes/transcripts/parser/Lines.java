package qa.childes.transcripts.parser;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import danbikel.wordnet.Morphy;
import danbikel.wordnet.WordNet;

import qa.util.*;
public class Lines {

	
	public ArrayList<Line> allLines;
	// filtered line indices are not in order
	public ArrayList<Integer> filteredLinesIdx;
	public ArrayList<Transcript> transcripts;
	public HashMap<Integer, Integer> allLinesIDtoLineIDMap;
	public HashMap<Integer, Integer> lineIDtoAllLinesIDMap;
	
	public Lines() {
		allLines = new ArrayList<Line>();
		filteredLinesIdx = new ArrayList<Integer>();
		transcripts = new ArrayList<Transcript>();
		allLinesIDtoLineIDMap = new HashMap<Integer, Integer>();
		lineIDtoAllLinesIDMap = new HashMap<Integer, Integer>();
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
		allLinesIDtoLineIDMap = new HashMap<Integer, Integer>();
		lineIDtoAllLinesIDMap = new HashMap<Integer, Integer>();
		
		for (int i = 0; i < nLine; ++i) filteredLinesIdx.add(i);
		
		int transcriptI = 0;
		for (Directory d : directories) {
			for (Transcript t : d.getWorkingTranscripts()) {
				allLines.addAll(t.allLines);
				transcripts.add(t);
				++transcriptI;
			}
		}
		allLinesIDtoLineIDMap = getAllLinesIDtoLineIDMap();
		lineIDtoAllLinesIDMap = getLineIDtoAllLinesIDMap();
	}
	
	public Lines(ArrayList<Line> lines, ArrayList<Transcript> transcripts) {
		filteredLinesIdx = new ArrayList<Integer>(lines.size());
		for (int i = 0; i < lines.size(); ++i) filteredLinesIdx.add(i);
		allLines = lines;
		allLinesIDtoLineIDMap = getAllLinesIDtoLineIDMap(); // these should be passed in....
		lineIDtoAllLinesIDMap = getLineIDtoAllLinesIDMap();
		this.transcripts = transcripts;
	}
	
	// to create a shallow copy
	public Lines(ArrayList<Line> lines, ArrayList<Transcript> transcripts, ArrayList<Integer> filteredLinesIdx) {
		this.filteredLinesIdx = filteredLinesIdx;
		allLines = lines;
		this.transcripts = transcripts;
		allLinesIDtoLineIDMap = getAllLinesIDtoLineIDMap(); // these should be passed in
		lineIDtoAllLinesIDMap = getLineIDtoAllLinesIDMap();
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
		    allLinesIDtoLineIDMap = getAllLinesIDtoLineIDMap();
			lineIDtoAllLinesIDMap = getLineIDtoAllLinesIDMap();
	}

	public Lines copy() {
		ArrayList<Integer> filteredLinesIdxCopy = new ArrayList<Integer>();
		for (int i : filteredLinesIdx) 
			filteredLinesIdxCopy.add(i);
		return new Lines(this.allLines, this.transcripts, filteredLinesIdxCopy);
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
		allLinesIDtoLineIDMap = getAllLinesIDtoLineIDMap();
		lineIDtoAllLinesIDMap = getLineIDtoAllLinesIDMap();
	}
	
	public void addLine(Line l) {
		allLines.add(l);
		allLinesIDtoLineIDMap.put(allLines.size()-1, l.ID);
		lineIDtoAllLinesIDMap.put(l.ID, allLines.size()-1);
	}
	
	// filtered line indices are not in order
	public void addFilteredLineIdx(int i) {
		filteredLinesIdx.add(i);
	}
	
	public int getFilteredLineIdx(int filteredLineIdxIdx) {
		return filteredLinesIdx.get(filteredLineIdxIdx);
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
	
	public Line getFilteredLine(int idx) {
		return allLines.get(filteredLinesIdx.get(idx));
	}
	
	public Line getLineFromAll(int idx) {
		return allLines.get(idx);
	}
	/* METHODS FOR K MEANS CLUSTERING */
	public int getCentroid() {
		// the first line in Lines is the centroid
		//Line maxLine = null;
		int maxLineFilteredIdx = -1;
		int maxSimCount = -1;
		//for (Line lineCand : getFilteredLinesArr()) {
		for (int i = 0; i < filteredLinesIdx.size(); ++i) {
			int currFilteredIdx = filteredLinesIdx.get(i);
			Line lineCand = getFilteredLine(i);//getFilteredLine(currFilteredIdx);
			int tmpSimCount = 0; 
			for (Line otherLine : getFilteredLinesArr()) {
			//for (int i = 0; i < allLines.size()-1; ++i) {
				//Line otherLine = allLines.get(i);
				tmpSimCount += lineCand.getSimilarity(otherLine);
			//}
			}
			tmpSimCount -= lineCand.getSimilarity(lineCand);
			if (tmpSimCount > maxSimCount) {
				maxSimCount = tmpSimCount;
				//maxLine = lineCand;
				maxLineFilteredIdx = currFilteredIdx;
			}
		}
		return maxLineFilteredIdx;
	}

	// return the index of the most similar line
	public int getMaxSimLineIdx(Line l) {
		int maxIdx = -1;
		double maxSim = -1;
		for (int i = 0; i < filteredLinesIdx.size(); ++i) {
			//if (getFilteredLine(i) != l) {  
				double tmpSim = l.getSimilarity(getFilteredLine(i));
				if (tmpSim > maxSim) {
					maxSim = tmpSim;
					maxIdx = i;
				}
			//}
		}
		return maxIdx;
	}
	
	public void kMeansClustering(int nClusters, int nIterations, Lines centroidsOrig, ArrayList<Lines> lineGroupsOrig) {
		
		//nClusters = 40;
		//nIterations = 20;
		int nLines = filteredLinesIdx.size();
		Random r = new Random();
		Lines centroids = new Lines();
		centroids.allLines = this.allLines;
		centroids.transcripts = this.transcripts;
		
		ArrayList<Lines> lineGroups = new ArrayList<Lines>();
		
		System.out.println("Beginning K Means Clustering");
		System.out.println("Initializing clusters");
		
		// for each cluster, randomly choose a line and make it the centroid
		// create groups of lines for each cluster
		ArrayList<Integer> nums = new ArrayList<Integer>();
		for (int i = 0; i < this.filteredLinesIdx.size(); ++i)
			nums.add(i);
		for (int i = 0; i < Math.min(nClusters, nums.size()); ++i) {
			int randomLineIdx = nums.remove(r.nextInt(nums.size()));
			Line l = getFilteredLine(randomLineIdx);//linesOrder.removeFirst();
			
			//centroids.addLine(l);
			centroids.addFilteredLineIdx(getFilteredLineIdx(randomLineIdx));
			
			Lines lineGroup = new Lines();
			lineGroup.allLines = this.allLines;
			// add centroid to the line group (otherwise program could break later)
			lineGroup.filteredLinesIdx.add(filteredLinesIdx.get(randomLineIdx));
			lineGroups.add(lineGroup);
		}
		
		// for each line, assign it to the cluster with the most similar centroid
		for (int j = 0; j < filteredLinesIdx.size(); ++j) {
			Line l = getFilteredLine(j);
			int maxIdx = centroids.getMaxSimLineIdx(l);
			//lineGroups.get(maxIdx).addLine(l);
			lineGroups.get(maxIdx).addFilteredLineIdx(getFilteredLineIdx(j));
		}
		
		int currPercentage = 10;
		
		for (int i = 0; i < nIterations; ++i) {
			
			// clear centroids
			centroids.removeAllFilteredIdx();
			
			// create new line groups
			ArrayList<Lines> oldLineGroups = lineGroups;
			lineGroups = new ArrayList<Lines>();
			
			// find new centroids for each of the old line groups;
			// create line groups for each of the new clusters
			for (int iGroup = 0; iGroup < oldLineGroups.size(); ++iGroup) {
				int centroidFilteredIdx = oldLineGroups.get(iGroup).getCentroid();
				centroids.addFilteredLineIdx(centroidFilteredIdx);
				//centroids.addLine(centroid);
				Lines lineGroup = new Lines();
				lineGroup.allLines = allLines;
				lineGroup.transcripts = transcripts;
				lineGroups.add(lineGroup);
			}
			
			// for each line, assign it to a cluster
			//System.out.println(i);
			for (int j = 0; j < filteredLinesIdx.size(); ++j) {
				//System.out.println(" " + j);
				Line l = getFilteredLine(j);
				int maxIdx = centroids.getMaxSimLineIdx(l);
				lineGroups.get(maxIdx).addFilteredLineIdx(getFilteredLineIdx(j));//.filteredLinesIdx.add(filteredLinesIdx.get(j));
			}
			
			
			// Merge groups that are too small
			int j = 0;
			while (j < lineGroups.size()) {
				if (lineGroups.size() == 1) break;
				Lines ls = lineGroups.get(j);
				if (ls.filteredLinesIdx.size() < 3) {
					lineGroups.remove(j);
					centroids.filteredLinesIdx.remove(j);
					//centroids.allLines.remove(j);
					//for (Line l : ls.allLines) {
					for (int k = 0; k < ls.filteredLinesIdx.size(); ++k) {
						Line l = ls.getFilteredLine(k);
						int maxIdx = centroids.getMaxSimLineIdx(l);
						// HACK
						if (maxIdx < lineGroups.size() && maxIdx != -1)
							lineGroups.get(maxIdx).addFilteredLineIdx(getFilteredLineIdx(k));
					}
				} else {
					++j;
				}
			}
			
			if (lineGroups.size() == 1) {
				System.out.println("Too few categories.. breaking.");
				break;
			}
			if (Math.floor(i*100/nIterations) > currPercentage) {
				System.out.println(currPercentage + "% Iterations Done");
				currPercentage += 10;
			}
		}
		centroidsOrig.allLines = centroids.allLines;
		centroidsOrig.filteredLinesIdx = centroids.filteredLinesIdx;
		for (Lines ls : lineGroups) {
			lineGroupsOrig.add(ls);
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
			
			// don't count punctuation
			if (allLines.get(filteredLinesIdx.get(filterI)).grammarLine.size() > minLength) {
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
			if (! allLines.get(filteredLinesIdx.get(filterI)).containsKeywordSpoken(word)) {
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
	
	// very thrown together. this should be merged with pruneByPattern at some point
	public int pruneByPatternSpoken(ArrayList<String> pattern) {	
		ArrayList<Integer> newFilteredLinesIdx = new ArrayList<Integer>();
		int pruned = 0;
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			if (! allLines.get(filteredLinesIdx.get(filterI)).containsPatternSpoken(pattern)) {
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
	
	public void keepOnlyNotPerson(String person) {
		ArrayList<Integer> newFilteredLinesIdx = new ArrayList<Integer>();
		for (int filterI = 0; filterI < filteredLinesIdx.size(); ++filterI) {
			if (! allLines.get(filteredLinesIdx.get(filterI)).participant.equals(person)) {
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
			if (line.stemmedSpokenLine != null) {
				for (String word : line.stemmedSpokenLine)
				freq.incrementCount(word, 1.0);
			}
		}
		return freq;
	}
	
	// WANT TO MAKE THIS OBSOLETE
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
	
	
	
	public Counter<String> filteredStrNGramFrequencies(int n) {
		Counter<ArrayList<String>> nGramFreq = filteredNGramFrequencies(n);
		Counter<String> strNGramFreq  = new Counter<String>();
		for (ArrayList<String> key : nGramFreq.keySet()) {
			strNGramFreq.incrementCount(key.toString(), nGramFreq.getCount(key));
		}
		return strNGramFreq;
	}
	
	
	public Counter<ArrayList<String>> tfidfNGramWeights(int n) {
		Counter<ArrayList<String>> filteredNGramFreq = filteredNGramFrequencies(n);
		Counter<ArrayList<String>> allNGramFreq = allNGramFrequencies(n);
		Counter<ArrayList<String>> weights = new Counter<ArrayList<String>>();
		double filteredNGramTotal = filteredNGramFreq.totalCount();
		double allNGramTotal = allNGramFreq.totalCount();
		double allLinesTotal = allLines.size();
		for (ArrayList<String> nGram : filteredNGramFreq.keySet()) {
			Lines copy = this.copy();
			// tf = (# times term occurs in filtered lines) / (total count of terms in filtered lines)
			// idf = log( (# lines all) / (# lines containing the term) )  
			weights.incrementCount(nGram, (filteredNGramFreq.getCount(nGram)/filteredNGramTotal)*Math.log(allLinesTotal/allNGramFreq.getCount(nGram)));
		}
		return weights;
	}
	// return nGram frequencies of all words from filtered lines
	public Counter<ArrayList<String>> filteredNGramFrequencies(int n) {
		return getFilteredLines().allNGramFrequencies(n);
	}
	
	// NOT OBSOLETE
	public Counter<ArrayList<String>> allNGramFrequencies(int n) {
		Counter<ArrayList<String>> freq = new Counter<ArrayList<String>>();
		for (Line line : allLines) {
			// puncutation is included in these counts
			for (int i = 0; i < line.stemmedSpokenLine.size()-n; ++i) {
				ArrayList<String> tmp = new ArrayList<String>(n);
				for (int j = i; j < i+n; ++j) {
					tmp.add(line.stemmedSpokenLine.get(j));
				}
				freq.incrementCount(tmp, 1.0);
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
	
	
	public Lines getFilteredLines() {
		ArrayList<Line> lines = new ArrayList<Line>();
		//for (Iterator<Integer> it = filteredLinesIdx.iterator(); it.hasNext(); )
		//	lines.add(allLines.get(it.next()));
		for (int i = 0; i < filteredLinesIdx.size(); ++i)
			lines.add(getFilteredLine(i));
		return new Lines(lines, transcripts);
	}
	
	public ArrayList<Line> getFilteredLinesArr() {
		ArrayList<Line> lines = new ArrayList<Line>(filteredLinesIdx.size());
		for (Iterator<Integer> it = filteredLinesIdx.iterator(); it.hasNext(); )
			lines.add(allLines.get(it.next()));
		return lines;
	}
	
	
	// if the speaker is CHI, find num lines until another person talks
	// if the speaker is not CHI, find num lines until the CHI talks
	public int findNumLinesUntilOtherSpeaker(int idxFromAllLines, String speaker) {
		boolean findChild = false;
		if (! speaker.equals("CHI"))
			findChild = true;
		Line l = allLines.get(idxFromAllLines);
		int transcriptID = l.transcriptId;
		
		int count = 1;
		for (int i = idxFromAllLines+1; i < allLines.size(); ++i) {
			l = allLines.get(i);
			if (l.transcriptId != transcriptID)
				break;
			if ((findChild && l.participant.equals("CHI")) || (! findChild && ! l.participant.equals("CHI")))
				return count;
			++count;
		}
		return -1;
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
	
	

	// create mapping from lineIDs to filteredLineIdx
	// O(n^2) time
	public HashMap<Integer, Integer> getLineIDToAllLineIdxMapping(ArrayList<Integer> lineIDs) {
		HashMap<Integer, Integer> mapping = new HashMap<Integer, Integer> (); 
		for (int lineID : lineIDs) {
			for (int allLineIdx = 0; allLineIdx < this.totalLines(); ++allLineIdx) {
				if (lineID == allLines.get(allLineIdx).ID) {
					mapping.put(lineID, allLineIdx);
				}
			}
		}
		return mapping;
	}
	
	public HashMap<Integer, Integer> getLineIDToFilteredLineIdxMapping(ArrayList<Integer> lineIDs) {
		HashMap<Integer, Integer> mapping = new HashMap<Integer, Integer> (); 
		for (int lineID : lineIDs) {
			int currFilteredLineIdx;
			for (currFilteredLineIdx = 0; currFilteredLineIdx < this.filteredLinesIdx.size(); ++currFilteredLineIdx) {
				
				if (lineID == getFilteredLine(currFilteredLineIdx).ID) {
					mapping.put(lineID, currFilteredLineIdx);
				//	currFilteredLineIdx = filteredLinesIdx.size();
					break;
				}
				
			}
			if (currFilteredLineIdx == filteredLinesIdx.size()) {
				// ERROR
				int x = 0;
				x = 1;
			
			}
			
		}
		return mapping;
	}
	
	public void setStemmedLines(Morphy m, WordNet wn, HashMap<String, ArrayList<String>> memoized) {
		for (Line l : allLines) {
			l.setStemmedLine(m, wn, memoized);
		}
	}
	public void removeAllLineFeatures() {
		for (Line l : allLines)
			l.features = new Counter<String>();
	}
	public void removeAllFilteredIdx() {
		filteredLinesIdx = new ArrayList<Integer>();
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer();
		for (int i = 0 ; i < filteredLinesIdx.size(); ++i)
			s.append(getFilteredLine(i));
		return s.toString();
	}

	public void setMaps() {
		lineIDtoAllLinesIDMap = getLineIDtoAllLinesIDMap();
		allLinesIDtoLineIDMap = getAllLinesIDtoLineIDMap();
	}
	public HashMap<Integer, Integer> getAllLinesIDtoLineIDMap() {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < allLines.size(); ++i) {
			map.put(i, getLineFromAll(i).ID);
		}
		return map;
	}
	public HashMap<Integer, Integer> getLineIDtoAllLinesIDMap() {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < allLines.size(); ++i) {
			map.put(getLineFromAll(i).ID,i);
		}
		return map;
	}
	
}
