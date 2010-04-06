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

import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

/**
 * @author a.mueller
 * @created 10.07.2008
 * @version 1.0
 */
public interface IFeatureTreeDao extends IIdentifiableDao<FeatureTree> {
	public List<FeatureTree> list();

	public void loadNodes(FeatureNode root, List<String> nodePaths);
}
