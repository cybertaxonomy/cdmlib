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
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.FetchType;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.ClassBridges;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.ReflectionUtils;

import eu.etaxonomy.cdm.compare.taxon.HomotypicGroupTaxonComparator;
import eu.etaxonomy.cdm.compare.taxon.TaxonComparator;
import eu.etaxonomy.cdm.hibernate.search.GroupByTaxonClassBridge;
import eu.etaxonomy.cdm.hibernate.search.TaxonRelationshipClassBridge;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.CdmClass;
import eu.etaxonomy.cdm.model.common.IRelated;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IDescribable;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.ITaxonNameBase;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.ICdmTarget;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.taxon.ITaxonCacheStrategy;

/**
 * The class for "accepted/correct" {@link TaxonBase taxa} (only these taxa according to
 * the opinion of the {@link eu.etaxonomy.cdm.model.reference.Reference reference} can build a classification).
 * An {@link java.lang.Iterable interface} is supported to iterate through taxonomic children.<BR>
 * Splitting taxa in "accepted/correct" and {@link Synonym "synonyms"} makes it easier to handle
 * particular relationships between ("accepted/correct") taxa on the one hand
 * and between ("synonym") taxa and ("accepted/correct") taxa on the other.
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:56
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Taxon", propOrder = {
    "taxonNodes",
    "synonyms",
    "relationsFromThisTaxon",
    "relationsToThisTaxon",
    "descriptions",
    "conceptId",
    "conceptDefinitions",
    "conceptStatus",
    "taxonTypes",
    "currentConceptPeriod"
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
            implements IRelated<RelationshipBase>, IDescribable<TaxonDescription>, ICdmTarget{

    private static final long serialVersionUID = -584946869762749006L;
    private static final Logger logger = LogManager.getLogger();

    private static final TaxonComparator defaultTaxonComparator = new TaxonComparator();

    @XmlElementWrapper(name = "Descriptions")
    @XmlElement(name = "Description")
    @OneToMany(mappedBy="taxon", fetch= FetchType.LAZY)
    @Cascade({CascadeType.DELETE})
    @NotNull
    @ContainedIn
    private Set<TaxonDescription> descriptions = new HashSet<>();

    // all related synonyms
    @XmlElementWrapper(name = "Synonyms")
    @XmlElement(name = "Synonym")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="acceptedTaxon", fetch=FetchType.LAZY, orphanRemoval=false) //we allow synonyms to stay on their own for dirty data and for intermediate states during e.g. imports
    @NotNull
    @Valid
    @ContainedIn
    private Set<Synonym> synonyms = new HashSet<>();

    // all taxa relations with rel.fromTaxon==this
    @XmlElementWrapper(name = "RelationsFromThisTaxon")
    @XmlElement(name = "FromThisTaxonRelationship")
    @OneToMany(mappedBy="relatedFrom", fetch=FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @NotNull
//    @Valid
    @ContainedIn
    private Set<TaxonRelationship> relationsFromThisTaxon = new HashSet<>();

    // all taxa relations with rel.toTaxon==this
    @XmlElementWrapper(name = "RelationsToThisTaxon")
    @XmlElement(name = "ToThisTaxonRelationship")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="relatedTo", fetch=FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
//    @Valid
    @ContainedIn
    private Set<TaxonRelationship> relationsToThisTaxon = new HashSet<>();

    @XmlAttribute(name= "taxonStatusUnknown")
    private boolean taxonStatusUnknown = false;
    /**
     * The status of this taxon is unknown it could also be some kind of synonym.
     * @return the taxonStatusUnknown
     */
    public boolean isTaxonStatusUnknown() {return taxonStatusUnknown;}
     /** @see #isTaxonStatusUnknown()*/
    public void setTaxonStatusUnknown(boolean taxonStatusUnknown) {this.taxonStatusUnknown = taxonStatusUnknown;}

    @XmlElementWrapper(name = "taxonNodes")
    @XmlElement(name = "taxonNode")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="taxon", fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    @IndexedEmbedded
    private Set<TaxonNode> taxonNodes = new HashSet<>();

    private String conceptId;

    @XmlAttribute(name ="TaxonType")
    @NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumSetUserType",
        parameters = {@org.hibernate.annotations.Parameter(name="enumClass", value="eu.etaxonomy.cdm.model.taxon.TaxonType")}
    )
    @Audited
    private EnumSet<TaxonType> taxonTypes = EnumSet.noneOf(TaxonType.class);

    @XmlAttribute(name ="ConceptDefinition")
    @NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumSetUserType",
        parameters = {@org.hibernate.annotations.Parameter(name="enumClass", value="eu.etaxonomy.cdm.model.taxon.ConceptDefinition")}
    )
    @Audited
    private EnumSet<ConceptDefinition> conceptDefinitions = EnumSet.noneOf(ConceptDefinition.class);

    @XmlAttribute(name ="ConceptStatus")
    @NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumSetUserType",
        parameters = {@org.hibernate.annotations.Parameter(name="enumClass", value="eu.etaxonomy.cdm.model.taxon.ConceptStatus")}
    )
    @Audited
    private EnumSet<ConceptStatus> conceptStatus = EnumSet.noneOf(ConceptStatus.class);

    private TimePeriod currentConceptPeriod = TimePeriod.NewInstance();

// ************************* FACTORY METHODS ********************************/

    /**
     * @see #NewInstance(TaxonName, Reference)
     * @param taxonName
     * @param sec
     * @return
     */
    public static Taxon NewInstance(ITaxonNameBase taxonName, Reference sec){
        return NewInstance(TaxonName.castAndDeproxy(taxonName), sec);
    }

    /**
     * Creates a new (accepted/valid) taxon instance with
     * the {@link eu.etaxonomy.cdm.model.name.TaxonName taxon name} used and the {@link eu.etaxonomy.cdm.model.reference.Reference reference}
     * using it.
     *
     * @param  taxonName	the taxon name used
     * @param  sec				the reference using the taxon name
     * @see    					#Taxon(TaxonName, Reference)
     */
    public static Taxon NewInstance(TaxonName taxonName, Reference sec){
        Taxon result = new Taxon(taxonName, sec);
        return result;
    }

    /**
     * Creates a new Taxon for the given name, secundum reference and secundum detail
     * @param taxonName
     * @param sec
     * @param secMicroReference
     * @see #
     */
    public static Taxon NewInstance(TaxonName taxonName, Reference sec, String secMicroReference){
        Taxon result = new Taxon(taxonName, sec, secMicroReference);
        return result;
    }

    /**
     * Creates a new taxon instance with an unknown status (accepted/synonym) and with
     * the {@link eu.etaxonomy.cdm.model.name.TaxonName taxon name} used and the {@link eu.etaxonomy.cdm.model.reference.Reference reference}
     * using it.
     *
     * @param  taxonName	the taxon name used
     * @param  sec				the reference using the taxon name
     * @see    					#Taxon(TaxonName, Reference)
     */
    public static Taxon NewUnknownStatusInstance(TaxonName taxonName, Reference sec){
        Taxon result = new Taxon(taxonName, sec);
        result.setTaxonStatusUnknown(true);
        return result;
    }
// ************* CONSTRUCTORS *************/

    //for hibernate use only, *packet* private required by bytebuddy
    //TODO should be private, but still produces Spring init errors
    @Deprecated
    Taxon(){}

    private Taxon(TaxonName taxonName, Reference sec){
        super(taxonName, sec, null);
    }

    private Taxon(TaxonName taxonName, Reference sec, String secMicroReference){
        super(taxonName, sec, secMicroReference);
    }

