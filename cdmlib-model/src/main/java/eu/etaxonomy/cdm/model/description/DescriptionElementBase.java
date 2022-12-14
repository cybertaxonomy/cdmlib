/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.description;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.IMultiLanguageTextHolder;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.ICdmTarget;
import eu.etaxonomy.cdm.model.reference.IOriginalSource;
import eu.etaxonomy.cdm.model.reference.ISourceable;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;

/**
 * The upmost (abstract) class for a piece of information) about
 * a {@link SpecimenOrObservationBase specimen}, a {@link Taxon taxon} or even a {@link TaxonName taxon name}.
 * A concrete description element assigns descriptive data to one {@link Feature feature}.<BR>
 * Experts use the word feature for the property itself but not for the actual
 * description element. Therefore naming this class FeatureBase would have
 * leaded to confusion.
 * <P>
 * This class corresponds to: <ul>
 * <li> DescriptionsBaseType according to the the SDD schema
 * <li> InfoItem according to the TDWG ontology
 * <li> MeasurementOrFactAtomised according to the ABCD schema
 * </ul>
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:24
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescriptionElementBase", propOrder = {
    "feature",
    "inDescription",
    "timeperiod",
    "modifiers",
    "modifyingText",
    "media",
    "sources",
    "sortIndex"
})
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class DescriptionElementBase
        extends AnnotatableEntity
        implements ISourceable<DescriptionElementSource>, IModifiable, IMultiLanguageTextHolder{

    private static final long serialVersionUID = 5000910777835755905L;
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    //type, category of information. In structured descriptions characters
    @XmlElement(name = "Feature")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
//    @Cascade(CascadeType.MERGE)   remove cascade #5755
    @IndexedEmbedded // no depth for terms
    private Feature feature;

    @XmlElementWrapper(name = "Modifiers")
    @XmlElement(name = "Modifier")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="DescriptionElementBase_Modifier")
    @IndexedEmbedded(depth=1)
    private Set<DefinedTerm> modifiers = new HashSet<>();

    @XmlElement(name = "ModifyingText")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "DescriptionElementBase_ModifyingText")
    @MapKeyJoinColumn(name="modifyingtext_mapkey_id")
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    @IndexedEmbedded
    private Map<Language,LanguageString> modifyingText = new HashMap<>();

    @XmlElementWrapper(name = "Media")
    @XmlElement(name = "Medium")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @OrderColumn(name="sortIndex")
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    private List<Media> media = new ArrayList<>();

    @XmlElement(name = "InDescription")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @IndexedEmbedded(includeEmbeddedObjectId=true)
    private DescriptionBase<?> inDescription;

	@XmlElement(name = "TimePeriod")
    private TimePeriod timeperiod = TimePeriod.NewInstance();

    @XmlElementWrapper(name = "Sources")
    @XmlElement(name = "DescriptionElementSource")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sourcedElement", orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @Merge(MergeMode.ADD_CLONE)
    private Set<DescriptionElementSource> sources = new HashSet<>();

    //#8004 optional sortIndex
    private Integer sortIndex = null;



    // ************* CONSTRUCTORS *************/
    /**
     * Class constructor: creates a new empty description element instance.
     *
     * @see #DescriptionElementBase(Feature)
     */
    protected DescriptionElementBase(){
    }

    /**
     * Class constructor: creates a new description element instance with the
     * given {@link Feature feature} that is described or measured.
     *
     * @param	feature	the feature described or measured
     * @see 			#DescriptionElementBase()
     */
    protected DescriptionElementBase(Feature feature){
        if (feature == null){
            feature = Feature.UNKNOWN();
        }
        this.feature = feature;
    }

