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
import java.util.UUID;
import java.util.function.Predicate;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.ImportResult;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.media.RightsType;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategyEqual;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.MatchException;
import eu.etaxonomy.cdm.strategy.match.MatchMode;
import eu.etaxonomy.cdm.strategy.match.MatchStrategyFactory;

/**
 * Helper class for deduplicating authors, references, names, etc.
 * during import.
 *
 * Note 2021: Was originally used as fast deduplication tool for commandline imports
 * into empty databases. Currently it is transformed into a deduplication tool that
 * can be used during application based imports.
 *
 * @author a.mueller
 * @since 11.02.2017
 */
/**
 * @author a.mueller
 * @date 27.01.2022
 *
 */
public class ImportDeduplicationHelper {

    private static final Logger logger = Logger.getLogger(ImportDeduplicationHelper.class);

    private ICdmRepository repository;

    //for possible future use
    @SuppressWarnings("unused")
    private ImportStateBase<?,?> state;

    public static final int NEVER_USE_MAP = 0;
    public static final int ALWAYS_USE_MAP = -1;
    //should deduplication use maps indexing the full database content? If yes, what is the maximum number of records for this.
    //If more records exist deduplication is done on the fly.
    //0 = never use map
    //-1 = always use map
    private int maxCountFullLoad = ALWAYS_USE_MAP;
    public int getMaxCountFullLoad() {
        return maxCountFullLoad;
    }
    public void setMaxCountFullLoad(int maxCountFullLoad) {
        this.maxCountFullLoad = maxCountFullLoad;
    }

    private enum Status{
        NOT_INIT,
        USE_MAP,
        USE_REPO;
    }

    private class DedupInfo<S extends IMatchable>{
        Class<S> clazz;
        IMatchStrategyEqual defaultMatcher;
        IMatchStrategy parsedMatcher;
        Map<String, Set<S>> map = new HashMap<>();
        Status status = Status.NOT_INIT;

        @SuppressWarnings("unchecked")
        private DedupInfo(Class<S> clazz, DedupMap dedupMap){
            this.clazz = clazz;
            if (IMatchable.class.isAssignableFrom(clazz)) {
                defaultMatcher = DefaultMatchStrategy.NewInstance(clazz);
                if (Reference.class.isAssignableFrom(clazz)) {
                    parsedMatcher = MatchStrategyFactory.NewParsedReferenceInstance();
                }else if (TeamOrPersonBase.class.isAssignableFrom(clazz)) {
                    parsedMatcher = MatchStrategyFactory.NewParsedTeamOrPersonInstance();
//                }else if (TaxonName.class.isAssignableFrom(clazz)){
//                    parsedMatcher = MatchStrategyFactory.NewParsedTaxonNameInstance();
                }
            }
            dedupMap.put(clazz, this);
        }
        @Override
        public String toString() {
            return clazz.getSimpleName() + ":" + status.name()+":mapsize=" + map.size()+":"+ (defaultMatcher == null?"without":"with") + " defaultMatcher" + (parsedMatcher == null? "" : " and with parsedMatcher");
        }
    }

    private class DedupMap<T extends IMatchable> extends HashMap<Class<T>, DedupInfo<T>>{
        private static final long serialVersionUID = 3757206594833330646L;
    }
    private DedupMap<? extends IdentifiableEntity> dedupMap = new DedupMap<>();

    private DedupInfo<Reference> referenceDedupInfo = new DedupInfo<>(Reference.class, dedupMap);
    private DedupInfo<Person> personDedupInfo = new DedupInfo<>(Person.class, dedupMap);
    private DedupInfo<Team> teamDedupInfo = new DedupInfo<>(Team.class, dedupMap);
    private DedupInfo<TaxonName> nameDedupInfo = new DedupInfo<>(TaxonName.class, dedupMap);


