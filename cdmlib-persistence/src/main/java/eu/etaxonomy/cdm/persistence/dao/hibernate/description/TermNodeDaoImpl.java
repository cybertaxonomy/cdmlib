/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.List;

import org.hibernate.Criteria;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.persistence.dao.description.ITermNodeDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.VersionableDaoBase;

/**
 * @author a.babadshanjan
 * @since 09.09.2008
 */
@Repository
public class TermNodeDaoImpl
        extends VersionableDaoBase<TermNode>
        implements ITermNodeDao {

	public TermNodeDaoImpl() {
		super(TermNode.class);
	}

	@Override
    public List<TermNode> list() {
		Criteria crit = getSession().createCriteria(type);
		@SuppressWarnings("unchecked")
        List<TermNode> result = crit.list();
		return result;
	}

}
