/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.occurrence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.BooleanBridge;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.hibernate.search.StripHtmlBridge;
import eu.etaxonomy.cdm.hibernate.search.UriBridge;
import eu.etaxonomy.cdm.jaxb.FormattedTextAdapter;
import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IIntextReferenceTarget;
import eu.etaxonomy.cdm.model.common.IMultiLanguageTextHolder;
import eu.etaxonomy.cdm.model.common.IPublishable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IDescribable;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.Match;
import eu.etaxonomy.cdm.strategy.match.Match.ReplaceMode;
import eu.etaxonomy.cdm.strategy.match.MatchMode;

/**
 * type figures are observations with at least a figure object in media
 * @author m.doering
 * @since 08-Nov-2007 13:06:41
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpecimenOrObservationBase", propOrder = {
    "recordBasis",
    "identityCache",
    "protectedIdentityCache",
    "publish",
    "preferredStableUri",
    "sex",
    "lifeStage",
    "kindOfUnit",
    "individualCount",
    "definition",
    "descriptions",
    "determinations",
    "derivationEvents"
})
@XmlRootElement(name = "SpecimenOrObservationBase")
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(name="SpecimenOrObservationBase", indexes = { @Index(name = "specimenOrObservationBaseTitleCacheIndex", columnList = "titleCache"),
        @Index(name = "specimenOrObservationBaseIdentityCacheIndex", columnList = "identityCache") })
public abstract class SpecimenOrObservationBase<S extends IIdentifiableEntityCacheStrategy<?>>
                extends IdentifiableEntity<S>
                implements IMultiLanguageTextHolder, IIntextReferenceTarget, IDescribable<DescriptionBase<S>>, IPublishable  {

    private static final long serialVersionUID = 6932680139334408031L;
    private static final Logger logger = LogManager.getLogger();

    /**
     * An indication of what the unit record describes.
     *
     * NOTE: The name of the attribute was chosen against the common naming conventions of the CDM
     * as it is well known in common standards like ABCD and DarwinCore. According to CDM naming
     * conventions it would be specimenOrObservationType.
     *
     * @see ABCD: DataSets/DataSet/Units/Unit/RecordBasis
     * @see Darwin Core: http://wiki.tdwg.org/twiki/bin/view/DarwinCore/BasisOfRecord
     */
    @XmlAttribute(name ="RecordBasis")
    @Column(name="recordBasis")
    @NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
        parameters = {@org.hibernate.annotations.Parameter(name = "enumClass", value = "eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType")}
    )
    @Audited
    private SpecimenOrObservationType recordBasis;


    @XmlElementWrapper(name = "Descriptions")
    @XmlElement(name = "Description")
    @OneToMany(mappedBy="describedSpecimenOrObservation", fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @NotNull
    private Set<DescriptionBase<S>> descriptions = new HashSet<>();


    @XmlElementWrapper(name = "Determinations")
    @XmlElement(name = "Determination")
    @OneToMany(mappedBy="identifiedUnit", orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @IndexedEmbedded(depth = 2)
    @NotNull
    private Set<DeterminationEvent> determinations = new HashSet<>();

    @XmlElement(name = "Sex")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    private DefinedTerm sex;

    @XmlElement(name = "LifeStage")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    private DefinedTerm lifeStage;

    /**
     * Part(s) of organism or class of materials represented by this unit.
     * Example: fruits, seeds, tissue, gDNA, leaves
     *
     * @see ABCD: DataSets/DataSet/Units/Unit/KindOfUnit
     * @see TermType#KindOfUnit
     */
    @XmlElement(name = "KindOfUnit")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
//    @IndexedEmbedded(depth=1)
    private DefinedTerm kindOfUnit;

    @XmlElement(name = "IndividualCount")
    @Field(analyze = Analyze.NO)
    private String individualCount;

    /**
     * The preferred stable identifier (URI) as discussed in
     * {@link  https://dev.e-taxonomy.eu/redmine/issues/5606}
     */
    @XmlElement(name = "PreferredStableUri")
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = UriBridge.class)
    @Type(type="uriUserType")
    private URI preferredStableUri;

    // the verbatim description of this occurrence. Free text usable when no atomised data is available.
    // in conjunction with titleCache which serves as the "citation" string for this object
    @XmlElement(name = "Description")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
    @MapKeyJoinColumn(name="definition_mapkey_id")
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.DELETE})
    @IndexedEmbedded
    @NotNull
    protected Map<Language,LanguageString> definition = new HashMap<>();

    // events that created derivedUnits from this unit
    @XmlElementWrapper(name = "DerivationEvents")
    @XmlElement(name = "DerivationEvent")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.DELETE})
    @NotNull
    protected Set<DerivationEvent> derivationEvents = new HashSet<>();

    @XmlAttribute(name = "publish")
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl=BooleanBridge.class)
    private boolean publish = true;

    @XmlElement(name = "IdentityCache", required = false)
    @XmlJavaTypeAdapter(FormattedTextAdapter.class)
    @Match(value=MatchMode.CACHE, cacheReplaceMode=ReplaceMode.ALL)
