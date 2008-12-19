package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoImpl;

@Repository
public class DescriptionElementDaoImpl extends AnnotatableDaoImpl<DescriptionElementBase> implements IDescriptionElementDao {

	public int countMedia(DescriptionElementBase descriptionElement) {
		Query query = getSession().createQuery("select count(media) from DescriptionElementBase descriptionElement join descriptionElement.media media where descriptionElement = :descriptionElement");
		query.setParameter("descriptionElement", descriptionElement);
		
		return ((Long)query.uniqueResult()).intValue();
	}

	public int countTextData(String queryString) {
		throw new UnsupportedOperationException("Free text searching isn't implemented yet, sorry!");
	}

	public List<Media> getMedia(DescriptionElementBase descriptionElement,	Integer pageSize, Integer pageNumber) {
		Query query = getSession().createQuery("select media from DescriptionElementBase descriptionElement join descriptionElement.media media where descriptionElement = :descriptionElement");
		query.setParameter("descriptionElement", descriptionElement);
		
		if(pageSize != null) {
		    query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		        query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}
		
		return (List<Media>)query.list();
	}

	public List<TextData> searchTextData(String queryString, Integer pageSize,	Integer pageNumber) {
		throw new UnsupportedOperationException("Free text searching isn't implemented yet, sorry!");
	}

}
