// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.merge;


/**
 * Enumeration for merge modes
 * @author a.mueller
 * @created 31.07.2009
 * @version 1.0
 */
public enum MergeMode {
	FIRST,
	SECOND,
	NULL,   //for all but collections(, cdmBases?)
	CONCAT,   //for Strings
	ADD,    //for collections
	ADD_CLONE,  //for collections
	AND,     //only for boolean values
	OR,	 	//only for boolean values
	RELATION, //only for collections of relationsshipbase
	MERGE
}
