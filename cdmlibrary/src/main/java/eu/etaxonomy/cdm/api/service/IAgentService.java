package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.agent.Agent;

public interface IAgentService extends IIdentifiableEntityService<Agent> {
	public abstract Agent getAgentByUuid(String uuid);

	public abstract String saveAgent(Agent agent);
	
	public abstract List<Agent> findAgentsByTitle(String title);

}
