/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.molecular;

import java.util.HashSet;

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
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;

import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.model.common.MaterialAndMethod;

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
	"method",
	"forwardPrimer",
	"reversePrimer"
})
@XmlRootElement(name = "Cloning")
@Entity
@Audited
//TODO which base class  (..., identifiable, definedTerm, ...)
public class Cloning extends EventBase implements Cloneable{
	private static final long serialVersionUID = 6179007910988646989L;
	private static final Logger logger = Logger.getLogger(Cloning.class);
	
	/** @see #getStrain() */
    @XmlElement(name = "strain")
	@Field
	@Size(max=100)
	private String strain;
	
	/** @see #getMethod()*/
	private MaterialAndMethod method;
	
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
	
// ********************* CONSTRUCTOR ********************/
	
// ********************* GETTER / SETTER ********************/
	

	/**
	 * The material and/or method used for cloning.
	 */
	public MaterialAndMethod getMethod() {
		return method;
	}

	/**
	 * @see #getMethod()
	 */
	public void setMethod(MaterialAndMethod method) {
		this.method = method;
	}
	
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
		try{
			Amplification result = (Amplification)super.clone();
			
			//don't change strain, method, forwardPrimer, backwardPrimer
			return result;
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}
