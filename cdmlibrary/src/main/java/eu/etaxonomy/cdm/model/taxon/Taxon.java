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
	private Set<Description> descriptions;
	private Set<SynonymRelationship> inverseSynonymRelations;
	private Set<TaxonRelationship> taxonRelations;
	private Set<TaxonRelationship> inverseTaxonRelations;


	@OneToMany
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
	public Set<SynonymRelationship> getInverseSynonymRelations() {
		return inverseSynonymRelations;
	}
	protected void setInverseSynonymRelations(
			Set<SynonymRelationship> inverseSynonymRelations) {
		this.inverseSynonymRelations = inverseSynonymRelations;
	}


	@OneToMany
	public Set<TaxonRelationship> getTaxonRelations() {
		return taxonRelations;
	}
	protected void setTaxonRelations(Set<TaxonRelationship> taxonRelations) {
		this.taxonRelations = taxonRelations;
	}
	public void addTaxonRelations(TaxonRelationship taxonRelation) {
		this.taxonRelations.add(taxonRelation);
	}
	public void removeTaxonRelations(TaxonRelationship taxonRelation) {
		this.taxonRelations.remove(taxonRelation);
	}


	@OneToMany
	public Set<TaxonRelationship> getInverseTaxonRelations() {
		return inverseTaxonRelations;
	}
	protected void setInverseTaxonRelations(
			Set<TaxonRelationship> inverseTaxonRelations) {
		this.inverseTaxonRelations = inverseTaxonRelations;
	}


	@Override
	public String generateTitle(){
		return "";
	}

}