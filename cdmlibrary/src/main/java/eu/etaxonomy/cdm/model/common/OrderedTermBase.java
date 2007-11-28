/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:23
 */
@Entity
public abstract class OrderedTermBase extends DefinedTermBase {
	static Logger logger = Logger.getLogger(OrderedTermBase.class);
	public OrderedTermBase() {
		super();
	}
	public OrderedTermBase(String term, String label, TermVocabulary vocabulary) {
		super(term, label);
		setVocabulary(vocabulary);
	}
}