    @SuppressWarnings("unused")
    private Status institutionStatus = Status.NOT_INIT;
    private Status copyrightStatus = Status.NOT_INIT;
    private Status collectionStatus = Status.NOT_INIT;

    private Map<String, Set<Institution>> institutionMap = new HashMap<>();
    //using titleCache
    private Map<String, Set<Rights>> copyrightMap = new HashMap<>();
    private Map<String, Set<Collection>> collectionMap = new HashMap<>();

    /**
     * Clears all internal maps.
     */
    public void reset() {
        dedupMap.values().forEach(di->{di.map.clear();di.status=Status.NOT_INIT;});
        institutionMap.clear();
        copyrightMap.clear();
        collectionMap.clear();
    }

//    private IMatchStrategy collectionMatcher = DefaultMatchStrategy.NewInstance(Collection.class);

 // ************************** FACTORY *******************************/

     public static <STATE extends ImportStateBase<?,?>> ImportDeduplicationHelper NewInstance(ICdmRepository repository, STATE state){
         return new ImportDeduplicationHelper(repository, state);
     }

 // ************************ CONSTRUCTOR *****************************/

    private ImportDeduplicationHelper(ICdmRepository repository, ImportStateBase<?,?> state) {
         this.repository = repository;
         if (repository == null){
             logger.warn("Repository is null. Deduplication does not work against database.");
         }
         if (state == null){
             logger.warn("State is null. Deduplication works without state.");
         }
         this.state = state;
         try {
             dedupMap.get(Reference.class).defaultMatcher.setMatchMode("title", MatchMode.EQUAL);
             dedupMap.get(Team.class).defaultMatcher.setMatchMode("nomenclaturalTitleCache", MatchMode.EQUAL);
         } catch (MatchException e) {
             throw new RuntimeException(e);  //should not happen
         }
    }

    public void restartSession(){
        restartSession(repository, null);
    }

    /**
     * Clears all internal maps and loads them with same data as before but in current session.
     */
    public void restartSession(ICdmRepository repository, ImportResult importResult){
        if (repository == null){
            return;
        }
        referenceDedupInfo.map = refreshSetMap(referenceDedupInfo.map, (IService)repository.getReferenceService(), importResult);
        personDedupInfo.map = refreshSetMap(personDedupInfo.map, (IService)repository.getAgentService(), importResult);
        teamDedupInfo.map = refreshSetMap(teamDedupInfo.map, (IService)repository.getAgentService(), importResult);
        institutionMap = refreshSetMap(institutionMap, (IService)repository.getAgentService(), importResult);

        nameDedupInfo.map = refreshSetMap(nameDedupInfo.map, (IService)repository.getNameService(), importResult);
        collectionMap = refreshSetMap(collectionMap, (IService)repository.getCollectionService(), importResult);
        copyrightMap = refreshSetMap(copyrightMap, (IService)repository.getRightsService(), importResult);
    }

    //maybe this was used for Institution before
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
                    newMap.put(key, CdmBase.deproxy(cdmBase));
                }
            }else{
                String message = "Value for key " +  key + " was null in deduplication map";
                importResult.addWarning(message);
            }
        }
        return newMap;
    }

    private <T extends ICdmBase> Map<String, Set<T>> refreshSetMap(Map<String, Set<T>> oldMap,
            IService<T> service, ImportResult importResult) {

        Map<String, Set<T>> newMap = new HashMap<>();
        //create UUID set
        Set<UUID> uuidSet = new HashSet<>();
        for (String key : oldMap.keySet()){
            Set<T> oldSet = oldMap.get(key);
            for (T item : oldSet){
                UUID uuid = item.getUuid();
                uuidSet.add(uuid);
            }
        }
        //create uuid-item map
        Map<UUID, T> itemMap = new HashMap<>();
        List<T> list = service.find(uuidSet);
        for (T item : list){
            itemMap.put(item.getUuid(), item);
        }
        //refresh
        for (String key : oldMap.keySet()){
            Set<T> oldSet = oldMap.get(key);
            Set<T> newSet = new HashSet<>();
            if (oldSet != null){
                newMap.put(key, newSet);
                for (T item : oldSet){
                    T cdmBase = CdmBase.deproxy(itemMap.get(item.getUuid()));
                    if (cdmBase == null){
                        String message = "No cdm object was found for uuid " + item.getUuid() + " of class " + item.getClass().getSimpleName();
                        importResult.addWarning(message);
                    }else{
                        newSet.add(cdmBase);
                    }
                }
            }else{
                String message = "Value for key " +  key + " was null in deduplication map";
                importResult.addWarning(message);
            }
        }
        return newMap;
    }

