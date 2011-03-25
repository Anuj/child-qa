package datamining.simplewiki;
import datamining.ParagraphInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import datamining.ParagraphInfo;


public class SimpleWikiParagraphInfo implements ParagraphInfo {

	public String concept;
	public String topic;
	public String text;
	
	public SimpleWikiParagraphInfo(String concept, String topic, String text) {
		this.concept = concept;
		this.topic = topic;
		this.text = text;
	}

	public String toString() {
		return concept + ":" + topic + "--" + text;
	}
	
	
  
  
}