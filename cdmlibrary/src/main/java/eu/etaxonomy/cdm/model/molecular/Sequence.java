/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.molecular;


import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Media;
import eu.etaxonomy.cdm.model.common.IReferencedEntity;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:51
 */
@Entity
public class Sequence extends IdentifiableEntity implements IReferencedEntity {
	static Logger logger = Logger.getLogger(Sequence.class);
	//the sequence as a string of base pairs. 5'->3'
	private String sequence;
	//should be calculated in case sequence is set
	private int length;
	//should be calculated in case sequence is set
	private Calendar dateSequenced;
	//should be calculated in case sequence is set
	private boolean isBarcode;
	//the sequence as a string of base pairs. 5'->3'
	private String citationMicroReference;
	private ReferenceBase publishedIn;
	private Set<ReferenceBase> citations;
	private Set<GenBankAccession> genBankAccession;
	private Locus locus;
	private Set<Media> chromatograms;

	public Locus getLocus(){
		return this.locus;
	}

	/**
	 * 
	 * @param locus    locus
	 */
	public void setLocus(Locus locus){
		this.locus = locus;
	}

	public ReferenceBase getPublishedIn(){
		return this.publishedIn;
	}

	/**
	 * 
	 * @param publishedIn    publishedIn
	 */
	public void setPublishedIn(ReferenceBase publishedIn){
		this.publishedIn = publishedIn;
	}


	
	@OneToMany
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

	
	@OneToMany
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

	
	@OneToMany
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

	public int getLength(){
		return this.length;
	}

	/**
	 * 
	 * @param length    length
	 */
	public void setLength(int length){
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

	public String generateTitle(){
		return "";
	}

	@Transient
	public ReferenceBase getCitation(){
		return null;
	}

}