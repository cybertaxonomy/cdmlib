/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.common.IRelated;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.lang.reflect.Method;
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
public class Taxon extends TaxonBase implements Iterable<Taxon>, IRelated<RelationshipBase>{
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

	private static Method methodDescriptionSetTaxon;
	
	

	/**
	 * Factory method
	 * @param taxonNameBase The TaxonNameBase that belongs to the new taxon
	 * @param sec The taxon concept reference that 
	 * @return
	 */
	public static Taxon NewInstance(TaxonNameBase taxonNameBase, ReferenceBase sec){
		Taxon result = new Taxon(taxonNameBase, sec);
		return result;
	}
	
	//TODO should be private, but still produces Spring init errors
	@Deprecated
	public Taxon(){
	}
	
	public Taxon(TaxonNameBase taxonNameBase, ReferenceBase sec){
		super(taxonNameBase, sec);
	}
	

	@OneToMany(mappedBy="taxon", fetch= FetchType.EAGER)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<TaxonDescription> getDescriptions() {
		return descriptions;
	}
	protected void setDescriptions(Set<TaxonDescription> descriptions) {
		this.descriptions = descriptions;
	}
	/**
	 * Adds a description to this taxon. Set the taxon property of description to this taxon.
	 * @param description
	 */
	public void addDescription(TaxonDescription description) {
		initMethods();
		this.invokeSetMethod(methodDescriptionSetTaxon, description);
		descriptions.add(description);
	}
	public void removeDescription(TaxonDescription description) {
		initMethods();
		this.invokeSetMethod(methodDescriptionSetTaxon, null);
		descriptions.remove(description);
	}

	private void initMethods(){
		if (methodDescriptionSetTaxon == null){
			try {
				methodDescriptionSetTaxon = TaxonDescription.class.getDeclaredMethod("setTaxon", Taxon.class);
				methodDescriptionSetTaxon.setAccessible(true);
			} catch (Exception e) {
				e.printStackTrace();
				//TODO handle exception
			}
		}
	}


	//TODO FetchType (set to Eager because lazyLoading problem in TaxEditor, try to solve problem - 14.4.08)
	@OneToMany(mappedBy="relatedTo", fetch=FetchType.EAGER)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<SynonymRelationship> getSynonymRelations() {
		return synonymRelations;
	}
	protected void setSynonymRelations(Set<SynonymRelationship> synonymRelations) {
		this.synonymRelations = synonymRelations;
	}
	protected void addSynonymRelation(SynonymRelationship synonymRelation) {
		this.synonymRelations.add(synonymRelation);
	}
	protected void removeSynonymRelation(SynonymRelationship synonymRelation) {
		synonymRelation.setAcceptedTaxon(null);
		Synonym synonym = synonymRelation.getSynonym();
		if (synonym != null){
			synonymRelation.setSynonym(null);
			synonym.removeSynonymRelation(synonymRelation);
		}
		this.synonymRelations.remove(synonymRelation);
	}
	

	@OneToMany(mappedBy="relatedFrom", fetch=FetchType.EAGER)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<TaxonRelationship> getRelationsFromThisTaxon() {
		return relationsFromThisTaxon;
	}
	protected void setRelationsFromThisTaxon(
			Set<TaxonRelationship> relationsFromThisTaxon) {
		this.relationsFromThisTaxon = relationsFromThisTaxon;
	}


	//TODO FetchType (set to Eager because lazyLoading problem in TaxEditor, try to solve problem - 14.4.08)
	@OneToMany(mappedBy="relatedTo", fetch=FetchType.EAGER)
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
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IRelated#addRelationship(eu.etaxonomy.cdm.model.common.RelationshipBase)
	 */
	public void addRelationship(RelationshipBase rel){
		if (rel instanceof TaxonRelationship){
			addTaxonRelation((TaxonRelationship)rel);
		}else if (rel instanceof SynonymRelationship){
			addSynonymRelation((SynonymRelationship)rel);
		}else{
			throw new ClassCastException("Wrong Relationsship type for Taxon.addRelationship");
		}
	}
	
	
	public void addTaxonRelation(Taxon toTaxon, TaxonRelationshipType type, ReferenceBase citation, String microcitation) {
		TaxonRelationship rel = new TaxonRelationship(this, toTaxon, type, citation, microcitation);
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
		Set<Synonym> syns = new HashSet<Synonym>();
		for (SynonymRelationship rel: this.getSynonymRelations()){
			syns.add(rel.getSynonym());
		}
		return syns;
	}
	@Transient
	public Set<Synonym> getSynonymsSortedByType(){
		// FIXME: need to sort synonyms according to type!!!
		logger.warn("getSynonymsSortedByType() not yet implemented");
		return getSynonyms();
	}
	@Transient
	public Set<TaxonNameBase> getSynonymNames(){
		Set<TaxonNameBase> names = new HashSet<TaxonNameBase>();
		for (SynonymRelationship rel: this.getSynonymRelations()){
			names.add(rel.getSynonym().getName());
		}
		return names;
	}
	/**
	 * Adds a synonym as a Synonym to this Taxon using the defined synonym relationship type.<BR>
	 * If you want to add further information to this relationship use the returned SynonymRelationship.
	 * @param synonym the Synoynm to add as a synonym
	 * @param synonymType the SynonymRelationshipType between <i>this</i> taxon and the synonym (e.g. homotypic, heterotypic, proparte ...)
	 * @return The newly created synonym relationship
	 */
	public SynonymRelationship addSynonym(Synonym synonym, SynonymRelationshipType synonymType){
		return addSynonym(synonym, synonymType, null, null);
	}
	public SynonymRelationship addSynonym(Synonym synonym, SynonymRelationshipType synonymType, ReferenceBase citation, String citationMicroReference){
		SynonymRelationship synonymRelationship = new SynonymRelationship(synonym, this, synonymType, citation, citationMicroReference);
		return synonymRelationship;
	}
	
