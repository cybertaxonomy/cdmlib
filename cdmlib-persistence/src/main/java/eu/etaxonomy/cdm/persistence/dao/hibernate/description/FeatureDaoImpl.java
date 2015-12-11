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

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;


@Repository
public class FeatureDaoImpl extends CdmEntityDaoBase<Feature> implements IFeatureDao{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FeatureDaoImpl.class);

	public FeatureDaoImpl() {
		super(Feature.class); 
	}
	
	public List<Feature> list() {
		Criteria crit = getSession().createCriteria(type); 
		return crit.list(); 
	}
}
