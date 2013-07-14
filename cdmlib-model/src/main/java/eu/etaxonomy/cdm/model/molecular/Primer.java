/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.molecular;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;
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

import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * 
 * @author a.mueller
 * @created 2013-07-08
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Primer", propOrder = {
	"label",
	"sequence",
	"publishedIn"
})
@XmlRootElement(name = "Primer")
@Entity
@Audited
//TODO which base class  (..., identifiable, definedTerm, ...)
public class Primer extends AnnotatableEntity {
	private static final long serialVersionUID = 6179007910988646989L;
	private static final Logger logger = Logger.getLogger(Primer.class);
	
	/** @see #getLabel() */
	@XmlElement(name = "Label")
	@Size(max=255)
	private String label;
	
	/** @see #getSequence() */
	@XmlElement(name = "Sequence")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE})
	private Sequence sequence;
    
	/** @see #getPublishedIn() */
	@XmlElement(name = "PublishedIn")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE})
	private Reference publishedIn;

	
// ******************** FACTORY METHOD ******************/	
	
// ********************* CONSTRUCTOR ********************/
	
// ********************* GETTER / SETTER ********************/
	

	/**
	 * The name of this primer, usually given by the producers.
	 * @return the label of this primer.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @see #getLabel()
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * The DNA {@link Sequence} of this primer. A primer is usually a 
	 * small piece of DNA and therefore can be expressed as a sequence.
	 */
	public Sequence getSequence() {
		return sequence;
	}

	/**
	 * @see Primer#getSequence()
	 */
	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}
	
	/**
	 * The reference in which this primer was published and described 
	 * for the first time. It is not a reference or citation for the 
	 * sequence of this primer.<BR>
	 * Links to this reference are stored with the reference itself.
	 * @return the describing publication of this primer
	 */
	public Reference getPublishedIn() {
		return publishedIn;
	}

	/**
	 * @see #getPublishedIn()
	 */
	public void setPublishedIn(Reference publishedIn) {
		this.publishedIn = publishedIn;
	}

	// ********************* CLONE ********************/
	/** 
	 * Clones <i>this</i> sequence. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> sequencing by
	 * modifying only some of the attributes.<BR><BR>
	 * 
	 *  
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone()  {
		try{
		Sequence result = (Sequence)super.clone();

//		don't change label, sequence
		
		return result;
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}
