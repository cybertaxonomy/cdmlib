/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.molecular;


import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;

import java.util.*;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:22
 */
@Entity
public class DnaSample extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(DnaSample.class);
	private String bankNumber;
	//Notes on extraction, purification or amplification process
	private String productionNotes;
	private Calendar dateProduced;
	private Set<Sequence> sequences = new HashSet();
	private TissueSample extractedFrom;
	private Collection storedAt;


	

	@OneToMany
	public Set<Sequence> getSequences() {
		return sequences;
	}
	protected void setSequences(Set<Sequence> sequences) {
		this.sequences = sequences;
	}
	public void addSequences(Sequence sequence) {
		this.sequences.add(sequence);
	}
	public void removeSequences(Sequence sequence) {
		this.sequences.remove(sequence);
	}


	@ManyToOne
	public Collection getStoredAt(){
		return this.storedAt;
	}
	public void setStoredAt(Collection storedAt){
		this.storedAt = storedAt;
	}

	@ManyToOne
	public TissueSample getExtractedFrom(){
		return this.extractedFrom;
	}
	public void setExtractedFrom(TissueSample extractedFrom){
		this.extractedFrom = extractedFrom;
	}

	public String getBankNumber(){
		return this.bankNumber;
	}
	public void setBankNumber(String bankNumber){
		this.bankNumber = bankNumber;
	}

	public String getProductionNotes(){
		return this.productionNotes;
	}
	public void setProductionNotes(String productionNotes){
		this.productionNotes = productionNotes;
	}

	@Temporal(TemporalType.DATE)
	public Calendar getDateProduced(){
		return this.dateProduced;
	}
	public void setDateProduced(Calendar dateProduced){
		this.dateProduced = dateProduced;
	}

	public String generateTitle(){
		return "";
	}

}