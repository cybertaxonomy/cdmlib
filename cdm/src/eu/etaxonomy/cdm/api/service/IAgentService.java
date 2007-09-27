package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.TaxonName;

public interface IAgentService extends IService {
	public abstract Team getTeamById(Integer id);

	public abstract int saveTeam(Team team);

}
