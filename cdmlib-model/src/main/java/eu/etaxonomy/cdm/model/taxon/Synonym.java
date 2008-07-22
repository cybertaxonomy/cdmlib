/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.IRelated;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The class for synonyms: these are {@link TaxonBase taxa} the {@link name.TaxonNameBase taxon names}
 * of which are not used by the {@link TaxonBase#getSec() reference} to designate a real
 * taxon but are mentioned as taxon names that were oder are used by some other
 * unspecified references to designate (at least to some extent) the same
 * particular real taxon. Synonyms that are involved in no
 * {@link SynonymRelationship synonym relationship} are actually meaningless.<BR>
 * Splitting taxa in "accepted/correct" and "synonyms"
 * makes it easier to handle particular relationships between
 * ("accepted/correct") {@link Taxon taxa} on the one hand and between ("synonym") taxa
 * and ("accepted/correct") taxa on the other.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:55
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Synonym", propOrder = {
    "synonymRelations"
})
@XmlRootElement(name = "Synonym")
@Entity
public class Synonym extends TaxonBase implements IRelated<SynonymRelationship>{
	
	static Logger logger = Logger.getLogger(Synonym.class);

	//@XmlTransient
	// Don't need the synonym relations here since they are stored at taxon side?
	@XmlElementWrapper(name = "SynonymRelations")
	@XmlElement(name = "SynonymRelationship")
	private Set<SynonymRelationship> synonymRelations = new HashSet<SynonymRelationship>();

	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty synonym instance.
	 * 
	 * @see 	#Synonym(TaxonNameBase, ReferenceBase)
	 */
	//TODO should be private, but still produces Spring init errors
	public Synonym(){
	}
	
	/** 
	 * Class constructor: creates a new synonym instance with
	 * the {@link name.TaxonNameBase taxon name} used and the {@link reference.ReferenceBase reference}
	 * using it as a synonym and not as an ("accepted/correct") {@link Taxon taxon}.
	 * 
	 * @param  taxonNameBase	the taxon name used
	 * @param  sec				the reference using the taxon name
	 * @see    					Synonym#Synonym(TaxonNameBase, ReferenceBase)
	 */
	public Synonym(TaxonNameBase taxonNameBase, ReferenceBase sec){
		super(taxonNameBase, sec);
	}
	 
	//********* METHODS **************************************/

	/** 
	 * Creates a new synonym instance with
	 * the {@link name.TaxonNameBase taxon name} used and the {@link reference.ReferenceBase reference}
	 * using it as a synonym and not as an ("accepted/correct") {@link Taxon taxon}.
	 * 
	 * @param  taxonNameBase	the taxon name used
	 * @param  sec				the reference using the taxon name
	 * @see    					#Synonym(TaxonNameBase, ReferenceBase)
	 */
	public static Synonym NewInstance(TaxonNameBase taxonName, ReferenceBase sec){
		Synonym result = new Synonym(taxonName, sec);
		return result;
	}
	
