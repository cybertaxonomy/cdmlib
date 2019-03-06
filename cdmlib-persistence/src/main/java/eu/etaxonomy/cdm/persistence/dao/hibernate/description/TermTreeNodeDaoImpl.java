/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.List;

import org.hibernate.Criteria;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.term.TermTreeNode;
import eu.etaxonomy.cdm.persistence.dao.description.ITermTreeNodeDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.VersionableDaoBase;

/**
 * @author a.babadshanjan
 * @since 09.09.2008
 */
@Repository
public class TermTreeNodeDaoImpl
        extends VersionableDaoBase<TermTreeNode>
        implements ITermTreeNodeDao {

	public TermTreeNodeDaoImpl() {
		super(TermTreeNode.class);
	}

	@Override
    public List<TermTreeNode> list() {
		Criteria crit = getSession().createCriteria(type);
		@SuppressWarnings("unchecked")
        List<TermTreeNode> result = crit.list();
		return result;
	}

}
