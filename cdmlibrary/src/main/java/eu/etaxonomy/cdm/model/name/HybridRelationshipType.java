/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.common.EnumeratedTermBase;
import org.apache.log4j.Logger;

/**
 * The list should be extensible at runtime through configuration. This needs to
 * be investigated.
 * 
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:21
 */
public class HybridRelationshipType extends EnumeratedTermBase {
	static Logger logger = Logger.getLogger(HybridRelationshipType.class);

	public static final HybridRelationshipType FIRST_PARENT(){
		return null;
	}

	public static final HybridRelationshipType SECOND_PARENT(){
		return null;
	}

	public static final HybridRelationshipType FEMALE_PARENT(){
		return null;
	}

	public static final HybridRelationshipType MALE_PARENT(){
		return null;
	}

}