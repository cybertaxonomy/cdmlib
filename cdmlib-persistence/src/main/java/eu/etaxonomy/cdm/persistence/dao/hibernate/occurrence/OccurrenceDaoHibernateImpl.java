/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.occurrence;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;

/**
 * @author a.babadshanjan
 * @created 01.09.2008
 */
@Repository
public class OccurrenceDaoHibernateImpl extends IdentifiableDaoBase<SpecimenOrObservationBase> implements
		IOccurrenceDao {

	static Logger logger = Logger.getLogger(TaxonDaoHibernateImpl.class);

	public OccurrenceDaoHibernateImpl() {
		super(SpecimenOrObservationBase.class);
	}

	public int countDerivationEvents(SpecimenOrObservationBase occurence) {
		Query query = getSession().createQuery("select count(distinct derivationEvent) from DerivationEvent derivationEvent join derivationEvent.originals occurence where occurence = :occurence");
		query.setParameter("occurence", occurence);
		
		return ((Long)query.uniqueResult()).intValue();
	}

	public int countDeterminations(SpecimenOrObservationBase occurence) {
		Query query = getSession().createQuery("select count(determination) from DeterminationEvent determination where determination.identifiedUnit = :occurence");
		
		return ((Long)query.uniqueResult()).intValue();
	}

	public int countMedia(SpecimenOrObservationBase occurence) {
		Query query = getSession().createQuery("select count(media) from SpecimenOrObservationBase occurence join occurence.media media where occurence = :occurence");
		query.setParameter("occurence", occurence);
		
		return ((Long)query.uniqueResult()).intValue();
	}

	public List<DerivationEvent> getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize,Integer pageNumber) {
		Query query = getSession().createQuery("select distinct derivationEvent from DerivationEvent derivationEvent join derivationEvent.originals occurence where occurence = :occurence");
		query.setParameter("occurence", occurence);
		
		if(pageSize != null) {
		    query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		        query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}
		
		return (List<DerivationEvent>)query.list();
	}

	public List<DeterminationEvent> getDeterminations(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber) {
		Query query = getSession().createQuery("select determination from DeterminationEvent determination where determination.identifiedUnit = :occurence");
		query.setParameter("occurence", occurence);
		
		if(pageSize != null) {
		    query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		        query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}
		
		return (List<DeterminationEvent>)query.list();
	}

	public List<Media> getMedia(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber) {
		Query query = getSession().createQuery("select media from SpecimenOrObservationBase occurence join occurence.media media where occurence = :occurence");
		query.setParameter("occurence", occurence);
		
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
}
