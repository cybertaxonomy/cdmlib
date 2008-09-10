/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * @author a.babadshanjan
 * @created 01.09.2008
 */
public interface IOccurrenceService extends IIdentifiableEntityService<SpecimenOrObservationBase> {

	/**
	 * Computes all specimen or observation bases.
	 * @param limit
	 * @param start
	 * @return
	 */
	public abstract List<SpecimenOrObservationBase> getAllSpecimenOrObservationBases(int limit, int start);

	/** Saves a collection of specimen or observation bases.
	 * @return Map with UUID as key and SpecimenOrObservationBase as value.
	 **/
	public abstract Map<UUID, SpecimenOrObservationBase> 
	saveSpecimenOrObservationBaseAll(java.util.Collection<SpecimenOrObservationBase> specimenOrObservationBaseCollection);

	/** save a specimen or observation and return its UUID**/
	public abstract UUID saveSpecimenOrObservationBase (SpecimenOrObservationBase specimenOrObservationBase);
	
	public WaterbodyOrCountry getCountryByIso(String iso639);
	
	public List<WaterbodyOrCountry> getWaterbodyOrCountryByName(String name);
	
	/** */
	public abstract List<Collection> searchCollectionByCode(String code);
}
