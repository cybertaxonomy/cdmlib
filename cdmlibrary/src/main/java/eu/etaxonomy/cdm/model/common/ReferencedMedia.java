/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.common;


import etaxonomy.cdm.model.reference.ReferenceBase;
import etaxonomy.cdm.model.reference.StrictReferenceBase;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:13
 */
public abstract class ReferencedMedia extends Media implements IReferencedEntity {
	static Logger logger = Logger.getLogger(ReferencedMedia.class);

	@Description("")
	private String citationMicroReference;
	private ReferenceBase citation;

	public ReferenceBase getCitation(){
		return citation;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCitation(ReferenceBase newVal){
		citation = newVal;
	}

	public String getCitationMicroReference(){
		return citationMicroReference;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCitationMicroReference(String newVal){
		citationMicroReference = newVal;
	}

	@Transient
	public String getCitation(){
		return "";
	}

	@Transient
	public StrictReferenceBase getCitation(){
		return null;
	}

}