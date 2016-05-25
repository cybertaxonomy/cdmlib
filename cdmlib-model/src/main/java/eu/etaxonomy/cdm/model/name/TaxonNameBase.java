/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.ReflectionUtils;

import eu.etaxonomy.cdm.model.common.IParsable;
import eu.etaxonomy.cdm.model.common.IRelated;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.name.CacheUpdate;
import eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.Match;
import eu.etaxonomy.cdm.strategy.match.Match.ReplaceMode;
import eu.etaxonomy.cdm.strategy.match.MatchMode;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;
import eu.etaxonomy.cdm.strategy.parser.ParserProblem;
import eu.etaxonomy.cdm.validation.Level2;
import eu.etaxonomy.cdm.validation.Level3;
import eu.etaxonomy.cdm.validation.annotation.ValidTaxonomicYear;

/**
 * The upmost (abstract) class for scientific taxon names regardless of any
 * particular {@link NomenclaturalCode nomenclature code}. The scientific taxon name does not depend
 * on the use made of it in a publication or a treatment
 * ({@link eu.etaxonomy.cdm.model.taxon.TaxonBase taxon concept respectively potential taxon})
 * as an {@link eu.etaxonomy.cdm.model.taxon.Taxon "accepted" respectively "correct" (taxon) name}
 * or as a {@link eu.etaxonomy.cdm.model.taxon.Synonym synonym}.
 * <P>
 * This class corresponds partially to: <ul>
 * <li> TaxonName according to the TDWG ontology
 * <li> ScientificName and CanonicalName according to the TCS
 * <li> ScientificName according to the ABCD schema
 * </ul>
 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:57
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonNameBase", propOrder = {
    "appendedPhrase",
    "nomenclaturalMicroReference",
    "nomenclaturalReference",
    "rank",
    "fullTitleCache",
    "protectedFullTitleCache",
    "homotypicalGroup",
    "typeDesignations",
    "relationsFromThisName",
    "relationsToThisName",
    "status",
    "descriptions",
    "taxonBases"
})
@XmlRootElement(name = "TaxonNameBase")
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(appliesTo="TaxonNameBase", indexes = { @Index(name = "taxonNameBaseTitleCacheIndex", columnNames = { "titleCache" }),  @Index(name = "taxonNameBaseNameCacheIndex", columnNames = { "nameCache" }) })
public abstract class TaxonNameBase<T extends TaxonNameBase<?,?>, S extends INameCacheStrategy> extends IdentifiableEntity<S> implements IParsable, IRelated, IMatchable, Cloneable {
    private static final long serialVersionUID = -4530368639601532116L;
    private static final Logger logger = Logger.getLogger(TaxonNameBase.class);

    @XmlElement(name = "FullTitleCache")
    @Column(length=800, name="fullTitleCache")  //see #1592
    @Match(value=MatchMode.CACHE, cacheReplaceMode=ReplaceMode.ALL)
    @CacheUpdate(noUpdate ="titleCache")
    @NotEmpty(groups = Level2.class)
    protected String fullTitleCache;

    //if true titleCache will not be automatically generated/updated
    @XmlElement(name = "ProtectedFullTitleCache")
    @CacheUpdate(value ="fullTitleCache", noUpdate ="titleCache")
    private boolean protectedFullTitleCache;

    @XmlElementWrapper(name = "Descriptions")
    @XmlElement(name = "Description")
    @OneToMany(mappedBy="taxonName", fetch= FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @NotNull
    private Set<TaxonNameDescription> descriptions = new HashSet<TaxonNameDescription>();

    @XmlElement(name = "AppendedPhrase")
    @Field
    @CacheUpdate(value ="nameCache")
    //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
    private String appendedPhrase;

    @XmlElement(name = "NomenclaturalMicroReference")
    @Field
    @CacheUpdate(noUpdate ="titleCache")
    //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
    private String nomenclaturalMicroReference;

    @XmlAttribute
    @CacheUpdate(noUpdate ={"titleCache","fullTitleCache"})
    private int parsingProblem = 0;

    @XmlAttribute
    @CacheUpdate(noUpdate ={"titleCache","fullTitleCache"})
    private int problemStarts = -1;

    @XmlAttribute
    @CacheUpdate(noUpdate ={"titleCache","fullTitleCache"})
    private int problemEnds = -1;

    @XmlElementWrapper(name = "TypeDesignations")
    @XmlElement(name = "TypeDesignation")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name="TaxonNameBase_TypeDesignationBase",
        joinColumns=@javax.persistence.JoinColumn(name="TaxonNameBase_id"),
        inverseJoinColumns=@javax.persistence.JoinColumn(name="typedesignations_id")
    )
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @NotNull
    private Set<TypeDesignationBase> typeDesignations = new HashSet<TypeDesignationBase>();

    @XmlElement(name = "HomotypicalGroup")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    @Match(MatchMode.IGNORE)
    @CacheUpdate(noUpdate ="titleCache")
    //TODO Val #3379
//    @NotNull
    private HomotypicalGroup homotypicalGroup;

    @XmlElementWrapper(name = "RelationsFromThisName")
    @XmlElement(name = "RelationFromThisName")
    @OneToMany(mappedBy="relatedFrom", fetch= FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @Merge(MergeMode.RELATION)
    @NotNull
    @Valid
    private Set<NameRelationship> relationsFromThisName = new HashSet<NameRelationship>();

    @XmlElementWrapper(name = "RelationsToThisName")
    @XmlElement(name = "RelationToThisName")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="relatedTo", fetch= FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @Merge(MergeMode.RELATION)
    @NotNull
    @Valid
    private Set<NameRelationship> relationsToThisName = new HashSet<NameRelationship>();

    @XmlElementWrapper(name = "NomenclaturalStatuses")
    @XmlElement(name = "NomenclaturalStatus")
    @OneToMany(fetch= FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE,CascadeType.DELETE})
    @NotNull
    @IndexedEmbedded(depth=1)
    private Set<NomenclaturalStatus> status = new HashSet<NomenclaturalStatus>();

    @XmlElementWrapper(name = "TaxonBases")
    @XmlElement(name = "TaxonBase")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="name", fetch= FetchType.LAZY)
    @NotNull
    @IndexedEmbedded(depth=1)
    private Set<TaxonBase> taxonBases = new HashSet<TaxonBase>();

    @XmlElement(name = "Rank")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.EAGER)
    @CacheUpdate(value ="nameCache")
    //TODO Val #3379, handle maybe as groups = Level2.class ??
//    @NotNull
    @IndexedEmbedded(depth=1)
    private Rank rank;

    @XmlElement(name = "NomenclaturalReference")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @CacheUpdate(noUpdate ="titleCache")
    @IndexedEmbedded
    private Reference nomenclaturalReference;

// ************* CONSTRUCTORS *************/
    /**
     * Class constructor: creates a new empty taxon name.
     *
     * @see #TaxonNameBase(Rank)
     * @see #TaxonNameBase(HomotypicalGroup)
     * @see #TaxonNameBase(Rank, HomotypicalGroup)
     */
    public TaxonNameBase() {
        super();
    }
    /**
     * Class constructor: creates a new taxon name
     * only containing its {@link Rank rank}.
     *
     * @param  rank  the rank to be assigned to <i>this</i> taxon name
     * @see    		 #TaxonNameBase()
     * @see    		 #TaxonNameBase(HomotypicalGroup)
     * @see    		 #TaxonNameBase(Rank, HomotypicalGroup)
     */
    public TaxonNameBase(Rank rank) {
        this(rank, null);
    }
    /**
     * Class constructor: creates a new taxon name
     * only containing its {@link HomotypicalGroup homotypical group}.
     * The new taxon name will be also added to the set of taxon names
     * belonging to this homotypical group.
     *
     * @param  homotypicalGroup  the homotypical group to which <i>this</i> taxon name belongs
     * @see    					 #TaxonNameBase()
     * @see    					 #TaxonNameBase(Rank)
     * @see    					 #TaxonNameBase(Rank, HomotypicalGroup)
     */
    public TaxonNameBase(HomotypicalGroup homotypicalGroup) {
        this(null, homotypicalGroup);
    }
    /**
     * Class constructor: creates a new taxon name
     * only containing its {@link Rank rank} and
     * its {@link HomotypicalGroup homotypical group}.
     * The new taxon name will be also added to the set of taxon names
     * belonging to this homotypical group.
     *
     * @param  rank  			 the rank to be assigned to <i>this</i> taxon name
     * @param  homotypicalGroup  the homotypical group to which <i>this</i> taxon name belongs
     * @see    					 #TaxonNameBase()
     * @see    					 #TaxonNameBase(Rank)
     * @see    					 #TaxonNameBase(HomotypicalGroup)
     */
    public TaxonNameBase(Rank rank, HomotypicalGroup homotypicalGroup) {
        super();
        this.setRank(rank);
        if (homotypicalGroup == null){
            homotypicalGroup = new HomotypicalGroup();
        }
        homotypicalGroup.addTypifiedName(this);
        this.homotypicalGroup = homotypicalGroup;
    }

    abstract protected Map<String, java.lang.reflect.Field> getAllFields();

