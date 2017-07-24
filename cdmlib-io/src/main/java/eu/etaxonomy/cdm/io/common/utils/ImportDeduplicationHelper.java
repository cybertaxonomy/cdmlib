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
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.io.common.ImportResult;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.media.RightsType;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.TaxonName;
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

    boolean referenceMapIsInitialized = false;
    boolean nameMapIsInitialized = false;
    boolean agentMapIsInitialized = false;
    boolean copyrightMapIsInitialized = false;

    private Map<String, Set<Reference>> refMap = new HashMap<>();
    private Map<String, Team> teamMap = new HashMap<>();
    private Map<String, Person> personMap = new HashMap<>();
    private Map<String, Institution> institutionMap = new HashMap<>();
    //using titleCache
    private Map<String, Set<INonViralName>> nameMap = new HashMap<>();
    private Map<String, Set<Rights>> copyrightMap = new HashMap<>();


    private IMatchStrategy referenceMatcher = DefaultMatchStrategy.NewInstance(Reference.class);
    private IMatchStrategy nameMatcher = DefaultMatchStrategy.NewInstance(TaxonName.class);



    public void restartSession(){
        restartSession(repository, null);
    }

    public void restartSession(ICdmRepository repository, ImportResult importResult){
        if (repository == null){
            return;
        }
        personMap = refreshMap(personMap, (IService)repository.getAgentService(), importResult);
        teamMap = refreshMap(teamMap, (IService)repository.getAgentService(), importResult);
        institutionMap = refreshMap(institutionMap, (IService)repository.getAgentService(), importResult);
    }


    /**
     * @param oldMap
     * @param service
     * @return
     */
    private <T extends ICdmBase> Map<String, T> refreshMap(Map<String, T> oldMap,
            IService<T> service, ImportResult importResult) {
        Map<String, T> newMap = new HashMap<>();
        for (String key : oldMap.keySet()){
            T old = oldMap.get(key);
            if (old!= null){
                T cdmBase = service.find(old.getUuid());
                if (cdmBase == null){
                    String message = "No cdm object was found for uuid " + old.getUuid() + " of class " + old.getClass().getSimpleName();
                    importResult.addWarning(message);
                }else{
                    newMap.put(key, cdmBase);
                }
            }else{
                String message = "Value for key " +  key + " was null in deduplication map";
                importResult.addWarning(message);
            }
        }
        return newMap;
    }

