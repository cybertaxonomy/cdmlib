/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.model.common.IRelated;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.taxon.ITaxonCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.taxon.TaxonBaseDefaultCacheStrategy;
import eu.etaxonomy.cdm.validation.Level2;

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
@Indexed(index = "eu.etaxonomy.cdm.model.taxon.TaxonBase")
@Audited
@Configurable
public class Synonym extends TaxonBase<ITaxonCacheStrategy<Synonym>> implements IRelated<SynonymRelationship>{
	private static final long serialVersionUID = -454067515022159757L;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Synonym.class);

	// Don't need the synonym relations here since they are stored at taxon side?
	@XmlElementWrapper(name = "SynonymRelations")
	@XmlElement(name = "SynonymRelationship")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="relatedFrom", fetch=FetchType.LAZY, orphanRemoval=true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	@NotNull
	@NotEmpty(groups = Level2.class,message="{eu.etaxonomy.cdm.model.taxon.Synonym.noOrphanedSynonyms.message}")
	@Valid
	private Set<SynonymRelationship> synonymRelations = new HashSet<SynonymRelationship>();

	// ************* CONSTRUCTORS *************/
	/**
	 * Class constructor: creates a new empty synonym instance.
	 *
	 * @see 	#Synonym(TaxonNameBase, Reference)
	 */
	//TODO should be private, but still produces Spring init errors
	public Synonym(){
		this.cacheStrategy = new TaxonBaseDefaultCacheStrategy<Synonym>();
	}

	/**
	 * Class constructor: creates a new synonym instance with
	 * the {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name} used and the {@link eu.etaxonomy.cdm.model.reference.Reference reference}
	 * using it as a synonym and not as an ("accepted/correct") {@link Taxon taxon}.
	 *
	 * @param  taxonNameBase	the taxon name used
	 * @param  sec				the reference using the taxon name
	 * @see    					Synonym#Synonym(TaxonNameBase, Reference)
	 */
	public Synonym(TaxonNameBase taxonNameBase, Reference sec){
		super(taxonNameBase, sec);
		this.cacheStrategy = new TaxonBaseDefaultCacheStrategy<Synonym>();
	}

	//********* METHODS **************************************/

	/**
	 * Creates a new synonym instance with
	 * the {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name} used and the {@link eu.etaxonomy.cdm.model.reference.Reference reference}
	 * using it as a synonym and not as an ("accepted/correct") {@link Taxon taxon}.
	 *
	 * @param  taxonNameBase	the taxon name used
	 * @param  sec				the reference using the taxon name
	 * @see    					#Synonym(TaxonNameBase, Reference)
	 */
	public static Synonym NewInstance(TaxonNameBase taxonName, Reference sec){
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
	public Set<SynonymRelationship> getSynonymRelations() {
		if(synonymRelations == null) {
			this.synonymRelations = new HashSet<SynonymRelationship>();
		}
		return synonymRelations;
	}

	/**
	 * @see    #getSynonymRelations()
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
	 * the {@link SynonymRelationship#getAcceptedTaxon() accepted taxon} attribute and of the
	 * {@link SynonymRelationship#getSynonym() synonym} attribute within the synonym relationship
	 * itself will be set to "null".
	 *
	 * @param  synonymRelation  the synonym relationship which should be deleted
	 * @see     		  		#getSynonymRelations()
	 * @see     		  		#addRelationship(SynonymRelationship)
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
	 * This methods does the same as the {@link #addSynonymRelation(SynonymRelationship) addSynonymRelation} method.
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
	@Override
    public void addRelationship(SynonymRelationship rel){
		addSynonymRelation(rel);
	}


	/**
	 * Returns the set of all ("accepted/correct") {@link Taxon taxa} involved in the same
	 * {@link SynonymRelationship synonym relationships} as <i>this</i> synonym.
	 * Each taxon is the target and <i>this</i> synonym is the source of a {@link SynonymRelationship synonym relationship}
	 * belonging to the {@link #getSynonymRelations() set of synonym relationships} assigned to
	 * <i>this</i> synonym. For a particular synonym there are more than one
	 * ("accepted/correct") taxa only if the {@link SynonymRelationship#isProParte() "is pro parte" flag}
	 * of the corresponding {@link SynonymRelationship synonym relationships} is set.
	 *
	 * @see    #getSynonymRelations()
	 * @see    #getRelationType(Taxon)
	 * @see    SynonymRelationship#isProParte()
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
	 * Returns true if <i>this</i> is a synonym of the given taxon.
	 *
	 * @param taxon	the taxon to check synonym for
	 * @return	true if <i>this</i> is a ynonms of the given taxon
	 *
	 * @see #getAcceptedTaxa()
	 */
	@Transient
	public boolean isSynonymOf(Taxon taxon){
		return getAcceptedTaxa().contains(taxon);
	}

	@Override
    @Transient
	public boolean isOrphaned() {
		return false;
	}
	/**
	 * Returns the set of {@link SynonymRelationshipType synonym relationship types} of the
	 * {@link SynonymRelationship synonym relationships} where the {@link SynonymRelationship#getSynonym() synonym}
	 * is <i>this</i> synonym and the {@link SynonymRelationship#getAcceptedTaxon() taxon}
	 * is the given one. "Null" is returned if the given taxon is "null" or if
	 * no synonym relationship exists from <i>this</i> synonym to the
	 * given taxon.
	 *
	 * @param taxon	the ("accepted/correct") taxon which a synonym relationship
	 * 				from <i>this</i> synonym should point to
	 * @see    		#getSynonymRelations()
	 * @see    		#getAcceptedTaxa()
	 */
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

	/**
	 * Replaces ALL accepted taxa of this synonym by the new accepted taxon.
	 * The citation information (citation /microcitation) of the synonym relationship
	 * is kept.
	 * @param newAcceptedTaxon
	 * 			the new accepted taxon
	 * @param relType
	 * 			if not <code>null</code> the relationship type is changed to relType
	 * @param copyCitationInfo
	 * 			if true the citation and the microcitation of relationship
	 * 			is not changed.
	 * @param citation
	 * 			if copyCitationInfo is <code>false</code> this citation is set
	 * 			to the synonym relationship.
	 * @param microCitation
	 * 			if copyCitationInfo is <code>false</code> this micro citation is set
	 * 			to the synonym relationship.

	 * @param acceptedTaxon
	 */
	public void replaceAcceptedTaxon(Taxon newAcceptedTaxon, SynonymRelationshipType relType, boolean copyCitationInfo, Reference citation, String microCitation) {
		Set<SynonymRelationship> rels = new HashSet<SynonymRelationship>();
		rels.addAll(this.getSynonymRelations());  //avoid concurrent modification exception

		for (SynonymRelationship rel : rels){
			Taxon oldAcceptedTaxon = rel.getAcceptedTaxon();
			Synonym syn = rel.getSynonym();

			oldAcceptedTaxon.removeSynonym(rel.getSynonym(), false);

			SynonymRelationship newRel = (SynonymRelationship)rel.clone();
			newRel.setAcceptedTaxon(newAcceptedTaxon);
			newAcceptedTaxon.getSynonymRelations().add(newRel);
			newRel.setSynonym(syn);
			syn.addSynonymRelation(newRel);

			newRel.setType(relType);
		}
	}
//*********************** CLONE ********************************************************/

	@Override
	public Object clone() {
		Synonym result;
		result = (Synonym)super.clone();

		result.setSynonymRelations(new HashSet<SynonymRelationship>());

			for (SynonymRelationship synRelationship : this.getSynonymRelations()){
				SynonymRelationship newRelationship = (SynonymRelationship)synRelationship.clone();
				newRelationship.setRelatedFrom(result);
				result.synonymRelations.add(newRelationship);
			}
			return result;

	}


}