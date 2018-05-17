/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.media;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.molecular.PhylogeneticTree;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.OperationNotSupportedInPriorViewException;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaDao;

/**
 * @author a.babadshanjan
 * @since 08.09.2008
 */
@Repository
public class MediaDaoHibernateImpl extends IdentifiableDaoBase<Media> implements IMediaDao {

	protected String getDefaultField() {
		return "title.text";
	}

	public MediaDaoHibernateImpl() {
		super(Media.class);
		indexedClasses = new Class[3];
		indexedClasses[0] = Media.class;
		indexedClasses[1] = MediaKey.class;
		indexedClasses[2] = PhylogeneticTree.class;
	}

	@Override
    public int countMediaKeys(Set<Taxon> taxonomicScope,	Set<NamedArea> geoScopes) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = getSession().createCriteria(MediaKey.class);

			if(taxonomicScope != null && !taxonomicScope.isEmpty()) {
				Set<Integer> taxonomicScopeIds = new HashSet<Integer>();
				for(Taxon n : taxonomicScope) {
					taxonomicScopeIds.add(n.getId());
				}
				criteria.createCriteria("taxonomicScope").add(Restrictions.in("id", taxonomicScopeIds));
			}

			if(geoScopes != null && !geoScopes.isEmpty()) {
				Set<Integer> geoScopeIds = new HashSet<Integer>();
				for(NamedArea n : geoScopes) {
					geoScopeIds.add(n.getId());
				}
				criteria.createCriteria("geographicalScope").add(Restrictions.in("id", geoScopeIds));
			}

			criteria.setProjection(Projections.countDistinct("id"));

			return ((Number)criteria.uniqueResult()).intValue();
		} else {
			if((taxonomicScope == null || taxonomicScope.isEmpty()) && (geoScopes == null || geoScopes.isEmpty())) {
				AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(MediaKey.class,auditEvent.getRevisionNumber());
				query.addProjection(AuditEntity.id().countDistinct());
				return ((Long)query.getSingleResult()).intValue();
			} else {
				throw new OperationNotSupportedInPriorViewException("countMediaKeys(Set<Taxon> taxonomicScope,	Set<NamedArea> geoScopes)");
			}
		}
	}

	@Override
    public List<MediaKey> getMediaKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria inner = getSession().createCriteria(MediaKey.class);

			if(taxonomicScope != null && !taxonomicScope.isEmpty()) {
				Set<Integer> taxonomicScopeIds = new HashSet<Integer>();
				for(Taxon n : taxonomicScope) {
					taxonomicScopeIds.add(n.getId());
				}
				inner.createCriteria("taxonomicScope").add(Restrictions.in("id", taxonomicScopeIds));
			}

			if(geoScopes != null && !geoScopes.isEmpty()) {
				Set<Integer> geoScopeIds = new HashSet<Integer>();
				for(NamedArea n : geoScopes) {
					geoScopeIds.add(n.getId());
				}
				inner.createCriteria("geographicalScope").add(Restrictions.in("id", geoScopeIds));
			}

			inner.setProjection(Projections.distinct(Projections.id()));

			Criteria criteria = getSession().createCriteria(MediaKey.class);
			criteria.add(Restrictions.in("id", inner.list()));

			if(pageSize != null) {
				criteria.setMaxResults(pageSize);
				if(pageNumber != null) {
					criteria.setFirstResult(pageNumber * pageSize);
				}
			}

			List<MediaKey> results = criteria.list();

			defaultBeanInitializer.initializeAll(results, propertyPaths);

			return results;
		} else {
			if((taxonomicScope == null || taxonomicScope.isEmpty()) && (geoScopes == null || geoScopes.isEmpty())) {
				AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(MediaKey.class,auditEvent.getRevisionNumber());

				if(pageSize != null) {
			        query.setMaxResults(pageSize);
			        if(pageNumber != null) {
			            query.setFirstResult(pageNumber * pageSize);
			        } else {
			    	    query.setFirstResult(0);
			        }
			    }
				List<MediaKey> results = query.getResultList();
				defaultBeanInitializer.initializeAll(results, propertyPaths);
				return results;
			} else {
				throw new OperationNotSupportedInPriorViewException("getMediaKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes, Integer pageSize, Integer pageNumber, List<String> propertyPaths)");
			}
		}
	}

	@Override
    public List<Rights> getRights(Media media, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		checkNotInPriorView("MediaDaoHibernateImpl.getRights(Media t, Integer pageSize, Integer pageNumber, List<String> propertyPaths)");
		Query query = getSession().createQuery("select rights from Media media join media.rights rights where media = :media");
		query.setParameter("media",media);
		setPagingParameter(query, pageSize, pageNumber);
		List<Rights> results = query.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}

	@Override
    public int countRights(Media media) {
		checkNotInPriorView("MediaDaoHibernateImpl.countRights(Media t)");
		Query query = getSession().createQuery("select count(rights) from Media media join media.rights rights where media = :media");
		query.setParameter("media",media);
		return ((Long)query.uniqueResult()).intValue();
	}



	@Override
	public void rebuildIndex() {
        FullTextSession fullTextSession = Search.getFullTextSession(getSession());

		for(Media media : list(null,null)) { // re-index all media
			Hibernate.initialize(media.getTitle());
			Hibernate.initialize(media.getAllDescriptions());
			Hibernate.initialize(media.getArtist());
			fullTextSession.index(media);
		}
		fullTextSession.flushToIndexes();
	}

}
