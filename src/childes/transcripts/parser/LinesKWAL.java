package qa.childes.transcripts.parser;
import java.util.ArrayList;

import qa.util.*;

public class LinesKWAL {

	ArrayList<Line> beforeLines;
	Counter<String> beforeLinesCount;
	ArrayList<Line> afterLines;
	Line rootLine;
	
	public LinesKWAL(ArrayList<Line> beforeLines, ArrayList<Line> afterLines, Line rootLine) {
		this.beforeLines = beforeLines;
		this.afterLines = afterLines;
		this.rootLine = rootLine;
		beforeLinesCount = new Counter<String>();
		
	}
	
	public void initializeBeforeLinesCount(ArrayList<Line> beforeLines) {
		for (Line l : beforeLines) {
			for (String w : l.spokenLine2) {
				beforeLinesCount.incrementCount(w, 1.0);
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
				for (int i = lineIdx+1; i < end; ++i) 
					linesStart.add(lines.allLines.get(i));
				
				
				lineGroups.add(new LinesKWAL(linesStart,linesEnd,lines.allLines.get(lineIdx)));	
			}	
			return lineGroups;
		
	}
	
	public ArrayList<ContextState> createContextStates() {
		ArrayList<ContextState> contextStates = new ArrayList<ContextState>();
		int nBefore = beforeLines.size();
		boolean inState = false;
		int iLine = 0;
		int start = 0;
		ArrayList<Integer> nounIndices;
		ArrayList<String> nouns = null;
		ArrayList<String> newNouns = null; 
		ArrayList<Line> currLines = new ArrayList<Line>();
		while (iLine < nBefore) {
			Line line = beforeLines.get(iLine);
			nounIndices = line.retPOSIndices("n:");
			if (nounIndices.size() > 0) {
				nouns = new ArrayList<String>(nounIndices.size());
				for (int i : nounIndices)
					nouns.add(line.grammaticalLine.get(i));
				currLines.add(line);
				break;
			}
			++iLine;
		}
		if (nouns != null) { // enter in with nouns already
			for (iLine = iLine+1; iLine < nBefore; ++iLine) {
				Line line = beforeLines.get(iLine);
				nounIndices = line.retPOSIndices("n:");
				
				if (nounIndices.size() != 0) {
					newNouns = new ArrayList<String>(nounIndices.size());
					for (int i : nounIndices)
						try {
							newNouns.add(line.grammaticalLine.get(i));
						} catch(IndexOutOfBoundsException e) {
							int blah = 0;
							blah = 0;
						}
				}
				
				if (nounIndices.size() == 0 || newNouns.equals(nouns))  {
					currLines.add(line);
				} else {
					contextStates.add(new ContextState(nouns, currLines));
					currLines = new ArrayList<Line>();
					currLines.add(line);
					nouns = newNouns;
					//nouns = new ArrayList<String>(nounIndices.size());
					//for (int i : nounIndices)
						//nouns.add(line.spokenLine2.get(i));
				}
			}
			contextStates.add(new ContextState(nouns, currLines));
		} // else there is no nouns in the entire context, and there are no states. i don't believe this will happen
		// an exception should be thrown or something?
		
		// collapse the states that have the same nouns
		// there is the possibility that two nouns will be metonyms, but this is unlikely 
		/*int i = 0;
		start = 0;
		int stop = 0;
		ArrayList<String> currNouns = contextStates.get(i).nouns;
		ArrayList<ArrayList<Integer>> contextStatesIdxToCollapse = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> range;
		for (i = i+1; i < contextStates.size(); ++i) {
			ArrayList<String> nextNouns = contextStates.get(i).nouns;
			if (currNouns.equals(nextNouns)) {
				++stop;
			} else {
				range = new ArrayList<Integer>(2);
				range.add(start);
				range.add(stop);
				contextStatesIdxToCollapse.add(range);
				currNouns = nextNouns;
				start = i;
				stop = i;
			}
		}
		range = new ArrayList<Integer>(2);
		range.add(start);
		range.add(stop);
		contextStatesIdxToCollapse.add(range);
		ArrayList<ContextState> newContextStates = new ArrayList<ContextState>();
		for (i = 0; i < contextStatesIdxToCollapse.size(); ++i) 
			newContextStates.add(new ContextState(contextStates, contextStatesIdxToCollapse.get(i).get(0), contextStatesIdxToCollapse.get(i).get(1)));
			*/
		
	return contextStates;	
	}
	
}

