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
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
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
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.NumericField;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.media.Media;
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
	"dnaSample",
	"dnaMarker",
	"forwardPrimer",
	"reversePrimer",
	"purification",
	"cloning",
	"successful",
	"successText",
	"ladderUsed",
	"electrophoresisVoltage",
	"gelRunningTime",
	"gelConcentration",
	"gelPhoto",
	"singleReads"
})
@XmlRootElement(name = "Amplification")
@Entity
@Audited
public class Amplification extends EventBase implements Cloneable{
	private static final long serialVersionUID = -8614860617229484621L;
	private static final Logger logger = Logger.getLogger(Amplification.class);
	
	
    /** @see #getDnaSample() */
	@XmlElement( name = "DnaSample")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @IndexedEmbedded
    private DnaSample dnaSample;
	
    @XmlElementWrapper(name = "SingleReads")
    @XmlElement(name = "SingleRead")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE})
	private Set<SingleRead> singleReads = new HashSet<SingleRead>();
	
    /** @see #getDnaMarker()*/
    @XmlElement(name = "DnaMarker")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
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
    
    @XmlElement(name = "Cloning")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE})
    private Cloning cloning;
    
	
    /** @see #getSuccessful() */
    @XmlAttribute(name = "successful")
    private Boolean successful;
	
    /** @see #getSuccessText() */
    @XmlElement(name = "successText")
	@Field
	@Size(max=255)
	private String successText;
	
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

    @XmlElement(name = "GelPhoto")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE})
	private Media gelPhoto;

	
// ********************* FACTORY METHODS ************************/	
	
	public static Amplification NewInstance(DnaSample dnaSample){
		Amplification result = new Amplification();
		dnaSample.addAmplification(result);
		return result;
	}
	
	
// ******************* CONSTRUCTOR *******************************/	
	
	private Amplification(){}

	
//********************* GETTER / SETTER ************/	
	

	/**
	 * The {@link DnaSample dna sample} which is the input for this {@link Amplification amplification}. 
	 */
	public DnaSample getDnaSample() {
		return dnaSample;
	}

	/**
	 * For use by DnaSample.addAmplification(ampl.) only. For now.
	 * @see #getDnaSample()
	 */
	protected void setDnaSample(DnaSample dnaSample) {
		this.dnaSample = dnaSample;
	}
	
	/**
	 * The {@link SingleRead single sequences} created by using this amplification's result (PCR).
	 */
	public Set<SingleRead> getSingleReads() {
		return singleReads;
	}

	public void addSingleRead(SingleRead singleRead){
		if (singleRead.getAmplification() != null){
			singleRead.getAmplification().singleReads.remove(singleRead);
		}
		this.singleReads.add(singleRead);
		singleRead.setAmplification(this);
	}
	
	/**
	 * @see #getSingleReads()
	 */
	//TODO private until it is clear how bidirectionality is handled
	private void setSingleReads(Set<SingleRead> singleReads) {
		this.singleReads = singleReads;
	}


	/**
	 * The {@link TermType#DnaMarker DNA marker} used for this amplification.
	 * The DNA marker also defines the part (locality) of the DNA/RNA examined.
	 * It may also be called <i>locus</i> 
	 */
	public DefinedTerm getDnaMarker() {
		return dnaMarker;
	}

	/**
	 * @see #getDnaMarker()
	 */
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
	 * Information if this amplification was successful or not. Success may be defined
	 * by the results of the electrophoresis.
	 * 
	 * @see #getSuccessText()
	 */
	public Boolean getSuccessful() {
		return successful;
	}

	/**
	 * @see #getSuccessful()
	 * @see #getSuccessText()
	 */
	public void setSuccessful(Boolean successful) {
		this.successful = successful;
	}

	/**
	 * Freetext about the success of this amplification explaining
	 * in detail why it is concidered to be successful/unsucessful
	 * 
	 * @see #getSuccessful()
	 */
	public String getSuccessText() {
		return successText;
	}

	/**
	 * @see #getSuccessText()
	 * @see #getSuccessful()
	 */
	public void setSuccessText(String successText) {
		this.successText = successText;
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
	 * The {@link Cloning cloning process} involved in this amplification.
	 */
	public Cloning getCloning() {
		return cloning;
	}

	/**
	 * @see #getCloning()
	 */
	public void setCloning(Cloning cloning) {
		this.cloning = cloning;
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
	 * Base unit is 
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

	/**
	 * The photo taken from the electrophoresis result showing the quality of the amplification.
	 * Gelphotos often do show multiple electrophoresis results. One may either cut or mark 
	 * the part of the photo that displays <code>this</code> amplification. However, this may make
	 * the concrete media file unusable for other amplifications also represented by the same image.
	 * @see #getElectrophoresisVoltage()
	 * @see #getLadderUsed()
	 * @see #getGelConcentration()
	 * @see #getGelRunningTime()
	 */
	public Media getGelPhoto() {
		return gelPhoto;
	}


	/**
	 * @param gelPhoto the gelPhoto to set
	 */
	public void setGelPhoto(Media gelPhoto) {
		this.gelPhoto = gelPhoto;
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
			
			result.singleReads = new HashSet<SingleRead>();
			for (SingleRead seq: this.singleReads){
				result.singleReads.add((SingleRead) seq);
			
			}
			
			//don't change dnaSample, marker, successful, successText, forwardPrimer,
			//reversePrimer, purifiaction, cloning, ladderUsed, electrophoresisVoltage,
			//gelRunningTime, gelPhoto, gelConcentration
			return result;
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
	
	
}
