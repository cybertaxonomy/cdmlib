/**
 * 
 */
package eu.etaxonomy.cdm.io.tcs;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * @author a.mueller
 *
 */
public class CdmTextElementMapper extends CdmIoXmlMapperBase {
	private static final Logger logger = Logger.getLogger(CdmTextElementMapper.class);
	
	/**
	 * @param dbValue
	 * @param cdmValue
	 */
	public CdmTextElementMapper(String sourceElementString,Namespace sourceNamespace, String cdmAttributeString) {
		super(sourceElementString, cdmAttributeString);
		this.sourceNamespace = sourceNamespace;
	}

	/**
	 * @param dbValue
	 * @param cdmValue
	 */
	public CdmTextElementMapper(String dbAttributString, String cdmAttributeString) {
		super(dbAttributString, cdmAttributeString);
	}
	
	public Namespace getSourceNamespace(){
		return sourceNamespace;
	}
		
	public Class getTypeClass(){
		return String.class;
	}
	
	public boolean mapsSource(Content content, Element parentElement){
		return super.mapsSource(content, parentElement);	
	}
}
