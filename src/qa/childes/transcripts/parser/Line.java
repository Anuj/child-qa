package qa.childes.transcripts.parser;

import java.util.ArrayList;
import java.util.HashMap;

import qa.util.*;


public class Line {
	
	public String[] spokenLine;
	ArrayList<String> spokenLine2;
	ArrayList<String> grammarLine;
	ArrayList<String> grammaticalLine;
	public String spokenLineStr;
	public String participant;
	public HashMap<String, String> otherLines;
	public Counter<String> features;
	public int ID = 0;
	public int transcriptId;
	
	
	/** constructor for a Line object
	 * 
	 * @param lines : ArrayList<String>. 
	 * 					the first String should be the spoken line
	 * 					subsequent Strings should be descriptors of the line (mor, etc.)
	 * @param transcriptId : int. a unique numerical ID for the Line  
	 */
	public Line(ArrayList<String> lines, int transcriptId, int lineId) {
		ID = lineId;
		
		grammarLine = new ArrayList<String>(0);
		grammaticalLine = new ArrayList<String>(0);
		this.transcriptId = transcriptId;
		spokenLineStr = lines.get(0);
		spokenLine = lines.get(0).split(" ");
		
		participant = spokenLine[0].substring(1, spokenLine[0].indexOf('\t')-1);
		spokenLine[0] = spokenLine[0].substring(spokenLine[0].indexOf('\t')+1);
		
		// NEW CODE REPLACE TRANSLATIONS SUCH AS dis [: this]
		spokenLine = makeGrammatical(spokenLine);
		
		otherLines = new HashMap<String, String>();
		for (int i = 1; i < lines.size(); ++i) {
			String otherLine = lines.get(i);
			if (otherLine.length() > 1 && otherLine.indexOf('\t') != -1 && otherLine.charAt(0) == '%') {
				
				String[] tmpLine = otherLine.split(" ");
				
				
					String lineType = tmpLine[0].substring(1, tmpLine[0].indexOf('\t')-1);
					String lineStr = otherLine.substring(otherLine.indexOf('\t')+1);//otherLine.split("\t")[1];
					tmpLine[0] = tmpLine[0].substring(tmpLine[0].indexOf('\t')+1);
					if (lineType.equals("mor"))
						parseMor(tmpLine);
					if (lineType.equals("act")) {
						int j = 0;
						j = 0;
					}
					otherLines.put(lineType, lineStr);//lines.get(i).substring());
				
			}
		}
		spokenLine2 = strArrayToArrayList(spokenLine);
		features = new Counter<String>();
	}
	
	/**
	 * correct translations such as dis [: this]
	 * 
	 * return String[]
	 */
	public String[] makeGrammatical(String[] spokenLine) {
		ArrayList<String> newSpokenLine = new ArrayList<String>();
		for (int i = 0; i < spokenLine.length; ++i) {
			if (spokenLine[i].equals("[:")) {
				newSpokenLine.remove(newSpokenLine.size()-1);
				newSpokenLine.add(spokenLine[++i].substring(0, spokenLine[i].length()-1));
			} else if (spokenLine[i].equals("(.)")) {
				int x; 
				x = 1;
			} else if (spokenLine[i].indexOf('(') != -1) { // in the case of an(d)
				int paren1 = spokenLine[i].indexOf('(');
				int paren2 = spokenLine[i].indexOf(')');
				String s = spokenLine[i].substring(0,paren1) + spokenLine[i].substring(paren1+1, paren2);
				if (paren2 != spokenLine[i].length()-1) 
					s += spokenLine[i].substring(paren2+1);
				newSpokenLine.add(s);
			} else {
				newSpokenLine.add(spokenLine[i]);
			}
		}
		String[] newSpokenLine2 = new String[newSpokenLine.size()];
		newSpokenLine.toArray(newSpokenLine2);
		return newSpokenLine2;
	}
	/** 
	 * parses a mor line according to the CHAT transcription protocol 
	 *  
	 * @param morLine : String. the mor line from a transcript
	 */
	public void parseMor(String[] morLine) {
		grammarLine = new ArrayList<String>(morLine.length+3);
		grammaticalLine = new ArrayList<String>(morLine.length+3);
		int i = 0;
		for (String element : morLine) {
			String[] separateWords = null;
			if (element.indexOf('~') != -1)  {// conjunction
				separateWords = element.split("~");
			} else {
				separateWords = new String[1];
				separateWords[0] = element;
			}
			for (String subElement : separateWords) {
				int pipeIdx = subElement.indexOf('|');
				if (pipeIdx == -1) {
					grammarLine.add(subElement);
					grammaticalLine.add(subElement);
				} else {
					grammarLine.add(subElement.substring(0,pipeIdx));
					grammaticalLine.add(subElement.substring(pipeIdx+1));
				}
				++i;
			}
		}
	}
	
