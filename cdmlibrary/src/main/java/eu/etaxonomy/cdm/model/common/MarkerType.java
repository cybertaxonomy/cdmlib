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
 * Marker types similar to dynamically defined attributes. These  content types
 * like "IS_DOUBTFUL", "COMPLETE"  or specific local flags.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:33
 */
@Entity
public class MarkerType extends NonOrderedTermBase {
	static Logger logger = Logger.getLogger(MarkerType.class);

	public MarkerType(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
	}


	public static final MarkerType IMPORTED(){
		return null;
	}

	public static final MarkerType TO_BE_CHECKED(){
		return null;
	}

	public static final MarkerType IS_DOUBTFUL(){
		return null;
	}

	public static final MarkerType COMPLETE(){
		return null;
	}

}