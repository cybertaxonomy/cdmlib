/**
 * 
 */
package eu.etaxonomy.cdm.io.tcs;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.io.common.CdmIoMapperBase;

/**
 * @author a.mueller
 *
 */
public abstract class CdmIoXmlMapperBase extends CdmIoMapperBase {
	private static final Logger logger = Logger.getLogger(CdmIoXmlMapperBase.class);
	
	protected Namespace sourceNamespace;

	
	/**
	 * @param sourceElementString
	 * @param sourceNamespace
	 * @param cdmAttributeString
	 */
	protected CdmIoXmlMapperBase(String sourceElementString,Namespace sourceNamespace, String cdmAttributeString){
		super(sourceElementString, cdmAttributeString);
		
	}
	
	
	/**
	 * Uses the Namespace of the parent Element
	 * @param sourceElementString
	 * @param cdmAttributeString
	 */
	protected CdmIoXmlMapperBase(String sourceElementString, String cdmAttributeString){
		super(sourceElementString, cdmAttributeString);
		
	}

	public String getSourceElement(){
		return super.getSourceAttribute();
	}

	public String getDestinationAttribute(){
		return super.getDestinationAttribute();
	}
	
	
	public Namespace getSourceNamespace(){
		return sourceNamespace;
	}
	
	/**
	 * Returns the namespace. If namespace is null, return parentElement namespace
	 * @param parentElement
	 * @return
	 */
	public Namespace getSourceNamespace(Element parentElement){
		if (this.sourceNamespace != null){
			return sourceNamespace;
		}else if (parentElement != null){
			return parentElement.getNamespace();
		}else{
			return null;
		}
	}
	
	public boolean mapsSource(Content content, Element parentElement){
		if (! (content instanceof Element)){
			return false;
		}
		Element element = (Element)content;
		if (content == null){
			return false;
		}else if (! element.getName().equals(getSourceElement())){
			return false;
		}
		Namespace thisNamespace = getSourceNamespace(parentElement);
		if (thisNamespace == null){
			if (element.getNamespace() == null){
				return true;
			}else{
				return false;
			}
		}
		if (! thisNamespace.equals(element.getNamespace())){
			return false;
		}
		return true;
	}

	public String toString(){
		//TODO
		return this.getSourceElement();
	}	
}
