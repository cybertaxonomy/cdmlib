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

import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.Scope;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

public interface IDescriptionDao extends IIdentifiableDao<DescriptionBase> {
	<TYPE extends DescriptionBase> List<TYPE> listDescriptions(Class<TYPE> type, Boolean hasImages, Boolean hasText, Set<Feature> feature, Integer pageSize, Integer pageNumber);
	
	<TYPE extends DescriptionBase> int countDescriptions(Class<TYPE> type, Boolean hasImages, Boolean hasText, Set<Feature> feature);
	
	<TYPE extends DescriptionElementBase> List<TYPE> getDescriptionElements(DescriptionBase description,	List<Feature> features, Class<TYPE> type, Integer pageSize, Integer pageNumber);
	
	<TYPE extends DescriptionElementBase> int countDescriptionElements(DescriptionBase description,	List<Feature> features, Class<TYPE> type);
	
	List<TaxonDescription> getTaxonDescriptions(Taxon taxon, Set<Scope> scopes, Set<NamedArea> geographicalScope, Integer pageSize, Integer pageNumber);
	
	int countTaxonDescriptions(Taxon taxon, Set<Scope> scopes, Set<NamedArea> geographicalScope);
	
	List<TaxonDescription> searchDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase presence, Integer pageSize, Integer pageNumber);
	
	int countDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase presence);
}
