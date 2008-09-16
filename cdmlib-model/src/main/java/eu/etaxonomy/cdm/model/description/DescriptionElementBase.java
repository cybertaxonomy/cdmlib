/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.media.IMediaEntity;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
 * <li> MeasurementOrFact according to the ABCD schema
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
	    "media"
})
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class DescriptionElementBase extends ReferencedEntityBase implements IMediaEntity{
	private static final Logger logger = Logger.getLogger(DescriptionElementBase.class);
	

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
	
	//type, category of information. In structured descriptions characters
	@XmlElement(name = "Feature")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private Feature feature;
	
	@XmlElementWrapper(name = "Modifiers")
	@XmlElement(name = "Modifier")
	private Set<Modifier> modifiers = new HashSet<Modifier>();
	
	@XmlElement(name = "ModifyingText")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
	private MultilanguageText modifyingText;
	
	@XmlElementWrapper(name = "Media")
	@XmlElement(name = "Medium")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private Set<Media> media = new HashSet<Media>();


	/** 
	 * Returns the set of {@link Media media} (that is pictures, movies,
	 * recorded sounds ...) <i>this</i> description element is based on.
	 */
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<Media> getMedia(){
		return this.media;
	}
	/**
	 * @see	#getMedia() 
	 */
	protected void setMedia(Set<Media> media) {
		this.media = media;
	}
	/**
	 * Adds a {@link Media media} to the set of {@link #getMedia() media}
	 * <i>this</i> description element is based on.
	 * 
	 * @param media	the media to be added to <i>this</i> description element
	 * @see    	   	#getMedia()
	 */
	public void addMedia(Media media){
		this.media.add(media);
	}
	/** 
	 * Removes one element from the set of {@link #getMedia() media}
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
	 * Does exactly the same as getFeature().
	 * 
	 * @see #getFeature() 
	 */
	@Transient
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
	public void setType(Feature type){
		this.setFeature(type);
	}
	
	/** 
	 * Returns the {@link Feature feature} <i>this</i> description element is for.
	 * A feature is a property that can be described or measured but not the
	 * description or the measurement itself.
	 */
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
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
	 * Returns the set of {@link Modifier modifiers} which modulate
	 * <i>this</i> description element.
	 */
	@OneToMany
	public Set<Modifier> getModifiers(){
		return this.modifiers;
	}
	/**
	 * @see	#getModifiers() 
	 */
	protected void setModifiers(Set<Modifier> modifiers){
		this.modifiers = modifiers;
	}
	/**
	 * Adds a {@link Modifier modifier} to the set of {@link #getModifiers() modifiers}
	 * which modulate <i>this</i> description element.
	 * 
	 * @param modifier	the modifier to be added to <i>this</i> description element
	 * @see    	   		#getModifiers()
	 */
	public void addModifier(Modifier modifier){
		this.modifiers.add(modifier);
	}
	/** 
	 * Removes one element from the set of {@link #getModifiers() modifiers}
	 * which modulate <i>this</i> description element.
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
	 * NOTE: the actual descriptive information for <i>this</i> description element
	 * is NOT stored in the modifying text. This is only metainformation for
	 * the element.
	 */
	public MultilanguageText getModifyingText(){
		return this.modifyingText;
	}
	/**
	 * @see	#getModifyingText() 
	 */
	protected void setModifyingText(MultilanguageText modifyingText){
		this.modifyingText = modifyingText;
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
		return this.modifyingText.add(description);
	}
	/**
	 * Creates a {@link LanguageString language string} based on the given text string
	 * and the given {@link Language language} and adds it to the {@link MultilanguageText multilanguage text} 
	 * used to modulate <i>this</i> description element.
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
}