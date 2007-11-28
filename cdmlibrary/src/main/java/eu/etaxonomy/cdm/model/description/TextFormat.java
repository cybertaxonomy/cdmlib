/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.NonOrderedTermBase;

import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * kind of format used for structuring text. E.g. xml schema namespace, rdf, or
 * any other format
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:59
 */
@Entity
public class TextFormat extends DefinedTermBase {
	static Logger logger = Logger.getLogger(TextFormat.class);

	public TextFormat() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TextFormat(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
	}
}