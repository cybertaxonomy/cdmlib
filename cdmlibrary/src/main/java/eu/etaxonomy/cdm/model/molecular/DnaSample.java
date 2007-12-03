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
import eu.etaxonomy.cdm.model.occurrence.PhysicalOrganism;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservation;
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
public class DnaSample extends Specimen {
	static Logger logger = Logger.getLogger(DnaSample.class);
	private String bankNumber;
	private Set<Sequence> sequences = new HashSet();

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


	@Transient
	public Collection getStoredAt(){
		return this.getCollection();
	}
	public void setStoredAt(Collection storedAt){
		this.setCollection(storedAt);
	}

	@Transient
	public Set<SpecimenOrObservation> getExtractedFrom(){
		return this.getDerivedFrom().getOriginals();
	}

	@Transient
	public String getBankNumber(){
		return this.getCatalogNumber();
	}
	public void setBankNumber(String bankNumber){
		this.setCatalogNumber(bankNumber);
	}

	@Transient
	public String getProductionNotes(){
		return this.getDerivedFrom().getDescription();
	}
	public void setProductionNotes(String productionNotes){
		this.getDerivedFrom().setDescription(productionNotes);
	}

	@Transient
	public Calendar getDateProduced(){
		return this.getDerivedFrom().getTimeperiod().getStart();
	}

}