	/** 
	 * Returns the set of all {@link SynonymRelationship synonym relationships}
	 * in which <i>this</i> synonym is involved. <i>This</i> synonym can only
	 * be the source within these synonym relationships. 
	 *  
	 * @see    #addSynonymRelation(SynonymRelationship)
	 * @see    #addRelationship(SynonymRelationship)
	 * @see    #removeSynonymRelation(SynonymRelationship)
	 */
	@OneToMany(mappedBy="relatedFrom", fetch=FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	public Set<SynonymRelationship> getSynonymRelations() {
		return synonymRelations;
	}
	/** 
	 * @see    #getSynonymRelations()
	 * @see    #addRelationship(SynonymRelationship)
	 */
	protected void setSynonymRelations(Set<SynonymRelationship> synonymRelations) {
		this.synonymRelations = synonymRelations;
	}
	/**
	 * Adds an existing {@link SynonymRelationship synonym relationship} to the set of
	 * {@link #getSynonymRelations() synonym relationships} assigned to <i>this</i> synonym. If
	 * the source of the synonym relationship does not match with <i>this</i>
	 * synonym no addition will be carried out.<BR>
	 * This methods does the same as the {@link #addRelationship() addRelationship} method.
	 * 
	 * @param synonymRelation	the synonym relationship to be added to <i>this</i> synonym's
	 * 							synonym relationships set
	 * @see    	   				#addRelationship(SynonymRelationship)
	 * @see    	   				#getSynonymRelations()
	 * @see    	   				#removeSynonymRelation(SynonymRelationship)
	 */
	protected void addSynonymRelation(SynonymRelationship synonymRelation) {
		this.synonymRelations.add(synonymRelation);
	}
	/** 
	 * Removes one element from the set of {@link SynonymRelationship synonym relationships} assigned
	 * to <i>this</i> synonym. Due to bidirectionality the given
	 * synonym relationship will also be removed from the set of synonym
	 * relationships assigned to the {@link Taxon#getSynonymRelations() taxon} involved in the
	 * relationship. Furthermore the content of
	 * the {@link SynonymRelationship#getAcceptedTaxon() accepted taxon attribute} and of the
	 * {@link SynonymRelationship#getSynonym() synonym attribute} within the synonym relationship
	 * itself will be set to "null".
	 *
	 * @param  synonymRelation  the synonym relationship which should be deleted
	 * @see     		  		#getSynonymRelations()
	 * @see     		  		#addRelationship(SynonymRelationship)
	 * @see 			  		#removeSynonym(Synonym)
	 */
	public void removeSynonymRelation(SynonymRelationship synonymRelation) {
		synonymRelation.setSynonym(null);
		Taxon taxon = synonymRelation.getAcceptedTaxon();
		if (taxon != null){
			synonymRelation.setAcceptedTaxon(null);
			taxon.removeSynonymRelation(synonymRelation);
		}
		this.synonymRelations.remove(synonymRelation);
	}
	
	
	/**
	 * Adds an existing {@link SynonymRelationship synonym relationship} to the set of
	 * {@link #getSynonymRelations() synonym relationships} assigned to <i>this</i> synonym. If
	 * the source of the synonym relationship does not match with <i>this</i>
	 * synonym no addition will be carried out.<BR>
	 * This methods does the same as the {@link #addSynonymRelation() addSynonymRelation} method.
	 * 
	 * @param synonymRelation	the synonym relationship to be added to <i>this</i> synonym's
	 * 							synonym relationships set
	 * @see    	   				#addSynonymRelation(SynonymRelationship)
	 * @see    	   				#getSynonymRelations()
	 * @see    	   				#removeSynonymRelation(SynonymRelationship)
	 */
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IRelated#addRelationship(eu.etaxonomy.cdm.model.common.RelationshipBase)
	 */
	public void addRelationship(SynonymRelationship rel){
		addSynonymRelation(rel);
	}


	/** 
	 * Returns the set of all ("accepted/correct") {@link Taxon taxa} involved in the same
	 * {@link SynonymRelationship synonym relationships} as <i>this</i> synonym.
	 * Each taxon is the target and <i>this</i> synonym is the source of a {@link SynonymRelationship synonym relationship}
	 * belonging to the {@link #getSynonymRelations() set of synonym relationships} assigned to
	 * <i>this</i> synonym. For a particular synonym there can be more than one
	 * ("accepted/correct") taxon only if the corresponding
	 * {@link SynonymRelationshipType synonym relationship type} is "pro parte synonym of".
	 *  
	 * @see    #getSynonymRelations()
	 * @see    #getRelationType(Taxon)
	 * @see    SynonymRelationshipType#PRO_PARTE_SYNONYM_OF()
	 */
	@Transient
	public Set<Taxon> getAcceptedTaxa() {
		Set<Taxon>taxa=new HashSet<Taxon>();
		for (SynonymRelationship rel:getSynonymRelations()){
			taxa.add(rel.getAcceptedTaxon());
		}
		return taxa;
	}

	/** 
	 * Returns set of {@link SynonymRelationshipType synonym relationship types} of the
	 * {@link SynonymRelationship synonym relationships} where the {@link SynonymRelationship#getSynonym() synonym}
	 * is <i>this</i> synonym and the {@link SynonymRelationship#getAcceptedTaxon() taxon}
	 * is the given one. "Null" is returned if the given taxon is "null" or if
	 * no synonym relationship exists from <i>this</i> synonym to the
	 * given taxon.
	 *  
	 * @param taxon	the ("accepted/correct") taxon to which a synonym relationship 
	 * 				from <i>this</i> synonym should point 
	 * @see    		#getSynonymRelations()
	 * @see    		#getAcceptedTaxa()
	 */
	@Transient
	//TODO	should return a Set<SynonymRelationshipType> since there might be more than one relation
	//		between the synonym and the taxon: see Taxon#removeSynonym(Synonym)
	public Set<SynonymRelationshipType> getRelationType(Taxon taxon){
		Set<SynonymRelationshipType> result = new HashSet<SynonymRelationshipType>();
		if (taxon == null ){
			return result;
		}
		for (SynonymRelationship rel : getSynonymRelations()){
			Taxon acceptedTaxon = rel.getAcceptedTaxon();
			if (taxon.equals(acceptedTaxon)){
				result.add(rel.getType());
			}
		}
		return result;
	}
}