	public boolean containsKeywordSpoken(String keyword) {
		for (String s : spokenLine2)
			if (s.equals(keyword))
				return true;
		return false;
	}
	/**
	 * determines whether the Line has the specified keyword
	 * Note: if pos is of the form pos:, include all pos:*
	 * 
	 * @param keyword : String. pos|, pos|word OR word
	 * @return : boolean. true if keyword is in the line 
	 */
	public boolean containsKeyword(String keyword) {
		int j;
		ArrayList<String> line;
		String word;
		String pos = "";
		boolean containsPOS = false;
		boolean containsWord = true;
		boolean includeAllPOSSubtypes = false;
		int pipeIdx = keyword.indexOf('|');
		
		
		// if keyword is of the form: pos|word
		if (pipeIdx != -1) {
			if (grammarLine.size() == 0)
				return false; // this should really be an exception.... 
			pos = keyword.substring(0,pipeIdx);
			int colon = pos.indexOf(':');
			if (colon == pos.length()-1) {
				includeAllPOSSubtypes = true;
				pos = pos.substring(0,pos.length()-1);
			}
			word= keyword.substring(pipeIdx+1);
			containsPOS = true;
			if (word.equals("")) containsWord = false; // if keyword is of the form pos|
		} else { // else keyword is of the form: word
			word = keyword;
		}
		
		ArrayList<String> l = null;
		if (containsPOS)
			l = grammaticalLine;
		else 
			l = spokenLine2;
		for (int i = 0; i < l.size(); ++i) {
			if (containsWord) { // if need to check word
				if (l.get(i).equals(word)) { // check if the word is there
					if (containsPOS) { // if ALSO need to check pos 
						String linePOS = grammarLine.get(i);
						if (includeAllPOSSubtypes) {	// if pos includes all subtypes
							if (linePOS.indexOf(':') != -1
								&& linePOS.substring(0,linePOS.indexOf(':')).equals(pos))
								return true;
							else if (linePOS.equals(pos))
								return true;
						} else if (linePOS.equals(pos)) { // pos does not include all subtypes
							return true;
						}
					} else {
						return true;
					}
				}
			} else  {
				if (containsPOS) {
					if (containsPOS) {
						String linePOS = grammarLine.get(i);
						if (includeAllPOSSubtypes) {
							if (linePOS.indexOf(':') != -1
								&& linePOS.substring(0,linePOS.indexOf(':')).equals(pos))
								return true;
							else if (linePOS.equals(pos))
								return true;
						} else if (linePOS.equals(pos)) return true;
					}
				}
			}
		}
		
		return false;
	}
	
	/** 
	 * Determine whether the Line at the specified index is the keyword
	 * 
	 * @param keyword : String. pos|, pos|word OR word
	 * @param index : int. index in the Line
	 * @return : boolean. 
	 */
	public boolean containsKeyword(String keyword, int index) {
		
		//ArrayList<String> line;
		String keywordWord = "";
		String keywordPOS = "";
		// THIS WILL BREAK SOMETHING
		String lineWord = spokenLine2.get(index);
		//String lineWord = grammaticalLine.get(index);
		String linePOS = "";//grammarLine.get(index);
		boolean checkPOSAll = false;
		boolean checkPOS = false;
		boolean checkWord = false;
		int pipeIdx = keyword.indexOf('|');
		int colonIdx = keyword.indexOf(':');
		int nKeyword = keyword.length();
		if (pipeIdx == -1) {
			checkWord = true;
			keywordWord = keyword;
		} else {
			checkPOS = true;
			keywordPOS = keyword.substring(0,pipeIdx);
			if (pipeIdx != nKeyword-1) {
				checkWord = true;
				keywordWord = keyword.substring(pipeIdx+1);
			}
			if (colonIdx != -1) {
				checkPOSAll = true;
				keywordPOS = keywordPOS.substring(0,colonIdx);
				int linePOSColonIdx =linePOS.indexOf(':');
				if (linePOSColonIdx != -1)
					linePOS = linePOS.substring(0,linePOSColonIdx);
			}
		}
		if (checkWord && ! lineWord.equals(keywordWord))
			return false;
		
		if (checkPOS) 
			return linePOS.equals(keywordPOS);
		return true;
		
	
		
		/*String word;
		String pos = "";
		boolean containsPOS = false;
		
		int pipeIdx = keyword.indexOf('|');
		
		//if (grammaticalLine != null)
		//	line = grammaticalLine;
		//else
		//	line = spokenLine2;
		
		// if keyword is of the form: pos|word
		if (pipeIdx != -1) {
			pos = keyword.substring(0,pipeIdx);
			word= keyword.substring(pipeIdx+1);
			if (pos.equals(""))
				return grammaticalLine.get(index).equals(word);
			
			boolean hasPOS = false;
			boolean allPOS = false;
			int colon = pos.indexOf(':');
			if (colon == pos.length()-1) {
				allPOS = true;
				pos = pos.substring(0,pos.length()-1);
			}
			String linePOS = grammarLine.get(index);
			if (allPOS) {
				int lineColon = linePOS.indexOf(':');
				if (lineColon != -1) {
					linePOS = linePOS.substring(0,lineColon);
				}
				hasPOS = pos.equals(linePOS);
			}
			
			if (word.equals(""))
				return hasPOS;
			return hasPOS && grammaticalLine.get(index).equals(word);
		//	if (grammarLine == null)
		//		return false; // this should really be an exception.... 
		//	pos = keyword.substring(0,pipeIdx);
		//	word= keyword.substring(pipeIdx+1);
		//	containsPOS = true;
		} else { // else keyword is of the form: word
			return grammaticalLine.get(index).equals(keyword);
		}*/
		
	}
	
