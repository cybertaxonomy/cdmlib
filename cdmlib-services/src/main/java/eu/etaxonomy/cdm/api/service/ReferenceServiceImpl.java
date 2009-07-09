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

	/**
	 * FIXME Candidate for harmonization
	 * find
	 */
	public ReferenceBase getReferenceByUuid(UUID uuid) {
		return super.getCdmObjectByUuid(uuid); 
	}

	/**
	 * FIXME Candidate for harmonization
	 * getByTitle?
	 */
	public List<ReferenceBase> getReferencesByTitle(String title) {
		return super.findCdmObjectsByTitle(title);
	}
	
	public List<ReferenceBase> getReferencesByTitle(String title, Class<ReferenceBase> clazz) {
		return super.findCdmObjectsByTitle(title, clazz);
	}
	
	/**
	 * FIXME Candidate for harmonization
	 * save
	 */
	@Transactional(readOnly = false)
	public UUID saveReference(ReferenceBase reference) {
		return super.saveCdmObject(reference);
	}

	/**
	 * FIXME Candidate for harmonization
	 * save(Set<Reference> references)
	 */
	@Transactional(readOnly = false)
	public Map<UUID, ReferenceBase> saveReferenceAll(Collection<ReferenceBase> referenceCollection){
		return saveCdmObjectAll(referenceCollection);
	}

	/**
	 * FIXME Candidate for harmonization
	 * list()
	 *  (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IReferenceService#getAllReferences(int, int)
	 */
	@Deprecated
	public List<ReferenceBase> getAllReferences(int limit, int start){
			return dao.list(limit, start);
	}
	
	/**
	 * FIXME Candidate for harmonization
	 * list
	 *  (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IReferenceService#getAllReferences(java.lang.Integer, java.lang.Integer)
	 */
	public Pager<ReferenceBase> getAllReferences(Integer pageSize, Integer pageNumber, List<OrderHint> orderHints) {
        Integer numberOfResults = dao.count();
		
		List<ReferenceBase> results = new ArrayList<ReferenceBase>();
		if(numberOfResults > 0) { // no point checking again
			Integer start = pageSize == null ? 0 : pageSize * (pageNumber - 1);
			results = dao.list(pageSize, start, orderHints); //TODO implement pager like method in dao?
		}
		return new DefaultPagerImpl<ReferenceBase>(pageNumber, numberOfResults, pageSize, results);
	}

	/**
	 * FIXME Candidate for harmonization
	 * list
	 */
	public Pager<ReferenceBase> getAllReferences(Integer pageSize, Integer pageNumber) {
		return getAllReferences(pageSize, pageNumber, null);
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
}
