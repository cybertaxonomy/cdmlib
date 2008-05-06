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
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;

import javax.persistence.*;

/**
 * An accepted potential taxon defined by the combination of a Name and a sec reference
 * {@link Iterable} interface is supported to iterate through taxonomic children
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:56
 */
@Entity
public class Taxon extends TaxonBase implements Iterable<Taxon>{
	static Logger logger = Logger.getLogger(Taxon.class);
	private Set<TaxonDescription> descriptions = new HashSet<TaxonDescription>();
	// all related synonyms
	private Set<SynonymRelationship> synonymRelations = new HashSet<SynonymRelationship>();
	// all taxa relations with rel.fromTaxon==this
	private Set<TaxonRelationship> relationsFromThisTaxon = new HashSet<TaxonRelationship>();
	// all taxa relations with rel.toTaxon==this
	private Set<TaxonRelationship> relationsToThisTaxon = new HashSet<TaxonRelationship>();
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


	@OneToMany(mappedBy="taxon", fetch= FetchType.EAGER)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<TaxonDescription> getDescriptions() {
		return descriptions;
	}
	protected void setDescriptions(Set<TaxonDescription> descriptions) {
		this.descriptions = descriptions;
	}
	public void addDescription(TaxonDescription description) {
		this.descriptions.add(description);
	}
	public void removeDescription(DescriptionBase description) {
		this.descriptions.remove(description);
	}


	//TODO FetchType (set to Eager because lazyLoading problem in TaxEditor, try to solve problem - 14.4.08)
	@OneToMany(mappedBy="acceptedTaxon", fetch=FetchType.EAGER)
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
	

	@OneToMany(mappedBy="fromTaxon", fetch=FetchType.EAGER)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<TaxonRelationship> getRelationsFromThisTaxon() {
		return relationsFromThisTaxon;
	}
	protected void setRelationsFromThisTaxon(
			Set<TaxonRelationship> relationsFromThisTaxon) {
		this.relationsFromThisTaxon = relationsFromThisTaxon;
	}


	//TODO FetchType (set to Eager because lazyLoading problem in TaxEditor, try to solve problem - 14.4.08)
	@OneToMany(mappedBy="toTaxon", fetch=FetchType.EAGER)
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
		Set<TaxonRelationship> rels = new HashSet<TaxonRelationship>();
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
				relationsFromThisTaxon.add(rel);
				// also add relation to other taxon object
				Taxon toTaxon=rel.getToTaxon();
				if (toTaxon!=null){
					toTaxon.addTaxonRelation(rel);
				}
				// check if this sets the taxonomical parent. If so, remember a shortcut to this taxon
				if (rel.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN()) && toTaxon!=null ){
					this.setTaxonomicParentCache(rel.getToTaxon());
				}
			}else if (rel.getToTaxon().equals(this)){
				relationsToThisTaxon.add(rel);
			}
		}
	}
	public void addTaxonRelation(Taxon toTaxon, TaxonRelationshipType type, ReferenceBase citation, String microcitation) {
		TaxonRelationship rel = new TaxonRelationship();
		rel.setToTaxon(toTaxon);
		rel.setFromTaxon(this);
		rel.setType(type);
		rel.setCitation(citation);
		rel.setCitationMicroReference(microcitation);
		this.addTaxonRelation(rel);
	}
	public void addMisappliedName(Taxon toTaxon, ReferenceBase citation, String microcitation) {
		addTaxonRelation(toTaxon, TaxonRelationshipType.MISAPPLIEDNAMEFOR(), citation, microcitation);
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
		// TODO: remove previously existing parent relationship!!!
		if (parent != null){
			addTaxonRelation(parent, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),citation,microcitation);
		}
	}

	/**
	 * @return
	 */
	@Transient
	public Set<Taxon> getTaxonomicChildren() {
		Set<Taxon> taxa = new HashSet<Taxon>();
		Set<TaxonRelationship> rels = this.getRelationsToThisTaxon();
		for (TaxonRelationship rel: rels){
			TaxonRelationshipType tt = rel.getType();
			TaxonRelationshipType incl = TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(); 
			if (tt.equals(incl)){
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
	
	/**
	 * @return
	 */
	@Transient
	public boolean hasSynonyms(){
		return this.getSynonymRelations().size() > 0;
	}

	
	/*
	 * MISAPPLIED NAMES
	 */
	@Transient
	public Set<Taxon> getMisappliedNames(){
		Set<Taxon> taxa = new HashSet<Taxon>();
		Set<TaxonRelationship> rels = this.getRelationsToThisTaxon();
		for (TaxonRelationship rel: rels){
			TaxonRelationshipType tt = rel.getType();
			TaxonRelationshipType incl = TaxonRelationshipType.MISAPPLIEDNAMEFOR(); 
			if (tt.equals(incl)){
				taxa.add(rel.getFromTaxon());
			}
		}
		return taxa;
	}
		
	
	/*
	 * DEALING WITH SYNONYMS
	 */
	@Transient
	public Set<Synonym> getSynonyms(){
		Set<Synonym> syns = new HashSet();
		for (SynonymRelationship rel: this.getSynonymRelations()){
			syns.add(rel.getSynonym());
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
			names.add(rel.getSynonym().getName());
		}
		return names;
	}
	/**
	 * ass a synonym to this taxon (a taxon can have multiple synonyms that should be proparte synonyms then!)
	 * The {@link SynonymRelationship} constructor immediately adds a relationship instance to both 
	 * the synonym and taxon instance!
	 * @param synonym
	 * @param synonymType
	 */
	public void addSynonym(Synonym synonym, SynonymRelationshipType synonymType){
		SynonymRelationship synonymRelationship = new SynonymRelationship(synonym, this, synonymType);
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<Taxon> iterator() {
		return new TaxonIterator(this.getTaxonomicChildren());
	}
	/**
	 * inner iterator class for the iterable interface
	 * @author m.doering
	 *
	 */
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
	
	@Transient
	public HomotypicalGroup getHomotypicGroup(){
		if (this.getName() == null){
			return null;
		}else{
			return this.getName().getHomotypicalGroup();
		}
	}
	
	@Transient
	public List<Synonym> getHomotypicSynonyms(){
		if (this.getHomotypicGroup() == null){
			return null;
		}else{
			return this.getHomotypicGroup().getSynonymsInGroup(this.getSec());
		}
	}
	
	@Transient
	public List<HomotypicalGroup> getHeterotypicSynonymyGroups(){
		List<HomotypicalGroup> result = new ArrayList<HomotypicalGroup>();
		for (TaxonNameBase n:this.getSynonymNames()){
			if (!result.contains(n.getHomotypicalGroup())){
				result.add(n.getHomotypicalGroup());
			}
		}
		// TODO: sort list according to date of first published name within each group
		return result;
	}	
}