// ******************** GETTER / SETTER ***********************************/

    /**
     * Returns the list of {@link Media media} (that is pictures, movies,
     * recorded sounds ...) <i>this</i> description element is based on.
     */
    public List<Media> getMedia(){
        return this.media;
    }

    /**
     * Adds a {@link Media media} to the list of {@link #getMedia() media}
     * <i>this</i> description element is based on.
     *
     * @param media	the media to be added to <i>this</i> description element
     * @see    	   	#getMedia()
     */
    public void addMedia(Media media){
        this.media.add(media);
    }
    /**
     * Removes one element from the list of {@link #getMedia() media}
     * <i>this</i> description element is based on.
     *
     * @param  media	the media which should be removed
     * @see     		#getMedia()
     * @see     		#addMedia(Media)
     */
    public void removeMedia(Media media){
        this.media.remove(media);
    }

    /**
     * Returns the {@link DescriptionBase description} that <i>this</i> DescriptionElement is
     * part of.
     * @return
     */
    public DescriptionBase getInDescription() {
        return this.inDescription;
    }

    /**
     * @see	#setInDescription()
     */
    protected void setInDescription(DescriptionBase inDescription) {
        this.inDescription = inDescription;
    }

    /**
     * Returns the {@link Feature feature} <i>this</i> description element is for.
     * A feature is a property that can be described or measured but not the
     * description or the measurement itself.
     */
    public Feature getFeature(){
        return this.feature;
    }

    /**
     * @see	#getFeature()
     */
    public void setFeature(Feature feature){
        this.feature = feature;
    }

    /**
	 * The point in time, the time period or the season for which this description element
	 * is valid. A season may be expressed by not filling the year part(s) of the time period.
	 */
	public TimePeriod getTimeperiod() {
		return timeperiod;
	}

	/**
	 * @see #getTimeperiod()
	 */
	public void setTimeperiod(TimePeriod timeperiod) {
		if (timeperiod == null){
			timeperiod = TimePeriod.NewInstance();
		}
		this.timeperiod = timeperiod;
	}

	/**
     * Returns the set of {@link Modifier modifiers} used to qualify the validity of
     * <i>this</i> description element. This is only metainformation.
     */
    @Override
    public Set<DefinedTerm> getModifiers(){
        return this.modifiers;
    }

    /**
     * Adds a {@link Modifier modifier} to the set of {@link #getModifiers() modifiers}
     * used to qualify the validity of <i>this</i> description element.
     *
     * @param modifier	the modifier to be added to <i>this</i> description element
     * @see    	   		#getModifiers()
     */
    @Override
    public void addModifier(DefinedTerm modifier){
        this.modifiers.add(modifier);
    }
    /**
     * Removes one element from the set of {@link #getModifiers() modifiers}
     * used to qualify the validity of <i>this</i> description element.
     *
     * @param  modifier	the modifier which should be removed
     * @see     		#getModifiers()
     * @see     		#addModifier(Modifier)
     */
    @Override
    public void removeModifier(DefinedTerm modifier){
        this.modifiers.remove(modifier);
    }


    /**
     * Returns the {@link MultilanguageText multilanguage text} used to qualify the validity
     * of <i>this</i> description element.  The different {@link LanguageString language strings}
     * contained in the multilanguage text should all have the same meaning.<BR>
     * A multilanguage text does not belong to a controlled {@link TermVocabulary term vocabulary}
     * as a {@link Modifier modifier} does.
     * <P>
     * NOTE: the actual content of <i>this</i> description element is NOT
     * stored in the modifying text. This is only metainformation
     * (like "Some experts express doubt about this assertion").
     */
    public Map<Language,LanguageString> getModifyingText(){
        return this.modifyingText;
    }

    /**
     * Adds a translated {@link LanguageString text in a particular language}
     * to the {@link MultilanguageText multilanguage text} used to qualify the validity
     * of <i>this</i> description element.
     *
     * @param description	the language string describing the validity
     * 						in a particular language
     * @see    	   			#getModifyingText()
     * @see    	   			#putModifyingText(Language, String)
     * @deprecated 			should follow the put semantic of maps, this method will be removed in v4.0
     * 						Use the {@link #putModifyingText(LanguageString) putModifyingText} method
     */
    @Deprecated
    public LanguageString addModifyingText(LanguageString description){
        return this.putModifyingText(description);
    }

    /**
     * Adds a translated {@link LanguageString text in a particular language}
     * to the {@link MultilanguageText multilanguage text} used to qualify the validity
     * of <i>this</i> description element.
     *
     * @param description	the language string describing the validity
     * 						in a particular language
     * @see    	   			#getModifyingText()
     * @see    	   			#putModifyingText(Language, String)
     */
    public LanguageString putModifyingText(LanguageString description){
        return this.modifyingText.put(description.getLanguage(),description);
    }
    /**
     * Creates a {@link LanguageString language string} based on the given text string
     * and the given {@link Language language} and adds it to the {@link MultilanguageText multilanguage text}
     * used to qualify the validity of <i>this</i> description element.
     *
     * @param text		the string describing the validity
     * 					in a particular language
     * @param language	the language in which the text string is formulated
     * @see    	   		#getModifyingText()
     * @see    	   		#putModifyingText(LanguageString)
     * @deprecated 		should follow the put semantic of maps, this method will be removed in v4.0
     * 					Use the {@link #putModifyingText(Language, String) putModifyingText} method
     */
    @Deprecated
    public LanguageString addModifyingText(String text, Language language){
        return this.putModifyingText(language, text);
    }

    /**
     * Creates a {@link LanguageString language string} based on the given text string
     * and the given {@link Language language} and adds it to the {@link MultilanguageText multilanguage text}
     * used to qualify the validity of <i>this</i> description element.
     *
     * @param language	the language in which the text string is formulated
     * @param text		the string describing the validity
     * 					in a particular language
     *
     * @see    	   		#getModifyingText()
     * @see    	   		#putModifyingText(LanguageString)
     *
     */
    public LanguageString putModifyingText(Language language, String text){
        return this.modifyingText.put(language, LanguageString.NewInstance(text, language));
    }
    /**
     * Removes from the {@link MultilanguageText multilanguage text} used to qualify the validity
     * of <i>this</i> description element the one {@link LanguageString language string}
     * with the given {@link Language language}.
     *
     * @param  language	the language in which the language string to be removed
     * 					has been formulated
     * @see     		#getModifyingText()
     */
    public LanguageString removeModifyingText(Language language){
        return this.modifyingText.remove(language);
    }

    @Override
    public Set<DescriptionElementSource> getSources() {
        return this.sources;
    }

    @Override
    public void addSource(DescriptionElementSource source) {
        if (source != null){
            this.sources.add(source);
            source.setSourcedElement(this);
        }
    }

    @Override
    public DescriptionElementSource addSource(OriginalSourceType type, String id, String idNamespace,
            Reference reference, String microReference) {
        if (id == null && idNamespace == null && reference == null && microReference == null){
            return null;
        }
        DescriptionElementSource source = DescriptionElementSource.NewInstance(type, id, idNamespace,
                reference, microReference);
        addSource(source);
        return source;
    }

    @Override
    public void addSources(Set<DescriptionElementSource> sources){
    	for (DescriptionElementSource source:sources){
    		addSource(source);
    	}
    }

    @Override
    public DescriptionElementSource addImportSource(String id, String idNamespace, Reference reference, String microReference) {
        if (id == null && idNamespace == null && reference == null && microReference == null){
            return null;
        }
        DescriptionElementSource source = DescriptionElementSource.NewInstance(OriginalSourceType.Import, id, idNamespace, reference, microReference);
        addSource(source);
        return source;
    }

    @Override
    public DescriptionElementSource addPrimaryTaxonomicSource(Reference reference, String microReference) {
        if (reference == null && microReference == null){
            return null;
        }
        DescriptionElementSource source = DescriptionElementSource.NewPrimarySourceInstance(reference, microReference);
        addSource(source);
        return source;
    }

    @Override
    public DescriptionElementSource addPrimaryTaxonomicSource(Reference reference) {
        return addPrimaryTaxonomicSource(reference, null);
    }



    @Override
    public DescriptionElementSource addSource(OriginalSourceType type, Reference reference,
            String microReference, String originalInformation) {
        DescriptionElementSource newSource = DescriptionElementSource.NewInstance(type, null, null,
                reference, microReference, null, originalInformation);
        addSource(newSource);
        return newSource;
    }

    /**
     * Adds a {@link IOriginalSource source} to this description element.
     * @param type the type of the source
     * @param idInSource the id used in the source
     * @param idNamespace the namespace for the id in the source
     * @param reference the source as a {@link Reference reference}
     * @param microReference the details (e.g. page number) in the reference
     * @param nameUsedInSource the taxon name used in the source
     * @param originalInfo any information as original mentioned in the text
     */
    public DescriptionElementSource addSource(OriginalSourceType type, String idInSource, String idNamespace,
            Reference reference, String microReference, TaxonName nameUsedInSource, String originalInfo){
        DescriptionElementSource newSource = DescriptionElementSource.NewInstance(type, idInSource, idNamespace,
                reference, microReference, nameUsedInSource, originalInfo);
        addSource(newSource);
        return newSource;
    }

    @Override
    public DescriptionElementSource addAggregationSource(ICdmTarget target) {
        DescriptionElementSource source = DescriptionElementSource.NewInstance(
                OriginalSourceType.Aggregation, null, null, null, null,
                null, null, target);
        addSource(source);
        return source;
    }

    @Override
    public void removeSource(DescriptionElementSource source) {
        this.sources.remove(source);
    }


    public Integer getSortIndex() {
        return sortIndex;
    }
    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }

