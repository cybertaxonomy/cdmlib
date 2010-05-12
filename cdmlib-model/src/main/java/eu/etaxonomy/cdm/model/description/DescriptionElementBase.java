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
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
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
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
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
 * @version 1.0
 * @created 08-Nov-2007 13:06:24
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescriptionElementBase", propOrder = {
	    "feature",
	    "modifiers",
	    "modifyingText",
	    "media",
	    "inDescription",
	    "sources"
})
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class DescriptionElementBase extends AnnotatableEntity implements ISourceable<DescriptionElementSource> {
	private static final long serialVersionUID = 5000910777835755905L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DescriptionElementBase.class);
	
	//type, category of information. In structured descriptions characters
	@XmlElement(name = "Feature")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    //@Cascade(CascadeType.SAVE_UPDATE)
    @Cascade(CascadeType.MERGE)
    @IndexedEmbedded
	private Feature feature;
	
	@XmlElementWrapper(name = "Modifiers")
	@XmlElement(name = "Modifier")
	@XmlIDREF
    @XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="DescriptionElementBase_Modifier")
	private Set<Modifier> modifiers = new HashSet<Modifier>();
	
	@XmlElement(name = "ModifyingText")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "DescriptionElementBase_ModifyingText")
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
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
    @Cascade(CascadeType.SAVE_UPDATE)
	@IndexedEmbedded
    private DescriptionBase inDescription;
	
    @XmlElementWrapper(name = "Sources")
    @XmlElement(name = "DescriptionElementSource")
    @OneToMany(fetch = FetchType.LAZY)		
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE, CascadeType.DELETE_ORPHAN})
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
	 * Does exactly the same as getFeature().
	 * @author ben.clark
	 * FIXME Is there a need to have two methods with different names which do the same thing?
	 * 
	 * @see #getFeature() 
	 */
	@Transient
	@Deprecated //will be removed in version 3. 
	public Feature getType(){
		return this.getFeature();
	}
	/**
	 * Does exactly the same as setFeature(Feature).
	 * 
	 * @param type	the feature to be described or measured
	 * @see 		#setFeature(Feature) 
	 * @see 		#getFeature() 
	 */
	@Deprecated  //will be removed in version 3
	public void setType(Feature type){
		this.setFeature(type);
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
	 * Returns the set of {@link Modifier modifiers} used to qualify the validity of
	 * <i>this</i> description element. This is only metainformation.
	 */
	public Set<Modifier> getModifiers(){
		return this.modifiers;
	}

	/**
	 * Adds a {@link Modifier modifier} to the set of {@link #getModifiers() modifiers}
	 * used to qualify the validity of <i>this</i> description element.
	 * 
	 * @param modifier	the modifier to be added to <i>this</i> description element
	 * @see    	   		#getModifiers()
	 */
	public void addModifier(Modifier modifier){
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
	public void removeModifier(Modifier modifier){
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
	 * @see    	   			#addModifyingText(String, Language)
	 */
	public LanguageString addModifyingText(LanguageString description){
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
	 * @see    	   		#addModifyingText(LanguageString)
	 */
	public LanguageString addModifyingText(String text, Language language){
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
	public Set<DescriptionElementSource> getSources() {
		return this.sources;		
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.ISourceable#addSource(eu.etaxonomy.cdm.model.common.IOriginalSource)
	 */
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
	 * @see eu.etaxonomy.cdm.model.common.ISourceable#addSource(java.lang.String, java.lang.String, eu.etaxonomy.cdm.model.reference.ReferenceBase, java.lang.String)
	 */
	public DescriptionElementSource addSource(String id, String idNamespace, ReferenceBase citation, String microCitation) {
		if (id == null && idNamespace == null && citation == null && microCitation == null){
			return null;
		}
		DescriptionElementSource source = DescriptionElementSource.NewInstance(id, idNamespace, citation, microCitation);
		addSource(source);
		return source;
	}
	
	public void addSource(String id, String idNamespace, ReferenceBase citation, String microReference, TaxonNameBase nameUsedInSource, String originalNameString){
		DescriptionElementSource newSource = DescriptionElementSource.NewInstance(id, idNamespace, citation, microReference, nameUsedInSource, originalNameString);
		addSource(newSource);
	}
	 
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.ISourceable#removeSource(eu.etaxonomy.cdm.model.common.IOriginalSource)
	 */
	public void removeSource(DescriptionElementSource source) {
		this.sources.remove(source);
	}

	
	/**
	 * Gets the citation micro reference of the first source. This method is deprecated and exists only to be compliant with version 2.0.
	 * It will be removed in v2.3
	 * @return
	 */
	@Transient
	@Deprecated
	public String getCitationMicroReference(){
		if (this.sources.size() < 1){
			return null;
		}else{
			return this.sources.iterator().next().getCitationMicroReference();
		}
	}
	
	/**
	 * Sets the citation micro reference of the first source. This method is deprecated and exists only to be compliant with version 2.0.
	 * It will be removed in v2.3
	 * If more than one source exists an IllegalStateException is thrown
	 **/
	@Transient
	@Deprecated
	public void setCitationMicroReference(String citationMicroReference){
		if (this.sources.size() < 1){
			ReferenceBase citation = null;
			this.addSource(DescriptionElementSource.NewInstance(null, null, citation, citationMicroReference));
		}else if (this.sources.size() > 1){
			throw new IllegalStateException("When adding a microcitation via the setCitationMicroReference method there must be only one source available");
		}else{
			this.sources.iterator().next().setCitationMicroReference(citationMicroReference);
		}
	}

	/**
	 * Gets the citation of the first source. This method is deprecated and exists only to be compliant with version 2.0.
	 * It will be removed in v2.3
	 */ 
	@Transient
	@Deprecated
	public ReferenceBase getCitation(){
		if (this.sources.size() < 1){
			return null;
		}else{
			return this.sources.iterator().next().getCitation();
		}
	}
	
	/**
	 * Sets the citation of the first source. This method is deprecated and exists only to be compliant with version 2.0.
	 * It will be removed in v2.3
	 * If more than one source exists an IllegalStateException is thrown
	 **/
	@Deprecated
	public void setCitation(ReferenceBase citation) {
		if (this.sources.size() < 1){
			this.addSource(DescriptionElementSource.NewInstance(null, null, citation, null));
		}else if (this.sources.size() > 1){
			throw new IllegalStateException("When adding a citation via the setCitation method there must be only one source available");
		}else{
			this.sources.iterator().next().setCitation(citation);
		}
	}
	
	
	/**
	 * Gets the original name string of the first source. This method is deprecated and exists only to be compliant with version 2.0.
	 * It will be removed in v2.3
	 * @return
	 */
	@Transient
	@Deprecated
	public String getOriginalNameString(){
		if (this.sources.size() < 1){
			return null;
		}else{
			return this.sources.iterator().next().getOriginalNameString();
		}
	}
	
	/**
	 * Sets the original name string of the first source. This method is deprecated and exists only to be compliant with version 2.0.
	 * It will be removed in v2.3
	 * If more than one source exists an IllegalStateException is thrown
	 **/
	@Transient
	@Deprecated
	public void setOriginalNameString(String originalNameString){
		if (this.sources.size() < 1){
			this.addSource(DescriptionElementSource.NewInstance(null, null, null, null, null, originalNameString));
		}else if (this.sources.size() > 1){
			throw new IllegalStateException("When adding a microcitation via the setCitationMicroReference method there must be only one source available");
		}else{
			this.sources.iterator().next().setOriginalNameString(originalNameString);
		}
	}
	

	/**
	 * Gets the name used in source of the first source. This method is deprecated and exists only to be compliant with version 2.0.
	 * It will be removed in v2.3
	 */ 
	@Transient
	@Deprecated
	public TaxonNameBase getNameUsedInReference(){
		if (this.sources.size() < 1){
			return null;
		}else{
			return this.sources.iterator().next().getNameUsedInSource();
		}
	}
	
	/**
	 * Sets the name used in reference of the first source. This method is deprecated and exists only to be compliant with version 2.0.
	 * It will be removed in v2.3
	 * If more than one source exists an IllegalStateException is thrown
	 **/
	@Deprecated
	public void setNameUsedInReference(TaxonNameBase nameUsedInSource) {
		if (this.sources.size() < 1){
			this.addSource(DescriptionElementSource.NewInstance(null, null, null, null, nameUsedInSource, null));
		}else if (this.sources.size() > 1){
			throw new IllegalStateException("When adding a citation via the setCitation method there must be only one source available");
		}else{
			this.sources.iterator().next().setNameUsedInSource(nameUsedInSource);
		}
	}

//************************** CLONE **********************************************************/	
	
	/** 
	 * Clones the description element. The element is <b>not</b> added to the same 
	 * description as the orginal element (inDescription is set to <code>null</null>.
	 * @see eu.etaxonomy.cdm.model.common.AnnotatableEntity#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException{
		DescriptionElementBase result = (DescriptionElementBase)super.clone();
		
		//Sources
		result.sources = new HashSet<DescriptionElementSource>();
		for (DescriptionElementSource source : getSources()){
			DescriptionElementSource newSource = (DescriptionElementSource)source.clone();
			result.addSource(newSource);
		}
		
		//inDescription
		this.inDescription = null;

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

	
}