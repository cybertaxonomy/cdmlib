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
import eu.etaxonomy.cdm.model.common.Media;
import eu.etaxonomy.cdm.model.common.IReferencedEntity;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.MultilanguageArray;
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
public abstract class FeatureBase extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(FeatureBase.class);
	//type, category of information. In structured descriptions characters
	private FeatureType type;
	private ArrayList<Modifier> modifiers;
	private MultilanguageArray modifyingText;
	private ArrayList<Media> media;

	public ArrayList<Media> getMedia(){
		return this.media;
	}

	/**
	 * 
	 * @param media    media
	 */
	public void addMedia(Media media){
		this.media.add(media);
	}
	public void removeMedia(Media media){
		this.media.remove(media);
	}

	public FeatureType getType(){
		return this.type;
	}

	/**
	 * 
	 * @param type    type
	 */
	public void setType(FeatureType type){
		this.type = type;
	}

	public ArrayList<Modifier> getModifiers(){
		return this.modifiers;
	}

	/**
	 * 
	 * @param modifiers    modifiers
	 */
	public void setModifiers(ArrayList<Modifier> modifiers){
		this.modifiers = modifiers;
	}

	public MultilanguageArray getModifyingText(){
		return this.modifyingText;
	}

	/**
	 * 
	 * @param modifyingText    modifyingText
	 */
	public void setModifyingText(MultilanguageArray modifyingText){
		this.modifyingText = modifyingText;
	}

}