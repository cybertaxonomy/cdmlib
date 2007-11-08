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
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:24
 */
@Entity
public abstract class FeatureBase extends AnnotatableEntity implements IReferencedEntity {
	static Logger logger = Logger.getLogger(FeatureBase.class);
	private String modifyingText;
	private ArrayList media;
	//type, category of information. In structured descriptions characters
	private FeatureType type;
	private ArrayList modifiers;

	public ArrayList getMedia(){
		return this.media;
	}

	/**
	 * 
	 * @param media    media
	 */
	public void setMedia(ArrayList media){
		this.media = media;
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

	public String getModifyingText(){
		return this.modifyingText;
	}

	/**
	 * 
	 * @param modifyingText    modifyingText
	 */
	public void setModifyingText(String modifyingText){
		this.modifyingText = modifyingText;
	}

	@Transient
	public ReferenceBase getCitation(){
		return null;
	}

}