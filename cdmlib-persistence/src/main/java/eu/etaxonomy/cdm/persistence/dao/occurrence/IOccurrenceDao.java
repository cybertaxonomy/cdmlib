/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.occurrence;

import java.util.List;

import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.babadshanjan
 * @created 01.09.2008
 */
public interface IOccurrenceDao extends IIdentifiableDao<SpecimenOrObservationBase> {
	
	/**
	 * Returns the number of occurences belonging to a certain subclass - which must extend SpecimenOrObservationBase
	 * @param clazz optionally restrict the counted occurrences to those of a certain subclass of SpecimenOrObservationBase
	 * @param determinedAs the taxon concept that these specimens are determined to belong to 
	 * @return
	 */
	public int count(Class<? extends SpecimenOrObservationBase> clazz,TaxonBase determinedAs);
	
	/**
	 * Returns a sublist of SpecimenOrObservationBase instances stored in the database. A maximum
	 * of 'limit' objects are returned, starting at object with index 'start'. Only occurrences which 
	 * have been determined to belong to the supplied concept are returned.
	 * 
	 * @param type 
	 * @param determinedAs the taxon concept that these specimens are determined to belong to 
	 * @param limit
	 *            the maximum number of entities returned (can be null to return
	 *            all entities)
	 * @param start
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @return
	 * @throws DataAccessException
	 */
	public List<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> type, TaxonBase determinedAs, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
     * Returns a count of Media that are associated with a given occurence
     * 
	 * @param occurence the occurence associated with these media
     * @return a count of media instances
     */
	public int countMedia(SpecimenOrObservationBase occurence);
	
    /**
     * Returns a List of Media that are associated with a given occurence
     * 
	 * @param occurence the occurence associated with these media
	 * @param pageSize The maximum number of media returned (can be null for all related media)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
     * @return a List of media instances
     */
	public List<Media> getMedia(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
	
	/**
     * Returns a count of determinations that have been made for a given occurence and for a given taxon concept
     * 
	 * @param occurence the occurence associated with these determinations (can be null for all occurrences)
	 * @param taxonbase the taxon concept associated with these determinations (can be null for all taxon concepts)
     * @return a count of determination events
     */
    public int countDeterminations(SpecimenOrObservationBase occurence,TaxonBase taxonbase);
	
    /**
     * Returns a List of determinations that have been made for a given occurence and for a given taxon concept
     * 
	 * @param occurence the occurence associated with these determinations (can be null for all occurrences)
	 * @param taxonbase the taxon concept associated with these determinations (can be null for all taxon concepts)
	 * @param pageSize The maximum number of determinations returned (can be null for all related determinations)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
     * @return a List of determination instances
     */
	public List<DeterminationEvent> getDeterminations(SpecimenOrObservationBase occurence,TaxonBase taxonbase, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

	/**
     * Returns a count of derivation events that have involved creating new DerivedUnits from this occurence
     * 
	 * @param occurence the occurence that was a source of these derivation events
     * @return a count of derivation events
     */
    public int countDerivationEvents(SpecimenOrObservationBase occurence);
	
    /**
     * Returns a list of derivation events that have involved creating new DerivedUnits from this occurence
     * 
	 * @param occurence the occurence that was a source of these derivation events
	 * @param pageSize The maximum number of derivation events returned (can be null for all related derivation events)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
     * @return a List of derivation events
     */
	public List<DerivationEvent> getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
}
