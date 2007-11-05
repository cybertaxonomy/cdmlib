/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.persistence.dao.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.INonViralNameDao;


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

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IAgentService#getTeamById(java.lang.Integer)
	 */
	public Team getTeamById(Integer id) {
		return (Team)agentDao.findById(id);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IAgentService#saveTeam(eu.etaxonomy.cdm.model.agent.Team)
	 */
	public int saveTeam(Team team) {
		return (Integer)agentDao.save(team);
	}

}
