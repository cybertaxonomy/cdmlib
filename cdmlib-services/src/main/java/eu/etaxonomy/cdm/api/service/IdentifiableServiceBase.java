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
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.IIdentifiableEntityServiceConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Transactional(readOnly = true)
public abstract class IdentifiableServiceBase<T extends IdentifiableEntity,DAO extends IIdentifiableDao<T>> extends AnnotatableServiceBase<T,DAO> 
						implements IIdentifiableEntityService<T>{
	@SuppressWarnings("unused")
	private static final  Logger logger = Logger.getLogger(IdentifiableServiceBase.class);

	
	public Pager<Rights> getRights(T t, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countRights(t);
		
		List<Rights> results = new ArrayList<Rights>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getRights(t, pageSize, pageNumber,propertyPaths); 
		}
		
		return new DefaultPagerImpl<Rights>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<OriginalSource> getSources(T t, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		 Integer numberOfResults = dao.countSources(t);
			
			List<OriginalSource> results = new ArrayList<OriginalSource>();
			if(numberOfResults > 0) { // no point checking again
				results = dao.getSources(t, pageSize, pageNumber,propertyPaths); 
			}
			
			return new DefaultPagerImpl<OriginalSource>(pageNumber, numberOfResults, pageSize, results);
	}

	protected List<T> findByTitle(IIdentifiableEntityServiceConfigurator config){
		return ((IIdentifiableDao)dao).findByTitle(config.getTitleSearchString(),
				config.getMatchMode(), 0, -1, null);
		// TODO: Implement parameters pageSize, pageNumber, and criteria
	}
	/**
	 * FIXME Candidate for harmonization
	 * Given that this method is strongly typed, and generic, could we not simply expose it as
	 * List<T> findByTitle(String title) as it is somewhat less cumbersome. Admittedly, I don't 
	 * understand what is going on with the configurators etc. so maybe there is a good reason for
	 * the design of this method. 
	 * @param title
	 * @return
	 */
	protected List<T> findCdmObjectsByTitle(String title){
		return ((IIdentifiableDao)dao).findByTitle(title);
	}
	
	protected List<T> findCdmObjectsByTitle(String title, Class<T> clazz){
		return ((IIdentifiableDao)dao).findByTitleAndClass(title, clazz);
	}
	protected List<T> findCdmObjectsByTitle(String title, CdmBase sessionObject){
		return ((IIdentifiableDao)dao).findByTitle(title, sessionObject);
	}
	
	/*
	 * TODO - Migrated from CommonServiceBase
	 *  (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ICommonService#getSourcedObjectById(java.lang.String, java.lang.String)
	 */
	public ISourceable getSourcedObjectByIdInSource(Class clazz, String idInSource, String idNamespace) {
		ISourceable result = null;

		List<T> list = dao.findOriginalSourceByIdInSource(idInSource, idNamespace);
		if (! list.isEmpty()){
			result = list.get(0);
		}
		return result;
	}

      public Pager<T> findByTitle(Class<? extends T> clazz, String queryString,MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		 Integer numberOfResults = dao.countByTitle(clazz, queryString, matchmode, criteria);
			
		 List<T> results = new ArrayList<T>();
		 if(numberOfResults > 0) { // no point checking again
				results = dao.findByTitle(clazz, queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths); 
		 }
			
		  return new DefaultPagerImpl<T>(pageNumber, numberOfResults, pageSize, results);
	}
}
