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

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.jaxb.DateTimeAdapter;
import eu.etaxonomy.cdm.model.common.IReferencedEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.media.IMediaDocumented;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.common.IdentifiableEntityDefaultCacheStrategy;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:51
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Sequence", propOrder = {
    "sequence",
    "length",
    "dateSequenced",
    "barcode",
    "citationMicroReference",
    "publishedIn",
    "locus",
    "citations",
    "genBankAccession",
    "chromatograms"
})
@XmlRootElement(name = "Sequence")
@Entity
@Audited
@Configurable
@Table(appliesTo="Sequence", indexes = { @Index(name = "sequenceTitleCacheIndex", columnNames = { "titleCache" }) })
public class Sequence extends IdentifiableEntity<IIdentifiableEntityCacheStrategy<Sequence>> implements IReferencedEntity, IMediaDocumented{
	private static final long serialVersionUID = 8298983152731241775L;
	private static final Logger logger = Logger.getLogger(Sequence.class);
	
	//the sequence as a string of base pairs. 5'->3'
	@XmlElement(name = "Sequence")
	private String sequence;
	
	//should be calculated in case sequence is set
	@XmlElement(name = "Length")
	private Integer length;
	
	//should be calculated in case sequence is set
	@XmlElement (name = "DateSequenced", type= String.class)
	@XmlJavaTypeAdapter(DateTimeAdapter.class)
	@Type(type="dateTimeUserType")
	@Basic(fetch = FetchType.LAZY)
	private DateTime dateSequenced;
	
	//should be calculated in case sequence is set
	@XmlAttribute(name = "isBarcode")
	private boolean barcode;
	
	//the sequence as a string of base pairs. 5'->3'
	@XmlElement(name = "CitationMicroReference")
	private String citationMicroReference;
	
	@XmlElement(name = "PublishedIn")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade(CascadeType.SAVE_UPDATE)
	private Reference<?> publishedIn;
	
	@XmlElementWrapper(name = "Citations")
	@XmlElement(name = "Citation")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(fetch = FetchType.LAZY)
	private Set<Reference> citations = new HashSet<Reference>();
	
	@XmlElementWrapper(name = "GenBankAccessions")
	@XmlElement(name = "GenBankAccession")
    @OneToMany(fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
    private Set<GenBankAccession> genBankAccession = new HashSet<GenBankAccession>();
	
	@XmlElement(name = "Locus")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade(CascadeType.SAVE_UPDATE)
	private Locus locus;
	
	@XmlElementWrapper(name = "Chromatograms")
	@XmlElement(name = "Chromatogram")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(fetch = FetchType.LAZY)
	private Set<Media> chromatograms = new HashSet<Media>();
	
//*********************** FACTORY ****************************************************/
	
	public static Sequence NewInstance(String sequence){
		Sequence result = new Sequence();
		result.setSequence(sequence);
		return result;
	}
	
//*********************** CONSTRUCTOR ****************************************************/
	
	protected Sequence() {
		super(); // FIXME I think this is explicit - do we really need to call this?
		this.cacheStrategy = new IdentifiableEntityDefaultCacheStrategy<Sequence>();
	}

//*********************** GETTER / SETTER ****************************************************/
	
	public Locus getLocus(){
		return this.locus;
	}

	public void setLocus(Locus locus){
		this.locus = locus;
	}

	public Reference getPublishedIn(){
		return this.publishedIn;
	}
	
	public void setPublishedIn(Reference publishedIn){
		this.publishedIn = publishedIn;
	}

	public Set<Reference> getCitations() {
		return citations;
	}
	protected void setCitations(Set<Reference> citations) {
		this.citations = citations;
	}
	public void addCitation(Reference citation) {
		this.citations.add(citation);
	}
	public void removeCitation(Reference citation) {
		this.citations.remove(citation);
	}

	public Set<GenBankAccession> getGenBankAccession() {
		return genBankAccession;
	}

	public void addGenBankAccession(GenBankAccession genBankAccession) {
		this.genBankAccession.add(genBankAccession);
	}
	
	public void removeGenBankAccession(GenBankAccession genBankAccession) {
		this.genBankAccession.remove(genBankAccession);
	}
	
	public Set<Media> getChromatograms() {
		return chromatograms;
	}

	public void addChromatogram(Media chromatogram) {
		this.chromatograms.add(chromatogram);
	}
	
	public void removeChromatogram(Media chromatogram) {
		this.chromatograms.remove(chromatogram);
	}
	
	@Transient
	public Set<Media> getMedia() {
		return getChromatograms();
	}

	public String getSequence(){
		return this.sequence;
	}

	/**
	 * 
	 * @param sequence    sequence
	 */
	public void setSequence(String sequence){
		this.sequence = sequence;
	}

	public Integer getLength(){
		return this.length;
	}

	/**
	 * 
	 * @param length    length
	 */
	public void setLength(Integer length){
		this.length = length;
	}

	public DateTime getDateSequenced(){
		return this.dateSequenced;
	}

	/**
	 * 
	 * @param dateSequenced    dateSequenced
	 */
	public void setDateSequenced(DateTime dateSequenced){
		this.dateSequenced = dateSequenced;
	}

	public boolean isBarcode(){
		return this.barcode;
	}

	/**
	 * 
	 * @param isBarcode    isBarcode
	 */
	public void setBarcode(boolean barcode){
		this.barcode = barcode;
	}

	public String getCitationMicroReference(){
		return this.citationMicroReference;
	}

	/**
	 * 
	 * @param citationMicroReference    citationMicroReference
	 */
	public void setCitationMicroReference(String citationMicroReference){
		this.citationMicroReference = citationMicroReference;
	}

	public Reference getCitation(){
		return publishedIn;
	}
	
	//*********************** CLONE ********************************************************/
	/** 
	 * Clones <i>this</i> sequence. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> sequence by
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
		
		result.citations = new HashSet<Reference>();
		for (Reference ref: this.citations){
			result.citations.add((Reference) ref.clone());
		
		}
		
		result.genBankAccession = new HashSet<GenBankAccession>();
		for (GenBankAccession genBankAcc: this.genBankAccession){
			result.genBankAccession.add((GenBankAccession)genBankAcc.clone());
		}
		
		result.chromatograms = new HashSet<Media>();
		
		for (Media chromatogram: this.chromatograms){
			result.chromatograms.add((Media)chromatogram.clone());
		}
		
		return result;
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}