// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeNotSupportedException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.babadshanjan
 * @created 01.09.2008
 */
public interface IOccurrenceService extends IIdentifiableEntityService<SpecimenOrObservationBase> {
	
	public WaterbodyOrCountry getCountryByIso(String iso639);
	
	public List<WaterbodyOrCountry> getWaterbodyOrCountryByName(String name);
	
	/**
	 * Returns a paged list of occurrences that have been determined to belong
	 * to the taxon concept determinedAs, optionally restricted to objects
	 * belonging to a class that that extends SpecimenOrObservationBase.
	 * <p>
	 * In contrast to {@link #listByAnyAssociation(Class, Taxon, List)} this
	 * method only takes SpecimenOrObservationBase instances into account which
	 * are actually determined as the taxon specified by
	 * <code>determinedAs</code>.
	 * 
	 * @param type
	 *            The type of entities to return (can be null to count all
	 *            entities of type <T>)
	 * @param determinedAs
	 *            the taxon concept that the occurrences have been determined to
	 *            belong to
	 * @param pageSize
	 *            The maximum number of objects returned (can be null for all
	 *            matching objects)
	 * @param pageNumber
	 *            The offset (in pageSize chunks) from the start of the result
	 *            set (0 - based, can be null, equivalent of starting at the
	 *            beginning of the recordset)
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @param propertyPaths
	 *            properties to be initialized
	 * @return
	 */
	public Pager<SpecimenOrObservationBase> list(Class<? extends SpecimenOrObservationBase> type, TaxonBase determinedAs, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);
	
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
	 * @param occurence the occurence associated with these determinations (can be null for all occurrences)
	 * @param taxonbase the taxon concept associated with these determinations (can be null for all taxon concepts)
	 * @param pageSize The maximum number of determinations returned (can be null for all related determinations)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a Pager of determination instances
     */
	public Pager<DeterminationEvent> getDeterminations(SpecimenOrObservationBase occurence, TaxonBase taxonBase, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
	
	/**
     * Returns a list of derivation events that have involved creating new DerivedUnits from this occurence
     * 
	 * @param occurence the occurence that was a source of these derivation events
	 * @param pageSize The maximum number of derivation events returned (can be null for all related derivation events)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a Pager of derivation events
     */
	public Pager<DerivationEvent> getDerivationEvents(SpecimenOrObservationBase occurence, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
	
	/**
	 * Returns a Paged List of SpecimenOrObservationBase instances where the default field matches the String queryString (as interpreted by the Lucene QueryParser)
	 * 
	 * @param clazz filter the results by class (or pass null to return all SpecimenOrObservationBase instances)
	 * @param queryString
	 * @param pageSize The maximum number of occurrences returned (can be null for all matching occurrences)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @param propertyPaths properties to be initialized
	 * @return a Pager SpecimenOrObservationBase instances
	 * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
	 */
	public Pager<SpecimenOrObservationBase> search(Class<? extends SpecimenOrObservationBase> clazz, String query, Integer pageSize,Integer pageNumber, List<OrderHint> orderHints,List<String> propertyPaths);
	
	public List<UuidAndTitleCache<FieldObservation>> getFieldObservationUuidAndTitleCache();
	
	public List<UuidAndTitleCache<DerivedUnitBase>> getDerivedUnitBaseUuidAndTitleCache();
	
	public DerivedUnitFacade getDerivedUnitFacade(DerivedUnitBase derivedUnit, List<String> propertyPaths) throws DerivedUnitFacadeNotSupportedException;
	
	public List<DerivedUnitFacade> listDerivedUnitFacades(DescriptionBase description, List<String> propertyPaths);
	
	/**
	 * Lists all instances of {@link SpecimenOrObservationBase} which are associated with the <code>taxon</code> specified as parameter.
	 * SpecimenOrObservationBase instances can be associated to taxa in multiple ways, all these possible relations are taken into account:
	 * <ul>
	 * <li>The {@link IndividualsAssociation} elements in a {@link TaxonDescription} contain {@link DerivedUnitBase}s</li>
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
	public <T extends SpecimenOrObservationBase> List<T> listByAnyAssociation(Class<T> type,
			Taxon associatedTaxon, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);
		
}
