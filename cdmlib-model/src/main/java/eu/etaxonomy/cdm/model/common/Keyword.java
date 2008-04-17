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
 * simple keywords. could be taxonomic scope/skill , geographic scope or anything else
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:31
 */
@Entity
public class Keyword extends OrderedTermBase<Keyword> {
	static Logger logger = Logger.getLogger(Keyword.class);

	public static Keyword NewInstance(){
		return new Keyword();
	}


	public static Keyword NewInstance(String term, String label){
		return new Keyword(term, label);
	}

	
	private Keyword() {
		super();
		// TODO Auto-generated constructor stub
	}

	private Keyword(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
	}

}