/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

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
	
	public abstract List<Agent> getAgentByCode(String code);


}
