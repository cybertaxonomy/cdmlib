/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.persistence.dao.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.ITaxonNameDao;


/**
 * @author a.mueller
 *
 */
public class AgentServiceImpl implements IAgentService {
    private static final Logger logger = Logger.getLogger(AgentServiceImpl.class);
	
    private IAgentDao agentDao;
	
	/**
	 * @return the agentDao
	 */
	public IAgentDao getAgentDao() {
		return agentDao;
	}

	/**
	 * @param agentDao the agentDao to set
	 */
	public void setAgentDao(IAgentDao agentDao) {
		this.agentDao = agentDao;
	}


	public Agent getAgentById(Integer id) {
		return (Team)agentDao.findById(id);
	}

	public int saveAgent(Agent agent) {
		return (Integer)agentDao.save(agent);
	}

}
