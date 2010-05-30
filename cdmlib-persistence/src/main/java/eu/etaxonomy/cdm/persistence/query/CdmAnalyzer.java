package eu.etaxonomy.cdm.persistence.query;

import java.io.Reader;

import org.apache.lucene.analysis.ISOLatin1AccentFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class CdmAnalyzer extends StandardAnalyzer {

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		TokenStream result = super.tokenStream(fieldName, reader);
		result = new ISOLatin1AccentFilter(result); 
        return result; 
	}

}
