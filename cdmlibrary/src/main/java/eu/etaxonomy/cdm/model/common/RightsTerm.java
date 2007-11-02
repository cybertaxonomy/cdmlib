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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:30
 */
@Entity
public class RightsTerm extends DefinedTermBase {
	static Logger logger = Logger.getLogger(RightsTerm.class);

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