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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;

/**
 * The physical process of amplification (also called PCR) extracts and replicates parts of the DNA of
 * a given {@link #getDnaSample() DNA Sample} . The part of the DNA being replicated is defined by the
 * {@link Amplification#getDnaMarker() marker} (also called locus) - implemented in CDM as a {@link DefinedTerm}
 * of term type {@link TermType#DnaMarker}.
 *
 * <BR>
 * To execute the replication {@link Primer primers} (short DNA fractions) are
 * used. They may work in both directions of the DNA part therefore we do have a
 * {@link #getForwardPrimer() forward primer} and a {@link #getReversePrimer() reverse primer}.
 * Most (or all?) amplifications require a {@link #getPurification() purification process}. Additionally
 * some use {@link #getCloning()} for replication.
 *
 * <H3>Quality control</H3>
 * <BR>
 * For quality control the resulting product (PCR) is tested using a chromatographic method called
 * electrophoresis. The parameters (voltage, ladder used, running time, and gel concentration) used
 * for this electrophoresis as well as the resulting
 * {@link #getGelPhoto() photo} are also relevant for an amplification.
 *
 * We have 2 classes to store the core data for an amplification: {@link Amplification} and {@link AmplificationResult}.
 * <BR>
 * In {@link Amplification} we store all data that is equal for an amplification event which includes amplification
 * of many {@link DnaSample dna samples}. Those data which are relevant only for a specific dna sample are
 * stored in {@link AmplificationResult}. Theoretically this includes data on the resulting PCR. However, as the
 * PCR itself is not persistent we do not store further information on it in the CDM and do not handle
 * {@link AmplificationResult} as a {@link DerivedUnit}.
 * <BR>
 * This may change in future: http://dev.e-taxonomy.eu/trac/ticket/3717.
 * <BR>
 *
 * @author a.mueller
 * @since 2013-07-05
 *
 * @see Amplification
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AmplificationResult", propOrder = {
	"dnaSample",
	"amplification",
	"cloning",
	"successful",
	"successText",
	"gelPhoto",
	"singleReads"
})
@XmlRootElement(name = "AmplificationResult")
@Entity
@Audited
public class AmplificationResult extends AnnotatableEntity implements Cloneable{
	private static final long serialVersionUID = -8614860617229484621L;
	private static final Logger logger = Logger.getLogger(AmplificationResult.class);


    /** @see #getDnaSample() */
	@XmlElement( name = "DnaSample")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @IndexedEmbedded
    private DnaSample dnaSample;

   /** @see #getAmplification() */
	@XmlElement( name = "Amplification")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	@IndexedEmbedded
    private Amplification amplification;

    @XmlElementWrapper(name = "SingleReads")
    @XmlElement(name = "SingleRead")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="amplificationResult" , fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private Set<SingleRead> singleReads = new HashSet<>();

    @XmlElement(name = "Cloning")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private Cloning cloning;

	/** @see #getSuccessful() */
    @XmlAttribute(name = "successful")
    private Boolean successful;

    /** @see #getSuccessText() */
    @XmlElement(name = "successText")
	@Field
    @Column(length=255)
	private String successText;
//
//    /** @see #getGelRunningTime() */
//	@XmlElement(name = "gelRunningTime")
//	@Field(analyze = Analyze.NO)
//	@NumericField
//	private Double gelRunningTime;

    @XmlElement(name = "GelPhoto")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private Media gelPhoto;


// ********************* FACTORY METHODS ************************/

	public static AmplificationResult NewInstance(DnaSample dnaSample){
		AmplificationResult result = new AmplificationResult();
		dnaSample.addAmplificationResult(result);
		return result;
	}

	public static AmplificationResult NewInstance(SingleRead singleRead){
	    AmplificationResult result = new AmplificationResult();
	    result.addSingleRead(singleRead);
	    return result;
	}

	public static AmplificationResult NewInstance(DnaSample dnaSample,
			Amplification amplification) {
		AmplificationResult result = new AmplificationResult();
		dnaSample.addAmplificationResult(result);
		result.setAmplification(amplification);
		return result;
	}

	public static AmplificationResult NewInstance(){
	    return new AmplificationResult();
	}


// ******************* CONSTRUCTOR *******************************/

	protected AmplificationResult(){}


//********************* GETTER / SETTER ************/


	/**
	 * The {@link DnaSample dna sample} which is the input for this {@link AmplificationResult amplification}.
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
	 * The {@link Amplification amplification event} this amplification result resulted from.
	 * @see #setAmplification(Amplification)
	 * @see Amplification
	 * @return the amplification event
	 */
	public Amplification getAmplification() {
		return amplification;
	}
	/**
	 * {@link #getAmplification()}
	 * @param amplification
	 */
	public void setAmplification(Amplification amplification) {
		this.amplification = amplification;
	}

	/**
	 * The {@link SingleRead single sequences} created by using this amplification's result (PCR).
	 */
	public Set<SingleRead> getSingleReads() {
		return singleReads;
	}

	public void addSingleRead(SingleRead singleRead){
		if (singleRead.getAmplificationResult() != null){
			singleRead.getAmplificationResult().singleReads.remove(singleRead);
		}
		this.singleReads.add(singleRead);
		singleRead.setAmplificationResult(this);
	}

	public void removeSingleRead(SingleRead singleRead){
	    if(this.singleReads.contains(singleRead)){
	        this.singleReads.remove(singleRead);
	        singleRead.setAmplificationResult(null);
	    }
	}

	/**
	 * @see #getSingleReads()
	 */
	//TODO private until it is clear how bidirectionality is handled
	@SuppressWarnings("unused")
	private void setSingleReads(Set<SingleRead> singleReads) {
		this.singleReads = singleReads;
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
//
//	/**
//	 * The time for running the electrophoresis quality check.
//	 * Base unit is minutes [min].
//	 */
//	public Double getGelRunningTime() {
//		return gelRunningTime;
//	}
//
//	/**
//	 * @see #getGelRunningTime()
//	 */
//	public void setGelRunningTime(Double gelRunningTime) {
//		this.gelRunningTime = gelRunningTime;
//	}


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
			AmplificationResult result = (AmplificationResult)super.clone();

			result.singleReads = new HashSet<SingleRead>();
			for (SingleRead seq: this.singleReads){
				result.singleReads.add(seq);

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
