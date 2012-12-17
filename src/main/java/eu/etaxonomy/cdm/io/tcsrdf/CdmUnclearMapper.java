/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsrdf;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;


/**
 * @author a.mueller
 * @created 29.07.2008
 * @version 1.0
 */
public class CdmUnclearMapper extends CdmSingleAttributeXmlMapperBase {
	private static final Logger logger = Logger.getLogger(CdmUnclearMapper.class);
	
	
	/**
	 * @param dbValue
	 * @param cdmValue
	 */
	public CdmUnclearMapper(String sourceElementString, Namespace sourceNamespace) {
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
	
	public String toString(){
		return this.getSourceElement();
	}


}