	/**
	 * Adds a taxon name to <i>this</i> taxon as a heterotypic synonym.<BR>
	 * The new synonym gets the same concept reference as <i>this</i> taxon.
	 * @param synonymName the TaxonNameBase to add as a synonym name of the defined type. 
	 * @param synonymType the SynonymRelationshipType between <i>this</i> taxon and the synonym (e.g. homotypic, heterotypic, proparte ...)
	 * @return The newly created synonym relationship
	 */
	public SynonymRelationship addSynonymName(TaxonNameBase synonymName, SynonymRelationshipType synonymType){
		return addSynonymName(synonymName, synonymType, null, null);
	}
	public SynonymRelationship addSynonymName(TaxonNameBase synonymName, SynonymRelationshipType synonymType, ReferenceBase citation, String citationMicroReference){
		Synonym synonym = Synonym.NewInstance(synonymName, this.getSec());
		return addSynonym(synonym, synonymType, citation, citationMicroReference);
	}
	

	/**
	 * Adds a taxon name to <i>this</i> taxon as a heterotypic synonym. <BR>
	 * The new synonym gets the same concept reference as <i>this</i> taxon.<BR>
	 * @param synonymName the TaxonNameBase to add as a heterotypic synonym name
	 * @return The newly created synonym relationship
	 */
	public SynonymRelationship addHeterotypicSynonymName(TaxonNameBase synonymName){
		return addHeterotypicSynonymName(synonymName, null);
	}

	
	/**
	 * Adds a taxon name to <i>this</i> taxon as a heterotypic synonym. <BR>
	 * The new synonym gets the same concept reference as <i>this</i> taxon.<BR>
	 * The name gets the homotypic group given as parameter <i>homotypicalGroup</i><BR>
	 * @param synonymName the TaxonNameBase to add as a heterotypic synonym name
	 * @param homotypicSynonym an existing heterotypic (to <i>this</i> taxon) synonym that has the same type (is homotypic) as the new synonym 
	 * @return The newly created synonym relationship
	 */
	public SynonymRelationship addHeterotypicSynonymName(TaxonNameBase synonymName, HomotypicalGroup homotypicalGroup){
		Synonym synonym = Synonym.NewInstance(synonymName, this.getSec());
		if (homotypicalGroup != null){
			homotypicalGroup.addTypifiedName(synonymName);
		}
		return addSynonym(synonym, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
	}
	
	/**
	 * Adds a taxon name to <i>this</i> taxon as a homotypic synonym. <BR>
	 * The added name gets the same homotypic group as <i>this</i> taxon.<BR>
	 * The new synonym gets the same concept reference as <i>this</i> taxon.<BR>
	 * @param synonymName the TaxonNameBase to add as a homotypic synonym name
	 * @return The newly created synonym relationship
	 */
	public SynonymRelationship addHomotypicSynonymName(TaxonNameBase synonymName){
		Synonym synonym = Synonym.NewInstance(synonymName, this.getSec());
		if (this.getName() != null){
			this.getName().getHomotypicalGroup().addTypifiedName(synonymName);
		}
		SynonymRelationship synRel = addSynonym(synonym, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());
		return synRel;
	}
	
	/**
	 * Deletes all synonym relationships between <this>taxon and the given synonym
	 * @param synonym
	 */
	public void removeSynonym(Synonym synonym){
		Set<SynonymRelationship> synonymRelationships = new HashSet<SynonymRelationship>();
		synonymRelationships.addAll(this.getSynonymRelations());
		for(SynonymRelationship synonymRelationship : synonymRelationships){
			if (synonymRelationship.getAcceptedTaxon().equals(this) && synonymRelationship.getSynonym().equals(synonym)){
				this.removeSynonymRelation(synonymRelationship);
			}
		}
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
	public List<Synonym> getHomotypicSynonyms(){
		if (this.getHomotypicGroup() == null){
			return null;
		}else{
			return this.getHomotypicGroup().getSynonymsInGroup(this.getSec());
		}
	}
	
	/**
	 * Returns the List of all homotypic groups synonyms of this taxon belongs too.
	 * This includes the homotypic group of <i>this</i> taxon.
	 * @return
	 */
	@Transient
	public List<HomotypicalGroup> getHomotypicSynonymyGroups(){
		List<HomotypicalGroup> result = new ArrayList<HomotypicalGroup>();
		result.add(this.getHomotypicGroup());
		for (TaxonNameBase taxonNameBase :this.getSynonymNames()){
			if (!result.contains(taxonNameBase.getHomotypicalGroup())){
				result.add(taxonNameBase.getHomotypicalGroup());
			}
		}
		// TODO: sort list according to date of first published name within each group
		return result;
	}
	
	/**
	 * Returns the List of all homotypic groups heterotypic synonyms of this taxon belongs too.
	 * This does not include the homotypic group of <i>this</i> taxon.
	 * @return
	 */
	@Transient
	public List<HomotypicalGroup> getHeterotypicSynonymyGroups(){
		List<HomotypicalGroup> result = getHomotypicSynonymyGroups();
		result.remove(this.getHomotypicGroup());
		return result;
	}	

}