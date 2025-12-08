/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.occurrence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Hibernate;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.query.Query;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.occurrence.ICollectionDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCacheWithCode;

@Repository
public class CollectionDaoHibernateImpl extends IdentifiableDaoBase<Collection> implements
		ICollectionDao {

	@SuppressWarnings("unchecked")
    public CollectionDaoHibernateImpl() {
		super(Collection.class);
		indexedClasses = new Class[1];
		indexedClasses[0] = Collection.class;
	}

	@Override
    public List<Collection> getCollectionByCode(String code) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    CriteriaBuilder cb = getCriteriaBuilder();
	        CriteriaQuery<Collection> cq = cb.createQuery(Collection.class);
	        Root<Collection> root = cq.from(Collection.class);

	        cq.select(root);
	        cq.where(predicateStrNotNull(cb, root, "code", code));

	        List<Collection> results = getSession().createQuery(cq).getResultList();
	        return results;

		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(Collection.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.property("code").eq(code));
			return query.getResultList();
		}
	}

	@Override
    public List<UuidAndTitleCache<Collection>> getUuidAndTitleCacheByCode(String codePattern){
        String queryString = "SELECT c.uuid, c.id, c.titleCache, c.code FROM Collection as c WHERE c.code LIKE :code";


        Query<Object[]> query = getSession().createQuery(queryString, Object[].class);
        codePattern = codePattern + "%";
        codePattern = codePattern.replace("*", "%");
        codePattern = codePattern.replace("?", "_");
        query.setParameter("code", codePattern);

        List<Object[]> queryResults = query.list();
        List<UuidAndTitleCache<Collection>> results = new ArrayList<>();
        for (Object[] result: queryResults) {
            results.add(new UuidAndTitleCacheWithCode((UUID)result[0], (Integer)result[1], (String)result[2], (String)result[3]));
        }

        return results;
    }

	@Override
    public List<UuidAndTitleCache<Collection>> getUuidAndTitleCache(Integer limit, String pattern){
        String queryString = "SELECT c.uuid, c.id, c.titleCache, c.code FROM Collection as c WHERE c.titleCache like  :pattern OR c.name like :pattern OR c.townOrLocation like :pattern";


        Query<Object[]> query = getSession().createQuery(queryString, Object[].class);
        pattern = pattern + "%";
        pattern = pattern.replace("*", "%");
        pattern = pattern.replace("?", "_");
        query.setParameter("pattern", pattern);

        List<Object[]> queryResults = query.list();
        List<UuidAndTitleCache<Collection>> results = new ArrayList<>();
        for (Object[] result: queryResults) {
            results.add(new UuidAndTitleCacheWithCode((UUID)result[0], (Integer)result[1], (String)result[2], (String)result[3]));
        }

        return results;
    }

	@Override
	public void rebuildIndex() {
		FullTextSession fullTextSession = Search.getFullTextSession(getSession());

		for(Collection collection : list(null,null)) { // re-index all taxon base

			Hibernate.initialize(collection.getSuperCollection());
			Hibernate.initialize(collection.getInstitute());
			fullTextSession.index(collection);
		}
		fullTextSession.flushToIndexes();
	}
}
