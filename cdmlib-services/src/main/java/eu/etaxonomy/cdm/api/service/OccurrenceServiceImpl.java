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
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.ICollectionDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.babadshanjan
 * @created 01.09.2008
 */
@Service
@Transactional(readOnly = true)
public class OccurrenceServiceImpl extends IdentifiableServiceBase<SpecimenOrObservationBase,IOccurrenceDao> implements IOccurrenceService {

	static private final Logger logger = Logger.getLogger(OccurrenceServiceImpl.class);
	
	@Autowired
	private IDefinedTermDao definedTermDao;
	
	@Autowired
	private IOccurrenceDao occurenceDao;
	
	@Autowired
	private ICollectionDao collectionDao;
	

	public OccurrenceServiceImpl() {
		logger.debug("Load OccurrenceService Bean");
	}
	
	/**
	 * FIXME Candidate for harmonization
	 * list
	 *  (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#getAllspecimenOrObservationBases(int, int)
	 */
	public List<SpecimenOrObservationBase> getAllSpecimenOrObservationBases(
			int limit, int start) {
		return dao.list(limit, start);
	}

	/**
	 * FIXME Candidate for harmonization
	 * save(Set<SpecimenOrObservationBase> specimens)
	 *  (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#saveSpecimenOrObservationBaseAll(java.util.Collection)
	 */
	@Transactional(readOnly = false)
	public Map<UUID, ? extends SpecimenOrObservationBase> saveSpecimenOrObservationBaseAll(
			Collection<? extends SpecimenOrObservationBase> specimenOrObservationBaseCollection) {
		return saveCdmObjectAll(specimenOrObservationBaseCollection);
	}

	/**
	 * FIXME Candidate for harmonization
	 * save
	 */
	@Transactional(readOnly = false)
	public UUID saveSpecimenOrObservationBase(
			SpecimenOrObservationBase specimenOrObservationBase) {
		return super.saveCdmObject(specimenOrObservationBase);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#generateTitleCache()
	 */
	public void generateTitleCache() {
		// TODO Auto-generated method stub
		logger.warn("Not yet implemented");
	}

	/**
	 * FIXME Candidate for harmonization
	 * move to termService
	 */
	public WaterbodyOrCountry getCountryByIso(String iso639) {
		return this.definedTermDao.getCountryByIso(iso639);
		
	}

	/**
	 * FIXME Candidate for harmonization
	 * move to termService
	 */
	public List<WaterbodyOrCountry> getWaterbodyOrCountryByName(String name) {
		List<? extends DefinedTermBase> terms = this.definedTermDao.getDefinedTermByRepresentationText(name, WaterbodyOrCountry.class);
		List<WaterbodyOrCountry> countries = new ArrayList<WaterbodyOrCountry>();
		for (int i=0;i<terms.size();i++){
			countries.add((WaterbodyOrCountry)terms.get(i));
		}
		return countries;
	}
	
	/**
	 * FIXME Candidate for harmonization
	 * move to collectionService
	 */
	public List<eu.etaxonomy.cdm.model.occurrence.Collection> searchCollectionByCode(String code) {
		return this.collectionDao.getCollectionByCode(code);
	}

	@Autowired
	protected void setDao(IOccurrenceDao dao) {
		this.dao = dao;
	}

	public Pager<DerivationEvent> getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize,Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countDerivationEvents(occurence);
		
		List<DerivationEvent> results = new ArrayList<DerivationEvent>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getDerivationEvents(occurence, pageSize, pageNumber,propertyPaths); 
		}
		
		return new DefaultPagerImpl<DerivationEvent>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<DeterminationEvent> getDeterminations(SpecimenOrObservationBase occurence, Integer pageSize,Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countDeterminations(occurence);
		
		List<DeterminationEvent> results = new ArrayList<DeterminationEvent>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getDeterminations(occurence, pageSize, pageNumber, propertyPaths); 
		}
		
		return new DefaultPagerImpl<DeterminationEvent>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<Media> getMedia(SpecimenOrObservationBase occurence,Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countMedia(occurence);
		
		List<Media> results = new ArrayList<Media>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getMedia(occurence, pageSize, pageNumber, propertyPaths); 
		}
		
		return new DefaultPagerImpl<Media>(pageNumber, numberOfResults, pageSize, results);
	}

	/**
	 * FIXME Candidate for harmonization
	 * collectionService.save
	 */
	public UUID saveCollection(eu.etaxonomy.cdm.model.occurrence.Collection collection) {
		return collectionDao.save(collection);
	}

	public Pager<SpecimenOrObservationBase> search(Class<? extends SpecimenOrObservationBase> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.count(clazz,queryString);
		
		List<SpecimenOrObservationBase> results = new ArrayList<SpecimenOrObservationBase>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.search(clazz,queryString, pageSize, pageNumber, orderHints, propertyPaths); 
		}
		
		return new DefaultPagerImpl<SpecimenOrObservationBase>(pageNumber, numberOfResults, pageSize, results);
	}	
}
