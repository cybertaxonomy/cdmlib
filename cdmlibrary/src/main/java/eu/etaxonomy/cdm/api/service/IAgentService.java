package eu.etaxonomy.cdm.api.service;

import eu.etaxonomy.cdm.model.agent.Agent;

public interface IAgentService extends IService {
	public abstract Agent getAgentById(Integer id);

	public abstract int saveAgent(Agent agent);

}
