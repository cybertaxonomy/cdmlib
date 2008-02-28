package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.agent.Agent;

public interface IAgentService extends IIdentifiableEntityService<Agent> {
	
	public abstract Agent getAgentByUuid(UUID uuid);

	public abstract UUID saveAgent(Agent agent);
	
	public abstract Map<UUID, Agent> saveAgentAll(Collection<Agent> agentCollection);
	
	public abstract List<Agent> findAgentsByTitle(String title);

	public abstract List<Agent> getAllAgents(int limit, int start);

}
