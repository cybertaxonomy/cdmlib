/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * abstract class for all objects that may have a reference
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:47
 */
@Entity
public abstract class ReferencedEntityBase extends AnnotatableEntity implements IReferencedEntity {
	public ReferencedEntityBase() {
		super();
		// TODO Auto-generated constructor stub
	}

	static Logger logger = Logger.getLogger(ReferencedEntityBase.class);
	//Details of the reference. These are mostly (implicitly) pages but can also be tables or any other element of a
	//publication. {if the citationMicroReference exists then there must be also a reference}
	private String citationMicroReference;
	private String originalNameString;
	private ReferenceBase citation;

	public String getCitationMicroReference(){
		return this.citationMicroReference;
	}

	/**
	 * 
	 * @param citationMicroReference    citationMicroReference
	 */
	public void setCitationMicroReference(String citationMicroReference){
		this.citationMicroReference = citationMicroReference;
	}

	public String getOriginalNameString(){
		return this.originalNameString;
	}

	/**
	 * 
	 * @param originalNameString    originalNameString
	 */
	public void setOriginalNameString(String originalNameString){
		this.originalNameString = originalNameString;
	}

	public ReferenceBase getCitation(){
		return this.citation;
	}

	public void setCitation(ReferenceBase citation) {
		this.citation = citation;
	}

}