/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.occurrence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
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
     * Queries the database for specimens which match the given criteria
     *
     * @param clazz
     *            the class to match
     * @param queryString
     *            the queryString to match
     * @param type
     *            the {@link SpecimenOrObservationType} to match
     * @param associatedTaxon
     *            the taxon these specimens are in any way associated to via
     *            determination, type designations, individuals associations,
     *            etc.
     * @param matchmode
     *            determines how the query string should be matched
     * @param limit
     *            the maximum number of entities returned (can be null to return
     *            all entities)
     * @param start
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths
     * @return a list of specimens that match the given parameters
     */
    public <T extends SpecimenOrObservationBase> List<T> findOccurrences(Class<T> clazz, String queryString,
            String significantIdentifier, SpecimenOrObservationType type, Taxon determinedAs, MatchMode matchmode,
            Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Returns the number of specimens that match the given parameters
	 * @param clazz the class to match
     * @param queryString the queryString to match
     * @param type the {@link SpecimenOrObservationType} to match
     * @param associatedTaxon the taxon these specimens are in any way associated to via
     * determination, type designations, individuals associations, etc.
     * @param matchmode determines how the query string should be matched
     * @param limit
     *            the maximum number of entities returned (can be null to return
     *            all entities)
     * @param start
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
	 * @return the number of found specimens
	 */
    public <T extends SpecimenOrObservationBase> int countOccurrences(Class<T> clazz, String queryString,
            String significantIdentifier, SpecimenOrObservationType recordBasis, Taxon associatedTaxon,
            MatchMode matchmode, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

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
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
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
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
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
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @return a List of derivation events
     */
	public List<DerivationEvent> getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Retrieves the {@link UUID} and the string representation (title cache) of all
     * {@link FieldUnit}s found in the data base.
     * @return a list of {@link UuidAndTitleCache}
     */
	public List<UuidAndTitleCache<FieldUnit>> getFieldUnitUuidAndTitleCache();

    /**
     * Retrieves the {@link UUID} and the string representation (title cache) of all
     * {@link DerivedUnit}s found in the data base.
     * @return a list of {@link UuidAndTitleCache}
     */
	public List<UuidAndTitleCache<DerivedUnit>> getDerivedUnitUuidAndTitleCache();

	/**
	 * Lists all instances of {@link SpecimenOrObservationBase} which are associated with the <code>taxon</code> specified as parameter.
	 * SpecimenOrObservationBase instances can be associated to taxa in multiple ways, all these possible relations are taken into account:
	 * <ul>
	 * <li>The {@link IndividualsAssociation} elements in a {@link TaxonDescription} contain {@link DerivedUnit}s</li>
	 * <li>{@link SpecimenTypeDesignation}s may be associated with any {@link HomotypicalGroup} related to the specific {@link Taxon}.</li>
	 * <li>A {@link Taxon} may be referenced by the {@link DeterminationEvent} of the {@link SpecimenOrObservationBase}</li>
	 * </ul>
	 *
	 * @param <T>
	 * @param type
	 * @param associatedTaxon
	 * @param limit
	 * @param start
	 * @param orderHints
	 * @param propertyPaths
	 * @return
	 */
	public <T extends SpecimenOrObservationBase> List<T> listByAssociatedTaxon(Class<T> type, Taxon associatedTaxon,
			Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Retrieves all {@link IndividualsAssociation} with the given specimen.<br>
     * @param specimen the specimen for which the associations are retrieved
     * @param limit
     * @param start
     * @param orderHints
     * @param propertyPaths
     * @return collection of all associations
     */
	public Collection<IndividualsAssociation> listIndividualsAssociations(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Retrieves all {@link SpecimenTypeDesignation}s which have the given specimen as a type specimen.
     * @param specimen the type specimen
     * @param limit
     * @param start
     * @param orderHints
     * @param propertyPaths
     * @return collection of all designations with the given type specimen
     */
    public Collection<SpecimenTypeDesignation> listTypeDesignations(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Retrieves all {@link DescriptionBase}s that have the given specimen set as described specimen.
     * @param specimen the described specimen
     * @param limit
     * @param start
     * @param orderHints
     * @param propertyPaths
     * @return collection of all descriptions with the given described specimen
     */
    public Collection<DescriptionBase<?>> listDescriptionsWithDescriptionSpecimen(SpecimenOrObservationBase<?> specimen, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Retrieves all {@link SpecimenOrObservationBase}s that have the given {@link SpecimenOrObservationType}.
     * @param type
     * @param limit
     * @param start
     * @param orderHints
     * @param propertyPaths
     * @return collection of specimen with the given type
     */
    public Collection<SpecimenOrObservationBase> listBySpecimenOrObservationType(SpecimenOrObservationType type, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);
}
