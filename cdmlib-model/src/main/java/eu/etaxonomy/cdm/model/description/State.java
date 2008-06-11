/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:53
 */
@Entity
public class State extends OrderedTermBase<State> {
	static Logger logger = Logger.getLogger(State.class);

	/**
	 * Factory method
	 * @return
	 */
	public static State NewInstance(){
		return new State();
	}
	
	/**
	 * Factory method
	 * @return
	 */
	public static State NewInstance(String term, String label, String labelAbbrev){
		return new State(term, label, labelAbbrev);
	}
	
	public State() {
		super();
	}

	public State(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}
	

}