//************************ PUTTER / GETTER *****************************/

    //ENTITY
    private <S extends IdentifiableEntity<?>> void putEntity(String title, S entity, Map<String,Set<S>> map){
        Set<S> entitySet = map.get(title);
        if (entitySet == null){
            entitySet = new HashSet<>();
            map.put(title, entitySet);
        }
        entitySet.add(CdmBase.deproxy(entity));
    }

    private <S extends IMatchable> Set<S> getEntityByTitle(String title, DedupInfo<S> dedupInfo){
        return dedupInfo.map.get(title);
    }

    private <S extends IMatchable> Optional<S> getMatchingEntity(S entityOrig, DedupInfo<S> dedupInfo, boolean parsed){
        S entity = CdmBase.deproxy(entityOrig);
        //choose matcher depending on the type of matching required. If matching of a parsed entity is required
        //   try to use the parsed matcher (if it exists)
        IMatchStrategy matcher = parsed && dedupInfo.parsedMatcher != null ? dedupInfo.parsedMatcher : dedupInfo.defaultMatcher;
        Predicate<S> matchFilter = persistedEntity ->{
            try {
                return matcher.invoke((IMatchable)entity, (IMatchable)persistedEntity).isSuccessful();
            } catch (MatchException e) {
                throw new RuntimeException(e);
            }
        };
        //TODO casting
        Optional<S> result = Optional.ofNullable(getEntityByTitle(((IdentifiableEntity<?>)entity).getTitleCache(), dedupInfo))
                .orElse(new HashSet<>())
                .stream()
                .filter(matchFilter)
                .findAny();
        if (result.isPresent() || dedupInfo.status == Status.USE_MAP || repository == null){
            return result;
        }else {
            try {
                return (Optional)repository.getCommonService().findMatching((IMatchable)entity, matcher).stream().findFirst();
            } catch (MatchException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // AGENTS
    private void putAgentBase(String title, AgentBase<?> agent){
        if (agent.isInstanceOf(Person.class) ){
            putEntity(title, CdmBase.deproxy(agent, Person.class), personDedupInfo.map);
        }else if (agent.isInstanceOf(Team.class)){
            putEntity(title, CdmBase.deproxy(agent, Team.class), teamDedupInfo.map);
        }else{
            putEntity(title, CdmBase.deproxy(agent, Institution.class), institutionMap);
        }
    }

    private <T extends TeamOrPersonBase<?>> T getTeamOrPerson(T agent, boolean parsed){
        T result = agent;
        if (agent.isInstanceOf(Person.class)){
            result = (T)getMatchingEntity(CdmBase.deproxy(agent, Person.class), personDedupInfo, parsed).orElse(null) ; // personMap.get(title);
        }else if (agent.isInstanceOf(Team.class)) {
            result = (T)getMatchingEntity(CdmBase.deproxy(agent, Team.class), teamDedupInfo, parsed).orElse(null); // teamMap.get(title);
        }
        return result;
    }

    //COLLECTIONS
    private Set<Collection> getCollections(String title){
        return collectionMap.get(title);
    }

    private Optional<Collection> getMatchingCollections(Collection existing){
        Predicate<Collection> matchFilter = collection ->{
//            try {
                //TODO right Collection matching
                if (CdmUtils.nullSafeEqual(collection.getName(), existing.getName())
                        && CdmUtils.nullSafeEqual(collection.getCode(), existing.getCode())){
                    return true;
                }else{
                    return false;
                }
//                return collectionMatcher.invoke(collection, existing);
//            } catch (MatchException e) {
//                throw new RuntimeException(e);
//            }
        };
        return Optional.ofNullable(getCollections(existing.getTitleCache()))
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
    public void replaceAuthorNamesAndNomRef(INonViralName name) {

        boolean parsed = true;
        TeamOrPersonBase<?> combAuthor = name.getCombinationAuthorship();
        name.setCombinationAuthorship(getExistingAuthor(combAuthor, parsed));
        if (combAuthor == name.getCombinationAuthorship()) {
            replaceTeamMembers(combAuthor, parsed);
        }

        TeamOrPersonBase<?> exAuthor = name.getExCombinationAuthorship();
        name.setExCombinationAuthorship(getExistingAuthor(exAuthor, parsed));
        if (exAuthor == name.getExCombinationAuthorship()) {
            replaceTeamMembers(exAuthor, parsed);
        }

        TeamOrPersonBase<?> basioAuthor = name.getBasionymAuthorship();
        name.setBasionymAuthorship(getExistingAuthor(basioAuthor, parsed));
        if (basioAuthor == name.getBasionymAuthorship()) {
            replaceTeamMembers(basioAuthor, parsed);
        }

        TeamOrPersonBase<?> exBasioAuthor = name.getExBasionymAuthorship();
        name.setExBasionymAuthorship(getExistingAuthor(exBasioAuthor, parsed));
        if (exBasioAuthor == name.getExBasionymAuthorship()) {
            replaceTeamMembers(exBasioAuthor, parsed);
        }

        Reference newNomRef = name.getNomenclaturalReference();
        Reference newOrExistingNomRef = getExistingReference(newNomRef, parsed);
        if (newNomRef != null) {
            if (newOrExistingNomRef == newNomRef){
                replaceReferenceRelatedData(newNomRef, parsed);
            }else {
                name.setNomenclaturalReference(newOrExistingNomRef);
            }
        }
    }

    public void replaceReferenceRelatedData(Reference ref, boolean parsed) {

        //author
        TeamOrPersonBase<?> newAuthor = ref.getAuthorship();
        TeamOrPersonBase<?> newOrExistingAuthor = getExistingAuthor(newAuthor, parsed);
        if (newAuthor != null) {
            if (newOrExistingAuthor == newAuthor) {
                replaceTeamMembers(newAuthor, parsed);
            }else {
                ref.setAuthorship(newOrExistingAuthor);
            }
        }

        //in-ref
        Reference newInRef = ref.getInReference();
        Reference newOrExistingInRef = getExistingReference(newInRef, parsed);
        if (newInRef != null) {
            if (newOrExistingInRef == newInRef){
                replaceReferenceRelatedData(newInRef, parsed);
            }else {
                ref.setInReference(newOrExistingInRef);
            }
        }
    }

    private void replaceTeamMembers(TeamOrPersonBase<?> teamOrPerson, boolean parsed) {
        if (teamOrPerson != null && teamOrPerson.isInstanceOf(Team.class)) {
            Team team = CdmBase.deproxy(teamOrPerson, Team.class);

            for (int i = 0; i < team.getTeamMembers().size(); i++) {
                Person person = team.getTeamMembers().get(i);
                team.getTeamMembers().set(i, getExistingAuthor(person, parsed));
            }
        }
    }

    public <T extends TeamOrPersonBase<?>> T getExistingAuthor(T author, boolean parsed) {
        if (author == null){
            return null;
        }else{
            init(personDedupInfo);
            init(teamDedupInfo);
            initAuthorTitleCaches(author);
            T result = getTeamOrPerson(author, parsed);
            if (result == null){
                putAgentBase(author.getTitleCache(), author);
                if (author.isInstanceOf(Team.class)){
                    handleTeam(CdmBase.deproxy(author, Team.class), parsed);
                }
                result = author;
            }
            return result;
        }
    }

    private <T extends TeamOrPersonBase<?>> void initAuthorTitleCaches(T teamOrPerson) {
        if (teamOrPerson == null) {
            return;
        }
        //NOTE: this is more or less redundant copy from CdmPreDataChangeListener
        if (teamOrPerson.isInstanceOf(Team.class)){
            Team team = CdmBase.deproxy(teamOrPerson, Team.class);
            if (!team.isProtectedNomenclaturalTitleCache()){
                team.setNomenclaturalTitleCache(null, false);
            }
            if (!team.isProtectedCollectorTitleCache()){
                team.setCollectorTitleCache(null, false);
            }
            //not redundant part
            for (Person member : team.getTeamMembers()) {
                initAuthorTitleCaches(member);
            }
            //end not redundant part
        }
        teamOrPerson.getNomenclaturalTitleCache();
        teamOrPerson.getCollectorTitleCache();
        if (! teamOrPerson.isProtectedTitleCache()){
            teamOrPerson.setTitleCache(teamOrPerson.generateTitle(), false);
        }
    }

    private void initReferenceCaches(Reference ref) {
        if (ref == null) {
            return;
        }
        ////TODO better do via matching strategy  (newReference might have caches == null)
        //the below is more or less a copy from CdmPreDataChangeListener (except for inReference handling)
        ref.getAbbrevTitleCache();
        ref.getTitleCache();

        initAuthorTitleCaches(ref.getAuthorship());
        initReferenceCaches(ref.getInReference());
   }

    public AgentBase<?> getExistingAgent(AgentBase<?> agent, boolean parsed) {
        if (agent == null){
            return null;
        } else if (agent.isInstanceOf(TeamOrPersonBase.class)){
            return getExistingAuthor(CdmBase.deproxy(agent, TeamOrPersonBase.class), parsed);
        }else{
            throw new RuntimeException("Institution matching not yet implemented");
//            initInstitutionMap();
//            Set<Institution> result = institutionMap.get(agent.getTitleCache());
//            if (result == null){
//                result = putEntity(agent.getTitleCache(), CdmBase.deproxy(agent, Institution.class), institutionMap);
//            }
//            return result;
        }
    }

    private <S extends IMatchable> void init(DedupInfo<S> dedupInfo) {
        dedupInfo.status = init(dedupInfo.clazz, dedupInfo.status, dedupInfo.map);
    }

    private <S extends IMatchable> Status init(Class<S> clazz, Status status, Map<String,Set<S>> map) {

        //FIXME cast
        Class<IdentifiableEntity> entityClass = (Class)clazz;
        if (status == Status.NOT_INIT && repository != null){
            if (maxCountFullLoad != NEVER_USE_MAP){
                long nExisting = -2;
                if (maxCountFullLoad != ALWAYS_USE_MAP){
                    nExisting = repository.getCommonService().count(entityClass);
                }
                if (nExisting <= maxCountFullLoad ){
                    List<String> propertyPaths = Arrays.asList("");
                    List<IdentifiableEntity> existingEntities = repository.getCommonService().list(entityClass, null, null, null, propertyPaths);
                    for (IdentifiableEntity<?> entity : existingEntities){
                        //TODO casting
                        putEntity(entity.getTitleCache(), entity, (Map)map);
                    }
                    return Status.USE_MAP;
                }else{
                    return Status.USE_REPO;
                }
            }else{
                return Status.USE_REPO;
            }
        }
        return status;
    }

    private void handleTeam(Team team, boolean parsed) {
        List<Person> members = team.getTeamMembers();
        for (int i =0; i< members.size(); i++){
            Person person = CdmBase.deproxy(members.get(i));
            Person existingPerson = getMatchingEntity(person, personDedupInfo, parsed).orElse(null);
            if (existingPerson != null){
                members.set(i, existingPerson);
            }else{
                putAgentBase(person.getTitleCache(), person);
            }
        }
    }

    public Collection getExistingCollection(Collection collection) {
        if (collection == null){
            return null;
        }else{
            initCollectionMap();
            Collection result = getMatchingCollections(collection).orElse(null);
            if (result == null){
                result = collection;
                putEntity(result.getTitleCache(), result, collectionMap);
            }else{
                if(logger.isDebugEnabled()) {
                    logger.debug("Matches");
                 }
            }
            return result;
        }
    }

    private void initCollectionMap() {
        if (collectionStatus == Status.NOT_INIT && repository != null){
            List<String> propertyPaths = Arrays.asList("");
            List<Collection> existingCollections = repository.getCollectionService().list(null, null, null, null, propertyPaths);
            for (Collection collection : existingCollections){
                putEntity(collection.getTitleCache(), collection, collectionMap);
            }
        }
        collectionStatus = Status.USE_MAP;
//      collectionStatus = init(Collection.class, collectionStatus, collectionMap); //for future, once Collection becomes IMatchable
    }

   /**
     * Returns an existing matching persistend reference or the the given reference
     * if no matching reference exists.
     * @param ref given reference
     * @param parsed if <code>true</code> use matching strategy for parsed references,
     *               the default matching strategy otherwise
     * @return matching reference
     */
   public Reference getExistingReference(Reference ref, boolean parsed) {
       if (ref == null){
           return null;
       }else{
           init(referenceDedupInfo);
           initReferenceCaches(ref);
           Reference result = getMatchingEntity(ref, referenceDedupInfo, parsed).orElse(null);
           if (result == null){
               result = ref;
               Reference inRef = result.getInReference();
               if (inRef != null){
                   result.setInReference(getExistingReference(result.getInReference(), parsed));
               }
               putEntity(result.getTitleCache(), result, referenceDedupInfo.map);
           }else{
               if(logger.isDebugEnabled()) {logger.debug("Matches");}
           }
           return result;
       }
   }

   public TaxonName getExistingName(TaxonName name, boolean parsed) {
       if (name == null){
           return null;
       }else{
           init(nameDedupInfo);
           TaxonName result = getMatchingEntity(name, nameDedupInfo, parsed).orElse(null);
           if (result == null){
               result = name;
               Set<HybridRelationship> parentRelations = result.getHybridChildRelations();
               for (HybridRelationship rel : parentRelations){
                   TaxonName parent = rel.getParentName();
                   if (parent != null){
                       rel.setParentName(getExistingName(parent, parsed));
                   }
               }
               putEntity(result.getTitleCache(), result, nameDedupInfo.map);
           }else{
               if(logger.isDebugEnabled()) {
                   logger.debug("Matches");
                }
           }
           return result;
       }
   }

   public Rights getExistingCopyright(Rights right) {
       if (right == null || !RightsType.COPYRIGHT().equals(right.getType())){
           return null;
       }else{
           initCopyrightMap();
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

    private void initCopyrightMap() {
        if (copyrightStatus == Status.NOT_INIT && repository != null){
            List<String> propertyPaths = Arrays.asList("");
            List<Rights> existingRights = repository.getRightsService().list(null, null, null, null, propertyPaths);
            for (Rights right : existingRights){
                if (RightsType.COPYRIGHT().equals(right.getType())){
                    putCopyright(makeCopyrightKey(right), right);
                }
            }
            copyrightStatus = Status.USE_MAP;
        }
    }

    private void putCopyright(String key, Rights right) {
        Set<Rights> rights = copyrightMap.get(key);
        if (rights == null){
            rights = new HashSet<>();
            copyrightMap.put(key, rights);
        }
        rights.add(CdmBase.deproxy(right));
    }

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
