// $Id$
/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.taxonx2013;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author pkelbert
 * @date 2 avr. 2013
 *
 */
public class TaxonXExtractor {

    protected final static String SPLITTER = ",";
    Logger logger = Logger.getLogger(getClass());

    protected  int askQuestion(String question){
        Scanner scan = new Scanner(System.in);
        logger.info(question);
        int index = scan.nextInt();
        return index;
    }


    /**
     * @param reftype
     * @return
     */
    protected Reference<?> getReferenceType(int reftype) {
        Reference<?> ref = null;
        switch (reftype) {
        case 1:
            ref = ReferenceFactory.newGeneric();
            break;
        case 2:
            IBook tmp= ReferenceFactory.newBook();
            ref = (Reference<?>)tmp;
            break;
        case 3:
            ref = ReferenceFactory.newArticle();
            break;
        case 4:
            IBookSection tmp2 = ReferenceFactory.newBookSection();
            ref = (Reference<?>)tmp2;
            break;
        case 5:
            ref = ReferenceFactory.newJournal();
            break;
        case 6:
            ref = ReferenceFactory.newPrintSeries();
            break;
        case 7:
            ref = ReferenceFactory.newThesis();
            break;
        default:
            break;
        }
        return ref;
    }
    /**
     * @param unitsList
     * @param state
     */
    protected void prepareCollectors(TaxonXImportState state,IAgentService agentService) {
        logger.info("PREPARE COLLECTORS");
        List<String> collectors = new ArrayList<String>();
//        List<String> teams = new ArrayList<String>();
//        List<List<String>> collectorinteams = new ArrayList<List<String>>();
        String tmp;

//        for (int i = 0; i < unitsList.getLength(); i++) {
//            this.getCollectorsFromXML((Element) unitsList.item(i));
//            for (String agent : dataHolder.gatheringAgentList) {
//                collectors.add(agent);
//            }
//            List<String> tmpTeam = new ArrayList<String>(new HashSet<String>(dataHolder.gatheringTeamList));
//            if(!tmpTeam.isEmpty()) {
//                teams.add(StringUtils.join(tmpTeam.toArray()," & "));
//            }
//            for (String agent:tmpTeam) {
//                collectors.add(agent);
//            }
//        }

        List<String> collectorsU = new ArrayList<String>(new HashSet<String>(collectors));
//        List<String> teamsU = new ArrayList<String>(new HashSet<String>(teams));


        //existing teams in DB
//        Map<String,Team> titleCacheTeam = new HashMap<String, Team>();
//        List<UuidAndTitleCache<Team>> hiberTeam = agentService.getTeamUuidAndTitleCache();

        Set<UUID> uuids = new HashSet<UUID>();
//        for (UuidAndTitleCache<Team> hibernateT:hiberTeam){
//            uuids.add(hibernateT.getUuid());
//        }
//        if (!uuids.isEmpty()){
//            List<AgentBase> existingTeams = agentService.find(uuids);
//            for (AgentBase existingP:existingTeams){
//                titleCacheTeam.put(existingP.getTitleCache(),(Team) existingP);
//            }
//        }


//        Map<String,UUID> teamMap = new HashMap<String, UUID>();
//        for (UuidAndTitleCache<Team> uuidt:hiberTeam){
//            teamMap.put(uuidt.getTitleCache(), uuidt.getUuid());
//        }

        //existing persons in DB
        List<UuidAndTitleCache<Person>> hiberPersons = agentService.getPersonUuidAndTitleCache();
        Map<String,Person> titleCachePerson = new HashMap<String, Person>();
        uuids = new HashSet<UUID>();
        for (UuidAndTitleCache<Person> hibernateP:hiberPersons){
            uuids.add(hibernateP.getUuid());
        }

        if (!uuids.isEmpty()){
            List<AgentBase> existingPersons = agentService.find(uuids);
            for (AgentBase existingP:existingPersons){
                titleCachePerson.put(existingP.getTitleCache(),(Person) existingP);
            }
        }

        Map<String,UUID> personMap = new HashMap<String, UUID>();
        for (UuidAndTitleCache<Person> person:hiberPersons){
            personMap.put(person.getTitleCache(), person.getUuid());
        }

        java.util.Collection<AgentBase> personToadd = new ArrayList<AgentBase>();
//        java.util.Collection<AgentBase> teamToAdd = new ArrayList<AgentBase>();

        for (String collector:collectorsU){
            Person p = Person.NewInstance();
            p.setTitleCache(collector,true);
            if (!personMap.containsKey(p.getTitleCache())){
                personToadd.add(p);
            }
        }
//        for (String team:teamsU){
//            Team p = Team.NewInstance();
//            p.setTitleCache(team,true);
//            if (!teamMap.containsKey(p.getTitleCache())){
//                teamToAdd.add(p);
//            }
//        }



        if(!personToadd.isEmpty()){
            Map<UUID, AgentBase> uuuidPerson = agentService.save(personToadd);
            for (UUID u:uuuidPerson.keySet()){
                titleCachePerson.put(uuuidPerson.get(u).getTitleCache(),(Person) uuuidPerson.get(u) );
            }
        }



//        Person ptmp ;
//        Map <String,Integer>teamdone = new HashMap<String, Integer>();
//        for (List<String> collteam: collectorinteams){
//            if (!teamdone.containsKey(StringUtils.join(collteam.toArray(),"-"))){
//                Team team = new Team();
//                boolean em =true;
//                for (String collector:collteam){
//                    ptmp = Person.NewInstance();
//                    ptmp.setTitleCache(collector,true);
//                    Person p2 = titleCachePerson.get(ptmp.getTitleCache());
//                    team.addTeamMember(p2);
//                    em=false;
//                }
//                if (!em) {
//                    teamToAdd.add(team);
//                }
//                teamdone.put(StringUtils.join(collteam.toArray(),"-"),0);
//            }
//        }
//
//        if(!teamToAdd.isEmpty()){
//            Map<UUID, AgentBase> uuuidTeam =  agentService.save(teamToAdd);
//            for (UUID u:uuuidTeam.keySet()){
//                titleCacheTeam.put(uuuidTeam.get(u).getTitleCache(), (Team) uuuidTeam.get(u) );
//            }
//        }

//        state.getConfig().setTeams(titleCacheTeam);
        state.getConfig().setPersons(titleCachePerson);
    }
}
