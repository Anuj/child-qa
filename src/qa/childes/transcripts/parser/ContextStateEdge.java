package qa.childes.transcripts.parser;
import java.util.ArrayList;
import java.util.HashMap;


public class ContextStateEdge {
	ContextStateNode nodeI;
	ContextStateNode nodeJ;
	double potentialIJ;
	double potentialIJ2;
	int edgeIdx;
	int oppEdgeIdx;
	String relation;
	double messageII;
	ArrayList<ArrayList<Double>> potentialsIJ;
	
	public ContextStateEdge(int edgeIdx, ContextStateNode nodeI, ContextStateNode nodeJ,
			double potentialIJ, double potentialIJ2, String relation, int oppEdgeIdx) {
		this.nodeI = nodeI;
		this.nodeJ = nodeJ; 
		this.edgeIdx = edgeIdx;
		this.potentialIJ = potentialIJ;
		this.potentialIJ2 = potentialIJ2;
		this.relation = relation;
		this.messageII = nodeI.messageII;
		this.oppEdgeIdx = oppEdgeIdx;
	}
	
	public ContextStateEdge(int edgeIdx, ContextStateNode nodeI, ContextStateNode nodeJ,
			double potentialIJ, double potentialIJ2, String relation, int oppEdgeIdx, double messageII) {
		this.nodeI = nodeI;
		this.nodeJ = nodeJ; 
		this.edgeIdx = edgeIdx;
		this.potentialIJ = potentialIJ;
		this.potentialIJ2 = potentialIJ2;
		this.relation = relation;
		this.oppEdgeIdx = oppEdgeIdx;
		this.messageII = messageII;
	}
	
	public void calculatePotentials() {
		ArrayList<Double> tmpPotentials = new ArrayList<Double>(2);
		tmpPotentials.add(.1);
		tmpPotentials.add(potentialIJ);
		potentialsIJ = new ArrayList<ArrayList<Double>>(2);
		potentialsIJ.add(tmpPotentials);
		tmpPotentials = new ArrayList<Double>(2);
		tmpPotentials.add(potentialIJ2);
		tmpPotentials.add(.01);
		potentialsIJ.add(tmpPotentials);
	}
	
	public double getPotential(int value1, int value2) {
		return potentialsIJ.get(value1).get(value2);
	}
	
	public double getMessageII(int valueI) {
		if (valueI == 1)
			return messageII;
		else
			return 1;
	}
	
	public void synchronizeOppEdgeIdx(HashMap<ArrayList<ContextStateNode>, Integer> edgeIndices, int idx) {
		if (this.edgeIdx != idx) {
			System.out.println("uh oh...");
		}
		this.edgeIdx = idx; // JUST IN CASE ma
		ArrayList<ContextStateNode> oppEdge = new ArrayList<ContextStateNode>(2);
		oppEdge.add(nodeJ);
		oppEdge.add(nodeI);
		
		oppEdgeIdx = edgeIndices.get(oppEdge);
		
	}
	//public ArrayList<Double> calculateEdge() {
		
	//}
}
