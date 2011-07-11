/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.description;

import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.Scope;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IDescriptionDao extends IIdentifiableDao<DescriptionBase> {
	/**
	 * List the descriptions of type <TYPE>, filtered using the following parameters
	 *  
	 * @param type The type of description returned (Taxon, TaxonName, or Specimen)
	 * @param hasMedia Restrict the description to those that do (true) or don't (false) contain <i>elements</i> that have one or more media (can be null)
	 * @param hasText Restrict the description to those that do (true) or don't (false) contain TextData <i>elements</i> that have some textual content (can be null)
	 * @param feature Restrict the description to those <i>elements</i> which are scoped by one of the Features passed (can be null or empty)
	 * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
	 * @return a List of DescriptionBase instances
	 */
	 List<DescriptionBase> listDescriptions(Class<? extends DescriptionBase> type, Boolean hasMedia, Boolean hasText, Set<Feature> feature, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Count the descriptions of type <TYPE>, filtered using the following parameters
	 * 
	 * @param type The type of description returned (Taxon, TaxonName, or Specimen)
	 * @param hasMedia Restrict the description to those that do (true) or don't (false) contain <i>elements</i> that have one or more media (can be null)
	 * @param hasText Restrict the description to those that do (true) or don't (false) contain TextData <i>elements</i> that have some textual content (can be null)
	 * @param feature Restrict the description to those <i>elements</i> which are scoped by one of the Features passed (can be null or empty)
	 * @return a count of DescriptionBase instances
	 */
	 int countDescriptions(Class<? extends DescriptionBase> type, Boolean hasImages, Boolean hasText, Set<Feature> feature);
	
	/**
	 * Returns description elements of type <TYPE>, belonging to a given description, optionally filtered by one or more features
	 * 
	 * @param description The description which these description elements belong to (can be null to count all description elements)
	 * @param features Restrict the results to those description elements which are scoped by one of the Features passed (can be null or empty)
	 * @param type The type of description
	 * @param pageSize The maximum number of description elements returned (can be null for all description elements)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a List of DescriptionElementBase instances
	 */
	 List<DescriptionElementBase> getDescriptionElements(DescriptionBase description,Set<Feature> features, Class<? extends DescriptionElementBase> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
	
	/**
	 * Returns a count of description elements of type <TYPE>, belonging to a given description, optionally filtered by one or more features
	 * 
	 * @param description The description which these description elements belong to (can be null to count all description elements)
	 * @param features Restrict the results to those description elements which are scoped by one of the Features passed (can be null or empty)
	 * @param type The type of description
	 * @return a count of DescriptionElementBase instances
	 */
	 int countDescriptionElements(DescriptionBase description,	Set<Feature> features, Class<? extends DescriptionElementBase> type);
	
	/**
	 * Returns a List of TaxonDescription instances, optionally filtered by parameters passed to this method
	 * 
	 * @param taxon The taxon which the description refers to (can be null for all TaxonDescription instances)
	 * @param scopes Restrict the results to those descriptions which are scoped by one of the Scope instances passed (can be null or empty)
	 * @param geographicalScope Restrict the results to those descriptions which have a geographical scope that overlaps with the NamedArea instances passed (can be null or empty)
	 * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a List of TaxonDescription instances
	 */
	List<TaxonDescription> getTaxonDescriptions(Taxon taxon, Set<Scope> scopes, Set<NamedArea> geographicalScope, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
	
	/**
	 * Returns a count of TaxonDescription instances, optionally filtered by parameters passed to this method
	 * 
	 * @param taxon Restrict the results to those descriptions that refer to a specific taxon (can be null for all TaxonDescription instances)
	 * @param scopes Restrict the results to those descriptions which are scoped by one of the Scope instances passed (can be null or empty)
	 * @param geographicalScope Restrict the results to those descriptions which have a geographical scope that overlaps with the NamedArea instances passed (can be null or empty)
	 * @return a count of TaxonDescription instances
	 */
	int countTaxonDescriptions(Taxon taxon, Set<Scope> scopes, Set<NamedArea> geographicalScope);
	
	/**
	 * Returns a List of TaxonNameDescription instances, optionally filtered by the name which they refer to
	 * 
	 * @param name Restrict the results to those descriptions that refer to a specific name (can be null for all TaxonNameDescription instances)
	 * @param pageSize The maximum number of descriptions returned (can be null for all descriptions)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a List of TaxonNameBase instances
	 */
    List<TaxonNameDescription> getTaxonNameDescriptions(TaxonNameBase name, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
	
    /**
	 * Returns a count of TaxonNameDescription instances, optionally filtered by the name which they refer to
	 * 
	 * @param name Restrict the results to those descriptions that refer to a specific name (can be null for all TaxonNameDescription instances)
	 * @return a count of TaxonNameBase instances
	 */
	int countTaxonNameDescriptions(TaxonNameBase name);
	
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
	 * @return a List of TaxonDescription instances
	 */
	List<TaxonDescription> searchDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase presence, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Returns a list of CommonTaxonName instances that match a search string
	 * @param searchString
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 */
	List<CommonTaxonName> searchDescriptionByCommonName(String queryString, MatchMode matchMode, Integer pageSize, Integer pageNumber);
 
	
	/**
	 * @param queryString
	 * @param matchMode
	 * @return
	 */
	Integer countDescriptionByCommonName(String queryString, MatchMode matchMode); 
	
	/**
	 * Returns a count of distinct TaxonDescription instances which have Distribution elements that refer to one of the NamedArea instances passed (optionally
	 * filtered by a type of PresenceAbsenceTerm e.g. PRESENT / ABSENT / NATIVE / CULTIVATED etc)
	 * 
	 * @param namedAreas The set of NamedArea instances
	 * @param presence Restrict the descriptions to those which have Distribution elements are of this status (can be null)
	 * @return a count of TaxonDescription instances
	 */
	int countDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase presence);

	<T extends DescriptionElementBase> List<T> getDescriptionElementForTaxon(Taxon taxon,
			Set<Feature> features,
			Class<T> type, Integer pageSize,
			Integer pageNumber, List<String> propertyPaths);
}
