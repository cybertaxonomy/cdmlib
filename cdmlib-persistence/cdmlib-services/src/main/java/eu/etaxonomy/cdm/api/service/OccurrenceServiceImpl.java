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
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeConfigurator;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeNotSupportedException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.AbstractBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * @author a.babadshanjan
 * @created 01.09.2008
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class OccurrenceServiceImpl extends IdentifiableServiceBase<SpecimenOrObservationBase,IOccurrenceDao> implements IOccurrenceService {

	static private final Logger logger = Logger.getLogger(OccurrenceServiceImpl.class);
	
	@Autowired
	private IDefinedTermDao definedTermDao;	
	
	@Autowired
	private IDescriptionService descriptionService;	
	
	@Autowired
	private AbstractBeanInitializer beanInitializer;
	
	@Autowired
	private ITaxonDao taxonDao;
	
	

	public OccurrenceServiceImpl() {
		logger.debug("Load OccurrenceService Bean");
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache(java.lang.Integer, eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy)
	 */
	@Override
	public void updateTitleCache(Class<? extends SpecimenOrObservationBase> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<SpecimenOrObservationBase> cacheStrategy, IProgressMonitor monitor) {
		if (clazz == null){
			clazz = SpecimenOrObservationBase.class;
		}
		super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
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
		List<? extends DefinedTermBase> terms = this.definedTermDao.findByTitle(WaterbodyOrCountry.class, name, null, null, null, null, null, null) ;
		List<WaterbodyOrCountry> countries = new ArrayList<WaterbodyOrCountry>();
		for (int i=0;i<terms.size();i++){
			countries.add((WaterbodyOrCountry)terms.get(i));
		}
		return countries;
	}

	@Autowired
	protected void setDao(IOccurrenceDao dao) {
		this.dao = dao;
	}

	public Pager<DerivationEvent> getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize,Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countDerivationEvents(occurence);
		
		List<DerivationEvent> results = new ArrayList<DerivationEvent>();
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getDerivationEvents(occurence, pageSize, pageNumber,propertyPaths); 
		}
		
		return new DefaultPagerImpl<DerivationEvent>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<DeterminationEvent> getDeterminations(SpecimenOrObservationBase occurrence, TaxonBase taxonBase, Integer pageSize,Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countDeterminations(occurrence, taxonBase);
		
		List<DeterminationEvent> results = new ArrayList<DeterminationEvent>();
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getDeterminations(occurrence,taxonBase, pageSize, pageNumber, propertyPaths); 
		}
		
		return new DefaultPagerImpl<DeterminationEvent>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<Media> getMedia(SpecimenOrObservationBase occurence,Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countMedia(occurence);
		
		List<Media> results = new ArrayList<Media>();
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getMedia(occurence, pageSize, pageNumber, propertyPaths); 
		}
		
		return new DefaultPagerImpl<Media>(pageNumber, numberOfResults, pageSize, results);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#list(java.lang.Class, eu.etaxonomy.cdm.model.taxon.TaxonBase, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
	 */
	public Pager<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> type, TaxonBase determinedAs, Integer pageSize, Integer pageNumber,	List<OrderHint> orderHints, List<String> propertyPaths) {
		Integer numberOfResults = dao.count(type,determinedAs);
		List<SpecimenOrObservationBase> results = new ArrayList<SpecimenOrObservationBase>();
		pageNumber = pageNumber == null ? 0 : pageNumber;
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			Integer start = pageSize == null ? 0 : pageSize * pageNumber;
			results = dao.list(type,determinedAs, pageSize, start, orderHints,propertyPaths);
		}
		return new DefaultPagerImpl<SpecimenOrObservationBase>(pageNumber, numberOfResults, pageSize, results);	
	}

	@Override
	public List<UuidAndTitleCache<DerivedUnitBase>> getDerivedUnitBaseUuidAndTitleCache() {
		return dao.getDerivedUnitBaseUuidAndTitleCache();
	}

	@Override
	public List<UuidAndTitleCache<FieldObservation>> getFieldObservationUuidAndTitleCache() {
		return dao.getFieldObservationUuidAndTitleCache();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#getDerivedUnitFacade(eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase)
	 */
	@Override
	public DerivedUnitFacade getDerivedUnitFacade(DerivedUnitBase derivedUnit, List<String> propertyPaths) throws DerivedUnitFacadeNotSupportedException {
		derivedUnit = (DerivedUnitBase<?>)dao.load(derivedUnit.getUuid(), null);
		DerivedUnitFacadeConfigurator config = DerivedUnitFacadeConfigurator.NewInstance();
		config.setThrowExceptionForNonSpecimenPreservationMethodRequest(false);
		DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade.NewInstance(derivedUnit, config);
		beanInitializer.initialize(derivedUnitFacade, propertyPaths);
		return derivedUnitFacade;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#listDerivedUnitFacades(eu.etaxonomy.cdm.model.description.DescriptionBase, java.util.List)
	 */
	@Override
	public List<DerivedUnitFacade> listDerivedUnitFacades(
			DescriptionBase description, List<String> propertyPaths) {
		
		List<DerivedUnitFacade> derivedUnitFacadeList = new ArrayList<DerivedUnitFacade>();
		IndividualsAssociation tempIndividualsAssociation;
		SpecimenOrObservationBase tempSpecimenOrObservationBase;
		List<DescriptionElementBase> elements = descriptionService.listDescriptionElements(description, null, IndividualsAssociation.class, null, 0, Arrays.asList(new String []{"associatedSpecimenOrObservation"}));
		for(DescriptionElementBase element : elements){
			if(element instanceof IndividualsAssociation){
				tempIndividualsAssociation = (IndividualsAssociation)element;
				if(tempIndividualsAssociation.getAssociatedSpecimenOrObservation() != null){
					tempSpecimenOrObservationBase = HibernateProxyHelper.deproxy(tempIndividualsAssociation.getAssociatedSpecimenOrObservation(), SpecimenOrObservationBase.class);
					if(tempSpecimenOrObservationBase instanceof DerivedUnitBase){
						try {
							derivedUnitFacadeList.add(DerivedUnitFacade.NewInstance((DerivedUnitBase)tempSpecimenOrObservationBase));
						} catch (DerivedUnitFacadeNotSupportedException e) {
							logger.warn(tempIndividualsAssociation.getAssociatedSpecimenOrObservation().getTitleCache() + " : " +e.getMessage());
						}
					}
				}
				
			}
		}
		
		beanInitializer.initializeAll(derivedUnitFacadeList, propertyPaths);
			
		return derivedUnitFacadeList;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#listSpecimenOrObservationsFor(java.lang.Class, eu.etaxonomy.cdm.model.taxon.Taxon, java.util.List)
	 */
	@Override
	public <T extends SpecimenOrObservationBase> List<T> listByAnyAssociation(Class<T> type,
			Taxon associatedTaxon, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths)
	{
		
		associatedTaxon = (Taxon) taxonDao.load(associatedTaxon.getUuid());
		return dao.listByAnyAssociation(type, associatedTaxon, limit, start, orderHints, propertyPaths);
		
	}
	
	
}
