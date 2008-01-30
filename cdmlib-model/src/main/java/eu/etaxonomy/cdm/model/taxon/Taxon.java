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
public class Taxon extends TaxonBase implements Iterable<Taxon>{
	static Logger logger = Logger.getLogger(Taxon.class);
	private Set<TaxonDescription> descriptions = new HashSet();
	// all related synonyms
	private Set<SynonymRelationship> synonymRelations = new HashSet();
	// all taxa relations with rel.fromTaxon==this
	private Set<TaxonRelationship> relationsFromThisTaxon = new HashSet();
	// all taxa relations with rel.toTaxon==this
	private Set<TaxonRelationship> relationsToThisTaxon = new HashSet();
	// shortcut to the taxonomicIncluded (parent) taxon. Managed by the taxonRelations setter
	private Taxon taxonomicParentCache;


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
	

	@OneToMany(mappedBy="fromTaxon")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<TaxonRelationship> getRelationsFromThisTaxon() {
		return relationsFromThisTaxon;
	}
	protected void setRelationsFromThisTaxon(
			Set<TaxonRelationship> relationsFromThisTaxon) {
		this.relationsFromThisTaxon = relationsFromThisTaxon;
	}


	@OneToMany(mappedBy="toTaxon")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<TaxonRelationship> getRelationsToThisTaxon() {
		return relationsToThisTaxon;
	}
	protected void setRelationsToThisTaxon(Set<TaxonRelationship> relationsToThisTaxon) {
		this.relationsToThisTaxon = relationsToThisTaxon;
	}

	@ManyToOne
	// used by hibernate only...
	private Taxon getTaxonomicParentCache() {
		return taxonomicParentCache;
	}
	private void setTaxonomicParentCache(Taxon taxonomicParent) {
		this.taxonomicParentCache = taxonomicParent;
	}

	@Transient
	public Set<TaxonRelationship> getTaxonRelations() {
		Set<TaxonRelationship> rels = new HashSet();
		rels.addAll(getRelationsToThisTaxon());
		rels.addAll(getRelationsFromThisTaxon());
		return rels;
	}
	public void removeTaxonRelation(TaxonRelationship rel) {
		this.relationsToThisTaxon.remove(rel);
		this.relationsFromThisTaxon.remove(rel);
		// check if this removes the taxonomical parent. If so, also remove shortcut to the higher taxon
		if (rel.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN()) && rel.getFromTaxon().equals(this)){
			this.setTaxonomicParentCache(null);
		}
		// TODO: remove in related taxon too?
	}
	public void addTaxonRelation(TaxonRelationship rel) {
		if (rel!=null && rel.getType()!=null && !getTaxonRelations().contains(rel)){
			if (rel.getFromTaxon().equals(this)){
				Taxon toTaxon=rel.getToTaxon();
				relationsFromThisTaxon.add(rel);
				// also add relation to other taxon object
				toTaxon.addTaxonRelation(rel);
				// check if this sets the taxonomical parent. If so, remember a shortcut to this taxon
				if (rel.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN()) && toTaxon!=null ){
					this.setTaxonomicParentCache(rel.getToTaxon());
				}
			}else if (rel.getToTaxon().equals(this)){
				relationsToThisTaxon.add(rel);
			}
		}
	}
	public void addTaxonRelation(Taxon toTaxon, TaxonRelationshipType type, ReferenceBase citation, String microreference) {
		TaxonRelationship rel = new TaxonRelationship();
		rel.setToTaxon(toTaxon);
		rel.setFromTaxon(this);
		rel.setType(type);
		rel.setCitation(citation);
		rel.setCitationMicroReference(microreference);
		this.addTaxonRelation(rel);
	}

	
	
	
	@Override
	public String generateTitle(){
		return this.toString();
	}

	@Transient
	public void addTaxonomicChild(Taxon child, ReferenceBase citation, String microcitation){
		if (child == null){
			throw new NullPointerException("Child Taxon is 'null'");
		}else{
			child.setTaxonomicParent(this, citation, microcitation);
		}
	}
	
	/**
	 * @return
	 */
	@Transient
	public Taxon getTaxonomicParent() {
		return getTaxonomicParentCache();
	}
	/**
	 * @param parent
	 * @param citation
	 * @param microcitation 
	 */
	public void setTaxonomicParent(Taxon parent, ReferenceBase citation, String microcitation){
		if (parent == null){
			throw new NullPointerException("Parent Taxon is 'null'");
		}else{
			addTaxonRelation(parent, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),citation,microcitation);
		}
	}

	/**
	 * @return
	 */
	@Transient
	public Set<Taxon> getTaxonomicChildren() {
		Set<Taxon> taxa = new HashSet<Taxon>();
		for (TaxonRelationship rel: this.getRelationsToThisTaxon()){
			if (rel.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())){
				taxa.add(rel.getFromTaxon());
			}
		}
		return taxa;
	}
	/**
	 * @return
	 */
	@Transient
	public boolean hasTaxonomicChildren(){
		for (TaxonRelationship rel: this.getRelationsToThisTaxon()){
			if (rel.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())){
				return true;
			}
		}
		return false;
	}

	
	
	/*
	 * DEALING WITH SYNONYMS
	 */
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

	public Iterator<Taxon> iterator() {
		return new TaxonIterator(this.getTaxonomicChildren());

	}
	// inner iterator class for the iterable interface
	private class TaxonIterator implements Iterator<Taxon> {
		   private Taxon[] items;
		   private int i= 0;
		   public TaxonIterator(Set<Taxon> items) {
		      // check for null being passed in etc.
		      this.items= items.toArray(new Taxon[0]);
		   }
		   // interface implementation
		   public boolean hasNext() { return i < items.length; }
		   public Taxon next() { return items[i++]; }
		   public void remove() { throw new UnsupportedOperationException(); }
	}
	
}