/**
 * 
 */
package eu.etaxonomy.cdm.strategy.cache;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;

/**
 * @author AM
 *
 */
public class TeamDefaultCacheStrategy extends StrategyBase implements INomenclaturalAuthorCacheStrategy<Team> {
	private static final Logger logger = Logger.getLogger(TeamDefaultCacheStrategy.class);

	final static UUID uuid = UUID.fromString("1cbda0d1-d5cc-480f-bf38-40a510a3f223");

	static public TeamDefaultCacheStrategy NewInstance(){
		return new TeamDefaultCacheStrategy();
	}
	
	/**
	 * 
	 */
	private TeamDefaultCacheStrategy() {
		super();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.StrategyBase#getUuid()
	 */
	@Override
	protected UUID getUuid() {
		return uuid;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INomenclaturalAuthorCacheStrategy#getNomenclaturalTitle(eu.etaxonomy.cdm.model.name.TaxonNameBase)
	 */
	public String getNomenclaturalTitle(Team team) {
		String result = "";
		List<Person> teamMembers = team.getTeamMembers();
		for (Person teamMember : teamMembers){
			result += teamMember.getNomenclaturalTitle() + " & ";
		}
		if (teamMembers.size() > 1){
			result = result.substring(0, result.length() - 3);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INomenclaturalAuthorCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.name.TaxonNameBase)
	 */
	public String getTitleCache(Team team) {
		// TODO is still dummy
		String result = "";
		List<Person> teamMembers = team.getTeamMembers();
		for (Person teamMember : teamMembers){
			result += teamMember.getTitleCache() + " & ";
		}
		if (teamMembers.size() > 1){
			result = result.substring(0, result.length() - 3);
		}
		return result;
	}

}
