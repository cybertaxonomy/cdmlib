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
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatus;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.lang.reflect.Method;
import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * The class for "accepted/correct" {@link TaxonBase taxa} (only these taxa can
 * build a taxonomical tree according to the opinion of the {@link reference.ReferenceBase reference}.
 * An {@link java.lang.Iterable interface} is supported to iterate through taxonomic children.
 * Splitting taxa in "accepted/correct" and "synonyms" makes it easier to handle
 * particular relationship between ("accepted/correct") taxa on the one hand
 * and between ("synonym") taxa and ("accepted/correct") taxa on the other.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:56
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Taxon", propOrder = {
    "taxonomicParentCache",
    "taxonomicChildrenCount",
    "synonymRelations",
    "relationsFromThisTaxon",
    "relationsToThisTaxon",
    "descriptions"
})
@XmlRootElement(name = "Taxon")
@Entity
public class Taxon extends TaxonBase implements Iterable<Taxon>, IRelated<RelationshipBase>{

	static Logger logger = Logger.getLogger(Taxon.class);

	@XmlElementWrapper(name = "Descriptions")
	@XmlElement(name = "Description")
	private Set<TaxonDescription> descriptions = new HashSet<TaxonDescription>();

	// all related synonyms
	@XmlElementWrapper(name = "SynonymRelations")
	@XmlElement(name = "SynonymRelationship")
	private Set<SynonymRelationship> synonymRelations = new HashSet<SynonymRelationship>();

	// all taxa relations with rel.fromTaxon==this
	@XmlElementWrapper(name = "RelationsFromThisTaxon")
	@XmlElement(name = "FromThisTaxonRelationship")
	private Set<TaxonRelationship> relationsFromThisTaxon = new HashSet<TaxonRelationship>();

	// all taxa relations with rel.toTaxon==this
	@XmlElementWrapper(name = "RelationsToThisTaxon")
	@XmlElement(name = "ToThisTaxonRelationship")
	private Set<TaxonRelationship> relationsToThisTaxon = new HashSet<TaxonRelationship>();

	// shortcut to the taxonomicIncluded (parent) taxon. Managed by the taxonRelations setter
	@XmlElement(name = "TaxonomicParentCache")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private Taxon taxonomicParentCache;

	//cached number of taxonomic children
	@XmlElement(name = "TaxonomicChildrenCount")
	private int taxonomicChildrenCount;

	private static Method methodDescriptionSetTaxon;
	
	
// ************* CONSTRUCTORS *************/	

	//TODO should be private, but still produces Spring init errors
	@Deprecated
	public Taxon(){
	}
	
	/** 
	 * Class constructor: creates a new (accepted/correct) taxon instance with
	 * the {@link name.TaxonNameBase taxon name} used and the {@link reference.ReferenceBase reference}
	 * using it.
	 * 
	 * @param  taxonNameBase	the taxon name used
	 * @param  sec				the reference using the taxon name
	 * @see    					TaxonBase#TaxonBase(TaxonNameBase, ReferenceBase)
	 */
	public Taxon(TaxonNameBase taxonNameBase, ReferenceBase sec){
		super(taxonNameBase, sec);
	}
	 
//********* METHODS **************************************/