// ******************* METHODS *************************************************************/

    protected Map<TermVocabulary, List<DefinedTerm>> makeModifierMap(){
        Map<TermVocabulary, List<DefinedTerm>> result = new HashMap<>();
        for (DefinedTerm modifier : getModifiers()){
            TermVocabulary<DefinedTerm> voc = modifier.getVocabulary();
            if (result.get(voc) == null){
                result.put(voc, new ArrayList<>());
            }
            result.get(voc).add(modifier);
        }
        return result;
    }

    public List<DefinedTerm> getModifiers(TermVocabulary voc){
        List<DefinedTerm> result = makeModifierMap().get(voc);
        if (result == null){
            result = new ArrayList<>();
        }
        return result;
    }


    /**
     * Is this description item of a class type which is considered to
     * represent character data? These classes are {@link QuantitativeData}
     * and {@link CategoricalData}.
     * To be overridden by these classes.
     */
    @Transient
    @XmlTransient
    public boolean isCharacterData() {
        return false;
    }

//************************** CLONE **********************************************************/

    /**
     * Clones the description element. The element is <b>not</b> added to the same
     * description as the original element (inDescription is set to <code>null</null>).
     * @see eu.etaxonomy.cdm.model.common.AnnotatableEntity#clone()
     */
    @Override
    public DescriptionElementBase clone() {

        try {
            DescriptionElementBase result = (DescriptionElementBase)super.clone();

            //inDescription
            if (result.inDescription != null){
                result.inDescription.removeElement(result);
                result.inDescription = null;
            }

            //Sources
            result.sources = new HashSet<>();
            for (DescriptionElementSource source : getSources()){
                DescriptionElementSource newSource = source.clone();
                result.addSource(newSource);
            }

            //media
            result.media = new ArrayList<>();
            for (Media media : getMedia()){
                result.media.add(media);
            }

            //modifying text
            result.modifyingText = cloneLanguageString(getModifyingText());

            //modifiers
            result.modifiers = new HashSet<>();
            for (DefinedTerm modifier : getModifiers()){
                result.modifiers.add(modifier);
            }

            result.setTimeperiod(timeperiod == null? null:timeperiod.clone());

            //no changes to: feature
            return result;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Clones the description element.<BR>
     * The new element is added to the <code>description</code>.<BR>
     * @see eu.etaxonomy.cdm.model.common.AnnotatableEntity#clone()
     */
    public DescriptionElementBase clone(DescriptionBase description) {
        DescriptionElementBase result = clone();
        description.addElement(result);
        return result;
    }
}