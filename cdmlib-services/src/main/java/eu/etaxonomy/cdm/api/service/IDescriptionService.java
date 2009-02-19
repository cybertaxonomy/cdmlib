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
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.Scope;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public interface IDescriptionService extends IIdentifiableEntityService<DescriptionBase> {

	/**
	 * @param uuid
	 * @return
	 */
	public DescriptionBase getDescriptionBaseByUuid(UUID uuid);

	/**
	 * Persists a <code>Description</code>
	  * @param description
	 * @return
	 */
	public UUID saveDescription(DescriptionBase description);

	/**
	 * Persists a <code>FeatureTree</code>
	 * @param tree
	 * @return
	 */
	public UUID saveFeatureTree(FeatureTree tree);
	// FIXME could you handle the feature data elements using @Cascade?
	public void saveFeatureDataAll(Collection<VersionableEntity> featureData);
	public Map<UUID, FeatureTree> saveFeatureTreeAll(Collection<FeatureTree> trees);
	public Map<UUID, FeatureNode> saveFeatureNodeAll(Collection<FeatureNode> nodes);
	
	public List<FeatureTree> getFeatureTreesAll();
	public List<FeatureNode> getFeatureNodesAll();
	
	public TermVocabulary<Feature> getDefaultFeatureVocabulary();
	//public TermVocabulary<Feature> getFeatureVocabulary();
	public TermVocabulary<Feature> getFeatureVocabulary(UUID uuid);

	/**
	 * List the descriptions of type <TYPE>, filtered using the following parameters
	 *  
	 * @param type The type of description returned (Taxon, TaxonName, or Specimen)
	 * @param hasMedia Restrict the description to those that do (true) or don't (false) contain <i>elements</i> that have one or more media (can be null)
	 * @param hasText Restrict the description to those that do (true) or don't (false) contain TextData <i>elements</i> that have some textual content (can be null)
	 * @param feature Restrict the description to those <i>elements</i> which are scoped by one of the Features passed (can be null or empty)
	 * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager containing DescriptionBase instances
	 */
	public <TYPE extends DescriptionBase> Pager<TYPE> listDescriptions(Class<TYPE> type, Boolean hasMedia, Boolean hasText, Set<Feature> feature, Integer pageSize, Integer pageNumber);
	
	/**
	 * Count the descriptions of type <TYPE>, filtered using the following parameters
	 * 
	 * @param type The type of description returned (Taxon, TaxonName, or Specimen)
	 * @param hasMedia Restrict the description to those that do (true) or don't (false) contain <i>elements</i> that have one or more media (can be null)
	 * @param hasText Restrict the description to those that do (true) or don't (false) contain TextData <i>elements</i> that have some textual content (can be null)
	 * @param feature Restrict the description to those <i>elements</i> which are scoped by one of the Features passed (can be null or empty)
	 * @return a count of DescriptionBase instances
	 */
	public <TYPE extends DescriptionBase> int countDescriptions(Class<TYPE> type, Boolean hasImages, Boolean hasText, Set<Feature> feature);
	
	/**
	 * Returns description elements of type <TYPE>, belonging to a given description, optionally filtered by one or more features
	 * 
	 * @param description The description which these description elements belong to (can be null to count all description elements)
	 * @param features Restrict the results to those description elements which are scoped by one of the Features passed (can be null or empty)
	 * @param type The type of description
	 * @param pageSize The maximum number of description elements returned (can be null for all description elements)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager containing DescriptionElementBase instances
	 */
	public <TYPE extends DescriptionElementBase> Pager<TYPE> getDescriptionElements(DescriptionBase description,Set<Feature> features, Class<TYPE> type, Integer pageSize, Integer pageNumber);
	
	/**
	 * Returns a List of TaxonDescription instances, optionally filtered by parameters passed to this method
	 * 
	 * @param taxon The taxon which the description refers to (can be null for all TaxonDescription instances)
	 * @param scopes Restrict the results to those descriptions which are scoped by one of the Scope instances passed (can be null or empty)
	 * @param geographicalScope Restrict the results to those descriptions which have a geographical scope that overlaps with the NamedArea instances passed (can be null or empty)
	 * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager containing TaxonDescription instances
	 */
	public Pager<TaxonDescription> getTaxonDescriptions(Taxon taxon, Set<Scope> scopes, Set<NamedArea> geographicalScope, Integer pageSize, Integer pageNumber);
	
	/**
	 * Returns a List of TaxonNameDescription instances, optionally filtered by the name which they refer to
	 * 
	 * @param name Restrict the results to those descriptions that refer to a specific name (can be null for all TaxonNameDescription instances)
	 * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager containing TaxonNameBase instances
	 */
	public Pager<TaxonNameDescription> getTaxonNameDescriptions(TaxonNameBase name, Integer pageSize, Integer pageNumber);
	
	/**
	 * Returns a List of distinct TaxonDescription instances which have Distribution elements that refer to one of the NamedArea instances passed (optionally
	 * filtered by a type of PresenceAbsenceTerm e.g. PRESENT / ABSENT / NATIVE / CULTIVATED etc)
	 * 
	 * @param namedAreas The set of NamedArea instances
	 * @param presence Restrict the descriptions to those which have Distribution elements are of this status (can be null)
	 * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager containing TaxonDescription instances
	 */
	public Pager<TaxonDescription> searchDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase presence, Integer pageSize, Integer pageNumber);
	
	/**
     * Returns a List of TextData elements that match a given queryString provided.
	 * 
	 * @param queryString 
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 * @throws QueryParseException
	 */
	public Pager<TextData> searchTextData(String queryString, Integer pageSize, Integer pageNumber);
	
	/**
     * Returns a List of Media that are associated with a given description element
     * 
	 * @param descriptionElement the description element associated with these media
	 * @param pageSize The maximum number of media returned (can be null for all related media)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a Pager containing media instances
     */
    public Pager<Media> getMedia(DescriptionElementBase descriptionElement, Integer pageSize, Integer pageNumber);
	
}