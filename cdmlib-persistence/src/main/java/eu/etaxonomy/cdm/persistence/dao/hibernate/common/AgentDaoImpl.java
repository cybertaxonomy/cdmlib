package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.persistence.dao.common.IAgentDao;


@Repository
public class AgentDaoImpl extends IdentifiableDaoBase<Agent> implements IAgentDao{
	private static final Logger logger = Logger.getLogger(AgentDaoImpl.class);

	public AgentDaoImpl() {
		super(Agent.class); 
	}

}
