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
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * This class represents terms describing different states (like "oval" or
 * "triangular") for {@link Feature features} that can be described with
 * categorical values (like for instance shapes).
 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:53
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "State")
@XmlRootElement(name = "State")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class State extends OrderedTermBase<State> {
	private static final long serialVersionUID = -4816292463790262516L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(State.class);

	protected static Map<UUID, State> termMap = null;


//********************************** Constructor *******************************************************************/

	//for hibernate use only
	@Deprecated
	protected State() {
		super(TermType.State);
	}

	/**
	 * Class constructor: creates a new state with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 *
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new state to be created
	 * @param	label  		 the string identifying the new state to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new state to be created
	 * @see 				 #State()
	 */
	private State(String term, String label, String labelAbbrev) {
		super(TermType.State, term, label, labelAbbrev);
	}

	//********* METHODS **************************************/
	/**
	 * Creates a new empty state.
	 *
	 * @see #NewInstance(String, String, String)
	 */
	public static State NewInstance(){
		return new State();
	}

	/**
	 * Creates a new state with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 *
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new state to be created
	 * @param	label  		 the string identifying the new state to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new state to be created
	 * @see 				 #NewInstance()
	 */
	public static State NewInstance(String term, String label, String labelAbbrev){
		return new State(term, label, labelAbbrev);
	}

//************************** METHODS ********************************

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}


	@Override
	protected void setDefaultTerms(TermVocabulary<State> termVocabulary){
		termMap = new HashMap<UUID, State>();
		for (State term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

}