	public boolean containsKeywordSpoken(String keyword, int index) {
		
		//ArrayList<String> line;
		String keywordWord = "";
		String keywordPOS = "";
		String lineWord = spokenLine2.get(index);
		
		return lineWord.equals(keyword);	
	
		
		/*String word;
		String pos = "";
		boolean containsPOS = false;
		
		int pipeIdx = keyword.indexOf('|');
		
		//if (grammaticalLine != null)
		//	line = grammaticalLine;
		//else
		//	line = spokenLine2;
		
		// if keyword is of the form: pos|word
		if (pipeIdx != -1) {
			pos = keyword.substring(0,pipeIdx);
			word= keyword.substring(pipeIdx+1);
			if (pos.equals(""))
				return grammaticalLine.get(index).equals(word);
			
			boolean hasPOS = false;
			boolean allPOS = false;
			int colon = pos.indexOf(':');
			if (colon == pos.length()-1) {
				allPOS = true;
				pos = pos.substring(0,pos.length()-1);
			}
			String linePOS = grammarLine.get(index);
			if (allPOS) {
				int lineColon = linePOS.indexOf(':');
				if (lineColon != -1) {
					linePOS = linePOS.substring(0,lineColon);
				}
				hasPOS = pos.equals(linePOS);
			}
			
			if (word.equals(""))
				return hasPOS;
			return hasPOS && grammaticalLine.get(index).equals(word);
		//	if (grammarLine == null)
		//		return false; // this should really be an exception.... 
		//	pos = keyword.substring(0,pipeIdx);
		//	word= keyword.substring(pipeIdx+1);
		//	containsPOS = true;
		} else { // else keyword is of the form: word
			return grammaticalLine.get(index).equals(keyword);
		}*/
		
	}

	/** 
	 * Determine whether the Line has any of the listed keywords
	 * 
	 * @param keywords ArrayList of keywords
	 * @return boolean.
	 */
	public boolean containsOneKeyword(ArrayList<String> keywords) {
		
		for (int i = 0; i < keywords.size(); ++i) {
			if (this.containsKeyword(keywords.get(i)))
				return true;
		}
		return false;
	}
	
	public boolean containsPattern(ArrayList<String> pattern, boolean useGrammaticalLine) {
		int j = 0;
		boolean next;
		
		// * works as a regex does
		// if * isn't the first element, then the first element of pattern must be the first word of the sentence
		if (pattern.get(0).equals("*")) {
			next = false;
			++j;
		} else {
			next = true;
		}
		
		
		ArrayList<String> line ;
		
		if (useGrammaticalLine)
			line = grammaticalLine;
		else
			line = spokenLine2;
		//line = spokenLine2;
		
		for (int i = 0; i < line.size(); ++i) {
			if (containsKeyword(pattern.get(j), i)) {//line.get(i).equals(pattern.get(j))) {
				++j;
				
				if (j == pattern.size())
					break;
				
				if (pattern.get(j).equals("*")) {
					next = false;
					++j;
					if (j == pattern.size())
						break;
				} else {
					next = true;
				}
				
			} else {
				if (next)
					return false;
			}
		}
		if (j == pattern.size()) {
			if (pattern.get(pattern.size()-1).equals("*")) 
				return true;
			else
				return containsKeyword(pattern.get(pattern.size()-1), line.size()-1);
		}
	
		return false;
	}
	
