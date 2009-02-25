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
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.IdentificationKey;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoImpl;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaDao;

/**
 * @author a.babadshanjan
 * @created 08.09.2008
 */
@Repository
public class MediaDaoHibernateImpl extends AnnotatableDaoImpl<Media> 
	implements IMediaDao {

	public MediaDaoHibernateImpl() {
		super(Media.class);
	}

	public int countIdentificationKeys(Set<Taxon> taxonomicScope,	Set<NamedArea> geoScopes) {
		checkNotInPriorView("MediaDaoHibernateImpl.countIdentificationKeys(Set<Taxon> taxonomicScope,	Set<NamedArea> geoScopes)");
		Criteria criteria = getSession().createCriteria(IdentificationKey.class);
		
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
			criteria.createCriteria("geoScopes").add(Restrictions.in("id", geoScopeIds));
		}
		
		criteria.setProjection(Projections.countDistinct("id"));
		
		return (Integer)criteria.uniqueResult();
	}

	public List<IdentificationKey> getIdentificationKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes, Integer pageSize, Integer pageNumber) {
		checkNotInPriorView("MediaDaoHibernateImpl.getIdentificationKeys(Set<Taxon> taxonomicScope, Set<NamedArea> geoScopes, Integer pageSize, Integer pageNumber)");
        Criteria inner = getSession().createCriteria(IdentificationKey.class);
		
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
			inner.createCriteria("geoScopes").add(Restrictions.in("id", geoScopeIds));
		}
		
		inner.setProjection(Projections.distinct(Projections.id()));
		
		Criteria criteria = getSession().createCriteria(IdentificationKey.class);
		criteria.add(Restrictions.in("id", (List<Integer>)inner.list()));
		
		if(pageSize != null) {
			criteria.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	criteria.setFirstResult(pageNumber * pageSize);
		    }
		}
		
		return (List<IdentificationKey>)criteria.list();
	}
}
