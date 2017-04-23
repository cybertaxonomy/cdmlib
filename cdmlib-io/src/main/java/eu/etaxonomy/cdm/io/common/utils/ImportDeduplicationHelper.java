/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.MatchException;

/**
 * Helper class for deduplicating authors, references, names, etc.
 * during import.
 * @author a.mueller
 * @date 11.02.2017
 *
 */
public class ImportDeduplicationHelper<STATE extends ImportStateBase<?,?>> {
    private static final Logger logger = Logger.getLogger(ImportDeduplicationHelper.class);

    private ICdmRepository repository;


    boolean referenceMapIsInitialized;

    private Map<String, Set<Reference>> refMap = new HashMap<>();

    private Map<String, Team> teamMap = new HashMap<>();

    private Map<String, Person> personMap = new HashMap<>();

    private IMatchStrategy referenceMatcher = DefaultMatchStrategy.NewInstance(Reference.class);

    //using titleCache
    private Map<String, INonViralName> nameMap = new HashMap<>();

    public static ImportDeduplicationHelper<?> NewInstance(ICdmRepository repository){
        return new ImportDeduplicationHelper<>(repository);
    }

    public static ImportDeduplicationHelper<?> NewStandaloneInstance(){
        return new ImportDeduplicationHelper<>(null);
    }

    /**
     * @param repository
     * @param state not used, only for correct casting of generics
     * @return
     */
    public static <STATE extends ImportStateBase<?,?>> ImportDeduplicationHelper<STATE> NewInstance(ICdmRepository repository, STATE state){
        return new ImportDeduplicationHelper<>(repository);
    }

// ************************ CONSTRUCTOR *****************************/

    public ImportDeduplicationHelper(ICdmRepository repository) {
        this.repository = repository;
        if (repository == null){
            logger.warn("Repository is null. Deduplication does not work agains database");
        }
    }

//************************ PUTTER / GETTER *****************************/

    //REFERENCES
    private void putReference(String title, Reference ref){
        Set<Reference> refs = refMap.get(title);
        if (refs == null){
            refs = new HashSet<>();
            refMap.put(title, refs);
        }
        refs.add(ref);
    }
    private Set<Reference> getReferences(String title){
        return refMap.get(title);
    }

    private Optional<Reference> getMatchingReference(Reference existing){
        Predicate<Reference> matchFilter = reference ->{
            try {
                return referenceMatcher.invoke(reference, existing);
            } catch (MatchException e) {
                throw new RuntimeException(e);
            }
        };
        return Optional.ofNullable(getReferences(existing.getTitleCache()))
                .orElse(new HashSet<>())
                .stream()
                .filter(matchFilter)
                .findAny();
    }

    // AGENTS
    private void putAgentBase(String title, TeamOrPersonBase<?> agent){
        if (agent.isInstanceOf(Person.class) ){
            personMap.put(title, CdmBase.deproxy(agent, Person.class));
        }else{
            teamMap.put(title, CdmBase.deproxy(agent, Team.class));
        }
    }

    private TeamOrPersonBase<?> getAgentBase(String title){
        TeamOrPersonBase<?> result = personMap.get(title);
        if (result == null){
            result = teamMap.get(title);
        }
        return result;
    }

    private Person getPerson(String title){
        return personMap.get(title);
    }

    /**
     * @param state
     * @param name
     */
    public void replaceAuthorNamesAndNomRef(STATE state,
            INonViralName name) {
        TeamOrPersonBase<?> combAuthor = name.getCombinationAuthorship();
        name.setCombinationAuthorship(getExistingAuthor(state, combAuthor));

        TeamOrPersonBase<?> exAuthor = name.getExCombinationAuthorship();
        name.setExCombinationAuthorship(getExistingAuthor(state, exAuthor));

        TeamOrPersonBase<?> basioAuthor = name.getBasionymAuthorship();
        name.setBasionymAuthorship(getExistingAuthor(state, basioAuthor));

        TeamOrPersonBase<?> exBasioAuthor = name.getExBasionymAuthorship();
        name.setExBasionymAuthorship(getExistingAuthor(state, exBasioAuthor));

        INomenclaturalReference nomRef = name.getNomenclaturalReference();
        if (nomRef != null){
            TeamOrPersonBase<?> refAuthor = nomRef.getAuthorship();
            nomRef.setAuthorship(getExistingAuthor(state, refAuthor));

            Reference existingRef = getExistingReference(state, (Reference)nomRef);
            if (existingRef != null){
                name.setNomenclaturalReference(existingRef);
            }
        }
    }

    /**
     * @param state
     * @param combAuthor
     * @return
     */
    public TeamOrPersonBase<?> getExistingAuthor(STATE state,
            TeamOrPersonBase<?> author) {
        if (author == null){
            return null;
        }else{
            initAgentMap(state);
            TeamOrPersonBase<?> result = getAgentBase(author.getTitleCache());
            if (result == null){
                putAgentBase(author.getTitleCache(), author);
                if (author instanceof Team){
                    handleTeam(state, (Team)author);
                }
                result = author;
            }
            return result;
        }
    }

    boolean agentMapIsInitialized = false;

    /**
     * @param state
     *
     */
    @SuppressWarnings("rawtypes")
    private void initAgentMap(STATE state) {
        if (!agentMapIsInitialized && repository != null){
            List<String> propertyPaths = Arrays.asList("");
            List<TeamOrPersonBase> existingAgents = repository.getAgentService().list(null, null, null, null, propertyPaths);
            for (TeamOrPersonBase agent : existingAgents){
                putAgentBase(agent.getTitleCache(), agent);
            }
            agentMapIsInitialized = true;
        }
    }

    /**
     * @param state
     * @param author
     */
    private void handleTeam(STATE state, Team team) {
        List<Person> members = team.getTeamMembers();
        for (int i =0; i< members.size(); i++){
            Person person = members.get(i);
            Person existingPerson = getPerson(person.getTitleCache());
            if (existingPerson != null){
                members.set(i, existingPerson);
            }else{
                putAgentBase(person.getTitleCache(), person);
            }
        }
    }

    /**
    * @param state
    * @param nomRef
    */
   public Reference getExistingReference(STATE state, Reference ref) {
       if (ref == null){
           return null;
       }else{
           initRerenceMap(state);
           Reference result = getMatchingReference(ref).orElse(null);
           if (result == null){
               result = ref;
               Reference inRef = result.getInReference();
               if (inRef != null){
                   result.setInReference(getExistingReference(state, result.getInReference()));
               }
               putReference(result.getTitleCache(), result);
           }else{
               if(logger.isDebugEnabled()) {
                   logger.debug("Matches");
                }
           }
           return result;
       }
   }

   /**
    * @param state
    */
   private void initRerenceMap(STATE state) {
       if (!referenceMapIsInitialized && repository != null){
           List<String> propertyPaths = Arrays.asList("");
           List<Reference> existingReferences = repository.getReferenceService().list(null, null, null, null, propertyPaths);
           for (Reference ref : existingReferences){
               putReference(ref.getTitleCache(), ref);
           }
           referenceMapIsInitialized = true;
       }

   }
}
