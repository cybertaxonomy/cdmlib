package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotationDao;

@Repository
public class AnnotationDaoImpl extends LanguageStringBaseDaoImpl<Annotation> implements IAnnotationDao {

	public AnnotationDaoImpl() {
		super(Annotation.class);
	}

	public int count(Person commentator, MarkerType status) {
		checkNotInPriorView("AnnotationDaoImpl.count(Person commentator, MarkerType status)");
        Query query = null;
		
		if(status == null) {
			query = getSession().createQuery("select count(annotation) from Annotation annotation where annotation.commentator = :commentator");
		} else {
			query = getSession().createQuery("select count(annotation) from Annotation annotation join annotation.markers marker where annotation.commentator = :commentator and marker.markerType = :status");
			query.setParameter("status",status);
		}
		
		query.setParameter("commentator",commentator);
		
		return ((Long)query.uniqueResult()).intValue();
	}

	public List<Annotation> list(Person commentator, MarkerType status,	Integer pageSize, Integer pageNumber) {
		checkNotInPriorView("AnnotationDaoImpl.list(Person commentator, MarkerType status,	Integer pageSize, Integer pageNumber)");
        Query query = null;
		
		if(status == null) {
			query = getSession().createQuery("select annotation from Annotation annotation where annotation.commentator = :commentator");
		} else {
			query = getSession().createQuery("select annotation from Annotation annotation join annotation.markers marker where annotation.commentator = :commentator and marker.markerType = :status");
			query.setParameter("status",status);
		}
		
		query.setParameter("commentator",commentator);
		
		if(pageSize != null) {
			query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		    	query.setFirstResult(pageNumber * pageSize);
		    }
		}
		
		return (List<Annotation>)query.list();
	}
}
