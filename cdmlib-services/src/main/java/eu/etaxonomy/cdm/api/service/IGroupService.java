// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import org.springframework.security.provisioning.GroupManager;

import eu.etaxonomy.cdm.model.common.Group;

/**
 * @author n.hoffmann
 * @created Mar 9, 2011
 * @version 1.0
 */
public interface IGroupService extends IService<Group>, GroupManager{

}
