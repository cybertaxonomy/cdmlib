/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

/**
 * @author a.mueller
 * @created 21.05.2008
 * @version 1.0
 */
public interface IParsable {

	/** 
	 * this flag will be set to true if the parseName method was unable to successfully parse the name
	 */
	public boolean getHasProblem();
	
	public void setHasProblem(boolean hasProblem);
	
}
