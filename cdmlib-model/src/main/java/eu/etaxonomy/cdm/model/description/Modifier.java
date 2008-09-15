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
import javax.persistence.*;

/**
 * This class represents possible modulations for the InfoItems such as "variance",
 * "maximum", "often", "probably". Its instances build a controlled
 * {@link TermVocabulary term vocabulary}.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:35
 */
@Entity
public class Modifier extends OrderedTermBase<Modifier> {
	static Logger logger = Logger.getLogger(Modifier.class);

	/**
	 * Factory method
	 * @return
	 */
	public static Modifier NewInstance(){
		return new Modifier();
	}
	
	/**
	 * Constructor
	 */
	protected Modifier(){
		super();
	}
	

	public Modifier(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}
}