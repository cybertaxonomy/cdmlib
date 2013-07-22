/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.molecular;


import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
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
import org.codehaus.plexus.util.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * Alignment of multiple single sequences to a consensus sequence, 
 * may also include the extracted barcode sequence.
 * 
 * This class holds information about both the combining process of 
 * {@link SingleRead single sequences} to one consensus sequence
 * (singleReads, contigFile) as well as sequence related information.
 * The later includes the sequence string itself, important genetic information
 * (marker, haplotype) as well as registration information (genetic accession number)
 * citations and barcoding information.
 * 
 * @author m.doering
 * @created 08-Nov-2007 13:06:51
 * @author a.mueller
 * @updated 11-Jul-2013
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Sequence", propOrder = {
    "dnaSample",
	"consensusSequence",
	"isBarcode",
    "barcodeSequencePart",
    "dnaMarker",
    "geneticAccessionNumber",
    "boldProcessId",
    "haplotype",
    "contigFile",
    "singleReads",
    "citations"
})
@XmlRootElement(name = "Sequencing")
@Entity
@Audited
@Configurable
//@Table(appliesTo="Sequence", indexes = { @Index(name = "sequenceTitleCacheIndex", columnNames = { "titleCache" }) })
public class Sequence extends AnnotatableEntity implements Cloneable{
	private static final long serialVersionUID = 8298983152731241775L;
	private static final Logger logger = Logger.getLogger(Sequence.class);
	
	private static final String GENBANK_BASE_URI = "http://www.ncbi.nlm.nih.gov/nuccore/%s";
	private static final String EMBL_BASE_URI = "http://www.ebi.ac.uk/ena/data/view/%s";
	private static final String DDBJ_BASE_URI = "http://getentry.ddbj.nig.ac.jp/getentry/na/%s/?filetype=html";
	private static final String BOLD_BASE_URI = "http://www.boldsystems.org/index.php/Public_RecordView?processid=%s";
	
    @XmlElement( name = "DnaSample")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @IndexedEmbedded
    private DnaSample dnaSample;
    
	
	/** @see #getContigFile() */
	@XmlElement(name = "ContigFile")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private Media contigFile;
    
	/** @see #getConsensusSequence() */
	@XmlElement(name = "ConsensusSequence")
    private SequenceString consensusSequence = SequenceString.NewInstance();
	
//	/**{@link #getSequence()}*/
//	@XmlElement(name = "Sequence")
//    @Lob
//	private String sequence;
//	
//	@XmlElement(name = "Length")
//	private Integer length;
    
	
	@XmlAttribute(name = "isBarcode")
	private Boolean isBarcode = null;
	
	/** @see #getBarcodeSequence()*/
	@XmlElement(name = "BarcodeSequencePart")
    private SequenceString barcodeSequencePart = SequenceString.NewInstance();

	/** @see #getGeneticAccessionNumber()*/
	@XmlElement(name = "GeneticAccessionNumber")
	@Size(max=20)
	private String geneticAccessionNumber;
    
	/** @see #getBoldProcessId() */
	@XmlElement(name = "BoldProcessId")
	@Size(max=20)
	private String boldProcessId;
	
    @XmlElementWrapper(name = "SingleReads")
    @XmlElement(name = "SingleRead")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE})
	private Set<SingleRead> singleReads = new HashSet<SingleRead>();
    
	/** @see #getDnaMarker() */
	@XmlElement(name = "DnaMarker")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	//no cascade as it is a defined term
	private DefinedTerm dnaMarker;

	
	/** @see #getHaplotype() */
	@XmlElement(name = "Haplotype")
	@Size(max=100)
	private String haplotype;
	
	/** @see #getCitations() */
	@XmlElementWrapper(name = "Citations")
    @XmlElement(name = "Citation")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE})
	private Set<Reference> citations = new HashSet<Reference>();
	
//	//should be calculated in case sequence is set
//	@XmlElement (name = "DateSequenced", type= String.class)
//	@XmlJavaTypeAdapter(DateTimeAdapter.class)
//	@Type(type="dateTimeUserType")
//	@Basic(fetch = FetchType.LAZY)
//	private DateTime dateSequenced;
	
	
//*********************** FACTORY ****************************************************/
	
	public static Sequence NewInstance(String consensusSequence){
		Sequence result = new Sequence();
		result.setSequenceString(consensusSequence);
		return result;
	}
	
