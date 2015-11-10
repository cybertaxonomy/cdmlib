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

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.IMultiLanguageTextHolder;
import eu.etaxonomy.cdm.model.common.IOriginalSource;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;

/**
 * The upmost (abstract) class for a piece of information) about
 * a {@link SpecimenOrObservationBase specimen}, a {@link Taxon taxon} or even a {@link TaxonNameBase taxon name}.
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
 * @created 08-Nov-2007 13:06:24
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescriptionElementBase", propOrder = {
    "feature",
    "inDescription",
    "timeperiod",
    "modifiers",
    "modifyingText",
    "media",
    "sources"
})
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class DescriptionElementBase extends AnnotatableEntity implements ISourceable<DescriptionElementSource>, IModifiable, IMultiLanguageTextHolder{
    private static final long serialVersionUID = 5000910777835755905L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DescriptionElementBase.class);

    //type, category of information. In structured descriptions characters
    @XmlElement(name = "Feature")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    //@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @Cascade(CascadeType.MERGE)
    @IndexedEmbedded // no depth for terms
    private Feature feature;

    @XmlElementWrapper(name = "Modifiers")
    @XmlElement(name = "Modifier")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="DescriptionElementBase_Modifier")
    @IndexedEmbedded(depth=1)
    private Set<DefinedTerm> modifiers = new HashSet<DefinedTerm>();

    @XmlElement(name = "ModifyingText")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "DescriptionElementBase_ModifyingText")
    @MapKeyJoinColumn(name="modifyingtext_mapkey_id")
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    @IndexedEmbedded
    private Map<Language,LanguageString> modifyingText = new HashMap<Language,LanguageString>();

    @XmlElementWrapper(name = "Media")
    @XmlElement(name = "Medium")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @IndexColumn(name="sortIndex", base = 0)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    private List<Media> media = new ArrayList<Media>();

    @XmlElement(name = "InDescription")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @IndexedEmbedded
    private DescriptionBase inDescription;

	@XmlElement(name = "TimePeriod")
    private TimePeriod timeperiod = TimePeriod.NewInstance();

    @XmlElementWrapper(name = "Sources")
    @XmlElement(name = "DescriptionElementSource")
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    @Merge(MergeMode.ADD_CLONE)
    private Set<DescriptionElementSource> sources = new HashSet<DescriptionElementSource>();



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

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.ISourceable#getSources()
     */
    @Override
    public Set<DescriptionElementSource> getSources() {
        return this.sources;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.ISourceable#addSource(eu.etaxonomy.cdm.model.common.IOriginalSource)
     */
    @Override
    public void addSource(DescriptionElementSource source) {
        if (source != null){
            DescriptionElementBase oldSourcedObj = source.getSourcedObj();
            if (oldSourcedObj != null && oldSourcedObj != this){
                oldSourcedObj.getSources().remove(source);
            }
            this.sources.add(source);
            source.setSourcedObj(this);
        }
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.ISourceable#addSource(eu.etaxonomy.cdm.model.common.OriginalSourceType, java.lang.String, java.lang.String, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String)
     */
    @Override
    public DescriptionElementSource addSource(OriginalSourceType type, String id, String idNamespace, Reference citation, String microCitation) {
        if (id == null && idNamespace == null && citation == null && microCitation == null){
            return null;
        }
        DescriptionElementSource source = DescriptionElementSource.NewInstance(type, id, idNamespace, citation, microCitation);
        addSource(source);
        return source;
    }
    @Override
    public void addSources(Set<DescriptionElementSource> sources){
    	for (DescriptionElementSource source:sources){
    		addSource(source);
    	}
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.ISourceable#addImportSource(java.lang.String, java.lang.String, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String)
     */
    @Override
    public DescriptionElementSource addImportSource(String id, String idNamespace, Reference<?> citation, String microCitation) {
        if (id == null && idNamespace == null && citation == null && microCitation == null){
            return null;
        }
        DescriptionElementSource source = DescriptionElementSource.NewInstance(OriginalSourceType.Import, id, idNamespace, citation, microCitation);
        addSource(source);
        return source;
    }

    /**
     * Adds a {@link IOriginalSource source} to this description element.
     * @param type the type of the source
     * @param idInSource the id used in the source
     * @param idNamespace the namespace for the id in the source
     * @param citation the source as a {@link Reference reference}
     * @param microReference the details (e.g. page number) in the reference
     * @param nameUsedInSource the taxon name used in the source
     * @param originalNameString the name as text used in the source
     */
    public void addSource(OriginalSourceType type, String idInSource, String idNamespace, Reference citation, String microReference, TaxonNameBase nameUsedInSource, String originalNameString){
        DescriptionElementSource newSource = DescriptionElementSource.NewInstance(type, idInSource, idNamespace, citation, microReference, nameUsedInSource, originalNameString);
        addSource(newSource);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.ISourceable#removeSource(eu.etaxonomy.cdm.model.common.IOriginalSource)
     */
    @Override
    public void removeSource(DescriptionElementSource source) {
        this.sources.remove(source);
    }

// ******************* METHODS *************************************************************/

    protected Map<TermVocabulary, List<DefinedTerm>> makeModifierMap(){
        Map<TermVocabulary, List<DefinedTerm>> result = new HashMap<TermVocabulary, List<DefinedTerm>>();
        for (DefinedTerm modifier : getModifiers()){
            TermVocabulary<DefinedTerm> voc = modifier.getVocabulary();
            if (result.get(voc) == null){
                result.put(voc, new ArrayList<DefinedTerm>());
            }
            result.get(voc).add(modifier);
        }
        return result;
    }

    public List<DefinedTerm> getModifiers(TermVocabulary voc){
        List<DefinedTerm> result = makeModifierMap().get(voc);
        if (result == null){
            result = new ArrayList<DefinedTerm>();
        }
        return result;
    }



//************************** CLONE **********************************************************/

    /**
     * Clones the description element. The element is <b>not</b> added to the same
     * description as the orginal element (inDescription is set to <code>null</null>).
     * @see eu.etaxonomy.cdm.model.common.AnnotatableEntity#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException{
        DescriptionElementBase result = (DescriptionElementBase)super.clone();

        //inDescription
        result.inDescription = null;

        //Sources
        result.sources = new HashSet<DescriptionElementSource>();
        for (DescriptionElementSource source : getSources()){
            DescriptionElementSource newSource = (DescriptionElementSource)source.clone();
            result.addSource(newSource);
        }

        //media
        result.media = new ArrayList<Media>();
        for (Media media : getMedia()){
            result.media.add(media);
        }

        //modifying text
        result.modifyingText = new HashMap<Language, LanguageString>();
        for (Language language : getModifyingText().keySet()){
            //TODO clone needed? See also IndividualsAssociation
            LanguageString newLanguageString = (LanguageString)getModifyingText().get(language).clone();
            result.modifyingText.put(language, newLanguageString);
        }

        //modifiers
        result.modifiers = new HashSet<DefinedTerm>();
        for (DefinedTerm modifier : getModifiers()){
            result.modifiers.add(modifier);
        }

        //no changes to: feature
        return result;
    }

    /**
     * Clones the description element.<BR>
     * The new element is added to the <code>description</code>.<BR>
     * @see eu.etaxonomy.cdm.model.common.AnnotatableEntity#clone()
     */
    public DescriptionElementBase clone(DescriptionBase description) throws CloneNotSupportedException{
        DescriptionElementBase result = (DescriptionElementBase)clone();
        description.addElement(result);
        return result;
    }

    /**
     * Is this description item of a class type which is considere to
     * represent character data? These classes are {@link QuantitativeData}
     * and {@link CategoricalData}.
     * To be overriden by these classes.
     */
    @Transient
    @XmlTransient
    public boolean isCharacterData() {
        return false;
    }


}