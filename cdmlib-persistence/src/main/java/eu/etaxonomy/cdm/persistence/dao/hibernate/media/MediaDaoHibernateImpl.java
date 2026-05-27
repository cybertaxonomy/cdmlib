/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.media;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Hibernate;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.query.Query;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.molecular.PhylogeneticTree;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.common.OperationNotSupportedInPriorViewException;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaDao;

/**
 * @author a.babadshanjan
 * @since 08.09.2008
 */
@Repository
public class MediaDaoHibernateImpl
        extends IdentifiableDaoBase<Media>
        implements IMediaDao {

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
    public long countMediaKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScope) {

	    AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {

		    CriteriaBuilder cb = getCriteriaBuilder();
		    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		    Root<MediaKey> root = cq.from(MediaKey.class);

		    List<Predicate> predicates = getMediaKeyPredicates(taxonomicScope, geoScope, root);

		    cq.select(cb.countDistinct(root))
		      .where(predicateAnd(cb, predicates));

		    return getSession().createQuery(cq).getSingleResult();

		} else {
			if((taxonomicScope == null || taxonomicScope.isEmpty()) && (geoScope == null || geoScope.isEmpty())) {
				AuditQuery query = makeAuditQuery(MediaKey.class,auditEvent);
				query.addProjection(AuditEntity.id().countDistinct());
				return (Long)query.getSingleResult();
			} else {
				throw new OperationNotSupportedInPriorViewException("countMediaKeys(Set<Taxon> taxonomicScope,	Set<NamedArea> geoScopes)");
			}
		}
	}

    private List<Predicate> getMediaKeyPredicates(Set<Taxon> taxonomicScope, Set<NamedArea> geoScope,
            Root<MediaKey> root) {
        List<Predicate> predicates = new ArrayList<>();

        //taxonomic scope
        if(!CdmUtils.isNullSafeEmpty(taxonomicScope)) {
            Set<Integer> taxonomicScopeIds = taxonomicScope.stream()
                .map(Taxon::getId)
                .collect(Collectors.toSet());
            //TODO unclear if generics for path are correct, but seems to be irrelevant here
            CollectionJoin<MediaKey, Taxon> path = root.joinCollection("taxonomicScope", JoinType.INNER);
            Predicate predicate = predicateIn(path, "id", taxonomicScopeIds);
            predicates.add(predicate);
        }

          //taxonomic scope
        if(!CdmUtils.isNullSafeEmpty(geoScope)) {
            Set<Integer> geoScopeIds = geoScope.stream()
                .map(NamedArea::getId)
                .collect(Collectors.toSet());
            //TODO unclear if generics for path are correct, but seems to be irrelevant here
            CollectionJoin<MediaKey, Taxon> path = root.joinCollection("geographicalScope", JoinType.INNER);
            Predicate predicate = predicateIn(path, "id", geoScopeIds);
            predicates.add(predicate);
        }
        return predicates;
    }

	@Override
    public List<MediaKey> getMediaKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

	    AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    CriteriaBuilder cb = getCriteriaBuilder();
		    CriteriaQuery<MediaKey> cq = cb.createQuery(MediaKey.class);
		    Root<MediaKey> root = cq.from(MediaKey.class);

		    List<Predicate> predicates = getMediaKeyPredicates(taxonomicScope, geoScopes, root);

		    cq.select(root)
		      .distinct(true)
		      .where(predicateAnd(cb, predicates))
		      .orderBy(ordersFrom(cb, root, null));

		    List<MediaKey> results = addPageSizeAndNumber(
		            getSession().createQuery(cq), pageSize, pageNumber)
		           .getResultList();
		    defaultBeanInitializer.initializeAll(results, propertyPaths);
		    return deduplicateResult(results);
		} else {
			if((taxonomicScope == null || taxonomicScope.isEmpty()) && (geoScopes == null || geoScopes.isEmpty())) {
				AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(MediaKey.class,auditEvent.getRevisionNumber());

				addPageSizeAndNumber(query, pageSize, pageNumber);
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
		Query<Rights> query = getSession().createQuery("SELECT rights FROM Media media JOIN media.rights rights WHERE media = :media", Rights.class);
		query.setParameter("media",media);
		addPageSizeAndNumber(query, pageSize, pageNumber);
		List<Rights> results = query.list();
		defaultBeanInitializer.initializeAll(results, propertyPaths);
		return results;
	}

    @Override
    public long countRights(Media media) {
		checkNotInPriorView("MediaDaoHibernateImpl.countRights(Media t)");
		Query<Long> query = getSession().createQuery("select count(rights) from Media media join media.rights rights where media = :media", Long.class);
		query.setParameter("media",media);
		return query.uniqueResult();
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