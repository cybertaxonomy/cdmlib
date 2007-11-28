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
 * @created 08-Nov-2007 13:06:50
 */
@Entity
public class RightsTerm extends NonOrderedTermBase {
	static Logger logger = Logger.getLogger(RightsTerm.class);

	public RightsTerm(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
	}


	/**
	 * http://purl.org/dc/terms/accessRights
	 */
	public static final RightsTerm ACCESS_RIGHTS(){
		return null;
	}

	public static final RightsTerm COPYRIGHT(){
		return null;
	}

	public static final RightsTerm LICENSE(){
		return null;
	}

}