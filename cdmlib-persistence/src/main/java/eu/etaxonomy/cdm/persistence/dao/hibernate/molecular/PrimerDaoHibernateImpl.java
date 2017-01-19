/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.molecular;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.molecular.Primer;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoImpl;
import eu.etaxonomy.cdm.persistence.dao.molecular.IPrimerDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author pplitzner
 * @date 11.03.2014
 *
 */
@Repository
public class PrimerDaoHibernateImpl extends AnnotatableDaoImpl<Primer> implements IPrimerDao{

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(PrimerDaoHibernateImpl.class);

    /**
     * @param type
     */
    public PrimerDaoHibernateImpl() {
        super(Primer.class);
    }

    @Override
    public List<UuidAndTitleCache<Primer>> getPrimerUuidAndTitleCache() {
        List<UuidAndTitleCache<Primer>> list = new ArrayList<UuidAndTitleCache<Primer>>();
        Session session = getSession();

        Query query = session.createQuery("select uuid, id, label from Primer");

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();

        for(Object[] object : result){
            list.add(new UuidAndTitleCache<Primer>(Primer.class, (UUID) object[0], (Integer)object[1], (String) object[2]));
        }

        return list;
    }

    @Override
    public long countByTitle(String queryString, MatchMode matchmode, List<Criterion> criteria) {
        return countByParam(Primer.class, "label", queryString, matchmode, criteria);
    }

    @Override
    public List<Primer> findByTitle(String queryString, MatchMode matchmode, List<Criterion> criteria,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        return findByParam(Primer.class, "label", queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths);
    }

}
