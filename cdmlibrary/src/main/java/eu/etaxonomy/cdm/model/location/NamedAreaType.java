/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;


import eu.etaxonomy.cdm.model.common.DefinedTermBase;


import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * Controlled vocabulary to diferenctiate categories of areas
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:37
 */
@Entity
public class NamedAreaType extends DefinedTermBase {
	static Logger logger = Logger.getLogger(NamedAreaType.class);

	public NamedAreaType(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
	}


	/**
	 * The boundaries are given by natural factors (mountains, valleys, climate, etc.)
	 */
	public static final NamedAreaType NATURAL_AREA(){
		return null;
	}

	/**
	 * The boundaries depend on administration (county, state, reserve, etc.)
	 */
	public static final NamedAreaType ADMINISTRATION_AREA(){
		return null;
	}

}