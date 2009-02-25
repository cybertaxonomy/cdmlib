package eu.etaxonomy.cdm.persistence.dao.hibernate.occurrence;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
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
	}

	public List<Collection> getCollectionByCode(String code) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Criteria crit = getSession().createCriteria(Collection.class);
    		crit.add(Restrictions.eq("code", code));
		
		    return (List<Collection>)crit.list();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(Collection.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.property("code").eq(code));
			return (List<Collection>)query.getResultList();
		}
	}
}
