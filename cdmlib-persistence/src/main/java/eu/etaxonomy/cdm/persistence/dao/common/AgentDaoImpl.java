package eu.etaxonomy.cdm.persistence.dao.common;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.agent.Agent;


@Repository
public class AgentDaoImpl extends IdentifiableDaoBase<Agent> implements IAgentDao{
	private static final Logger logger = Logger.getLogger(AgentDaoImpl.class);

	public AgentDaoImpl() {
		super(Agent.class); 
	}

}