//********* METHODS **************************************/

    public String getConceptId() {
        return conceptId;
    }
    public void setConceptId(String conceptId) {
        this.conceptId = conceptId;
    }

    public TimePeriod getCurrentConceptPeriod() {
        return currentConceptPeriod;
    }
    public void setCurrentConceptPeriod(TimePeriod currentConceptPeriod) {
        this.currentConceptPeriod = currentConceptPeriod;
    }

// ************************ Concept Defintion ************************/

    public boolean isHomotypicGroups() {
        return ConceptDefinition.includesType(conceptDefinitions, ConceptDefinition.HOMOTYPIC_GROUP);
    }
    public void setHomotypicGroups(boolean isHomotypicGroups) {
        setConceptDefinition(ConceptDefinition.HOMOTYPIC_GROUP, isHomotypicGroups);
    }

    //calling it descriptionConcept not Descriptions to avoid name clash with setter for TaxonDescriptions
    public boolean isDescriptionConcept() {
        return ConceptDefinition.includesType(conceptDefinitions, ConceptDefinition.DESCRIPTION);
    }
    public void setDescriptionConcept(boolean isDescriptions) {
        setConceptDefinition(ConceptDefinition.DESCRIPTION, isDescriptions);
    }

    protected EnumSet<ConceptDefinition> getConceptDefinition() {
        return conceptDefinitions;
    }

    /**
     * for know it is private and the boolean getters and setters should be used instead.
     * If you make it public make sure to guarantee that any change to the enum set results
     * in a new enum set (see also {@link #newEnumSet(EnumSet, ConceptDefinition, ConceptDefinition)}
     * and that the client is aware of the enum set being immutable.
     */
    private void setConceptDefinitions(EnumSet<ConceptDefinition> conceptDefinitions){
        this.conceptDefinitions = conceptDefinitions;
    }

    /**
     * Sets the value for taxon concept definitions
     * @param conceptDefinition the concept definition
     * @param value the value if this taxon has this concept definition (<code>true</code>) or not (<code>false</code>)
     */
    protected void setConceptDefinition(ConceptDefinition conceptDefinition, boolean value) {
        if (value && !this.conceptDefinitions.contains(conceptDefinition)){
            setConceptDefinitions(newEnumSet(this.conceptDefinitions, conceptDefinition, null));
        }else if (!value && this.conceptDefinitions.contains(conceptDefinition)){
            setConceptDefinitions(newEnumSet(this.conceptDefinitions, null, conceptDefinition));
        }else{
            return;
        }
    }

// ************************ TaxonType ********************************/

    public boolean isConcept() {
        return TaxonType.includesType(taxonTypes, TaxonType.CONCEPT);
    }
    public void setConcept(boolean isConcept) {
        setTaxonType(TaxonType.CONCEPT, isConcept);
    }

    public boolean isNameUsage() {
        return TaxonType.includesType(taxonTypes, TaxonType.NAME_USAGE);
    }
    public void setNameUsage(boolean isNameUsage) {
        setTaxonType(TaxonType.NAME_USAGE, isNameUsage);
    }

    protected EnumSet<TaxonType> getTaxonTypes() {
        return taxonTypes;
    }

    /**
     * for know it is private and the boolean getters and setters should be used instead.
     * If you make it public make sure to guarantee that any change to the enum set results
     * in a new enum set (see also {@link #newEnumSet(EnumSet, TaxonType, TaxonType)}
     * and that the client is aware of the enum set being immutable.
     */
    private void setTaxonTypes(EnumSet<TaxonType> taxonTypes){
        this.taxonTypes = taxonTypes;
    }

    /**
     * Sets the value for taxon types
     * @param type the taxon type
     * @param value the value if this taxon has this type (<code>true</code>) or not (<code>false</code>)
     */
    protected void setTaxonType(TaxonType type, boolean value) {
        if (value && !this.taxonTypes.contains(type)){
            setTaxonTypes(newEnumSet(this.taxonTypes, type, null));
        }else if (!value && this.taxonTypes.contains(type)){
            setTaxonTypes(newEnumSet(this.taxonTypes, null, type));
        }else{
            return;
        }
    }

// ************************ Concept Status ********************************/

    public boolean isPersistent() {
        return ConceptStatus.includesType(conceptStatus, ConceptStatus.PERSISTENT);
    }
    public void setPersistent(boolean isPersistent) {
        setConceptStatus(ConceptStatus.PERSISTENT, isPersistent);
    }
    public boolean isSupportsProvenance() {
        return ConceptStatus.includesType(conceptStatus, ConceptStatus.SUPPORTS_PROVENANCE);
    }
    public void setSupportsProvenance(boolean isSupportsProvenance) {
        setConceptStatus(ConceptStatus.SUPPORTS_PROVENANCE, isSupportsProvenance);
    }
    public boolean isCurrentConcept() {
        return ConceptStatus.includesType(conceptStatus, ConceptStatus.CURRENT);
    }
    public void setCurrentConcept(boolean isCurrentConcept) {
        setConceptStatus(ConceptStatus.CURRENT, isCurrentConcept);
    }

    protected EnumSet<ConceptStatus> getConceptStatus() {
        return conceptStatus;
    }

    /**
     * for know it is private and the boolean getters and setters should be used instead.
     * If you make it public make sure to guarantee that any change to the enum set results
     * in a new enum set (see also {@link #newEnumSet(EnumSet, CdmClass, CdmClass)}
     * and that the client is aware of the enum set being immutable.
     */
    private void setConceptStatus(EnumSet<ConceptStatus> conceptStatus){
        this.conceptStatus = conceptStatus;
    }

    /**
     * Sets the value for concept status
     * @param conceptStatus the concept status
     * @param value the value if this concept has this status (<code>true</code>) or not (<code>false</code>)
     */
    protected void setConceptStatus(ConceptStatus conceptStatus, boolean value) {
        if (value && !this.conceptStatus.contains(conceptStatus)){
            setConceptStatus(newEnumSet(this.conceptStatus, conceptStatus, null));
        }else if (!value && this.conceptStatus.contains(conceptStatus)){
            setConceptStatus(newEnumSet(this.conceptStatus, null, conceptStatus));
        }else{
            return;
        }
    }

