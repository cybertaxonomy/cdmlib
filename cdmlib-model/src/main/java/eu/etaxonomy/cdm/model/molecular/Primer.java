/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.molecular;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
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
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * A primer is a (short) DNA Sequence used for replication and extraction
 * of DNA parts during e.g. {@link Amplification amplification} or
 * {@link SingleRead sequence reading}.
 *
 * @author a.mueller
 * @created 2013-07-08
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Primer", propOrder = {
	"label",
	"sequence",
	"dnaMarker",
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
    @Column(length=255)
	private String label;

	/** @see #getSequence() */
	//(see #4139)
	@XmlElement(name = "Sequence")
 	private SequenceString sequence = SequenceString.NewInstance();


    /** @see #getDnaMarker()*/
    @XmlElement(name = "DnaMarker")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    private DefinedTerm dnaMarker;

	/** @see #getPublishedIn() */
	@XmlElement(name = "PublishedIn")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private Reference publishedIn;



	// ******************** FACTORY METHOD ******************/

	public static Primer NewInstance(String label){
		Primer result = new Primer();
		result.setLabel(label);
		return result;
	}

	// ********************* CONSTRUCTOR ********************/

	//made protected to fix a java.lang.InstantiationException which occurred while loading an Amplification
	//and its primer. see https://stackoverflow.com/questions/7273125/hibernate-envers-and-javassist-enhancement-failed-exception
	protected Primer(){}

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
	public SequenceString getSequence() {
		return sequence;
	}

	/**
	 * @see Primer#getSequence()
	 */
	public void setSequence(SequenceString sequence) {
		if (sequence == null){
			sequence = SequenceString.NewInstance();
		}
		this.sequence = sequence;
	}


	/**
	 * #4470
	 */
	public DefinedTerm getDnaMarker() {
		return dnaMarker;
	}

	public void setDnaMarker(DefinedTerm dnaMarker) {
		this.dnaMarker = dnaMarker;
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
	 * Clones <i>this</i> primer. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> primer by
	 * modifying only some of the attributes.<BR><BR>
	 *
	 *
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone()  {
		try{
		Primer result = (Primer)super.clone();

//		don't change label, sequence
		result.publishedIn = this.publishedIn;

		return result;
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}
