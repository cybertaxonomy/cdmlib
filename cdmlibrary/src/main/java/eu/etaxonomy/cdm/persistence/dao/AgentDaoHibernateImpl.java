package eu.etaxonomy.cdm.persistence.dao;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.Agent;

public class AgentDaoHibernateImpl extends DaoBase<Agent, Integer> implements IAgentDao{
	private static final Logger logger = Logger.getLogger(AgentDaoHibernateImpl.class);

	/**
	 * 
	 */
	public AgentDaoHibernateImpl() {
		super(Agent.class); 
	}
}
