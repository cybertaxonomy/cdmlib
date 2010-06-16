/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * This class represents the assignment of values ({@link State state terms}) to {@link Feature features}
 * corresponding to {@link CategoricalData categorical data}. A state data instance
 * constitutes an atomized part of an information piece (categorical data) so
 * that several state data instances may belong to one categorical data
 * instance.
 * <P>
 * This class corresponds to CharacterStateDataType according to the SDD schema.
 * 
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
@Audited
public class StateData extends VersionableEntity {
	private static final long serialVersionUID = -4380314126624505415L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(StateData.class);
	
	@XmlElement(name = "State")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private State state;
	
	@XmlElementWrapper(name = "Modifiers")
	@XmlElement(name = "Modifier")
	@ManyToMany(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private Set<Modifier> modifiers = new HashSet<Modifier>();
	
	@XmlElement(name = "ModifyingText")
	@XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
	@OneToMany(fetch = FetchType.LAZY)
	private Map<Language,LanguageString> modifyingText = new HashMap<Language,LanguageString>();
	
	/** 
	 * Class constructor: creates a new empty state data instance.
	 */
	public StateData() {
		super();
	}
	
	/** 
	 * Creates a new empty state data instance.
	 */
	public static StateData NewInstance(){
		return new StateData();
	}

	/** 
	 * Returns the {@link State state term} used in <i>this</i> state data.
	 */
	public State getState(){
		return this.state;
	}
	/** 
	 * @see	#getState()
	 */
	public void setState(State state){
		this.state = state;
	}
	

	/** 
	 * Returns the set of {@link Modifier modifiers} used to qualify the validity
	 * of <i>this</i> state data. This is only metainformation.
	 */
	public Set<Modifier> getModifiers(){
		return this.modifiers;
	}
	
	/**
	 * Adds a {@link Modifier modifier} to the set of {@link #getModifiers() modifiers}
	 * used to qualify the validity of <i>this</i> state data.
	 * 
	 * @param modifier	the modifier to be added to <i>this</i> state data
	 * @see    	   		#getModifiers()
	 */
	public void addModifier(Modifier modifier){
		this.modifiers.add(modifier);
	}
	/** 
	 * Removes one element from the set of {@link #getModifiers() modifiers}
	 * used to qualify the validity of <i>this</i> state data.
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
	 * of <i>this</i> state data.  The different {@link LanguageString language strings}
	 * contained in the multilanguage text should all have the same meaning.<BR>
	 * A multilanguage text does not belong to a controlled {@link TermVocabulary term vocabulary}
	 * as a {@link Modifier modifier} does.
	 * <P>
	 * NOTE: the actual content of <i>this</i> state data is NOT
	 * stored in the modifying text. This is only metainformation
	 * (like "Some experts express doubt about this assertion").
	 */
	public Map<Language,LanguageString> getModifyingText(){
		return this.modifyingText;
	}

	/**
	 * Creates a {@link LanguageString language string} based on the given text string
	 * and the given {@link Language language} and adds it to the {@link MultilanguageText multilanguage text} 
	 * used to qualify the validity of <i>this</i> state data.
	 * 
	 * @param text		the string describing the validity
	 * 					in a particular language
	 * @param language	the language in which the text string is formulated
	 * @see    	   		#getModifyingText()
	 * @see    	   		#addModifyingText(LanguageString)
	 */
	public void addModifyingText(String text, Language language){
		this.modifyingText.put(language, LanguageString.NewInstance(text, language));
	}
	/**
	 * Adds a translated {@link LanguageString text in a particular language}
	 * to the {@link MultilanguageText multilanguage text} used to qualify the validity
	 * of <i>this</i> state data.
	 * 
	 * @param text	the language string describing the validity
	 * 				in a particular language
	 * @see    	   	#getModifyingText()
	 * @see    	   	#addModifyingText(String, Language)
	 */
	public void addModifyingText(LanguageString text){
		this.modifyingText.put(text.getLanguage(),text);
	}
	/** 
	 * Removes from the {@link MultilanguageText multilanguage text} used to qualify the validity
	 * of <i>this</i> state data the one {@link LanguageString language string}
	 * with the given {@link Language language}.
	 *
	 * @param  lang	the language in which the language string to be removed
	 * 				has been formulated
	 * @see     	#getModifyingText()
	 */
	public void removeModifyingText(Language lang){
		this.modifyingText.remove(lang);
	}

}