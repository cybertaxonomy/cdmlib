/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.occurrence;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.occurrence.ICollectionDao;

@Repository
public class CollectionDaoHibernateImpl extends IdentifiableDaoBase<Collection> implements
		ICollectionDao {

	public CollectionDaoHibernateImpl() {
		super(Collection.class);
		indexedClasses = new Class[1];
		indexedClasses[0] = Collection.class;
	}

	@Override
    public List<Collection> getCollectionByCode(String code) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Criteria crit = getSession().createCriteria(Collection.class);
    		crit.add(Restrictions.eq("code", code));

		    return crit.list();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(Collection.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.property("code").eq(code));
			return query.getResultList();
		}
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
