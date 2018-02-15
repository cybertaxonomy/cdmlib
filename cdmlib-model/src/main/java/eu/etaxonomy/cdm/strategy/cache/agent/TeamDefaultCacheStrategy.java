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

	public static final String EMPTY_TEAM = "-empty team-";

	final static UUID uuid = UUID.fromString("1cbda0d1-d5cc-480f-bf38-40a510a3f223");

	private int etAlPosition = 4;

// ************************* FACTORY ************************/

	static public TeamDefaultCacheStrategy NewInstance(){
		return new TeamDefaultCacheStrategy();
	}

    static public TeamDefaultCacheStrategy NewInstance(int etAlPosition){
        TeamDefaultCacheStrategy result = new TeamDefaultCacheStrategy();
        result.setEtAlPosition(etAlPosition);
        return result;
    }

// ********************* CONSTRUCTOR ***************************/

	private TeamDefaultCacheStrategy() {
		super();
	}

	@Override
	protected UUID getUuid() {
		return uuid;
	}

// ************** GETTER / SETTER *******************/

    public int getEtAlPosition() {
        return etAlPosition;
    }

    public void setEtAlPosition(int etAlPosition) {
        this.etAlPosition = etAlPosition;
    }

// *********************** MTEHODS ****************/

	@Override
    public String getNomenclaturalTitle(Team team) {
		String result = "";

		List<Person> teamMembers = team.getTeamMembers();
		int i = 0;
		for (Person teamMember : teamMembers){
			if(teamMember == null){
                // this can happen in UIs in the process of adding new members
			    continue;
			}
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


    @Override
    public String getTitleCache(Team team) {
        String result = "";
        List<Person> teamMembers = team.getTeamMembers();

        for (int i = 1; i <= teamMembers.size() && i < etAlPosition; i++){
            Person teamMember = teamMembers.get(i-1);
            if(teamMember == null){
                // this can happen in UIs in the process of adding new members
                continue;
            }
            String concat = concatString(team, teamMembers, i);
            result += concat + teamMember.getTitleCache();
        }
        if (teamMembers.size() == 0){
            result = EMPTY_TEAM;
        } else if (team.isHasMoreMembers() || teamMembers.size() >= etAlPosition){
            result += ET_AL_TEAM_CONCATINATION_FULL + "al.";
        }
        return result;
    }


	@Override
    public String getFullTitle(Team team) {
		String result = "";
		List<Person> teamMembers = team.getTeamMembers();

		int i = 0;
		for (Person teamMember : teamMembers){
		    if(teamMember == null){
                // this can happen in UIs in the process of adding new members
                continue;
            }
			i++;
			String concat = concatString(team, teamMembers, i);
			result += concat + teamMember.getFullTitle();
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


    /**
     * Add the et al. to the team string
     * @param str team string without et al.
     * @return
     */
    public static String addHasMoreMembers(String str) {
        return str + ET_AL_TEAM_CONCATINATION_ABBREV + "al.";
    }

}
