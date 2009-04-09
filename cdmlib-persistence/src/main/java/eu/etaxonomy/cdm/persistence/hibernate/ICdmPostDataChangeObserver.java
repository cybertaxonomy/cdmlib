// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.hibernate;

import java.util.List;
import java.util.Observer;


/**
 * Objects implementing this will be updated when the CdmPostCrudListener fires
 * 
 * 
 * @author n.hoffmann
 * @created 17.03.2009
 * @version 1.0
 */
public interface ICdmPostDataChangeObserver {
	
	/**
	 * gets called when the observable objects notifies its observers
	 */
	public void update(CdmDataChangeMap changeEvents);
}
