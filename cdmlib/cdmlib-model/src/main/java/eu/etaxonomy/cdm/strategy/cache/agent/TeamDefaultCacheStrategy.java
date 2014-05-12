/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.strategy.cache.agent;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.strategy.StrategyBase;

/**
 * @author AM
 *
 */
public class TeamDefaultCacheStrategy extends StrategyBase implements INomenclaturalAuthorCacheStrategy<Team> {
	private static final String FINAL_TEAM_CONCATINATION = " & ";
	private static final String STD_TEAM_CONCATINATION = ", ";
	private static final long serialVersionUID = 8375295443642690479L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TeamDefaultCacheStrategy.class);

	final static UUID uuid = UUID.fromString("1cbda0d1-d5cc-480f-bf38-40a510a3f223");

	public static final String EMPTY_TEAM = "-empty team-";
	
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
		int i = 0;
		for (Person teamMember : teamMembers){
			i++;
			String concat;
			if (i <= 1){
				concat = "";
			}else if (i < teamMembers.size()){
				concat = STD_TEAM_CONCATINATION;
			}else{
				concat = FINAL_TEAM_CONCATINATION;
			}
			result += concat + teamMember.getNomenclaturalTitle();
		}
		if (teamMembers.size() == 0){
			result = team.getTitleCache();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INomenclaturalAuthorCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.name.TaxonNameBase)
	 */
	public String getTitleCache(Team team) {
		// TODO is still dummy
		String result = "";
		List<Person> teamMembers = team.getTeamMembers();//Hibernate.initialize(teamMembers);
		
		int i = 0;
		for (Person teamMember : teamMembers){
			i++;
			String concat;
			if (i <= 1){
				concat = "";
			}else if (i < teamMembers.size()){
				concat = STD_TEAM_CONCATINATION;
			}else{
				concat = FINAL_TEAM_CONCATINATION;
			}
			result += concat + teamMember.getTitleCache();
		}
		if (teamMembers.size() == 0){
			result = EMPTY_TEAM;
		}
		return result;
	}

}
