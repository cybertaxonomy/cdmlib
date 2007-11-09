/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MultilanguageArray;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:53
 */
@Entity
public class StateData extends VersionableEntity {
	static Logger logger = Logger.getLogger(StateData.class);
	private State state;
	private ArrayList<Modifier> modifiers;
	private MultilanguageArray modifyingText;

	public State getState(){
		return this.state;
	}

	/**
	 * 
	 * @param state    state
	 */
	public void setState(State state){
		this.state = state;
	}

	public ArrayList<Modifier> getModifiers(){
		return this.modifiers;
	}

	/**
	 * 
	 * @param modifiers    modifiers
	 */
	public void addModifier(Modifier modifier){
		this.modifiers.add(modifier);
	}
	public void removeModifier(Modifier modifier){
		this.modifiers.remove(modifier);
	}

	public MultilanguageArray getModifyingText(){
		return this.modifyingText;
	}

	/**
	 * 
	 * @param modifyingText    modifyingText
	 */
	public void addModifyingText(String text, Language lang){
		this.modifyingText.addText(text, lang);
	}
	public void removeModifyingText(Language lang){
		this.modifyingText.removeText(lang);
	}

}