/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.reference.IReference;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoImpl;
import eu.etaxonomy.cdm.persistence.dao.name.IRegistrationDao;

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
    public List<Registration> list(IReference reference, Integer limit, Integer start,
            List<String> propertyPaths) {

        if (reference != null && reference.getId() == 0){
            return Collections.emptyList();
        }

        String hql = "SELECT DISTINCT r "
                + " FROM Registration r LEFT JOIN r.typeDesignations desig "
                + "     LEFT JOIN r.name n "
                + " WHERE (n IS NOT NULL AND n.nomenclaturalReference =:ref)"
                + "     OR desig.citation =:ref "
                + " ORDER BY r.id ";
        if (reference == null){
            hql = "SELECT DISTINCT r "
                    + " FROM Registration r LEFT JOIN r.typeDesignations desig "
                    + "    LEFT JOIN r.name n"
                    + " WHERE (r.name IS NULL AND size(r.typeDesignations) = 0 ) "
                    + "     OR (n IS NOT NULL AND r.name.nomenclaturalReference IS NULL ) "
                    + "     OR (size(r.typeDesignations) > 0 AND desig.citation IS NULL )  "
                    + " ORDER BY r.id ";
        }

        Query query = getSession().createQuery(hql);
        if (reference != null){
            query.setParameter("ref", reference);
        }

        // TODO complete ....
        if(limit != null /*&&  !doCount*/) {
            query.setMaxResults(limit);
            if(start != null) {
                query.setFirstResult(start);
            }
        }

        //TODO order hints do not work with queries

        List<Registration> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);

        return results;
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
