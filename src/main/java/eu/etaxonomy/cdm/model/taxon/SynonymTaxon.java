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
import java.util.*;
import javax.persistence.*;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:14
 */
@Entity
public class SynonymTaxon extends TaxonBase {
	static Logger logger = Logger.getLogger(SynonymTaxon.class);

	private ArrayList synoynmRelations;

	public ArrayList getSynoynmRelations(){
		return synoynmRelations;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSynoynmRelations(ArrayList newVal){
		synoynmRelations = newVal;
	}

}