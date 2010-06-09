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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.Scope;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IDescriptionService extends IIdentifiableEntityService<DescriptionBase> {
	
	/**
	 * 
	 * @return
	 * @deprecated use TermService#getVocabulary(VocabularyType) instead
	 */
	public TermVocabulary<Feature> getDefaultFeatureVocabulary();

	/**
	 * @deprecated use TermService#getVocabulary(VocabularyType) instead
	 */
	public TermVocabulary<Feature> getFeatureVocabulary(UUID uuid);

	/**
	 * Gets a DescriptionElementBase instance matching the supplied uuid
	 * 
	 * @param uuid the uuid of the DescriptionElement of interest
	 * @return a DescriptionElement, or null if the DescriptionElement does not exist
	 */
	public DescriptionElementBase getDescriptionElementByUuid(UUID uuid);
	
	/**
	 * Loads and existing DescriptionElementBase instance matching the supplied uuid,
	 * and recursively initializes all bean properties given in the
	 * <code>propertyPaths</code> parameter.
	 * <p>
	 * For detailed description and examples <b>please refer to:</b> 
	 * {@link BeanInitializer#initialize(Object, List)}
	 * 
	 * @param uuid the uuid of the DescriptionElement of interest
	 * @return a DescriptionElement, or null if the DescriptionElement does not exist
	 */
	public DescriptionElementBase loadDescriptionElement(UUID uuid,List<String> propertyPaths);
	
	/**
	 * Persists a <code>DescriptionElementBase</code>
	 * @param descriptionElement
	 * @return
	 */
	public UUID saveDescriptionElement(DescriptionElementBase descriptionElement);
	
	/**
	 * Persists a collection of <code>DescriptionElementBase</code>
	 * @param descriptionElements
	 * @return
	 */
	public Map<UUID, DescriptionElementBase> saveDescriptionElement(Collection<DescriptionElementBase> descriptionElements);
	
	/**
	 * Delete an existing description element
	 * 
	 * @param descriptionElement the description element to be deleted
	 * @return the unique identifier of the deleted entity
	 */
	public UUID deleteDescriptionElement(DescriptionElementBase descriptionElement);
	
	/**
	 * List the descriptions of type <T>, filtered using the following parameters
	 *  
	 * @param type The type of description returned (Taxon, TaxonName, or Specimen)
	 * @param hasMedia Restrict the description to those that do (true) or don't (false) contain <i>elements</i> that have one or more media (can be null)
	 * @param hasText Restrict the description to those that do (true) or don't (false) contain TextData <i>elements</i> that have some textual content (can be null)
	 * @param feature Restrict the description to those <i>elements</i> which are scoped by one of the Features passed (can be null or empty)
	 * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager containing DescriptionBase instances
	 */
	public Pager<DescriptionBase> page(Class<? extends DescriptionBase> type, Boolean hasMedia, Boolean hasText, Set<Feature> feature, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Count the descriptions of type <TYPE>, filtered using the following parameters
	 * 
	 * @param type The type of description returned (Taxon, TaxonName, or Specimen)
	 * @param hasMedia Restrict the description to those that do (true) or don't (false) contain <i>elements</i> that have one or more media (can be null)
	 * @param hasText Restrict the description to those that do (true) or don't (false) contain TextData <i>elements</i> that have some textual content (can be null)
	 * @param feature Restrict the description to those <i>elements</i> which are scoped by one of the Features passed (can be null or empty)
	 * @return a count of DescriptionBase instances
	 */
	public int count(Class<? extends DescriptionBase> type, Boolean hasImages, Boolean hasText, Set<Feature> feature);
	
	/**
	 * Returns description elements of type <TYPE>, belonging to a given description, optionally filtered by one or more features
	 * 
	 * @param description The description which these description elements belong to (can be null to count all description elements)
	 * @param features Restrict the results to those description elements which are scoped by one of the Features passed (can be null or empty)
	 * @param type The type of description
	 * @param class 
	 * @param pageSize The maximum number of description elements returned (can be null for all description elements)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager containing DescriptionElementBase instances
	 */
	public Pager<DescriptionElementBase> getDescriptionElements(DescriptionBase description,Set<Feature> features, Class<? extends DescriptionElementBase> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
	
	/**
	 * Returns description elements of type <TYPE>, belonging to a given description, optionally filtered by one or more features
	 * 
	 * @param description The description which these description elements belong to (can be null to count all description elements)
	 * @param features Restrict the results to those description elements which are scoped by one of the Features passed (can be null or empty)
	 * @param type The type of description
	 * @param class 
	 * @param pageSize The maximum number of description elements returned (can be null for all description elements)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a List containing DescriptionElementBase instances
	 */
	public List<DescriptionElementBase> listDescriptionElements(DescriptionBase description,Set<Feature> features, Class<? extends DescriptionElementBase> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

	/**
	 * Return a Pager containing Annotation entities belonging to the DescriptionElementBase instance supplied, optionally filtered by MarkerType
     * @param annotatedObj The object that "owns" the annotations returned
	 * @param status Only return annotations which are marked with a Marker of this type (can be null to return all annotations)
	 * @param pageSize The maximum number of terms returned (can be null for all annotations)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager of Annotation entities
	 */
	public Pager<Annotation> getDescriptionElementAnnotations(DescriptionElementBase annotatedObj, MarkerType status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
		
	
	/**
	 * Returns a List of TaxonDescription instances, optionally filtered by parameters passed to this method
	 * 
	 * @param taxon The taxon which the description refers to (can be null for all TaxonDescription instances)
	 * @param scopes Restrict the results to those descriptions which are scoped by one of the Scope instances passed (can be null or empty)
	 * @param geographicalScope Restrict the results to those descriptions which have a geographical scope that overlaps with the NamedArea instances passed (can be null or empty)
	 * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager containing TaxonDescription instances
	 */
	public Pager<TaxonDescription> getTaxonDescriptions(Taxon taxon, Set<Scope> scopes, Set<NamedArea> geographicalScope, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
	
	/**
	 * Returns a List of TaxonNameDescription instances, optionally filtered by the name which they refer to
	 * 
	 * @param name Restrict the results to those descriptions that refer to a specific name (can be null for all TaxonNameDescription instances)
	 * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager containing TaxonNameBase instances
	 */
	public Pager<TaxonNameDescription> getTaxonNameDescriptions(TaxonNameBase name, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
	
	/**
	 * Returns a List of distinct TaxonDescription instances which have Distribution elements that refer to one of the NamedArea instances passed (optionally
	 * filtered by a type of PresenceAbsenceTerm e.g. PRESENT / ABSENT / NATIVE / CULTIVATED etc)
	 * 
	 * @param namedAreas The set of NamedArea instances
	 * @param presence Restrict the descriptions to those which have Distribution elements are of this status (can be null)
	 * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager containing TaxonDescription instances
	 */
	public Pager<TaxonDescription> searchDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase presence, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Returns a Paged List of DescriptionElementBase instances where the default field matches the String queryString (as interpreted by the Lucene QueryParser)
	 * 
	 * @param clazz filter the results by class (or pass null to return all DescriptionElementBase instances)
	 * @param queryString
	 * @param pageSize The maximum number of descriptionElements returned (can be null for all matching descriptionElements)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @param propertyPaths properties to be initialized
	 * @return a Pager DescriptionElementBase instances
	 * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
	 */
	public Pager<DescriptionElementBase> searchElements(Class<? extends DescriptionElementBase> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
     * Returns a List of Media that are associated with a given description element
     * 
	 * @param descriptionElement the description element associated with these media
	 * @param pageSize The maximum number of media returned (can be null for all related media)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
     * @return a Pager containing media instances
     */
    public Pager<Media> getMedia(DescriptionElementBase descriptionElement, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
    
    public List<DescriptionElementBase> getDescriptionElementsForTaxon(Taxon taxon, Set<Feature> features, Class<? extends DescriptionElementBase> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

}