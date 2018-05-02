/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.tcsxml;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.io.common.mapping.IXmlMapper;
import eu.etaxonomy.cdm.io.common.mapping.CdmSingleAttributeMapperBase;


/**
 * @author a.mueller
 * @since 29.07.2008
 * @version 1.0
 */

public abstract class CdmSingleAttributeXmlMapperBase extends CdmSingleAttributeMapperBase implements IXmlMapper{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmSingleAttributeXmlMapperBase.class);
	
	protected Namespace sourceNamespace;

	
	/**
	 * @param sourceElementString
	 * @param sourceNamespace
	 * @param cdmAttributeString
	 */
	protected CdmSingleAttributeXmlMapperBase(String sourceElementString,Namespace sourceNamespace, String cdmAttributeString){
		super(sourceElementString, cdmAttributeString);
		
	}
	
	
	/**
	 * Uses the Namespace of the parent Element
	 * @param sourceElementString
	 * @param cdmAttributeString
	 */
	protected CdmSingleAttributeXmlMapperBase(String sourceElementString, String cdmAttributeString){
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