//*********************** CONSTRUCTOR ****************************************************/
	
	protected Sequence() {}

//*********************** GETTER / SETTER ****************************************************/
	

	/**
	 * The {@link DnaSample dna sample} this sequencing belongs too. 
	 */
	public DnaSample getDnaSample() {
		return dnaSample;
	}

	//TODO bidirectionality??
	/**
	 * @see #getDnaSample()
	 */
	private void setDnaSample(DnaSample dnaSample) {
		this.dnaSample = dnaSample;
	}

	/**
	 * The consensus sequence achieved by this sequencing.
	 */
	public SequenceString getConsensusSequence() {
		return consensusSequence;
	}


	/**
	 * @see #getConsensusSequence()
	 */
	public void setConsensusSequence(SequenceString sequenceString) {
		if (sequenceString == null){
			sequenceString = SequenceString.NewInstance();
		}
		this.consensusSequence = sequenceString;
	}
	
	/**
	 * The isBarcode flag should be set to true if this (consensus) sequence is or includes 
	 * a barcode sequence. If the barcode sequence is only a part of the consensus sequence
	 * this part is to be stored as {@link #getBarcodeSequencePart() barcode sequence part}.
	 * A isBarcode value of <code>null</code> indicates that we do have no knowledge
	 * wether the sequence is a barcoding sequence or not.
	 * 
	 * @see #getBarcodeSequencePart()
	 * @see #getSequenceString()
	 * @returns the isBarcode flag value (tri-state)
	 * 
	 */
	public Boolean getIsBarcode() {
		return isBarcode;
	}

	/**
	 * @see #getIsBarcode()
	 * @see #getBarcodeSequencePart()
	 */
	public void setIsBarcode(Boolean isBarcode) {
		this.isBarcode = isBarcode;
	}

	/**
	 * If the barcode sequence string does not include 100% of the (consensus) sequence 
	 * the part used as barcode is provided here. However, the barcode part
	 * should be kept if consensus sequence string and barcode sequence string are equal.
	 * 
	 * @see #getIsBarcode()
	 */
	public SequenceString getBarcodeSequencePart() {
		return barcodeSequencePart;
	}

	/**
	 * @see #getBarcodeSequence()
	 */
	public void setBarcodeSequence(SequenceString barcodeSequencePart) {
		if (barcodeSequencePart == null){
			barcodeSequencePart = SequenceString.NewInstance();
		}
		this.barcodeSequencePart = barcodeSequencePart;
	}
	
	/**
	 * Sets the {@link TermType#DnaMarker marker} examined and described by this sequencing.
	 * @return
	 */
	public DefinedTerm getDnaMarker(){
		return this.dnaMarker;
	}

	/**
	 * @see #getDnaMarker()
	 * @param marker
	 */
	public void setDnaMarker(DefinedTerm dnaMarker){
		this.dnaMarker = dnaMarker;
	}

	/**
	 * The accession number used in GenBank, EMBL and DDBJ. 
	 * @return
	 */
	public String getGeneticAccessionNumber() {
		return geneticAccessionNumber;
	}

	/**
	 * Sets the genetic accession number.
	 * @see #getGeneticAccessionNumber()
	 */
	public void setGeneticAccessionNumber(String geneticAccessionNumber) {
		this.geneticAccessionNumber = geneticAccessionNumber;
	}
	

	/**
	 * The identifier used by the Barcode of Life Data Systems (BOLD, http://www.boldsystems.org/).
	 */
	public String getBoldProcessId() {
		return boldProcessId;
	}

	public void setBoldProcessId(String boldProcessId) {
		this.boldProcessId = boldProcessId;
	}

	/**
	 * Returns the name of the haplotype.
	 * A haplotype (haploide genotype) is a variant of nucleotide sequences on the same chromosome.
	 * A certain haplotype may be specific for an individual, a population or a species.
	 * @return
	 */
	public String getHaplotype() {
		return haplotype;
	}

	/**
	 * @see #getHaplotype()
	 */
	public void setHaplotype(String haplotype) {
		this.haplotype = haplotype;
	}

	/**
	 * The contigFile containing all data and data processing for this sequencing.
	 */
	public Media getContigFile() {
		return contigFile;
	}

	/**
	 * @see #getContigFile()
	 */
	public void setContigFile(Media contigFile) {
		this.contigFile = contigFile;
	}
	
	
	/**
	 * Citations are the set of references in which this sequence was published.
	 * Unlike taxonomic names the first publication of a sequence
	 * is not so important (maybe because it is required by publishers
	 * that they are all registered at Genbank) therefore we do not have something like an 
	 * "original reference" attribute.<BR> 
	 * Links to these references are to be stored within the reference itself.
	 * @return the set of references in which this sequence was published.
	 */
	public Set<Reference> getCitations() {
		return citations;
	}
	/**
	 * @see #getCitations()
	 */
	protected void setCitations(Set<Reference> citations) {
		this.citations = citations;
	}
	/**
	 * @see #getCitations()
	 */
	public void addCitation(Reference citation) {
		this.citations.add(citation);
	}
	/**
	 * @see #getCitations()
	 */
	public void removeCitation(Reference citation) {
		this.citations.remove(citation);
	}

	/**
	 * The single reads that where used to create this consensus sequence.
	 */
	public Set<SingleRead> getSingleReads() {
		return singleReads;
	}
	/**
	 * @see #getSingleReads()
	 */
	public void addSingleRead(SingleRead singleRead) {
		this.singleReads.add(singleRead);
	}
	/**
	 * @see #getSingleReads()
	 */
	public void removeSingleRead(SingleRead singleRead) {
		this.singleReads.remove(singleRead);
	}
	/**
	 * @see #getSingleReads()
	 */
	//TODO private as long it is unclear how bidirectionality is handled
	private void setSingleReads(Set<SingleRead> singleReads) {
		this.singleReads = singleReads;
	}


	//*************************** Transient GETTER /SETTER *****************************/

	/**
	 * Delegate method to get the text representation of the consensus sequence
	 * @see #setSequenceString(String)
	 */
	@Transient
	public String getSequenceString() {
		return consensusSequence.getString();
	}

	/**
	 * Delegate method to set the text representation of the {@link #getConsensusSequence()
	 * consensus sequence}.
	 */
	@Transient
	public void setSequenceString(String sequence) {
		consensusSequence.setString(sequence);
	}
	
	/**
	 * Convenience method which computes the set of all related pherograms
	 * @return the set of pherograms.
	 */
	@Transient
	public Set<Media> getPherograms(){
		Set<Media> result = new HashSet<Media>();
		for (SingleRead singleSeq : singleReads){
			if (singleSeq.getPherogram() != null){
				result.add(singleSeq.getPherogram());
			}
		}
		return result;
	}
	

	//***** Registrations ************/
	/**
	 * Returns the computed genBank uri.
	 * @return
	 */
	@Transient
	public URI getGenBankUri() {
		return createExternalUri(GENBANK_BASE_URI);
	}

	/**
	 * Returns the computed EMBL uri.
	 * @return
	 */
	@Transient
	public URI getEmblUri() {
		return createExternalUri(EMBL_BASE_URI);
	}

	/**
	 * Returns the computed DDBJ uri.
	 * @return
	 */
	@Transient
	public URI getDdbjUri() {
		return createExternalUri(DDBJ_BASE_URI);
	}
	
	/**
	 * Returns the URI for the BOLD entry.
	 * @see #getBoldProcessId()
	 */
	@Transient
	public URI getBoldUri() {
		return createExternalUri(BOLD_BASE_URI);
	}

	
	private URI createExternalUri(String baseUri){
		if (StringUtils.isNotBlank(geneticAccessionNumber)){
			return URI.create(String.format(baseUri, geneticAccessionNumber.trim()));
		}else{
			return null;
		}
	}
	
	
	//*********************** CLONE ********************************************************/
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
		
		//sequences
		result.consensusSequence = (SequenceString)this.consensusSequence.clone();
		result.barcodeSequencePart = (SequenceString)this.barcodeSequencePart.clone();
		
		
		//single sequences
		result.singleReads = new HashSet<SingleRead>();
		for (SingleRead seq: this.singleReads){
			result.singleReads.add((SingleRead) seq);
		}
		
		//citations  //TODO do we really want to copy these ??
		result.citations = new HashSet<Reference>();
		for (Reference ref: this.citations){
			result.citations.add((Reference) ref);
		}
		
		
		
		return result;
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}


}