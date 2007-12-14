/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

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
	private Set<TaxonDescription> descriptions = new HashSet();
	private Set<SynonymRelationship> synonymRelations = new HashSet();
	// all taxa relations, no matter if this is fromTaxon or toTaxon
	private Set<TaxonRelationship> taxonRelations = new HashSet();
	// shortcut to the taxonomicIncluded (parent) taxon. Managed by the taxonRelations setter
	private Taxon higherTaxon;

	public static Taxon NewInstance(TaxonNameBase taxonName, ReferenceBase sec){
		Taxon result = new Taxon();
		result.setName(taxonName);
		result.setSec(sec);
		return result;
	}
	
	//TODO should be private, but still produces Spring init errors
	public Taxon(){
	}

	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<TaxonDescription> getDescriptions() {
		return descriptions;
	}
	protected void setDescriptions(Set<TaxonDescription> descriptions) {
		this.descriptions = descriptions;
	}
	public void addDescriptions(TaxonDescription description) {
		this.descriptions.add(description);
	}
	public void removeDescriptions(DescriptionBase description) {
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
	

	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<TaxonRelationship> getTaxonRelations() {
		return taxonRelations;
	}
	protected void setTaxonRelations(Set<TaxonRelationship> taxonRelations) {
		this.taxonRelations = taxonRelations;
	}
	public void addTaxonRelation(TaxonRelationship taxonRelation) {
		// check if this sets the taxonomical parent. If so, remember a shortcut to this taxon
		// TODO: all relation objects need both taxa (to/from). How can this be guaranteed so we dont need to check isNotNull all the time?
		if (taxonRelation.getType().equals(ConceptRelationshipType.TAXONOMICALLY_INCLUDED_IN()) && taxonRelation.getToTaxon()!=null && taxonRelation.getFromTaxon().equals(this)){
			this.setHigherTaxon(taxonRelation.getToTaxon());
		}
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

	@Transient
	public Taxon getTaxonomicParent() {
		for (TaxonRelationship rel: this.getTaxonRelations()){
			if (rel.getType().equals(ConceptRelationshipType.TAXONOMICALLY_INCLUDED_IN()) && rel.getFromTaxon().equals(this)){
				return rel.getToTaxon();
			}
		}
		return null;
	}
	@Transient
	public Set<Taxon> getTaxonomicChildren() {
		Set<Taxon> taxa = new HashSet<Taxon>();
		for (TaxonRelationship rel: this.getTaxonRelations()){
			if (rel.getType().equals(ConceptRelationshipType.TAXONOMICALLY_INCLUDED_IN()) && rel.getToTaxon().equals(this)){
				taxa.add(rel.getFromTaxon());
			}
		}
		return taxa;
	}
	@Transient
	public boolean hasTaxonomicChildren(){
		for (TaxonRelationship rel: this.getTaxonRelations()){
			if (rel.getType().equals(ConceptRelationshipType.TAXONOMICALLY_INCLUDED_IN()) && rel.getToTaxon().equals(this)){
				return true;
			}
		}
		return false;
	}
	
	
	@Transient
	public Set<Synonym> getSynonyms(){
		Set<Synonym> syns = new HashSet();
		for (SynonymRelationship rel: this.getSynonymRelations()){
			syns.add(rel.getSynoynm());
		}
		return syns;
	}
	@Transient
	public Set<Synonym> getSynonymsSortedByType(){
		// FIXME: need to sort synonyms according to type!!!
		return getSynonyms();
	}
	@Transient
	public Set<TaxonNameBase> getSynonymNames(){
		Set<TaxonNameBase> names = new HashSet();
		for (SynonymRelationship rel: this.getSynonymRelations()){
			names.add(rel.getSynoynm().getName());
		}
		return names;
	}
	
	@Transient
	public void addSynonym(Synonym synonym, SynonymRelationshipType synonymType){
		SynonymRelationship synonymRelationship = new SynonymRelationship();
		synonymRelationship.setSynoynm(synonym);
		synonymRelationship.setAcceptedTaxon(this);
		synonymRelationship.setType(synonymType);
		this.addSynonymRelation(synonymRelationship);
	}
	
	@Transient
	public void addChild(Taxon child){
		if (child == null){
			throw new NullPointerException("Chilc Taxon is 'null'");
		}else{
			TaxonRelationship taxonRelation = new TaxonRelationship();
			taxonRelation.setFromTaxon(child);
			taxonRelation.setToTaxon(this);
			taxonRelation.setType(ConceptRelationshipType.TAXONOMICALLY_INCLUDED_IN());
			this.addTaxonRelation(taxonRelation);
		}
	}
	
	
	@Transient
	public void addParent(Taxon parent){
		if (parent != null){
			throw new NullPointerException("Parent Taxon is 'null'");
		}else{
			parent.addChild(this);
		}
	}

	public Taxon getHigherTaxon() {
		return higherTaxon;
	}
	private void setHigherTaxon(Taxon higherTaxon) {
		this.higherTaxon = higherTaxon;
	}

}