//********* METHODS **************************************/

    /**
     * Returns the boolean value "false" since the components of <i>this</i> taxon name
     * cannot follow the rules of a corresponding {@link NomenclaturalCode nomenclatural code}
     * which is not defined for this class. The nomenclature code depends on
     * the concrete name subclass ({@link BacterialName BacterialName},
     * {@link BotanicalName BotanicalName}, {@link CultivarPlantName CultivarPlantName},
     * {@link ZoologicalName ZoologicalName} or {@link ViralName ViralName})
     * to which a taxon name belongs.
     *
     * @return  false
     */
    @Transient
    public abstract boolean isCodeCompliant();

    public abstract String generateFullTitle();



    @Transient
    public List<TaggedText> getTaggedName(){
        return getCacheStrategy().getTaggedTitle(this);
    }

    @Transient
    public String getFullTitleCache(){
        if (protectedFullTitleCache){
            return this.fullTitleCache;
        }
        if (fullTitleCache == null ){
            this.fullTitleCache = getTruncatedCache(generateFullTitle());
        }
        return fullTitleCache;
    }


    public void setFullTitleCache(String fullTitleCache){
        setFullTitleCache(fullTitleCache, PROTECTED);
    }

    public void setFullTitleCache(String fullTitleCache, boolean protectCache){
        fullTitleCache = getTruncatedCache(fullTitleCache);
        this.fullTitleCache = fullTitleCache;
        this.setProtectedFullTitleCache(protectCache);
    }


    public boolean isProtectedFullTitleCache() {
        return protectedFullTitleCache;
    }

    public void setProtectedFullTitleCache(boolean protectedFullTitleCache) {
        this.protectedFullTitleCache = protectedFullTitleCache;
    }

    /**
     * Returns the set of all {@link NameRelationship name relationships}
     * in which <i>this</i> taxon name is involved. A taxon name can be both source
     * in some name relationships or target in some others.
     *
     * @see    #getRelationsToThisName()
     * @see    #getRelationsFromThisName()
     * @see    #addNameRelationship(NameRelationship)
     * @see    #addRelationshipToName(TaxonNameBase, NameRelationshipType, String)
     * @see    #addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
     */
    @Transient
    public Set<NameRelationship> getNameRelations() {
        Set<NameRelationship> rels = new HashSet<NameRelationship>();
        rels.addAll(getRelationsFromThisName());
        rels.addAll(getRelationsToThisName());
        return rels;
    }

    /**
     * Creates a new {@link NameRelationship#NameRelationship(TaxonNameBase, TaxonNameBase, NameRelationshipType, String) name relationship} from <i>this</i> taxon name to another taxon name
     * and adds it both to the set of {@link #getRelationsFromThisName() relations from <i>this</i> taxon name} and
     * to the set of {@link #getRelationsToThisName() relations to the other taxon name}.
     *
     * @param toName		  the taxon name of the target for this new name relationship
     * @param type			  the type of this new name relationship
     * @param ruleConsidered  the string which specifies the rule on which this name relationship is based
     * @see    				  #getRelationsToThisName()
     * @see    				  #getNameRelations()
     * @see    				  #addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
     * @see    				  #addNameRelationship(NameRelationship)
     */
    public void addRelationshipToName(TaxonNameBase toName, NameRelationshipType type, String ruleConsidered){
        addRelationshipToName(toName, type, null, null, ruleConsidered);
        //		NameRelationship rel = new NameRelationship(toName, this, type, ruleConsidered);
    }

    /**
     * Creates a new {@link NameRelationship#NameRelationship(TaxonNameBase, TaxonNameBase, NameRelationshipType, String) name relationship} from <i>this</i> taxon name to another taxon name
     * and adds it both to the set of {@link #getRelationsFromThisName() relations from <i>this</i> taxon name} and
     * to the set of {@link #getRelationsToThisName() relations to the other taxon name}.
     *
     * @param toName		  the taxon name of the target for this new name relationship
     * @param type			  the type of this new name relationship
     * @param ruleConsidered  the string which specifies the rule on which this name relationship is based
     * @return
     * @see    				  #getRelationsToThisName()
     * @see    				  #getNameRelations()
     * @see    				  #addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
     * @see    				  #addNameRelationship(NameRelationship)
     */
    public NameRelationship addRelationshipToName(TaxonNameBase toName, NameRelationshipType type, Reference citation, String microCitation, String ruleConsidered){
        if (toName == null){
            throw new NullPointerException("Null is not allowed as name for a name relationship");
        }
        NameRelationship rel = new NameRelationship(toName, this, type, citation, microCitation, ruleConsidered);
        return rel;
    }

    /**
     * Creates a new {@link NameRelationship#NameRelationship(TaxonNameBase, TaxonNameBase, NameRelationshipType, String) name relationship} from another taxon name to <i>this</i> taxon name
     * and adds it both to the set of {@link #getRelationsToThisName() relations to <i>this</i> taxon name} and
     * to the set of {@link #getRelationsFromThisName() relations from the other taxon name}.
     *
     * @param fromName		  the taxon name of the source for this new name relationship
     * @param type			  the type of this new name relationship
     * @param ruleConsidered  the string which specifies the rule on which this name relationship is based
     * @param citation		  the reference in which this relation was described
     * @param microCitation	  the reference detail for this relation (e.g. page)
     * @see    				  #getRelationsFromThisName()
     * @see    				  #getNameRelations()
     * @see    				  #addRelationshipToName(TaxonNameBase, NameRelationshipType, String)
     * @see    				  #addNameRelationship(NameRelationship)
     */
    public NameRelationship addRelationshipFromName(TaxonNameBase fromName, NameRelationshipType type, String ruleConsidered){
        //fromName.addRelationshipToName(this, type, null, null, ruleConsidered);
        return this.addRelationshipFromName(fromName, type, null, null, ruleConsidered);
    }
    /**
     * Creates a new {@link NameRelationship#NameRelationship(TaxonNameBase, TaxonNameBase, NameRelationshipType, String) name relationship} from another taxon name to <i>this</i> taxon name
     * and adds it both to the set of {@link #getRelationsToThisName() relations to <i>this</i> taxon name} and
     * to the set of {@link #getRelationsFromThisName() relations from the other taxon name}.
     *
     * @param fromName		  the taxon name of the source for this new name relationship
     * @param type			  the type of this new name relationship
     * @param ruleConsidered  the string which specifies the rule on which this name relationship is based
     * @param citation		  the reference in which this relation was described
     * @param microCitation	  the reference detail for this relation (e.g. page)
     * @see    				  #getRelationsFromThisName()
     * @see    				  #getNameRelations()
     * @see    				  #addRelationshipToName(TaxonNameBase, NameRelationshipType, String)
     * @see    				  #addNameRelationship(NameRelationship)
     */
    public NameRelationship addRelationshipFromName(TaxonNameBase fromName, NameRelationshipType type, Reference citation, String microCitation, String ruleConsidered){
        return fromName.addRelationshipToName(this, type, citation, microCitation, ruleConsidered);
    }

    /**
     * Adds an existing {@link NameRelationship name relationship} either to the set of
     * {@link #getRelationsToThisName() relations to <i>this</i> taxon name} or to the set of
     * {@link #getRelationsFromThisName() relations from <i>this</i> taxon name}. If neither the
     * source nor the target of the name relationship match with <i>this</i> taxon name
     * no addition will be carried out.
     *
     * @param rel  the name relationship to be added to one of <i>this</i> taxon name's name relationships sets
     * @see    	   #getNameRelations()
     * @see    	   #addRelationshipToName(TaxonNameBase, NameRelationshipType, String)
     * @see    	   #addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
     */
    protected void addNameRelationship(NameRelationship rel) {
        if (rel!=null && rel.getToName().equals(this)){
            this.relationsToThisName.add(rel);
        }else if(rel!=null && rel.getFromName().equals(this)){
            this.relationsFromThisName.add(rel);
        }else{
            throw new RuntimeException("NameRelationship is either null or the relationship does not reference this name");
        }
    }
    /**
     * Removes one {@link NameRelationship name relationship} from one of both sets of
     * {@link #getNameRelations() name relationships} in which <i>this</i> taxon name is involved.
     * The name relationship will also be removed from one of both sets belonging
     * to the second taxon name involved. Furthermore the fromName and toName
     * attributes of the name relationship object will be nullified.
     *
     * @param  nameRelation  the name relationship which should be deleted from one of both sets
     * @see    				 #getNameRelations()
     */
    public void removeNameRelationship(NameRelationship nameRelation) {

        TaxonNameBase fromName = nameRelation.getFromName();
        TaxonNameBase toName = nameRelation.getToName();

        if (nameRelation != null) {
            nameRelation.setToName(null);
            nameRelation.setFromName(null);
        }

        if (fromName != null) {
            fromName.removeNameRelationship(nameRelation);
        }

        if (toName != null) {
            toName.removeNameRelationship(nameRelation);
        }

        this.relationsToThisName.remove(nameRelation);
        this.relationsFromThisName.remove(nameRelation);
    }

    public void removeRelationToTaxonName(TaxonNameBase toTaxonName) {
        Set<NameRelationship> nameRelationships = new HashSet<NameRelationship>();
//		nameRelationships.addAll(this.getNameRelations());
        nameRelationships.addAll(this.getRelationsFromThisName());
        nameRelationships.addAll(this.getRelationsToThisName());
        for(NameRelationship nameRelationship : nameRelationships) {
            // remove name relationship from this side
            if (nameRelationship.getFromName().equals(this) && nameRelationship.getToName().equals(toTaxonName)) {
                this.removeNameRelationship(nameRelationship);
            }
        }
    }


    /**
     * Does exactly the same as the addNameRelationship method provided that
     * the given relationship is a name relationship.
     *
     * @param relation  the relationship to be added to one of <i>this</i> taxon name's name relationships sets
     * @see    	   		#addNameRelationship(NameRelationship)
     * @see    	   		#getNameRelations()
     * @see    	   		NameRelationship
     * @see    	   		eu.etaxonomy.cdm.model.common.RelationshipBase
     */
    @Override
    public void addRelationship(RelationshipBase relation) {
        if (relation instanceof NameRelationship){
            addNameRelationship((NameRelationship)relation);
            NameRelationshipType type = (NameRelationshipType)relation.getType();
            if (type != null && ( type.isBasionymRelation() || type.isReplacedSynonymRelation() ) ){
                TaxonNameBase fromName = ((NameRelationship)relation).getFromName();
                TaxonNameBase toName = ((NameRelationship)relation).getToName();
                fromName.mergeHomotypicGroups(toName);
            }
        }else{
            logger.warn("Relationship not of type NameRelationship!");
            throw new IllegalArgumentException("Relationship not of type NameRelationship");
        }
    }


    /**
     * Returns the set of all {@link NameRelationship name relationships}
     * in which <i>this</i> taxon name is involved as a source ("from"-side).
     *
     * @see    #getNameRelations()
     * @see    #getRelationsToThisName()
     * @see    #addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
     */
    public Set<NameRelationship> getRelationsFromThisName() {
        if(relationsFromThisName == null) {
            this.relationsFromThisName = new HashSet<NameRelationship>();
        }
        return relationsFromThisName;
    }

    /**
     * Returns the set of all {@link NameRelationship name relationships}
     * in which <i>this</i> taxon name is involved as a target ("to"-side).
     *
     * @see    #getNameRelations()
     * @see    #getRelationsFromThisName()
     * @see    #addRelationshipToName(TaxonNameBase, NameRelationshipType, String)
     */
    public Set<NameRelationship> getRelationsToThisName() {
        if(relationsToThisName == null) {
            this.relationsToThisName = new HashSet<NameRelationship>();
        }
        return relationsToThisName;
    }

    /**
     * Returns the set of {@link NomenclaturalStatus nomenclatural status} assigned
     * to <i>this</i> taxon name according to its corresponding nomenclature code.
     * This includes the {@link NomenclaturalStatusType type} of the nomenclatural status
     * and the nomenclatural code rule considered.
     *
     * @see     NomenclaturalStatus
     * @see     NomenclaturalStatusType
     */
    public Set<NomenclaturalStatus> getStatus() {
        if(status == null) {
            this.status = new HashSet<NomenclaturalStatus>();
        }
        return status;
    }

    /**
     * Adds a new {@link NomenclaturalStatus nomenclatural status}
     * to <i>this</i> taxon name's set of nomenclatural status.
     *
     * @param  nomStatus  the nomenclatural status to be added
     * @see 			  #getStatus()
     */
    public void addStatus(NomenclaturalStatus nomStatus) {
        this.status.add(nomStatus);
    }
    public NomenclaturalStatus addStatus(NomenclaturalStatusType statusType, Reference citation, String microCitation) {
        NomenclaturalStatus newStatus = NomenclaturalStatus.NewInstance(statusType, citation, microCitation);
        this.status.add(newStatus);
        return newStatus;
    }

    /**
     * Removes one element from the set of nomenclatural status of <i>this</i> taxon name.
     * Type and ruleConsidered attributes of the nomenclatural status object
     * will be nullified.
     *
     * @param  nomStatus  the nomenclatural status of <i>this</i> taxon name which should be deleted
     * @see     		  #getStatus()
     */
    public void removeStatus(NomenclaturalStatus nomStatus) {
        //TODO to be implemented?
        logger.warn("not yet fully implemented?");
        this.status.remove(nomStatus);
    }


    /**
     * Indicates whether <i>this</i> taxon name is a {@link NameRelationshipType#BASIONYM() basionym}
     * or a {@link NameRelationshipType#REPLACED_SYNONYM() replaced synonym}
     * of any other taxon name. Returns "true", if a basionym or a replaced
     * synonym {@link NameRelationship relationship} from <i>this</i> taxon name to another taxon name exists,
     * false otherwise (also in case <i>this</i> taxon name is the only one in the
     * homotypical group).
     */
    @Transient
    public boolean isOriginalCombination(){
        Set<NameRelationship> relationsFromThisName = this.getRelationsFromThisName();
        for (NameRelationship relation : relationsFromThisName) {
            if (relation.getType().isBasionymRelation() ||
                    relation.getType().isReplacedSynonymRelation()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indicates <i>this</i> taxon name is a {@link NameRelationshipType#REPLACED_SYNONYM() replaced synonym}
     * of any other taxon name. Returns "true", if a replaced
     * synonym {@link NameRelationship relationship} from <i>this</i> taxon name to another taxon name exists,
     * false otherwise (also in case <i>this</i> taxon name is the only one in the
     * homotypical group).
     */
    @Transient
    public boolean isReplacedSynonym(){
        Set<NameRelationship> relationsFromThisName = this.getRelationsFromThisName();
        for (NameRelationship relation : relationsFromThisName) {
            if (relation.getType().isReplacedSynonymRelation()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the taxon name which is the {@link NameRelationshipType#BASIONYM() basionym} of <i>this</i> taxon name.
     * The basionym of a taxon name is its epithet-bringing synonym.
     * For instance <i>Pinus abies</i> L. was published by Linnaeus and the botanist
     * Karsten transferred later <i>this</i> taxon to the genus Picea. Therefore,
     * <i>Pinus abies</i> L. is the basionym of the new combination <i>Picea abies</i> (L.) H. Karst.
     *
     * If more than one basionym exists one is choosen at radom.
     *
     * If no basionym exists null is returned.
     */
    @Transient
    public TaxonNameBase getBasionym(){
        Set<TaxonNameBase> basionyms = getBasionyms();
        if (basionyms.size() == 0){
            return null;
        }else{
            return basionyms.iterator().next();
        }
    }

    /**
     * Returns the set of taxon names which are the {@link NameRelationshipType#BASIONYM() basionyms} of <i>this</i> taxon name.
     * The basionym of a taxon name is its epithet-bringing synonym.
     * For instance <i>Pinus abies</i> L. was published by Linnaeus and the botanist
     * Karsten transferred later <i>this</i> taxon to the genus Picea. Therefore,
     * <i>Pinus abies</i> L. is the basionym of the new combination <i>Picea abies</i> (L.) H. Karst.
     */
    @Transient
    public Set<TaxonNameBase> getBasionyms(){
        Set<TaxonNameBase> result = new HashSet<TaxonNameBase>();
        Set<NameRelationship> rels = this.getRelationsToThisName();
        for (NameRelationship rel : rels){
            if (rel.getType()!= null && rel.getType().isBasionymRelation()){
                TaxonNameBase<?,?> basionym = rel.getFromName();
                result.add(basionym);
            }
        }
        return result;
    }

    /**
     * Assigns a taxon name as {@link NameRelationshipType#BASIONYM() basionym} of <i>this</i> taxon name.
     * The basionym {@link NameRelationship relationship} will be added to <i>this</i> taxon name
     * and to the basionym. The basionym cannot have itself a basionym.
     * The {@link HomotypicalGroup homotypical groups} of <i>this</i> taxon name and of the basionym
     * will be {@link HomotypicalGroup#merge(HomotypicalGroup) merged}.
     *
     * @param  basionym		the taxon name to be set as the basionym of <i>this</i> taxon name
     * @see  				#getBasionym()
     * @see  				#addBasionym(TaxonNameBase, String)
     */
    public void addBasionym(T basionym){
        addBasionym(basionym, null, null, null);
    }
    /**
     * Assigns a taxon name as {@link NameRelationshipType#BASIONYM() basionym} of <i>this</i> taxon name
     * and keeps the nomenclatural rule considered for it. The basionym
     * {@link NameRelationship relationship} will be added to <i>this</i> taxon name and to the basionym.
     * The basionym cannot have itself a basionym.
     * The {@link HomotypicalGroup homotypical groups} of <i>this</i> taxon name and of the basionym
     * will be {@link HomotypicalGroup#merge(HomotypicalGroup) merged}.
     *
     * @param  basionym			the taxon name to be set as the basionym of <i>this</i> taxon name
     * @param  ruleConsidered	the string identifying the nomenclatural rule
     * @return
     * @see  					#getBasionym()
     * @see  					#addBasionym(TaxonNameBase)
     */
    public NameRelationship addBasionym(T basionym, Reference citation, String microcitation, String ruleConsidered){
        if (basionym != null){
            return basionym.addRelationshipToName(this, NameRelationshipType.BASIONYM(), citation, microcitation, ruleConsidered);
        }else{
            return null;
        }
    }

    /**
     * Returns the set of taxon names which are the {@link NameRelationshipType#REPLACED_SYNONYM() replaced synonyms} of <i>this</i> taxon name.
     *
     */
    @Transient
    public Set<TaxonNameBase> getReplacedSynonyms(){
        Set<TaxonNameBase> result = new HashSet<TaxonNameBase>();
        Set<NameRelationship> rels = this.getRelationsToThisName();
        for (NameRelationship rel : rels){
            if (rel.getType().isReplacedSynonymRelation()){
                TaxonNameBase replacedSynonym = rel.getFromName();
                result.add(replacedSynonym);
            }
        }
        return result;
    }

    /**
     * Assigns a taxon name as {@link NameRelationshipType#REPLACED_SYNONYM() replaced synonym} of <i>this</i> taxon name
     * and keeps the nomenclatural rule considered for it. The replaced synonym
     * {@link NameRelationship relationship} will be added to <i>this</i> taxon name and to the replaced synonym.
     * The {@link HomotypicalGroup homotypical groups} of <i>this</i> taxon name and of the replaced synonym
     * will be {@link HomotypicalGroup#merge(HomotypicalGroup) merged}.
     *
     * @param  basionym			the taxon name to be set as the basionym of <i>this</i> taxon name
     * @param  ruleConsidered	the string identifying the nomenclatural rule
     * @see  					#getBasionym()
     * @see  					#addBasionym(TaxonNameBase)
     */
    //TODO: Check if true: The replaced synonym cannot have itself a replaced synonym (?).
    public void addReplacedSynonym(T replacedSynonym, Reference citation, String microcitation, String ruleConsidered){
        if (replacedSynonym != null){
            replacedSynonym.addRelationshipToName(this, NameRelationshipType.REPLACED_SYNONYM(), citation, microcitation, ruleConsidered);
        }
    }

    /**
     * Removes the {@link NameRelationshipType#BASIONYM() basionym} {@link NameRelationship relationship} from the set of
     * {@link #getRelationsToThisName() name relationships to} <i>this</i> taxon name. The same relationhip will be
     * removed from the set of {@link #getRelationsFromThisName() name relationships from} the taxon name
     * previously used as basionym.
     *
     * @see   #getBasionym()
     * @see   #addBasionym(TaxonNameBase)
     */
    public void removeBasionyms(){
        Set<NameRelationship> removeRelations = new HashSet<NameRelationship>();
        for (NameRelationship nameRelation : this.getRelationsToThisName()){
            if (nameRelation.getType().isBasionymRelation()){
                removeRelations.add(nameRelation);
            }
        }
        // Removing relations from a set through which we are iterating causes a
        // ConcurrentModificationException. Therefore, we delete the targeted
        // relations in a second step.
        for (NameRelationship relation : removeRelations){
            this.removeNameRelationship(relation);
        }
    }

    /**
     * Returns the taxonomic {@link Rank rank} of <i>this</i> taxon name.
     *
     * @see 	Rank
     */
    public Rank getRank(){
        return this.rank;
    }

    /**
     * @see  #getRank()
     */
    public void setRank(Rank rank){
        this.rank = rank;
    }

    /**
     * Returns the {@link eu.etaxonomy.cdm.model.reference.INomenclaturalReference nomenclatural reference} of <i>this</i> taxon name.
     * The nomenclatural reference is here meant to be the one publication
     * <i>this</i> taxon name was originally published in while fulfilling the formal
     * requirements as specified by the corresponding {@link NomenclaturalCode nomenclatural code}.
     *
     * @see 	eu.etaxonomy.cdm.model.reference.INomenclaturalReference
     * @see 	eu.etaxonomy.cdm.model.reference.Reference
     */
    public INomenclaturalReference getNomenclaturalReference(){
        return this.nomenclaturalReference;
    }
    /**
     * Assigns a {@link eu.etaxonomy.cdm.model.reference.INomenclaturalReference nomenclatural reference} to <i>this</i> taxon name.
     * The corresponding {@link eu.etaxonomy.cdm.model.reference.Reference.isNomenclaturallyRelevant nomenclaturally relevant flag} will be set to true
     * as it is obviously used for nomenclatural purposes.
     *
     * @throws IllegalArgumentException if parameter <code>nomenclaturalReference</code> is not assignable from {@link INomenclaturalReference}
     * @see  #getNomenclaturalReference()
     */
    public void setNomenclaturalReference(INomenclaturalReference nomenclaturalReference){
        if(nomenclaturalReference != null){
            if(!INomenclaturalReference.class.isAssignableFrom(nomenclaturalReference.getClass())){
                throw new IllegalArgumentException("Parameter nomenclaturalReference is not assignable from INomenclaturalReference");
            }
            this.nomenclaturalReference = (Reference)nomenclaturalReference;
        } else {
            this.nomenclaturalReference = null;
        }
    }

    /**
     * Returns the appended phrase string assigned to <i>this</i> taxon name.
     * The appended phrase is a non-atomised addition to a name. It is
     * not ruled by a nomenclatural code.
     */
    public String getAppendedPhrase(){
        return this.appendedPhrase;
    }

    /**
     * @see  #getAppendedPhrase()
     */
    public void setAppendedPhrase(String appendedPhrase){
        this.appendedPhrase = StringUtils.isBlank(appendedPhrase)? null : appendedPhrase;
    }

    /**
     * Returns the details string of the {@link #getNomenclaturalReference() nomenclatural reference} assigned
     * to <i>this</i> taxon name. The details describe the exact localisation within
     * the publication used as nomenclature reference. These are mostly
     * (implicitly) pages but can also be figures or tables or any other
     * element of a publication. A nomenclatural micro reference (details)
     * requires the existence of a nomenclatural reference.
     */
    //Details of the nomenclatural reference (protologue).
    public String getNomenclaturalMicroReference(){
        return this.nomenclaturalMicroReference;
    }
    /**
     * @see  #getNomenclaturalMicroReference()
     */
    public void setNomenclaturalMicroReference(String nomenclaturalMicroReference){
        this.nomenclaturalMicroReference = StringUtils.isBlank(nomenclaturalMicroReference)? null : nomenclaturalMicroReference;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.IParsable#getHasProblem()
     */
    @Override
    public int getParsingProblem(){
        return this.parsingProblem;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.IParsable#setHasProblem(int)
     */
    @Override
    public void setParsingProblem(int parsingProblem){
        this.parsingProblem = parsingProblem;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.IParsable#addProblem(eu.etaxonomy.cdm.strategy.parser.NameParserWarning)
     */
    @Override
    public void addParsingProblem(ParserProblem problem){
        parsingProblem = ParserProblem.addProblem(parsingProblem, problem);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.IParsable#removeParsingProblem(eu.etaxonomy.cdm.strategy.parser.ParserProblem)
     */
    @Override
    public void removeParsingProblem(ParserProblem problem) {
        parsingProblem = ParserProblem.removeProblem(parsingProblem, problem);
    }

    /**
     * @param warnings
     */
    public void addParsingProblems(int problems){
        parsingProblem = ParserProblem.addProblems(parsingProblem, problems);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.IParsable#hasProblem()
     */
    @Override
    public boolean hasProblem(){
        return parsingProblem != 0;
    }



    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.IParsable#hasProblem(eu.etaxonomy.cdm.strategy.parser.ParserProblem)
     */
    @Override
    public boolean hasProblem(ParserProblem problem) {
        return getParsingProblems().contains(problem);
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.IParsable#problemStarts()
     */
    @Override
    public int getProblemStarts(){
        return this.problemStarts;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.IParsable#setProblemStarts(int)
     */
    @Override
    public void setProblemStarts(int start) {
        this.problemStarts = start;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.IParsable#problemEnds()
     */
    @Override
    public int getProblemEnds(){
        return this.problemEnds;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.IParsable#setProblemEnds(int)
     */
    @Override
    public void setProblemEnds(int end) {
        this.problemEnds = end;
    }

//*********************** TYPE DESIGNATION *********************************************//

    /**
     * Returns the set of {@link TypeDesignationBase type designations} assigned
     * to <i>this</i> taxon name.
     * @see     NameTypeDesignation
     * @see     SpecimenTypeDesignation
     */
    public Set<TypeDesignationBase> getTypeDesignations() {
        if(typeDesignations == null) {
            this.typeDesignations = new HashSet<TypeDesignationBase>();
        }
        return typeDesignations;
    }

    /**
     * Removes one element from the set of {@link TypeDesignationBase type designations} assigned to
     * <i>this</i> taxon name. The type designation itself will be nullified.
     *
     * @param  typeDesignation  the type designation which should be deleted
     */
    @SuppressWarnings("deprecation")
    public void removeTypeDesignation(TypeDesignationBase typeDesignation) {
        this.typeDesignations.remove(typeDesignation);
        typeDesignation.removeTypifiedName(this);
    }

    /**
     * Returns the set of {@link SpecimenTypeDesignation specimen type designations} assigned
     * to <i>this</i> taxon name. The {@link Rank rank} of <i>this</i> taxon name is generally
     * "species" or below. The specimen type designations include all the
     * specimens on which the typification of this name is based (which are
     * exclusively used to typify taxon names belonging to the same
     * {@link HomotypicalGroup homotypical group} to which <i>this</i> taxon name
     * belongs) and eventually the status of these designations.
     *
     * @see     SpecimenTypeDesignation
     * @see     NameTypeDesignation
     * @see     HomotypicalGroup
     */
    @Transient
    public Set<SpecimenTypeDesignation> getSpecimenTypeDesignationsOfHomotypicalGroup() {
        return this.getHomotypicalGroup().getSpecimenTypeDesignations();
    }

//*********************** NAME TYPE DESIGNATION *********************************************//

    /**
     * Returns the set of {@link NameTypeDesignation name type designations} assigned
     * to <i>this</i> taxon name the rank of which must be above "species".
     * The name type designations include all the taxon names used to typify
     * <i>this</i> taxon name and eventually the rejected or conserved status
     * of these designations.
     *
     * @see     NameTypeDesignation
     * @see     SpecimenTypeDesignation
     */
    @Transient
    public Set<NameTypeDesignation> getNameTypeDesignations() {
        Set<NameTypeDesignation> result = new HashSet<NameTypeDesignation>();
        for (TypeDesignationBase typeDesignation : this.typeDesignations){
            if (typeDesignation instanceof NameTypeDesignation){
                result.add((NameTypeDesignation)typeDesignation);
            }
        }
        return result;
    }

    /**
     * Creates and adds a new {@link NameTypeDesignation name type designation}
     * to <i>this</i> taxon name's set of type designations.
     *
     * @param  typeSpecies				the taxon name to be used as type of <i>this</i> taxon name
     * @param  citation					the reference for this new designation
     * @param  citationMicroReference	the string with the details (generally pages) within the reference
     * @param  originalNameString		the taxon name string used in the reference to assert this designation
     * @param  isRejectedType			the boolean status for a rejected name type designation
     * @param  isConservedType			the boolean status for a conserved name type designation
     * @param  isLectoType				the boolean status for a lectotype name type designation
     * @param  isNotDesignated			the boolean status for a name type designation without name type
     * @param  addToAllHomotypicNames	the boolean indicating whether the name type designation should be
     * 									added to all taxon names of the homotypical group this taxon name belongs to
     * @return
     * @see 			  				#getNameTypeDesignations()
     * @see 			  				NameTypeDesignation
     * @see 			  				TypeDesignationBase#isNotDesignated()
     */
    public NameTypeDesignation addNameTypeDesignation(TaxonNameBase typeSpecies,
                Reference citation,
                String citationMicroReference,
                String originalNameString,
                NameTypeDesignationStatus status,
                boolean isRejectedType,
                boolean isConservedType,
                /*boolean isLectoType, */
                boolean isNotDesignated,
                boolean addToAllHomotypicNames) {
        NameTypeDesignation nameTypeDesignation = new NameTypeDesignation(typeSpecies, citation, citationMicroReference, originalNameString, status, isRejectedType, isConservedType, isNotDesignated);
        //nameTypeDesignation.setLectoType(isLectoType);
        addTypeDesignation(nameTypeDesignation, addToAllHomotypicNames);
        return nameTypeDesignation;
    }

    /**
     * Creates and adds a new {@link NameTypeDesignation name type designation}
     * to <i>this</i> taxon name's set of type designations.
     *
     * @param  typeSpecies				the taxon name to be used as type of <i>this</i> taxon name
     * @param  citation					the reference for this new designation
     * @param  citationMicroReference	the string with the details (generally pages) within the reference
     * @param  originalNameString		the taxon name string used in the reference to assert this designation
     * @param  status                   the name type designation status
     * @param  addToAllHomotypicNames	the boolean indicating whether the name type designation should be
     * 									added to all taxon names of the homotypical group this taxon name belongs to
     * @return
     * @see 			  				#getNameTypeDesignations()
     * @see 			  				NameTypeDesignation
     * @see 			  				TypeDesignationBase#isNotDesignated()
     */
    public NameTypeDesignation addNameTypeDesignation(TaxonNameBase typeSpecies,
                Reference citation,
                String citationMicroReference,
                String originalNameString,
                NameTypeDesignationStatus status,
                boolean addToAllHomotypicNames) {
        NameTypeDesignation nameTypeDesignation = new NameTypeDesignation(typeSpecies, status, citation, citationMicroReference, originalNameString);
        addTypeDesignation(nameTypeDesignation, addToAllHomotypicNames);
        return nameTypeDesignation;
    }

//*********************** SPECIMEN TYPE DESIGNATION *********************************************//

    /**
     * Returns the set of {@link SpecimenTypeDesignation specimen type designations}
     * that typify <i>this</i> taxon name.
     */
    @Transient
    public Set<SpecimenTypeDesignation> getSpecimenTypeDesignations() {
        Set<SpecimenTypeDesignation> result = new HashSet<SpecimenTypeDesignation>();
        for (TypeDesignationBase typeDesignation : this.typeDesignations){
            if (typeDesignation instanceof SpecimenTypeDesignation){
                result.add((SpecimenTypeDesignation)typeDesignation);
            }
        }
        return result;
    }


    /**
     * Creates and adds a new {@link SpecimenTypeDesignation specimen type designation}
     * to <i>this</i> taxon name's set of type designations.
     *
     * @param  typeSpecimen				the specimen to be used as a type for <i>this</i> taxon name
     * @param  status					the specimen type designation status
     * @param  citation					the reference for this new specimen type designation
     * @param  citationMicroReference	the string with the details (generally pages) within the reference
     * @param  originalNameString		the taxon name used in the reference to assert this designation
     * @param  isNotDesignated			the boolean status for a specimen type designation without specimen type
     * @param  addToAllHomotypicNames	the boolean indicating whether the specimen type designation should be
     * 									added to all taxon names of the homotypical group the typified
     * 									taxon name belongs to
     * @return
     * @see 			  				#getSpecimenTypeDesignations()
     * @see 			  				SpecimenTypeDesignationStatus
     * @see 			  				SpecimenTypeDesignation
     * @see 			  				TypeDesignationBase#isNotDesignated()
     */
    public SpecimenTypeDesignation addSpecimenTypeDesignation(DerivedUnit typeSpecimen,
                SpecimenTypeDesignationStatus status,
                Reference citation,
                String citationMicroReference,
                String originalNameString,
                boolean isNotDesignated,
                boolean addToAllHomotypicNames) {
        SpecimenTypeDesignation specimenTypeDesignation = new SpecimenTypeDesignation(typeSpecimen, status, citation, citationMicroReference, originalNameString, isNotDesignated);
        addTypeDesignation(specimenTypeDesignation, addToAllHomotypicNames);
        return specimenTypeDesignation;
    }

    //used by merge strategy
    private boolean addTypeDesignation(TypeDesignationBase typeDesignation){
        return addTypeDesignation(typeDesignation, true);
    }

    /**
     * Adds a {@link TypeDesignationBase type designation} to <code>this</code> taxon name's set of type designations
     *
     * @param typeDesignation			the typeDesignation to be added to <code>this</code> taxon name
     * @param addToAllNames				the boolean indicating whether the type designation should be
     * 									added to all taxon names of the homotypical group the typified
     * 									taxon name belongs to
     * @return							true if the operation was succesful
     *
     * @throws IllegalArgumentException	if the type designation already has typified names, an {@link IllegalArgumentException exception}
     * 									is thrown. We do this to prevent a type designation to be used for multiple taxon names.
     *
     */
    public boolean addTypeDesignation(TypeDesignationBase typeDesignation, boolean addToAllNames){
        //currently typeDesignations are not persisted with the homotypical group
        //so explicit adding to the homotypical group is not necessary.
        if (typeDesignation != null){
            checkHomotypicalGroup(typeDesignation);
            this.typeDesignations.add(typeDesignation);
            typeDesignation.addTypifiedName(this);

            if (addToAllNames){
                for (TaxonNameBase taxonName : this.getHomotypicalGroup().getTypifiedNames()){
                    if (taxonName != this){
                        taxonName.addTypeDesignation(typeDesignation, false);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Throws an Exception this type designation already has typified names from another homotypical group.
     * @param typeDesignation
     */
    private void checkHomotypicalGroup(TypeDesignationBase typeDesignation) {
        if(typeDesignation.getTypifiedNames().size() > 0){
            Set<HomotypicalGroup> groups = new HashSet<HomotypicalGroup>();
            Set<TaxonNameBase> names = typeDesignation.getTypifiedNames();
            for (TaxonNameBase taxonName: names){
                groups.add(taxonName.getHomotypicalGroup());
            }
            if (groups.size() > 1){
                throw new IllegalArgumentException("TypeDesignation already has typified names from another homotypical group.");
            }
        }
    }



//*********************** HOMOTYPICAL GROUP *********************************************//


    /**
     * Returns the {@link HomotypicalGroup homotypical group} to which
     * <i>this</i> taxon name belongs. A homotypical group represents all taxon names
     * that share the same types.
     *
     * @see 	HomotypicalGroup
     */

    public HomotypicalGroup getHomotypicalGroup() {
        if (homotypicalGroup == null){
            homotypicalGroup = new HomotypicalGroup();
            homotypicalGroup.typifiedNames.add(this);
        }
    	return homotypicalGroup;
    }

    /*
     * @see #getHomotypicalGroup()
     */
    public void setHomotypicalGroup(HomotypicalGroup homotypicalGroup) {
        if (homotypicalGroup == null){
            throw new IllegalArgumentException("HomotypicalGroup of name should never be null but was set to 'null'");
        }
        /*if (this.homotypicalGroup != null){
        	this.homotypicalGroup.removeTypifiedName(this, false);
        }*/
        this.homotypicalGroup = homotypicalGroup;
        if (!this.homotypicalGroup.typifiedNames.contains(this)){
        	 this.homotypicalGroup.addTypifiedName(this);
        }
    }



// *************************************************************************//

    /**
     * Returns the complete string containing the
     * {@link eu.etaxonomy.cdm.model.reference.INomenclaturalReference#getNomenclaturalCitation() nomenclatural reference citation}
     * and the {@link #getNomenclaturalMicroReference() details} assigned to <i>this</i> taxon name.
     *
     * @return  the string containing the nomenclatural reference of <i>this</i> taxon name
     * @see		eu.etaxonomy.cdm.model.reference.INomenclaturalReference#getNomenclaturalCitation()
     * @see		#getNomenclaturalReference()
     * @see		#getNomenclaturalMicroReference()
     */
    @Transient
    public String getCitationString(){
        return getNomenclaturalReference().getNomenclaturalCitation(getNomenclaturalMicroReference());
    }

    /**
     * Returns the parsing problems
     * @return
     */
    @Override
    public List<ParserProblem> getParsingProblems(){
        return ParserProblem.warningList(this.parsingProblem);
    }

    /**
     * Returns the string containing the publication date (generally only year)
     * of the {@link #getNomenclaturalReference() nomenclatural reference} for <i>this</i> taxon name, null if there is
     * no nomenclatural reference.
     *
     * @return  the string containing the publication date of <i>this</i> taxon name
     * @see		eu.etaxonomy.cdm.model.reference.INomenclaturalReference#getYear()
     */
    @Transient
    @ValidTaxonomicYear(groups=Level3.class)
    public String getReferenceYear(){
        if (this.getNomenclaturalReference() != null ){
            return this.getNomenclaturalReference().getYear();
        }else{
            return null;
        }
    }

    /**
     * Returns the set of {@link eu.etaxonomy.cdm.model.taxon.TaxonBase taxon bases} that refer to <i>this</i> taxon name.
     * In this context a taxon base means the use of a taxon name by a reference
     * either as a {@link eu.etaxonomy.cdm.model.taxon.Taxon taxon} ("accepted/correct" name) or
     * as a (junior) {@link eu.etaxonomy.cdm.model.taxon.Synonym synonym}.
     * A taxon name can be used by several distinct {@link eu.etaxonomy.cdm.model.reference.Reference references} but only once
     * within a taxonomic treatment (identified by one reference).
     *
     * @see	#getTaxa()
     * @see	#getSynonyms()
     */
    public Set<TaxonBase> getTaxonBases() {
        if(taxonBases == null) {
            this.taxonBases = new HashSet<TaxonBase>();
        }
        return this.taxonBases;
    }

    /**
     * Adds a new {@link eu.etaxonomy.cdm.model.taxon.TaxonBase taxon base}
     * to the set of taxon bases using <i>this</i> taxon name.
     *
     * @param  taxonBase  the taxon base to be added
     * @see 			  #getTaxonBases()
     * @see 			  #removeTaxonBase(TaxonBase)
     */
    //TODO protected
    public void addTaxonBase(TaxonBase taxonBase){
        Method method = ReflectionUtils.findMethod(TaxonBase.class, "setName", new Class[] {TaxonNameBase.class});
        ReflectionUtils.makeAccessible(method);
        ReflectionUtils.invokeMethod(method, taxonBase, new Object[] {this});
        taxonBases.add(taxonBase);
    }
    /**
     * Removes one element from the set of {@link eu.etaxonomy.cdm.model.taxon.TaxonBase taxon bases} that refer to <i>this</i> taxon name.
     *
     * @param  taxonBase	the taxon base which should be removed from the corresponding set
     * @see    				#getTaxonBases()
     * @see    				#addTaxonBase(TaxonBase)
     */
    public void removeTaxonBase(TaxonBase taxonBase){
        Method method = ReflectionUtils.findMethod(TaxonBase.class, "setName", new Class[] {TaxonNameBase.class});
        ReflectionUtils.makeAccessible(method);
        ReflectionUtils.invokeMethod(method, taxonBase, new Object[] {null});


    }

    /**
     * Returns the set of {@link eu.etaxonomy.cdm.model.taxon.Taxon taxa} ("accepted/correct" names according to any
     * reference) that are based on <i>this</i> taxon name. This set is a subset of
     * the set returned by getTaxonBases().
     *
     * @see	eu.etaxonomy.cdm.model.taxon.Taxon
     * @see	#getTaxonBases()
     * @see	#getSynonyms()
     */
    @Transient
    public Set<Taxon> getTaxa(){
        Set<Taxon> result = new HashSet<Taxon>();
        for (TaxonBase taxonBase : this.taxonBases){
            if (taxonBase instanceof Taxon){
                result.add((Taxon)taxonBase);
            }
        }
        return result;
    }

    /**
     * Returns the set of {@link eu.etaxonomy.cdm.model.taxon.Synonym (junior) synonyms} (according to any
     * reference) that are based on <i>this</i> taxon name. This set is a subset of
     * the set returned by getTaxonBases().
     *
     * @see	eu.etaxonomy.cdm.model.taxon.Synonym
     * @see	#getTaxonBases()
     * @see	#getTaxa()
     */
    @Transient
    public Set<Synonym> getSynonyms() {
        Set<Synonym> result = new HashSet<Synonym>();
        for (TaxonBase taxonBase : this.taxonBases){
            if (taxonBase instanceof Synonym){
                result.add((Synonym)taxonBase);
            }
        }
        return result;
    }


// *********** DESCRIPTIONS *************************************

    /**
     * Returns the set of {@link eu.etaxonomy.cdm.model.description.TaxonNameDescription taxon name descriptions} assigned
     * to <i>this</i> taxon name. A taxon name description is a piece of information
     * concerning the taxon name like for instance the content of its first
     * publication (protolog) or a picture of this publication.
     *
     * @see	#addDescription(TaxonNameDescription)
     * @see	#removeDescription(TaxonNameDescription)
     * @see	eu.etaxonomy.cdm.model.description.TaxonNameDescription
     */
    public Set<TaxonNameDescription> getDescriptions() {
        return descriptions;
    }

    /**
     * Adds a new {@link eu.etaxonomy.cdm.model.description.TaxonNameDescription taxon name description}
     * to the set of taxon name descriptions assigned to <i>this</i> taxon name. The
     * content of the {@link eu.etaxonomy.cdm.model.description.TaxonNameDescription#getTaxonName() taxonName attribute} of the
     * taxon name description itself will be replaced with <i>this</i> taxon name.
     *
     * @param  description  the taxon name description to be added
     * @see					#getDescriptions()
     * @see 			  	#removeDescription(TaxonNameDescription)
     */
    public void addDescription(TaxonNameDescription description) {
        java.lang.reflect.Field field = ReflectionUtils.findField(TaxonNameDescription.class, "taxonName", TaxonNameBase.class);
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, description, this);
        descriptions.add(description);
    }
    /**
     * Removes one element from the set of {@link eu.etaxonomy.cdm.model.description.TaxonNameDescription taxon name descriptions} assigned
     * to <i>this</i> taxon name. The content of the {@link eu.etaxonomy.cdm.model.description.TaxonNameDescription#getTaxonName() taxonName attribute}
     * of the description itself will be set to "null".
     *
     * @param  description  the taxon name description which should be removed
     * @see     		  	#getDescriptions()
     * @see     		  	#addDescription(TaxonNameDescription)
     * @see 			  	eu.etaxonomy.cdm.model.description.TaxonNameDescription#getTaxonName()
     */
    public void removeDescription(TaxonNameDescription description) {
        java.lang.reflect.Field field = ReflectionUtils.findField(TaxonNameDescription.class, "taxonName", TaxonNameBase.class);
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, description, null);
        descriptions.remove(description);
    }

// *********** HOMOTYPIC GROUP METHODS **************************************************

    @Transient
    public void mergeHomotypicGroups(TaxonNameBase name){
        this.getHomotypicalGroup().merge(name.getHomotypicalGroup());
        //HomotypicalGroup thatGroup = name.homotypicalGroup;
        name.setHomotypicalGroup(this.homotypicalGroup);
    }

    /**
     * Returns the boolean value indicating whether a given taxon name belongs
     * to the same {@link HomotypicalGroup homotypical group} as <i>this</i> taxon name (true)
     * or not (false). Returns "true" only if the homotypical groups of both
     * taxon names exist and if they are identical.
     *
     * @param	homoTypicName  the taxon name the homotypical group of which is to be checked
     * @return  			   the boolean value of the check
     * @see     			   HomotypicalGroup
     */
    @Transient
    public boolean isHomotypic(TaxonNameBase homoTypicName) {
        if (homoTypicName == null) {
            return false;
        }
        HomotypicalGroup homotypicGroup = homoTypicName.getHomotypicalGroup();
        if (homotypicGroup == null || this.getHomotypicalGroup() == null) {
            return false;
        }
        if (homotypicGroup.equals(this.getHomotypicalGroup())) {
            return true;
        }
        return false;
    }


    /**
     * Checks whether name is a basionym for ALL names
     * in its homotypical group.
     * Returns <code>false</code> if there are no other names in the group
     * @param name
     * @return
     */
    @Transient
    public boolean isGroupsBasionym() {
    	if (homotypicalGroup == null){
    		homotypicalGroup = HomotypicalGroup.NewInstance();
    		homotypicalGroup.addTypifiedName(this);
    	}
        Set<TaxonNameBase> typifiedNames = homotypicalGroup.getTypifiedNames();

        // Check whether there are any other names in the group
        if (typifiedNames.size() == 1) {
                return false;
        }

        boolean isBasionymToAll = true;

        for (TaxonNameBase taxonName : typifiedNames) {
                if (!taxonName.equals(this)) {
                        if (! isBasionymFor(taxonName)) {
                                return false;
                        }
                }
        }
        return true;
    }

    /**
     * Checks whether a basionym relationship exists between fromName and toName.
     *
     * @param fromName
     * @param toName
     * @return
     */
    @Transient
    public boolean isBasionymFor(TaxonNameBase newCombinationName) {
            Set<NameRelationship> relations = newCombinationName.getRelationsToThisName();
            for (NameRelationship relation : relations) {
                    if (relation.getType().equals(NameRelationshipType.BASIONYM()) &&
                                    relation.getFromName().equals(this)) {
                            return true;
                    }
            }
            return false;
    }

    /**
     * Creates a basionym relationship to all other names in this names homotypical
     * group.
     *
     * @see HomotypicalGroup.setGroupBasionym(TaxonNameBase basionymName)

     */
    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.name.HomotypicalGroup#setGroupBasionym(TaxonNameBase)
     */
    @Transient
    public void makeGroupsBasionym() {
        this.homotypicalGroup.setGroupBasionym(this);
    }


//*********  Rank comparison shortcuts   ********************//
    /**
     * Returns the boolean value indicating whether the taxonomic {@link Rank rank} of <i>this</i>
     * taxon name is higher than the genus rank (true) or not (false).
     * Suprageneric non viral names are monomials.
     * Returns false if rank is null.
     *
     * @see  #isGenus()
     * @see  #isInfraGeneric()
     * @see  #isSpecies()
     * @see  #isInfraSpecific()
     */
    @Transient
    public boolean isSupraGeneric() {
        if (rank == null){
            return false;
        }
        return getRank().isSupraGeneric();
    }
    /**
     * Returns the boolean value indicating whether the taxonomic {@link Rank rank} of <i>this</i>
     * taxon name is the genus rank (true) or not (false). Non viral names with
     * genus rank are monomials. Returns false if rank is null.
     *
     * @see  #isSupraGeneric()
     * @see  #isInfraGeneric()
     * @see  #isSpecies()
     * @see  #isInfraSpecific()
     */
    @Transient
    public boolean isGenus() {
        if (rank == null){
            return false;
        }
        return getRank().isGenus();
    }
    /**
     * Returns the boolean value indicating whether the taxonomic {@link Rank rank} of <i>this</i>
     * taxon name is higher than the species rank and lower than the
     * genus rank (true) or not (false). Infrageneric non viral names are
     * binomials. Returns false if rank is null.
     *
     * @see  #isSupraGeneric()
     * @see  #isGenus()
     * @see  #isSpecies()
     * @see  #isInfraSpecific()
     */
    @Transient
    public boolean isInfraGeneric() {
        if (rank == null){
            return false;
        }
        return getRank().isInfraGeneric();
    }

    /**
     * Returns the boolean value indicating whether the taxonomic {@link Rank rank} of <i>this</i>
     * taxon name is higher than the species rank (true) or not (false).
     * Returns false if rank is null.
     *
     * @see  #isGenus()
     * @see  #isInfraGeneric()
     * @see  #isSpecies()
     * @see  #isInfraSpecific()
     */
    @Transient
    public boolean isSupraSpecific(){
        if (rank == null) {
            return false;
        }
        return getRank().isHigher(Rank.SPECIES());
    }

    /**
     * Returns the boolean value indicating whether the taxonomic {@link Rank rank} of <i>this</i>
     * taxon name is the species rank (true) or not (false). Non viral names
     * with species rank are binomials.
     * Returns false if rank is null.
     *
     * @see  #isSupraGeneric()
     * @see  #isGenus()
     * @see  #isInfraGeneric()
     * @see  #isInfraSpecific()
     */
    @Transient
    public boolean isSpecies() {
        if (rank == null){
            return false;
        }
        return getRank().isSpecies();
    }
    /**
     * Returns the boolean value indicating whether the taxonomic {@link Rank rank} of <i>this</i>
     * taxon name is lower than the species rank (true) or not (false).
     * Infraspecific non viral names are trinomials.
     * Returns false if rank is null.
     *
     * @see  #isSupraGeneric()
     * @see  #isGenus()
     * @see  #isInfraGeneric()
     * @see  #isSpecies()
     */
    @Transient
    public boolean isInfraSpecific() {
        if (rank == null){
            return false;
        }
        return getRank().isInfraSpecific();
    }

    /**
     * Returns true if this name's rank indicates a rank that aggregates species like species
     * aggregates or species groups, false otherwise. This methods currently returns false
     * for all user defined ranks.
     *
     *@see Rank#isSpeciesAggregate()
     *
     * @return
     */
    @Transient
    public boolean isSpeciesAggregate() {
        if (rank == null){
            return false;
        }
        return getRank().isSpeciesAggregate();
    }


    /**
     * Returns null as the {@link NomenclaturalCode nomenclatural code} that governs
     * the construction of <i>this</i> taxon name since there is no specific
     * nomenclatural code defined. The real implementention takes place in the
     * subclasses {@link ViralName ViralName}, {@link BacterialName BacterialName},
     * {@link BotanicalName BotanicalName}, {@link CultivarPlantName CultivarPlantName} and
     * {@link ZoologicalName ZoologicalName}. Each taxon name is governed by one
     * and only one nomenclatural code.
     *
     * @return  null
     * @see  	#isCodeCompliant()
     * @see  	#getHasProblem()
     */
    abstract public NomenclaturalCode getNomenclaturalCode();

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.IdentifiableEntity#generateTitle()
     */
    /**
     * Generates and returns the string with the scientific name of <i>this</i>
     * taxon name (only non viral taxon names can be generated from their
     * components). This string may be stored in the inherited
     * {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache() titleCache} attribute.
     * This method overrides the generic and inherited
     * {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#generateTitle() method} from
     * {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity IdentifiableEntity}.
     *
     * @return  the string with the composed name of this non viral taxon name with authorship (and maybe year)
     * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#generateTitle()
     * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache()
     */
//	@Override
//	public abstract String generateTitle();

    /**
     * Creates a basionym relationship between this name and
     * 	each name in its homotypic group.
     *
     * @param basionymName
     */
    @Transient
    public void setAsGroupsBasionym() {


        HomotypicalGroup homotypicalGroup = this.getHomotypicalGroup();


        if (homotypicalGroup == null) {
            return;
        }

        Set<NameRelationship> relations = new HashSet<NameRelationship>();
        Set<NameRelationship> removeRelations = new HashSet<NameRelationship>();

        for(TaxonNameBase<?, ?> typifiedName : homotypicalGroup.getTypifiedNames()){

            Set<NameRelationship> nameRelations = typifiedName.getRelationsFromThisName();

            for(NameRelationship nameRelation : nameRelations){
                relations.add(nameRelation);
            }
        }

        for (NameRelationship relation : relations) {

            // If this is a basionym relation, and toName is in the homotypical group,
            //	remove the relationship.
            if (relation.getType().equals(NameRelationshipType.BASIONYM()) &&
                    relation.getToName().getHomotypicalGroup().equals(homotypicalGroup)) {
                removeRelations.add(relation);
            }
        }

        // Removing relations from a set through which we are iterating causes a
        //	ConcurrentModificationException. Therefore, we delete the targeted
        //	relations in a second step.
        for (NameRelationship relation : removeRelations) {
            this.removeNameRelationship(relation);
        }


        for (TaxonNameBase<?, ?> name : homotypicalGroup.getTypifiedNames()) {
            if (!name.equals(this)) {

                // First check whether the relationship already exists
                if (!this.isBasionymFor(name)) {

                    // Then create it
                    name.addRelationshipFromName(this,
                            NameRelationshipType.BASIONYM(), null);
                }
            }
        }
    }

    /**
     * Removes basionym relationship between this name and
     * 	each name in its homotypic group.
     *
     * @param basionymName
     */
    @Transient
    public void removeAsGroupsBasionym() {

        HomotypicalGroup homotypicalGroup = this.getHomotypicalGroup();

        if (homotypicalGroup == null) {
            return;
        }

        Set<NameRelationship> relations = new HashSet<NameRelationship>();
        Set<NameRelationship> removeRelations = new HashSet<NameRelationship>();

        for(TaxonNameBase<?, ?> typifiedName : homotypicalGroup.getTypifiedNames()){

            Set<NameRelationship> nameRelations = typifiedName.getRelationsFromThisName();

            for(NameRelationship nameRelation : nameRelations){
                relations.add(nameRelation);
            }
        }

        for (NameRelationship relation : relations) {

            // If this is a basionym relation, and toName is in the homotypical group,
            //	and fromName is basionymName, remove the relationship.
            if (relation.getType().equals(NameRelationshipType.BASIONYM()) &&
                    relation.getFromName().equals(this) &&
                    relation.getToName().getHomotypicalGroup().equals(homotypicalGroup)) {
                removeRelations.add(relation);
            }
        }

        // Removing relations from a set through which we are iterating causes a
        //	ConcurrentModificationException. Therefore, we delete the targeted
        //	relations in a second step.
        for (NameRelationship relation : removeRelations) {
            this.removeNameRelationship(relation);
        }
    }

//*********************** CLONE ********************************************************/

    /**
     * Clones <i>this</i> taxon name. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> taxon name by
     * modifying only some of the attributes.<BR><BR>
     * Usages of this name in a taxon concept are <b>not</b> cloned.<BR>
     * <b>The name gets a newly created homotypical group</b><BR>
     * (CAUTION: this behaviour needs to be discussed and may change in future).<BR><BR>
     * {@link TaxonNameDescription Name descriptions} are cloned and not reused.<BR>
     * {@link TypeDesignationBase Type designations} are cloned and not reused.<BR>
     *
     * @see eu.etaxonomy.cdm.model.media.IdentifiableEntity#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        TaxonNameBase result;
        try {
            result = (TaxonNameBase)super.clone();

            //taxonBases -> empty
            result.taxonBases = new HashSet<TaxonBase>();

            //empty caches
            if (! protectedFullTitleCache){
                result.fullTitleCache = null;
            }

            //descriptions
            result.descriptions = new HashSet<TaxonNameDescription>();
            for (TaxonNameDescription taxonNameDescription : getDescriptions()){
                TaxonNameDescription newDescription = (TaxonNameDescription)taxonNameDescription.clone();
                result.descriptions.add(newDescription);
            }

            //status
            result.status = new HashSet<NomenclaturalStatus>();
            for (NomenclaturalStatus nomenclaturalStatus : getStatus()){
                NomenclaturalStatus newStatus = (NomenclaturalStatus)nomenclaturalStatus.clone();
                result.status.add(newStatus);
            }


            //To Relations
            result.relationsToThisName = new HashSet<NameRelationship>();
            for (NameRelationship toRelationship : getRelationsToThisName()){
                NameRelationship newRelationship = (NameRelationship)toRelationship.clone();
                newRelationship.setRelatedTo(result);
                result.relationsToThisName.add(newRelationship);
            }

            //From Relations
            result.relationsFromThisName = new HashSet<NameRelationship>();
            for (NameRelationship fromRelationship : getRelationsFromThisName()){
                NameRelationship newRelationship = (NameRelationship)fromRelationship.clone();
                newRelationship.setRelatedFrom(result);
                result.relationsFromThisName.add(newRelationship);
            }

            //type designations
            result.typeDesignations = new HashSet<TypeDesignationBase>();
            for (TypeDesignationBase typeDesignation : getTypeDesignations()){
                TypeDesignationBase newDesignation = (TypeDesignationBase)typeDesignation.clone();
                result.typeDesignations.add(newDesignation);
                newDesignation.addTypifiedName(result);
            }

            //homotypicalGroup
            //TODO still needs to be discussed
            result.homotypicalGroup = HomotypicalGroup.NewInstance();
            result.homotypicalGroup.addTypifiedName(this);

            //no changes to: appendedPharse, nomenclaturalReference,
            //nomenclaturalMicroReference, parsingProblem, problemEnds, problemStarts
            //protectedFullTitleCache, rank
            return result;
        } catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }

    }
}
