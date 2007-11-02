/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.common.Media;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;

/**
 * Dichotomous or multifurcating
 * authored keys (incl. legacy data)
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:17
 */
public class IdentificationKey extends Media VersionableEntity {
	static Logger logger = Logger.getLogger(IdentificationKey.class);

	private ArrayList coveredTaxa;

	public ArrayList getCoveredTaxa(){
		return coveredTaxa;
	}

	/**
	 * 
	 * @param coveredTaxa
	 */
	public void setCoveredTaxa(ArrayList coveredTaxa){
		;
	}

}