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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;


@Service
@Transactional(readOnly = true)
public class ReferenceServiceImpl extends IdentifiableServiceBase<ReferenceBase,IReferenceDao> implements IReferenceService {
	
	static Logger logger = Logger.getLogger(ReferenceServiceImpl.class);

	/**
	 * Constructor
	 */
	public ReferenceServiceImpl(){
		if (logger.isDebugEnabled()) { logger.debug("Load ReferenceService Bean"); }
	}

	public void generateTitleCache() {
		logger.warn("Not yet implemented");
		// TODO Auto-generated method stub
		
	}

	@Autowired
	protected void setDao(IReferenceDao dao) {
		this.dao = dao;
	}
	
	public Pager<ReferenceBase> search(Class<? extends ReferenceBase> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.count(clazz,queryString);
		
		List<ReferenceBase> results = new ArrayList<ReferenceBase>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.search(clazz,queryString, pageSize, pageNumber, orderHints, propertyPaths); 
		}
		
		return new DefaultPagerImpl<ReferenceBase>(pageNumber, numberOfResults, pageSize, results);
	}

	public List<UuidAndTitleCache<ReferenceBase>> getUuidAndTitle() {
		
		return dao.getUuidAndTitle();
	}
	
	public List<ReferenceBase> getAllReferencesForPublishing(){
		return dao.getAllNotNomenclaturalReferencesForPublishing();
	}

	public List<ReferenceBase> getAllNomenclaturalReferences() {
		
		return dao.getAllNomenclaturalReferences();
	}

}