// ****************************************************************/

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
            descriptions = new HashSet<>();
        }
        return descriptions;
    }

    /**
     * Returns the default description if exist {@link eu.etaxonomy.cdm.model.description.TaxonDescription taxon descriptions}
     * concerning <i>this</i> taxon.
     *
     * @see eu.etaxonomy.cdm.model.description.TaxonDescription#getTaxon()
     */
    @Transient
    public TaxonDescription getDefaultDescription() {
        if(descriptions == null) {
            return null;
        }

        Set<TaxonDescription> descriptions= getDescriptions();
        for (TaxonDescription desc: descriptions) {
            if (desc.isDefault()) {
                return desc;
            }
        }
        return null;
    }

    public Set<TaxonDescription> getDescriptions(DescriptionType type) {
        Set<TaxonDescription> result = new HashSet<>();
        for (TaxonDescription description : getDescriptions()){
            if (description.getTypes().contains(type)){
                result.add(description);
            }
        }
        return result;
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

    public boolean hasDefaultDescription() {
        return !descriptions.stream().filter(d -> d.isDefault()).collect(Collectors.toList()).isEmpty();
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
        List<TaxonNode> removeNodes = new ArrayList<>();
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

    public TaxonNode getTaxonNode(Classification classification) {
        if (classification == null){
            return null;
        }
        for (TaxonNode node : this.getTaxonNodes()){
            if (classification.equals(node.getClassification())){
                return node;
            }
        }
        return null;
    }

    /**
     * Returns the set of all {@link Synonym synonyms}
     * for which <i>this</i> ("accepted/valid") taxon is the accepted taxon.
     *
     * @see    #addSynonym(Synonym, SynonymType)
     * @see    #removeSynonym(Synonym)
     */
    public Set<Synonym> getSynonyms() {
        if(synonyms == null) {
            this.synonyms = new HashSet<>();
        }
        return synonyms;
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
            this.relationsFromThisTaxon = new HashSet<>();
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
            this.relationsToThisTaxon = new HashSet<>();
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
        Set<TaxonRelationship> rels = new HashSet<>();
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
        Set<TaxonRelationship> relations = new HashSet<>();

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
     *
     * @param  rel  the taxon relationship which should be removed from one
     * 				of both sets
     * @see    		#getTaxonRelations()
     * @see    		eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedFrom()
     * @see    		eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedTo()
     *
     */
    public void removeTaxonRelation(TaxonRelationship rel) {
        this.relationsToThisTaxon.remove(rel);
        this.relationsFromThisTaxon.remove(rel);
        Taxon fromTaxon = rel.getFromTaxon();
        Taxon toTaxon = rel.getToTaxon();

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
     *
     * @param rel  the taxon relationship to be added to one of <i>this</i> taxon's taxon relationships sets
     * @see    	   #addTaxonRelation(Taxon, TaxonRelationshipType, Reference, String)
     * @see    	   #getTaxonRelations()
     * @see    	   #getRelationsFromThisTaxon()
     * @see    	   #getRelationsToThisTaxon()
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
                }else if (this.equals(toTaxon)){
                    relationsToThisTaxon.add(rel);
                    // also add relation to other taxon object
                    if (fromTaxon!=null){
                        fromTaxon.addTaxonRelation(rel);
                    }
                }
            }else if (toTaxon == null || fromTaxon == null){
                if (toTaxon == null){
                    toTaxon = this;
                    relationsToThisTaxon.add(rel);
                    if (fromTaxon!= null){
                        fromTaxon.addTaxonRelation(rel);
                    }
                }else if (fromTaxon == null && toTaxon != null){
                    fromTaxon = this;
                    relationsFromThisTaxon.add(rel);
                    toTaxon.addTaxonRelation(rel);
                }
            }
        }
    }

    @Override
    @Deprecated //for inner use by RelationshipBase only
    public void addRelationship(RelationshipBase rel){
        if (rel instanceof TaxonRelationship){
            addTaxonRelation((TaxonRelationship)rel);
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
     * @param misappliedNameTaxon	the taxon which plays the source role in the new taxon relationship
     * @param citation				the reference source for the new taxon relationship
     * @param microcitation			the string with the details describing the exact localisation within the reference
     * @return
     * @see    	   					#getMisappliedNames()
     * @see                         #addProParteMisappliedName(Taxon, Reference, String)
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
     * Creates a new {@link TaxonRelationship taxon relationship} (with {@link TaxonRelationshipType taxon relationship type}
     * "pro parte misapplied name for") instance where <i>this</i> taxon plays the target role
     * and adds it to the set of {@link #getRelationsToThisTaxon() taxon relationships to <i>this</i> taxon}.
     * The taxon relationship will also be added to the set of taxon
     * relationships to the other (pro parte misapplied name) taxon involved in the created relationship.
     *
     * @param proParteMisappliedNameTaxon   the taxon which plays the source role in the new taxon relationship
     * @param citation              the reference source for the new taxon relationship
     * @param microcitation         the string with the details describing the exact localisation within the reference
     * @return
     * @see                         #addMisappliedName(Taxon, Reference, String)
     * @see                         #getMisappliedNames()
     * @see                         #addTaxonRelation(Taxon, TaxonRelationshipType, Reference, String)
     * @see                         #addTaxonRelation(TaxonRelationship)
     * @see                         #getTaxonRelations()
     * @see                         #getRelationsFromThisTaxon()
     * @see                         #getRelationsToThisTaxon()
     */
    public TaxonRelationship addProParteMisappliedName(Taxon proParteMisappliedNameTaxon, Reference citation, String microcitation) {
        return proParteMisappliedNameTaxon.addTaxonRelation(this, TaxonRelationshipType.PRO_PARTE_MISAPPLIED_NAME_FOR(), citation, microcitation);
    }

    /**
     * Creates a new {@link TaxonRelationship taxon relationship} (with {@link TaxonRelationshipType taxon relationship type}
     * "partial misapplied name for") instance where <i>this</i> taxon plays the target role
     * and adds it to the set of {@link #getRelationsToThisTaxon() taxon relationships to <i>this</i> taxon}.
     * The taxon relationship will also be added to the set of taxon
     * relationships to the other (pro parte misapplied name) taxon involved in the created relationship.
     *
     * @param partialMisappliedNameTaxon   the taxon which plays the source role in the new taxon relationship
     * @param citation              the reference source for the new taxon relationship
     * @param microcitation         the string with the details describing the exact localization within the reference
     * @return
     * @see                         #addMisappliedName(Taxon, Reference, String)
     * @see                         #addProParteMisappliedName(Taxon, Reference, String)
     * @see                         #getMisappliedNames()
     * @see                         #addTaxonRelation(Taxon, TaxonRelationshipType, Reference, String)
     * @see                         #addTaxonRelation(TaxonRelationship)
     * @see                         #getTaxonRelations()
     * @see                         #getRelationsFromThisTaxon()
     * @see                         #getRelationsToThisTaxon()
     */
    public TaxonRelationship addPartialMisappliedName(Taxon partialMisappliedNameTaxon, Reference citation, String microcitation) {
        return partialMisappliedNameTaxon.addTaxonRelation(this, TaxonRelationshipType.PARTIAL_MISAPPLIED_NAME_FOR(), citation, microcitation);
    }

    /**
     * Creates a new {@link TaxonRelationship taxon relationship} (with {@link TaxonRelationshipType taxon relationship type}
     * "pro parte synonym for") instance where <i>this</i> taxon plays the target role
     * and adds it to the set of {@link #getRelationsToThisTaxon() taxon relationships to <i>this</i> taxon}.
     * The taxon relationship will also be added to the set of taxon
     * relationships to the other (pro parte synonym) taxon involved in the created relationship.
     *
     * @param proParteTaxon         the taxon which plays the source role in the new taxon relationship
     * @param citation              the reference source for the new taxon relationship
     * @param microcitation         the string with the details describing the exact localisation within the reference
     * @return
     * @see                         #getMisappliedNames()
     * @see                         #addProParteMisappliedName(Taxon, Reference, String)
     * @see                         #addTaxonRelation(Taxon, TaxonRelationshipType, Reference, String)
     * @see                         #addTaxonRelation(TaxonRelationship)
     * @see                         #getTaxonRelations()
     * @see                         #getRelationsFromThisTaxon()
     * @see                         #getRelationsToThisTaxon()
     */
    public TaxonRelationship addProparteSynonym(Taxon proParteTaxon, Reference citation, String microcitation) {
        return proParteTaxon.addTaxonRelation(this, TaxonRelationshipType.PRO_PARTE_SYNONYM_FOR(), citation, microcitation);
    }

    /**
     * Creates a new {@link TaxonRelationship taxon relationship} instance with
     * {@link TaxonRelationshipType taxon relationship type} {@link TaxonRelationshipType#PARTIAL_SYNONYM_FOR()
     * partial synonym for} where <i>this</i> taxon plays the target role
     * and adds it to the set of {@link #getRelationsToThisTaxon() taxon relationships to <i>this</i> taxon}.
     * The taxon relationship will also be added to the set of taxon
     * relationships to the other (partial synonym) taxon involved in the created relationship.
     *
     * @param partialTaxon         the taxon which plays the source role in the new taxon relationship
     * @param citation             the reference source for the new taxon relationship
     * @param microcitation        the string with the details describing the exact localisation within the reference
     * @return
     * @see                         #addProparteSynonym(Taxon, Reference, String)
     * @see                         #addTaxonRelation(Taxon, TaxonRelationshipType, Reference, String)
     * @see                         #addTaxonRelation(TaxonRelationship)
     * @see                         #getTaxonRelations()
     * @see                         #getRelationsFromThisTaxon()
     * @see                         #getRelationsToThisTaxon()
     */
    public TaxonRelationship addPartialSynonym(Taxon partialTaxon, Reference citation, String microcitation) {
        return partialTaxon.addTaxonRelation(this, TaxonRelationshipType.PARTIAL_SYNONYM_FOR(), citation, microcitation);
    }

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

    @Transient
    public boolean isMisapplicationOnly() {
        if (!getTaxonNodes().isEmpty()){
            return false;
        }
        int nMan = computeMisapliedNameRelations();
        if (nMan > 0 && nMan == this.relationsFromThisTaxon.size() + this.relationsToThisTaxon.size()){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Returns the boolean value indicating whether <i>this</i> taxon is a misapplication
     * (misapplied name) for at least one other taxon.
     */
    // TODO cache as for #hasTaxonomicChildren
    @Transient
    public boolean isMisapplication(){
        return computeMisapliedNameRelations() > 0;
    }

    /**
     * Counts the number of misapplied name relationships (including pro parte and partial
     * misapplied names) where this taxon represents the
     * misapplied name for another taxon.
     * @return
     */
    private int computeMisapliedNameRelations(){
        int count = 0;
        for (TaxonRelationship rel: this.getRelationsFromThisTaxon()){
            if (rel.getType().isAnyMisappliedName()){
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the boolean value indicating whether <i>this</i> taxon is a misapplication
     * (misapplied name) for at least one other taxon.
     */
    // TODO cache as for #hasTaxonomicChildren
    @Transient
    public boolean isProparteSynonym(){
        return computeProparteSynonymRelations() > 0;
    }

    /**
     * Counts the number of misapplied name relationships (including pro parte misapplied
     * names) where this taxon represents the
     * misapplied name for another taxon.
     * @return
     */
    private int computeProparteSynonymRelations(){
        int count = 0;
        for (TaxonRelationship rel: this.getRelationsFromThisTaxon()){
            if (rel.getType().isAnySynonym()){
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
     * {@link Synonym synonym} (true) or not (false). If true the {@link #getSynonyms() set of synonyms}
     * belonging to <i>this</i> ("accepted/valid") taxon is not empty .
     *
     * @see  #getSynonyms()
     * @see  #getSynonymNames()
     * @see  #removeSynonym(Synonym)
     */
    @Transient
    public boolean hasSynonyms(){
        return this.getSynonyms().size() > 0;
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
     * {@link eu.etaxonomy.cdm.model.name.TaxonName taxon name} of which has been erroneously used
     * by its {@link TaxonBase#getSec() taxon reference} to denominate the same real taxon
     * as the one meant by <i>this</i> ("accepted/correct") taxon.
     *
     * @see  #getTaxonRelations()
     * @see  #getRelationsToThisTaxon()
     * @see  #addMisappliedName(Taxon, Reference, String)
     * @param includeNonCongruent if <code>true</code> also those taxa are returned that are related
     * via a non congruent relationship like {@link TaxonRelationshipType#PRO_PARTE_MISAPPLIED_NAME_FOR()
     * pro parte misapplied name}
     */
    @Transient
    public Set<Taxon> getMisappliedNames(boolean includeNonCongruent){
        Set<Taxon> taxa = new HashSet<>();
        Set<TaxonRelationship> rels = this.getRelationsToThisTaxon();
        for (TaxonRelationship rel: rels){
            TaxonRelationshipType relType = rel.getType();
            if ( (includeNonCongruent && relType.isAnyMisappliedName())
                    || relType.equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())){
                taxa.add(rel.getFromTaxon());
            }
        }
        return taxa;
    }

    /**
     * Returns the set of misapplied name relationships in which this taxon
     * plays the role of the correctly accepted taxon (target). A misapplied name is a taxon the
     * {@link eu.etaxonomy.cdm.model.name.TaxonName taxon name} of which has been erroneously used
     * by its {@link TaxonBase#getSec() taxon reference} to denominate the same real taxon
     * as the one meant by <i>this</i> ("accepted/correct") taxon.
     */
    @Transient
    public Set<TaxonRelationship> getMisappliedNameRelations(){
        Set<TaxonRelationship> result = new HashSet<>();
        Set<TaxonRelationship> rels = this.getRelationsToThisTaxon();
        for (TaxonRelationship rel: rels){
            TaxonRelationshipType relType = rel.getType();
            if (relType.isAnyMisappliedName()){
                result.add(rel);
            }
        }
        return result;
    }

    /**
     * Returns the set of taxa playing the target role in {@link TaxonRelationship taxon relationships}
     * (with {@link TaxonRelationshipType taxon relationship type} "misapplied name for"
     * or "pro parte misapplied name for") where
     * <i>this</i> taxon plays the source role. A misapplied name is a taxon the
     * {@link eu.etaxonomy.cdm.model.name.TaxonName taxon name} of which has been erroneously used
     * by its {@link TaxonBase#getSec() taxon reference} to denominate the same real taxon
     * as the one meant by <i>this</i> ("accepted/correct") taxon.

     * @param includeNonCongruent if <code>true</code> also those taxa are returned that are related
     * via a non congruent relationship like {@link TaxonRelationshipType#PRO_PARTE_MISAPPLIED_NAME_FOR()
     * pro parte misapplied name}
     *
     * @see  #getTaxonRelations()
     * @see  #getRelationsToThisTaxon()
     * @see  #addMisappliedName(Taxon, Reference, String)
     * @see  #addProParteMisappliedName(Taxon, Reference, String)
     */
    @Transient
    public Set<Taxon> getTaxaForMisappliedName(boolean includeNonCongruent){
        Set<Taxon> taxa = new HashSet<>();
        Set<TaxonRelationship> rels = this.getRelationsFromThisTaxon();
        for (TaxonRelationship rel: rels){
            TaxonRelationshipType relType = rel.getType();
            if ( (includeNonCongruent && relType.isAnyMisappliedName())
                    || relType.equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())){
                taxa.add(rel.getToTaxon());
            }
        }
        return taxa;
    }

    /**
     * Returns the set of pro parte or partial synonym relationships in which this taxon
     * plays the role of the "correctly" accepted taxon (target).
     *
     * @see #getProParteAndPartialSynonyms()
     * @see #getMisappliedNameRelations()
     */
    @Transient
    public Set<TaxonRelationship> getProParteAndPartialSynonymRelations(){
        Set<TaxonRelationship> result = new HashSet<>();
        Set<TaxonRelationship> rels = this.getRelationsToThisTaxon();
        for (TaxonRelationship rel: rels){
            TaxonRelationshipType relType = rel.getType();
            if (relType.isAnySynonym()){
                result.add(rel);
            }
        }
        return result;
    }

    /**
     * Returns the set of pro parte or partial synonyms in which this taxon
     * plays the role of the "correctly" accepted taxon (target).
     *
     * @see #getProParteAndPartialSynonymRelations()
     * @see #getMisappliedNames(boolean)
     */
    @Transient
    public Set<Taxon> getProParteAndPartialSynonyms(){
        Set<Taxon> synonyms = new HashSet<>();
        Set<TaxonRelationship> rels = this.getProParteAndPartialSynonymRelations();
        for (TaxonRelationship rel: rels){
            synonyms.add(rel.getFromTaxon());
        }
        return synonyms;
    }

    /**
     * Returns the set of all {@link TaxonName taxon names} used as {@link Synonym synonyms}
     * of <i>this</i> ("accepted/valid") taxon.
     *
     * @see    #getSynonyms()
     * @see    #getSynonymsSortedByType()
     * @see    #addSynonymName(TaxonName, SynonymType)
     * @see    #addSynonym(Synonym, SynonymType, Reference, String)
     * @see    #removeSynonym(Synonym)
     */
    @Transient
    public Set<TaxonName> getSynonymNames(){
        Set<TaxonName> names = new HashSet<>();
        for (Synonym syn: this.getSynonyms()){
            names.add(syn.getName());
        }
        return names;
    }

    /**
     * Might be public in future. For the moment protected to ensure that
     * synonym type is always set after refactoring.
     *
     * @param synonym
     */
    protected void addSynonym(Synonym synonym){
        if (! this.equals(synonym.getAcceptedTaxon())){
            synonym.setAcceptedTaxon(this);
        }
        if (!synonyms.contains(synonym)){
            synonyms.add(synonym);
        }
    }

    //TODO full functionality still needs to be checked
    public Synonym addBasionymSynonym(TaxonName basionym, Reference synSec, String synSecDetail){
        SynonymType synonymType = SynonymType.HOMOTYPIC_SYNONYM_OF;
        Synonym synonym = addSynonymName(basionym, synSec, synSecDetail, synonymType);
        TaxonName thisName = this.getName();
        thisName.addBasionym(basionym);
        addSynonym(synonym);
        return synonym;
    }

    /**
     * Adds the given {@link Synonym synonym} to <code>this</code> taxon
     * and changes the {@link SynonymType
     * synonym type} before.
     *
     * @param synonym       the synonym to be added
     * @param synonymType   the synonym type of the synonym to be added. If not <code>null</code>
     *                      and if the synonym already has a type the existing type will be overwritten.<BR>
     *                      If synonymType is {@link SynonymType#HOMOTYPIC_SYNONYM_OF()}
     *                      the homotypic group of the synonym is changed to that of <code>this</code> taxon.<BR>
     *                      To explicitly set the type to <code>null</code> use {@link Synonym#setType(SynonymType)}
     * @see                 #addSynonym(Synonym)
     * @see                 #addSynonym(Synonym, SynonymType, Reference, String)
     * @see                 #addSynonymName(TaxonName, SynonymType)
     * @see                 #addSynonymName(TaxonName, SynonymType, Reference, String)
     * @see                 #addHomotypicSynonymName(TaxonName, Reference, String)
     * @see                 #addHeterotypicSynonymName(TaxonName)
     * @see                 #addHeterotypicSynonymName(TaxonName, Reference, String, HomotypicalGroup)
     * @see                 #getSynonyms()
     * @see                 #removeSynonym(Synonym)
     * @see                 Synonym#getAcceptedTaxon()
     */
    public void addSynonym(Synonym synonym, SynonymType synonymType){
        synonym.setType(synonymType); //must be set before as otherwise merging of homotypical groups may not work correctly in Synonym.checkHomotypic()
        addSynonym(synonym);
    }

    /**
     * Adds the given {@link Synonym synonym} with the given {@link SynonymType
     * synonym relationship type}
     *
     * @param synonym		the synonym to be added
     * @param synonymType	the synonym  type of the synonym to be added. If not null
     *                      and if the synonym already has a type the existing type will be overwritten.
//     * @param citation		the reference source for the new synonym relationship
//     * @param microcitation	the string with the details describing the exact localization within the reference
     * @see    	   			#addSynonym(Synonym)
     * @see    	   			#addSynonym(Synonym, SynonymType, Reference, String)
     * @see    	   			#addSynonymName(TaxonName, SynonymType)
     * @see    	   			#addSynonymName(TaxonName, SynonymType, Reference, String)
     * @see    	   			#addHomotypicSynonymName(TaxonName, Reference, String)
     * @see    	   			#addHeterotypicSynonymName(TaxonName)
     * @see    	   			#addHeterotypicSynonymName(TaxonName, HomotypicalGroup, Reference, String)
     * @see    	   			#getSynonyms()
     * @see    				#removeSynonym(Synonym)
     * @see    	   			Synonym#getAcceptedTaxon()
     */
    private void addSynonym(Synonym synonym, SynonymType synonymType, Reference newSecReference, String newSecMicroReference){
        if (newSecReference != null){
            synonym.setSec(newSecReference);
        }
        if (newSecMicroReference != null){
            synonym.setSecMicroReference(newSecMicroReference);
        }
        addSynonym(synonym, synonymType);
        return;
    }

    /**
     * Creates a new {@link Synonym synonym} to <code>this</code> {@link Taxon taxon}) using the
     * given {@link TaxonName synonym name} and with the given
     * {@link SynonymType synonym type}. If the later is
     * {@link SynonymType#HOMOTYPIC_SYNONYM_OF() homotypic synonym}
     * the name will be added to the same {@link HomotypicalGroup homotypical group}
     * as the <code>this</code> accepted taxon.<BR>
     * The secundum reference of the new synonym is taken from <code>this</code> taxon.
     * A secundum detail is not set.
     *
     * @param synonymName	the taxon name to be used as a synonym to be added
     * 						to <i>this</i> taxon's set of synonyms
     * @param synonymType	the synonym  type of the synonym
     * 						relationship to be added
     * @return 				the created synonym
     * @see    	   			#addSynonymName(TaxonName, SynonymType, Reference, String)
     * @see    	   			#addSynonym(Synonym, SynonymType)
     * @see    	   			#addSynonym(Synonym, SynonymType, Reference, String)
     * @see    	   			#addHomotypicSynonym(Synonym, Reference, String)
     * @see    	   			#addHomotypicSynonymName(TaxonName, Reference, String)
     * @see    	   			#addHeterotypicSynonymName(TaxonName)
     * @see    	   			#addHeterotypicSynonymName(TaxonName, HomotypicalGroup, Reference, String)
     * @see    	   			#getSynonyms()
     * @see    				#removeSynonym(Synonym)
     */
    public Synonym addSynonymName(TaxonName synonymName, SynonymType synonymType){
        return addSynonymName(synonymName, null, null, synonymType);
    }

    /**
     * Creates a new {@link Synonym synonym} to <code>this</code> {@link Taxon taxon}) using the
     * given {@link TaxonName synonym name} and with the given
     * {@link SynonymType synonym type}. If the later is
     * {@link SynonymType#HOMOTYPIC_SYNONYM_OF() homotypic synonym}
     * the name will be added to the same {@link HomotypicalGroup homotypical group}
     * as the <code>this</code> accepted taxon.<BR>
     *
     * If secReference is not <code>null</code>, the new synonym will have this as
     * secundum reference. Otherwise <code>this</code> taxons sec reference is taken
     * as secundum reference for the synonym. SecDetail will be the secMicroReference of the
     * new synonym.<BR>
     *
     * @param synonymName	the taxon name to be used as a synonym to be added
     * 						to <i>this</i> taxon's set of synonyms
     * @param secReference	the secundum reference for the new synonym (if <code>null</code>
     *                      <code>this</code> taxon's secundum reference is taken.
     * @param secMicroReference the secundum micro reference of the new synonym
     * @param synonymType	the synonym type of the synonym to be added
     *
     * @see    	   			#addSynonymName(TaxonName, SynonymType, Reference, String)
     * @see    	   			#addSynonym(Synonym, SynonymType)
     * @see    	   			#addSynonym(Synonym, SynonymType, Reference, String)
     * @see    	   			#addHomotypicSynonym(Synonym, Reference, String)
     * @see    	   			#addHomotypicSynonymName(TaxonName, Reference, String)
     * @see    	   			#addHeterotypicSynonymName(TaxonName)
     * @see    	   			#addHeterotypicSynonymName(TaxonName, HomotypicalGroup, Reference, String)
     * @see    	   			#getSynonyms()
     * @see    				#removeSynonym(Synonym)
     */
    public Synonym addSynonymName(TaxonName synonymName, Reference secReference, String secMicroReference, SynonymType synonymType){
        Synonym synonym = Synonym.NewInstance(synonymName, this.getSec()); //default sec
        synonym.setPublish(this.isPublish());
        addSynonym(synonym, synonymType, secReference, secMicroReference);
        return synonym;
    }

    /**
     * Creates a new {@link Synonym synonym} to <code>this</code> {@link Taxon taxon}) using the given
     * {@link TaxonName synonym name}. The synonym will have the synonym type
     * {@link SynonymType#HETEROTYPIC_SYNONYM_OF() "is heterotypic synonym of"}.<BR>
     * The secundum reference is taken from <code>this</code> taxon.
     * No secMicroReference will be set for the new synonym.<BR>
     * The synonym will keep it's old homotypical group.<BR>
     *
     * @param synonymName	the taxon name to be used as an heterotypic synonym
     * 						to be added to <i>this</i> taxon's set of synonyms
     * @return 				the created synonym
     * @see    	   			#addHeterotypicSynonymName(TaxonName, Reference, String, HomotypicalGroup)
     * @see    	   			#addSynonymName(TaxonName, SynonymType)
     * @see    	   			#addSynonymName(TaxonName, SynonymType, Reference, String)
     * @see    	   			#addSynonym(Synonym, SynonymType)
     * @see    	   			#addSynonym(Synonym, SynonymType, Reference, String)
     * @see    	   			#addHomotypicSynonym(Synonym, Reference, String)
     * @see    	   			#addHomotypicSynonymName(TaxonName, Reference, String)
     * @see    	   			#getSynonyms()
     * @see    				#removeSynonym(Synonym)
     */
    public Synonym addHeterotypicSynonymName(TaxonName synonymName){
        return addHeterotypicSynonymName(synonymName, null, null, null);
    }

    /**
     * Creates a new {@link Synonym synonym} to <code>this</code> {@link Taxon taxon}) using the given
     * {@link TaxonName synonym name}. The synonym will have the synonym type
     * {@link SynonymType#HETEROTYPIC_SYNONYM_OF() "is heterotypic synonym of"}.<BR>
     *
     * If secReference is not <code>null</code>, the new synonym will have this as
     * secundum reference. Otherwise <code>this</code> taxons sec reference is taken
     * as secundum reference for the synonym. SecDetail will be the secMicroReference of the
     * new synonym.<BR>
     * Furthermore the taxon name used as synonym will be added
     * to the given {@link name.HomotypicalGroup homotypical group} (if not <code>null</code>).<BR>
     *
     * @param synonymName		the taxon name to be used as an heterotypic synonym
     * 							to be added to <i>this</i> taxon's set of synonyms
     * @param synSecReference		the secundum reference for the new synonym
     * @param synSecDetail		    the secundum detail for the new synonym
     * @param homotypicalGroup	the homotypical group to which the taxon name
     * 							of the synonym will be added. If <code>null</code>
     *                          the homotypical group of synonymName is not changed
     * @return 					the created synonym
     * @see    	   				#addHeterotypicSynonymName(TaxonName)
     * @see    	   				#addSynonymName(TaxonName, SynonymType, Reference, String)
     * @see    	   				#addSynonymName(TaxonName, SynonymType)
     * @see    	   				#addSynonym(Synonym, SynonymType)
     * @see    	   				#addSynonym(Synonym, SynonymType, Reference, String)
     * @see    	   				#addHomotypicSynonym(Synonym, Reference, String)
     * @see    	   				#addHomotypicSynonymName(TaxonName, Reference, String)
     * @see    	   				#getSynonyms()
     * @see    					#removeSynonym(Synonym)
     */
    public Synonym addHeterotypicSynonymName(TaxonName synonymName, Reference synSecReference, String synSecDetail, HomotypicalGroup homotypicalGroup){
        Synonym synonym = Synonym.NewInstance(synonymName, this.getSec());
        if (homotypicalGroup != null){
            homotypicalGroup.addTypifiedName(synonymName);
        }
        synonym.setPublish(this.isPublish());

        addSynonym(synonym, SynonymType.HETEROTYPIC_SYNONYM_OF, synSecReference, synSecDetail);
        return synonym;
    }

    /**
    * Creates a new {@link Synonym synonym} to <code>this</code> {@link Taxon taxon}) using the given
     * {@link TaxonName synonym name}. The synonym will have the synonym type
     * {@link SynonymType#HOMOTYPIC_SYNONYM_OF() "is homotypic synonym of"}.<BR>
     * The secundum reference is taken from <code>this</code> taxon.
     * No secMicroReference will be set for the new synonym.<BR>
     * The synonym's homotypic group will be changed to <code>this</code> taxon's group.<BR>
     *
     * @param synonymName	the taxon name to be used as an homotypic synonym
     * 						to be added to <i>this</i> taxon's set of synonyms
     * @return 				the created synonym
     * @see    	   			#addHomotypicSynonym(Synonym, Reference, String)
     * @see    	   			#addSynonymName(TaxonName, SynonymType, Reference, String)
     * @see    	   			#addSynonymName(TaxonName, SynonymType)
     * @see    	   			#addSynonym(Synonym, SynonymType)
     * @see    	   			#addSynonym(Synonym, SynonymType, Reference, String)
     * @see    	   			#addHeterotypicSynonymName(TaxonName)
     * @see    	   			#addHeterotypicSynonymName(TaxonName, Reference, String, HomotypicalGroup)
     * @see    	   			#getSynonyms()
     * @see    				#removeSynonym(Synonym)
     */
    public Synonym addHomotypicSynonymName(TaxonName synonymName){
        Synonym synonym = Synonym.NewInstance(synonymName, this.getSec());
        synonym.setPublish(this.isPublish());
        addHomotypicSynonym(synonym);
        return synonym;
    }

    /**
     * Adds the given {@link Synonym synonym} to <code>this</code> taxon,
     * with the {@link SynonymType#HOMOTYPIC_SYNONYM_OF() "is homotypic synonym of"
     * relationship type} and returns it.
     * Furthermore the {@link TaxonName taxon name}
     * used as synonym will be added to the same {@link HomotypicalGroup homotypic group}
     * to which the taxon name of <i>this</i> taxon belongs.<BR>
     *
     * @param synonym		the synonym added to <i>this</i> taxon's synonym set
     * @see    	   			#addHomotypicSynonymName(TaxonName, Reference, String)
     * @see    	   			#addSynonym(Synonym, SynonymType)
     * @see    	   			#addSynonym(Synonym, SynonymType, Reference, String)
     * @see    	   			#addSynonymName(TaxonName, SynonymType, Reference, String)
     * @see    	   			#addSynonymName(TaxonName, SynonymType)
     * @see    	   			#addHeterotypicSynonymName(TaxonName)
     * @see    	   			#addHeterotypicSynonymName(TaxonName, Reference, String, HomotypicalGroup)
     * @see    	   			#getSynonyms()
     * @see    				#removeSynonym(Synonym)
     */
    public void addHomotypicSynonym(Synonym synonym){
    	if (!this.getSynonyms().contains(synonym)){
    		addSynonym(synonym, SynonymType.HOMOTYPIC_SYNONYM_OF);
    	} else{
    		logger.warn("Tried to add a synonym to an accepted taxon that already is a synonym of this taxon.");
    	}
        return;
    }

    /**
     * Like {@link #removeSynonym(Synonym, boolean)} with <code>removeSynonymNameFromHomotypicalGroup</code> set to true.
     * @see #removeSynonym(Synonym, boolean)
     */
    public void removeSynonym(Synonym synonym){
        removeSynonym(synonym, true);
    }


    /**
     * Removes one element from the set of {@link Synonym synonyms} assigned
     * to <i>this</i> (accepted/valid) taxon.
     *
     * @param synonym  the synonym to be removed
     * @param removeSynonymNameFromHomotypicalGroup
     *              if <code>true</code> the synonym name will also be deleted from its homotypical group if the
     *              group contains other names
     * @see     #getSynonyms()
     * @see     #removeSynonym(Synonym)
     */
    public void removeSynonym(Synonym synonym, boolean removeSynonymNameFromHomotypicalGroup) {
        if (synonym != null && this.equals(synonym.getAcceptedTaxon())){
            if(removeSynonymNameFromHomotypicalGroup){
                HomotypicalGroup synHG = synonym.getName().getHomotypicalGroup();
                if (synHG.getTypifiedNames().size() > 1){
                    synHG.removeTypifiedName(synonym.getName(), false);
                }
            }
            this.synonyms.remove(synonym);
            synonym.setAcceptedTaxon(null);
        }
    }

    /**
     * @see #getHomotypicSynonymsByHomotypicGroup(TaxonComparator)
     */
    @Transient
    public List<Synonym> getHomotypicSynonymsByHomotypicGroup(){
        return getHomotypicSynonymsByHomotypicGroup(null);
    }

    /**
     * Retrieves the ordered list (depending on the date of publication) of
     * homotypic {@link Synonym synonyms} (according to the same {@link eu.etaxonomy.cdm.model.reference.Reference reference}
     * as for <i>this</i> taxon) under the condition that the {@link eu.etaxonomy.cdm.model.name.TaxonName taxon names}
     * of these synonyms and the taxon name of <i>this</i> taxon belong to the
     * same {@link eu.etaxonomy.cdm.model.name.HomotypicalGroup homotypical group}.
     *
     * @param       comparator the taxon comparator to use, if <code>null</code> the default comparator is taken.
     * @return      the ordered list of homotypic synonyms
     * @see         #getHomotypicSynonymsByHomotypicSynonymType()
     * @see         #getSynonyms()
     * @see         #getHomotypicSynonymyGroups()
     * @see         eu.etaxonomy.cdm.model.name.HomotypicalGroup
     * @see         eu.etaxonomy.cdm.model.name.HomotypicalGroup#getSynonymsInGroup(Reference)
     */
    @Transient
    public List<Synonym> getHomotypicSynonymsByHomotypicGroup(TaxonComparator comparator){
        if (this.getHomotypicGroup() == null){
            return null;
        }else if (comparator == null){
            return this.getSynonymsInGroup(this.getHomotypicGroup());
        }else{
            return this.getSynonymsInGroup(this.getHomotypicGroup(), comparator);
        }
    }

    /**
     * Retrieves the list of homotypic {@link Synonym synonyms}
     * (according to the same {@link eu.etaxonomy.cdm.model.reference.Reference reference}
     * as for <i>this</i> taxon) under the condition that these synonyms and
     * <i>this</i> taxon are involved in {@link SynonymRelationship synonym relationships} with an
     * "is homotypic synonym of" {@link SynonymType#HOMOTYPIC_SYNONYM_OF() synonym relationship type}.
     *
     * @return		the ordered list of homotypic synonyms
     * @see			#getHomotypicSynonymsByHomotypicGroup()
     * @see			#getSynonyms()
     * @see			#getHomotypicSynonymyGroups()
     * @see			SynonymType
     * @deprecated as the method currently returns data not matching the original description of the method
     *    as an ordered list (according to date of publication) of synonyms with same secundum as <i>this</i> taxon.<BR>
     *    In future this method will either be removed or semantics may change.
     */
    @Deprecated
    @Transient
    public List<Synonym> getHomotypicSynonymsByHomotypicSynonymType(){
        Set<Synonym> synonyms = this.getSynonyms();
        List<Synonym> result = new ArrayList<>();
        for(Synonym synonym : synonyms) {
            if(synonym.getType().equals(SynonymType.HOMOTYPIC_SYNONYM_OF)){
                result.add(synonym);
            }
        }
        return result;
    }

    /**
     * Returns the ordered list of all {@link eu.etaxonomy.cdm.model.name.HomotypicalGroup homotypical groups} {@link Synonym synonyms} of
     * <i>this</i> taxon belong to. {@link eu.etaxonomy.cdm.model.name.TaxonName Taxon names} of homotypic synonyms
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
        List<HomotypicalGroup> result = new ArrayList<>();
        HomotypicalGroup myGroup = this.getHomotypicGroup();
        if (myGroup != null){  //if taxon has no name HG might be null
            result.add(myGroup);
        }
        for (TaxonName taxonName :this.getSynonymNames()){
            if (taxonName != null) {
                if (!result.contains(taxonName.getHomotypicalGroup())){
                    result.add(taxonName.getHomotypicalGroup());
                }
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}.
     *
     * <BR>Also returns <code>false</code> if it is a misapplied name or has a similar concept relationship that
     * is similar to synonym relationship (shows up in the synonymy of applications)
     */
    @Override
    @Transient
    public boolean isOrphaned() {

        if(taxonNodes == null || taxonNodes.isEmpty()) {
            if(getRelationsFromThisTaxon().isEmpty()) {
                return true;
            }else{
                for (TaxonRelationship rel : getRelationsFromThisTaxon()){
                    if (rel.getType() != null && ! rel.getType().isConceptRelationship()){
                        return false;  //a synonym relationship type similar relationship exists => not orphaned
                    }
                }
                return true;  //all relations are real concept relations and therefore not relevant
            }
        }else{
            return false;
        }
    }

    /**
     * Returns the ordered list of all
     * {@link eu.etaxonomy.cdm.model.name.HomotypicalGroup homotypical groups}
     * that contain {@link Synonym synonyms} that are heterotypic to <i>this</i> taxon.<BR>
     *
     * {@link eu.etaxonomy.cdm.model.name.TaxonName Taxon names} of heterotypic synonyms
     * belong to a homotypical group which cannot be the homotypical group to which the
     * taxon name of <i>this</i> taxon belongs.
     * This method returns the same
     * list as the {@link #getHomotypicSynonymyGroups() getHomotypicSynonymyGroups} method
     * but without the homotypical group to which the taxon name of <i>this</i> taxon
     * belongs.<BR>
     * The list returned is <B>ordered</B> according to the rules defined for
     * the {@link HomotypicGroupTaxonComparator} which includes 1) grouping of
     * basionym groups, 2) replaced synonym relationships, 3) publication date,
     * 4) ranks and 5) alphabetical order.
     *
     * @see			#getHeterotypicSynonymyGroups()
     * @see			#getSynonyms()
     * @see			SynonymType#HETEROTYPIC_SYNONYM_OF()
     * @see			eu.etaxonomy.cdm.model.name.HomotypicalGroup
     */
    @Transient
    public List<HomotypicalGroup> getHeterotypicSynonymyGroups(){
        List<HomotypicalGroup> list = getHomotypicSynonymyGroups();
        //remove homotypic group
        list.remove(this.getHomotypicGroup());
        //sort
        Map<Synonym, HomotypicalGroup> map = new HashMap<>();
        for (HomotypicalGroup homotypicalGroup: list){
            List<Synonym> synonymList = getSynonymsInGroup(homotypicalGroup);
            if (synonymList.size() > 0){
                //select the first synonym in the group
                map.put(synonymList.get(0), homotypicalGroup);
            }
        }
        List<Synonym> keyList = new ArrayList<>();
        keyList.addAll(map.keySet());
        //order by first synonym
        Collections.sort(keyList, defaultTaxonComparator);

        List<HomotypicalGroup> result = new ArrayList<>();
        for(Synonym synonym: keyList){
            //"replace" synonyms by homotypic groups
            result.add(map.get(synonym));
        }
        //sort end
        return result;
    }

    /**
     * Retrieves the ordered list (depending on the rules defined for
     * the {@link HomotypicGroupTaxonComparator}) of
     * {@link taxon.Synonym synonyms} (according to a given reference)
     * the {@link TaxonName taxon names} of which belong to the homotypical group.
     * If other names are part of the group that are not considered synonyms of
     * <i>this</i> taxon, then they will not be included in
     * the result set.
     *
     * @param homotypicGroup
     * @see          #getHeterotypicSynonymyGroups()
     * @see			TaxonName#getSynonyms()
     * @see			TaxonName#getTaxa()
     * @see			taxon.Synonym
     */
    @Transient
    public List<Synonym> getSynonymsInGroup(HomotypicalGroup homotypicGroup){
        return getSynonymsInGroup(homotypicGroup, new HomotypicGroupTaxonComparator(this));
    }

    /**
     * @param homotypicGroup
     * @param comparator
     * @return
     * @see     #getSynonymsInGroup(HomotypicalGroup)
     * @see     #getHeterotypicSynonymyGroups()
     */
    @Transient
    public List<Synonym> getSynonymsInGroup(HomotypicalGroup homotypicGroup, TaxonComparator comparator){
        List<Synonym> result = new ArrayList<>();
        if (homotypicGroup == null){
            return result;  //always empty
        }

        for (Synonym synonym : this.getSynonyms()){
            if (homotypicGroup.equals(synonym.getHomotypicGroup())){
                result.add(synonym);
            }
        }

        Collections.sort(result, comparator);
        return result;
    }

    /**
     * @see     #getSynonymsGroups()
     */
    @Transient
    public List<Taxon> getAllMisappliedNames(){
        List<Taxon> result = new ArrayList<>();

        for (TaxonRelationship rel : this.getRelationsToThisTaxon()){
            if (rel.getType().isAnyMisappliedName() ){
                result.add(rel.getFromTaxon());
            }
        }
        sortBySimpleTitleCacheComparator(result);
        return result;
    }


    /**
     * @see     #getSynonymsGroups()
     */
    @Transient
    public List<Taxon> getAllProParteSynonyms(){
        List<Taxon> result = new ArrayList<>();

        for (TaxonRelationship rel : this.getRelationsToThisTaxon()){
            if (rel.getType().isAnySynonym()){
                result.add(rel.getFromTaxon());
            }
        }
        sortBySimpleTitleCacheComparator(result);
        return result;
    }

//    /**
//     * @see     #getSynonymsGroups()
//     */
//    @Transient
//    public List<Taxon> getProParteSynonyms(){
//        List<Taxon> result = new ArrayList<>();
//
//        for (TaxonRelationship rel : this.getRelationsToThisTaxon()){
//            if (rel.getType().isProParte()){
//                result.add(rel.getFromTaxon());
//            }
//        }
//        sortBySimpleTitleCacheComparator(result);
//        return result;
//    }
//
//    /**
//     * @see     #getSynonymsGroups()
//     */
//    @Transient
//    public List<Taxon> getPartialSynonyms(){
//        List<Taxon> result = new ArrayList<>();
//
//        for (TaxonRelationship rel : this.getRelationsToThisTaxon()){
//            if (rel.getType().isPartial()){
//                result.add(rel.getFromTaxon());
//            }
//        }
//        sortBySimpleTitleCacheComparator(result);
//        return result;
//    }
    private void sortBySimpleTitleCacheComparator(List<Taxon> result) {

        Comparator<Taxon> taxonComparator = new Comparator<Taxon>(){

            @Override
            public int compare(Taxon o1, Taxon o2) {

                if (o1.getTitleCache() == o2.getTitleCache()){
                    return 0;
                }
                if (o1.getTitleCache() == null){
                    return -1;
                }
                if (o2.getTitleCache() == null){
                    return 1;
                }
                return o1.getTitleCache().compareTo(o2.getTitleCache());

            }
        };
        Collections.sort(result, taxonComparator);
    }

    /**
     * Returns the image gallery description. If no image gallery exists, a new one is created using the
     * defined title and adds the string "-Image Gallery" to the title.</BR>
     * If multiple image galleries exist an arbitrary one is choosen.
     * @param title
     * @return
     */
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

    public void clearDescriptions() {
        this.descriptions = new HashSet<>();
    }

    /**
     * Compiles all description items attached to this taxon having the given feature
     * and being of the given class. If feature or clazz is null no according filter
     * is applied.
     */
    public <T extends DescriptionElementBase> Set<T> getDescriptionItems(Feature feature, Class<T> clazz) {
        Set<T> result = new HashSet<>();
        Set<TaxonDescription> descriptions = this.getDescriptions();
        for (TaxonDescription description : descriptions) {
            for (DescriptionElementBase deb : description.getElements()) {
                if (clazz == null || deb.isInstanceOf(clazz)) {
                    if (feature == null || feature.equals(deb.getFeature())) {
                        T matchingDeb = CdmBase.deproxy(deb, clazz);
                        result.add(matchingDeb);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Taxon acceptedTaxon() {
        return this;
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
    public Taxon clone() {
        return clone(true, true, true, true);
    }

    public Taxon clone(boolean withSynonyms, boolean withTaxonRelations, boolean withDescriptions,
            boolean withMedia) {

        Taxon result;
        result = (Taxon)super.clone();

        result.setRelationsFromThisTaxon(new HashSet<>());
        result.setRelationsToThisTaxon(new HashSet<>());

        if (withTaxonRelations || withSynonyms){
            for (TaxonRelationship fromRelationship : this.getRelationsFromThisTaxon()){
                boolean isSynonymRelation = fromRelationship.getType() != null &&
                        fromRelationship.getType().isAnySynonymOrMisappliedName();
                if (isSynonymRelation && withSynonyms || !isSynonymRelation && withTaxonRelations){
                    TaxonRelationship newRelationship = fromRelationship.clone();
                    newRelationship.setRelatedFrom(result);
                    result.relationsFromThisTaxon.add(newRelationship);
                }
            }

            for (TaxonRelationship toRelationship : this.getRelationsToThisTaxon()){
                boolean isSynonymRelation = toRelationship.getType() != null &&
                        toRelationship.getType().isAnySynonymOrMisappliedName();
                if (isSynonymRelation && withSynonyms || !isSynonymRelation && withTaxonRelations){
                    TaxonRelationship newRelationship = toRelationship.clone();
                    newRelationship.setRelatedTo(result);
                    result.relationsToThisTaxon.add(newRelationship);
                }
            }
        }

        //clone synonyms (is this wanted or should we remove synonyms
        result.synonyms = new HashSet<>();
        if(withSynonyms){
            for (Synonym synonym : this.getSynonyms()){
                Synonym newSyn = synonym.clone();
                newSyn.setAcceptedTaxon(result);
            }
        }

        result.descriptions = new HashSet<>();
        for (TaxonDescription description : this.getDescriptions()){
            if (description.isImageGallery() && withMedia ||
                    !description.isImageGallery() && withDescriptions){
                TaxonDescription newDescription = description.clone();
                result.addDescription(newDescription);
            }
        }

        result.taxonNodes = new HashSet<>();

        /*for (TaxonNode taxonNode : this.getTaxonNodes()){
            TaxonNode newTaxonNode = (TaxonNode)taxonNode.clone();
            newTaxonNode.setTaxon(result);
            result.addTaxonNode(newTaxonNode);
        }*/


        //concept related attributes
        result.taxonTypes = this.taxonTypes.clone();
        result.conceptDefinitions = this.conceptDefinitions.clone();
        result.conceptStatus = this.conceptStatus.clone();

        if (this.currentConceptPeriod != null){
            result.currentConceptPeriod = this.currentConceptPeriod.clone();
        }

        return result;
    }

}
