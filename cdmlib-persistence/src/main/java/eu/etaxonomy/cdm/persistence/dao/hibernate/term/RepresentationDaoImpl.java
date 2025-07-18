/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.term;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoBaseImpl;
import eu.etaxonomy.cdm.persistence.dao.term.IRepresentationDao;

/**
 * @author a.babadshanjan
 * @since 10.09.2008
 */
@Repository
@Deprecated
public class RepresentationDaoImpl
        extends AnnotatableDaoBaseImpl<Representation>
        implements IRepresentationDao {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	public RepresentationDaoImpl() {
		super(Representation.class);
	}

	@Override
    public List<Representation> getAllRepresentations(Integer limit, Integer start) {

	    CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Representation> cq = cb.createQuery(Representation.class);
        Root<Representation> root = cq.from(Representation.class);

        cq.select(root);

        List<Representation> results = addLimitAndStart(
                getSession().createQuery(cq), limit, start)
                .getResultList();
        return results;
	}
}

