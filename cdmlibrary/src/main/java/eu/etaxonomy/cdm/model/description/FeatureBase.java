/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Media;
import eu.etaxonomy.cdm.model.common.IReferencedEntity;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.MultilanguageSet;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;

import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:24
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class FeatureBase extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(FeatureBase.class);
	//type, category of information. In structured descriptions characters
	private FeatureType type;
	private Set<Modifier> modifiers;
	private MultilanguageSet modifyingText;
	private Set<Media> media;

	
	@OneToMany
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


	@ManyToOne
	public FeatureType getType(){
		return this.type;
	}
	public void setType(FeatureType type){
		this.type = type;
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
	public void addModifyingText(LanguageString description){
		this.modifyingText.add(description);
	}
	public void addModifyingText(String text, Language lang){
		this.modifyingText.add(text, lang);
	}
	public void removeModifyingText(Language lang){
		this.modifyingText.remove(lang);
	}
}