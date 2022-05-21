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
 */
public class TeamDefaultCacheStrategy extends StrategyBase implements INomenclaturalAuthorCacheStrategy<Team> {

    private static final long serialVersionUID = 8375295443642690479L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TeamDefaultCacheStrategy.class);


    public static final String FINAL_TEAM_CONCATINATION = " & ";
	public static final String STD_TEAM_CONCATINATION = ", ";
	public static final String ET_AL_TEAM_CONCATINATION_FULL = " & ";
	public static final String ET_AL_TEAM_CONCATINATION_ABBREV = " & ";

	private static final int DEFAULT_ET_AL_POS = Integer.MAX_VALUE;
	private static final int DEFAULT_NOM_ET_AL_POS = Integer.MAX_VALUE;

	public static final String EMPTY_TEAM = "-empty team-";

	private static TeamDefaultCacheStrategy instance;
	private static TeamDefaultCacheStrategy instance2;
	private static TeamDefaultCacheStrategy instance3;

	final static UUID uuid = UUID.fromString("1cbda0d1-d5cc-480f-bf38-40a510a3f223");

	private final int etAlPositionTitleCache;   //NO_ET_AL_POS is default,
    private final int etAlPositionNomTitleCache;   //NOMCACHE_ET_AL_POS is default
    private final int etAlPositionFamilyTitle;   //NOMCACHE_ET_AL_POS is default
    private final int etAlPositionFullTitle;   //NOMCACHE_ET_AL_POS is default
    private final int etAlPositionCollectorTitle;   //NOMCACHE_ET_AL_POS is default

// ************************* FACTORY ************************/

    public static TeamDefaultCacheStrategy NewInstance(){
		return new TeamDefaultCacheStrategy(DEFAULT_ET_AL_POS, DEFAULT_NOM_ET_AL_POS, DEFAULT_ET_AL_POS,
		        DEFAULT_ET_AL_POS, DEFAULT_ET_AL_POS);
	}
    public static TeamDefaultCacheStrategy NewInstanceTitleEtAl(int etAlPositionForTitleCache){
        return new TeamDefaultCacheStrategy(etAlPositionForTitleCache, DEFAULT_NOM_ET_AL_POS, DEFAULT_ET_AL_POS,
                DEFAULT_ET_AL_POS, DEFAULT_ET_AL_POS);
    }
    public static TeamDefaultCacheStrategy NewInstanceNomEtAl(int etAlPositionForNomenclaturalCache){
        return new TeamDefaultCacheStrategy(DEFAULT_ET_AL_POS, etAlPositionForNomenclaturalCache,
                DEFAULT_ET_AL_POS, DEFAULT_ET_AL_POS, DEFAULT_ET_AL_POS);
    }
    public static TeamDefaultCacheStrategy NewInstance(int etAlPostionForAll){
        return new TeamDefaultCacheStrategy(etAlPostionForAll, etAlPostionForAll,
                etAlPostionForAll, etAlPostionForAll, etAlPostionForAll);
    }

    public static TeamDefaultCacheStrategy INSTANCE(){
        if (instance == null){
            instance = NewInstance();
        }
        return instance;
    }

    public static TeamDefaultCacheStrategy INSTANCE_ET_AL_2(){
        if (instance2 == null){
            instance2 = NewInstance(2);
        }
        return instance2;
    }

    public static TeamDefaultCacheStrategy INSTANCE_ET_AL_3(){
        if (instance3 == null){
            instance3 = NewInstance(3);
        }
        return instance3;
    }

// ********************* CONSTRUCTOR ***************************/

	private TeamDefaultCacheStrategy(int etAlPositionTitleCache, int etAlPositionForNomenclaturalCache,
	        int etAlPositionFamilyTitle, int etAlPositionFullTitle, int etAlPositionCollectorTitle) {
	    this.etAlPositionTitleCache = etAlPositionTitleCache;
	    this.etAlPositionNomTitleCache = etAlPositionForNomenclaturalCache;
	    this.etAlPositionFamilyTitle = etAlPositionFamilyTitle;
	    this.etAlPositionFullTitle = etAlPositionFullTitle;
	    this.etAlPositionCollectorTitle = etAlPositionCollectorTitle;
	}

	@Override
	protected UUID getUuid() {
		return uuid;
	}

    private enum CacheType{
        TITLECACHE,
        ABBREV,
        FULL,
        COLLECTOR,
        FAMILY;

        private String getCache(Person member){
            if (this == TITLECACHE){
                return member.getTitleCache();
            }else if (this == ABBREV){
                return member.getNomenclaturalTitleCache();
            }else if (this == FULL){
                return member.getFullTitle();
            }else if (this == FAMILY){
                return member.cacheStrategy().getFamilyTitle(member);
            }else if (this == COLLECTOR){
              return member.getCollectorTitleCache();
            }
            throw new IllegalStateException("CacheType not supported: " + this);
        }
    }

// ************** GETTER *******************/

    public int getEtAlPositionNomTitleCache() {
        return etAlPositionNomTitleCache;
    }

    public int getEtAlPositionTitleCache() {
        return etAlPositionTitleCache;
    }

// *********************** MTEHODS ****************/


    @Override
    public String getTitleCache(Team team) {
        return getCache(team, CacheType.TITLECACHE, etAlPositionTitleCache);
    }

    @Override
    public String getNomenclaturalTitleCache(Team team) {
        return getCache(team, CacheType.ABBREV, etAlPositionNomTitleCache);
    }

    @Override
    public String getFullTitle(Team team) {
        return getCache(team, CacheType.FULL, etAlPositionFullTitle);
    }

    @Override
    public String getFamilyTitle(Team team) {
        return getCache(team, CacheType.FAMILY, etAlPositionFamilyTitle);
    }

    @Override
    public String getCollectorTitleCache(Team team) {
        return getCache(team, CacheType.COLLECTOR, etAlPositionCollectorTitle);
    }

    private String getCache(Team team, CacheType cacheType, int etAlPosition) {

        String result = "";
        List<Person> teamMembers = team.getTeamMembers();
        int size = teamMembers.size();
        for (int i = 1; i <= size && (i < etAlPosition || (size == etAlPosition && !team.isHasMoreMembers())); i++){
            Person teamMember = teamMembers.get(i-1);
            if(teamMember == null){
                // this can happen in UIs in the process of adding new members
                continue;
            }
            String concat = teamConcatSeparator(team, i);
            result += concat + cacheType.getCache(teamMember);
        }
        if (teamMembers.size() == 0){
            if (cacheType == CacheType.TITLECACHE){
                result = EMPTY_TEAM;
            }else{
                return team.getTitleCache();
            }
        } else if (team.isHasMoreMembers() || teamMembers.size() > etAlPosition){
            result = addHasMoreMembers(result);
        }
        return result;
    }

    /**
     * Computes the team concat separator for the member
     * at position <code>index</code>
     * @param team
     * @param index
     */
	public static String teamConcatSeparator(Team team, int index) {
	    List<Person> teamMembers = team.getTeamMembers();
	    if (index <= 1){
			return "";
		}else if (index < teamMembers.size() || ( team.isHasMoreMembers() && index == teamMembers.size())){
			return STD_TEAM_CONCATINATION;
		}else{
			return FINAL_TEAM_CONCATINATION;
		}
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