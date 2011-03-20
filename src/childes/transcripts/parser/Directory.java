package qa.childes.transcripts.parser;
/** 
 * Used as an intermediate object to process a directory of CHAT transcription files
 */
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;


public class Directory {

	private ArrayList<String> transcriptPaths;
	private ArrayList<Transcript> allTranscripts;
	private ArrayList<Transcript> workingTranscripts;

	
	// shallow copy
	public Directory(Directory d) {
		this.setTranscriptPaths(d.getTranscriptPaths());
		this.allTranscripts = d.allTranscripts;
		this.workingTranscripts = d.workingTranscripts;
	}
	
	public Directory(String path) {
		this(path, 0);
	}
	
	public Directory(String path, int transcriptIDBegin) { // create transcripts of all .cha files in the path given
		File dir = new File(path);
		if (! dir.isDirectory()) {
			
		} else {
			
			String[] dirChildren = dir.list();
			this.transcriptPaths = new ArrayList<String>(dirChildren.length);
			for (int i = 0; i < dirChildren.length; ++i) {
				String dirChildName = dirChildren[i];
				int dirChildLength = dirChildName.length();
				if (dirChildLength > 5 && dirChildName.substring(dirChildLength-5).equals(".chai")) {
					this.transcriptPaths.add(path + "/" + dirChildName);
				}
			}
		}
		
		this.transcriptPaths = sortedArrayList(this.transcriptPaths);
		
		//setTranscriptPaths(sortedArrayList(getTranscriptPaths()));
		
		this.allTranscripts = new ArrayList<Transcript>();
		this.workingTranscripts = new ArrayList<Transcript>();
		
		int transcriptId = transcriptIDBegin;
		for (String sFile : this.transcriptPaths) {
			
			Transcript t = new Transcript(sFile, transcriptId);
			this.allTranscripts.add(t);
			this.workingTranscripts.add(t);
			++transcriptId;
		}
	}
	
	public Directory(ArrayList<Transcript> allTranscripts, ArrayList<Transcript> workingTranscripts) {
		this.allTranscripts = allTranscripts;
		this.workingTranscripts = workingTranscripts;
	}
	
	
	
	public void pruneByAge(int minYear, double minMonth, int maxYear, double maxMonth) {
		ArrayList<Integer> indicesToRemove = new ArrayList<Integer>();
		int i = 0;
		for (Transcript t : workingTranscripts) {
			if (t.childAgeYear < minYear || (t.childAgeYear == minYear && t.childAgeMonth < minMonth)
					|| t.childAgeYear > maxYear || (t.childAgeYear == maxYear && t.childAgeMonth > maxMonth)) 
				indicesToRemove.add(i);
			++i;
		}
		
		int iToRemove = 0;
		int iTranscriptIdx = 0;
		ArrayList<Transcript> newWorkingTranscripts = new ArrayList<Transcript>();
		while (iToRemove < indicesToRemove.size()) {
			if (indicesToRemove.get(iToRemove) == iTranscriptIdx) {
				++iToRemove;
				++iTranscriptIdx;
			} else if (indicesToRemove.get(iToRemove) > iTranscriptIdx) {
				newWorkingTranscripts.add(workingTranscripts.get(iTranscriptIdx));
				++iTranscriptIdx;
			} else {
				break; // ERROR
			}
		}
		while (iTranscriptIdx < workingTranscripts.size()) {
			newWorkingTranscripts.add(workingTranscripts.get(iTranscriptIdx));
			++iTranscriptIdx;
		}
		workingTranscripts = newWorkingTranscripts;
	}

	public int getTotalLines() {
		int count = 0;
		for (Transcript transcript : allTranscripts)
			count += transcript.getTotalLines();
		return count;
	}


	// insertion sort... because I'm lazy
	public ArrayList<String> sortedArrayList(ArrayList<String> oldArray) {
		LinkedList<String> newArray = new LinkedList<String>();
		boolean inserted = false;
		for (String s : oldArray) {
			int j = 0;
			while (j < newArray.size()) {
				if (s.compareTo(newArray.get(j)) < 0) {
					newArray.add(j, s);
					inserted = true;
					break;
				}
				++j;
			}
			
			if (! inserted)
				newArray.add(s);
			inserted = false;
		}
		ArrayList<String> newArrList = new ArrayList<String>(oldArray.size());
		for (String s : newArray)
			newArrList.add(s);
		return newArrList;
	}

	public void setTranscriptPaths(ArrayList<String> transcriptPaths) {
		this.transcriptPaths = transcriptPaths;
	}

	public ArrayList<String> getTranscriptPaths() {
		return transcriptPaths;
	}
	
	public ArrayList<Transcript> getWorkingTranscripts() {
		return workingTranscripts; 
	}
	
	public ArrayList<Transcript> getAllTranscripts() {
		return allTranscripts; 
	}
	
}
