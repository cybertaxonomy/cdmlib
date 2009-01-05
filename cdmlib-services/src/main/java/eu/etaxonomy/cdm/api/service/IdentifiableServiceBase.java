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

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

public abstract class IdentifiableServiceBase<T extends IdentifiableEntity,DAO extends IIdentifiableDao<T>> extends AnnotatableServiceBase<T,DAO> 
						implements IIdentifiableEntityService<T>{
	@SuppressWarnings("unused")
	private static final  Logger logger = Logger.getLogger(IdentifiableServiceBase.class);

	
	public Pager<Rights> getRights(T t, Integer pageSize, Integer pageNumber) {
        Integer numberOfResults = dao.countRights(t);
		
		List<Rights> results = new ArrayList<Rights>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getRights(t, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<Rights>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<OriginalSource> getSources(T t, Integer pageSize, Integer pageNumber) {
		 Integer numberOfResults = dao.countSources(t);
			
			List<OriginalSource> results = new ArrayList<OriginalSource>();
			if(numberOfResults > 0) { // no point checking again
				results = dao.getSources(t, pageSize, pageNumber); 
			}
			
			return new DefaultPagerImpl<OriginalSource>(pageNumber, numberOfResults, pageSize, results);
	}

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
}
