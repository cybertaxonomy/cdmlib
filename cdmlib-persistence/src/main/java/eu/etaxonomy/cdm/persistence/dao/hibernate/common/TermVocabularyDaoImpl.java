/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;

/**
 * @author a.mueller
 *
 */
@Repository
public class TermVocabularyDaoImpl extends VersionableDaoBase<TermVocabulary<DefinedTermBase>> implements
		ITermVocabularyDao {

	
	/**
	 * @param type
	 */
	public TermVocabularyDaoImpl() {
		super((Class)TermVocabulary.class);
	}

	public int countTerms(TermVocabulary termVocabulary) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = getSession().createQuery("select count(term) from DefinedTermBase term where term.vocabulary = :vocabulary");
		    query.setParameter("vocabulary", termVocabulary);
		
		    return ((Long)query.uniqueResult()).intValue();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			query.addProjection(AuditEntity.id().count("id"));
			query.add(AuditEntity.relatedId("vocabulary").eq(termVocabulary.getId()));
			return (Integer)query.getSingleResult();
		}
	}

	public <T extends DefinedTermBase> List<T> getTerms(TermVocabulary<T> termVocabulary, Integer pageSize, Integer pageNumber) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
    		Query query = getSession().createQuery("select term from DefinedTermBase term where term.vocabulary = :vocabulary");
	    	query.setParameter("vocabulary", termVocabulary);
		
		    if(pageSize != null) {
			    query.setMaxResults(pageSize);
		        if(pageNumber != null) {
		    	    query.setFirstResult(pageNumber * pageSize);
		        }
		    }
		
		    return (List<T>)query.list();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("vocabulary").eq(termVocabulary.getId()));
			
			if(pageSize != null) {
			    query.setMaxResults(pageSize);
		        if(pageNumber != null) {
		    	    query.setFirstResult(pageNumber * pageSize);
		        }
		    }
			
			return (List<T>)query.getResultList();
		}
	}

	public <T extends DefinedTermBase> TermVocabulary<T> findByUri(String termSourceUri, Class<T> clazz) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
    		Query query = getSession().createQuery("select vocabulary from TermVocabulary vocabulary where vocabulary.termSourceUri= :termSourceUri");
	    	query.setParameter("termSourceUri", termSourceUri);
		 
		    return (TermVocabulary<T>)query.uniqueResult();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(type,auditEvent.getRevisionNumber());
			query.add(AuditEntity.property("termSourceUri").eq(termSourceUri));
			
			return (TermVocabulary<T>)query.getSingleResult();
		}
	}


}
