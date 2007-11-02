/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.common.Media;
import eu.etaxonomy.cdm.model.common.IReferencedEntity;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:13
 */
public abstract class FeatureBase extends AnnotatableEntity implements IReferencedEntity {
	static Logger logger = Logger.getLogger(FeatureBase.class);

	@Description("")
	private String modifyingText;
	private ArrayList media;
	/**
	 * type, category of information. In structured descriptions characters
	 */
	private FeatureType type;
	private ArrayList modifiers;

	public ArrayList getMedia(){
		return media;
	}

	/**
	 * 
	 * @param media
	 */
	public void setMedia(ArrayList media){
		;
	}

	public FeatureType getType(){
		return type;
	}

	/**
	 * 
	 * @param type
	 */
	public void setType(FeatureType type){
		;
	}

	public ArrayList getModifiers(){
		return modifiers;
	}

	/**
	 * 
	 * @param modifiers
	 */
	public void setModifiers(ArrayList modifiers){
		;
	}

	public String getModifyingText(){
		return modifyingText;
	}

	/**
	 * 
	 * @param modifyingText
	 */
	public void setModifyingText(String modifyingText){
		;
	}

	@Transient
	public String getCitation(){
		return "";
	}

	@Transient
	public StrictReferenceBase getCitation(){
		return null;
	}

}