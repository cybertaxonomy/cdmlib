/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureNodeDao;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureTreeDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;

/**
 * @author a.babadshanjan
 * @created 09.09.2008
 */
@Repository
public class FeatureNodeDaoImpl extends CdmEntityDaoBase<FeatureNode> implements IFeatureNodeDao {
//public class AgentDaoImpl extends IdentifiableDaoBase<Agent> implements IAgentDao{

	public FeatureNodeDaoImpl() {
		super(FeatureNode.class); 
	}

	public List<FeatureNode> list() {
		Criteria crit = getSession().createCriteria(type); 
		return crit.list(); 
	}
	
}
