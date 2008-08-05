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
public class CdmUnclearMapper extends CdIoXmlMapperBase {
	private static final Logger logger = Logger.getLogger(CdmUnclearMapper.class);
	
	private Namespace sourceNamespace;
	
	/**
	 * @param dbValue
	 * @param cdmValue
	 */
	public CdmUnclearMapper(String sourceElementString,Namespace sourceNamespace) {
		super(sourceElementString, null);
		this.sourceNamespace = sourceNamespace;
	}

	/**
	 * @param dbValue
	 * @param cdmValue
	 */
	public CdmUnclearMapper(String dbAttributString) {
		super(dbAttributString, null);
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
