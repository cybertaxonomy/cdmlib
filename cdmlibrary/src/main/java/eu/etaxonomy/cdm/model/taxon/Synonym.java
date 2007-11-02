/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:41
 */
public class Synonym extends TaxonBase {
	static Logger logger = Logger.getLogger(Synonym.class);

	private ArrayList synoynmRelations;

	public ArrayList getSynoynmRelations(){
		return synoynmRelations;
	}

	/**
	 * 
	 * @param synoynmRelations
	 */
	public void setSynoynmRelations(ArrayList synoynmRelations){
		;
	}

}