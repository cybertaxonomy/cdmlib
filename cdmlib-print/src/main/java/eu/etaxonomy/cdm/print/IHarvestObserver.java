// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.print;


/**
 * A client implementing this interface and registering with a {@link NotificationMediator}
 * will get notified of the progress of the print publishing process.
 * 
 * @author n.hoffmann
 * @created Jul 19, 2010
 * @version 1.0
 */
public interface IHarvestObserver {

	/**
	 * 
	 * @param message
	 */
	public void update(String message);
}
