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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * Abstract class for all objects that may have a reference
 * @author m.doering
 * @since 08-Nov-2007 13:06:47
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SingleSourcedEntityBase", propOrder = {
    "source",
    "originalNameString",
    "citation",
    "citationMicroReference"
})
@XmlRootElement(name = "SingleSourcedEntityBase")
@MappedSuperclass
@Audited
public abstract class SingleSourcedEntityBase
        extends AnnotatableEntity
        implements IReferencedEntity {

    static final long serialVersionUID = 2035568689268762760L;
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SingleSourcedEntityBase.class);

	@XmlElement(name = "Citation")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	@Deprecated
	private Reference citation;

	//Details of the reference. These are mostly (implicitly) pages but can also be tables or any other element of a
    //publication. {if the citationMicroReference exists then there must be also a reference}
    @XmlElement(name = "CitationMicroReference")
    @Deprecated
    private String citationMicroReference;

    @XmlElement(name = "OriginalNameString")
    @Deprecated
    private String originalNameString;

    //the source for this single sourced entity
    @XmlElement(name = "source")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.DELETE})
    private IdentifiableSource source;

// ************ CONSTRUCTOR ********************************************/

	//for hibernate use only
    protected SingleSourcedEntityBase() {
		super();
	}

	public SingleSourcedEntityBase(Reference citation, String citationMicroReference,
			String originalNameString) {
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

// **************** EMPTY ************************/

    @Override
    protected boolean isEmpty(){
       return super.isEmpty()
            && this.getCitation() == null
            && this.getCitationMicroReference() == null
            && isBlank(this.getOriginalNameString())
           ;
    }

//****************** CLONE ************************************************/

	@Override
	public Object clone() throws CloneNotSupportedException{
		SingleSourcedEntityBase result = (SingleSourcedEntityBase)super.clone();

		//no changes to: citation, citationMicroReference, originalNameString
		return result;
	}

//*********************************** EQUALS *********************************************************/

	/**
	 * Indicates whether some other object is "equal to" this one.
	 *
	 * Uses a content based compare strategy which avoids bean initialization. This is achieved by
	 * comparing the cdm entity ids.
	 */
	public boolean equalsByShallowCompare(SingleSourcedEntityBase other) {

	    int thisCitationId = -1;
	    int otherCitationId = -1;
	    if(this.getCitation() != null) {
	        thisCitationId = this.getCitation().getId();
	    }
	    if(other.getCitation() != null) {
	        otherCitationId = other.getCitation().getId();
        }

        if(thisCitationId != otherCitationId
                || !StringUtils.equals(this.getCitationMicroReference(), other.getCitationMicroReference())
                || !StringUtils.equals(this.getOriginalNameString(), other.getOriginalNameString())
                        ){
            return false;
        }

        return true;
    }


}
