/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.Scope;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;


@Repository
public class DescriptionDaoImpl extends IdentifiableDaoBase<DescriptionBase> implements IDescriptionDao{
	private static final Logger logger = Logger.getLogger(DescriptionDaoImpl.class);

	public DescriptionDaoImpl() {
		super(DescriptionBase.class); 
	}

	public int countDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase presence) {
		// TODO Auto-generated method stub
		return 0;
	}

	public <TYPE extends DescriptionElementBase> int countDescriptionElements(DescriptionBase description, List<Feature> features,	Class<TYPE> type) {
		// TODO Auto-generated method stub
		return 0;
	}

	public <TYPE extends DescriptionBase> int countDescriptions(Class<TYPE> type, Boolean hasImages, Boolean hasText, Set<Feature> feature) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int countTaxonDescriptions(Taxon taxon, Set<Scope> scopes,Set<NamedArea> geographicalScope) {
		// TODO Auto-generated method stub
		return 0;
	}

	public <TYPE extends DescriptionElementBase> List<TYPE> getDescriptionElements(DescriptionBase description, List<Feature> features,	Class<TYPE> type, Integer pageSize, Integer pageNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<TaxonDescription> getTaxonDescriptions(Taxon taxon,	Set<Scope> scopes, Set<NamedArea> geographicalScope,Integer pageSize, Integer pageNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	public <TYPE extends DescriptionBase> List<TYPE> listDescriptions(Class<TYPE> type, Boolean hasImages, Boolean hasText,	Set<Feature> feature, Integer pageSize, Integer pageNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<TaxonDescription> searchDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase presence, Integer pageSize, Integer pageNumber) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
