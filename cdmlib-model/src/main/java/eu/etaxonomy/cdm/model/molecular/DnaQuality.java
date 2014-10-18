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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.MaterialOrMethodEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;

/**
 * A DNA Quality describes the quality of a {@link SpecimenOrObservationType#DnaSample}
 *  
 * @author a.mueller
 * @created 18-Oct-2014
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DnaQuality", propOrder = {
    "ratioOfAbsorbance230_260",
    "ratioOfAbsorbance260_280",
    "concentration",
    "qualityTerm"
//    ,"purificationMethod"
//    ,"dateQualityCheck"
//    ,"dateNanoDrop"
    
})
@XmlRootElement(name = "DnaQuality")
@Entity
//@Indexed(index = "eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase")
@Audited
public class DnaQuality extends VersionableEntity implements Cloneable {
	private static final long serialVersionUID = -8829069331010573654L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DnaQuality.class);
	
// ****************** FACTORY METHOD *****************/
	
	/**
	 * Factory method
	 * @return
	 */
	public static DnaQuality NewInstance(){
		return new DnaQuality();
	}

// ************** ATTRIBUTES ****************************/	
	
	private Double ratioOfAbsorbance230_260;
	
	private Double ratioOfAbsorbance260_280;
	
	private Double concentration;

	@XmlElement(name = "Type")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private DefinedTerm qualityTerm;
    
    //TODO
    @XmlTransient
    @Transient
    private MaterialOrMethodEvent purificationMethod;
    
  //TODO
    @XmlTransient
    @Transient
    private DateTime dateQualityCheck;
    
  //TODO
    @XmlTransient
    @Transient
    private DateTime dateNanoDrop;
    

// ******************* CONSTRUCTOR *************************/
	
	/**
	 * Constructor
	 */
	private DnaQuality() {}
	
//************ GETTER / SETTER  **********************************/	

	public Double getRatioOfAbsorbance230_260() {
		return ratioOfAbsorbance230_260;
	}

	public void setRatioOfAbsorbance230_260(Double ratioOfAbsorbance230_260) {
		this.ratioOfAbsorbance230_260 = ratioOfAbsorbance230_260;
	}

	public Double getRatioOfAbsorbance260_280() {
		return ratioOfAbsorbance260_280;
	}

	public void setRatioOfAbsorbance260_280(Double ratioOfAbsorbance260_280) {
		this.ratioOfAbsorbance260_280 = ratioOfAbsorbance260_280;
	}
	
	
    public Double getConcentration() {
		return concentration;
	}

	public void setConcentration(Double concentration) {
		this.concentration = concentration;
	}

	public DefinedTerm getQualityTerm() {
		return qualityTerm;
	}

	public void setQualityTerm(DefinedTerm qualityTerm) {
		this.qualityTerm = qualityTerm;
	}

	

//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> dna quality. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> dna quality
	 * by modifying only some of the attributes.<BR>
	 * @throws CloneNotSupportedException 
	 * 
	 * @see Specimen#clone()
	 * @see DerivedUnit#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DnaQuality clone() {
		try {
			DnaQuality result = (DnaQuality)super.clone();
			
			//purification method ??
			
			//no changes to: rationXXX, concentration, dates, qualityTerm, 
			return result;
			
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);  //may not occur as Clonable is implemented 
		}
	}
}