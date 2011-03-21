package qa.childes.transcripts.parser;
import java.util.ArrayList;
import java.util.HashMap;


public class ContextStateNode {
	int nodeIdx;
	double messageII;
	
	ArrayList<ContextStateEdge> contextStateEdges;
	ContextState node;
	HashMap<ContextStateNode, Integer> neighbors; // indexed by the neighbor node, value is the position in the contextStateEdges
	
	public ContextStateNode(int nodeIdx, ContextState node, double messageII) {
		this.nodeIdx = nodeIdx;
		this.messageII = messageII;
		contextStateEdges = new ArrayList<ContextStateEdge>();
		this.node = node;
		neighbors = new HashMap<ContextStateNode, Integer>();
	}
	
	public boolean addEdge(ContextStateEdge e) {
		
		e.messageII = messageII;
		if (neighbors.containsKey(e.nodeJ)) {
			ContextStateEdge existingEdge = contextStateEdges.get(neighbors.get(e.nodeJ));
			if (existingEdge.potentialIJ < e.potentialIJ) {
				contextStateEdges.add(e);
				neighbors.put(e.nodeJ, contextStateEdges.size()-1);
				return true;
			} else {
				return false;
			}
		} else {
			contextStateEdges.add(e);
			neighbors.put(e.nodeJ, contextStateEdges.size()-1);
			return true;
		}
		
	}
	
	public void incrementPotentialIJ(ContextState other, double increment) {
		int edgeIdx = neighbors.get(other);
		contextStateEdges.get(edgeIdx).potentialIJ += increment;
	}
	public void incrementPotentialIJ2(ContextState other, double increment) {
		int edgeIdx = neighbors.get(other);
		contextStateEdges.get(edgeIdx).potentialIJ2 += increment;
	}
	
	// calculates /prod_{x_k in N(x_i) \ x_j} m_{ki}(x_i) = /prod_{x_k in N(x_i) \ x_j} m_{ik}(x_i)  
	public double getProductOfNeighboringMessages(ArrayList<ArrayList<Double>> messages, int valueI, ContextStateNode exclude) {
		double product = 1;
		for (ContextStateEdge e_ik : contextStateEdges) {
			if (! e_ik.nodeJ.equals(exclude)) {
				int edge_kiIdx = e_ik.oppEdgeIdx;
				//ContextStateEdge_ki = allEdges.get(edge_kiIdx);
				product *= messages.get(edge_kiIdx).get(valueI);
			}
		}
		return product;
	}
	
	public String toString() {
		return node.nouns.get(0);
	}
	/*public double getMessageII(int valueI) {
		if (valueI == 1)
			return messageII;
		else
			return 1;
	}*/
}
