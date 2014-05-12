// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.envers.query.criteria.AuditCriterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.DeleteConfiguratorBase;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.model.view.AuditEventRecord;
import eu.etaxonomy.cdm.persistence.dao.common.AuditEventSort;
import eu.etaxonomy.cdm.persistence.dao.common.IVersionableDao;

public abstract class VersionableServiceBase<T extends VersionableEntity, DAO extends IVersionableDao<T>> extends ServiceBase<T,DAO> implements IVersionableService<T> {
	@Autowired
    protected ICommonService commonService;
	@Transactional(readOnly = true)
	public Pager<AuditEventRecord<T>> pageAuditEvents(T t, Integer pageSize,	Integer pageNumber, AuditEventSort sort, List<String> propertyPaths) {
		Integer numberOfResults = dao.countAuditEvents(t, sort);
			
		List<AuditEventRecord<T>> results = new ArrayList<AuditEventRecord<T>>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getAuditEvents(t, pageSize, pageNumber, sort,propertyPaths);
		}
		
		return new DefaultPagerImpl<AuditEventRecord<T>>(pageNumber, numberOfResults, pageSize, results);
	}

	@Transactional(readOnly = true)
	public AuditEventRecord<T> getNextAuditEvent(T t) {
		return dao.getNextAuditEvent(t);
	}

	@Transactional(readOnly = true)
	public AuditEventRecord<T> getPreviousAuditEvent(T t) {
		return dao.getPreviousAuditEvent(t);
	}
	
	@Transactional(readOnly = true)
	public Pager<AuditEventRecord<T>> pageAuditEvents(Class<? extends T> clazz,AuditEvent from,AuditEvent to, List<AuditCriterion> criteria, Integer pageSize, Integer pageNumber, AuditEventSort sort,List<String> propertyPaths) {
		Integer numberOfResults = dao.countAuditEvents(clazz, from, to, criteria);
		
		List<AuditEventRecord<T>> results = new ArrayList<AuditEventRecord<T>>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getAuditEvents(clazz,from,to,criteria, pageSize, pageNumber, sort,propertyPaths);
		}
		
		return new DefaultPagerImpl<AuditEventRecord<T>>(pageNumber, numberOfResults, pageSize, results);
	}
	
	 /**
     * the basic isDeletable method return false if the object is referenced from any other object.
     */
    
    @Override
    public List<String> isDeletable(T base, DeleteConfiguratorBase config){
    	List<String> result = new ArrayList<String>();
    	Set<CdmBase> references = commonService.getReferencingObjects(base);
    	Iterator<CdmBase> iterator = references.iterator();
    	CdmBase ref;
    	while (iterator.hasNext()){
    		ref = iterator.next();
    		String message = "An object of " + ref.getClass().getName() + " with ID " + ref.getId() + " is referencing the object" ;
    		result.add(message);
    	}
    	return result;
    }

}
