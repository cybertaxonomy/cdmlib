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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.MaterialOrMethodEvent;

/**
 * Instances of this the {@link SingleRead} class describe the process and the result of a single
 * sequence generation (read). It has as an input the PCR result ({@link AmplificationResult}). A primer
 * is used for expressing the DNA in either {@link SequenceDirection#Forward forward} or
 * {@link SequenceDirection#Reverse reverse} direction.
 * The result of the process is a {@link #getPherogram() pherogram} which by interpretation results
 * in the most probable {@link #getSequence() sequence}.
 * The event dates like the sequencing date and the sequencing agent(person) are inherited by {@link EventBase}.
 *
 * @see AmplificationResult
 * @see SequenceString
 * @see Sequence
 *
 * @author a.mueller
 * @since 2013-07-05
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SingleRead", propOrder = {
	"amplificationResult",
	"sequence",
	"primer",
	"direction",
	"pherogram",
	"materialOrMethod"
})
@XmlRootElement(name = "SingleRead")
@Entity
@Audited
public class SingleRead extends EventBase implements Cloneable{
	private static final long serialVersionUID = 1735535003073536132L;
	private static final Logger logger = Logger.getLogger(SingleRead.class);

	/** @see #getAmplificationResult()  */
	@XmlElement(name = "AmplificationResult")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private AmplificationResult amplificationResult;

	/** @see #getPrimer()*/
	@XmlElement(name = "Primer")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private Primer primer;

	/** @see #getSequence()*/
	/**{@link #getSequence()}*/
	@XmlElement(name = "Sequence")
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private SequenceString sequence = SequenceString.NewInstance();

	@XmlElement(name = "Pherogram")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private Media pherogram;

	/** @see #getDirection()*/
	@XmlAttribute(name ="direction")
	//TODO length = 3
	@Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
    	parameters = {@org.hibernate.annotations.Parameter(name  = "enumClass", value = "eu.etaxonomy.cdm.model.molecular.SequenceDirection")}
	)
	private SequenceDirection direction;

	@XmlElement(name = "MaterialOrMethod")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private MaterialOrMethodEvent materialOrMethod;


	// ******************** FACTORY METHOD ******************/

	public static SingleRead NewInstance(){
		return new SingleRead();
	}

	// ********************* CONSTRUCTOR ********************/

	//protected for Javassist, otherwise private
	protected SingleRead(){};

	// ********************* GETTER / SETTER ********************/


	/**
	 * Returns the {@link AmplificationResult amplification product} that was the input for this
	 * {@link SingleRead single sequence}.
	 */
	public AmplificationResult getAmplificationResult() {
		return amplificationResult;
	}

	/**
	 * TODO this method is protected as long as bidirectionality is not clear.
	 * @see #getAmplificationResult()
	 */
	protected void setAmplificationResult(AmplificationResult amplificationResult) {
		this.amplificationResult = amplificationResult;
	}

	/**
	 * The {@link Primer primer} used for processing this single sequence.
	 * Often this primer already has been used in the according {@link Amplification amplification}.
	 * However, there are exceptions from this rule.
	 */
	public Primer getPrimer() {
		return primer;
	}

	/**
	 * @see #getPrimer()
	 */
	public void setPrimer(Primer primer) {
		this.primer = primer;
	}

	/**
	 * The {@link SequenceString sequence string} of this single sequence process (e.g. AGTGGTAGGATG)
	 */
	public SequenceString getSequence() {
		return sequence;
	}

	/**
	 * @see #getSequence()
	 */
	public void setSequence(SequenceString sequence) {
		if (sequence == null){
			SequenceString.NewInstance();
		}
		this.sequence = sequence;
	}

	/**
	 * The {@link SequenceDirection direction} in which this single sequence has been created.
	 * Usually an {@link Amplification amplification} leads to 2 single sequences per {@link DnaSample},
	 * a {@link SequenceDirection#Forward forward} and a {@link SequenceDirection#Reverse reverse} one.
	 * These 2 result then in a {@link Sequence consensus sequence}.
	 * But there are exceptions from this rule.
	 */
	public SequenceDirection getDirection() {
		return direction;
	}

	/**
	 * @see #getDirection()
	 */
	public void setDirection(SequenceDirection direction) {
		this.direction = direction;
	}

	/**
	 * The pherogram (chromatogram) which visualizes the result of this single sequence.
	 */
	public Media getPherogram() {
		return pherogram;
	}

	/**
	 * @see #getPherogram()
	 */
	public void setPherogram(Media pherogram) {
		this.pherogram = pherogram;
	}


	/**
	 * The material and/or method used for this sequencing.
	 */
	public MaterialOrMethodEvent getMaterialOrMethod() {
		return materialOrMethod;
	}

	/**
	 * @see #getMaterialOrMethod()
	 */
	public void setMaterialOrMethod(MaterialOrMethodEvent materialOrMethod) {
		this.materialOrMethod = materialOrMethod;
	}



//*************************** Transient GETTER /SETTER *****************************/
	/**
	 * Delegate method to get the text representation of the {@link #getSequence() sequence}.
	 * @see #setSequenceString(String)
	 */
	@Transient
	public String getSequenceString() {
		return sequence.getString();
	}

	/**
	 * Delegate method to set the text representation of the {@link #getSequence() sequence}.
	 */
	@Transient
	public void setSequenceString(String sequence) {
		this.sequence.setString(sequence);
	}

	/**
	 * Transient convenience method which wrapps {@link EventBase#getActor()}.
	 * @return the {@link TimePeriod date/period} when this sequence was created.
	 */
	@Transient
	public TimePeriod getDateSequenced(){
		return ((EventBase)this).getTimeperiod();
	}

	/**
	 * @see #getDateSequenced()
	 */
	public void setDateSequenced(TimePeriod dateSequenced){
		this.setTimeperiod(dateSequenced);
	}

	/**
	 * Transient convenience method which wrapps {@link EventBase#getActor()}.
	 * @return the {@link AgentBase agent} who sequenced this single sequence.
	 */
	@Transient
	public AgentBase getSequencedBy(){
		return ((EventBase)this).getActor();
	}

	/**
	 * @see #getSequencedBy()
	 */
	public void setSequencedBy(AgentBase sequencedBy){
		this.setActor(sequencedBy);
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
		SingleRead result = (SingleRead)super.clone();

		//sequences
		result.sequence = (SequenceString)this.sequence.clone();


		//Don't change amplification, pherogram, primer, sequence, direction
		return result;

		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}

}
