/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.media.IMediaEntity;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;

import eu.etaxonomy.cdm.model.common.MultilanguageSet;
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
import javax.xml.bind.annotation.XmlType;

/**
 * The upmost (abstract) class for a description element of a specimen
 * or of a taxon. A concrete description element assigns descriptive data to
 * the feature. As experts use the word feature for the property itself but not
 * for the actual description naming this class FeatureBase would make no sense.  
 * 
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
	
	protected DescriptionElementBase(){
	}
	
	protected DescriptionElementBase(Feature feature){
		if (feature == null){
			feature = Feature.UNKNOWN();
		}
		this.feature = feature; 
	}
	
	//type, category of information. In structured descriptions characters
	@XmlElement(name = "Feature")
	private Feature feature;
	
	@XmlElementWrapper(name = "Modifiers")
	@XmlElement(name = "Modifier")
	private Set<Modifier> modifiers = new HashSet<Modifier>();
	
	@XmlElement(name = "ModifyingText")
	private MultilanguageSet modifyingText;
	
	@XmlElementWrapper(name = "Media")
	@XmlElement(name = "Medium")
	private Set<Media> media = new HashSet<Media>();


	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<Media> getMedia(){
		return this.media;
	}
	protected void setMedia(Set<Media> media) {
		this.media = media;
	}
	public void addMedia(Media media){
		this.media.add(media);
	}
	public void removeMedia(Media media){
		this.media.remove(media);
	}


	/**
	 * Same as getFeature()
	 * @see getFeature() 
	 * @return
	 */
	@Transient
	public Feature getType(){
		return this.getFeature();
	}
	/**
	 * Same as setFeature(Feature feature)
	 * @see setFeature(Feature feature) 
	 * @param type
	 */
	public void setType(Feature type){
		this.setFeature(type);
	}
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	public Feature getFeature(){
		return this.feature;
	}
	public void setFeature(Feature feature){
		this.feature = feature;
	}

	
	@OneToMany
	public Set<Modifier> getModifiers(){
		return this.modifiers;
	}
	protected void setModifiers(Set<Modifier> modifiers){
		this.modifiers = modifiers;
	}
	public void addModifier(Modifier modifier){
		this.modifiers.add(modifier);
	}
	public void removeModifier(Modifier modifier){
		this.modifiers.remove(modifier);
	}

	
	public MultilanguageSet getModifyingText(){
		return this.modifyingText;
	}
	protected void setModifyingText(MultilanguageSet modifyingText){
		this.modifyingText = modifyingText;
	}
	public LanguageString addModifyingText(LanguageString description){
		return this.modifyingText.add(description);
	}
	public LanguageString addModifyingText(String text, Language language){
		return this.modifyingText.put(language, LanguageString.NewInstance(text, language));
	}
	public LanguageString removeModifyingText(Language language){
		return this.modifyingText.remove(language);
	}
}