	/** 
	 * Creates a new (accepted/correct) taxon instance with
	 * the {@link name.TaxonNameBase taxon name} used and the {@link reference.ReferenceBase reference}
	 * using it.
	 * 
	 * @param  taxonNameBase	the taxon name used
	 * @param  sec				the reference using the taxon name
	 * @see    					#Taxon(TaxonNameBase, ReferenceBase)
	 */
	public static Taxon NewInstance(TaxonNameBase taxonNameBase, ReferenceBase sec){
		Taxon result = new Taxon(taxonNameBase, sec);
		return result;
	}
	
	 
	/** 
	 * Returns the set of {@link description.TaxonDescription taxon descriptions}
	 * concerning this taxon.
	 * 
	 * @see description.TaxonDescription#getTaxon()
	 * @see #removeDescription(TaxonDescription)
	 * @see #addDescription(TaxonDescription)
	 */
	@OneToMany(mappedBy="taxon", fetch= FetchType.LAZY) 
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<TaxonDescription> getDescriptions() {
		return descriptions;
	}
	/** 
	 * @see #getDescriptions()
	 */
	protected void setDescriptions(Set<TaxonDescription> descriptions) {
		this.descriptions = descriptions;
	}
	/** 
	 * Adds a new {@link description.TaxonDescription taxon description} to the set
	 * of taxon descriptions assigned to this (accepted/correct) taxon.
	 * The {@link description.TaxonDescription#getTaxon() taxon} of the taxon description
	 * will be filled with this taxon. The taxon description must be removed
	 * from the set of taxon descriptions assigned to the previous taxon. 
	 *
	 * @param  description	the taxon description to be added for this taxon
	 * @see     		  	#getDescriptions()
	 * @see     		  	#removeDescription(TaxonDescription)
	 * @see 			  	description.TaxonDescription#getTaxon()
	 */
	public void addDescription(TaxonDescription description) {
		initMethods();
		if (description.getTaxon() != null){
			description.getTaxon().removeDescription(description);
		}
		//description.setTaxon(this) for not visible method
		this.invokeSetMethod(methodDescriptionSetTaxon, description);
		descriptions.add(description);
		
	}
	/** 
	 * Removes one element from the set of {@link description.TaxonDescription taxon descriptions} assigned to the
	 * to this taxon. The The {@link description.TaxonDescription#getTaxon() taxon} in the description itself will be nullified.
	 *
	 * @param  description  the taxon description which should be deleted
	 * @see     		  	#getDescriptions()
	 * @see     		  	#addDescription(TaxonDescription)
	 */
	public void removeDescription(TaxonDescription description) {
		initMethods();
		//description.setTaxon(null) for not visible method
		this.invokeSetMethodWithNull(methodDescriptionSetTaxon, description);
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


	@OneToMany(mappedBy="relatedTo", fetch=FetchType.LAZY)
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
	public void removeSynonymRelation(SynonymRelationship synonymRelation) {
		synonymRelation.setAcceptedTaxon(null);
		Synonym synonym = synonymRelation.getSynonym();
		if (synonym != null){
			synonymRelation.setSynonym(null);
			synonym.removeSynonymRelation(synonymRelation);
		}
		this.synonymRelations.remove(synonymRelation);
	}

	
	@OneToMany(mappedBy="relatedFrom", fetch=FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN})
	public Set<TaxonRelationship> getRelationsFromThisTaxon() {
		return relationsFromThisTaxon;
	}
	protected void setRelationsFromThisTaxon(
			Set<TaxonRelationship> relationsFromThisTaxon) {
		this.relationsFromThisTaxon = relationsFromThisTaxon;
	}


