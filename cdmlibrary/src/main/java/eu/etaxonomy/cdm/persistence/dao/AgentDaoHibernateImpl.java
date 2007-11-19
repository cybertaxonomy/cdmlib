package eu.etaxonomy.cdm.persistence.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.agent.Agent;

@Repository
public class AgentDaoHibernateImpl extends DaoBase<Agent, Integer> implements IAgentDao{
	private static final Logger logger = Logger.getLogger(AgentDaoHibernateImpl.class);

	/**
	 * 
	 */
	public AgentDaoHibernateImpl() {
		super(Agent.class); 
	}

	@Override
	public List<Agent> find(String queryString) {
		// TODO Auto-generated method stub
		return null;
	}
}
