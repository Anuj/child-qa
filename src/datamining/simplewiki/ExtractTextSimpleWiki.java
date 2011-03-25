package datamining.simplewiki;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.Span;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import datamining.ParagraphInfo;
/**
 * ExtractTextSimpleWiki.java
 *
 * @author Ingrid
 */
public class ExtractTextSimpleWiki {
    
    /**
     * Takes a topic, and extracts the contents about that topic from simplewiki
     * If the topic is not in simplewiki, return null 
     */
    public List<? extends ParagraphInfo> getParagraphInfos(String concept) {
    	concept = concept .substring(0, 1).toUpperCase() + concept .substring(1);
    	String url = "http://simple.wikipedia.org/wiki/" + concept;
    	List<? extends ParagraphInfo> paragraphs = new ArrayList<SimpleWikiParagraphInfo>();
		try {
			NodeFilter pFilter = new TagNameFilter("p");
			Parser parser = new Parser (url);
			NodeList pList = parser.parse(pFilter);
			SimpleNodeIterator itr = pList.elements();
			paragraphs = getParagraphInfos(itr, concept);
		} catch (ParserException e) {
			return null;
			//e.printStackTrace();
		}
		return paragraphs;
	}

    public List<? extends ParagraphInfo> getParagraphInfos(SimpleNodeIterator itr, String concept) {
    	List<ParagraphInfo> paragraphs = new ArrayList<ParagraphInfo>();
        Node currNode = itr.nextNode();
        String currTopic = "";
        for ( ; itr.hasMoreNodes(); currNode = itr.nextNode()) {
        	currTopic = getTopic(currNode, currTopic);
        	ParagraphInfo m = new SimpleWikiParagraphInfo(concept, currTopic, currNode.toPlainTextString());
        	paragraphs.add(m);
        }
        return paragraphs;
	}
  
	public String getTopic(Node currNode, String currTopic) {
		Node prev = currNode.getPreviousSibling().getPreviousSibling();
		if (prev != null &&
			prev.getChildren() != null &&
			prev.getChildren().size() > 0 &&
			prev.getChildren().elementAt(0).getText().equals("span class=\"editsection\"") &&
			prev.getChildren().size() > 2 &&
			prev.getChildren().elementAt(2).getChildren() != null &&
			prev.getChildren().elementAt(2).getChildren().size() > 0) {
			return prev.getChildren().elementAt(2).getChildren().elementAt(0).getText();
		}
		return currTopic;	 
	}

    /**
     * Starts the program
     *
     * @param args the command line arguments
     */
  
	public static void main(String[] args) {
		ExtractTextSimpleWiki ex = new ExtractTextSimpleWiki();
		List<? extends ParagraphInfo> pInfo =ex.getParagraphInfos("dog");
		if (pInfo != null) {
			for (ParagraphInfo p : pInfo) {
				System.out.println(p);
			}
		} else {
			System.out.println("Page does not exist");
		}
	}
}