//    @NotEmpty(groups = Level2.class) // implicitly NotNull
    @Fields({
        @Field(store=Store.YES),
        //  If the field is only needed for sorting and nothing else, you may configure it as
        //  un-indexed and un-stored, thus avoid unnecessary index growth.
        @Field(name = "identityCache__sort", analyze = Analyze.NO, store=Store.NO, index = org.hibernate.search.annotations.Index.NO)
    })
    @SortableField(forField = "identityCache__sort")
    @FieldBridge(impl=StripHtmlBridge.class)
    private String identityCache;


    //if true identityCache will not be automatically generated/updated
    @XmlElement(name = "ProtectedIdentityCache")
    private boolean protectedIdentityCache;

//********************************** CONSTRUCTOR *********************************/

    //for hibernate use only, *packet* private required by bytebuddy and subclass
    SpecimenOrObservationBase(){}

    protected SpecimenOrObservationBase(SpecimenOrObservationType recordBasis) {
        if (recordBasis == null){ throw new IllegalArgumentException("RecordBasis must not be null");}
        this.recordBasis = recordBasis;
    }

//************************* GETTER / SETTER ***********************/

    /**@see #recordBasis */
    public SpecimenOrObservationType getRecordBasis() {
        return recordBasis;
    }
    /**@see #recordBasis */
    public void setRecordBasis(SpecimenOrObservationType recordBasis) {
        this.recordBasis = recordBasis;
    }


    /**
     * @return the identityCache
     */
    public String getIdentityCache() {
        return identityCache;
    }
    /**
     * @Deprecated For special use only.
     * Use {@link #setIdentityCache(String, boolean)} instead
     */
    @Deprecated
    public void setIdentityCache(String identityCache) {
        this.identityCache = identityCache;
    }

    /**
     * Sets the identity (short) cache. For how to use see {@link #setTitleCache(String, boolean)}
     */
    public void setIdentityCache(String identityCache, boolean isProtected) {
        setIdentityCache(identityCache);
        this.protectedIdentityCache = isProtected;
    }

    /**
     * @return the protectedIdentityCache
     */
    public boolean isProtectedIdentityCache() {
        return protectedIdentityCache;
    }
    /**
     * @param protectedIdentityCache the protectedIdentityCache to set
     */
    public void setProtectedIdentityCache(boolean protectedIdentityCache) {
        this.protectedIdentityCache = protectedIdentityCache;
    }

    /**@see #preferredStableUri */
    public URI getPreferredStableUri() {
        return preferredStableUri;
    }
    /**@see #preferredStableUri */
    public void setPreferredStableUri(URI preferredStableUri) {
        this.preferredStableUri = preferredStableUri;
    }


    /**
     * Returns the boolean value indicating if this specimen or observation should be withheld
     * (<code>publish=false</code>) or not (<code>publish=true</code>) during any publication
     * process to the general public.
     * This publish flag implementation is preliminary and may be replaced by a more general
     * implementation of READ rights in future.<BR>
     * The default value is <code>true</code>.
     */
    @Override
    public boolean isPublish() {
        return publish;
    }

    /**
     * @see #isPublish()
     * @param publish
     */
    @Override
    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    /**
     * The descriptions this specimen or observation is part of.<BR>
     * A specimen can not only have it's own {@link SpecimenDescription specimen description }
     * but can also be part of a {@link TaxonDescription taxon description} or a
     * {@link TaxonNameDescription taxon name description}.<BR>
     * @see #getSpecimenDescriptions()
     * @return
     */
    @Override
    public Set<DescriptionBase<S>> getDescriptions() {
        if(descriptions == null) {
            this.descriptions = new HashSet<>();
        }
        return this.descriptions;
    }

    /**
     * Returns the {@link SpecimenDescription specimen descriptions} this specimen is part of.
     * @see #getDescriptions()
     * @return
     */
    @Transient
    public Set<SpecimenDescription> getSpecimenDescriptions() {
        return getSpecimenDescriptions(true);
    }

    /**
     * Returns the {@link SpecimenDescription specimen descriptions} this specimen is part of.
     * @see #getDescriptions()
     * @return
     */
    @Transient
    public Set<SpecimenDescription> getSpecimenDescriptions(boolean includeImageGallery) {
        Set<SpecimenDescription> specimenDescriptions = new HashSet<>();
        for (DescriptionBase<?> descriptionBase : getDescriptions()){
            if (descriptionBase.isInstanceOf(SpecimenDescription.class)){
                if (includeImageGallery || descriptionBase.isImageGallery() == false){
                    specimenDescriptions.add(CdmBase.deproxy(descriptionBase, SpecimenDescription.class));
                }

            }
        }
        return specimenDescriptions;
    }
    /**
     * Returns the {@link SpecimenDescription specimen descriptions} which act as an image gallery
     * and which this specimen is part of.
     * @see #getDescriptions()
     */
    @Transient
    public Set<SpecimenDescription> getSpecimenDescriptionImageGallery() {
        Set<SpecimenDescription> specimenDescriptions = new HashSet<>();
        for (DescriptionBase<?> descriptionBase : getDescriptions()){
            if (descriptionBase.isInstanceOf(SpecimenDescription.class)){
                if (descriptionBase.isImageGallery() == true){
                    specimenDescriptions.add(descriptionBase.deproxy(descriptionBase, SpecimenDescription.class));
                }
            }
        }
        return specimenDescriptions;
    }

    /**
     * Adds a new description to this specimen or observation
     */
    @Override
    public void addDescription(DescriptionBase description) {
        if (description.getDescribedSpecimenOrObservation() != null){
            description.getDescribedSpecimenOrObservation().removeDescription(description);
        }
        descriptions.add(description);
        description.setDescribedSpecimenOrObservation(this);
    }

    /**
     * Removes a specimen from a description (removes a description from this specimen)
     */
    @Override
    public void removeDescription(DescriptionBase description) {
        boolean existed = descriptions.remove(description);
        if (existed){
            description.setDescribedSpecimenOrObservation(null);
        }
    }

    public Set<DerivationEvent> getDerivationEvents() {
        if(derivationEvents == null) {
            this.derivationEvents = new HashSet<>();
        }
        return this.derivationEvents;
    }

    public void addDerivationEvent(DerivationEvent derivationEvent) {
        if (! this.derivationEvents.contains(derivationEvent)){
            this.derivationEvents.add(derivationEvent);
            derivationEvent.addOriginal(this);
        }
    }

    public void removeDerivationEvent(DerivationEvent derivationEvent) {
        if (this.derivationEvents.contains(derivationEvent)){
            this.derivationEvents.remove(derivationEvent);
            derivationEvent.removeOriginal(this);
        }
    }

    public DerivationEvent addDerivedUnit(DerivedUnit derivedUnit, DerivationEventType derivationType) {
        return DerivationEvent.NewSimpleInstance(this, derivedUnit, derivationType);
    }

    //determination

    public Set<DeterminationEvent> getDeterminations() {
        if(determinations == null) {
            this.determinations = new HashSet<>();
        }
        return this.determinations;
    }

    /**
     * This method returns the preferred determination.
     * @see #getOtherDeterminations()
     * @see #getDeterminations()
     */
    @Transient
    public DeterminationEvent getPreferredDetermination() {
        for (DeterminationEvent event : getDeterminations()){
            if (event.getPreferredFlag() == true){
                return event;
            }
        }
        return null;
    }

    /**
     * This method returns the preferred determination.
     * @see #getOtherDeterminations()
     * @see #getDeterminations()
     */
    @Transient
    public void setPreferredDetermination(DeterminationEvent newEvent) {
        for (DeterminationEvent event : getDeterminations()){
            if (event.getPreferredFlag() == true){
                event.setPreferredFlag(false);
            }
        }
        newEvent.setPreferredFlag(true);
        getDeterminations().add(newEvent);
    }

    /**
     * This method returns all determinations except for the preferred one.
     * @see #getPreferredDetermination()
     * @see #getDeterminations()
     */
    @Transient
    public Set<DeterminationEvent> getOtherDeterminations(){
        Set<DeterminationEvent> result = new HashSet<>();
        for (DeterminationEvent event : getDeterminations()){
            if (event.getPreferredFlag() == false){
                result.add(event);
            }
        }
        return result;
    }

    @Transient
    public DeterminationEvent getDeterminationsForTaxonOrName(TaxonBase taxon, TaxonName name){
        for (DeterminationEvent event : getDeterminations()){
            if (taxon != null && event.getTaxon() != null &&event.getTaxon().equals(taxon) ){
                return event;
            }
            if (name != null && event.getTaxonName() != null && event.getTaxonName().equals(name)) {
                return event;
            }
        }
        return null;
    }

    public void addDetermination(DeterminationEvent determination) {
        // FIXME bidirectional integrity. Use protected Determination setter
        this.determinations.add(determination);
    }

    public void removeDetermination(DeterminationEvent determination) {
        // FIXME bidirectional integrity. Use protected Determination setter
        this.determinations.remove(determination);
    }

    public DefinedTerm getSex() {
        return sex;
    }

    public void setSex(DefinedTerm sex) {
        this.sex = sex;
    }

    public DefinedTerm getLifeStage() {
        return lifeStage;
    }

    public void setLifeStage(DefinedTerm lifeStage) {
        this.lifeStage = lifeStage;
    }


    /**
     * @see #kindOfUnit
     * @return
     */
    public DefinedTerm getKindOfUnit() {
        return kindOfUnit;
    }

    /**
     * @see #kindOfUnit
     * @param kindOfUnit
     */
    public void setKindOfUnit(DefinedTerm kindOfUnit) {
        this.kindOfUnit = kindOfUnit;
    }

    public String getIndividualCount() {
        return individualCount;
    }

    public void setIndividualCount(String individualCount) {
        this.individualCount = individualCount;
    }

    public Map<Language,LanguageString> getDefinition(){
        return this.definition;
    }

    /**
     * adds the {@link LanguageString description} to the {@link MultilanguageText multilanguage text}
     * used to define <i>this</i> specimen or observation.
     *
     * @param description	the languageString in with the title string and the given language
     *
     * @see    	   		#getDefinition()
     * @see    	   		#putDefinition(Language, String)
     */
    public void putDefinition(LanguageString description){
        this.definition.put(description.getLanguage(),description);
    }

    /**
     * Creates a {@link LanguageString language string} based on the given text string
     * and the given {@link Language language} and adds it to the {@link MultilanguageText multilanguage text}
     * used to define <i>this</i> specimen or observation.
     *
     * @param language	the language in which the title string is formulated
     * @param text		the definition in a particular language
     *
     * @see    	   		#getDefinition()
     * @see    	   		#putDefinition(LanguageString)
     */
    public void putDefinition(Language language, String text){
        this.definition.put(language, LanguageString.NewInstance(text, language));
    }


    public void removeDefinition(Language lang){
        this.definition.remove(lang);
    }

    /**
     * for derived units get the single next higher parental/original unit.
     * If multiple original units exist throw error
     * @return
     */
    @Transient
    public SpecimenOrObservationBase getOriginalUnit(){
        logger.warn("GetOriginalUnit not yet implemented");
        return null;
    }


    public boolean hasCharacterData() {
        Set<DescriptionBase<S>> descriptions = this.getDescriptions();
        for (DescriptionBase<?> descriptionBase : descriptions) {
            if (descriptionBase.isInstanceOf(SpecimenDescription.class)) {
                SpecimenDescription specimenDescription = HibernateProxyHelper.deproxy(descriptionBase, SpecimenDescription.class);
                Set<DescriptionElementBase> elements = specimenDescription.getElements();
                for (DescriptionElementBase descriptionElementBase : elements) {
                    if (descriptionElementBase.isCharacterData()){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns a list of all description items which are considered to be
     * character data. This currently means that they are of type
     * {@link QuantitativeData} or {@link CategoricalData}.
     *
     * @return the list of character data, an empty list if no character data exist
     */
    @Transient
    public Collection<DescriptionElementBase> characterData() {
        Collection<DescriptionElementBase> states = new ArrayList<>();
        Set<DescriptionBase<S>> descriptions = this.getDescriptions();
        for (DescriptionBase<?> descriptionBase : descriptions) {
            if (descriptionBase.isInstanceOf(SpecimenDescription.class)) {
                SpecimenDescription specimenDescription = HibernateProxyHelper.deproxy(descriptionBase, SpecimenDescription.class);
                Set<DescriptionElementBase> elements = specimenDescription.getElements();
                for (DescriptionElementBase descriptionElementBase : elements) {
                    if(descriptionElementBase.isCharacterData()){
                        states.add(descriptionElementBase);
                    }
                }
            }
        }
        return states;
    }

    public Collection<DerivedUnit> collectDerivedUnits(Integer maxDepth) {
        if(maxDepth == null) {
            maxDepth = Integer.MAX_VALUE;
        }
        Collection<DerivedUnit> derivedUnits = new ArrayList<>();
        for (DerivationEvent derivationEvent : getDerivationEvents()) {
            for (DerivedUnit derivative : derivationEvent.getDerivatives()) {
                derivedUnits.add(derivative);
                if(maxDepth > 0) {
                    derivedUnits.addAll(derivative.collectDerivedUnits(maxDepth - 1));
                }
            }
        }
        return derivedUnits;
    }

//******************** CLONE **********************************************/

    @Override
    public SpecimenOrObservationBase<S> clone() throws CloneNotSupportedException {
        SpecimenOrObservationBase<S> result = (SpecimenOrObservationBase<S>)super.clone();

        //defininion (description, languageString)
        result.definition = cloneLanguageString(this.definition);

        //sex
        result.setSex(this.sex);
        //life stage
        result.setLifeStage(this.lifeStage);

        result.descriptions = new HashSet<>();
        //Descriptions
        for(DescriptionBase<S> description : this.descriptions) {
            result.addDescription(description.clone());
        }

        result.determinations = new HashSet<>();
        for(DeterminationEvent determination : this.determinations) {
            result.addDetermination(determination.clone());
        }

        result.derivationEvents = new HashSet<>();
        //DerivationEvent
        for(DerivationEvent derivationEvent : this.derivationEvents) {
            //TODO should we clone this?
            result.addDerivationEvent(derivationEvent);
        }

        //no changes to: individualCount
        return result;
    }
}
