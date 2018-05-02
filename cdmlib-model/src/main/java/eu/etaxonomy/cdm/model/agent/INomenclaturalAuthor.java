/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;

/** 
* Interface providing methods for nomenclatural authorship. 
* 
* @author a.mueller
* @version 1.0
* @since 17-APR-2008
*/
public interface INomenclaturalAuthor {

	/**
	 * @see TeamOrPersonBase#getNomenclaturalTitle()
	 */
	public String getNomenclaturalTitle();

	
	/**
	 * @see getNomenclaturalTitle()
	 */
	public void setNomenclaturalTitle(String nomenclaturalTitle);

}