// ************************** FACTORY *******************************/

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
    private void putAgentBase(String title, AgentBase<?> agent){
        if (agent.isInstanceOf(Person.class) ){
            personMap.put(title, CdmBase.deproxy(agent, Person.class));
        }else if (agent.isInstanceOf(Team.class)){
            teamMap.put(title, CdmBase.deproxy(agent, Team.class));
        }else{
            institutionMap.put(title, CdmBase.deproxy(agent, Institution.class));
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

    //NAMES
    private void putName(String title, INonViralName name){
        Set<INonViralName> names = nameMap.get(title);
        if (names == null){
            names = new HashSet<>();
            nameMap.put(title, names);
        }
        names.add(name);
    }
    private Set<INonViralName> getNames(String title){
        return nameMap.get(title);
    }

    private Optional<INonViralName> getMatchingName(INonViralName existing){
        Predicate<INonViralName> matchFilter = name ->{
            try {
                return nameMatcher.invoke(name, existing);
            } catch (MatchException e) {
                throw new RuntimeException(e);
            }
        };
        return Optional.ofNullable(getNames(existing.getTitleCache()))
                .orElse(new HashSet<>())
                .stream()
                .filter(matchFilter)
                .findAny();
    }

// **************************** METHODS *****************************/

    /**
     * This method replaces name authors, nomenclatural reference and
     * nomenclatural reference author by existing authors and references
     * if matching authors or references exist. If not, the given authors
     * and references are added to the map of existing entities.
     *
     * @param state the import state
     * @param name the name with authors and references to replace
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

    public AgentBase<?> getExistingAgent(STATE state,
            AgentBase<?> agent) {
        if (agent == null){
            return null;
        } else if (agent.isInstanceOf(TeamOrPersonBase.class)){
            return getExistingAuthor(state, CdmBase.deproxy(agent, TeamOrPersonBase.class));
        }else{
            initAgentMap(state);
            Institution result = institutionMap.get(agent.getTitleCache());
            if (result == null){
                putAgentBase(agent.getTitleCache(), agent);
                result = CdmBase.deproxy(agent, Institution.class);
            }
            return result;
        }
    }


    /**
     * @param state
     *
     */
    @SuppressWarnings("rawtypes")
    private void initAgentMap(STATE state) {
        if (!agentMapIsInitialized && repository != null){
            List<String> propertyPaths = Arrays.asList("");
            List<AgentBase> existingAgents = repository.getAgentService().list(null, null, null, null, propertyPaths);
            for (AgentBase agent : existingAgents){
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

   /**
    * @param state
    * @param name
    */
   public <NAME extends INonViralName> NAME getExistingName(STATE state, NAME name) {
       if (name == null){
           return null;
       }else{
           initNameMap(state);
           @SuppressWarnings("unchecked")
           NAME result = (NAME)getMatchingName(name).orElse(null);
           if (result == null){
               result = name;
               Set<HybridRelationship> parentRelations = result.getHybridChildRelations();
               for (HybridRelationship rel : parentRelations){
                   INonViralName parent = rel.getParentName();
                   if (parent != null){
                       rel.setParentName(getExistingName(state, parent));
                   }
               }
               putName(result.getTitleCache(), result);
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
   private void initNameMap(STATE state) {
       if (!nameMapIsInitialized && repository != null){
           List<String> propertyPaths = Arrays.asList("");
           List<TaxonName> existingNames = repository.getNameService().list(null, null, null, null, propertyPaths);
           for (TaxonName name : existingNames){
               putName(name.getTitleCache(), name);
           }
          nameMapIsInitialized = true;
       }
   }

   public Rights getExistingCopyright(STATE state,
           Rights right) {
       if (right == null || !RightsType.COPYRIGHT().equals(right.getType())){
           return null;
       }else{
           initCopyrightMap(state);
           String key = makeCopyrightKey(right);
           Set<Rights> set = copyrightMap.get(key);
           if (set == null || set.isEmpty()){
               putCopyright(key, right);
               return right;
           }else if (set.size()>1){
               //TODO
               logger.warn("More than 1 matching copyright not yet handled for key: " + key);
           }
           return set.iterator().next();
       }
   }

    /**
     * @param state
     */
    private void initCopyrightMap(STATE state) {
        if (!copyrightMapIsInitialized && repository != null){
            List<String> propertyPaths = Arrays.asList("");
            List<Rights> existingRights = repository.getRightsService().list(null, null, null, null, propertyPaths);
            for (Rights right : existingRights){
                if (RightsType.COPYRIGHT().equals(right.getType())){
                    putCopyright(makeCopyrightKey(right), right);
                }
            }
            copyrightMapIsInitialized = true;
        }

    }

    /**
     * @param makeCopyrightKey
     * @param right
     */
    private void putCopyright(String key, Rights right) {
        Set<Rights> rights = copyrightMap.get(key);
        if (rights == null){
            rights = new HashSet<>();
            copyrightMap.put(key, rights);
        }
        rights.add(right);

    }

    /**
     * @param right
     * @return
     */
    private String makeCopyrightKey(Rights right) {
        if (right.getAgent() != null){
            return right.getAgent().getTitleCache();
        }else if (right.getText() != null){
            return right.getText();
        }else {
            logger.warn("Key for copyright could not be created: " + right);
            return right.getUuid().toString();
        }
    }


}
