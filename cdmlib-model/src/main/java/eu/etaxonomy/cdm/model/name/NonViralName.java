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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.name.CacheUpdate;
import eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.Match;
import eu.etaxonomy.cdm.strategy.match.Match.ReplaceMode;
import eu.etaxonomy.cdm.strategy.match.MatchMode;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;
import eu.etaxonomy.cdm.validation.Level2;
import eu.etaxonomy.cdm.validation.Level3;
import eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank;
import eu.etaxonomy.cdm.validation.annotation.NameMustHaveAuthority;
import eu.etaxonomy.cdm.validation.annotation.NoDuplicateNames;
import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;

/**
 * The taxon name class for all non viral taxa. Parenthetical authorship is derived
 * from basionym relationship. The scientific name including author strings and
 * maybe year can be stored as a string in the inherited {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache() titleCache} attribute.
 * The year itself is an information obtained from the {@link eu.etaxonomy.cdm.model.reference.Reference#getYear() nomenclatural reference}.
 * The scientific name string without author strings and year can be stored in the {@link #getNameCache() nameCache} attribute.
 * <P>
 * This class corresponds partially to: <ul>
 * <li> TaxonName according to the TDWG ontology
 * <li> ScientificName and CanonicalName according to the TCS
 * <li> ScientificName according to the ABCD schema
 * </ul>
 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:39
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NonViralName", propOrder = {
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
    "trinomHybrid"
})
@XmlRootElement(name = "NonViralName")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.name.TaxonNameBase")
@Audited
@Configurable
@CorrectEpithetsForRank(groups = Level2.class)
@NameMustHaveAuthority(groups = Level2.class)
@NoDuplicateNames(groups = Level3.class)
public class NonViralName<T extends NonViralName> extends TaxonNameBase<T, INonViralNameCacheStrategy> implements Cloneable{
    private static final long serialVersionUID = 4441110073881088033L;
    private static final Logger logger = Logger.getLogger(NonViralName.class);

    @XmlElement(name = "NameCache")
    @Fields({
        @Field(name = "nameCache_tokenized"),
        @Field(store = Store.YES, index = Index.YES, analyze = Analyze.YES)
    })
    @Analyzer(impl = org.apache.lucene.analysis.core.KeywordAnalyzer.class)
    @Match(value=MatchMode.CACHE, cacheReplaceMode=ReplaceMode.DEFINED,
            cacheReplacedProperties={"genusOrUninomial", "infraGenericEpithet", "specificEpithet", "infraSpecificEpithet"} )
    @NotEmpty(groups = Level2.class) // implicitly NotNull
    @Size(max = 255)
    private String nameCache;

    @XmlElement(name = "ProtectedNameCache")
    @CacheUpdate(value="nameCache")
    protected boolean protectedNameCache;

    @XmlElement(name = "GenusOrUninomial")
    @Field(analyze = Analyze.YES,indexNullAs=Field.DEFAULT_NULL_TOKEN)
    @Match(MatchMode.EQUAL_REQUIRED)
    @CacheUpdate("nameCache")
    @Size(max = 255)
    @Pattern(regexp = "[A-Z][a-z\\u00E4\\u00EB\\u00EF\\u00F6\\u00FC\\-]+", groups=Level2.class, message="{eu.etaxonomy.cdm.model.name.NonViralName.allowedCharactersForUninomial.message}")
    @NullOrNotEmpty
    @NotNull(groups = Level2.class)
    private String genusOrUninomial;

    @XmlElement(name = "InfraGenericEpithet")
    @Field(analyze = Analyze.YES,indexNullAs=Field.DEFAULT_NULL_TOKEN)
    @CacheUpdate("nameCache")
    //TODO Val #3379
//    @NullOrNotEmpty
    @Size(max = 255)
    @Pattern(regexp = "[a-z\\u00E4\\u00EB\\u00EF\\u00F6\\u00FC\\-]+", groups=Level2.class,message="{eu.etaxonomy.cdm.model.name.NonViralName.allowedCharactersForEpithet.message}")
    private String infraGenericEpithet;

    @XmlElement(name = "SpecificEpithet")
    @Field(analyze = Analyze.YES,indexNullAs=Field.DEFAULT_NULL_TOKEN)
    @CacheUpdate("nameCache")
    //TODO Val #3379
//    @NullOrNotEmpty
    @Size(max = 255)
    @Pattern(regexp = "[a-z\\u00E4\\u00EB\\u00EF\\u00F6\\u00FC\\-]+", groups=Level2.class, message = "{eu.etaxonomy.cdm.model.name.NonViralName.allowedCharactersForEpithet.message}")
    private String specificEpithet;

    @XmlElement(name = "InfraSpecificEpithet")
    @Field(analyze = Analyze.YES,indexNullAs=Field.DEFAULT_NULL_TOKEN)
    @CacheUpdate("nameCache")
    //TODO Val #3379
//    @NullOrNotEmpty
    @Size(max = 255)
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
    @Size(max = 255)
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
    private Set<HybridRelationship> hybridParentRelations = new HashSet<HybridRelationship>();

    @XmlElementWrapper(name = "HybridRelationsToThisName")
    @XmlElement(name = "HybridRelationsToThisName")
    @OneToMany(mappedBy="relatedTo", fetch = FetchType.LAZY, orphanRemoval=true) //a hybrid relation can be deleted automatically if the child is deleted.
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @Merge(MergeMode.RELATION)
    @NotNull
    private Set<HybridRelationship> hybridChildRelations = new HashSet<HybridRelationship>();

    //if set: this name is a hybrid formula (a hybrid that does not have an own name) and no other hybrid flags may be set. A
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

// ************************** CONSTRUCTORS *************/

    //needed by hibernate
    /**
     * Class constructor: creates a new non viral taxon name instance
     * only containing the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
     *
     * @see #NonViralName(Rank, HomotypicalGroup)
     * @see #NonViralName(Rank, String, String, String, String, TeamOrPersonBase, Reference, String, HomotypicalGroup)
     * @see eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
     * @see eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
     * @see eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
     */
    protected NonViralName(){
        super();
        setNameCacheStrategy();
    }

    /**
     * Class constructor: creates a new non viral taxon name instance
     * only containing its {@link Rank rank},
     * its {@link HomotypicalGroup homotypical group} and
     * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
     * The new non viral taxon name instance will be also added to the set of
     * non viral taxon names belonging to this homotypical group.
     *
     * @param	rank  the rank to be assigned to <i>this</i> non viral taxon name
     * @param	homotypicalGroup  the homotypical group to which <i>this</i> non viral taxon name belongs
     * @see 	#NonViralName()
     * @see		#NonViralName(Rank, String, String, String, String, TeamOrPersonBase, Reference, String, HomotypicalGroup)
     * @see		#NewInstance(Rank, HomotypicalGroup)
     * @see 	eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
     * @see 	eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
     * @see 	eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
     */
    protected NonViralName(Rank rank, HomotypicalGroup homotypicalGroup) {
        super(rank, homotypicalGroup);
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
     * @param	rank  the rank to be assigned to <i>this</i> non viral taxon name
     * @param	genusOrUninomial the string for <i>this</i> non viral taxon name
     * 			if its rank is genus or higher or for the genus part
     * 			if its rank is lower than genus
     * @param	infraGenericEpithet  the string for the first epithet of
     * 			<i>this</i> non viral taxon name if its rank is lower than genus
     * 			and higher than species aggregate
     * @param	specificEpithet  the string for the first epithet of
     * 			<i>this</i> non viral taxon name if its rank is species aggregate or lower
     * @param	infraSpecificEpithet  the string for the second epithet of
     * 			<i>this</i> non viral taxon name if its rank is lower than species
     * @param	combinationAuthorship  the author or the team who published <i>this</i> non viral taxon name
     * @param	nomenclaturalReference  the nomenclatural reference where <i>this</i> non viral taxon name was published
     * @param	nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
     * @param	homotypicalGroup  the homotypical group to which <i>this</i> non viral taxon name belongs
     * @see 	#NonViralName()
     * @see		#NonViralName(Rank, HomotypicalGroup)
     * @see		#NewInstance(Rank, HomotypicalGroup)
     * @see 	eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
     * @see 	eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
     * @see 	eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
     */
    protected NonViralName(Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorship, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
        super(rank, homotypicalGroup);
        setNameCacheStrategy();
        setGenusOrUninomial(genusOrUninomial);
        setInfraGenericEpithet (infraGenericEpithet);
        setSpecificEpithet(specificEpithet);
        setInfraSpecificEpithet(infraSpecificEpithet);
        setCombinationAuthorship(combinationAuthorship);
        setNomenclaturalReference(nomenclaturalReference);
        this.setNomenclaturalMicroReference(nomenclMicroRef);
    }



//**************************** METHODS **************************************/


    private void setNameCacheStrategy(){
        if (getClass() == NonViralName.class){
            this.cacheStrategy = NonViralNameDefaultCacheStrategy.NewInstance();
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
    @Override
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


    /**
     * Returns the {@link eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor author (team)} that published <i>this</i> non viral
     * taxon name.
     *
     * @return  the nomenclatural author (team) of <i>this</i> non viral taxon name
     * @see 	eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor
     * @see 	eu.etaxonomy.cdm.model.agent.TeamOrPersonBase#getNomenclaturalTitle()
     */
    public TeamOrPersonBase<?> getCombinationAuthorship(){
        return this.combinationAuthorship;
    }

    /**
     * @see  #getCombinationAuthorship()
     */
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
     * @see 	#getCombinationAuthorship()
     * @see 	eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor
     * @see 	eu.etaxonomy.cdm.model.agent.TeamOrPersonBase#getNomenclaturalTitle()
     */
    public TeamOrPersonBase<?> getExCombinationAuthorship(){
        return this.exCombinationAuthorship;
    }

    /**
     * @see  #getExCombinationAuthorship()
     */
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
     * @see 	#getCombinationAuthorship()
     * @see 	eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor
     * @see 	eu.etaxonomy.cdm.model.agent.TeamOrPersonBase#getNomenclaturalTitle()
     */
    public TeamOrPersonBase<?> getBasionymAuthorship(){
        return basionymAuthorship;
    }

    /**
     * @see  #getBasionymAuthorship()
     */
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
     * @see 	#getBasionymAuthorship()
     * @see 	#getExCombinationAuthorship()
     * @see 	#getCombinationAuthorship()
     * @see 	eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor
     * @see 	eu.etaxonomy.cdm.model.agent.TeamOrPersonBase#getNomenclaturalTitle()
     */
    public TeamOrPersonBase<?> getExBasionymAuthorship(){
        return exBasionymAuthorship;
    }

    /**
     * @see  #getExBasionymAuthorship()
     */
    public void setExBasionymAuthorship(TeamOrPersonBase<?> exBasionymAuthorship) {
        this.exBasionymAuthorship = exBasionymAuthorship;
    }
    /**
     * Returns either the scientific name string (without authorship) for <i>this</i>
     * non viral taxon name if its rank is genus or higher (monomial) or the string for
     * the genus part of it if its {@link Rank rank} is lower than genus (bi- or trinomial).
     * Genus or uninomial strings begin with an upper case letter.
     *
     * @return  the string containing the suprageneric name, the genus name or the genus part of <i>this</i> non viral taxon name
     * @see 	#getNameCache()
     */
    public String getGenusOrUninomial() {
        return genusOrUninomial;
    }

    /**
     * @see  #getGenusOrUninomial()
     */
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
     * @see 	#getNameCache()
     */
    public String getInfraGenericEpithet(){
        return this.infraGenericEpithet;
    }

    /**
     * @see  #getInfraGenericEpithet()
     */
    public void setInfraGenericEpithet(String infraGenericEpithet){
        this.infraGenericEpithet = StringUtils.isBlank(infraGenericEpithet)? null : infraGenericEpithet;
    }

    /**
     * Returns the species epithet string for <i>this</i> non viral taxon name if its {@link Rank rank} is
     * species aggregate or lower (bi- or trinomial). Species epithet strings
     * begin with a lower case letter.
     *
     * @return  the string containing the species epithet of <i>this</i> non viral taxon name
     * @see 	#getNameCache()
     */
    public String getSpecificEpithet(){
        return this.specificEpithet;
    }

    /**
     * @see  #getSpecificEpithet()
     */
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
     * @see 	#getNameCache()
     */
    public String getInfraSpecificEpithet(){
        return this.infraSpecificEpithet;
    }

    /**
     * @see  #getInfraSpecificEpithet()
     */
    public void setInfraSpecificEpithet(String infraSpecificEpithet){
        this.infraSpecificEpithet = StringUtils.isBlank(infraSpecificEpithet)?null : infraSpecificEpithet;
    }

    /**
     * Generates and returns the string with the scientific name of <i>this</i>
     * non viral taxon name including author strings and maybe year according to
     * the strategy defined in
     *  {@link eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy INonViralNameCacheStrategy}.
     * This string may be stored in the inherited
     * {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache() titleCache} attribute.
     * This method overrides the generic and inherited
     * TaxonNameBase#generateTitle() method.
     *
     * @return  the string with the composed name of <i>this</i> non viral taxon name with authorship (and maybe year)
     * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#generateTitle()
     * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache()
     * @see  	TaxonNameBase#generateTitle()
     */
//	@Override
//	public String generateTitle(){
//		if (cacheStrategy == null){
//			logger.warn("No CacheStrategy defined for nonViralName: " + this.getUuid());
//			return null;
//		}else{
//			return cacheStrategy.getTitleCache(this);
//		}
//	}

    @Override
    public String generateFullTitle(){
        if (cacheStrategy == null){
            logger.warn("No CacheStrategy defined for nonViralName: " + this.getUuid());
            return null;
        }else{
            return cacheStrategy.getFullTitleCache(this);
        }
    }

    /**
     * Generates the composed name string of <i>this</i> non viral taxon name without author
     * strings or year according to the strategy defined in
     * {@link eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy INonViralNameCacheStrategy}.
     * The result might be stored in {@link #getNameCache() nameCache} if the
     * flag {@link #isProtectedNameCache() protectedNameCache} is not set.
     *
     * @return  the string with the composed name of <i>this</i> non viral taxon name without authors or year
     * @see 	#getNameCache()
     */
    protected String generateNameCache(){
        if (cacheStrategy == null){
            logger.warn("No CacheStrategy defined for taxonName: " + this.toString());
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
     * @see 	#generateNameCache()
     */
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
     * @see	   #getNameCache()
     */
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
     * @see	   #getNameCache()
     */
    public void setNameCache(String nameCache, boolean protectedNameCache){
        this.nameCache = nameCache;
        this.setProtectedNameCache(protectedNameCache);
    }

    /**
     * Returns the boolean value of the flag intended to protect (true)
     * or not (false) the {@link #getNameCache() nameCache} (scientific name without author strings and year)
     * string of <i>this</i> non viral taxon name.
     *
     * @return  the boolean value of the protectedNameCache flag
     * @see     #getNameCache()
     */
    public boolean isProtectedNameCache() {
        return protectedNameCache;
    }

    /**
     * @see     #isProtectedNameCache()
     */
    public void setProtectedNameCache(boolean protectedNameCache) {
        this.protectedNameCache = protectedNameCache;
    }


    /**
     * Generates and returns a concatenated and formated authorteams string
     * including basionym and combination authors of <i>this</i> non viral taxon name
     * according to the strategy defined in
     * {@link eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy#getAuthorshipCache(NonViralName) INonViralNameCacheStrategy}.
     *
     * @return  the string with the concatenated and formated authorteams for <i>this</i> non viral taxon name
     * @see 	eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy#getAuthorshipCache(NonViralName)
     */
    public String generateAuthorship(){
        if (cacheStrategy == null){
            logger.warn("No CacheStrategy defined for nonViralName: " + this.getUuid());
            return null;
        }else{
            return ((INonViralNameCacheStrategy<T>)cacheStrategy).getAuthorshipCache((T)this);
        }
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
     * @see 	#generateAuthorship()
     */
    @Transient
    public String getAuthorshipCache() {
        if (protectedAuthorshipCache){
            return this.authorshipCache;
        }
        if (this.authorshipCache == null ){
            this.authorshipCache = generateAuthorship();
        }else{
            //TODO get is Dirty of authors, make better if possible
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
     * @see	   #getAuthorshipCache()
     */
    public void setAuthorshipCache(String authorshipCache) {
        setAuthorshipCache(authorshipCache, true);
    }

    @Override
    @Transient
    public String getFullTitleCache(){
        updateAuthorshipCache();
        return super.getFullTitleCache();
    }

    @Override
    public String getTitleCache(){
        if(!protectedTitleCache) {
            updateAuthorshipCache();
        }

        return super.getTitleCache();
    }


    /**
     * Assigns an authorshipCache string to <i>this</i> non viral taxon name.
     *
     * @param  authorshipCache  the string which identifies the complete authorship of <i>this</i> non viral taxon name
     * @param  protectedAuthorshipCache if true the isProtectedAuthorshipCache flag is set to <code>true</code>, otherwise
     * the flag is set to <code>false</code>.
     * @see	   #getAuthorshipCache()
     */
    public void setAuthorshipCache(String authorshipCache, boolean protectedAuthorshipCache) {
        this.authorshipCache = authorshipCache;
        this.setProtectedAuthorshipCache(protectedAuthorshipCache);
    }

    @Override
    public void setTitleCache(String titleCache, boolean protectCache){
        super.setTitleCache(titleCache, protectCache);
    }

    /**
     * Returns the boolean value "false" since the components of <i>this</i> taxon name
     * cannot follow the rules of a corresponding {@link NomenclaturalCode nomenclatural code}
     * which is not defined for this class. The nomenclature code depends on
     * the concrete name subclass ({@link BacterialName BacterialName},
     * {@link BotanicalName BotanicalName}, {@link CultivarPlantName CultivarPlantName} or
     * {@link ZoologicalName ZoologicalName} to which <i>this</i> non viral taxon name belongs.
     * This method overrides the isCodeCompliant method from the abstract
     * {@link TaxonNameBase#isCodeCompliant() TaxonNameBase} class.
     *
     * @return  false
     * @see	   	TaxonNameBase#isCodeCompliant()
     */
    @Override
    @Transient
    public boolean isCodeCompliant() {
        //FIXME
        logger.warn("is CodeCompliant not yet implemented");
        return false;
    }


    /**
     * Returns null as {@link NomenclaturalCode nomenclatural code} that governs
     * the construction of <i>this</i> non viral taxon name since there is no specific
     * nomenclatural code defined. The real implementention takes place in the
     * subclasses {@link BacterialName BacterialName},
     * {@link BotanicalName BotanicalName}, {@link CultivarPlantName CultivarPlantName} and
     * {@link ZoologicalName ZoologicalName}.
     * This method overrides the {@link TaxonNameBase#getNomenclaturalCode()} method from {@link TaxonNameBase TaxonNameBase}.
     *
     * @return  null
     * @see  	#isCodeCompliant()
     * @see  	TaxonNameBase#getHasProblem()
     */
    @Override
    @Transient
    public NomenclaturalCode getNomenclaturalCode() {
        logger.warn("Non Viral Name has no specific Code defined. Use subclasses");
        return null;
    }

    /**
     * Returns the boolean value of the flag intended to protect (true)
     * or not (false) the {@link #getAuthorshipCache() authorshipCache} (complete authorship string)
     * of <i>this</i> non viral taxon name.
     *
     * @return  the boolean value of the protectedAuthorshipCache flag
     * @see     #getAuthorshipCache()
     */
    public boolean isProtectedAuthorshipCache() {
        return protectedAuthorshipCache;
    }

    /**
     * @see     #isProtectedAuthorshipCache()
     * @see     #getAuthorshipCache()
     */
    public void setProtectedAuthorshipCache(boolean protectedAuthorshipCache) {
        this.protectedAuthorshipCache = protectedAuthorshipCache;
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
     * @see		#isMonomHybrid()
     * @see		#isBinomHybrid()
     * @see		#isTrinomHybrid()
     */
    public boolean isHybridFormula(){
        return this.hybridFormula;
    }

    /**
     * @see  #isHybridFormula()
     */
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
     * @see		#isHybridFormula()
     * @see		#isBinomHybrid()
     * @see		#isTrinomHybrid()
     */
    public boolean isMonomHybrid(){
        return this.monomHybrid;
    }

    /**
     * @see  #isMonomHybrid()
     * @see	 #isBinomHybrid()
     * @see	 #isTrinomHybrid()
     */
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
     * @see		#isHybridFormula()
     * @see		#isMonomHybrid()
     * @see		#isTrinomHybrid()
     */
    public boolean isBinomHybrid(){
        return this.binomHybrid;
    }

    /**
     * @see	 #isBinomHybrid()
     * @see  #isMonomHybrid()
     * @see	 #isTrinomHybrid()
     */
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
     * @see		#isHybridFormula()
     * @see		#isMonomHybrid()
     * @see		#isBinomHybrid()
     */
    public boolean isTrinomHybrid(){
        return this.trinomHybrid;
    }

    /**
     * @see	 #isTrinomHybrid()
     * @see	 #isBinomHybrid()
     * @see  #isMonomHybrid()
     */
    public void setTrinomHybrid(boolean trinomHybrid){
        this.trinomHybrid = trinomHybrid;
    }


    /**
     * Returns the set of all {@link HybridRelationship hybrid relationships}
     * in which <i>this</i> taxon name is involved as a {@link common.RelationshipBase#getRelatedFrom() parent}.
     *
     * @see    #getHybridRelationships()
     * @see    #getChildRelationships()
     * @see    HybridRelationshipType
     */
    public Set<HybridRelationship> getHybridParentRelations() {
        if(hybridParentRelations == null) {
            this.hybridParentRelations = new HashSet<HybridRelationship>();
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
    public Set<HybridRelationship> getHybridChildRelations() {
        if(hybridChildRelations == null) {
            this.hybridChildRelations = new HashSet<HybridRelationship>();
        }
        return hybridChildRelations;
    }

    private void setHybridChildRelations(Set<HybridRelationship> hybridChildRelations) {
        this.hybridChildRelations = hybridChildRelations;
    }

    /**
     * Returns the hybrid child relationships ordered by relationship type, or if equal
     * by title cache of the related names.
     * @see #getHybridParentRelations()
     */
    @Transient
    public List<HybridRelationship> getOrderedChildRelationships(){
        List<HybridRelationship> result = new ArrayList<HybridRelationship>();
        result.addAll(this.hybridChildRelations);
        Collections.sort(result);
        Collections.reverse(result);
        return result;

    }


    /**
     * Adds the given {@link HybridRelationship hybrid relationship} to the set
     * of {@link #getHybridRelationships() hybrid relationships} of both non-viral names
     * involved in this hybrid relationship. One of both non-viral names
     * must be <i>this</i> non-viral name otherwise no addition will be carried
     * out. The {@link eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedTo() child
     * non viral taxon name} must be a hybrid, which means that one of its four hybrid flags must be set.
     *
     * @param relationship  the hybrid relationship to be added
     * @see    				#isHybridFormula()
     * @see    				#isMonomHybrid()
     * @see    				#isBinomHybrid()
     * @see    				#isTrinomHybrid()
     * @see    				#getHybridRelationships()
     * @see    				#getParentRelationships()
     * @see    				#getChildRelationships()
     * @see    				#addRelationship(RelationshipBase)
     * @throws 				IllegalArgumentException
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
     * Does the same as the addHybridRelationship method if the given
     * {@link common.RelationshipBase relation} is also a {@link HybridRelationship hybrid relationship}.
     * Otherwise this method does the same as the overwritten {@link TaxonNameBase#addRelationship(RelationshipBase) addRelationship}
     * method from TaxonNameBase.
     *
     * @param relation  the relationship to be added to some of <i>this</i> taxon name's relationships sets
     * @see    	   		#addHybridRelationship(HybridRelationship)
     * @see    	   		TaxonNameBase#addRelationship(RelationshipBase)
     * @see    	   		TaxonNameBase#addNameRelationship(NameRelationship)
     * @deprecated to be used by RelationshipBase only
     */
    @Override
    @Deprecated //to be used by RelationshipBase only
    public void addRelationship(RelationshipBase relation) {
        if (relation instanceof HybridRelationship){
            addHybridRelationship((HybridRelationship)relation);
        }else {
            super.addRelationship(relation);
        }
    }

    /**
     * Creates a new {@link HybridRelationship#HybridRelationship(BotanicalName, BotanicalName, HybridRelationshipType, String) hybrid relationship}
     * to <i>this</i> botanical name. A HybridRelationship may be of type
     * "is first/second parent" or "is male/female parent". By invoking this
     * method <i>this</i> botanical name becomes a hybrid child of the parent
     * botanical name.
     *
     * @param parentName	  the botanical name of the parent for this new hybrid name relationship
     * @param type			  the type of this new name relationship
     * @param ruleConsidered  the string which specifies the rule on which this name relationship is based
     * @return
     * @see    				  #addHybridChild(BotanicalName, HybridRelationshipType,String )
     * @see    				  #getRelationsToThisName()
     * @see    				  #getNameRelations()
     * @see    				  #addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
     * @see    				  #addNameRelationship(NameRelationship)
     */
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
     * @param childName		  the botanical name of the child for this new hybrid name relationship
     * @param type			  the type of this new name relationship
     * @param ruleConsidered  the string which specifies the rule on which this name relationship is based
     * @return
     * @see    				  #addHybridParent(BotanicalName, HybridRelationshipType,String )
     * @see    				  #getRelationsToThisName()
     * @see    				  #getNameRelations()
     * @see    				  #addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
     * @see    				  #addNameRelationship(NameRelationship)
     */
    public HybridRelationship addHybridChild(NonViralName childName, HybridRelationshipType type, String ruleConsidered){
        return new HybridRelationship(childName, this, type, ruleConsidered);
    }


    /**
     * Removes one {@link HybridRelationship hybrid relationship} from the set of
     * {@link #getHybridRelationships() hybrid relationships} in which <i>this</i> botanical taxon name
     * is involved. The hybrid relationship will also be removed from the set
     * belonging to the second botanical taxon name involved.
     *
     * @param  relationship  the hybrid relationship which should be deleted from the corresponding sets
     * @see    				 #getHybridRelationships()
     */
    public void removeHybridRelationship(HybridRelationship hybridRelation) {
        if (hybridRelation == null) {
            return;
        }

        NonViralName<?> parent = hybridRelation.getParentName();
        NonViralName<?> child = hybridRelation.getHybridName();
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

    /**
      * Needs to be implemented by those classes that handle autonyms (e.g. botanical names).
      **/
    @Transient
    public boolean isAutonym(){
        return false;
    }


//	/**
//	 * Returns the boolean value indicating whether <i>this</i> names rank is Rank "unranked"
//	 * (uuid = 'a965befb-70a9-4747-a18f-624456c65223') but most likely it is an infrageneric rank
//	 * due to existing atomized data for the genus epithet and the infrageneric epithet but missing
//	 * specific epithet.
//	 * Returns false if <i>this</i> names rank is null.
//	 *
//	 * @see  #isSupraGeneric()
//	 * @see  #isGenus()
//	 * @see  #isSpeciesAggregate()
//	 * @see  #isSpecies()
//	 * @see  #isInfraSpecific()
//	 */
//	@Transient
//	public boolean isInfragenericUnranked() {
//		Rank rank = this.getRank();
//		if (rank == null || ! rank.equals(Rank.UNRANKED())){
//			return false;
//		}
//		if (StringUtils.isBlank(this.getSpecificEpithet()) && StringUtils.isBlank(this.getInfraSpecificEpithet()) ){
//			return true;
//		}else{
//			return false;
//		}
//	}


    /**
     * Tests if the given name has any authors.
     * @return false if no author ((ex)combination or (ex)basionym) exists, true otherwise
     */
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
    public String computeCombinationAuthorNomenclaturalTitle() {
        return computeNomenclaturalTitle(this.getCombinationAuthorship());
    }

    /**
     * Shortcut. Returns the basionym authors title cache. Returns null if no basionym author exists.
     * @return
     */
    public String computeBasionymAuthorNomenclaturalTitle() {
        return computeNomenclaturalTitle(this.getBasionymAuthorship());
    }


    /**
     * Shortcut. Returns the ex-combination authors title cache. Returns null if no ex-combination author exists.
     * @return
     */
    public String computeExCombinationAuthorNomenclaturalTitle() {
        return computeNomenclaturalTitle(this.getExCombinationAuthorship());
    }

    /**
     * Shortcut. Returns the ex-basionym authors title cache. Returns null if no exbasionym author exists.
     * @return
     */
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

//*********************** CLONE ********************************************************/

    /**
     * Clones <i>this</i> non-viral name. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> non-viral name by
     * modifying only some of the attributes.
     *
     * @see eu.etaxonomy.cdm.model.name.TaxonNameBase#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        NonViralName<?> result = (NonViralName<?>)super.clone();

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

        //no changes to: basionamyAuthorship, combinationAuthorship, exBasionymAuthorship, exCombinationAuthorship
        //genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet,
        //protectedAuthorshipCache, protectedNameCache
        //binomHybrid, monomHybrid, trinomHybrid, hybridFormula,
        return result;
    }
}
