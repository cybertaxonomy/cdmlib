/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.molecular;


import eu.etaxonomy.cdm.model.media.IMediaDocumented;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IReferencedEntity;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

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
    "isBarcode",
    "citationMicroReference",
    "publishedIn",
    "locus",
    "citations",
    "genBankAccession",
    "chromatograms"
})
@XmlRootElement(name = "Sequence")
@Entity
//@Audited
@Table(appliesTo="Sequence", indexes = { @Index(name = "sequenceTitleCacheIndex", columnNames = { "titleCache" }) })
public class Sequence extends IdentifiableEntity implements IReferencedEntity, IMediaDocumented{
	private static final long serialVersionUID = 8298983152731241775L;
	private static final Logger logger = Logger.getLogger(Sequence.class);
	
	//the sequence as a string of base pairs. 5'->3'
	@XmlElement(name = "Sequence")
	private String sequence;
	
	//should be calculated in case sequence is set
	@XmlElement(name = "Length")
	private Integer length;
	
	//should be calculated in case sequence is set
	@XmlElement(name = "DateSequenced")
	private Calendar dateSequenced;
	
	//should be calculated in case sequence is set
	@XmlAttribute(name = "isBarcode")
	private boolean isBarcode;
	
	//the sequence as a string of base pairs. 5'->3'
	@XmlElement(name = "CitationMicroReference")
	private String citationMicroReference;
	
	@XmlElement(name = "IublishedIn")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private ReferenceBase publishedIn;
	
	@XmlElementWrapper(name = "Citations")
	@XmlElement(name = "Citation")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private Set<ReferenceBase> citations = new HashSet<ReferenceBase>();
	
	@XmlElementWrapper(name = "GenBankAccessions")
	@XmlElement(name = "GenBankAccession")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private Set<GenBankAccession> genBankAccession = new HashSet<GenBankAccession>();
	
	@XmlElement(name = "Locus")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private Locus locus;
	
	@XmlElementWrapper(name = "Chromatograms")
	@XmlElement(name = "Chromatogram")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private Set<Media> chromatograms = new HashSet<Media>();

	@ManyToOne(fetch = FetchType.LAZY)
	public Locus getLocus(){
		logger.debug("getLocus");
		return this.locus;
	}
	public void setLocus(Locus locus){
		this.locus = locus;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public ReferenceBase getPublishedIn(){
		return this.publishedIn;
	}
	public void setPublishedIn(ReferenceBase publishedIn){
		this.publishedIn = publishedIn;
	}


	
	@OneToMany(fetch = FetchType.LAZY)
	public Set<ReferenceBase> getCitations() {
		return citations;
	}
	protected void setCitations(Set<ReferenceBase> citations) {
		this.citations = citations;
	}
	public void addCitation(ReferenceBase citation) {
		this.citations.add(citation);
	}
	public void removeCitation(ReferenceBase citation) {
		this.citations.remove(citation);
	}

	
	@OneToMany(fetch = FetchType.LAZY)
	public Set<GenBankAccession> getGenBankAccession() {
		return genBankAccession;
	}

	protected void setGenBankAccession(Set<GenBankAccession> genBankAccession) {
		this.genBankAccession = genBankAccession;
	}
	public void addGenBankAccession(GenBankAccession genBankAccession) {
		this.genBankAccession.add(genBankAccession);
	}
	public void removeGenBankAccession(GenBankAccession genBankAccession) {
		this.genBankAccession.remove(genBankAccession);
	}

	
	@OneToMany(fetch = FetchType.LAZY)
	public Set<Media> getChromatograms() {
		return chromatograms;
	}

	protected void setChromatograms(Set<Media> chromatograms) {
		this.chromatograms = chromatograms;
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

	@Temporal(TemporalType.DATE)
	public Calendar getDateSequenced(){
		return this.dateSequenced;
	}

	/**
	 * 
	 * @param dateSequenced    dateSequenced
	 */
	public void setDateSequenced(Calendar dateSequenced){
		this.dateSequenced = dateSequenced;
	}

	public boolean isBarcode(){
		return this.isBarcode;
	}

	/**
	 * 
	 * @param isBarcode    isBarcode
	 */
	public void setBarcode(boolean isBarcode){
		this.isBarcode = isBarcode;
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

	@Override
	public String generateTitle(){
		return "";
	}

	@Transient
	public ReferenceBase getCitation(){
		return null;
	}

}