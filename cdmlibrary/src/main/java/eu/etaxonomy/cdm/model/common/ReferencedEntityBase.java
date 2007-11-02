/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import org.apache.log4j.Logger;

/**
 * abstract class for all objects that may have a reference
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:34
 */
public abstract class ReferencedEntityBase extends AnnotatableEntity implements IReferencedEntity {
	static Logger logger = Logger.getLogger(ReferencedEntityBase.class);

	//Details of the reference. These are mostly (implicitly) pages but can also be tables or any other element of a
	//publication.
	//{if the citationMicroReference exists then there must be also a reference}

	@Description("Details of the reference. These are mostly (implicitly) pages but can also be tables or any other element of a publication.
	{if the citationMicroReference exists then there must be also a reference}
	")
	private String citationMicroReference;
	@Description("")
	private String originalNameString;

	public String getCitationMicroReference(){
		return citationMicroReference;
	}

	/**
	 * 
	 * @param citationMicroReference
	 */
	public void setCitationMicroReference(String citationMicroReference){
		;
	}

	public String getOriginalNameString(){
		return originalNameString;
	}

	/**
	 * 
	 * @param originalNameString
	 */
	public void setOriginalNameString(String originalNameString){
		;
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