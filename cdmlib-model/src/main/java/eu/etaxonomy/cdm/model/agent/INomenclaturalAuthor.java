/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;

import javax.persistence.Entity;

/**
 * @author a.mueller
 *
 */
public interface INomenclaturalAuthor {

	/**
	 * @return
	 */
	public String getNomenclaturalTitle();

	
	/**
	 * @param nomenclaturalTitle
	 * @return
	 */
	public void setNomenclaturalTitle(String nomenclaturalTitle);

}
