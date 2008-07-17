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
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
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
 * build a taxonomic tree according to the opinion of the {@link reference.ReferenceBase reference}.
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
	 * concerning <i>this</i> taxon.
	 * 
	 * @see #removeDescription(TaxonDescription)
	 * @see #addDescription(TaxonDescription)
	 * @see description.TaxonDescription#getTaxon()
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
	 * of taxon descriptions assigned to <i>this</i> (accepted/correct) taxon.
	 * Due to bidirectionality the content of the {@link description.TaxonDescription#getTaxon() taxon attribute} of the
	 * taxon description itself will be replaced with <i>this</i> taxon. The taxon
	 * description will also be removed from the set of taxon descriptions
	 * assigned to its previous taxon. 
	 *
	 * @param  description	the taxon description to be added for <i>this</i> taxon
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
	 * Removes one element from the set of {@link description.TaxonDescription taxon descriptions} assigned
	 * to <i>this</i> (accepted/correct) taxon. Due to bidirectionality the content of
	 * the {@link description.TaxonDescription#getTaxon() taxon attribute} of the taxon description
	 * itself will be set to "null".
	 *
	 * @param  description  the taxon description which should be removed
	 * @see     		  	#getDescriptions()
	 * @see     		  	#addDescription(TaxonDescription)
	 * @see 			  	description.TaxonDescription#getTaxon()
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


	/** 
	 * Returns the set of all {@link SynonymRelationship synonym relationships}
	 * in which <i>this</i> ("accepted/correct") taxon is involved. <i>This</i> taxon can only
	 * be the target of these synonym relationships.
	 *  
	 * @see    #addSynonymRelation(SynonymRelationship)
	 * @see    #removeSynonymRelation(SynonymRelationship)
	 * @see    #getSynonyms()
	 */
	@OneToMany(mappedBy="relatedTo", fetch=FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<SynonymRelationship> getSynonymRelations() {
		return synonymRelations;
	}
	/** 
	 * @see    #getSynonymRelations()
	 * @see    #addSynonymRelation(SynonymRelationship)
	 */
	protected void setSynonymRelations(Set<SynonymRelationship> synonymRelations) {
		this.synonymRelations = synonymRelations;
	}
	/**
	 * Adds an existing {@link SynonymRelationship synonym relationship} to the set of
	 * {@link #getSynonymRelations() synonym relationships} assigned to <i>this</i> taxon. If the
	 * the target of the synonym relationship does not match with <i>this</i> taxon
	 * no addition will be carried out.
	 * 
	 * @param synonymRelation	the synonym relationship to be added to <i>this</i> taxon's
	 * 							synonym relationships set
	 * @see    	   				#getSynonymRelations()
	 * @see    	   				#addSynonym(Synonym, SynonymRelationshipType)
	 * @see    	   				#addSynonym(Synonym, SynonymRelationshipType, ReferenceBase, String)
	 * @see    	   				#addSynonymName(TaxonNameBase, SynonymRelationshipType)
	 * @see    	   				#addSynonymName(TaxonNameBase, SynonymRelationshipType, ReferenceBase, String)
	 */
	protected void addSynonymRelation(SynonymRelationship synonymRelation) {
		this.synonymRelations.add(synonymRelation);
	}
	/** 
	 * Removes one element from the set of {@link SynonymRelationship synonym relationships} assigned
	 * to <i>this</i> (accepted/correct) taxon. Due to bidirectionality the given
	 * synonym relationship will also be removed from the set of synonym
	 * relationships assigned to the {@link Synonym#getSynonymRelations() synonym} involved in the
	 * relationship. Furthermore the content of
	 * the {@link SynonymRelationship#getAcceptedTaxon() accepted taxon attribute} and of the
	 * {@link SynonymRelationship#getSynonym() synonym attribute} within the synonym relationship
	 * itself will be set to "null".
	 *
	 * @param  synonymRelation  the synonym relationship which should be deleted
	 * @see     		  		#getSynonymRelations()
	 * @see     		  		#addSynonymRelation(SynonymRelationship)
	 * @see 			  		#removeSynonym(Synonym)
	 */
	public void removeSynonymRelation(SynonymRelationship synonymRelation) {
		synonymRelation.setAcceptedTaxon(null);
		Synonym synonym = synonymRelation.getSynonym();
		if (synonym != null){
			synonymRelation.setSynonym(null);
			synonym.removeSynonymRelation(synonymRelation);
		}
		this.synonymRelations.remove(synonymRelation);
	}

	
	/** 
	 * Returns the set of all {@link TaxonRelationship taxon relationships}
	 * between two taxa in which <i>this</i> taxon is involved as a source.
	 *  
	 * @see    #getRelationsToThisTaxon()
	 * @see    #getTaxonRelations()
	 */
	@OneToMany(mappedBy="relatedFrom", fetch=FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN})
	public Set<TaxonRelationship> getRelationsFromThisTaxon() {
		return relationsFromThisTaxon;
	}
	/** 
	 * @see    #getRelationsFromThisTaxon()
	 */
	protected void setRelationsFromThisTaxon(
			Set<TaxonRelationship> relationsFromThisTaxon) {
		this.relationsFromThisTaxon = relationsFromThisTaxon;
	}


	/** 
	 * Returns the set of all {@link TaxonRelationship taxon relationships}
	 * between two taxa in which <i>this</i> taxon is involved as a target.
	 *  
	 * @see    #getRelationsFromThisTaxon()
	 * @see    #getTaxonRelations()
	 */
	@OneToMany(mappedBy="relatedTo", fetch=FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN})
	public Set<TaxonRelationship> getRelationsToThisTaxon() {
		return relationsToThisTaxon;
	}
	/** 
	 * @see    #getRelationsToThisTaxon()
	 */
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

	/** 
	 * Returns the set of all {@link TaxonRelationship taxon relationships}
	 * between two taxa in which <i>this</i> taxon is involved either as a source or
	 * as a target.
	 *  
	 * @see    #getRelationsFromThisTaxon()
	 * @see    #getRelationsToThisTaxon()
	 */
	@Transient
	public Set<TaxonRelationship> getTaxonRelations() {
		Set<TaxonRelationship> rels = new HashSet<TaxonRelationship>();
		rels.addAll(getRelationsToThisTaxon());
		rels.addAll(getRelationsFromThisTaxon());
		return rels;
	}
	/** 
	 * Removes one {@link TaxonRelationship taxon relationship} from one of both sets of
	 * {@link #getTaxonRelations() taxon relationships} in which <i>this</i> taxon is involved
	 * either as a {@link #getRelationsFromThisTaxon() source} or as a {@link #getRelationsToThisTaxon() target}.
	 * The taxon relationship will also be removed from one of both sets
	 * belonging to the second taxon involved. Furthermore the inherited RelatedFrom and
	 * RelatedTo attributes of the given taxon relationship will be nullified.<P>
	 * If the taxon relationship concerns the taxonomic tree possible
	 * modifications of the {@link #getTaxonomicParent() parent taxon} or of the number of
	 * {@link #getTaxonomicChildrenCount() childrens} will be stored.
	 *
	 * @param  rel  the taxon relationship which should be removed from one
	 * 				of both sets
	 * @see    		#getTaxonRelations()
	 * @see    	    #getTaxonomicParent()
	 * @see    	    #getTaxonomicChildrenCount()
	 * @see    		common.RelationshipBase#getRelatedFrom()
	 * @see    		common.RelationshipBase#getRelatedTo()
	 * 
	 */
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

	/**
	 * Adds an existing {@link TaxonRelationship taxon relationship} either to the set of
	 * {@link #getRelationsToThisTaxon() taxon relationships to <i>this</i> taxon} or to the set of
	 * {@link #getRelationsFromThisTaxon() taxon relationships from <i>this</i> taxon}. If neither the
	 * source nor the target of the taxon relationship match with <i>this</i> taxon
	 * no addition will be carried out. The taxon relationship will also be
	 * added to the second taxon involved in the given relationship.<P>
	 * If the taxon relationship concerns the taxonomic tree possible
	 * modifications of the {@link #getTaxonomicParent() parent taxon} or of the number of
	 * {@link #getTaxonomicChildrenCount() childrens} will be stored.
	 * 
	 * @param rel  the taxon relationship to be added to one of <i>this</i> taxon's taxon relationships sets
	 * @see    	   #addTaxonRelation(Taxon, TaxonRelationshipType, ReferenceBase, String)
	 * @see    	   #getTaxonRelations()
	 * @see    	   #getRelationsFromThisTaxon()
	 * @see    	   #getRelationsToThisTaxon()
	 * @see    	   #getTaxonomicParent()
	 * @see    	   #getTaxonomicChildrenCount()
	 */
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
	
	/**
	 * Creates a new {@link TaxonRelationship taxon relationship} instance where <i>this</i> taxon
	 * plays the source role and adds it to the set of
	 * {@link #getRelationsFromThisTaxon() "taxon relationships from"} belonging to <i>this</i> taxon.
	 * The taxon relationship will also be added to the set of taxon
	 * relationships to the second taxon involved in the created relationship.<P>
	 * If the taxon relationship concerns the taxonomic tree possible
	 * modifications of the {@link #getTaxonomicParent() parent taxon} or of the number of
	 * {@link #getTaxonomicChildrenCount() childrens} will be stored.
	 * 
	 * @param toTaxon		the taxon which plays the target role in the new taxon relationship
	 * @param type			the taxon relationship type for the new taxon relationship
	 * @param citation		the reference source for the new taxon relationship
	 * @param microcitation	the string with the details describing the exact localisation within the reference
	 * @see    	   			#addTaxonRelation(TaxonRelationship)
	 * @see    	   			#getTaxonRelations()
	 * @see    	   			#getRelationsFromThisTaxon()
	 * @see    	   			#getRelationsToThisTaxon()
	 * @see    	   			#getTaxonomicParent()
	 * @see    	   			#getTaxonomicChildrenCount()
	 */
	public void addTaxonRelation(Taxon toTaxon, TaxonRelationshipType type, ReferenceBase citation, String microcitation) {
		TaxonRelationship rel = new TaxonRelationship(this, toTaxon, type, citation, microcitation);
	}
	/**
	 * Creates a new {@link TaxonRelationship taxon relationship} (with {@link TaxonRelationshipType taxon relationship type}
	 * "misapplied name for") instance where <i>this</i> taxon plays the target role
	 * and adds it to the set of {@link #getRelationsToThisTaxon() taxon relationships to <i>this</i> taxon}.
	 * The taxon relationship will also be added to the set of taxon
	 * relationships to the other (misapplied name) taxon involved in the created relationship.
	 * 
	 * @param misappliedNameTaxon	the taxon which plays the target role in the new taxon relationship
	 * @param citation				the reference source for the new taxon relationship
	 * @param microcitation			the string with the details describing the exact localisation within the reference
	 * @see    	   					#getMisappliedNames()
	 * @see    	   					#addTaxonRelation(Taxon, TaxonRelationshipType, ReferenceBase, String)
	 * @see    	   					#addTaxonRelation(TaxonRelationship)
	 * @see    	   					#getTaxonRelations()
	 * @see    	   					#getRelationsFromThisTaxon()
	 * @see    	   					#getRelationsToThisTaxon()
	 */
	public void addMisappliedName(Taxon misappliedNameTaxon, ReferenceBase citation, String microcitation) {
		misappliedNameTaxon.addTaxonRelation(this, TaxonRelationshipType.MISAPPLIEDNAMEFOR(), citation, microcitation);
	}

	
	/**
	 * Creates a new {@link TaxonRelationship taxon relationship} (with {@link TaxonRelationshipType taxon relationship type}
	 * "taxonomically included in") instance where <i>this</i> taxon plays the target
	 * role (parent) and adds it to the set of
	 * {@link #getRelationsToThisTaxon() "taxon relationships to"} belonging to <i>this</i> taxon.
	 * The taxon relationship will also be added to the set of
	 * {@link #getRelationsFromThisTaxon() "taxon relationships from"} belonging to the second taxon
	 * (child) involved in the created relationship.<P>
	 * Since the taxon relationship concerns the modifications
	 * of the number of {@link #getTaxonomicChildrenCount() childrens} for <i>this</i> taxon and
	 * of the {@link #getTaxonomicParent() parent taxon} for the child taxon will be stored.
	 * The {@link name.Rank rank} of the taxon name used as a parent taxon must be higher
	 * than the rank of the taxon name used as a child taxon.
	 * 
	 * @param child			the taxon which plays the source role (child) in the new taxon relationship
	 * @param citation		the reference source for the new taxon relationship
	 * @param microcitation	the string with the details describing the exact localisation within the reference
	 * @see    	   			#setTaxonomicParent(Taxon, ReferenceBase, String)
	 * @see    	   			#addTaxonRelation(Taxon, TaxonRelationshipType, ReferenceBase, String)
	 * @see    	   			#addTaxonRelation(TaxonRelationship)
	 * @see    	   			#getTaxonRelations()
	 * @see    	   			#getRelationsFromThisTaxon()
	 * @see    	   			#getRelationsToThisTaxon()
	 * @see    	   			#getTaxonomicParent()
	 * @see    	   			#getTaxonomicChildrenCount()
	 */
	@Transient
	public void addTaxonomicChild(Taxon child, ReferenceBase citation, String microcitation){
		if (child == null){
			throw new NullPointerException("Child Taxon is 'null'");
		}else{
			child.setTaxonomicParent(this, citation, microcitation);
		}
	}
	/** 
	 * Removes one {@link TaxonRelationship taxon relationship} with {@link TaxonRelationshipType taxon relationship type}
	 * "taxonomically included in" and with the given child taxon playing the
	 * source role from the set of {@link #getRelationsToThisTaxon() "taxon relationships to"} belonging
	 * to <i>this</i> taxon. The taxon relationship will also be removed from the set
	 * of {@link #getRelationsFromThisTaxon() "taxon relationships from"} belonging to the child taxon.
	 * Furthermore the inherited RelatedFrom and RelatedTo attributes of the
	 * taxon relationship will be nullified.<P>
	 * Since the taxon relationship concerns the taxonomic tree modifications
	 * of the number of {@link #getTaxonomicChildrenCount() childrens} for <i>this</i> taxon and
	 * of the {@link #getTaxonomicParent() parent taxon} for the child taxon will be stored.
	 *
	 * @param  child	the taxon playing the source role in the relationship to be removed
	 * @see    	    	#removeTaxonRelation(TaxonRelationship)
	 * @see    			#getRelationsToThisTaxon()
	 * @see    			#getRelationsFromThisTaxon()
	 * @see    	    	#getTaxonomicParent()
	 * @see    	    	#getTaxonomicChildrenCount()
	 * @see    			common.RelationshipBase#getRelatedFrom()
	 * @see    			common.RelationshipBase#getRelatedTo()
	 * 
	 */
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
	 * Returns the taxon which is the next higher taxon (parent) of <i>this</i> taxon
	 * within the taxonomic tree and which is stored in the
	 * TaxonomicParentCache attribute. Each taxon can have only one parent taxon.
	 * The child taxon and the parent taxon play the source respectively the
	 * target role in one {@link TaxonRelationship taxon relationship} with
	 * {@link TaxonRelationshipType taxon relationship type} "taxonomically included in".
	 * The {@link name.Rank rank} of the taxon name used as a parent taxon must be higher
	 * than the rank of the taxon name used as a child taxon.
	 * 
	 * @see  #setTaxonomicParent(Taxon, ReferenceBase, String)
	 * @see  #getTaxonomicChildren()
	 * @see  #getTaxonomicChildrenCount()
	 * @see  #getRelationsFromThisTaxon()
	 */
	@Transient
	public Taxon getTaxonomicParent() {
		return getTaxonomicParentCache();
	}
	/**
	 * Replaces both the taxonomic parent cache with the given new parent taxon
	 * and the corresponding taxon relationship with a new {@link TaxonRelationship taxon relationship}
	 * (with {@link TaxonRelationshipType taxon relationship type} "taxonomically included in") instance.
	 * In the new taxon relationship <i>this</i> taxon plays the source role (child).
	 * This method creates and adds the new taxon relationship to the set of
	 * {@link #getRelationsFromThisTaxon() "taxon relationships from"} belonging to <i>this</i> taxon.
	 * The taxon relationship will also be added to the set of
	 * {@link #getRelationsToThisTaxon() "taxon relationships to"} belonging to the second taxon
	 * (parent) involved in the new relationship.<P>
	 * Since the taxon relationship concerns the taxonomic tree modifications
	 * of the {@link #getTaxonomicParent() parent taxon} for <i>this</i> taxon and of the number of
	 * {@link #getTaxonomicChildrenCount() childrens} for the child taxon will be stored.
	 * 
	 * @param newParent		the taxon which plays the target role (parent) in the new taxon relationship
	 * @param citation		the reference source for the new taxon relationship
	 * @param microcitation	the string with the details describing the exact localisation within the reference
	 * @see    	   			#removeTaxonRelation(TaxonRelationship)
	 * @see    	   			#getTaxonomicParent()
	 * @see    	   			#addTaxonRelation(Taxon, TaxonRelationshipType, ReferenceBase, String)
	 * @see    	   			#addTaxonRelation(TaxonRelationship)
	 * @see    	   			#getTaxonRelations()
	 * @see    	   			#getRelationsFromThisTaxon()
	 * @see    	   			#getRelationsToThisTaxon()
	 * @see    	   			#getTaxonomicChildrenCount()
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
	 * Returns the set of taxa which have <i>this</i> taxon as next higher taxon
	 * (parent) within the taxonomic tree. Each taxon can have several child
	 * taxa. The child taxon and the parent taxon play the source respectively
	 * the target role in one {@link TaxonRelationship taxon relationship} with
	 * {@link TaxonRelationshipType taxon relationship type} "taxonomically included in".
	 * The {@link name.Rank rank} of the taxon name used as a parent taxon must be higher
	 * than the rank of the taxon name used as a child taxon.
	 * 
	 * @see  #getTaxonomicParent()
	 * @see  #addTaxonomicChild(Taxon, ReferenceBase, String)
	 * @see  #getTaxonomicChildrenCount()
	 * @see  #getRelationsToThisTaxon()
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
	 * Returns the number of taxa which have <i>this</i> taxon as next higher taxon
	 * (parent) within the taxonomic tree and the number of which is stored in
	 * the TaxonomicChildrenCount attribute. Each taxon can have several child
	 * taxa. The child taxon and the parent taxon play the source respectively
	 * the target role in one {@link TaxonRelationship taxon relationship} with
	 * {@link TaxonRelationshipType taxon relationship type} "taxonomically included in".
	 * The {@link name.Rank rank} of the taxon name used as a parent taxon must be higher
	 * than the rank of the taxon name used as a child taxon.
	 * 
	 * @see  #getTaxonomicChildren()
	 * @see  #getRelationsToThisTaxon()
	 */
	public int getTaxonomicChildrenCount(){
		return taxonomicChildrenCount;
	}	
	
	
	/**
	 * @see  #getTaxonomicChildrenCount()
	 */
	private void setTaxonomicChildrenCount(int taxonomicChildrenCount) {
		this.taxonomicChildrenCount = taxonomicChildrenCount;
	}

	/**
	 * Returns the boolean value indicating whether <i>this</i> taxon has at least one
	 * taxonomic child taxon within the taxonomic tree (true) or not (false).
	 * 
	 * @see  #getTaxonomicChildrenCount()
	 * @see  #getTaxonomicChildren()
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
	 * Returns the boolean value indicating whether <i>this</i> taxon has at least one
	 * {@link Synoynm synonym} (true) or not (false). If true the {@link #getSynonymRelations() set of synonym relationships}
	 * belonging to <i>this</i> ("accepted/correct") taxon is not empty .
	 * 
	 * @see  #getSynonymRelations()
	 * @see  #getSynonyms()
	 * @see  #getSynonymNames()
	 * @see  #removeSynonym(Synonym)
	 * @see  SynonymRelationship
	 */
	@Transient
	public boolean hasSynonyms(){
		return this.getSynonymRelations().size() > 0;
	}

	
	/**
	 * Returns the boolean value indicating whether <i>this</i> taxon is at least
	 * involved in one {@link #getTaxonRelations() taxon relationship} between
	 * two taxa (true), either as a source or as a target, or not (false).
	 * 
	 * @see  #getTaxonRelations()
	 * @see  #getRelationsToThisTaxon()
	 * @see  #getRelationsFromThisTaxon()
	 * @see  #removeTaxonRelation(TaxonRelationship)
	 * @see  TaxonRelationship
	 */
	@Transient
	public boolean hasTaxonRelationships(){
		return this.getTaxonRelations().size() > 0;
	}

	/*
	 * MISAPPLIED NAMES
	 */
	/**
	 * Creates a new {@link TaxonRelationship taxon relationship} (with {@link TaxonRelationshipType taxon relationship type}
	 * "misapplied name for") instance where <i>this</i> taxon plays the target role
	 * and adds it to the set of {@link #getRelationsToThisTaxon() taxon relationships to <i>this</i> taxon}.
	 * The taxon relationship will also be added to the set of taxon
	 * relationships to the other (misapplied name) taxon involved in the created relationship.
	 * 
	 * @param misappliedNameTaxon	the taxon which plays the target role in the new taxon relationship
	 * @param citation				the reference source for the new taxon relationship
	 * @param microcitation			the string with the details describing the exact localisation within the reference
	 * @see    	   					#getMisappliedNames()
	 * @see    	   					#addTaxonRelation(Taxon, TaxonRelationshipType, ReferenceBase, String)
	 * @see    	   					#addTaxonRelation(TaxonRelationship)
	 * @see    	   					#getTaxonRelations()
	 * @see    	   					#getRelationsFromThisTaxon()
	 * @see    	   					#getRelationsToThisTaxon()
	 */
	/** 
	 * Returns the set of taxa playing the source role in {@link TaxonRelationship taxon relationships}
	 * (with {@link TaxonRelationshipType taxon relationship type} "misapplied name for") where
	 * <i>this</i> taxon plays the target role. A misapplied name is a taxon the
	 * {@link name.TaxonNameBase taxon name} of which has been erroneously used
	 * by the {@link TaxonBase#getSec() taxon reference} to denominate the same taxonomic group
	 * as the one meant by <i>this</i> ("accepted/correct") taxon. 
	 * 
	 * @see  #getTaxonRelations()
	 * @see  #getRelationsToThisTaxon()
	 * @see  #addMisappliedName(Taxon, ReferenceBase, String)
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
	 * Adds a synonym as a Synonym to <i>this</i> Taxon using the defined synonym relationship type.<BR>
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