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
    private static final long serialVersionUID = 8375295443642690479L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TeamDefaultCacheStrategy.class);


    private static final String FINAL_TEAM_CONCATINATION = " & ";
	private static final String STD_TEAM_CONCATINATION = ", ";
	private static final String ET_AL_TEAM_CONCATINATION_FULL = " & ";
	private static final String ET_AL_TEAM_CONCATINATION_ABBREV = " & ";


	final static UUID uuid = UUID.fromString("1cbda0d1-d5cc-480f-bf38-40a510a3f223");

	public static final String EMPTY_TEAM = "-empty team-";

	static public TeamDefaultCacheStrategy NewInstance(){
		return new TeamDefaultCacheStrategy();
	}

	private TeamDefaultCacheStrategy() {
		super();
	}

	@Override
	protected UUID getUuid() {
		return uuid;
	}

	@Override
    public String getNomenclaturalTitle(Team team) {
		String result = "";

		List<Person> teamMembers = team.getTeamMembers();
		int i = 0;
		for (Person teamMember : teamMembers){
			i++;
			String concat = concatString(team, teamMembers, i);
			result += concat + teamMember.getNomenclaturalTitle();
		}
		if (teamMembers.size() == 0){
			result = team.getTitleCache();
		}else if (team.isHasMoreMembers()){
		    result = addHasMoreMembers(result);
		}
		return result;
	}

    /**
     * Add the et al. to the team string
     * @param str team string without et al.
     * @return
     */
    public static String addHasMoreMembers(String str) {
        return str + ET_AL_TEAM_CONCATINATION_ABBREV + "al.";
    }

	@Override
    public String getTitleCache(Team team) {
		// TODO is still dummy
		String result = "";
		List<Person> teamMembers = team.getTeamMembers();//Hibernate.initialize(teamMembers);

		int i = 0;
		for (Person teamMember : teamMembers){
			i++;
			String concat = concatString(team, teamMembers, i);
			result += concat + teamMember.getTitleCache();
		}
		if (teamMembers.size() == 0){
			result = EMPTY_TEAM;
		} else if (team.isHasMoreMembers()){
		    result += ET_AL_TEAM_CONCATINATION_FULL + "al.";
		}
		return result;
	}

	public static String concatString(Team team, List<Person> teamMembers, int i) {
		String concat;
		if (i <= 1){
			concat = "";
		}else if (i < teamMembers.size() || ( team.isHasMoreMembers() && i == teamMembers.size())){
			concat = STD_TEAM_CONCATINATION;
		}else{
			concat = FINAL_TEAM_CONCATINATION;
		}
		return concat;
	}

}
