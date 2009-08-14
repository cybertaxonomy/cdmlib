// $Id$
/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.babadshanjan
 * @created 01.09.2008
 */
public interface IOccurrenceService extends IIdentifiableEntityService<SpecimenOrObservationBase> {

	/**
	 * FIXME candidate for harmonization?
	 * Computes all specimen or observation bases.
	 * @param limit
	 * @param start
	 * @return
	 */
	public abstract List<SpecimenOrObservationBase> getAllSpecimenOrObservationBases(int limit, int start);

	/**
	 * FIXME candidate for harmonization? 
	 * Saves a collection of specimen or observation bases.
	 * @return Map with UUID as key and SpecimenOrObservationBase as value.
	 */
	public abstract Map<UUID, ? extends SpecimenOrObservationBase> 
	saveSpecimenOrObservationBaseAll(java.util.Collection<? extends SpecimenOrObservationBase> specimenOrObservationBaseCollection);

	/** 
	 * FIXME candidate for harmonizaion?
	 * save a specimen or observation and return its UUID
	 */
	public abstract UUID saveSpecimenOrObservationBase (SpecimenOrObservationBase specimenOrObservationBase);
	
	public WaterbodyOrCountry getCountryByIso(String iso639);
	
	public List<WaterbodyOrCountry> getWaterbodyOrCountryByName(String name);
	
	/** */
	public abstract List<Collection> searchCollectionByCode(String code);
	
	public abstract UUID saveCollection(Collection collection);
	
	/**
     * Returns a List of Media that are associated with a given occurence
     * 
	 * @param occurence the occurence associated with these media
	 * @param pageSize The maximum number of media returned (can be null for all related media)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
     * @return a Pager of media instances
     */
	public Pager<Media> getMedia(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
	
	/**
     * Returns a List of determinations that have been made for a given occurence
     * 
	 * @param occurence the occurence associated with these determinations
	 * @param pageSize The maximum number of determinations returned (can be null for all related determinations)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a Pager of determination instances
     */
	public Pager<DeterminationEvent> getDeterminations(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
	
	/**
     * Returns a list of derivation events that have involved creating new DerivedUnits from this occurence
     * 
	 * @param occurence the occurence that was a source of these derivation events
	 * @param pageSize The maximum number of derivation events returned (can be null for all related derivation events)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a Pager of derivation events
     */
	public Pager<DerivationEvent> getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
	
	public Pager<SpecimenOrObservationBase> search(Class<? extends SpecimenOrObservationBase> clazz, String query, Integer pageSize,Integer pageNumber, List<OrderHint> orderHints,List<String> propertyPaths);
}
