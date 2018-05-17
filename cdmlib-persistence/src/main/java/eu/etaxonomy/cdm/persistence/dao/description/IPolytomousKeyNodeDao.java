/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.description;

import java.util.List;

import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.persistence.dao.common.IVersionableDao;

/**
 * @author a.mueller
 * @since 08.11.2010
 */
public interface IPolytomousKeyNodeDao extends IVersionableDao<PolytomousKeyNode> {

	public List<PolytomousKeyNode> list();

	/**
	 * @param key
	 */
//	public void deleteByKey(PolytomousKey key);

}
