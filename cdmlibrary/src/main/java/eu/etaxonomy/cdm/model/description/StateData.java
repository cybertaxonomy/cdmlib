/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:33
 */
@Entity
public class StateData extends VersionableEntity {
	static Logger logger = Logger.getLogger(StateData.class);

	@Description("")
	private String modifyingText;
	private ArrayList modifiers;
	private State state;

	public State getState(){
		return state;
	}

	/**
	 * 
	 * @param state
	 */
	public void setState(State state){
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

}