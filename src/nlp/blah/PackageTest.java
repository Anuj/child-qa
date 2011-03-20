package qa.nlp.blah;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
//import edu.stanford.nlp.models.lexparser.*;
import edu.stanford.nlp.ling.HasWord;
import java.util.*;

public class PackageTest {

	public static void main(String[] args) {
	
		 LexicalizedParser lp = new LexicalizedParser("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		    lp.setOptionFlags(new String[]{"-maxLength", "80", "-retainTmpSubcategories"});

		    String[] sent = { "This", "is", "an", "easy", "sentence", "." };
		    /*List<? extends HasWord> blah = new ArrayList<HasWord>(); 
		    for (String s : sent) {
		    	//blah.add(new HasWord());
		    	
		    }*/
		    	//Arrays.asList(sent);
		    Tree parse = (Tree) lp.apply(Arrays.asList(sent));
		    
		    //parse.pennPrint();
		    /*
		     * (ROOT [36.535]
				  (S [36.392]
				    (NP [6.055] (DT [2.194] This))
				    (VP [29.185] (VBZ [0.107] is)
				      (NP [23.809] (DT [3.192] an) (JJ [8.457] easy) (NN [9.419] sentence)))
				    (. [0.003] .)))
		     */
		    System.out.println();

		    TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		    Collection tdl = gs.typedDependenciesCollapsed();
		    ///System.out.println(tdl);
		    // [nsubj(sentence-5, This-1), cop(sentence-5, is-2), det(sentence-5, an-3), amod(sentence-5, easy-4)]
		    System.out.println();

		    TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
		    tp.printTree(parse);
		    /* (ROOT
				  (S
				    (NP (DT This))
				    (VP (VBZ is)
				      (NP (DT an) (JJ easy) (NN sentence)))
				    (. .)))
				
				nsubj(sentence-5, This-1)
				cop(sentence-5, is-2)
				det(sentence-5, an-3)
				amod(sentence-5, easy-4)
				*/

	}
}
