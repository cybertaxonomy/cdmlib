/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.IIdentificationKey;
import eu.etaxonomy.cdm.persistence.dao.description.IIdentificationKeyDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DaoBase;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;

@Repository
public class IdentificationKeyDaoImpl extends DaoBase implements IIdentificationKeyDao {

	@Autowired
	@Qualifier("defaultBeanInitializer")
	protected IBeanInitializer defaultBeanInitializer;

	@Override
    public long count() {
		Query<Long> query = getSession().createQuery("SELECT count(key) FROM eu.etaxonomy.cdm.model.description.IIdentificationKey key", Long.class);

        List<Long> result = query.list();
		long total = 0;
		for(long l : result) {
			total += l;
		}
		return total;
	}

	@Override
    public List<IIdentificationKey> list(Integer limit, Integer start, List<String> propertyPaths) {
		Query<IIdentificationKey> query = getSession().createQuery("select key from eu.etaxonomy.cdm.model.description.IIdentificationKey key order by created desc", IIdentificationKey.class);
		addLimitAndStart(query, limit, start);
        List<IIdentificationKey> results = query.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}

	@Override
	public <T extends IIdentificationKey> List<T> findByTaxonomicScope(
	        UUID taxonUuid, @NotNull Class<T> type, Integer pageSize,
			Integer pageNumber, List<String> propertyPaths) {

		@SuppressWarnings("unchecked")
        Query<T> query = getSession().createQuery("SELECT key FROM " + type.getCanonicalName() +" key JOIN key.taxonomicScope ts WHERE ts.uuid = (:taxon_uuid)");
		query.setParameter("taxon_uuid", taxonUuid);
		List<T> results = query.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}

	@Override
	public <T extends IIdentificationKey> long countByTaxonomicScope(UUID taxonUuid, @NotNull Class<T> type) {

		Query<Long> query = getSession().createQuery("SELECT count(key) FROM " + type.getCanonicalName() +" key JOIN key.taxonomicScope ts WHERE ts.uuid = (:taxon_uuid)", Long.class);
		query.setParameter("taxon_uuid", taxonUuid);
		List<Long> list = query.list();
		long count = 0;
		for(long perTypeCount : list){
			count += perTypeCount;
		}
		return count;
	}
}