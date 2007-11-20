/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

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
	
	@Autowired
    private IAgentDao agentDao;
	

	public Agent getAgentById(Integer id) {
		return (Team)agentDao.findById(id);
	}

	public int saveAgent(Agent agent) {
		return (Integer)agentDao.save(agent);
	}

}
