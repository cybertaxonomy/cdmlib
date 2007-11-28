/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.description.Description;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:56
 */
@Entity
public class Taxon extends TaxonBase {
	static Logger logger = Logger.getLogger(Taxon.class);
	private Set<Description> descriptions = new HashSet();
	private Set<SynonymRelationship> synonymRelations = new HashSet();
	private Set<TaxonRelationship> taxonRelations = new HashSet();


	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<Description> getDescriptions() {
		return descriptions;
	}
	protected void setDescriptions(Set<Description> descriptions) {
		this.descriptions = descriptions;
	}
	public void addDescriptions(Description description) {
		this.descriptions.add(description);
	}
	public void removeDescriptions(Description description) {
		this.descriptions.remove(description);
	}


	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<SynonymRelationship> getSynonymRelations() {
		return synonymRelations;
	}
	protected void setSynonymRelations(Set<SynonymRelationship> synonymRelations) {
		this.synonymRelations = synonymRelations;
	}
	public void addSynonymRelation(SynonymRelationship synonymRelation) {
		this.synonymRelations.add(synonymRelation);
	}
	public void removeSynonymRelation(SynonymRelationship synonymRelation) {
		this.synonymRelations.remove(synonymRelation);
	}
	
	@Transient
	public List<Synonym> getSynonymsSortedByType(){
		// FIXME
		return null;
	}

	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<TaxonRelationship> getTaxonRelations() {
		return taxonRelations;
	}
	protected void setTaxonRelations(Set<TaxonRelationship> taxonRelations) {
		this.taxonRelations = taxonRelations;
	}
	public void addTaxonRelation(TaxonRelationship taxonRelation) {
		this.taxonRelations.add(taxonRelation);
	}
	public void removeTaxonRelation(TaxonRelationship taxonRelation) {
		this.taxonRelations.remove(taxonRelation);
	}

	@Transient
	public Set<TaxonRelationship> getIncomingTaxonRelations() {
		// FIXME: filter relations
		return taxonRelations;
	}
	@Transient
	public Set<TaxonRelationship> getOutgoingTaxonRelations() {
		// FIXME: filter relations
		return taxonRelations;
	}


	@Override
	public String generateTitle(){
		return "";
	}

}