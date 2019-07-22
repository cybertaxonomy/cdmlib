/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.description;

import java.util.List;

import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.persistence.dao.common.IVersionableDao;

/**
 * @author a.babadshanjan
 * @since 09.09.2008
 */
public interface ITermNodeDao extends IVersionableDao<TermNode> {

	public List<TermNode> list();

}
