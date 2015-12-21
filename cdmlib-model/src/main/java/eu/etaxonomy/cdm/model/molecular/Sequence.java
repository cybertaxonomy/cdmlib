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
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
import javax.xml.bind.annotation.XmlTransient;
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
 * Alignment of multiple {@link SingleRead single sequences} to a consensus sequence.
 * This sequence is a part of (or the complete) DNA sequences of the related {@link DnaSample DNA Sample},
 * while
 *
 * <BR>This class holds information about both the combining process of
 * {@link SingleRead single sequences} to one consensus sequence
 * ({@link #getSingleReads() singleReads} , {@link #getContigFile() contigFile} )
 * as well as sequence related information.
 * The later includes the {@link #getConsensusSequence() sequence string} itself,
 * important genetic information about the DNA that has been sequenced
 * ({@link #getDnaMarker() marker} , {@link #getHaplotype()} haplotype) as well as
 * registration information ({@link #getGeneticAccessionNumber() genetic accession number} ),
 * citations, and barcoding information ({@link #getBoldProcessId() BOLD-id},
 * {@link #getBarcodeSequencePart() barcode sequence}, ...).
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
    "singleReadAlignments",
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

	//TODO move to cdmlib-ext?
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
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private Media contigFile;

	/** @see #getConsensusSequence() */
	@XmlElement(name = "ConsensusSequence")
    private SequenceString consensusSequence = SequenceString.NewInstance();

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

    @XmlElementWrapper(name = "SingleReadAlignments")
    @XmlElement(name = "SingleReadAlignment")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="consensusAlignment", fetch = FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private Set<SingleReadAlignment> singleReadAlignments = new HashSet<SingleReadAlignment>();

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
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
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


	public static Sequence NewInstance(String consensusSequence, Integer length){
		Sequence result = NewInstance(consensusSequence);
		result.getConsensusSequence().setLength(length);
		return result;
	}

	public static Sequence NewInstance(DnaSample dnaSample, String consensusSequence, Integer length){
		Sequence result = NewInstance(consensusSequence);
		result.getConsensusSequence().setLength(length);
		dnaSample.addSequence(result);

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

	/**
	 * To be called only from {@link DnaSample#addSequence(Sequence)}
	 * @see #getDnaSample()
	 */
	//TODO implement full bidirectionality
	protected void setDnaSample(DnaSample dnaSample) {
		this.dnaSample = dnaSample;
		if (dnaSample != null && !dnaSample.getSequences().contains(this)){
			throw new RuntimeException("Don't use DNA setter");
		}
	}

	/**
	 * The resulting consensus sequence represened by this {@link Sequence sequence} .
	 * The consensus is usually computed from the {@link SingleRead single reads}.
	 * The result of which is stored in a file called {@link #getContigFile() contig file}
	 *
	 * #see {@link #getContigFile()}
	 * #see {@link #getSingleReads()}
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
	 * a barcoding sequence. If the barcoding sequence is only a part of the consensus sequence
	 * this part shall be stored as {@link #getBarcodeSequencePart() barcoding sequence part}.
	 * A isBarcode value of <code>null</code> indicates that we do have no knowledge
	 * whether the sequence is a barcoding sequence or not.
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
	 * should be kept empty if consensus sequence string and barcode sequence string are equal.
	 *
	 * @see #getIsBarcode()
	 */
	public SequenceString getBarcodeSequencePart() {
		return barcodeSequencePart;
	}

	/**
	 * @see #getBarcodeSequencePart()
	 */
	public void setBarcodeSequencePart(SequenceString barcodeSequencePart) {
		if (barcodeSequencePart == null){
			barcodeSequencePart = SequenceString.NewInstance();
		}
		this.barcodeSequencePart = barcodeSequencePart;
	}

	/**
	 * Sets the {@link TermType#DnaMarker DNA marker} examined and described by this sequencing.
	 * The marker should usually be similar to the one used in the according {@link Amplification
	 * amplification process}. However, it may slightly differ, or, if multiple amplifications where
	 * used to build this consensus sequence it may be the super set of the markers used in amplification.
	 *
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
	 *
	 * @see #getConsensusSequence()
	 * @see #getSingleReads()
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
	 * The {@link SingleRead single reads} that were used to build this consensus sequence.
	 *
	 * @see #getConsensusSequence()
	 * @see #getContigFile()
	 */
	public Set<SingleReadAlignment> getSingleReadAlignments() {
		return singleReadAlignments;
	}
	/**
	 * @see #getSingleReads()
	 */
	public void addSingleReadAlignment(SingleReadAlignment singleReadAlignment) {
		this.singleReadAlignments.add(singleReadAlignment);
		if (! this.equals(singleReadAlignment.getConsensusSequence())){
			singleReadAlignment.setConsensusAlignment(this);
		};
	}
	/**
	 * @see #getSingleReads()
	 */
	public void removeSingleReadAlignment(SingleReadAlignment singleReadAlignment) {
		this.singleReadAlignments.remove(singleReadAlignment);
		if (this.equals(singleReadAlignment.getConsensusSequence())){
			singleReadAlignment.setConsensusAlignment(null);
			singleReadAlignment.setSingleRead(null);
		}
	}
//	/**
//	 * @see #getSingleReads()
//	 */
//	//TODO private as long it is unclear how bidirectionality is handled
//	@SuppressWarnings("unused")
//	private void setSingleReadAlignments(Set<SingleReadAlignment> singleReadAlignments) {
//		this.singleReadAlignments = singleReadAlignments;
//	}

// *********************** CONVENIENCE ***********************************/

	/**
	 * Convenience method to add a single read to a consensus sequence
	 * by creating a {@link SingleReadAlignment}.
	 * @param singleRead the {@link SingleRead} to add
	 * @return the created SingleReadAlignment
	 */
	public SingleReadAlignment addSingleRead(SingleRead singleRead) {
		SingleReadAlignment alignment = SingleReadAlignment.NewInstance(this, singleRead);
		return alignment;
	}

	public void removeSingleRead(SingleRead singleRead) {
		Set<SingleReadAlignment> toRemove = new HashSet<SingleReadAlignment>();
		for (SingleReadAlignment align : this.singleReadAlignments){
			if (align.getSingleRead() != null && align.getSingleRead().equals(singleRead)){
				toRemove.add(align);
			}
		}
		for (SingleReadAlignment align : toRemove){
			removeSingleReadAlignment(align);
		}
		return;
	}

	/**
	 * Convenience method that returns all single reads this consensus sequence
	 * is based on via {@link SingleReadAlignment}s.
	 * @return set of related single reads
	 */
	@XmlTransient
	@Transient
	public Set<SingleRead> getSingleReads(){
		Set<SingleRead> singleReads = new HashSet<SingleRead>();
		for (SingleReadAlignment align : this.singleReadAlignments){
			if (align.getSingleRead() != null){  // == null should not happen
				singleReads.add(align.getSingleRead());
			}
		}
		return singleReads;
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
		for (SingleReadAlignment singleReadAlign : singleReadAlignments){
			if (singleReadAlign.getSingleRead() != null &&  singleReadAlign.getSingleRead().getPherogram() != null){
				result.add(singleReadAlign.getSingleRead().getPherogram());
			}
		}
		return result;
	}


	//***** Registrations ************/
	/**
	 * Returns the computed genBank uri.
	 * @return the uri composed of {@link #GENBANK_BASE_URI} and {@link #geneticAccessionNumber}
	 * @throws URISyntaxException when URI could not be created with {@link #geneticAccessionNumber}
	 */
	@Transient
	public URI getGenBankUri() throws URISyntaxException {
		return createExternalUri(GENBANK_BASE_URI, geneticAccessionNumber);
	}

	/**
	 * Returns the computed EMBL uri.
	 * @return the uri composed of {@link #EMBL_BASE_URI} and {@link #geneticAccessionNumber}
	 * @throws URISyntaxException when URI could not be created with {@link #geneticAccessionNumber}
	 */
	@Transient
	public URI getEmblUri() throws URISyntaxException {
		return createExternalUri(EMBL_BASE_URI, geneticAccessionNumber);
	}

	/**
	 * Returns the computed DDBJ uri.
	 * @return the uri composed of {@link #DDBJ_BASE_URI} and {@link #geneticAccessionNumber}
	 * @throws URISyntaxException when URI could not be created with {@link #geneticAccessionNumber}
	 */
	@Transient
	public URI getDdbjUri() throws URISyntaxException {
		return createExternalUri(DDBJ_BASE_URI, geneticAccessionNumber);
	}

	/**
	 * Returns the URI for the BOLD entry.
	 * @return the uri composed of {@link #BOLD_BASE_URI} and {@link #boldProcessId}
	 * @throws URISyntaxException when URI could not be created with {@link #boldProcessId}
	 * @see #getBoldProcessId()
	 */
	@Transient
	public URI getBoldUri() throws URISyntaxException {
		return createExternalUri(BOLD_BASE_URI, boldProcessId);
	}

	private URI createExternalUri(String baseUri, String id) throws URISyntaxException{
		if (StringUtils.isNotBlank(id)){
			return new URI(String.format(baseUri, id.trim()));
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
		result.singleReadAlignments = new HashSet<SingleReadAlignment>();
		for (SingleReadAlignment singleReadAlign: this.singleReadAlignments){
			SingleReadAlignment newAlignment = (SingleReadAlignment)singleReadAlign.clone();
			result.singleReadAlignments.add(newAlignment);
		}

		//citations  //TODO do we really want to copy these ??
		result.citations = new HashSet<Reference>();
		for (Reference<?> ref: this.citations){
			result.citations.add(ref);
		}



		return result;
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}


}