	@OneToMany(mappedBy="relatedTo", fetch=FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN})
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
		logger.warn("remove TaxonRelation");  //for testing only 
		this.relationsToThisTaxon.remove(rel);
		this.relationsFromThisTaxon.remove(rel);
		Taxon fromTaxon = rel.getFromTaxon();
		Taxon toTaxon = rel.getToTaxon();
		// check if this removes the taxonomical parent. If so, also remove shortcut to the higher taxon
		if (rel.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN()) ){
			if (fromTaxon != null && fromTaxon.equals(this)){
				this.setTaxonomicParentCache(null);
			}else if (toTaxon != null && toTaxon.equals(this)){
				this.setTaxonomicChildrenCount(computeTaxonomicChildrenCount());	
			}
		}
		//delete Relationship from other related Taxon
		if (fromTaxon != null && fromTaxon != this){
			rel.setToTaxon(null);  //remove this Taxon from relationship
			fromTaxon.removeTaxonRelation(rel);
		}
		if (toTaxon != null && toTaxon != this){
			rel.setFromTaxon(null); //remove this Taxon from relationship
			toTaxon.removeTaxonRelation(rel);
		}
	}

	public void addTaxonRelation(TaxonRelationship rel) {
		if (rel!=null && rel.getType()!=null && !getTaxonRelations().contains(rel) ){
			Taxon toTaxon=rel.getToTaxon();
			Taxon fromTaxon=rel.getFromTaxon();
			if ( this.equals(toTaxon) || this.equals(fromTaxon) ){
				if (this.equals(fromTaxon)){
					relationsFromThisTaxon.add(rel);
					// also add relation to other taxon object
					if (toTaxon!=null){
						toTaxon.addTaxonRelation(rel);
					}
					// check if this sets the taxonomical parent. If so, remember a shortcut to this taxon
					if (rel.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN()) && toTaxon!=null ){
						this.setTaxonomicParentCache(toTaxon);
					}
				}else if (this.equals(toTaxon)){
					relationsToThisTaxon.add(rel);
					// also add relation to other taxon object
					if (fromTaxon!=null){
						fromTaxon.addTaxonRelation(rel);
					}
					if (rel.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN()) && fromTaxon!=null ){
						this.taxonomicChildrenCount++;
					}
					
				}
			}
		}	
	}
		
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IRelated#addRelationship(eu.etaxonomy.cdm.model.common.RelationshipBase)
	 */
	@Deprecated //for inner use by RelationshipBase only
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
	public void addMisappliedName(Taxon misappliedNameTaxon, ReferenceBase citation, String microcitation) {
		misappliedNameTaxon.addTaxonRelation(this, TaxonRelationshipType.MISAPPLIEDNAMEFOR(), citation, microcitation);
	}

	
	@Transient
	public void addTaxonomicChild(Taxon child, ReferenceBase citation, String microcitation){
		if (child == null){
			throw new NullPointerException("Child Taxon is 'null'");
		}else{
			child.setTaxonomicParent(this, citation, microcitation);
		}
	}
	@Transient
	public void removeTaxonomicChild(Taxon child){
		Set<TaxonRelationship> taxRels = this.getTaxonRelations();
		for (TaxonRelationship taxRel : taxRels ){
			if (taxRel.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN()) && taxRel.getFromTaxon().equals(child)){
				this.removeTaxonRelation(taxRel);
			}
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
	public void setTaxonomicParent(Taxon newParent, ReferenceBase citation, String microcitation){
		//remove previously existing parent relationship!!!
		Taxon oldParent = this.getTaxonomicParent();
		Set<TaxonRelationship> taxRels = this.getTaxonRelations();
		for (TaxonRelationship taxRel : taxRels ){
			if (taxRel.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN()) && taxRel.getToTaxon().equals(oldParent)){
				this.removeTaxonRelation(taxRel);
			}
		}
		//add new parent
		if (newParent != null){
			addTaxonRelation(newParent, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),citation,microcitation);
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
	 * Cached number of taxonomic children of this taxon.
	 *	@return
	 */
	public int getTaxonomicChildrenCount(){
		return taxonomicChildrenCount;
	}	
	
	
	/**
	 * @param hasTaxonomicChildren the hasTaxonomicChildren to set
	 */
	private void setTaxonomicChildrenCount(int taxonomicChildrenCount) {
		this.taxonomicChildrenCount = taxonomicChildrenCount;
	}

	/**
	 * @see getHasTaxonomicChildren() 
	 *	@return
	 */
	@Transient
	public boolean hasTaxonomicChildren(){
		return this.taxonomicChildrenCount > 0;
	}

	@Transient
	private int computeTaxonomicChildrenCount(){
		int count = 0;
		for (TaxonRelationship rel: this.getRelationsToThisTaxon()){
			if (rel.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())){
				count++;
			}
		}
		return count;
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
		return addHeterotypicSynonymName(synonymName, null, null, null);
	}

	
	/**
	 * Adds a taxon name to <i>this</i> taxon as a heterotypic synonym. <BR>
	 * The new synonym gets the same concept reference as <i>this</i> taxon.<BR>
	 * The name gets the homotypic group given as parameter <i>homotypicalGroup</i><BR>
	 * @param synonymName the TaxonNameBase to add as a heterotypic synonym name
	 * @param homotypicSynonym an existing heterotypic (to <i>this</i> taxon) synonym that has the same type (is homotypic) as the new synonym 
	 * @return The newly created synonym relationship
	 */
	public SynonymRelationship addHeterotypicSynonymName(TaxonNameBase synonymName, HomotypicalGroup homotypicalGroup, ReferenceBase citation, String microCitation){
		Synonym synonym = Synonym.NewInstance(synonymName, this.getSec());
		if (homotypicalGroup != null){
			homotypicalGroup.addTypifiedName(synonymName);
		}
		return addSynonym(synonym, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF(), citation, microCitation);
	}
	
	/**
	 * Adds a taxon name to <i>this</i> taxon as a homotypic synonym. <BR>
	 * The added name gets the same homotypic group as <i>this</i> taxon.<BR>
	 * The new synonym gets the same concept reference as <i>this</i> taxon.<BR>
	 * @param synonymName the TaxonNameBase to add as a homotypic synonym name
	 * @return The newly created synonym relationship
	 */
	public SynonymRelationship addHomotypicSynonymName(TaxonNameBase synonymName, ReferenceBase citation, String microCitation){
		Synonym synonym = Synonym.NewInstance(synonymName, this.getSec());
		return addHomotypicSynonym(synonym, citation, microCitation);
	}
	
	/**
	 * Adds a taxon to <i>this</i> taxon as a homotypic synonym. <BR>
	 * The added synonym gets the same homotypic group as <i>this</i> taxon.<BR>
	 * @param synonymName the TaxonNameBase to add as a homotypic synonym name
	 * @return The newly created synonym relationship
	 */
	public SynonymRelationship addHomotypicSynonym(Synonym synonym, ReferenceBase citation, String microCitation){
		if (this.getName() != null){
			this.getName().getHomotypicalGroup().addTypifiedName(synonym.getName());
		}
		SynonymRelationship synRel = addSynonym(synonym, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF(), citation, microCitation);
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
	public List<Synonym> getHomotypicSynonymsByHomotypicGroup(){
		if (this.getHomotypicGroup() == null){
			return null;
		}else{
			return this.getHomotypicGroup().getSynonymsInGroup(this.getSec());
		}
	}
	
	@Transient
	public List<Synonym> getHomotypicSynonymsByHomotypicRelationship(){
		Set<SynonymRelationship> synonymRelations = this.getSynonymRelations(); 
		List<Synonym> result = new ArrayList<Synonym>();
		for(SynonymRelationship synonymRelation : synonymRelations) {
    		if(synonymRelation.getType().equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())){
				result.add(synonymRelation.getSynonym());
    		}
		}
		return result;
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
		List<HomotypicalGroup> list = getHomotypicSynonymyGroups();
		list.remove(this.getHomotypicGroup());
		//sort
		Map<Synonym, HomotypicalGroup> map = new HashMap<Synonym, HomotypicalGroup>();
		for (HomotypicalGroup homoGroup: list){
			List<Synonym> synonymList = homoGroup.getSynonymsInGroup(getSec());
			if (synonymList.size() > 0){
				map.put(synonymList.get(0), homoGroup);
			}
		}
		List<Synonym> keyList = new ArrayList<Synonym>();
		keyList.addAll(map.keySet());
		Collections.sort(keyList, new TaxonComparator());
		
		List<HomotypicalGroup> result = new ArrayList<HomotypicalGroup>();
		for(Synonym synonym: keyList){
			result.add(map.get(synonym));
		}
		//sort end
		return result;
	}	

}