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
import javax.persistence.Enumerated;
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
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.model.common.MaterialAndMethod;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.media.Media;

/**
 * Instances of this the {@link SingleRead} class describe the process and the result of a single
 * sequence generation (read). It has as an input the PCR result ({@link Amplification}). A primer 
 * is used for expressing the DNA in either {@link SequenceDirection#Forward forward} or 
 * {@link SequenceDirection#Reverse reverse} direction.
 * The result of the process is a {@link #getPherogram() pherogram} which by interpretation results
 * in the most probable {@link #getSequence() sequence}.
 * The event dates like the sequencing date and the sequencing agent(person) are inherited by {@link EventBase}.
 * 
 * @see Amplification
 * @see SequenceString
 * @see Sequence
 * 
 * @author a.mueller
 * @created 2013-07-05
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SingleRead", propOrder = {
	"amplification",
	"sequence",
	"primer",
	"direction",
	"pherogram",
	"materialAndMethod"
})
@XmlRootElement(name = "Primer")
@Entity
@Audited
public class SingleRead extends EventBase implements Cloneable{
	private static final long serialVersionUID = 1735535003073536132L;
	private static final Logger logger = Logger.getLogger(SingleRead.class);
	
	/** @see #getAmplification()  */
	@XmlElement(name = "Amplification")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private Amplification amplification;
	
	/** @see #getPrimer()*/
	@XmlElement(name = "Primer")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private Primer primer;
	
	/** @see #getSequence()*/
	/**{@link #getSequence()}*/
	@XmlElement(name = "Sequence")
    private SequenceString sequence = SequenceString.NewInstance();
	
	@XmlElement(name = "Pherogram")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private Media pherogram;
	
	/** @see #getDirection()*/
	//TODO
	@XmlAttribute(name ="direction")
	@Enumerated
	private SequenceDirection direction;
	
	@XmlElement(name = "MaterialAndMethod")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private MaterialAndMethod materialAndMethod;
	
	
	// ******************** FACTORY METHOD ******************/	
	
	public static SingleRead NewInstance(){
		return new SingleRead();
	}
	
	// ********************* CONSTRUCTOR ********************/
		
	private SingleRead(){};
	
	// ********************* GETTER / SETTER ********************/
	

	/**
	 * Returns the {@link Amplification amplification} that was the input for this 
	 * {@link SingleRead single sequence}.
	 */
	public Amplification getAmplification() {
		return amplification;
	}

	/**
	 * TODO this method is protected as long as bidirectionality is not clear.
	 * @see #getAmplification()
	 */
	protected void setAmplification(Amplification amplification) {
		this.amplification = amplification;
	}

	/**
	 * The {@link Primer primer} used for processing this single sequence.
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
	 * The direction in which this single sequence has been created.
	 * Usually an amplification and a sequencing has a forward single sequence and/or
	 * a reverse single sequence.
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
	public MaterialAndMethod getMaterialAndMethod() {
		return materialAndMethod;
	}

	/**
	 * @see #getMaterialAndMethod()
	 */
	public void setMaterialAndMethod(MaterialAndMethod materialAndMethod) {
		this.materialAndMethod = materialAndMethod;
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
