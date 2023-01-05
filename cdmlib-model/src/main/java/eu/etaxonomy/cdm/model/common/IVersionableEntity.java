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

import eu.etaxonomy.cdm.model.permission.User;

/**
 * @author n.hoffmann
 * @since Sep 15, 2010
 */
public interface IVersionableEntity extends ICdmBase {

	public User getUpdatedBy();

	public void setUpdatedBy(User updatedBy);

	public DateTime getUpdated();

	public void setUpdated(DateTime updated);

}