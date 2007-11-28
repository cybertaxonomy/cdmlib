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
import eu.etaxonomy.cdm.model.common.NonOrderedTermBase;

import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * Reference systems for coordinates also according to OGC (Open Geographical
 * Consosrtium) The list should be extensible at runtime through configuration.
 * This needs to be investigated.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:49
 */
@Entity
public class ReferenceSystem extends DefinedTermBase {
	static Logger logger = Logger.getLogger(ReferenceSystem.class);

	public ReferenceSystem(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
	}


	public static final ReferenceSystem WGS84(){
		return null;
	}

}