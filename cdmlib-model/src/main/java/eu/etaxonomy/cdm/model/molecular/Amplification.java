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
import javax.persistence.JoinColumn;
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
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.NumericField;

import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.occurrence.MaterialOrMethodEvent;

/**
 * The physical process of amplification (also called PCR) extracts and replicates parts of the DNA of
 * a given {@link #getDnaSample() DNA Sample} . The part of the DNA being replicated is defined by the
 * {@link #getDnaMarker() marker} (also called locus) - implemented in CDM as a {@link DefinedTerm}
 * of term type {@link TermType#DnaMarker}.
 * <BR>To execute the replication {@link Primer primers} (short DNA fractions) are
 * used. They may work in both directions of the DNA part therefore we do have a
 * {@link #getForwardPrimer() forward primer} and a {@link #getReversePrimer() reverse primer}.
 * Most (or all?) amplifications require a {@link #getPurification() purification process}. Additionally
 * some use {@link #getCloning()} for replication.
 * <H3>Quality control</H3>
 * <BR>For quality control the resulting product (PCR) is tested using a chromatographic method called
 * electrophoresis. The parameters (voltage, ladder used, running time, and gel concentration) used
 * for this electrophoresis as well as the resulting
 * {@link #getGelPhoto() photo} can also be stored in the amplification instance.
 * <BR>The resulting PCR will later be used in a {@link SingleRead DNA sequence reading process}.
 * The PCR itself is not persistent and therefore will not be stored in the CDM.
 * This may change in future: http://dev.e-taxonomy.eu/trac/ticket/3717.
 * <BR>
 *
 * @author a.mueller
 * @created 2013-07-05
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Amplification", propOrder = {
	"dnaMarker",
	"forwardPrimer",
	"reversePrimer",
	"purification",
	"institution",
	"ladderUsed",
	"electrophoresisVoltage",
	"gelRunningTime",
	"gelConcentration",
})
@XmlRootElement(name = "Amplification")
@Entity
@Audited
public class Amplification extends EventBase implements Cloneable{
	private static final long serialVersionUID = -6382383300974316261L;

	private static final Logger logger = Logger.getLogger(Amplification.class);

    /** @see #getDnaMarker()*/
    @XmlElement(name = "DnaMarker")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    //TODO why is this eager?
    @ManyToOne(fetch=FetchType.EAGER)
    private DefinedTerm dnaMarker;

    /** @see #getForwardPrimer() */
    @XmlElement(name = "ForwardPrimer")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE})
    private Primer forwardPrimer;

    /** @see #getReversePrimer()*/
    @XmlElement(name = "ReversePrimer")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE})
    private Primer reversePrimer;


    @XmlElement(name = "Purification")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE})
    private MaterialOrMethodEvent purification;
  
	@XmlElement(name = "Institution")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name="institution_id")
	private Institution institution;

    /** @see #getLadderUsed() */
    @XmlElement(name = "ladderUsed")
	@Field
	@Size(max=255)
	private String ladderUsed;

    /** @see #getElectrophoresisVoltage()*/
	@XmlElement(name = "electrophoresisVoltage")
	@Field(analyze = Analyze.NO)
	@NumericField
	private Double electrophoresisVoltage;

    /** @see #getGelRunningTime() */
	@XmlElement(name = "gelRunningTime")
	@Field(analyze = Analyze.NO)
	@NumericField
	private Double gelRunningTime;

    /** @see #getGelConcentration() */
	@XmlElement(name = "gelConcentration")
	@Field(analyze = Analyze.NO)
	@NumericField
	private Double gelConcentration;


// ********************* FACTORY METHODS ************************/

	public static Amplification NewInstance(){
	    return new Amplification();
	}


// ******************* CONSTRUCTOR *******************************/

	protected Amplification(){}


//********************* GETTER / SETTER ************/


	/**
	 * The {@link TermType#DnaMarker DNA marker} used for this amplification.
	 * The DNA marker also defines the part (locality) of the DNA/RNA examined.
	 * It may also be called <i>locus</i>
	 */
	public DefinedTerm getDnaMarker() {
		return dnaMarker;
	}
	/** @see #getDnaMarker()*/
	public void setDnaMarker(DefinedTerm marker) {
		this.dnaMarker = marker;
	}

	/**
	 * The primer used for forward amplification.
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
	 * The primer used for reverse amplification.
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


	/**
	 * The material and/or method used for purification.
	 */
	public MaterialOrMethodEvent getPurification() {
		return purification;
	}
	/**
	 * @see #getPurification()
	 */
	public void setPurification(MaterialOrMethodEvent purification) {
		this.purification = purification;
	}
	
    /**
     * The institution in which the amplification event took place.
     * Usually the {@link Amplification#getActor()} should be a person
     * or team that works for this institution at the given time
     * @return the institution
     */
//	#4498
    public Institution getInstitution() {
		return institution;
	}
	/**
	 * @see #getInstitution()
	 */
	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	/**
	 * The voltage used for running the electrophoresis quality check.
	 * Base unit is voltage [V].
	 * @see #getGelRunningTime()
	 * @see #getGelPhoto()
	 * @see #getLadderUsed()
	 * @see #getGelConcentration()

	 */
	public Double getElectrophoresisVoltage() {
		return electrophoresisVoltage;
	}
	/**
	 * @see #getElectrophoresisVoltage()
	 */
	public void setElectrophoresisVoltage(Double electrophoresisVoltage) {
		this.electrophoresisVoltage = electrophoresisVoltage;
	}

	/**
	 * The time for running the electrophoresis quality check.
	 * Base unit is minutes [min].
	 */
	public Double getGelRunningTime() {
		return gelRunningTime;
	}
	/**
	 * @see #getGelRunningTime()
	 */
	public void setGelRunningTime(Double gelRunningTime) {
		this.gelRunningTime = gelRunningTime;
	}


	/**
	 * The gel concentration used for the electrophoresis.
	 * Base unit is [%]
	 * @see #getElectrophoresisVoltage()
	 * @see #getGelRunningTime()
	 * @see #getGelPhoto()
	 * @see #getLadderUsed()
	 */
	public Double getGelConcentration() {
		return gelConcentration;
	}
	/**
	 * @see #getGelConcentration()
	 */
	public void setGelConcentration(Double gelConcentration) {
		this.gelConcentration = gelConcentration;
	}

	/**
	 * Material and method used for testing quality of this amplification.
	 * @see #getElectrophoresisVoltage()
	 * @see #getGelPhoto()
	 * @see #getGelConcentration()
	 * @see #getGelRunningTime()
	 */
	public String getLadderUsed() {
		return ladderUsed;
	}
	/**
	 * @see #getLadderUsed()
	 */
	public void setLadderUsed(String ladderUsed) {
		this.ladderUsed = ladderUsed;
	}


	// ********************** CLONE ***********************************/
	/**
	 * Clones <i>this</i> amplification. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> amplification by
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

			//don't change marker, forwardPrimer, reversePrimer, 
			//purifiaction, ladderUsed, electrophoresisVoltage,
			//gelRunningTime, gelConcentration
			return result;
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}


}
