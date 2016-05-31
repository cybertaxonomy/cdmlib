/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * abstract class for all objects that may have a reference
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:47
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReferencedEntityBase", propOrder = {
    "citationMicroReference",
    "originalNameString",
    "citation"
})
@XmlRootElement(name = "ReferencedEntityBase")
@MappedSuperclass
@Audited
public abstract class ReferencedEntityBase extends AnnotatableEntity implements IReferencedEntity {
	private static final long serialVersionUID = -5614669050360359126L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ReferencedEntityBase.class);

	//Details of the reference. These are mostly (implicitly) pages but can also be tables or any other element of a
	//publication. {if the citationMicroReference exists then there must be also a reference}
    @XmlElement(name = "Citation")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private Reference citation;

    @XmlElement(name = "CitationMicroReference")
	private String citationMicroReference;

    @XmlElement(name = "OriginalNameString")
	private String originalNameString;

// ************ CONSTRUCTOR ********************************************/

	//for hibernate use only
    protected ReferencedEntityBase() {
		super();
	}



	public ReferencedEntityBase(Reference citation, String citationMicroReference,
			String originalNameString) {
		super();
		this.citationMicroReference = citationMicroReference;
		this.originalNameString = originalNameString;
		this.citation = citation;
	}

//********************* GETTER / SETTER *******************************/

	public String getCitationMicroReference(){
		return this.citationMicroReference;
	}
	public void setCitationMicroReference(String citationMicroReference){
		this.citationMicroReference = citationMicroReference;
	}


	public String getOriginalNameString(){
		return this.originalNameString;
	}
	public void setOriginalNameString(String originalNameString){
		this.originalNameString = originalNameString;
	}

	@Override
    public Reference getCitation(){
		return this.citation;
	}
	public void setCitation(Reference citation) {
		this.citation = citation;
	}

//****************** CLONE ************************************************/

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException{
		ReferencedEntityBase result = (ReferencedEntityBase)super.clone();

		//no changes to: citation, citationMicroReference, originalNameString
		return result;
	}

}