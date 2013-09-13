/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.List;

import org.hibernate.Criteria;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureNodeDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.VersionableDaoBase;

/**
 * @author a.babadshanjan
 * @created 09.09.2008
 */
@Repository
public class FeatureNodeDaoImpl extends VersionableDaoBase<FeatureNode> implements IFeatureNodeDao {

	public FeatureNodeDaoImpl() {
		super(FeatureNode.class); 
	}

	public List<FeatureNode> list() {
		Criteria crit = getSession().createCriteria(type); 
		return crit.list(); 
	}
	
}
