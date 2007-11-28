/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.TermVocabulary;

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:50
 */
@Entity
public class Scope extends Modifier {
	static Logger logger = Logger.getLogger(Scope.class);

	public Scope() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Scope(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
	}
}