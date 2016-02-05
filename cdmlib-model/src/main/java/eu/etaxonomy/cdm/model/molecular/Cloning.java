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
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.model.occurrence.MaterialOrMethodEvent;

/**
 * Cloning is a method used in {@link Amplification DNA amplification} for multiplying the base
 * material. Various cloning methods exist. Classical cloning methods use bacteria for cloning
 * while the latest approaches use other techniques.
 *
 * @author a.mueller
 * @created 2013-07-11
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Cloning", propOrder = {
	"strain",
	"forwardPrimer",
	"reversePrimer"
})
@XmlRootElement(name = "Cloning")
@Entity
@Audited
public class Cloning extends MaterialOrMethodEvent implements Cloneable{
	private static final long serialVersionUID = 6179007910988646989L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Cloning.class);

	/** @see #getStrain() */
    @XmlElement(name = "strain")
	@Field
    @Column(length=100)
	private String strain;

    /** @see #getForwardPrimer() */
    @XmlElement(name = "ForwardPrimer")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    private Primer forwardPrimer;

    /** @see #getReversePrimer()*/
    @XmlElement(name = "ReversePrimer")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    private Primer reversePrimer;


// ******************** FACTORY METHOD ******************/

    public static Cloning NewInstance(){
    	return new Cloning();
    }

    public static Cloning NewInstance(DefinedTerm definedMaterialOrMethod, String methodText, String strain, Primer forwardPrimer, Primer reversePrimer){
    	return new Cloning(definedMaterialOrMethod, methodText, strain, forwardPrimer, reversePrimer);
    }

// ********************* CONSTRUCTOR ********************/


    //made protected to fix a java.lang.InstantiationException which occurred while loading
    //see https://stackoverflow.com/questions/7273125/hibernate-envers-and-javassist-enhancement-failed-exception
    protected Cloning(){};
    protected Cloning(DefinedTerm definedMaterialOrMethod, String methodText, String strain, Primer forwardPrimer, Primer reversePrimer){
    	super(definedMaterialOrMethod, methodText);
    	this.strain = strain;
    	this.forwardPrimer = forwardPrimer;
    	this.reversePrimer = reversePrimer;
    }

// ********************* GETTER / SETTER ********************/


	/**
	 * The primer used for forward cloning.
	 * @see #getReversePrimer()
	 */
	public Primer getForwardPrimer() {
		return forwardPrimer;
	}

	/**
	 * @see #getForwardPrimer()
	 * @see #getReversePrimer()
	 */
	public void setForwardPrimer(Primer forwardPrimer) {
		this.forwardPrimer = forwardPrimer;
	}

	/**
	 * The primer used for reverse cloning.
	 * @see #getForwardPrimer()
	 */
	public Primer getReversePrimer() {
		return reversePrimer;
	}

	/**
	 * @see #getReversePrimer()
	 * @see #getForwardPrimer()
	 */
	public void setReversePrimer(Primer reversePrimer) {
		this.reversePrimer = reversePrimer;
	}

	public String getStrain() {
		return strain;
	}

	public void setStrain(String strain) {
		this.strain = strain;
	}

// ********************* CLONE ********************/
	/**
	 * Clones <i>this</i> {@link Cloning}. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> cloning by
	 * modifying only some of the attributes.<BR><BR>
	 *
	 *
	 * @see EventBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone()  {
		Cloning result = (Cloning)super.clone();

		//don't change strain, forwardPrimer, backwardPrimer
		return result;
	}
}