	// use spokenLine2 if there is no grammar part in the pattern
	// useGrammaticaLine by default
	public boolean containsPattern(ArrayList<String> pattern) {
		for (String s : pattern)
			if (s.indexOf('|') != -1)
				return containsPattern(pattern, true);
		return containsPattern(pattern, false);
	}
	
	public boolean containsPatternSpoken(ArrayList<String> pattern) {
		return containsPattern(pattern, false);
	}
	
	public boolean containsPOS(String pos) {
		int pipeIdx = pos.indexOf('|');
		boolean posGeneral = false;
		if (pipeIdx == pos.length()-1) {
			posGeneral = true;
		}
		if (grammarLine == null)
			return false;
		for (int i = 0; i < grammarLine.size(); ++i) {
			String candidatePOS = grammarLine.get(i);
			if (posGeneral) {
				int candidatePipeIdx = candidatePOS.indexOf('|');
				if (candidatePipeIdx != -1)
					candidatePOS = candidatePOS.substring(0,pipeIdx);
			}
			if (candidatePOS.equals(pos))
				return true;
		}
		return false;
	}
	
	public ArrayList<String> strArrayToArrayList(String[] strArray) {
		ArrayList<String> l = new ArrayList<String>();
		for (String s : strArray)
			l.add(s);
		return l;
	}
	public Counter<String> extractFeatures(Counter<String> commonWords) {
		Counter<String> features = new Counter<String>();
		// add unigram features
		//if (grammaticalLine != null)
		ArrayList<String> line;
		if (grammaticalLine == null)
			line = strArrayToArrayList(spokenLine);
		else
			line = grammaticalLine;
		
		for (String unigram : line)
			if (commonWords.containsKey(unigram))
				features.incrementCount(unigram, 1.0);
		if (line.size() > 1) {
			for (int i = 1; i < line.size(); ++i)
				features.incrementCount(line.get(i-1) + line.get(i), 1.0);
		}
		if (grammarLine != null)
			for (String partOfSpeech : grammarLine) {
				features.incrementCount(partOfSpeech, 1.0);
			}
		
		if (line.size() == 1)
			features.incrementCount("one", 1.0);
		if (line.size() == 2)
			features.incrementCount("two", 1.0);
		if (line.size() == 3)
			features.incrementCount("three", 1.0);
		
		
		
		features.normalize();
		return features;
	}
	
	public void setFeatures(Counter<String> commonWords) {
		features = extractFeatures(commonWords);
	}

	public String toString() {
		return spokenLineStr;
	}
	public double getSimilarity(Line l) {
		double sim = 0;
		for (String key : this.features.keySet()) {
			if (l.features.containsKey(key)) {
				sim += l.features.getCount(key)*this.features.getCount(key); // this can be modified;
			}
		}
		return sim;
	}
	
	public ArrayList<Integer> retPOSIndices(String pos) {
		ArrayList<Integer> posIndices = new ArrayList<Integer>();
		int colonIdx = pos.indexOf(':');
		boolean posGeneral = false;
		if (colonIdx == pos.length()-1) {
			posGeneral = true;
			pos = pos.substring(0,pos.length()-1);
		}
		if (grammarLine == null)
			return posIndices;
		
		for (int i = 0; i < grammarLine.size(); ++i) {
			String candidatePOS = grammarLine.get(i);
			if (posGeneral) {
				int candidatePipeIdx = candidatePOS.indexOf(':');
				if (candidatePipeIdx != -1)
					candidatePOS = candidatePOS.substring(0,colonIdx);
			}
			if (candidatePOS.equals(pos))
				posIndices.add(i);
		}
		return posIndices;
	}
	
	public boolean equals(Object other) {
		Line otherLine = (Line)other;
		return spokenLine.equals(otherLine.spokenLine);
	}
	
	public int hashCode() {
		return spokenLine.hashCode();
	}

	
	// using spokenLine
	public ArrayList<ArrayList<String>> getNGrams(int n) {
		ArrayList<ArrayList<String>> nGrams = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < spokenLine.length-n; ++i) {
			ArrayList<String> tmp = new ArrayList<String>(n);
			for (int j = i; j < i+n; ++j) {
				tmp.add(spokenLine[j]);
			}
			nGrams.add(tmp);
		}
		return nGrams;
	}
	
	
	public void incrementFeaturesWithNgrams(Counter<ArrayList<String>> weights, int n) {
		ArrayList<String> line = spokenLine2;
		
		for (ArrayList<String> nGram : getNGrams(n))
			if (weights.containsKey(nGram))
				features.incrementCount(nGram.toString(), weights.getCount(nGram));
	}
	
	
	public void normalizeFeatures() {
		features.normalize();
	}
	
}
