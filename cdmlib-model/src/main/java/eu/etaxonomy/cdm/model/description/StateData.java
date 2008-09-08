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
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:53
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StateData", propOrder = {
    "state",
    "modifiers",
    "modifyingText"
})
@XmlRootElement(name = "StateData")
@Entity
public class StateData extends VersionableEntity {
	
	private static final Logger logger = Logger.getLogger(StateData.class);
	
	@XmlElement(name = "State")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private State state;
	
	@XmlElementWrapper(name = "Modifiers")
	@XmlElement(name = "Modifier")
	private Set<Modifier> modifiers = new HashSet<Modifier>();
	
	@XmlElement(name = "ModifyingText")
	private MultilanguageText modifyingText;
	
	/**
	 * Factory method
	 * @return
	 */
	public static StateData NewInstance(){
		return new StateData();
	}
	
	public StateData() {
		super();
	}

	@ManyToOne
	public State getState(){
		return this.state;
	}
	public void setState(State state){
		this.state = state;
	}
	

	@OneToMany
	public Set<Modifier> getModifiers(){
		return this.modifiers;
	}
	private void setModifiers(Set<Modifier> modifiers) {
		this.modifiers = modifiers;
	}
	public void addModifier(Modifier modifier){
		this.modifiers.add(modifier);
	}
	public void removeModifier(Modifier modifier){
		this.modifiers.remove(modifier);
	}


	public MultilanguageText getModifyingText(){
		return this.modifyingText;
	}
	private void setModifyingText(MultilanguageText modifyingText) {
		this.modifyingText = modifyingText;
	}
	public void addModifyingText(String text, Language language){
		this.modifyingText.put(language, LanguageString.NewInstance(text, language));
	}
	public void addModifyingText(LanguageString text){
		this.modifyingText.add(text);
	}
	public void removeModifyingText(Language lang){
		this.modifyingText.remove(lang);
	}

}