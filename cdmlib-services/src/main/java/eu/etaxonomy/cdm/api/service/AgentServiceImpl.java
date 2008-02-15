/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.persistence.dao.common.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;


/**
 * @author a.mueller
 *
 */
@Service
@Transactional
public class AgentServiceImpl extends IdentifiableServiceBase<Agent> implements IAgentService {
    private static final Logger logger = Logger.getLogger(AgentServiceImpl.class);

	private IAgentDao agentDao;
	@Autowired
	protected void setDao(IAgentDao dao) {
		this.dao = dao;
		this.agentDao = dao;
	}

	public List<Agent> findAgentsByTitle(String title) {
		return super.findCdmObjectsByTitle(title);
	}

	public Agent getAgentByUuid(UUID uuid) {
		return super.getCdmObjectByUuid(uuid);
	}

	public UUID saveAgent(Agent agent) {
		return super.saveCdmObject(agent);
	}
	
	public List<Agent> getAllAgents(int limit, int start){
		return agentDao.list(limit, start);
	}
}
