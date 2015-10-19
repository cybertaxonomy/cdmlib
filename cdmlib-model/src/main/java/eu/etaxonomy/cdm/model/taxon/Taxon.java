/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.ClassBridges;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.ReflectionUtils;

import eu.etaxonomy.cdm.hibernate.search.GroupByTaxonClassBridge;
import eu.etaxonomy.cdm.hibernate.search.TaxonRelationshipClassBridge;
import eu.etaxonomy.cdm.model.common.IRelated;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IDescribable;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.taxon.ITaxonCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.taxon.TaxonBaseDefaultCacheStrategy;

/**
 * The class for "accepted/correct" {@link TaxonBase taxa} (only these taxa according to
 * the opinion of the {@link eu.etaxonomy.cdm.model.reference.Reference reference} can build a classification).
 * An {@link java.lang.Iterable interface} is supported to iterate through taxonomic children.<BR>
 * Splitting taxa in "accepted/correct" and {@link Synonym "synonyms"} makes it easier to handle
 * particular relationships between ("accepted/correct") taxa on the one hand
 * and between ("synonym") taxa and ("accepted/correct") taxa on the other.
 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:56
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Taxon", propOrder = {
    "taxonomicParentCache",
    "taxonNodes",
    "taxonomicChildrenCount",
    "synonymRelations",
    "relationsFromThisTaxon",
    "relationsToThisTaxon",
    "descriptions"
})
@XmlRootElement(name = "Taxon")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.taxon.TaxonBase")
@Audited
@Configurable
@ClassBridges({
    @ClassBridge(impl = GroupByTaxonClassBridge.class),
    @ClassBridge(impl = TaxonRelationshipClassBridge.class)
})
public class Taxon
            extends TaxonBase<ITaxonCacheStrategy<Taxon>>
            implements IRelated<RelationshipBase>, IDescribable<TaxonDescription>, Cloneable{

    private static final long serialVersionUID = -584946869762749006L;
    private static final Logger logger = Logger.getLogger(Taxon.class);

    @XmlElementWrapper(name = "Descriptions")
    @XmlElement(name = "Description")
    @OneToMany(mappedBy="taxon", fetch= FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @NotNull
    @ContainedIn
    private Set<TaxonDescription> descriptions = new HashSet<TaxonDescription>();

    // all related synonyms
    @XmlElementWrapper(name = "SynonymRelations")
    @XmlElement(name = "SynonymRelationship")
    @OneToMany(mappedBy="relatedTo", fetch=FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @NotNull
    @Valid
    private Set<SynonymRelationship> synonymRelations = new HashSet<SynonymRelationship>();

    // all taxa relations with rel.fromTaxon==this
    @XmlElementWrapper(name = "RelationsFromThisTaxon")
    @XmlElement(name = "FromThisTaxonRelationship")
    @OneToMany(mappedBy="relatedFrom", fetch=FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @NotNull
//    @Valid
    private Set<TaxonRelationship> relationsFromThisTaxon = new HashSet<TaxonRelationship>();

    // all taxa relations with rel.toTaxon==this
    @XmlElementWrapper(name = "RelationsToThisTaxon")
    @XmlElement(name = "ToThisTaxonRelationship")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="relatedTo", fetch=FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    @NotNull
//    @Valid
    private Set<TaxonRelationship> relationsToThisTaxon = new HashSet<TaxonRelationship>();

    @XmlAttribute(name= "taxonStatusUnknown")
    private boolean taxonStatusUnknown = false;

    @XmlAttribute(name= "unplaced")
    private boolean unplaced = false;

    @XmlAttribute(name= "excluded")
    private boolean excluded = false;

    // shortcut to the taxonomicIncluded (parent) taxon. Managed by the taxonRelations setter
    @XmlElement(name = "TaxonomicParentCache")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Deprecated //will be removed in future versions. Use Classification/TaxonNode instead
    private Taxon taxonomicParentCache;


    @XmlElementWrapper(name = "taxonNodes")
    @XmlElement(name = "taxonNode")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="taxon", fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    @IndexedEmbedded
    private Set<TaxonNode> taxonNodes = new HashSet<TaxonNode>();

    //cached number of taxonomic children
    @XmlElement(name = "TaxonomicChildrenCount")
    @Deprecated //will be removed in future versions. Use Classification/TaxonNode instead
    private int taxonomicChildrenCount;

// ************************* FACTORY METHODS ********************************/

    /**
     * Creates a new (accepted/correct) taxon instance with
     * the {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name} used and the {@link eu.etaxonomy.cdm.model.reference.Reference reference}
     * using it.
     *
     * @param  taxonNameBase	the taxon name used
     * @param  sec				the reference using the taxon name
     * @see    					#Taxon(TaxonNameBase, Reference)
     */
    public static Taxon NewInstance(TaxonNameBase taxonNameBase, Reference sec){
        Taxon result = new Taxon(taxonNameBase, sec);
        return result;
    }

    /**
     * Creates a new taxon instance with an unknown status (accepted/synonym) and with
     * the {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name} used and the {@link eu.etaxonomy.cdm.model.reference.Reference reference}
     * using it.
     *
     * @param  taxonNameBase	the taxon name used
     * @param  sec				the reference using the taxon name
     * @see    					#Taxon(TaxonNameBase, Reference)
     */
    public static Taxon NewUnknownStatusInstance(TaxonNameBase taxonNameBase, Reference sec){
        Taxon result = new Taxon(taxonNameBase, sec);
        result.setTaxonStatusUnknown(true);
        return result;
    }
// ************* CONSTRUCTORS *************/

    //TODO should be private, but still produces Spring init errors
    @Deprecated
    public Taxon(){
        this.cacheStrategy = new TaxonBaseDefaultCacheStrategy<Taxon>();
    }

    /**
     * Class constructor: creates a new (accepted/correct) taxon instance with
     * the {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name} used and the {@link eu.etaxonomy.cdm.model.reference.Reference reference}
     * using it.
     *
     * @param  taxonNameBase	the taxon name used
     * @param  sec				the reference using the taxon name
     * @see    					TaxonBase#TaxonBase(TaxonNameBase, Reference)
     */
    public Taxon(TaxonNameBase taxonNameBase, Reference sec){
        super(taxonNameBase, sec);
        this.cacheStrategy = new TaxonBaseDefaultCacheStrategy<Taxon>();
    }

//********* METHODS **************************************/



    /**
     * Returns the set of {@link eu.etaxonomy.cdm.model.description.TaxonDescription taxon descriptions}
     * concerning <i>this</i> taxon.
     *
     * @see #removeDescription(TaxonDescription)
     * @see #addDescription(TaxonDescription)
     * @see eu.etaxonomy.cdm.model.description.TaxonDescription#getTaxon()
     */
    @Override
    public Set<TaxonDescription> getDescriptions() {
        if(descriptions == null) {
            descriptions = new HashSet<TaxonDescription>();
        }
        return descriptions;
    }

    /**
     * Adds a new {@link eu.etaxonomy.cdm.model.description.TaxonDescription taxon description} to the set
     * of taxon descriptions assigned to <i>this</i> (accepted/correct) taxon.
     * Due to bidirectionality the content of the {@link eu.etaxonomy.cdm.model.description.TaxonDescription#getTaxon() taxon attribute} of the
     * taxon description itself will be replaced with <i>this</i> taxon. The taxon
     * description will also be removed from the set of taxon descriptions
     * assigned to its previous taxon.
     *
     * @param  description	the taxon description to be added for <i>this</i> taxon
     * @see     		  	#getDescriptions()
     * @see     		  	#removeDescription(TaxonDescription)
     * @see 			  	eu.etaxonomy.cdm.model.description.TaxonDescription#getTaxon()
     */
    @Override
    public void addDescription(TaxonDescription description) {
        if (description.getTaxon() != null){
            description.getTaxon().removeDescription(description);
        }
        Field field = ReflectionUtils.findField(TaxonDescription.class, "taxon", Taxon.class);
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, description, this);
        descriptions.add(description);

    }
    /**
     * Removes one element from the set of {@link eu.etaxonomy.cdm.model.description.TaxonDescription taxon descriptions} assigned
     * to <i>this</i> (accepted/correct) taxon. Due to bidirectionality the content of
     * the {@link eu.etaxonomy.cdm.model.description.TaxonDescription#getTaxon() taxon attribute} of the taxon description
     * itself will be set to "null".
     *
     * @param  description  the taxon description which should be removed
     * @see     		  	#getDescriptions()
     * @see     		  	#addDescription(TaxonDescription)
     * @see 			  	eu.etaxonomy.cdm.model.description.TaxonDescription#getTaxon()
     */
    @Override
    public void removeDescription(TaxonDescription description) {
        //description.setTaxon(null) for not visible method
        Field field = ReflectionUtils.findField(TaxonDescription.class, "taxon", Taxon.class);
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, description, null);
        descriptions.remove(description);
    }


    public void removeDescription(TaxonDescription description, boolean removeElements){
    	if (removeElements){
    		Set<DescriptionElementBase> elements = new HashSet<DescriptionElementBase>(description.getElements());
            for (DescriptionElementBase el:elements){
            	description.getElements().remove(el);
            }
            removeDescription(description);
    	} else{
    		removeDescription(description);
    	}
    }

    /**
     * Returns the image gallery for a taxon. If there are multiple taxon descriptions
     * marked as image galleries an arbitrary one is chosen.
     * If no image gallery exists, a new one is created if <code>createNewIfNotExists</code>
     * is <code>true</code>.
     * @param createNewIfNotExists
     * @return
     */
    public TaxonDescription getImageGallery(boolean createNewIfNotExists) {
        TaxonDescription result = null;
        Set<TaxonDescription> descriptions= getDescriptions();
        for (TaxonDescription description : descriptions){
            if (description.isImageGallery()){
                result = description;
                break;
            }
        }
        if (result == null && createNewIfNotExists){
            result = TaxonDescription.NewInstance(this);
            result.setImageGallery(true);
        }
        return result;
    }



    public Set<TaxonNode> getTaxonNodes() {
        return taxonNodes;
    }
    //	protected void setTaxonNodes(Set<TaxonNode> taxonNodes) {
