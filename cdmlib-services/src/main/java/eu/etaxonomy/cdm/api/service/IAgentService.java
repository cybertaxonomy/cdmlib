package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.agent.Agent;

public interface IAgentService<T extends Agent> extends IIdentifiableEntityService<T> {
	
	public abstract T getAgentByUuid(UUID uuid);

	public abstract UUID saveAgent(T agent);
	
	public abstract Map<UUID, T> saveAgentAll(Collection<T> agentCollection);
	
	public abstract List<T> findAgentsByTitle(String title);

	public abstract List<? extends Agent> getAllAgents(int limit, int start);

}
