/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.IIdentificationKey;
import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.description.MultiAccessKey;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.persistence.dao.description.IIdentificationKeyDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DaoBase;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import jakarta.validation.constraints.NotNull;

@Repository
public class IdentificationKeyDaoImpl extends DaoBase implements IIdentificationKeyDao {

	/**
	 * Concrete entity types implementing {@link IIdentificationKey}.
	 * Hibernate 6 cannot resolve attributes (created, taxonomicScope, …) on the
	 * interface itself in polymorphic HQL, so queries run per implementor.
	 */
	@SuppressWarnings("unchecked")
	private static final Class<? extends IIdentificationKey>[] KEY_IMPLS = new Class[] {
			MediaKey.class, PolytomousKey.class, MultiAccessKey.class
	};

	@Autowired
	@Qualifier("defaultBeanInitializer")
	protected IBeanInitializer defaultBeanInitializer;

	@Override
    public long count() {
		long total = 0;
		for (Class<? extends IIdentificationKey> type : KEY_IMPLS) {
			Long n = getSession().createQuery(
					"SELECT count(k) FROM " + type.getName() + " k", Long.class)
					.uniqueResult();
			total += n == null ? 0 : n;
		}
		return total;
	}

	@Override
    public List<IIdentificationKey> list(Integer limit, Integer start, List<String> propertyPaths) {
		List<IIdentificationKey> results = new ArrayList<>();
		for (Class<? extends IIdentificationKey> type : KEY_IMPLS) {
			Query<? extends IIdentificationKey> query = getSession().createQuery(
					"SELECT k FROM " + type.getName() + " k", type);
			results.addAll(query.list());
		}
		results.sort(Comparator.comparing(
				(IIdentificationKey k) -> ((CdmBase) k).getCreated(),
				Comparator.nullsLast(Comparator.reverseOrder())));
		results = applyLimitAndStart(results, limit, start);
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}

	@Override
	public <T extends IIdentificationKey> List<T> findByTaxonomicScope(
	        UUID taxonUuid, @NotNull Class<T> type, Integer pageSize,
			Integer pageNumber, List<String> propertyPaths) {

		List<T> results = new ArrayList<>();
		for (Class<? extends T> impl : resolveTypes(type)) {
			@SuppressWarnings("unchecked")
			Query<T> query = getSession().createQuery(
					"SELECT k FROM " + impl.getName()
							+ " k JOIN k.taxonomicScope ts WHERE ts.uuid = :taxon_uuid");
			query.setParameter("taxon_uuid", taxonUuid);
			results.addAll(query.list());
		}
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}

	@Override
	public <T extends IIdentificationKey> long countByTaxonomicScope(UUID taxonUuid, @NotNull Class<T> type) {
		long count = 0;
		for (Class<? extends T> impl : resolveTypes(type)) {
			Long n = getSession().createQuery(
					"SELECT count(k) FROM " + impl.getName()
							+ " k JOIN k.taxonomicScope ts WHERE ts.uuid = :taxon_uuid",
					Long.class)
					.setParameter("taxon_uuid", taxonUuid)
					.uniqueResult();
			count += n == null ? 0 : n;
		}
		return count;
	}

	@SuppressWarnings("unchecked")
	private <T extends IIdentificationKey> List<Class<? extends T>> resolveTypes(Class<T> type) {
		if (type == null || type.equals(IIdentificationKey.class)) {
			List<Class<? extends T>> list = new ArrayList<>(KEY_IMPLS.length);
			for (Class<? extends IIdentificationKey> impl : KEY_IMPLS) {
				list.add((Class<? extends T>) impl);
			}
			return list;
		}
		return List.of(type);
	}

	private static <T> List<T> applyLimitAndStart(List<T> results, Integer limit, Integer start) {
		int from = start == null || start < 0 ? 0 : start;
		if (from >= results.size()) {
			return new ArrayList<>();
		}
		int to = limit == null || limit < 0 ? results.size() : Math.min(from + limit, results.size());
		return new ArrayList<>(results.subList(from, to));
	}
}
