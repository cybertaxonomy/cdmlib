/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.reference.IReference;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoImpl;
import eu.etaxonomy.cdm.persistence.dao.name.IRegistrationDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.kohlbecker
 * @since May 2, 2017
 *
 */
@Repository
public class RegistrationDaoHibernateImpl extends AnnotatableDaoImpl<Registration> implements IRegistrationDao {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(RegistrationDaoHibernateImpl.class);

    /**
     * @param type
     */
    public RegistrationDaoHibernateImpl() {
        super(Registration.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Registration> list(Integer limit, Integer start, IReference reference, List<OrderHint> orderHints,
            List<String> propertyPaths) {

        Query query = getSession().createQuery("from Registration order by uuid");
        // TODO complete ....
        query.setFirstResult(start);
        query.setMaxResults(limit);

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer count(IReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

}
