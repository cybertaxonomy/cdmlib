/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import org.joda.time.DateTime;

/**
 * @author n.hoffmann
 * @created Sep 15, 2010
 * @version 1.0
 */
public interface IVersionableEntity extends ICdmBase {

	public User getUpdatedBy();

	/**
	 * 
	 * @param updatedBy    updatedBy
	 */
	public void setUpdatedBy(User updatedBy);

	/**
	 * 
	 * @return
	 */
	public DateTime getUpdated();

	/**
	 * 
	 * @param updated    updated
	 */
	public void setUpdated(DateTime updated);
	
}
