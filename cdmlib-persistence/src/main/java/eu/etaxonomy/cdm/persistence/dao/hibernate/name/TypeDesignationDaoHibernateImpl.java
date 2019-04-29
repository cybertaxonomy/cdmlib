/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 *
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.SourcedEntityDaoImpl;
import eu.etaxonomy.cdm.persistence.dao.name.ITypeDesignationDao;

/**
 * @author a.mueller
 *
 */
@Repository
public class TypeDesignationDaoHibernateImpl
			extends SourcedEntityDaoImpl<TypeDesignationBase<?>>
            implements ITypeDesignationDao {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TypeDesignationDaoHibernateImpl.class);

	public TypeDesignationDaoHibernateImpl() {
		super((Class)(TypeDesignationBase.class));
	}

	//TODO limit start
	@Override
    public List<TypeDesignationBase<?>> getAllTypeDesignations(Integer limit, Integer start) {
		Criteria crit = getSession().createCriteria(TypeDesignationBase.class);
		if(limit != null){
		    crit.setMaxResults(limit);
		}
		if(start != null){
		    crit.setFirstResult(start);
		}
		@SuppressWarnings("unchecked")
        List<TypeDesignationBase<?>> results = crit.list();
		return results;
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TypeDesignationStatusBase> getTypeDesignationStatusInUse() {
        Query query = getSession().createQuery("select distinct tdb.typeStatus from TypeDesignationBase tdb");
        List<TypeDesignationStatusBase> terms = query.list();
        return terms;
    }

}
