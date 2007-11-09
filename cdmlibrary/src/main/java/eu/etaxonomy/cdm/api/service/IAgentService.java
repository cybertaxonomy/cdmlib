package eu.etaxonomy.cdm.api.service;

import eu.etaxonomy.cdm.model.agent.Team;

public interface IAgentService extends IService {
	public abstract Team getTeamById(Integer id);

	public abstract int saveTeam(Team team);

}
