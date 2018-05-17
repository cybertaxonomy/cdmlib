/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.description;

import java.util.List;

import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

/**
 * @author a.mueller
 * @since 08.11.2010
 * @version 1.0
 */
public interface IPolytomousKeyDao extends IIdentifiableDao<PolytomousKey> {
	
	public List<PolytomousKey> list();
	
	public void loadNodes(PolytomousKeyNode root, List<String> nodePaths);
}
