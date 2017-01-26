/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
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
import org.hibernate.annotations.Table;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.ReflectionUtils;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
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
import eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.Match;
import eu.etaxonomy.cdm.strategy.match.Match.ReplaceMode;
import eu.etaxonomy.cdm.strategy.match.MatchMode;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;
import eu.etaxonomy.cdm.strategy.parser.ParserProblem;
import eu.etaxonomy.cdm.validation.Level2;
import eu.etaxonomy.cdm.validation.Level3;
import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;
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
    "taxonBases",

    "nameCache",
    "genusOrUninomial",
    "infraGenericEpithet",
    "specificEpithet",
    "infraSpecificEpithet",
    "combinationAuthorship",
    "exCombinationAuthorship",
    "basionymAuthorship",
    "exBasionymAuthorship",
    "authorshipCache",
    "protectedAuthorshipCache",
    "protectedNameCache",
    "hybridParentRelations",
    "hybridChildRelations",
    "hybridFormula",
    "monomHybrid",
    "binomHybrid",
    "trinomHybrid",

    "acronym",

    "subGenusAuthorship",
    "nameApprobation"
})
@XmlRootElement(name = "TaxonNameBase")
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(appliesTo="TaxonNameBase", indexes = { @org.hibernate.annotations.Index(name = "taxonNameBaseTitleCacheIndex", columnNames = { "titleCache" }),  @org.hibernate.annotations.Index(name = "taxonNameBaseNameCacheIndex", columnNames = { "nameCache" }) })
public abstract class TaxonNameBase<T extends TaxonNameBase<?,?>, S extends INameCacheStrategy>
            extends IdentifiableEntity<S>
            implements ITaxonNameBase, INonViralName, IViralName, IBacterialName,
                IParsable, IRelated, IMatchable, Cloneable {

    private static final long serialVersionUID = -791164269603409712L;
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
    private Set<TypeDesignationBase> typeDesignations = new HashSet<>();

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
    private Set<NameRelationship> relationsFromThisName = new HashSet<>();

    @XmlElementWrapper(name = "RelationsToThisName")
    @XmlElement(name = "RelationToThisName")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="relatedTo", fetch= FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @Merge(MergeMode.RELATION)
    @NotNull
    @Valid
    private Set<NameRelationship> relationsToThisName = new HashSet<>();

    @XmlElementWrapper(name = "NomenclaturalStatuses")
    @XmlElement(name = "NomenclaturalStatus")
    @OneToMany(fetch= FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE,CascadeType.DELETE})
    @NotNull
    @IndexedEmbedded(depth=1)
    private Set<NomenclaturalStatus> status = new HashSet<>();

    @XmlElementWrapper(name = "TaxonBases")
    @XmlElement(name = "TaxonBase")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="name", fetch= FetchType.LAZY)
    @NotNull
    @IndexedEmbedded(depth=1)
    private Set<TaxonBase> taxonBases = new HashSet<>();

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