//		this.taxonNodes = taxonNodes;
//	}
    protected void addTaxonNode(TaxonNode taxonNode){
        taxonNodes.add(taxonNode);
    }

    public boolean removeTaxonNode(TaxonNode taxonNode){
        if (!taxonNodes.contains(taxonNode)){
            return false;
        }
        TaxonNode parent = taxonNode.getParent();
        if (parent != null){
            parent.removeChildNode(taxonNode);
        }
        taxonNode.setTaxon(null);
        return taxonNodes.remove(taxonNode);

    }

    public boolean removeTaxonNode(TaxonNode taxonNode, boolean deleteChildren){
        TaxonNode parent = taxonNode.getParent();
        boolean success = true;

        if ((!taxonNode.getChildNodes().isEmpty() && deleteChildren) || (taxonNode.getChildNodes().isEmpty()) ){

            taxonNode.delete();

        } else if (!taxonNode.isTopmostNode()){

            List<TaxonNode> nodes = new ArrayList<TaxonNode> (taxonNode.getChildNodes());
            for (TaxonNode childNode: nodes){
                taxonNode.getChildNodes().remove(childNode);
                parent.addChildNode(childNode, null, null);
            }

            taxonNode.delete();

        } else if (taxonNode.isTopmostNode()){
            success = false;
        }
        return success;
    }

    public boolean removeTaxonNodes(boolean deleteChildren){
        Iterator<TaxonNode> nodesIterator = taxonNodes.iterator();
        TaxonNode node;
        TaxonNode parent;
        boolean success = false;
        List<TaxonNode> removeNodes = new ArrayList<TaxonNode>();
        while (nodesIterator.hasNext()){
            node = nodesIterator.next();
            if (!deleteChildren){
                List<TaxonNode> children = node.getChildNodes();
                Iterator<TaxonNode> childrenIterator = children.iterator();
                parent = node.getParent();
                while (childrenIterator.hasNext()){
                    TaxonNode childNode = childrenIterator.next();
                    if (parent != null){
                        parent.addChildNode(childNode, null, null);
                    }else{
                        childNode.setParent(null);
                    }
                }

                for (int i = 0; i<node.getChildNodes().size(); i++){
                    node.removeChild(i);
                }


            }

            removeNodes.add(node);
         }
        for (int i = 0; i<removeNodes.size(); i++){
            TaxonNode removeNode = removeNodes.get(i);
            success = removeNode.delete(deleteChildren);
            removeNode.setTaxon(null);
            removeTaxonNode(removeNode);
        }
        return success;

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
    public Set<SynonymRelationship> getSynonymRelations() {
        if(synonymRelations == null) {
            this.synonymRelations = new HashSet<SynonymRelationship>();
        }
        return synonymRelations;
    }

    /**
     * Adds an existing {@link SynonymRelationship synonym relationship} to the set of
     * {@link #getSynonymRelations() synonym relationships} assigned to <i>this</i> taxon. If
     * the target of the synonym relationship does not match with <i>this</i> taxon
     * no addition will be carried out.
     *
     * @param synonymRelation	the synonym relationship to be added to <i>this</i> taxon's
     * 							synonym relationships set
     * @see    	   				#getSynonymRelations()
     * @see    	   				#addSynonym(Synonym, SynonymRelationshipType)
     * @see    	   				#addSynonym(Synonym, SynonymRelationshipType, Reference, String)
     * @see    	   				#addSynonymName(TaxonNameBase, SynonymRelationshipType)
     * @see    	   				#addSynonymName(TaxonNameBase, SynonymRelationshipType, Reference, String)
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
     * the {@link SynonymRelationship#getAcceptedTaxon() accepted taxon} attribute and of the
     * {@link SynonymRelationship#getSynonym() synonym} attribute within the synonym relationship
     * itself will be set to "null".
     *
     * @param synonymRelation  	the synonym relationship which should be deleted
     * @param removeSynonymNameFromHomotypicalGroup
     * 				if <code>true</code> the synonym name will also be deleted from its homotypical group if the
     * 				group contains other names
     * @see    	#getSynonymRelations()
     * @see    	#addSynonymRelation(SynonymRelationship)
     * @see 	#removeSynonym(Synonym)
     */
    public void removeSynonymRelation(SynonymRelationship synonymRelation, boolean removeSynonymNameFromHomotypicalGroup) {
        synonymRelation.setAcceptedTaxon(null);
        Synonym synonym = synonymRelation.getSynonym();
        if (synonym != null){
            synonymRelation.setSynonym(null);
            synonym.removeSynonymRelation(synonymRelation);
            if(removeSynonymNameFromHomotypicalGroup){
                HomotypicalGroup synHG = synonym.getName().getHomotypicalGroup();
                if (synHG.getTypifiedNames().size() > 1){
                    synHG.removeTypifiedName(synonym.getName(), false);
                }
            }
        }
        this.synonymRelations.remove(synonymRelation);
    }

    /**
     * Like {@link Taxon#removeSynonymRelation(SynonymRelationship, boolean)} but synonym name
     * will be deleted from homotypical group by default
     *
     * @param synonymRelation   the synonym relationship which should be deleted
     *
     * @see					#removeSynonymRelation(SynonymRelationship, boolean)
     */
    public void removeSynonymRelation(SynonymRelationship synonymRelation){
        removeSynonymRelation(synonymRelation, true);
    }


    /**
     * Returns the set of all {@link TaxonRelationship taxon relationships}
     * between two taxa in which <i>this</i> taxon is involved as a source.
     *
     * @see    #getRelationsToThisTaxon()
     * @see    #getTaxonRelations()
     */
    public Set<TaxonRelationship> getRelationsFromThisTaxon() {
        if(relationsFromThisTaxon == null) {
            this.relationsFromThisTaxon = new HashSet<TaxonRelationship>();
        }
        return relationsFromThisTaxon;
    }


    /**
     * Returns the set of all {@link TaxonRelationship taxon relationships}
     * between two taxa in which <i>this</i> taxon is involved as a target.
     *
     * @see    #getRelationsFromThisTaxon()
     * @see    #getTaxonRelations()
     */
    public Set<TaxonRelationship> getRelationsToThisTaxon() {
        if(relationsToThisTaxon == null) {
            this.relationsToThisTaxon = new HashSet<TaxonRelationship>();
        }
        return relationsToThisTaxon;
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
     * @see    #getRelationsToThisTaxon()
     */
    protected void setRelationsToThisTaxon(Set<TaxonRelationship> relationsToThisTaxon) {
        this.relationsToThisTaxon = relationsToThisTaxon;
    }

    /**
     * @see    #getRelationsFromThisTaxon()
     */
    protected void setRelationsFromThisTaxon(Set<TaxonRelationship> relationsFromThisTaxon) {
        this.relationsFromThisTaxon = relationsFromThisTaxon;
    }

    /**
     * If a relationships between <i>this</i> and the given taxon exists they will be returned.
     * <i>This</i> taxon is involved either as a source or as a target in the relationships.
     * The method will return <code>null</code> if no relations exist between the two taxa.
     *
     * @param possiblyRelatedTaxon
     * 			a taxon to check for a relationship
     * @return
     * 			a set of <code>TaxonRelationship</code>s or <code>null</null> if none exists.
     */
    public Set<TaxonRelationship> getTaxonRelations(Taxon possiblyRelatedTaxon){
        Set<TaxonRelationship> relations = new HashSet<TaxonRelationship>();

        for(TaxonRelationship relationship : getTaxonRelations()){
            if(relationship.getFromTaxon().equals(possiblyRelatedTaxon)) {
                relations.add(relationship);
            }
            if(relationship.getToTaxon().equals(possiblyRelatedTaxon)) {
                relations.add(relationship);
            }
        }

        return relations.size() > 0 ? relations : null;
    }

    /**
     * Removes one {@link TaxonRelationship taxon relationship} from one of both sets of
     * {@link #getTaxonRelations() taxon relationships} in which <i>this</i> taxon is involved
     * either as a {@link #getRelationsFromThisTaxon() source} or as a {@link #getRelationsToThisTaxon() target}.
     * The taxon relationship will also be removed from one of both sets
     * belonging to the second taxon involved. Furthermore the inherited RelatedFrom and
     * RelatedTo attributes of the given taxon relationship will be nullified.<P>
     * If the taxon relationship concerns the classification possible
     * modifications of the {@link #getTaxonomicParent() parent taxon} or of the number of
     * {@link #getTaxonomicChildrenCount() childrens} will be stored.
     *
     * @param  rel  the taxon relationship which should be removed from one
     * 				of both sets
     * @see    		#getTaxonRelations()
     * @see    	    #getTaxonomicParent()
     * @see    	    #getTaxonomicChildrenCount()
     * @see    		eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedFrom()
     * @see    		eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedTo()
     *
     */
    public void removeTaxonRelation(TaxonRelationship rel) {
        this.relationsToThisTaxon.remove(rel);
        this.relationsFromThisTaxon.remove(rel);
        Taxon fromTaxon = rel.getFromTaxon();
        Taxon toTaxon = rel.getToTaxon();
        // check if this removes the taxonomical parent. If so, also remove shortcut to the higher taxon
        if (rel.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN()) ){
            if (fromTaxon != null && fromTaxon.equals(this)){
                this.taxonomicParentCache = null;
            }else if (toTaxon != null && toTaxon.equals(this)){
                this.setTaxonomicChildrenCount(computeTaxonomicChildrenCount());
            }
        }
        //delete Relationship from other related Taxon
        if (fromTaxon != this){
            rel.setToTaxon(null);  //remove this Taxon from relationship
            if (fromTaxon != null){
                if (fromTaxon.getTaxonRelations().contains(rel)){
                    fromTaxon.removeTaxonRelation(rel);
                }
            }
        }
        if (toTaxon != this ){
            rel.setFromTaxon(null); //remove this Taxon from relationship
           if (toTaxon != null){
               if (toTaxon.getTaxonRelations().contains(rel)) {
                   toTaxon.removeTaxonRelation(rel);
               }
           }
        }
    }

    /**
     * Adds an existing {@link TaxonRelationship taxon relationship} either to the set of
     * {@link #getRelationsToThisTaxon() taxon relationships to <i>this</i> taxon} or to the set of
     * {@link #getRelationsFromThisTaxon() taxon relationships from <i>this</i> taxon}. If neither the
     * source nor the target of the taxon relationship match with <i>this</i> taxon
     * no addition will be carried out. The taxon relationship will also be
     * added to the second taxon involved in the given relationship.<P>
     * If the taxon relationship concerns the classification possible
     * modifications of the {@link #getTaxonomicParent() parent taxon} or of the number of
     * {@link #getTaxonomicChildrenCount() childrens} will be stored.
     *
     * @param rel  the taxon relationship to be added to one of <i>this</i> taxon's taxon relationships sets
     * @see    	   #addTaxonRelation(Taxon, TaxonRelationshipType, Reference, String)
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
                        this.taxonomicParentCache = toTaxon;
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
            }else if (toTaxon == null || fromTaxon == null){
                if (toTaxon == null){
                    toTaxon = this;
                    relationsToThisTaxon.add(rel);
                    if (fromTaxon!= null){
                        fromTaxon.addTaxonRelation(rel);
                    }
                    if (rel.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN()) && fromTaxon!=null ){
                        this.taxonomicChildrenCount++;
                    }
                }else if (fromTaxon == null && toTaxon != null){
                    fromTaxon = this;
                    relationsFromThisTaxon.add(rel);
                    if (toTaxon!=null){
                        toTaxon.addTaxonRelation(rel);
                    }
                    if (rel.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN()) && toTaxon!=null ){
                        this.taxonomicParentCache = toTaxon;
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.IRelated#addRelationship(eu.etaxonomy.cdm.model.common.RelationshipBase)
     */
    @Override
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
     * If the taxon relationship concerns the classification possible
     * modifications of the {@link #getTaxonomicParent() parent taxon} or of the number of
     * {@link #getTaxonomicChildrenCount() childrens} will be stored.
     *
     * @param toTaxon		the taxon which plays the target role in the new taxon relationship
     * @param type			the taxon relationship type for the new taxon relationship
     * @param citation		the reference source for the new taxon relationship
     * @param microcitation	the string with the details describing the exact localisation within the reference
     * @return
     * @see    	   			#addTaxonRelation(TaxonRelationship)
     * @see    	   			#getTaxonRelations()
     * @see    	   			#getRelationsFromThisTaxon()
     * @see    	   			#getRelationsToThisTaxon()
     * @see    	   			#getTaxonomicParent()
     * @see    	   			#getTaxonomicChildrenCount()
     */
    public TaxonRelationship addTaxonRelation(Taxon toTaxon, TaxonRelationshipType type, Reference citation, String microcitation) {
        return new TaxonRelationship(this, toTaxon, type, citation, microcitation);
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
     * @return
     * @see    	   					#getMisappliedNames()
     * @see    	   					#addTaxonRelation(Taxon, TaxonRelationshipType, Reference, String)
     * @see    	   					#addTaxonRelation(TaxonRelationship)
     * @see    	   					#getTaxonRelations()
     * @see    	   					#getRelationsFromThisTaxon()
     * @see    	   					#getRelationsToThisTaxon()
     */
    public TaxonRelationship addMisappliedName(Taxon misappliedNameTaxon, Reference citation, String microcitation) {
        return misappliedNameTaxon.addTaxonRelation(this, TaxonRelationshipType.MISAPPLIED_NAME_FOR(), citation, microcitation);
    }

//	public void removeMisappliedName(Taxon misappliedNameTaxon){
//		Set<TaxonRelationship> taxRels = this.getTaxonRelations();
//		for (TaxonRelationship taxRel : taxRels ){
//			if (taxRel.getType().equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())
//				&& taxRel.getFromTaxon().equals(misappliedNameTaxon)){
//				this.removeTaxonRelation(taxRel);
//			}
//		}
//	}

    /**
     * TODO update documentation
     * Removes one {@link TaxonRelationship taxon relationship} with {@link TaxonRelationshipType taxon relationship type}
     * taxonRelType and with the given child taxon playing the
     * source role from the set of {@link #getRelationsToThisTaxon() "taxon relationships to"} belonging
     * to <i>this</i> taxon. The taxon relationship will also be removed from the set
     * of {@link #getRelationsFromThisTaxon() "taxon relationships from"} belonging to the other side taxon.
     * Furthermore, the inherited RelatedFrom and RelatedTo attributes of the
     * taxon relationship will be nullified.<P>
     *
     * @param taxon			the taxon which plays the source role in the taxon relationship
     * @param taxonRelType	the taxon relationship type
     */
    public void removeTaxon(Taxon taxon, TaxonRelationshipType taxonRelType){
        Set<TaxonRelationship> taxRels = this.getTaxonRelations();
        for (TaxonRelationship taxRel : taxRels ){
            if (taxRel.getType().equals(taxonRelType)
                && taxRel.getFromTaxon().equals(taxon)){
                this.removeTaxonRelation(taxRel);
            }
        }
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
     * @see    	   			#setTaxonomicParent(Taxon, Reference, String)
     * @see    	   			#addTaxonRelation(Taxon, TaxonRelationshipType, Reference, String)
     * @see    	   			#addTaxonRelation(TaxonRelationship)
     * @see    	   			#getTaxonRelations()
     * @see    	   			#getRelationsFromThisTaxon()
     * @see    	   			#getRelationsToThisTaxon()
     * @see    	   			#getTaxonomicParent()
     * @see    	   			#getTaxonomicChildrenCount()
     */
    @Deprecated //will be removed in future versions. Use Classification/TaxonNode instead
    public void addTaxonomicChild(Taxon child, Reference citation, String microcitation){
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
     * Since the taxon relationship concerns the classification modifications
     * of the number of {@link #getTaxonomicChildrenCount() childrens} for <i>this</i> taxon and
     * of the {@link #getTaxonomicParent() parent taxon} for the child taxon will be stored.
     *
     * @param  child	the taxon playing the source role in the relationship to be removed
     * @see    	    	#removeTaxonRelation(TaxonRelationship)
     * @see    			#getRelationsToThisTaxon()
     * @see    			#getRelationsFromThisTaxon()
     * @see    	    	#getTaxonomicParent()
     * @see    	    	#getTaxonomicChildrenCount()
     * @see    			eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedFrom()
     * @see    			eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedTo()
     *
     */
    @Deprecated //will be removed in future versions. Use classification/TaxonNode instead
    public void removeTaxonomicChild(Taxon child){
        Set<TaxonRelationship> taxRels = this.getTaxonRelations();
        for (TaxonRelationship taxRel : taxRels ){
            if (taxRel.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())
                && taxRel.getFromTaxon().equals(child)){
                this.removeTaxonRelation(taxRel);
            }
        }
    }

    /**
     * Returns the taxon which is the next higher taxon (parent) of <i>this</i> taxon
     * within the classification and which is stored in the
     * TaxonomicParentCache attribute. Each taxon can have only one parent taxon.
     * The child taxon and the parent taxon play the source respectively the
     * target role in one {@link TaxonRelationship taxon relationship} with
     * {@link TaxonRelationshipType taxon relationship type} "taxonomically included in".
     * The {@link name.Rank rank} of the taxon name used as a parent taxon must be higher
     * than the rank of the taxon name used as a child taxon.
     *
     * @see  #setTaxonomicParent(Taxon, Reference, String)
     * @see  #getTaxonomicChildren()
     * @see  #getTaxonomicChildrenCount()
     * @see  #getRelationsFromThisTaxon()
     */
    @Deprecated //will be removed in future versions. Use Classification/TaxonNode instead
    public Taxon getTaxonomicParent() {
        return this.taxonomicParentCache;
    }

    /**
     * Sets the taxononomic parent of <i>this</i> taxon to null.
     * Note that this method does not handle taxonomic relationships.
     */
    @Deprecated //will be removed in future versions. Use Classification/TaxonNode instead
    public void nullifyTaxonomicParent() {
        this.taxonomicParentCache = null;
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
     * Since the taxon relationship concerns the classification modifications
     * of the {@link #getTaxonomicParent() parent taxon} for <i>this</i> taxon and of the number of
     * {@link #getTaxonomicChildrenCount() childrens} for the child taxon will be stored.
     *
     * @param newParent		the taxon which plays the target role (parent) in the new taxon relationship
     * @param citation		the reference source for the new taxon relationship
     * @param microcitation	the string with the details describing the exact localisation within the reference
     * @see    	   			#removeTaxonRelation(TaxonRelationship)
     * @see    	   			#getTaxonomicParent()
     * @see    	   			#addTaxonRelation(Taxon, TaxonRelationshipType, Reference, String)
     * @see    	   			#addTaxonRelation(TaxonRelationship)
     * @see    	   			#getTaxonRelations()
     * @see    	   			#getRelationsFromThisTaxon()
     * @see    	   			#getRelationsToThisTaxon()
     * @see    	   			#getTaxonomicChildrenCount()
     */
    @Deprecated //will be removed in future versions. Use Classification/TaxonNode instead
    public void setTaxonomicParent(Taxon newParent, Reference citation, String microcitation){
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
     * (parent) within the classification. Each taxon can have several child
     * taxa. The child taxon and the parent taxon play the source respectively
     * the target role in one {@link TaxonRelationship taxon relationship} with
     * {@link TaxonRelationshipType taxon relationship type} "taxonomically included in".
     * The {@link name.Rank rank} of the taxon name used as a parent taxon must be higher
     * than the rank of the taxon name used as a child taxon.
     *
     * @see  #getTaxonomicParent()
     * @see  #addTaxonomicChild(Taxon, Reference, String)
     * @see  #getTaxonomicChildrenCount()
     * @see  #getRelationsToThisTaxon()
     */
    @Transient
    @Deprecated //will be removed in future versions. Use Classification/TaxonNode instead
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
     * (parent) within the classification and the number of which is stored in
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
    @Deprecated //will be removed in future versions. Use Classification/TaxonNode instead
    public int getTaxonomicChildrenCount(){
        return taxonomicChildrenCount;
    }

    /**
     * @see  #getTaxonomicChildrenCount()
     */
    @Deprecated //will be removed in future versions. Use Classification/TaxonNode instead
    public void setTaxonomicChildrenCount(int taxonomicChildrenCount) {
        this.taxonomicChildrenCount = taxonomicChildrenCount;
    }

    /**
     * Returns the boolean value indicating whether <i>this</i> taxon has at least one
     * taxonomic child taxon within the classification (true) or not (false).
     *
     * @see  #getTaxonomicChildrenCount()
     * @see  #getTaxonomicChildren()
     */
    @Deprecated //will be removed in future versions. Use Classification/TaxonNode instead
    public boolean hasTaxonomicChildren(){
        return this.taxonomicChildrenCount > 0;
    }

    @Deprecated //will be removed in future versions. Use Classification/TaxonNode instead
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
     * Returns the boolean value indicating whether <i>this</i> taxon is a misaplication
     * (misapplied name) for at least one other taxon.
     */
    // TODO cache as for #hasTaxonomicChildren
    @Transient
    public boolean isMisapplication(){
        return computeMisapliedNameRelations() > 0;
    }

    /**
     * Counts the number of misaplied names relationships where this taxon represents the
     * misaplied name for another taxon.
     * @return
     */
    private int computeMisapliedNameRelations(){
        int count = 0;
        for (TaxonRelationship rel: this.getRelationsFromThisTaxon()){
            if (rel.getType().equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())){
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the boolean value indicating whether <i>this</i> taxon is a related
     * concept for at least one other taxon.
     */
    @Transient
    public boolean isRelatedConcept(){
        return computeConceptRelations() > 0;
    }

    /**
     * Counts the number of concept relationships where this taxon represents the
     * related concept for another taxon.
     * @return
     */
    private int computeConceptRelations(){
        int count = 0;
        for (TaxonRelationship rel: this.getRelationsFromThisTaxon()){
            TaxonRelationshipType type = rel.getType();
            if (type.isConceptRelationship()){
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the boolean value indicating whether <i>this</i> taxon has at least one
     * {@link Synonym synonym} (true) or not (false). If true the {@link #getSynonymRelations() set of synonym relationships}
     * belonging to <i>this</i> ("accepted/correct") taxon is not empty .
     *
     * @see  #getSynonymRelations()
     * @see  #getSynonyms()
     * @see  #getSynonymNames()
     * @see  #removeSynonym(Synonym)
     * @see  SynonymRelationship
     */
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
    public boolean hasTaxonRelationships(){
        return this.getTaxonRelations().size() > 0;
    }

    /*
     * MISAPPLIED NAMES
     */
    /**
     * Returns the set of taxa playing the source role in {@link TaxonRelationship taxon relationships}
     * (with {@link TaxonRelationshipType taxon relationship type} "misapplied name for") where
     * <i>this</i> taxon plays the target role. A misapplied name is a taxon the
     * {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name} of which has been erroneously used
     * by its {@link TaxonBase#getSec() taxon reference} to denominate the same real taxon
     * as the one meant by <i>this</i> ("accepted/correct") taxon.
     *
     * @see  #getTaxonRelations()
     * @see  #getRelationsToThisTaxon()
     * @see  #addMisappliedName(Taxon, Reference, String)
     */
    @Transient
    public Set<Taxon> getMisappliedNames(){
        Set<Taxon> taxa = new HashSet<Taxon>();
        Set<TaxonRelationship> rels = this.getRelationsToThisTaxon();
        for (TaxonRelationship rel: rels){
            TaxonRelationshipType tt = rel.getType();
            TaxonRelationshipType incl = TaxonRelationshipType.MISAPPLIED_NAME_FOR();
            if (tt.equals(incl)){
                taxa.add(rel.getFromTaxon());
            }
        }
        return taxa;
    }
    /**
     * Returns the set of taxa playing the target role in {@link TaxonRelationship taxon relationships}
     * (with {@link TaxonRelationshipType taxon relationship type} "misapplied name for") where
     * <i>this</i> taxon plays the source role. A misapplied name is a taxon the
     * {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name} of which has been erroneously used
     * by its {@link TaxonBase#getSec() taxon reference} to denominate the same real taxon
     * as the one meant by <i>this</i> ("accepted/correct") taxon.
     *
     * @see  #getTaxonRelations()
     * @see  #getRelationsToThisTaxon()
     * @see  #addMisappliedName(Taxon, Reference, String)
     */
    @Transient
    public Set<Taxon> getTaxonForMisappliedName(){
        Set<Taxon> taxa = new HashSet<Taxon>();
        Set<TaxonRelationship> rels = this.getRelationsFromThisTaxon();
        for (TaxonRelationship rel: rels){
            TaxonRelationshipType tt = rel.getType();
            TaxonRelationshipType incl = TaxonRelationshipType.MISAPPLIED_NAME_FOR();
            if (tt.equals(incl)){
                taxa.add(rel.getToTaxon());
            }
        }
        return taxa;
    }


    /*
     * DEALING WITH SYNONYMS
     */
    /**
     * Returns the set of all {@link Synonym synonyms} of <i>this</i> ("accepted/correct") taxon.
     * Each synonym is the source and <i>this</i> taxon is the target of a {@link SynonymRelationship synonym relationship}
     * belonging to the {@link #getSynonymRelations() set of synonym relationships} assigned to <i>this</i> taxon.
     * For a particular synonym and for a particular ("accepted/correct") taxon
     * there can be several synonym relationships (if two or more
     * {@link SynonymRelationshipType synonym relationship types} - for instance
     * "pro parte synonym of" and "is homotypic synonym of" - must be combined).
     *
     * @see    #getSynonymsSortedByType()
     * @see    #getSynonymNames()
     * @see    #getSynonymRelations()
     * @see    #addSynonym(Synonym, SynonymRelationshipType)
     * @see    #addSynonym(Synonym, SynonymRelationshipType, Reference, String)
     * @see    #removeSynonymRelation(SynonymRelationship)
     * @see    #removeSynonym(Synonym)
     */
    @Transient
    public Set<Synonym> getSynonyms(){
        Set<Synonym> syns = new HashSet<Synonym>();
        for (SynonymRelationship rel: this.getSynonymRelations()){
            syns.add(rel.getSynonym());
        }
        return syns;
    }
    /**
     * Returns the set of all {@link Synonym synonyms} of <i>this</i> ("accepted/correct") taxon
     * sorted by the different {@link SynonymRelationshipType categories of synonym relationships}.
     * Each synonym is the source and <i>this</i> taxon is the target of a {@link SynonymRelationship synonym relationship}
     * belonging to the {@link #getSynonymRelations() set of synonym relationships} assigned to <i>this</i> taxon.
     *
     * @see    #getSynonyms()
     * @see    #getSynonymNames()
     * @see    #getSynonymRelations()
     * @see    #addSynonym(Synonym, SynonymRelationshipType)
     * @see    #addSynonym(Synonym, SynonymRelationshipType, Reference, String)
     * @see    #removeSynonymRelation(SynonymRelationship)
     * @see    #removeSynonym(Synonym)
     */
    @Transient
    public Set<Synonym> getSynonymsSortedByType(){
        // FIXME: need to sort synonyms according to type!!!
        logger.warn("getSynonymsSortedByType() not yet implemented");
        return getSynonyms();
    }
    /**
     * Returns the set of all {@link name.TaxonNameBase taxon names} used as {@link Synonym synonyms}
     * of <i>this</i> ("accepted/correct") taxon. Each synonym is the source and
     * <i>this</i> taxon is the target of a {@link SynonymRelationship synonym relationship} belonging
     * to the {@link #getSynonymRelations() set of synonym relationships} assigned to <i>this</i> taxon.
     *
     * @see    #getSynonyms()
     * @see    #getSynonymsSortedByType()
     * @see    #getSynonymRelations()
     * @see    #addSynonymName(TaxonNameBase, SynonymRelationshipType)
     * @see    #addSynonym(Synonym, SynonymRelationshipType, Reference, String)
     * @see    #removeSynonymRelation(SynonymRelationship)
     * @see    #removeSynonym(Synonym)
     */
    @Transient
    public Set<TaxonNameBase> getSynonymNames(){
        Set<TaxonNameBase> names = new HashSet<TaxonNameBase>();
        for (SynonymRelationship rel: this.getSynonymRelations()){
            names.add(rel.getSynonym().getName());
        }
        return names;
    }
    /**
     * Creates a new {@link SynonymRelationship synonym relationship} (with the given {@link Synonym synonym}
     * and with the given {@link SynonymRelationshipType synonym relationship type}), returns it and adds it
     * to the set of {@link #getSynonymRelations() synonym relationships} assigned to <i>this</i> taxon.
     * The new synonym relationship will also be added to the set of
     * {@link Synonym#getSynonymRelations() synonym relationships} belonging to the synonym
     * involved in this synonym relationship.<BR>
     * The returned synonym relationship allows to add further information to it.
     *
     * @param synonym		the synonym involved in the relationship to be created
     * 						and added to <i>this</i> taxon's synonym relationships set
     * @param synonymType	the synonym relationship category of the synonym
     * 						relationship to be added
     * @return 				the created synonym relationship
     * @see    	   			#addSynonymRelation(SynonymRelationship)
     * @see    	   			#addSynonym(Synonym, SynonymRelationshipType, Reference, String)
     * @see    	   			#addSynonymName(TaxonNameBase, SynonymRelationshipType)
     * @see    	   			#addSynonymName(TaxonNameBase, SynonymRelationshipType, Reference, String)
     * @see    	   			#addHomotypicSynonym(Synonym, Reference, String)
     * @see    	   			#addHomotypicSynonymName(TaxonNameBase, Reference, String)
     * @see    	   			#addHeterotypicSynonymName(TaxonNameBase)
     * @see    	   			#addHeterotypicSynonymName(TaxonNameBase, HomotypicalGroup, Reference, String)
     * @see    	   			#getSynonymRelations()
     * @see    				#removeSynonym(Synonym)
     * @see    	   			Synonym#getSynonymRelations()
     */
    public SynonymRelationship addSynonym(Synonym synonym, SynonymRelationshipType synonymType){
        return addSynonym(synonym, synonymType, null, null);
    }
    /**
     * Creates a new {@link SynonymRelationship synonym relationship} (with the given {@link Synonym synonym},
     * with the given {@link SynonymRelationshipType synonym relationship type} and with the
     * {@link eu.etaxonomy.cdm.model.reference.Reference reference source} on which the relationship assertion is based),
     * returns it and adds it to the set of {@link #getSynonymRelations() synonym relationships}
     * assigned to <i>this</i> taxon. The new synonym relationship will also be
     * added to the set of {@link Synonym#getSynonymRelations() synonym relationships} belonging to the synonym
     * involved in this synonym relationship.<BR>
     * The returned synonym relationship allows to add further information to it.
     *
     * @param synonym		the synonym involved in the relationship to be created
     * 						and added to <i>this</i> taxon's synonym relationships set
     * @param synonymType	the synonym relationship category of the synonym
     * 						relationship to be added
     * @param citation		the reference source for the new synonym relationship
     * @param microcitation	the string with the details describing the exact localisation within the reference
     * @return 				the created synonym relationship
     * @see    	   			#addSynonymRelation(SynonymRelationship)
     * @see    	   			#addSynonym(Synonym, SynonymRelationshipType, Reference, String)
     * @see    	   			#addSynonymName(TaxonNameBase, SynonymRelationshipType)
     * @see    	   			#addSynonymName(TaxonNameBase, SynonymRelationshipType, Reference, String)
     * @see    	   			#addHomotypicSynonym(Synonym, Reference, String)
     * @see    	   			#addHomotypicSynonymName(TaxonNameBase, Reference, String)
     * @see    	   			#addHeterotypicSynonymName(TaxonNameBase)
     * @see    	   			#addHeterotypicSynonymName(TaxonNameBase, HomotypicalGroup, Reference, String)
     * @see    	   			#getSynonymRelations()
     * @see    				#removeSynonym(Synonym)
     * @see    	   			Synonym#getSynonymRelations()
     */
    public SynonymRelationship addSynonym(Synonym synonym, SynonymRelationshipType synonymType, Reference citation, String citationMicroReference){
        SynonymRelationship synonymRelationship = new SynonymRelationship(synonym, this, synonymType, citation, citationMicroReference);
        return synonymRelationship;
    }

    /**
     * Creates a new {@link Synonym synonym} (with the given {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name}),
     * a new {@link SynonymRelationship synonym relationship} (with the new synonym and with the given
     * {@link SynonymRelationshipType synonym relationship type}), returns the relationship and adds it
     * to the set of {@link #getSynonymRelations() synonym relationships} assigned to <i>this</i> taxon.
     * The new synonym will have the same {@link TaxonBase#getSec() concept reference}
     * as <i>this</i> taxon. The new synonym relationship will also be added to
     * the set of {@link Synonym#getSynonymRelations() synonym relationships} belonging
     * to the created synonym.<BR>
     * The returned synonym relationship allows to add further information to it.
     *
     * @param synonymName	the taxon name to be used as a synonym to be added
     * 						to <i>this</i> taxon's set of synonyms
     * @param synonymType	the synonym relationship category of the synonym
     * 						relationship to be added
     * @return 				the created synonym relationship
     * @see    	   			#addSynonymName(TaxonNameBase, SynonymRelationshipType, Reference, String)
     * @see    	   			#addSynonym(Synonym, SynonymRelationshipType)
     * @see    	   			#addSynonym(Synonym, SynonymRelationshipType, Reference, String)
     * @see    	   			#addSynonymRelation(SynonymRelationship)
     * @see    	   			#addHomotypicSynonym(Synonym, Reference, String)
     * @see    	   			#addHomotypicSynonymName(TaxonNameBase, Reference, String)
     * @see    	   			#addHeterotypicSynonymName(TaxonNameBase)
     * @see    	   			#addHeterotypicSynonymName(TaxonNameBase, HomotypicalGroup, Reference, String)
     * @see    	   			#getSynonymRelations()
     * @see    				#removeSynonym(Synonym)
     * @see    	   			Synonym#getSynonymRelations()
     */
    public SynonymRelationship addSynonymName(TaxonNameBase synonymName, SynonymRelationshipType synonymType){
        return addSynonymName(synonymName, synonymType, null, null);
    }
    /**
     * Creates a new {@link Synonym synonym} (with the given {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name}),
     * a new {@link SynonymRelationship synonym relationship} (with the new synonym, with the given
     * {@link SynonymRelationshipType synonym relationship type} and with the {@link eu.etaxonomy.cdm.model.reference.Reference reference source}
     * on which the relationship assertion is based), returns the relationship
     * and adds it to the set of {@link #getSynonymRelations() synonym relationships} assigned
     * to <i>this</i> taxon. The new synonym will have the same {@link TaxonBase#getSec() concept reference}
     * as <i>this</i> taxon. The new synonym relationship will also be added to
     * the set of {@link Synonym#getSynonymRelations() synonym relationships} belonging
     * to the created synonym.<BR>
     * The returned synonym relationship allows to add further information to it.
     *
     * @param synonymName	the taxon name to be used as a synonym to be added
     * 						to <i>this</i> taxon's set of synonyms
     * @param synonymType	the synonym relationship category of the synonym
     * 						relationship to be added
     * @param citation		the reference source for the new synonym relationship
     * @param microcitation	the string with the details describing the exact localisation within the reference
     * @return 				the created synonym relationship
     * @see    	   			#addSynonymName(TaxonNameBase, SynonymRelationshipType, Reference, String)
     * @see    	   			#addSynonym(Synonym, SynonymRelationshipType)
     * @see    	   			#addSynonym(Synonym, SynonymRelationshipType, Reference, String)
     * @see    	   			#addSynonymRelation(SynonymRelationship)
     * @see    	   			#addHomotypicSynonym(Synonym, Reference, String)
     * @see    	   			#addHomotypicSynonymName(TaxonNameBase, Reference, String)
     * @see    	   			#addHeterotypicSynonymName(TaxonNameBase)
     * @see    	   			#addHeterotypicSynonymName(TaxonNameBase, HomotypicalGroup, Reference, String)
     * @see    	   			#getSynonymRelations()
     * @see    				#removeSynonym(Synonym)
     * @see    	   			Synonym#getSynonymRelations()
     */
    public SynonymRelationship addSynonymName(TaxonNameBase synonymName, SynonymRelationshipType synonymType, Reference citation, String citationMicroReference){
        Synonym synonym = Synonym.NewInstance(synonymName, this.getSec());
        return addSynonym(synonym, synonymType, citation, citationMicroReference);
    }

    /**
     * Creates a new {@link Synonym synonym} (with the given {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name}),
     * a new {@link SynonymRelationship synonym relationship} (with the new synonym and with the
     * {@link SynonymRelationshipType#HETEROTYPIC_SYNONYM_OF() "is heterotypic synonym of" relationship type}),
     * returns the relationship and adds it to the set of
     * {@link #getSynonymRelations() synonym relationships} assigned to <i>this</i> taxon.
     * The new synonym will have the same {@link TaxonBase#getSec() concept reference}
     * as <i>this</i> taxon. The new synonym relationship will also be added to
     * the set of {@link Synonym#getSynonymRelations() synonym relationships} belonging
     * to the created synonym.<BR>
     * The returned synonym relationship allows to add further information to it.
     *
     * @param synonymName	the taxon name to be used as an heterotypic synonym
     * 						to be added to <i>this</i> taxon's set of synonyms
     * @return 				the created synonym relationship
     * @see    	   			#addHeterotypicSynonymName(TaxonNameBase, HomotypicalGroup, Reference, String)
     * @see    	   			#addSynonymName(TaxonNameBase, SynonymRelationshipType)
     * @see    	   			#addSynonymName(TaxonNameBase, SynonymRelationshipType, Reference, String)
     * @see    	   			#addSynonym(Synonym, SynonymRelationshipType)
     * @see    	   			#addSynonym(Synonym, SynonymRelationshipType, Reference, String)
     * @see    	   			#addSynonymRelation(SynonymRelationship)
     * @see    	   			#addHomotypicSynonym(Synonym, Reference, String)
     * @see    	   			#addHomotypicSynonymName(TaxonNameBase, Reference, String)
     * @see    	   			#getSynonymRelations()
     * @see    				#removeSynonym(Synonym)
     * @see    	   			Synonym#getSynonymRelations()
     */
    public SynonymRelationship addHeterotypicSynonymName(TaxonNameBase synonymName){
        return addHeterotypicSynonymName(synonymName, null, null, null);
    }

    /**
     * Creates a new {@link Synonym synonym} (with the given {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name}),
     * a new {@link SynonymRelationship synonym relationship} (with the new synonym, with the
     * {@link SynonymRelationshipType#HETEROTYPIC_SYNONYM_OF() "is heterotypic synonym of" relationship type}
     * and with the {@link eu.etaxonomy.cdm.model.reference.Reference reference source}
     * on which the relationship assertion is based), returns the relationship
     * and adds it to the set of {@link #getSynonymRelations() synonym relationships} assigned
     * to <i>this</i> taxon. The new synonym will have the same {@link TaxonBase#getSec() concept reference}
     * as <i>this</i> taxon. Furthermore the new synonym relationship will be
     * added to the set of {@link Synonym#getSynonymRelations() synonym relationships} belonging
     * to the created synonym and the taxon name used as synonym will be added
     * to the given {@link name.HomotypicalGroup homotypical group}.<BR>
     * The returned synonym relationship allows to add further information to it.
     *
     * @param synonymName		the taxon name to be used as an heterotypic synonym
     * 							to be added to <i>this</i> taxon's set of synonyms
     * @param homotypicalGroup	the homotypical group to which the taxon name
     * 							of the synonym will be added
     * @param citation			the reference source for the new synonym relationship
     * @param microcitation		the string with the details describing the exact localisation
     * 							within the reference
     * @return 					the created synonym relationship
     * @see    	   				#addHeterotypicSynonymName(TaxonNameBase)
     * @see    	   				#addSynonymName(TaxonNameBase, SynonymRelationshipType, Reference, String)
     * @see    	   				#addSynonymName(TaxonNameBase, SynonymRelationshipType)
     * @see    	   				#addSynonym(Synonym, SynonymRelationshipType)
     * @see    	   				#addSynonym(Synonym, SynonymRelationshipType, Reference, String)
     * @see    	   				#addSynonymRelation(SynonymRelationship)
     * @see    	   				#addHomotypicSynonym(Synonym, Reference, String)
     * @see    	   				#addHomotypicSynonymName(TaxonNameBase, Reference, String)
     * @see    	   				#getSynonymRelations()
     * @see    					#removeSynonym(Synonym)
     * @see    	   				Synonym#getSynonymRelations()
     */
    public SynonymRelationship addHeterotypicSynonymName(TaxonNameBase synonymName, HomotypicalGroup homotypicalGroup, Reference citation, String microCitation){
        Synonym synonym = Synonym.NewInstance(synonymName, this.getSec());
        if (homotypicalGroup != null){
            homotypicalGroup.addTypifiedName(synonymName);
        }
        return addSynonym(synonym, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF(), citation, microCitation);
    }

    /**
     * Creates a new {@link Synonym synonym} (with the given {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name}),
     * a new {@link SynonymRelationship synonym relationship} (with the new synonym, with the
     * {@link SynonymRelationshipType#HOMOTYPIC_SYNONYM_OF() "is homotypic synonym of" relationship type})
     * and with the {@link eu.etaxonomy.cdm.model.reference.Reference reference source}
     * on which the relationship assertion is based), returns the relationship
     * and adds it to the set of {@link #getSynonymRelations() synonym relationships} assigned
     * to <i>this</i> taxon. The new synonym will have the same {@link TaxonBase#getSec() concept reference}
     * as <i>this</i> taxon. Furthermore the new synonym relationship will be
     * added to the set of {@link Synonym#getSynonymRelations() synonym relationships} belonging
     * to the created synonym and the taxon name used as synonym will be added
     * to the same {@link eu.etaxonomy.cdm.model.name.HomotypicalGroup homotypical group} to which the taxon name
     * of <i>this</i> taxon belongs.<BR>
     * The returned synonym relationship allows to add further information to it.
     *
     * @param synonymName	the taxon name to be used as an homotypic synonym
     * 						to be added to <i>this</i> taxon's set of synonyms
     * @param citation		the reference source for the new synonym relationship
     * @param microcitation	the string with the details describing the exact localisation
     * 						within the reference
     * @return 				the created synonym relationship
     * @see    	   			#addHomotypicSynonym(Synonym, Reference, String)
     * @see    	   			#addSynonymName(TaxonNameBase, SynonymRelationshipType, Reference, String)
     * @see    	   			#addSynonymName(TaxonNameBase, SynonymRelationshipType)
     * @see    	   			#addSynonym(Synonym, SynonymRelationshipType)
     * @see    	   			#addSynonym(Synonym, SynonymRelationshipType, Reference, String)
     * @see    	   			#addSynonymRelation(SynonymRelationship)
     * @see    	   			#addHeterotypicSynonymName(TaxonNameBase)
     * @see    	   			#addHeterotypicSynonymName(TaxonNameBase, HomotypicalGroup, Reference, String)
     * @see    	   			#getSynonymRelations()
     * @see    				#removeSynonym(Synonym)
     * @see    	   			Synonym#getSynonymRelations()
     */
    public SynonymRelationship addHomotypicSynonymName(TaxonNameBase synonymName, Reference citation, String microCitation){
        Synonym synonym = Synonym.NewInstance(synonymName, this.getSec());
        return addHomotypicSynonym(synonym, citation, microCitation);
    }

    /**
     * Creates a new {@link SynonymRelationship synonym relationship} (with the given {@link Synonym synonym},
     * with the {@link SynonymRelationshipType#HOMOTYPIC_SYNONYM_OF() "is homotypic synonym of" relationship type}
     * and with the {@link eu.etaxonomy.cdm.model.reference.Reference reference source} on which the relationship
     * assertion is based), returns it and adds it to the set of
     * {@link #getSynonymRelations() synonym relationships} assigned to <i>this</i> taxon.
     * Furthermore the new synonym relationship will be added to the set of
     * {@link Synonym#getSynonymRelations() synonym relationships} belonging to the synonym
     * involved in this synonym relationship and the {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name}
     * used as synonym will be added to the same {@link eu.etaxonomy.cdm.model.name.HomotypicalGroup homotypical group}
     * to which the taxon name of <i>this</i> taxon belongs.<BR>
     * The returned synonym relationship allows to add further information to it.
     *
     * @param synonym		the synonym involved in the "is homotypic synonym of" relationship to be created
     * 						and added to <i>this</i> taxon's synonym relationships set
     * @param citation		the reference source for the new synonym relationship
     * @param microcitation	the string with the details describing the exact localisation within the reference
     * @return 				the created synonym relationship
     * @see    	   			#addHomotypicSynonymName(TaxonNameBase, Reference, String)
     * @see    	   			#addSynonym(Synonym, SynonymRelationshipType)
     * @see    	   			#addSynonym(Synonym, SynonymRelationshipType, Reference, String)
     * @see    	   			#addSynonymName(TaxonNameBase, SynonymRelationshipType, Reference, String)
     * @see    	   			#addSynonymName(TaxonNameBase, SynonymRelationshipType)
     * @see    	   			#addSynonymRelation(SynonymRelationship)
     * @see    	   			#addHeterotypicSynonymName(TaxonNameBase)
     * @see    	   			#addHeterotypicSynonymName(TaxonNameBase, HomotypicalGroup, Reference, String)
     * @see    	   			#getSynonymRelations()
     * @see    				#removeSynonym(Synonym)
     * @see    	   			Synonym#getSynonymRelations()
     */
    public SynonymRelationship addHomotypicSynonym(Synonym synonym, Reference citation, String microCitation){
    if (this.getName() != null){
            if (this.getName().getHomotypicalGroup().getTypifiedNames().isEmpty()){
                this.getName().getHomotypicalGroup().getTypifiedNames().add(this.getName());

            }
            this.getName().getHomotypicalGroup().addTypifiedName(synonym.getName());

        }
    	SynonymRelationship synRel = null;
    	if (!this.getSynonyms().contains(synonym)){
    		synRel = addSynonym(synonym, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF(), citation, microCitation);
    	} else{
    		logger.warn("The synonym is already related to the taxon.");

    	}
        return synRel;
    }

    /**
     * Like {@link #removeSynonym(Synonym, boolean)} with <code>removeSynonymNameFromHomotypicalGroup</code> set to true.
     * @see #removeSynonym(Synonym, boolean)
     */
    public void removeSynonym(Synonym synonym){
        removeSynonym(synonym, true);
    }

    /**
     * Removes the element(s) from the set of {@link SynonymRelationship synonym relationships}
     * assigned to <i>this</i> ("accepted/valid") taxon in which the given synonym is involved.
     * Due to bidirectionality the same synonym relationships will also be
     * removed from the set of synonym relationships assigned to the
     * {@link Synonym#getSynonymRelations() synonym} involved in the relationship. Furthermore the content of
     * the {@link SynonymRelationship#getAcceptedTaxon() accepted taxon} attribute and of the
     * {@link SynonymRelationship#getSynonym() synonym} attribute within the synonym relationships
     * themselves will be set to "null".
     *
     * @param  synonym  the synonym involved in the synonym relationship which should be deleted
     * @param removeSynonymNameFromHomotypicalGroup if <code>true</code> the removed synonyms
     * 		name will get a new homotypic group in case it is together with other names in a group.
     * @see     		#getSynonymRelations()
     * @see     		#addSynonym(Synonym, SynonymRelationshipType)
     * @see     		#addSynonym(Synonym, SynonymRelationshipType, Reference, String)
     * @see 			#removeSynonymRelation(SynonymRelationship)
     * @see				#removeSynonymRelation(SynonymRelationship, boolean)
     */
    public void removeSynonym(Synonym synonym, boolean removeSynonymNameFromHomotypicalGroup){
        Set<SynonymRelationship> synonymRelationships = new HashSet<SynonymRelationship>();
        synonymRelationships.addAll(this.getSynonymRelations());
        for(SynonymRelationship synonymRelationship : synonymRelationships){
            if (synonymRelationship.getAcceptedTaxon().equals(this) && synonymRelationship.getSynonym().equals(synonym)){
                this.removeSynonymRelation(synonymRelationship, removeSynonymNameFromHomotypicalGroup);
            }
        }
    }


    /**
     * Retrieves the ordered list (depending on the date of publication) of
     * homotypic {@link Synonym synonyms} (according to the same {@link eu.etaxonomy.cdm.model.reference.Reference reference}
     * as for <i>this</i> taxon) under the condition that the {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon names}
     * of these synonyms and the taxon name of <i>this</i> taxon belong to the
     * same {@link eu.etaxonomy.cdm.model.name.HomotypicalGroup homotypical group}.
     *
     * @return		the ordered list of homotypic synonyms
     * @see			#getHomotypicSynonymsByHomotypicRelationship()
     * @see			#getSynonyms()
     * @see			#getHomotypicSynonymyGroups()
     * @see			eu.etaxonomy.cdm.model.name.HomotypicalGroup
     * @see			eu.etaxonomy.cdm.model.name.HomotypicalGroup#getSynonymsInGroup(Reference)
     */
    @Transient
    public List<Synonym> getHomotypicSynonymsByHomotypicGroup(){
        if (this.getHomotypicGroup() == null){
            return null;
        }else{
            return this.getSynonymsInGroup(this.getHomotypicGroup());
        }
    }

    /**
     * Retrieves the ordered list (depending on the date of publication) of
     * homotypic {@link Synonym synonyms} (according to the same {@link eu.etaxonomy.cdm.model.reference.Reference reference}
     * as for <i>this</i> taxon) under the condition that these synonyms and
     * <i>this</i> taxon are involved in {@link SynonymRelationship synonym relationships} with an
     * "is homotypic synonym of" {@link SynonymRelationshipType#HOMOTYPIC_SYNONYM_OF() synonym relationship type}.
     *
     * @return		the ordered list of homotypic synonyms
     * @see			#getHomotypicSynonymsByHomotypicGroup()
     * @see			#getSynonyms()
     * @see			#getHomotypicSynonymyGroups()
     * @see			SynonymRelationshipType
     */
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
     * Returns the ordered list of all {@link eu.etaxonomy.cdm.model.name.HomotypicalGroup homotypical groups} {@link Synonym synonyms} of
     * <i>this</i> taxon belong to. {@link eu.etaxonomy.cdm.model.name.TaxonNameBase Taxon names} of homotypic synonyms
     * belong to the same homotypical group as the taxon name of <i>this</i>
     * taxon. Taxon names of heterotypic synonyms belong to at least one other
     * homotypical group. <BR>
     * The list returned is ordered according to the date of publication of the
     * first published name within each homotypical group.
     *
     * @see			#getHeterotypicSynonymyGroups()
     * @see			#getSynonyms()
     * @see			eu.etaxonomy.cdm.model.name.HomotypicalGroup
     */
    @Transient
    public List<HomotypicalGroup> getHomotypicSynonymyGroups(){
        List<HomotypicalGroup> result = new ArrayList<HomotypicalGroup>();
        result.add(this.getHomotypicGroup());
        for (TaxonNameBase taxonNameBase :this.getSynonymNames()){
            if (taxonNameBase != null) {
                if (!result.contains(taxonNameBase.getHomotypicalGroup())){
                    result.add(taxonNameBase.getHomotypicalGroup());
                }
            } // TODO: give error message to user
        }
        // TODO: sort list according to date of first published name within each group
        return result;
    }



    /**
     * The status of this taxon is unknown it could also be some kind of synonym.
     * @return the taxonStatusUnknown
     */
    public boolean isTaxonStatusUnknown() {
        return taxonStatusUnknown;
    }

    /**
     * @param taxonStatusUnknown the taxonStatusUnknown to set
     */
    public void setTaxonStatusUnknown(boolean taxonStatusUnknown) {
        this.taxonStatusUnknown = taxonStatusUnknown;
    }




    public boolean isUnplaced() {
        return unplaced;
    }

    @Override
    @Transient
    public boolean isOrphaned() {

        if(taxonNodes == null || taxonNodes.isEmpty()) {
            if(getRelationsFromThisTaxon().isEmpty() && getRelationsToThisTaxon().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void setUnplaced(boolean unplaced) {
        this.unplaced = unplaced;
    }

    public boolean isExcluded() {
        return excluded;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    /**
     * Returns the ordered list of all {@link eu.etaxonomy.cdm.model.name.HomotypicalGroup homotypical groups}
     * that contain {@link Synonym synonyms} that are heterotypic to <i>this</i> taxon.
     * {@link eu.etaxonomy.cdm.model.name.TaxonNameBase Taxon names} of heterotypic synonyms
     * belong to a homotypical group which cannot be the homotypical group to which the
     * taxon name of <i>this</i> taxon belongs. This method returns the same
     * list as the {@link #getHomotypicSynonymyGroups() getHomotypicSynonymyGroups} method
     * but without the homotypical group to which the taxon name of <i>this</i> taxon
     * belongs.<BR>
     * The list returned is ordered according to the date of publication of the
     * first published name within each homotypical group.
     *
     * @see			#getHeterotypicSynonymyGroups()
     * @see			#getSynonyms()
     * @see			SynonymRelationshipType#HETEROTYPIC_SYNONYM_OF()
     * @see			eu.etaxonomy.cdm.model.name.HomotypicalGroup
     */
    @Transient
    public List<HomotypicalGroup> getHeterotypicSynonymyGroups(){
        List<HomotypicalGroup> list = getHomotypicSynonymyGroups();
        list.remove(this.getHomotypicGroup());
        //sort
        Map<Synonym, HomotypicalGroup> map = new HashMap<Synonym, HomotypicalGroup>();
        for (HomotypicalGroup homotypicalGroup: list){
            List<Synonym> synonymList = getSynonymsInGroup(homotypicalGroup);
            if (synonymList.size() > 0){
                map.put(synonymList.get(0), homotypicalGroup);
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

    /**
     * Retrieves the ordered list (depending on the date of publication) of
     * {@link taxon.Synonym synonyms} (according to a given reference)
     * the {@link TaxonNameBase taxon names} of which belong to the homotypical group.
     * If other names are part of the group that are not considered synonyms of
     * <i>this</i> taxon, then they will not be included in
     * the result set.
     *
     * @param homoGroup
     * @see			TaxonNameBase#getSynonyms()
     * @see			TaxonNameBase#getTaxa()
     * @see			taxon.Synonym
     */
    @Transient
    public List<Synonym> getSynonymsInGroup(HomotypicalGroup homotypicGroup){
        List<Synonym> result = new ArrayList<Synonym>();

        for (TaxonNameBase<?, ?>name : homotypicGroup.getTypifiedNames()){
            for (Synonym synonym : name.getSynonyms()){
                for(SynonymRelationship synRel : synonym.getSynonymRelations()){
                    if (synRel.getAcceptedTaxon().equals(this)){
                        result.add(synRel.getSynonym());
                    }
                }
            }
        }
        Collections.sort(result, new TaxonComparator());
        return result;
    }


    /**
     * Returns the image gallery description. If no image gallery exists, a new one is created using the
     * defined title and adds the string "-Image Gallery" to the title.</BR>
     * If multiple image galleries exist an arbitrary one is choosen.
     * @param title
     * @return
     */
    @Transient
    public TaxonDescription getOrCreateImageGallery(String title){
        return getOrCreateImageGallery(title, true, false);
    }

    /**
     * Returns the image gallery description. If no image gallery exists, a new one is created using the
     * defined title.</BR>
     * If onlyTitle == true we look only for an image gallery with this title, create a new one otherwise.
     * If multiple image galleries exist that match the conditions an arbitrary one is choosen.
     * @param title
     * @param onlyTitle
     * @param if true, the String "Image Gallery
     * @return
     */
    @Transient
    public TaxonDescription getOrCreateImageGallery(String title, boolean addImageGalleryToTitle, boolean onlyTitle){
        TaxonDescription result = null;
        String titleCache = (title == null) ? "Image Gallery" : title;
        if (title != null && addImageGalleryToTitle){
            titleCache = titleCache+ "-Image Gallery";
        }
        Set<TaxonDescription> descriptionSet = this.getDescriptions();
        for (TaxonDescription desc: descriptionSet){
            if (desc.isImageGallery()){
                if (onlyTitle && ! titleCache.equals(desc.getTitleCache())){
                    continue;
                }
                result = desc;
                if (onlyTitle && titleCache.equals(desc.getTitleCache())){
                    break;
                }
            }
        }
        if (result == null){
            result = TaxonDescription.NewInstance();
            result.setTitleCache(titleCache, true);
            this.addDescription(result);
            result.setImageGallery(true);
        }
        return result;
    }
    //*********************** CLONE ********************************************************/


    /**
     * Clones <i>this</i> taxon. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> taxon by
     * modifying only some of the attributes.<BR><BR>
     * The TaxonNodes are not cloned, the list is empty.<BR>
     * (CAUTION: this behaviour needs to be discussed and may change in future).<BR><BR>
     * The taxon relationships and synonym relationships are cloned <BR>
     *
     * @see eu.etaxonomy.cdm.model.taxon.TaxonBase#clone()
     * @see eu.etaxonomy.cdm.model.media.IdentifiableEntity#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        Taxon result;
        result = (Taxon)super.clone();

        result.setRelationsFromThisTaxon(new HashSet<TaxonRelationship>());

        for (TaxonRelationship fromRelationship : this.getRelationsFromThisTaxon()){
            TaxonRelationship newRelationship = (TaxonRelationship)fromRelationship.clone();
            newRelationship.setRelatedFrom(result);
            result.relationsFromThisTaxon.add(newRelationship);
        }

        result.setRelationsToThisTaxon(new HashSet<TaxonRelationship>());
        for (TaxonRelationship toRelationship : this.getRelationsToThisTaxon()){
            TaxonRelationship newRelationship = (TaxonRelationship)toRelationship.clone();
            newRelationship.setRelatedTo(result);
            result.relationsToThisTaxon.add(newRelationship);
        }


        result.synonymRelations = new HashSet<SynonymRelationship>();
        for (SynonymRelationship synRelationship : this.getSynonymRelations()){
            SynonymRelationship newRelationship = (SynonymRelationship)synRelationship.clone();
            newRelationship.setRelatedTo(result);
            result.synonymRelations.add(newRelationship);
        }


        result.taxonNodes = new HashSet<TaxonNode>();

        /*for (TaxonNode taxonNode : this.getTaxonNodes()){
            TaxonNode newTaxonNode = (TaxonNode)taxonNode.clone();
            newTaxonNode.setTaxon(result);
            result.addTaxonNode(newTaxonNode);
        }*/

        return result;

    }

    public void clearDescriptions() {
		this.descriptions = new HashSet<TaxonDescription>();
	}

    @Override
    public void setCacheStrategy(ITaxonCacheStrategy<Taxon> cacheStrategy){
    	this.cacheStrategy = cacheStrategy;
    }
}