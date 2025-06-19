/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.term;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.LanguageStringBaseDaoImpl;
import eu.etaxonomy.cdm.persistence.dao.term.IRepresentationDao;

/**
 * @author a.babadshanjan
 * @since 10.09.2008
 */
@Repository
@Deprecated
public class RepresentationDaoImpl
        extends LanguageStringBaseDaoImpl<Representation>
        implements IRepresentationDao {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	public RepresentationDaoImpl() {
		super(Representation.class);
	}

	@Override
    public List<Representation> getAllRepresentations(Integer limit, Integer start) {
		Criteria crit = getSession().createCriteria(Representation.class);
		List<Representation> results = crit.list();
		return results;
	}
}

