/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.hibernate;



/**
 * Objects implementing this will be updated when the CdmPostCrudListener fires
 * 
 * 
 * @author n.hoffmann
 * @created 17.03.2009
 */
public interface ICdmPostDataChangeObserver {
	
	/**
	 * gets called when the observable objects notifies its observers
	 */
	public void update(CdmDataChangeMap changeEvents);
}
