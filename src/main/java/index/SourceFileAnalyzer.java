package index;

import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.CharArraySet;
//import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;

//import com.genesisdo.chinalxr.lucene.NumericFilter;
//import com.genesisdo.chinalxr.lucene.ProgramLanguageSyntaxFilter;
//import com.genesisdo.chinalxr.lucene.tokenizer.SourceFileTokenizer;
//import me.juanmacias.SourceFileTokenizer;

public class SourceFileAnalyzer extends Analyzer {

	private Set stops;//用于存放分词信息
	private final String[] JAVA_SYNTAX={"abstract",
			"boolean","break","byte",
			"case","catch","char","class","continue",
			"default","do","double",
			"else","extends",
			"fals","final","finally","float","for",
			"if","implements","import","int","instanceof",
			"long",
			"native","new","null",
			"private","package","public","protected",
			"return","short","static","super","switch","synchronized",
			"this","throw","throws","transient","try","true",
			"void","volatile",
			"while"};
	
    public SourceFileAnalyzer(String languageType) {
//        stops = StopAnalyzer.ENGLISH_STOP_WORDS_SET;//默认停用的语汇信息
        if("JAVA".equals(languageType)){
        	 stops = StopFilter.makeStopSet( JAVA_SYNTAX, false);
        }
    }
    public SourceFileAnalyzer() {
//      stops = StopAnalyzer.ENGLISH_STOP_WORDS_SET;//默认停用的语汇信息
     
  }
    //这里可以将通过数组产生分词对象
    public SourceFileAnalyzer(String[] sws) {
        //System.out.println(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
        stops = StopFilter.makeStopSet( sws, true);//最后的参数表示忽略大小写
//        stops.addAll(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
    }

//	@Override
//	protected TokenStreamComponents createComponents(String fieldName) {
//		final Tokenizer source = new SourceFileTokenizer();
//		return new Analyzer.TokenStreamComponents(source);
//	}

	//@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		
		final Tokenizer source = new SourceFileTokenizer();
	    
//	    TokenStream result =new NumericFilter(source);
//	    TokenStream result =new LengthFilter(source, 2,90);
//	     result =new ProgramLanguageSyntaxFilter(result, new CharArraySet(stops,false));
	     return new TokenStreamComponents(source);
//		return result;
	}

	




}
