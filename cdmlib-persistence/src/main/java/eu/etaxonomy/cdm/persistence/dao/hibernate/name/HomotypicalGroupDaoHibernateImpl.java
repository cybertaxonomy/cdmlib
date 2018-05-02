/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.name.IHomotypicalGroupDao;

/**
 * @author a.babadshanjan
 * @since 24.09.2008
 */
@Repository
public class HomotypicalGroupDaoHibernateImpl extends CdmEntityDaoBase<HomotypicalGroup> implements IHomotypicalGroupDao {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(HomotypicalGroupDaoHibernateImpl.class);

	public HomotypicalGroupDaoHibernateImpl() {
		super(HomotypicalGroup.class);
	}

	@Override
	public <T extends TypeDesignationBase> List<T> getTypeDesignations(
			HomotypicalGroup homotypicalGroup, Class<T> type,
			TypeDesignationStatusBase status, Integer pageSize,
			Integer pageNumber, List<String> propertyPaths) {

		// checkNotInPriorView("getTypeDesignations(HomotypicalGroup homotypicalGroup,TypeDesignationStatusBase status, Integer pageSize, Integer pageNumber,	List<String> propertyPaths)");
		Query query = null;
		String queryString = "select designation from TypeDesignationBase designation join designation.typifiedNames name join name.homotypicalGroup homotypicalGroup where homotypicalGroup = :homotypicalGroup";

		if(status != null) {
			queryString +=  " and designation.typeStatus = :status";
		}
		if(type != null){
			queryString +=  " and designation.class = :type";
		}

		query = getSession().createQuery(queryString);

		if(status != null) {
			query.setParameter("status", status);
		}
		if(type != null){
			query.setParameter("type", type.getSimpleName());
		}

		query.setParameter("homotypicalGroup",homotypicalGroup);

		if(pageSize != null) {
			query.setMaxResults(pageSize);
			if(pageNumber != null) {
				query.setFirstResult(pageNumber * pageSize);
			} else {
				query.setFirstResult(0);
			}
		}
		@SuppressWarnings("unchecked")
        List<T> result = defaultBeanInitializer.initializeAll(query.list(), propertyPaths);
		return result;
	}



}