//****** NonViralName attributes ***************************************/

    @XmlElement(name = "NameCache")
    @Fields({
        @Field(name = "nameCache_tokenized"),
        @Field(store = Store.YES, index = Index.YES, analyze = Analyze.YES)
    })
    @Analyzer(impl = org.apache.lucene.analysis.core.KeywordAnalyzer.class)
    @Match(value=MatchMode.CACHE, cacheReplaceMode=ReplaceMode.DEFINED,
            cacheReplacedProperties={"genusOrUninomial", "infraGenericEpithet", "specificEpithet", "infraSpecificEpithet"} )
    @NotEmpty(groups = Level2.class) // implicitly NotNull
    @Column(length=255)
    private String nameCache;

    @XmlElement(name = "ProtectedNameCache")
    @CacheUpdate(value="nameCache")
    protected boolean protectedNameCache;

    @XmlElement(name = "GenusOrUninomial")
    @Field(analyze = Analyze.YES, indexNullAs=Field.DEFAULT_NULL_TOKEN)
    @Match(MatchMode.EQUAL_REQUIRED)
    @CacheUpdate("nameCache")
    @Column(length=255)
    @Pattern(regexp = "[A-Z][a-z\\u00E4\\u00EB\\u00EF\\u00F6\\u00FC\\-]+", groups=Level2.class, message="{eu.etaxonomy.cdm.model.name.NonViralName.allowedCharactersForUninomial.message}")
    @NullOrNotEmpty
    @NotNull(groups = Level2.class)
    private String genusOrUninomial;

    @XmlElement(name = "InfraGenericEpithet")
    @Field(analyze = Analyze.YES,indexNullAs=Field.DEFAULT_NULL_TOKEN)
    @CacheUpdate("nameCache")
    //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
    @Pattern(regexp = "[a-z\\u00E4\\u00EB\\u00EF\\u00F6\\u00FC\\-]+", groups=Level2.class,message="{eu.etaxonomy.cdm.model.name.NonViralName.allowedCharactersForEpithet.message}")
    private String infraGenericEpithet;

    @XmlElement(name = "SpecificEpithet")
    @Field(analyze = Analyze.YES,indexNullAs=Field.DEFAULT_NULL_TOKEN)
    @CacheUpdate("nameCache")
    //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
    @Pattern(regexp = "[a-z\\u00E4\\u00EB\\u00EF\\u00F6\\u00FC\\-]+", groups=Level2.class, message = "{eu.etaxonomy.cdm.model.name.NonViralName.allowedCharactersForEpithet.message}")
    private String specificEpithet;

    @XmlElement(name = "InfraSpecificEpithet")
    @Field(analyze = Analyze.YES,indexNullAs=Field.DEFAULT_NULL_TOKEN)
    @CacheUpdate("nameCache")
    //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
    @Pattern(regexp = "[a-z\\u00E4\\u00EB\\u00EF\\u00F6\\u00FC\\-]+", groups=Level2.class, message = "{eu.etaxonomy.cdm.model.name.NonViralName.allowedCharactersForEpithet.message}")
    private String infraSpecificEpithet;

    @XmlElement(name = "CombinationAuthorship", type = TeamOrPersonBase.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
//    @Target(TeamOrPersonBase.class)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @JoinColumn(name="combinationAuthorship_id")
    @CacheUpdate("authorshipCache")
    @IndexedEmbedded
    private TeamOrPersonBase<?> combinationAuthorship;

    @XmlElement(name = "ExCombinationAuthorship", type = TeamOrPersonBase.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
//    @Target(TeamOrPersonBase.class)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @JoinColumn(name="exCombinationAuthorship_id")
    @CacheUpdate("authorshipCache")
    @IndexedEmbedded
    private TeamOrPersonBase<?> exCombinationAuthorship;

    @XmlElement(name = "BasionymAuthorship", type = TeamOrPersonBase.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
//    @Target(TeamOrPersonBase.class)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @JoinColumn(name="basionymAuthorship_id")
    @CacheUpdate("authorshipCache")
    @IndexedEmbedded
    private TeamOrPersonBase<?> basionymAuthorship;

    @XmlElement(name = "ExBasionymAuthorship", type = TeamOrPersonBase.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
//    @Target(TeamOrPersonBase.class)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @JoinColumn(name="exBasionymAuthorship_id")
    @CacheUpdate("authorshipCache")
    @IndexedEmbedded
    private TeamOrPersonBase<?> exBasionymAuthorship;

    @XmlElement(name = "AuthorshipCache")
    @Fields({
        @Field(name = "authorshipCache_tokenized"),
        @Field(analyze = Analyze.NO)
    })
    @Match(value=MatchMode.CACHE, cacheReplaceMode=ReplaceMode.DEFINED,
            cacheReplacedProperties={"combinationAuthorship", "basionymAuthorship", "exCombinationAuthorship", "exBasionymAuthorship"} )
    //TODO Val #3379
//    @NotNull
    @Column(length=255)
    @Pattern(regexp = "^[A-Za-z0-9 \\u00E4\\u00EB\\u00EF\\u00F6\\u00FC\\-\\&\\,\\(\\)\\.]+$", groups=Level2.class, message = "{eu.etaxonomy.cdm.model.name.NonViralName.allowedCharactersForAuthority.message}")
    private String authorshipCache;

    @XmlElement(name = "ProtectedAuthorshipCache")
    @CacheUpdate("authorshipCache")
    protected boolean protectedAuthorshipCache;

    @XmlElementWrapper(name = "HybridRelationsFromThisName")
    @XmlElement(name = "HybridRelationsFromThisName")
    @OneToMany(mappedBy="relatedFrom", fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    @Merge(MergeMode.RELATION)
    @NotNull
    private Set<HybridRelationship> hybridParentRelations = new HashSet<>();

    @XmlElementWrapper(name = "HybridRelationsToThisName")
    @XmlElement(name = "HybridRelationsToThisName")
    @OneToMany(mappedBy="relatedTo", fetch = FetchType.LAZY, orphanRemoval=true) //a hybrid relation can be deleted automatically if the child is deleted.
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @Merge(MergeMode.RELATION)
    @NotNull
    private Set<HybridRelationship> hybridChildRelations = new HashSet<>();


    //if set: this name is a hybrid formula (a hybrid that does not have an own name) and no
    //other hybrid flags may be set. A
    //hybrid name  may not have either an authorteam nor other name components.
    @XmlElement(name ="IsHybridFormula")
    @CacheUpdate("nameCache")
    private boolean hybridFormula = false;

    @XmlElement(name ="IsMonomHybrid")
    @CacheUpdate("nameCache")
    private boolean monomHybrid = false;

    @XmlElement(name ="IsBinomHybrid")
    @CacheUpdate("nameCache")
    private boolean binomHybrid = false;

    @XmlElement(name ="IsTrinomHybrid")
    @CacheUpdate("nameCache")
    private boolean trinomHybrid = false;

// ViralName attributes ************************* /

    @XmlElement(name = "Acronym")
    @Field
    //TODO Val #3379
//  @NullOrNotEmpty
    @Column(length=255)
    private String acronym;

// BacterialName attributes ***********************/

    //Author team and year of the subgenus name
    @XmlElement(name = "SubGenusAuthorship")
    @Field
    private String subGenusAuthorship;

    //Approbation of name according to approved list, validation list, or validly published, paper in IJSB after 1980
    @XmlElement(name = "NameApprobation")
    @Field
    private String nameApprobation;

// *************** FACTORY METHODS ********************************/

    /**
     * Creates a new non viral taxon name instance
     * only containing its {@link common.Rank rank} and
      * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
     *
     * @param  rank  the rank to be assigned to <i>this</i> non viral taxon name
     * @see    #NewInstance(Rank, HomotypicalGroup)
     * @see    #NonViralName(Rank, HomotypicalGroup)
     * @see    #NonViralName()
     * @see    #NonViralName(Rank, String, String, String, String, TeamOrPersonBase, Reference, String, HomotypicalGroup)
     * @see    eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
     * @see    eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
     * @see    eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
     */
    public static NonViralName NewInstance(Rank rank){
        return new NonViralName(rank, null);
    }

    /**
     * Creates a new non viral taxon name instance
     * only containing its {@link common.Rank rank},
     * its {@link HomotypicalGroup homotypical group} and
      * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
     * The new non viral taxon name instance will be also added to the set of
     * non viral taxon names belonging to this homotypical group.
     *
     * @param  rank  the rank to be assigned to <i>this</i> non viral taxon name
     * @param  homotypicalGroup  the homotypical group to which <i>this</i> non viral taxon name belongs
     * @see    #NewInstance(Rank)
     * @see    #NonViralName(Rank, HomotypicalGroup)
     * @see    #NonViralName()
     * @see    #NonViralName(Rank, String, String, String, String, TeamOrPersonBase, Reference, String, HomotypicalGroup)
     * @see    eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
     * @see    eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
     * @see    eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
     */
    public static NonViralName NewInstance(Rank rank, HomotypicalGroup homotypicalGroup){
        return new NonViralName(rank, homotypicalGroup);
    }


    /**
     * Creates a new viral taxon name instance only containing its {@link Rank rank}.
     *
     * @param   rank  the rank to be assigned to <i>this</i> viral taxon name
     * @see     #ViralName(Rank)
     */
    public static ViralName NewViralInstance(Rank rank){
        return new ViralName(rank);
    }

    /**
     * Creates a new bacterial taxon name instance
     * only containing its {@link Rank rank} and
     * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
     *
     * @param  rank  the rank to be assigned to <i>this</i> bacterial taxon name
     * @see    #NewInstance(Rank, HomotypicalGroup)
     * @see    #BacterialName(Rank, HomotypicalGroup)
     * @see    eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
     * @see    eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
     * @see    eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
     */
    public static BacterialName NewBacterialInstance(Rank rank){
        return new BacterialName(rank, null);
    }

    /**
     * Creates a new bacterial taxon name instance
     * only containing its {@link Rank rank},
     * its {@link HomotypicalGroup homotypical group} and
     * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
     * The new bacterial taxon name instance will be also added to the set of
     * bacterial taxon names belonging to this homotypical group.
     *
     * @param  rank  the rank to be assigned to <i>this</i> bacterial taxon name
     * @param  homotypicalGroup  the homotypical group to which <i>this</i> bacterial taxon name belongs
     * @see    #NewInstance(Rank)
     * @see    #BacterialName(Rank, HomotypicalGroup)
     * @see    eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
     * @see    eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
     * @see    eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
     */
    public static BacterialName NewBacterialInstance(Rank rank, HomotypicalGroup homotypicalGroup){
        return new BacterialName(rank, homotypicalGroup);
    }

// ************* CONSTRUCTORS *************/
    /**
     * Class constructor: creates a new empty taxon name.
     *
     * @see #TaxonNameBase(Rank)
     * @see #TaxonNameBase(HomotypicalGroup)
     * @see #TaxonNameBase(Rank, HomotypicalGroup)
     */
    protected TaxonNameBase() {
        super();
        setNameCacheStrategy();
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
    protected TaxonNameBase(Rank rank) {
        this(rank, null);
    }
    /**
     * Class constructor: creates a new taxon name instance
     * only containing its {@link HomotypicalGroup homotypical group}.
     * The new taxon name will be also added to the set of taxon names
     * belonging to this homotypical group.
     *
     * @param  homotypicalGroup  the homotypical group to which <i>this</i> taxon name belongs
     * @see    					 #TaxonNameBase()
     * @see    					 #TaxonNameBase(Rank)
     * @see    					 #TaxonNameBase(Rank, HomotypicalGroup)
     */
    protected TaxonNameBase(HomotypicalGroup homotypicalGroup) {
        this(null, homotypicalGroup);
    }

    /**
     * Class constructor: creates a new taxon name instance
     * only containing its {@link Rank rank} and
     * its {@link HomotypicalGroup homotypical group} and
     * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
     * The new taxon name will be also added to the set of taxon names
     * belonging to this homotypical group.
     *
     * @param  rank  			 the rank to be assigned to <i>this</i> taxon name
     * @param  homotypicalGroup  the homotypical group to which <i>this</i> taxon name belongs
     * @see    					 #TaxonNameBase()
     * @see    					 #TaxonNameBase(Rank)
     * @see    					 #TaxonNameBase(HomotypicalGroup)
     */
    protected TaxonNameBase(Rank rank, HomotypicalGroup homotypicalGroup) {
        super();
        this.setRank(rank);
        if (homotypicalGroup == null){
            homotypicalGroup = new HomotypicalGroup();
        }
        homotypicalGroup.addTypifiedName(this);
        this.homotypicalGroup = homotypicalGroup;
        setNameCacheStrategy();
    }


    /**
     * Class constructor: creates a new non viral taxon name instance
     * containing its {@link Rank rank},
     * its {@link HomotypicalGroup homotypical group},
     * its scientific name components, its {@link eu.etaxonomy.cdm.model.agent.TeamOrPersonBase author(team)},
     * its {@link eu.etaxonomy.cdm.model.reference.Reference nomenclatural reference} and
     * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
     * The new non viral taxon name instance will be also added to the set of
     * non viral taxon names belonging to this homotypical group.
     *
     * @param   rank  the rank to be assigned to <i>this</i> non viral taxon name
     * @param   genusOrUninomial the string for <i>this</i> non viral taxon name
     *          if its rank is genus or higher or for the genus part
     *          if its rank is lower than genus
     * @param   infraGenericEpithet  the string for the first epithet of
     *          <i>this</i> non viral taxon name if its rank is lower than genus
     *          and higher than species aggregate
     * @param   specificEpithet  the string for the first epithet of
     *          <i>this</i> non viral taxon name if its rank is species aggregate or lower
     * @param   infraSpecificEpithet  the string for the second epithet of
     *          <i>this</i> non viral taxon name if its rank is lower than species
     * @param   combinationAuthorship  the author or the team who published <i>this</i> non viral taxon name
     * @param   nomenclaturalReference  the nomenclatural reference where <i>this</i> non viral taxon name was published
     * @param   nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
     * @param   homotypicalGroup  the homotypical group to which <i>this</i> non viral taxon name belongs
     * @see     #NonViralName()
     * @see     #NonViralName(Rank, HomotypicalGroup)
     * @see     #NewInstance(Rank, HomotypicalGroup)
     * @see     eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
     * @see     eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
     * @see     eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
     */
    protected TaxonNameBase(Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorship, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
        this(rank, homotypicalGroup);
        setGenusOrUninomial(genusOrUninomial);
        setInfraGenericEpithet (infraGenericEpithet);
        setSpecificEpithet(specificEpithet);
        setInfraSpecificEpithet(infraSpecificEpithet);
        setCombinationAuthorship(combinationAuthorship);
        setNomenclaturalReference(nomenclaturalReference);
        this.setNomenclaturalMicroReference(nomenclMicroRef);
    }


    private void setNameCacheStrategy(){
        if (getClass() == NonViralName.class){
            this.cacheStrategy = (S)NonViralNameDefaultCacheStrategy.NewInstance();
        }
    }


    @Override
    public void initListener(){
        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                boolean protectedByLowerCache = false;
                //authorship cache
                if (fieldHasCacheUpdateProperty(e.getPropertyName(), "authorshipCache")){
                    if (protectedAuthorshipCache){
                        protectedByLowerCache = true;
                    }else{
                        authorshipCache = null;
                    }
                }

                //nameCache
                if (fieldHasCacheUpdateProperty(e.getPropertyName(), "nameCache")){
                    if (protectedNameCache){
                        protectedByLowerCache = true;
                    }else{
                        nameCache = null;
                    }
                }
                //title cache
                if (! fieldHasNoUpdateProperty(e.getPropertyName(), "titleCache")){
                    if (isProtectedTitleCache()|| protectedByLowerCache == true ){
                        protectedByLowerCache = true;
                    }else{
                        titleCache = null;
                    }
                }
                //full title cache
                if (! fieldHasNoUpdateProperty(e.getPropertyName(), "fullTitleCache")){
                    if (isProtectedFullTitleCache()|| protectedByLowerCache == true ){
                        protectedByLowerCache = true;
                    }else{
                        fullTitleCache = null;
                    }
                }
            }
        };
        addPropertyChangeListener(listener);  //didn't use this.addXXX to make lsid.AssemblerTest run in cdmlib-remote
    }

    private static Map<String, java.lang.reflect.Field> allFields = null;
    protected Map<String, java.lang.reflect.Field> getAllFields(){
        if (allFields == null){
            allFields = CdmUtils.getAllFields(this.getClass(), CdmBase.class, false, false, false, true);
        }
        return allFields;
    }

    /**
     * @param propertyName
     * @param string
     * @return
     */
    private boolean fieldHasCacheUpdateProperty(String propertyName, String cacheName) {
        java.lang.reflect.Field field;
        try {
            field = getAllFields().get(propertyName);
            if (field != null){
                CacheUpdate updateAnnotation = field.getAnnotation(CacheUpdate.class);
                if (updateAnnotation != null){
                    for (String value : updateAnnotation.value()){
                        if (cacheName.equals(value)){
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (SecurityException e1) {
            throw e1;
        }
    }

    private boolean fieldHasNoUpdateProperty(String propertyName, String cacheName) {
        java.lang.reflect.Field field;
        //do not update fields with the same name
        if (cacheName.equals(propertyName)){
            return true;
        }
        //evaluate annotation
        try {
            field = getAllFields().get(propertyName);
            if (field != null){
                CacheUpdate updateAnnotation = field.getAnnotation(CacheUpdate.class);
                if (updateAnnotation != null){
                    for (String value : updateAnnotation.noUpdate()){
                        if (cacheName.equals(value)){
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (SecurityException e1) {
            throw e1;
        }
    }

// ****************** GETTER / SETTER ****************************/

    /**
     * Returns the boolean value of the flag intended to protect (true)
     * or not (false) the {@link #getNameCache() nameCache} (scientific name without author strings and year)
     * string of <i>this</i> non viral taxon name.
     *
     * @return  the boolean value of the protectedNameCache flag
     * @see     #getNameCache()
     */
    @Override
    public boolean isProtectedNameCache() {
        return protectedNameCache;
    }

    /**
     * @see     #isProtectedNameCache()
     */
    @Override
    public void setProtectedNameCache(boolean protectedNameCache) {
        this.protectedNameCache = protectedNameCache;
    }

    /**
     * Returns either the scientific name string (without authorship) for <i>this</i>
     * non viral taxon name if its rank is genus or higher (monomial) or the string for
     * the genus part of it if its {@link Rank rank} is lower than genus (bi- or trinomial).
     * Genus or uninomial strings begin with an upper case letter.
     *
     * @return  the string containing the suprageneric name, the genus name or the genus part of <i>this</i> non viral taxon name
     * @see     #getNameCache()
     */
    @Override
    public String getGenusOrUninomial() {
        return genusOrUninomial;
    }

    /**
     * @see  #getGenusOrUninomial()
     */
    @Override
    public void setGenusOrUninomial(String genusOrUninomial) {
        this.genusOrUninomial = StringUtils.isBlank(genusOrUninomial) ? null : genusOrUninomial;
    }

    /**
     * Returns the genus subdivision epithet string (infrageneric part) for
     * <i>this</i> non viral taxon name if its {@link Rank rank} is infrageneric (lower than genus and
     * higher than species aggregate: binomial). Genus subdivision epithet
     * strings begin with an upper case letter.
     *
     * @return  the string containing the infrageneric part of <i>this</i> non viral taxon name
     * @see     #getNameCache()
     */
    @Override
    public String getInfraGenericEpithet(){
        return this.infraGenericEpithet;
    }

    /**
     * @see  #getInfraGenericEpithet()
     */
    @Override
    public void setInfraGenericEpithet(String infraGenericEpithet){
        this.infraGenericEpithet = StringUtils.isBlank(infraGenericEpithet)? null : infraGenericEpithet;
    }

    /**
     * Returns the species epithet string for <i>this</i> non viral taxon name if its {@link Rank rank} is
     * species aggregate or lower (bi- or trinomial). Species epithet strings
     * begin with a lower case letter.
     *
     * @return  the string containing the species epithet of <i>this</i> non viral taxon name
     * @see     #getNameCache()
     */
    @Override
    public String getSpecificEpithet(){
        return this.specificEpithet;
    }

    /**
     * @see  #getSpecificEpithet()
     */
    @Override
    public void setSpecificEpithet(String specificEpithet){
        this.specificEpithet = StringUtils.isBlank(specificEpithet) ? null : specificEpithet;
    }

    /**
     * Returns the species subdivision epithet string (infraspecific part) for
     * <i>this</i> non viral taxon name if its {@link Rank rank} is infraspecific
     * (lower than species: trinomial). Species subdivision epithet strings
     * begin with a lower case letter.
     *
     * @return  the string containing the infraspecific part of <i>this</i> non viral taxon name
     * @see     #getNameCache()
     */
    @Override
    public String getInfraSpecificEpithet(){
        return this.infraSpecificEpithet;
    }

    /**
     * @see  #getInfraSpecificEpithet()
     */
    @Override
    public void setInfraSpecificEpithet(String infraSpecificEpithet){
        this.infraSpecificEpithet = StringUtils.isBlank(infraSpecificEpithet)?null : infraSpecificEpithet;
    }

    /**
     * Returns the {@link eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor author (team)} that published <i>this</i> non viral
     * taxon name.
     *
     * @return  the nomenclatural author (team) of <i>this</i> non viral taxon name
     * @see     eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor
     * @see     eu.etaxonomy.cdm.model.agent.TeamOrPersonBase#getNomenclaturalTitle()
     */
    @Override
    public TeamOrPersonBase<?> getCombinationAuthorship(){
        return this.combinationAuthorship;
    }

    /**
     * @see  #getCombinationAuthorship()
     */
    @Override
    public void setCombinationAuthorship(TeamOrPersonBase<?> combinationAuthorship){
        this.combinationAuthorship = combinationAuthorship;
    }

    /**
     * Returns the {@link eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor author (team)} that contributed to
     * the publication of <i>this</i> non viral taxon name as generally stated by
     * the {@link #getCombinationAuthorship() combination author (team)} itself.<BR>
     * An ex-author(-team) is an author(-team) to whom a taxon name was ascribed
     * although it is not the author(-team) of a valid publication (for instance
     * without the validating description or diagnosis in case of a name for a
     * new taxon). The name of this ascribed authorship, followed by "ex", may
     * be inserted before the name(s) of the publishing author(s) of the validly
     * published name:<BR>
     * <i>Lilium tianschanicum</i> was described by Grubov (1977) as a new species and
     * its name was ascribed to Ivanova; since there is no indication that
     * Ivanova provided the validating description, the name may be cited as
     * <i>Lilium tianschanicum</i> N. A. Ivanova ex Grubov or <i>Lilium tianschanicum</i> Grubov.
     * <P>
     * The presence of an author (team) of <i>this</i> non viral taxon name is a
     * condition for the existence of an ex author (team) for <i>this</i> same name.
     *
     * @return  the nomenclatural ex author (team) of <i>this</i> non viral taxon name
     * @see     #getCombinationAuthorship()
     * @see     eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor
     * @see     eu.etaxonomy.cdm.model.agent.TeamOrPersonBase#getNomenclaturalTitle()
     */
    @Override
    public TeamOrPersonBase<?> getExCombinationAuthorship(){
        return this.exCombinationAuthorship;
    }

    /**
     * @see  #getExCombinationAuthorship()
     */
    @Override
    public void setExCombinationAuthorship(TeamOrPersonBase<?> exCombinationAuthorship){
        this.exCombinationAuthorship = exCombinationAuthorship;
    }

    /**
     * Returns the {@link eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor author (team)} that published the original combination
     * on which <i>this</i> non viral taxon name is nomenclaturally based. Such an
     * author (team) can only exist if <i>this</i> non viral taxon name is a new
     * combination due to a taxonomical revision.
     *
     * @return  the nomenclatural basionym author (team) of <i>this</i> non viral taxon name
     * @see     #getCombinationAuthorship()
     * @see     eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor
     * @see     eu.etaxonomy.cdm.model.agent.TeamOrPersonBase#getNomenclaturalTitle()
     */
    @Override
    public TeamOrPersonBase<?> getBasionymAuthorship(){
        return basionymAuthorship;
    }

    /**
     * @see  #getBasionymAuthorship()
     */
    @Override
    public void setBasionymAuthorship(TeamOrPersonBase<?> basionymAuthorship) {
        this.basionymAuthorship = basionymAuthorship;
    }

    /**
     * Returns the {@link eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor author (team)} that contributed to
     * the publication of the original combination <i>this</i> non viral taxon name is
     * based on. This should have been generally stated by
     * the {@link #getBasionymAuthorship() basionym author (team)} itself.
     * The presence of a basionym author (team) of <i>this</i> non viral taxon name is a
     * condition for the existence of an ex basionym author (team)
     * for <i>this</i> same name.
     *
     * @return  the nomenclatural ex basionym author (team) of <i>this</i> non viral taxon name
     * @see     #getBasionymAuthorship()
     * @see     #getExCombinationAuthorship()
     * @see     #getCombinationAuthorship()
     * @see     eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor
     * @see     eu.etaxonomy.cdm.model.agent.TeamOrPersonBase#getNomenclaturalTitle()
     */
    @Override
    public TeamOrPersonBase<?> getExBasionymAuthorship(){
        return exBasionymAuthorship;
    }

    /**
     * @see  #getExBasionymAuthorship()
     */
    @Override
    public void setExBasionymAuthorship(TeamOrPersonBase<?> exBasionymAuthorship) {
        this.exBasionymAuthorship = exBasionymAuthorship;
    }

    /**
     * Returns the boolean value of the flag intended to protect (true)
     * or not (false) the {@link #getAuthorshipCache() authorshipCache} (complete authorship string)
     * of <i>this</i> non viral taxon name.
     *
     * @return  the boolean value of the protectedAuthorshipCache flag
     * @see     #getAuthorshipCache()
     */
    @Override
    public boolean isProtectedAuthorshipCache() {
        return protectedAuthorshipCache;
    }

    /**
     * @see     #isProtectedAuthorshipCache()
     * @see     #getAuthorshipCache()
     */
    @Override
    public void setProtectedAuthorshipCache(boolean protectedAuthorshipCache) {
        this.protectedAuthorshipCache = protectedAuthorshipCache;
    }

    /**
     * Returns the set of all {@link HybridRelationship hybrid relationships}
     * in which <i>this</i> taxon name is involved as a {@link common.RelationshipBase#getRelatedFrom() parent}.
     *
     * @see    #getHybridRelationships()
     * @see    #getChildRelationships()
     * @see    HybridRelationshipType
     */
    @Override
    public Set<HybridRelationship> getHybridParentRelations() {
        if(hybridParentRelations == null) {
            this.hybridParentRelations = new HashSet<>();
        }
        return hybridParentRelations;
    }

    private void setHybridParentRelations(Set<HybridRelationship> hybridParentRelations) {
        this.hybridParentRelations = hybridParentRelations;
    }


    /**
     * Returns the set of all {@link HybridRelationship hybrid relationships}
     * in which <i>this</i> taxon name is involved as a {@link common.RelationshipBase#getRelatedTo() child}.
     *
     * @see    #getHybridRelationships()
     * @see    #getParentRelationships()
     * @see    HybridRelationshipType
     */
    @Override
    public Set<HybridRelationship> getHybridChildRelations() {
        if(hybridChildRelations == null) {
            this.hybridChildRelations = new HashSet<>();
        }
        return hybridChildRelations;
    }

    private void setHybridChildRelations(Set<HybridRelationship> hybridChildRelations) {
        this.hybridChildRelations = hybridChildRelations;
    }

    @Override
    public boolean isProtectedFullTitleCache() {
        return protectedFullTitleCache;
    }

    @Override
    public void setProtectedFullTitleCache(boolean protectedFullTitleCache) {
        this.protectedFullTitleCache = protectedFullTitleCache;
    }

    /**
     * Returns the boolean value of the flag indicating whether the name of <i>this</i>
     * botanical taxon name is a hybrid formula (true) or not (false). A hybrid
     * named by a hybrid formula (composed with its parent names by placing the
     * multiplication sign between them) does not have an own published name
     * and therefore has neither an {@link NonViralName#getAuthorshipCache() autorship}
     * nor other name components. If this flag is set no other hybrid flags may
     * be set.
     *
     * @return  the boolean value of the isHybridFormula flag
     * @see     #isMonomHybrid()
     * @see     #isBinomHybrid()
     * @see     #isTrinomHybrid()
     */
    @Override
    public boolean isHybridFormula(){
        return this.hybridFormula;
    }

    /**
     * @see  #isHybridFormula()
     */
    @Override
    public void setHybridFormula(boolean hybridFormula){
        this.hybridFormula = hybridFormula;
    }

    /**
     * Returns the boolean value of the flag indicating whether <i>this</i> botanical
     * taxon name is the name of an intergeneric hybrid (true) or not (false).
     * In this case the multiplication sign is placed before the scientific
     * name. If this flag is set no other hybrid flags may be set.
     *
     * @return  the boolean value of the isMonomHybrid flag
     * @see     #isHybridFormula()
     * @see     #isBinomHybrid()
     * @see     #isTrinomHybrid()
     */
    @Override
    public boolean isMonomHybrid(){
        return this.monomHybrid;
    }

    /**
     * @see  #isMonomHybrid()
     * @see  #isBinomHybrid()
     * @see  #isTrinomHybrid()
     */
    @Override
    public void setMonomHybrid(boolean monomHybrid){
        this.monomHybrid = monomHybrid;
    }

    /**
     * Returns the boolean value of the flag indicating whether <i>this</i> botanical
     * taxon name is the name of an interspecific hybrid (true) or not (false).
     * In this case the multiplication sign is placed before the species
     * epithet. If this flag is set no other hybrid flags may be set.
     *
     * @return  the boolean value of the isBinomHybrid flag
     * @see     #isHybridFormula()
     * @see     #isMonomHybrid()
     * @see     #isTrinomHybrid()
     */
    @Override
    public boolean isBinomHybrid(){
        return this.binomHybrid;
    }

    /**
     * @see  #isBinomHybrid()
     * @see  #isMonomHybrid()
     * @see  #isTrinomHybrid()
     */
    @Override
    public void setBinomHybrid(boolean binomHybrid){
        this.binomHybrid = binomHybrid;
    }

    /**
     * Returns the boolean value of the flag indicating whether <i>this</i> botanical
     * taxon name is the name of an infraspecific hybrid (true) or not (false).
     * In this case the term "notho-" (optionally abbreviated "n-") is used as
     * a prefix to the term denoting the infraspecific rank of <i>this</i> botanical
     * taxon name. If this flag is set no other hybrid flags may be set.
     *
     * @return  the boolean value of the isTrinomHybrid flag
     * @see     #isHybridFormula()
     * @see     #isMonomHybrid()
     * @see     #isBinomHybrid()
     */
    @Override
    public boolean isTrinomHybrid(){
        return this.trinomHybrid;
    }

    /**
     * @see  #isTrinomHybrid()
     * @see  #isBinomHybrid()
     * @see  #isMonomHybrid()
     */
    @Override
    public void setTrinomHybrid(boolean trinomHybrid){
        this.trinomHybrid = trinomHybrid;
    }

    // ****************** VIRAL NAME ******************/

    /**
     * Returns the accepted acronym (an assigned abbreviation) string for <i>this</i>
     * viral taxon name. For instance PCV stays for Peanut Clump Virus.
     *
     * @return  the string containing the accepted acronym of <i>this</i> viral taxon name
     */
    @Override
    public String getAcronym(){
        return this.acronym;
    }

    /**
     * @see  #getAcronym()
     */
    @Override
    public void setAcronym(String acronym){
        this.acronym = StringUtils.isBlank(acronym)? null : acronym;
    }

    // ****************** BACTERIAL NAME ******************/

    /**
     * Returns the string containing the authorship with the year and details
     * of the reference in which the subgenus included in the scientific name
     * of <i>this</i> bacterial taxon name was published.
     * For instance if the bacterial taxon name is
     * 'Bacillus (subgen. Aerobacillus Donker 1926, 128) polymyxa' the subgenus
     * authorship string is 'Donker 1926, 128'.
     *
     * @return  the string containing the complete subgenus' authorship
     *          included in <i>this</i> bacterial taxon name
     */
    @Override
    public String getSubGenusAuthorship(){
        return this.subGenusAuthorship;
    }

    /**
     * @see  #getSubGenusAuthorship()
     */
    @Override
    public void setSubGenusAuthorship(String subGenusAuthorship){
        this.subGenusAuthorship = subGenusAuthorship;
    }

    /**
     * Returns the string representing the reason for the approbation of <i>this</i>
     * bacterial taxon name. Bacterial taxon names are valid or approved
     * according to:
     * <ul>
     * <li>the approved list, c.f.r. IJSB 1980 (AL)
     * <li>the validation list, in IJSB after 1980 (VL)
     * </ul>
     * or
     * <ul>
     * <li>are validly published as paper in IJSB after 1980 (VP).
     * </ul>
     * IJSB is the acronym for International Journal of Systematic Bacteriology.
     *
     * @return  the string with the source of the approbation for <i>this</i> bacterial taxon name
     */
    @Override
    public String getNameApprobation(){
        return this.nameApprobation;
    }

    /**
     * @see  #getNameApprobation()
     */
    @Override
    public void setNameApprobation(String nameApprobation){
        this.nameApprobation = nameApprobation;
    }

// **************** ADDER / REMOVE *************************/

    /**
     * Adds the given {@link HybridRelationship hybrid relationship} to the set
     * of {@link #getHybridRelationships() hybrid relationships} of both non-viral names
     * involved in this hybrid relationship. One of both non-viral names
     * must be <i>this</i> non-viral name otherwise no addition will be carried
     * out. The {@link eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedTo() child
     * non viral taxon name} must be a hybrid, which means that one of its four hybrid flags must be set.
     *
     * @param relationship  the hybrid relationship to be added
     * @see                 #isHybridFormula()
     * @see                 #isMonomHybrid()
     * @see                 #isBinomHybrid()
     * @see                 #isTrinomHybrid()
     * @see                 #getHybridRelationships()
     * @see                 #getParentRelationships()
     * @see                 #getChildRelationships()
     * @see                 #addRelationship(RelationshipBase)
     * @throws              IllegalArgumentException
     */
    protected void addHybridRelationship(HybridRelationship rel) {
        if (rel!=null && rel.getHybridName().equals(this)){
            this.hybridChildRelations.add(rel);
        }else if(rel!=null && rel.getParentName().equals(this)){
            this.hybridParentRelations.add(rel);
        }else{
            throw new IllegalArgumentException("Hybrid relationship is either null or the relationship does not reference this name");
        }
    }


    /**
     * Removes one {@link HybridRelationship hybrid relationship} from the set of
     * {@link #getHybridRelationships() hybrid relationships} in which <i>this</i> botanical taxon name
     * is involved. The hybrid relationship will also be removed from the set
     * belonging to the second botanical taxon name involved.
     *
     * @param  relationship  the hybrid relationship which should be deleted from the corresponding sets
     * @see                  #getHybridRelationships()
     */
    @Override
    public void removeHybridRelationship(HybridRelationship hybridRelation) {
        if (hybridRelation == null) {
            return;
        }

        TaxonNameBase<?,?> parent = hybridRelation.getParentName();
        TaxonNameBase<?,?> child = hybridRelation.getHybridName();
        if (this.equals(parent)){
            this.hybridParentRelations.remove(hybridRelation);
            child.hybridChildRelations.remove(hybridRelation);
            hybridRelation.setHybridName(null);
            hybridRelation.setParentName(null);
        }
        if (this.equals(child)){
            parent.hybridParentRelations.remove(hybridRelation);
            this.hybridChildRelations.remove(hybridRelation);
            hybridRelation.setHybridName(null);
            hybridRelation.setParentName(null);
        }
    }

//********* METHODS **************************************/

    @Override
    public String generateFullTitle(){
        if (cacheStrategy == null){
            logger.warn("No CacheStrategy defined for taxon name: " + this.getUuid());
            return null;
        }else{
            return cacheStrategy.getFullTitleCache(this);
        }
    }


    @Override
    public void setFullTitleCache(String fullTitleCache){
        setFullTitleCache(fullTitleCache, PROTECTED);
    }

    @Override
    public void setFullTitleCache(String fullTitleCache, boolean protectCache){
        fullTitleCache = getTruncatedCache(fullTitleCache);
        this.fullTitleCache = fullTitleCache;
        this.setProtectedFullTitleCache(protectCache);
    }

    /**
      * Needs to be implemented by those classes that handle autonyms (e.g. botanical names).
      **/
    @Override
    @Transient
    public boolean isAutonym(){
        return false;
    }


    /**
     * Returns the boolean value "false" since the components of <i>this</i> taxon name
     * cannot follow the rules of a corresponding {@link NomenclaturalCode nomenclatural code}
     * which is not defined for this class. The nomenclature code depends on
     * the concrete name subclass ({@link BacterialName BacterialName},
     * {@link BotanicalName BotanicalName}, {@link CultivarPlantName CultivarPlantName} or
     * {@link ZoologicalName ZoologicalName} to which <i>this</i> non taxon name.
     *
     * @return  false
     */
    @Override
    @Transient
    public boolean isCodeCompliant() {
        //FIXME
        logger.warn("is CodeCompliant not implemented for TaxonNameBase");
        return false;
    }

    @Override
    @Transient
    public List<TaggedText> getTaggedName(){
        return getCacheStrategy().getTaggedTitle(this);
    }

    @Override
    @Transient
    public String getFullTitleCache(){
        if (protectedFullTitleCache){
            return this.fullTitleCache;
        }
        updateAuthorshipCache();
        if (fullTitleCache == null ){
            this.fullTitleCache = getTruncatedCache(generateFullTitle());
        }
        return fullTitleCache;
    }


    @Override
    public String getTitleCache(){
        if(!protectedTitleCache) {
            updateAuthorshipCache();
        }
        return super.getTitleCache();
    }

    @Override
    public void setTitleCache(String titleCache, boolean protectCache){
        super.setTitleCache(titleCache, protectCache);
    }

    /**
     * Returns the concatenated and formated authorteams string including
     * basionym and combination authors of <i>this</i> non viral taxon name.
     * If the protectedAuthorshipCache flag is set this method returns the
     * string stored in the the authorshipCache attribute, otherwise it
     * generates the complete authorship string, returns it and stores it in
     * the authorshipCache attribute.
     *
     * @return  the string with the concatenated and formated authorteams for <i>this</i> non viral taxon name
     * @see     #generateAuthorship()
     */
    @Override
    @Transient
    public String getAuthorshipCache() {
        if (protectedAuthorshipCache){
            return this.authorshipCache;
        }
        if (this.authorshipCache == null ){
            this.authorshipCache = generateAuthorship();
        }else{
            //TODO get isDirty of authors, make better if possible
            this.setAuthorshipCache(generateAuthorship(), protectedAuthorshipCache); //throw change event to inform higher caches

        }
        return authorshipCache;
    }



    /**
     * Updates the authorship cache if any changes appeared in the authors nomenclatural caches.
     * Deletes the titleCache and the fullTitleCache if not protected and if any change has happened
     * @return
     */
    private void updateAuthorshipCache() {
        //updates the authorship cache if necessary and via the listener updates all higher caches
        if (protectedAuthorshipCache == false){
            String oldCache = this.authorshipCache;
            String newCache = this.getAuthorshipCache();
            if ( (oldCache == null && newCache != null)  ||  CdmUtils.nullSafeEqual(oldCache,newCache)){
                this.setAuthorshipCache(this.getAuthorshipCache(), false);
            }
        }
    }


    /**
     * Assigns an authorshipCache string to <i>this</i> non viral taxon name. Sets the isProtectedAuthorshipCache
     * flag to <code>true</code>.
     *
     * @param  authorshipCache  the string which identifies the complete authorship of <i>this</i> non viral taxon name
     * @see    #getAuthorshipCache()
     */
    @Override
    public void setAuthorshipCache(String authorshipCache) {
        setAuthorshipCache(authorshipCache, true);
    }


    /**
     * Assigns an authorshipCache string to <i>this</i> non viral taxon name.
     *
     * @param  authorshipCache  the string which identifies the complete authorship of <i>this</i> non viral taxon name
     * @param  protectedAuthorshipCache if true the isProtectedAuthorshipCache flag is set to <code>true</code>, otherwise
     * the flag is set to <code>false</code>.
     * @see    #getAuthorshipCache()
     */
    @Override
    public void setAuthorshipCache(String authorshipCache, boolean protectedAuthorshipCache) {
        this.authorshipCache = authorshipCache;
        this.setProtectedAuthorshipCache(protectedAuthorshipCache);
    }

    /**
     * Generates and returns a concatenated and formated authorteams string
     * including basionym and combination authors of <i>this</i> non viral taxon name
     * according to the strategy defined in
     * {@link eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy#getAuthorshipCache(TaxonNameBase) INonViralNameCacheStrategy}.
     *
     * @return  the string with the concatenated and formatted author teams for <i>this</i> taxon name
     * @see     eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy#getAuthorshipCache(TaxonNameBase)
     */
    @Override
    public String generateAuthorship(){
        if (cacheStrategy == null){
            logger.warn("No CacheStrategy defined for taxon name: " + this.getUuid());
            return null;
        }else{
            return ((INonViralNameCacheStrategy)cacheStrategy).getAuthorshipCache(this);
        }
    }



    /**
     * Tests if the given name has any authors.
     * @return false if no author ((ex)combination or (ex)basionym) exists, true otherwise
     */
    @Override
    public boolean hasAuthors() {
        return (this.getCombinationAuthorship() != null ||
                this.getExCombinationAuthorship() != null ||
                this.getBasionymAuthorship() != null ||
                this.getExBasionymAuthorship() != null);
    }

    /**
     * Shortcut. Returns the combination authors title cache. Returns null if no combination author exists.
     * @return
     */
    @Override
    public String computeCombinationAuthorNomenclaturalTitle() {
        return computeNomenclaturalTitle(this.getCombinationAuthorship());
    }

    /**
     * Shortcut. Returns the basionym authors title cache. Returns null if no basionym author exists.
     * @return
     */
    @Override
    public String computeBasionymAuthorNomenclaturalTitle() {
        return computeNomenclaturalTitle(this.getBasionymAuthorship());
    }


    /**
     * Shortcut. Returns the ex-combination authors title cache. Returns null if no ex-combination author exists.
     * @return
     */
    @Override
    public String computeExCombinationAuthorNomenclaturalTitle() {
        return computeNomenclaturalTitle(this.getExCombinationAuthorship());
    }

    /**
     * Shortcut. Returns the ex-basionym authors title cache. Returns null if no exbasionym author exists.
     * @return
     */
    @Override
    public String computeExBasionymAuthorNomenclaturalTitle() {
        return computeNomenclaturalTitle(this.getExBasionymAuthorship());
    }

    private String computeNomenclaturalTitle(INomenclaturalAuthor author){
        if (author == null){
            return null;
        }else{
            return author.getNomenclaturalTitle();
        }
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
        if (rel != null ){
            if (rel.getToName().equals(this)){
                this.relationsToThisName.add(rel);
            }else if(rel.getFromName().equals(this)){
                this.relationsFromThisName.add(rel);
            }
            NameRelationshipType type = rel.getType();
            if (type != null && ( type.isBasionymRelation() || type.isReplacedSynonymRelation() ) ){
                rel.getFromName().mergeHomotypicGroups(rel.getToName());
            }
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
    @Override
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

    @Override
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
     * If relation is of type NameRelationship, addNameRelationship is called;
     * if relation is of type HybridRelationship addHybridRelationship is called,
     * otherwise an IllegalArgumentException is thrown.
     *
     * @param relation  the relationship to be added to one of <i>this</i> taxon name's name relationships sets
     * @see    	   		#addNameRelationship(NameRelationship)
     * @see    	   		#getNameRelations()
     * @see    	   		NameRelationship
     * @see    	   		RelationshipBase
     * @see             #addHybridRelationship(HybridRelationship)

     * @deprecated to be used by RelationshipBase only
     */
    @Deprecated
    @Override
    public void addRelationship(RelationshipBase relation) {
        if (relation instanceof NameRelationship){
            addNameRelationship((NameRelationship)relation);

        }else if (relation instanceof HybridRelationship){
            addHybridRelationship((HybridRelationship)relation);
        }else{
            logger.warn("Relationship not of type NameRelationship!");
            throw new IllegalArgumentException("Relationship not of type NameRelationship or HybridRelationship");
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
    @Override
    public Set<NameRelationship> getRelationsFromThisName() {
        if(relationsFromThisName == null) {
            this.relationsFromThisName = new HashSet<>();
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
    @Override
    public Set<NameRelationship> getRelationsToThisName() {
        if(relationsToThisName == null) {
            this.relationsToThisName = new HashSet<>();
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
    @Override
    public Set<NomenclaturalStatus> getStatus() {
        if(status == null) {
            this.status = new HashSet<>();
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
    @Override
    public void addStatus(NomenclaturalStatus nomStatus) {
        this.status.add(nomStatus);
    }
    @Override
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
    @Override
    public void removeStatus(NomenclaturalStatus nomStatus) {
        //TODO to be implemented?
        logger.warn("not yet fully implemented?");
        this.status.remove(nomStatus);
    }


    /**
     * Generates the composed name string of <i>this</i> non viral taxon name without author
     * strings or year according to the strategy defined in
     * {@link eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy INonViralNameCacheStrategy}.
     * The result might be stored in {@link #getNameCache() nameCache} if the
     * flag {@link #isProtectedNameCache() protectedNameCache} is not set.
     *
     * @return  the string with the composed name of <i>this</i> non viral taxon name without authors or year
     * @see     #getNameCache()
     */
    protected String generateNameCache(){
        if (cacheStrategy == null){
            logger.warn("No CacheStrategy defined for taxon name: " + this.toString());
            return null;
        }else{
            return cacheStrategy.getNameCache(this);
        }
    }

    /**
     * Returns or generates the nameCache (scientific name
     * without author strings and year) string for <i>this</i> non viral taxon name. If the
     * {@link #isProtectedNameCache() protectedNameCache} flag is not set (False)
     * the string will be generated according to a defined strategy,
     * otherwise the value of the actual nameCache string will be returned.
     *
     * @return  the string which identifies <i>this</i> non viral taxon name (without authors or year)
     * @see     #generateNameCache()
     */
    @Override
    @Transient
    public String getNameCache() {
        if (protectedNameCache){
            return this.nameCache;
        }
        // is title dirty, i.e. equal NULL?
        if (nameCache == null){
            this.nameCache = generateNameCache();
        }
        return nameCache;
    }

    /**
     * Assigns a nameCache string to <i>this</i> non viral taxon name and protects it from being overwritten.
     * Sets the protectedNameCache flag to <code>true</code>.
     *
     * @param  nameCache  the string which identifies <i>this</i> non viral taxon name (without authors or year)
     * @see    #getNameCache()
     */
    @Override
    public void setNameCache(String nameCache){
        setNameCache(nameCache, true);
    }

    /**
     * Assigns a nameCache string to <i>this</i> non viral taxon name and protects it from being overwritten.
     * Sets the protectedNameCache flag to <code>true</code>.
     *
     * @param  nameCache  the string which identifies <i>this</i> non viral taxon name (without authors or year)
     * @param  protectedNameCache if true teh protectedNameCache is set to <code>true</code> or otherwise set to
     * <code>false</code>
     * @see    #getNameCache()
     */
    @Override
    public void setNameCache(String nameCache, boolean protectedNameCache){
        this.nameCache = nameCache;
        this.setProtectedNameCache(protectedNameCache);
    }


    /**
     * Indicates whether <i>this</i> taxon name is a {@link NameRelationshipType#BASIONYM() basionym}
     * or a {@link NameRelationshipType#REPLACED_SYNONYM() replaced synonym}
     * of any other taxon name. Returns "true", if a basionym or a replaced
     * synonym {@link NameRelationship relationship} from <i>this</i> taxon name to another taxon name exists,
     * false otherwise (also in case <i>this</i> taxon name is the only one in the
     * homotypical group).
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public void addBasionym(TaxonNameBase basionym){
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
    @Override
    public NameRelationship addBasionym(TaxonNameBase basionym, Reference citation, String microcitation, String ruleConsidered){
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
    @Override
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
    @Override
    public void addReplacedSynonym(TaxonNameBase replacedSynonym, Reference citation, String microcitation, String ruleConsidered){
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
    @Override
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
    @Override
    public Rank getRank(){
        return this.rank;
    }

    /**
     * @see  #getRank()
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
    public String getAppendedPhrase(){
        return this.appendedPhrase;
    }

    /**
     * @see  #getAppendedPhrase()
     */
    @Override
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
    @Override
    public String getNomenclaturalMicroReference(){
        return this.nomenclaturalMicroReference;
    }
    /**
     * @see  #getNomenclaturalMicroReference()
     */
    @Override
    public void setNomenclaturalMicroReference(String nomenclaturalMicroReference){
        this.nomenclaturalMicroReference = StringUtils.isBlank(nomenclaturalMicroReference)? null : nomenclaturalMicroReference;
    }

    @Override
    public int getParsingProblem(){
        return this.parsingProblem;
    }

    @Override
    public void setParsingProblem(int parsingProblem){
        this.parsingProblem = parsingProblem;
    }

    @Override
    public void addParsingProblem(ParserProblem problem){
        parsingProblem = ParserProblem.addProblem(parsingProblem, problem);
    }

    @Override
    public void removeParsingProblem(ParserProblem problem) {
        parsingProblem = ParserProblem.removeProblem(parsingProblem, problem);
    }

    /**
     * @param warnings
     */
    @Override
    public void addParsingProblems(int problems){
        parsingProblem = ParserProblem.addProblems(parsingProblem, problems);
    }

    @Override
    public boolean hasProblem(){
        return parsingProblem != 0;
    }

    @Override
    public boolean hasProblem(ParserProblem problem) {
        return getParsingProblems().contains(problem);
    }

    @Override
    public int getProblemStarts(){
        return this.problemStarts;
    }

    @Override
    public void setProblemStarts(int start) {
        this.problemStarts = start;
    }

    @Override
    public int getProblemEnds(){
        return this.problemEnds;
    }

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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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

    @Override
    public HomotypicalGroup getHomotypicalGroup() {
        if (homotypicalGroup == null){
            homotypicalGroup = new HomotypicalGroup();
            homotypicalGroup.typifiedNames.add(this);
        }
    	return homotypicalGroup;
    }

    /**
     * @see #getHomotypicalGroup()
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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

// ************* RELATIONSHIPS *****************************/


    /**
     * Returns the hybrid child relationships ordered by relationship type, or if equal
     * by title cache of the related names.
     * @see #getHybridParentRelations()
     */
    @Override
    @Transient
    public List<HybridRelationship> getOrderedChildRelationships(){
        List<HybridRelationship> result = new ArrayList<HybridRelationship>();
        result.addAll(this.hybridChildRelations);
        Collections.sort(result);
        Collections.reverse(result);
        return result;

    }


    /**
     * Creates a new {@link HybridRelationship#HybridRelationship(BotanicalName, BotanicalName, HybridRelationshipType, String) hybrid relationship}
     * to <i>this</i> botanical name. A HybridRelationship may be of type
     * "is first/second parent" or "is male/female parent". By invoking this
     * method <i>this</i> botanical name becomes a hybrid child of the parent
     * botanical name.
     *
     * @param parentName      the botanical name of the parent for this new hybrid name relationship
     * @param type            the type of this new name relationship
     * @param ruleConsidered  the string which specifies the rule on which this name relationship is based
     * @return
     * @see                   #addHybridChild(BotanicalName, HybridRelationshipType,String )
     * @see                   #getRelationsToThisName()
     * @see                   #getNameRelations()
     * @see                   #addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
     * @see                   #addNameRelationship(NameRelationship)
     */
    @Override
    public HybridRelationship addHybridParent(NonViralName parentName, HybridRelationshipType type, String ruleConsidered){
        return new HybridRelationship(this, parentName, type, ruleConsidered);
    }

    /**
     * Creates a new {@link HybridRelationship#HybridRelationship(BotanicalName, BotanicalName, HybridRelationshipType, String) hybrid relationship}
     * to <i>this</i> botanical name. A HybridRelationship may be of type
     * "is first/second parent" or "is male/female parent". By invoking this
     * method <i>this</i> botanical name becomes a parent of the hybrid child
     * botanical name.
     *
     * @param childName       the botanical name of the child for this new hybrid name relationship
     * @param type            the type of this new name relationship
     * @param ruleConsidered  the string which specifies the rule on which this name relationship is based
     * @return
     * @see                   #addHybridParent(BotanicalName, HybridRelationshipType,String )
     * @see                   #getRelationsToThisName()
     * @see                   #getNameRelations()
     * @see                   #addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
     * @see                   #addNameRelationship(NameRelationship)
     */
    @Override
    public HybridRelationship addHybridChild(NonViralName childName, HybridRelationshipType type, String ruleConsidered){
        return new HybridRelationship(childName, this, type, ruleConsidered);
    }

    @Override
    public void removeHybridChild(NonViralName child) {
        Set<HybridRelationship> hybridRelationships = new HashSet<HybridRelationship>();
        hybridRelationships.addAll(this.getHybridChildRelations());
        hybridRelationships.addAll(this.getHybridParentRelations());
        for(HybridRelationship hybridRelationship : hybridRelationships) {
            // remove name relationship from this side
            if (hybridRelationship.getParentName().equals(this) && hybridRelationship.getHybridName().equals(child)) {
                this.removeHybridRelationship(hybridRelationship);
            }
        }
    }

    @Override
    public void removeHybridParent(NonViralName parent) {
        Set<HybridRelationship> hybridRelationships = new HashSet<HybridRelationship>();
        hybridRelationships.addAll(this.getHybridChildRelations());
        hybridRelationships.addAll(this.getHybridParentRelations());
        for(HybridRelationship hybridRelationship : hybridRelationships) {
            // remove name relationship from this side
            if (hybridRelationship.getParentName().equals(parent) && hybridRelationship.getHybridName().equals(this)) {
                this.removeHybridRelationship(hybridRelationship);
            }
        }
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
    @Override
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
    @Override
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
    @Override
    public void removeDescription(TaxonNameDescription description) {
        java.lang.reflect.Field field = ReflectionUtils.findField(TaxonNameDescription.class, "taxonName", TaxonNameBase.class);
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, description, null);
        descriptions.remove(description);
    }

// *********** HOMOTYPIC GROUP METHODS **************************************************

    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
     * subclasses {@link BacterialName BacterialName},
     * {@link BotanicalName BotanicalName}, {@link CultivarPlantName CultivarPlantName} and
     * {@link ZoologicalName ZoologicalName}. Each taxon name is governed by one
     * and only one nomenclatural code.
     *
     * @return  null
     * @see  	#isCodeCompliant()
     * @see  	#getHasProblem()
     */
    @Override
    public NomenclaturalCode getNomenclaturalCode() {
        logger.warn("TaxonNameBase has no specific Code defined. Use subclasses");
        return null;
    }


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
    @Override
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
    @Override
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


    /**
     * Defines the last part of the name.
     * This is for infraspecific taxa, the infraspecific epithet,
     * for species the specific epithet, for infageneric taxa the infrageneric epithet
     * else the genusOrUninomial.
     * However, the result does not depend on the rank (which may be not correctly set
     * in case of dirty data) but returns the first name part which is not blank
     * considering the above order.
     * @return the first not blank name part in reverse order
     */
    @Override
    public String getLastNamePart() {
        String result =
                StringUtils.isNotBlank(this.getInfraSpecificEpithet())?
                    this.getInfraSpecificEpithet() :
                StringUtils.isNotBlank(this.getSpecificEpithet()) ?
                    this.getSpecificEpithet():
                StringUtils.isNotBlank(this.getInfraGenericEpithet()) ?
                    this.getInfraGenericEpithet():
                this.getGenusOrUninomial();
        return result;
    }

// ********************** INTERFACES ********************************************/

    public static TaxonNameBase castAndDeproxy(ITaxonNameBase tnb){
        return deproxy(tnb, TaxonNameBase.class);
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
        TaxonNameBase<?,?> result;
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


            //HybridChildRelations
            result.hybridChildRelations = new HashSet<HybridRelationship>();
            for (HybridRelationship hybridRelationship : getHybridChildRelations()){
                HybridRelationship newChildRelationship = (HybridRelationship)hybridRelationship.clone();
                newChildRelationship.setRelatedTo(result);
                result.hybridChildRelations.add(newChildRelationship);
            }

            //HybridParentRelations
            result.hybridParentRelations = new HashSet<HybridRelationship>();
            for (HybridRelationship hybridRelationship : getHybridParentRelations()){
                HybridRelationship newParentRelationship = (HybridRelationship)hybridRelationship.clone();
                newParentRelationship.setRelatedFrom(result);
                result.hybridParentRelations.add(newParentRelationship);
            }

            //empty caches
            if (! protectedNameCache){
                result.nameCache = null;
            }

            //empty caches
            if (! protectedAuthorshipCache){
                result.authorshipCache = null;
            }

            //no changes to: appendedPharse, nomenclaturalReference,
            //nomenclaturalMicroReference, parsingProblem, problemEnds, problemStarts
            //protectedFullTitleCache, rank
            //basionamyAuthorship, combinationAuthorship, exBasionymAuthorship, exCombinationAuthorship
            //genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet,
            //protectedAuthorshipCache, protectedNameCache,
            //binomHybrid, monomHybrid, trinomHybrid, hybridFormula,
            //acronym
            //subGenusAuthorship, nameApprobation
            return result;
        } catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }

    }
}
