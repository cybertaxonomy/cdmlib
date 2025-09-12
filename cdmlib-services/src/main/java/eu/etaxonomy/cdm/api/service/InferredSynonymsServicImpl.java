/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.name.IZoologicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;

/**
 * @author muellera
 * @since 11.09.2025
 */
@Service
@Transactional(readOnly = true)
public class InferredSynonymsServicImpl
        implements IInferredSynonymsService {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private ITaxonDao taxonDao;

    @Autowired
    private ITaxonNameDao nameDao;

    @Override
    public List<Synonym> createAllInferredSynonyms(Taxon taxon, Classification tree,
            boolean doWithMisappliedNames, boolean includeUnpublished){

        List <Synonym> inferredSynonyms = new ArrayList<>();
        inferredSynonyms.addAll(createInferredSynonyms(taxon, tree, SynonymType.INFERRED_EPITHET_OF, doWithMisappliedNames, includeUnpublished));
        inferredSynonyms.addAll(createInferredSynonyms(taxon, tree, SynonymType.INFERRED_GENUS_OF, doWithMisappliedNames, includeUnpublished));
        inferredSynonyms.addAll(createInferredSynonyms(taxon, tree, SynonymType.POTENTIAL_COMBINATION_OF, doWithMisappliedNames, includeUnpublished));

        return inferredSynonyms;
    }

    @Override
    public List<Synonym> createInferredSynonyms(Taxon taxon, Classification classification,
            SynonymType inferredSynonymType, boolean doWithMisappliedNames,
            boolean includeUnpublished){


        TaxonNode node = taxon.getTaxonNode(classification);

        //do not handle root taxa
        if (node == null || node.isTopmostNode()){
            return new ArrayList<>();
        }

        //handle only species and species having genus, infragenus or species as parent
        Taxon parentTaxon = node.getParent().getTaxon();
        TaxonName parentName = CdmBase.deproxy(parentTaxon.getName());
        if (! ( parentName.isGenus()
                || parentName.isSpecies()
                || Rank.SUBGENUS().equals(parentName.getRank()))){
            return new ArrayList<>();
        }

        Map<UUID, TaxonName> zooHashMap = new HashMap<>();
        List<String> nameCacheList = new ArrayList<>();

        UUID nameUuid= taxon.getName().getUuid();
        IZoologicalName taxonName = getZoologicalName(nameUuid, zooHashMap);

        //define name parts/epithets
        String genusOfTaxon = taxonName.getGenusOrUninomial();
        String specificEpithetOfTaxon = null;
        String infraGenericEpithetOfTaxon = null;
        String infraSpecificEpithetOfTaxon = null;

        if (taxonName.isSpecies()){
             specificEpithetOfTaxon = taxonName.getSpecificEpithet();
        } else if (taxonName.isInfraGeneric()){
            infraGenericEpithetOfTaxon = taxonName.getInfraGenericEpithet();
        } else if (taxonName.isInfraSpecific()){
            infraSpecificEpithetOfTaxon = taxonName.getInfraSpecificEpithet();
        }

        //load synonyms
        //TODO do we really have to load them via dao? Shouldn't they be available
        //     in the taxon object (or do we only want to load persistent synonyms?
        List<String> propertyPaths = new ArrayList<>();
        propertyPaths.add("synonym");
        propertyPaths.add("synonym.name");
        List<OrderHint> orderHintsSynonyms = new ArrayList<>();
        orderHintsSynonyms.add(new OrderHint("titleCache", SortOrder.ASCENDING));

        List<Synonym> synonymsOfParent = taxonDao.getSynonyms(parentTaxon, SynonymType.HETEROTYPIC_SYNONYM_OF, null, null, orderHintsSynonyms, propertyPaths);
        List<Synonym> synonymsOfTaxon = taxonDao.getSynonyms(taxon, SynonymType.HETEROTYPIC_SYNONYM_OF,
                null, null, orderHintsSynonyms, propertyPaths);

        List<TaxonRelationship> taxonRelListParent = new ArrayList<>();
        List<TaxonRelationship> taxonRelListTaxon = new ArrayList<>();
        if (doWithMisappliedNames){
            List<OrderHint> orderHintsMisapplied = new ArrayList<>();
            orderHintsMisapplied.add(new OrderHint("relatedFrom.titleCache", SortOrder.ASCENDING));
            taxonRelListParent = taxonDao.getTaxonRelationships(parentTaxon, TaxonRelationshipType.MISAPPLIED_NAME_FOR(),
                    includeUnpublished, null, null, orderHintsMisapplied, propertyPaths, Direction.relatedTo);
            taxonRelListTaxon = taxonDao.getTaxonRelationships(taxon, TaxonRelationshipType.MISAPPLIED_NAME_FOR(),
                    includeUnpublished, null, null, orderHintsMisapplied, propertyPaths, Direction.relatedTo);
        }

        //inferred epithet
        if (inferredSynonymType.equals(SynonymType.INFERRED_EPITHET_OF)){

            return handleInferredEpithtet(taxon, doWithMisappliedNames, parentName, zooHashMap, nameCacheList,
                    taxonName, specificEpithetOfTaxon, infraGenericEpithetOfTaxon, infraSpecificEpithetOfTaxon,
                    synonymsOfParent, taxonRelListParent);

        //inferred genus
        } else if (inferredSynonymType.equals(SynonymType.INFERRED_GENUS_OF)){

            return handleInferredGenus(taxon, doWithMisappliedNames, parentName, zooHashMap, nameCacheList, taxonName,
                    genusOfTaxon, specificEpithetOfTaxon, infraSpecificEpithetOfTaxon, synonymsOfTaxon,
                    taxonRelListTaxon);

        //potential combination
        }else if (inferredSynonymType.equals(SynonymType.POTENTIAL_COMBINATION_OF)){

            return handlePotentialCombination(taxon, doWithMisappliedNames, zooHashMap, nameCacheList, synonymsOfParent,
                    synonymsOfTaxon, taxonRelListParent, taxonRelListTaxon);

        }else {
            //TODO handle better
            logger.info("The synonym type is not defined.");
            return new ArrayList<>();
        }
    }

    private List<Synonym> handlePotentialCombination(Taxon taxon, boolean doWithMisappliedNames,
            Map<UUID, TaxonName> zooHashMap, List<String> nameCacheList, List<Synonym> synonymsOfParent,
            List<Synonym> synonymsOfTaxon, List<TaxonRelationship> taxonRelListParent,
            List<TaxonRelationship> taxonRelListTaxon) {
        Reference sourceReference = null; // TODO: Determination of sourceReference is redundant

        List<Synonym> inferredSynonyms = new ArrayList<>();
        List<Synonym> inferredSynonymsToBeRemoved = new ArrayList<>();

        //for all synonyms of the parent...
        for (Synonym synonymOfParent: synonymsOfParent){

            // Set the sourceReference
            sourceReference = synonymOfParent.getSec();

            // Determine the idInSource
            String idInSourceParent = getIdInSource(synonymOfParent);

            IZoologicalName synonymOfParentZooName = getZoologicalName(synonymOfParent.getName().getUuid(), zooHashMap);
            String synParentGenus = synonymOfParentZooName.getGenusOrUninomial();
            String synParentInfragenericEpithet = null;
            String synParentSpecificEpithet = null;

            if (synonymOfParentZooName.isInfraGeneric()){
                synParentInfragenericEpithet = synonymOfParentZooName.getInfraGenericEpithet();
            }
            if (synonymOfParentZooName.isSpecies()){
                synParentSpecificEpithet = synonymOfParentZooName.getSpecificEpithet();
            }

           /* if (synGenusName != null && !synonymsGenus.containsKey(synGenusName)){
                synonymsGenus.put(synGenusName, idInSource);
            }*/

            //for all synonyms of the taxon

            for (Synonym synonymOfTaxon: synonymsOfTaxon){

                TaxonName zooSynName = getZoologicalName(synonymOfTaxon.getName().getUuid(), zooHashMap);
                Synonym potentialCombination = createPotentialCombination(idInSourceParent,
                        synonymOfParentZooName, zooSynName,
                        synParentGenus,
                        synParentInfragenericEpithet,
                        synParentSpecificEpithet, synonymOfTaxon, zooHashMap);

                taxon.addSynonym(potentialCombination, SynonymType.POTENTIAL_COMBINATION_OF);
                inferredSynonyms.add(potentialCombination);
                zooHashMap.put(potentialCombination.getName().getUuid(), potentialCombination.getName());
                nameCacheList.add(potentialCombination.getName().getNameCache());
            }
        }

        if (doWithMisappliedNames){

            for (TaxonRelationship parentRelationship: taxonRelListParent){

                Taxon misappliedParent = parentRelationship.getFromTaxon();
                TaxonName misappliedParentName = misappliedParent.getName();

                // Set the sourceReference
                sourceReference = misappliedParent.getSec();

                // Determine the idInSource
                String idInSourceParent = getIdInSource(misappliedParent);

                IZoologicalName parentSynZooName = getZoologicalName(misappliedParentName.getUuid(), zooHashMap);
                String synParentGenus = parentSynZooName.getGenusOrUninomial();
                String synParentInfragenericName = null;
                String synParentSpecificEpithet = null;

                if (parentSynZooName.isInfraGeneric()){
                    synParentInfragenericName = parentSynZooName.getInfraGenericEpithet();
                }
                if (parentSynZooName.isSpecies()){
                    synParentSpecificEpithet = parentSynZooName.getSpecificEpithet();
                }

                for (TaxonRelationship taxonRelationship: taxonRelListTaxon){
                    Taxon misappliedName = taxonRelationship.getFromTaxon();
                    TaxonName zooMisappliedName = getZoologicalName(misappliedName.getName().getUuid(), zooHashMap);
                    Synonym potentialCombination = createPotentialCombination(
                            idInSourceParent, parentSynZooName, zooMisappliedName,
                            synParentGenus,
                            synParentInfragenericName,
                            synParentSpecificEpithet, misappliedName, zooHashMap);

                    taxon.addSynonym(potentialCombination, SynonymType.POTENTIAL_COMBINATION_OF);
                    inferredSynonyms.add(potentialCombination);
                    zooHashMap.put(potentialCombination.getName().getUuid(), potentialCombination.getName());
                    nameCacheList.add(potentialCombination.getName().getNameCache());
                }
            }
        }

        if (!nameCacheList.isEmpty()){
            List<String> synNotInCDM = taxonDao.taxaByNameNotInDB(nameCacheList);
            if (!synNotInCDM.isEmpty()){
                inferredSynonymsToBeRemoved.clear();
                for (Synonym syn :inferredSynonyms){
                    TaxonName name;
                    try{
                        name = syn.getName();
                    }catch (ClassCastException e){
                        name = getZoologicalName(syn.getName().getUuid(), zooHashMap);
                    }
                    if (!synNotInCDM.contains(name.getNameCache())){
                        inferredSynonymsToBeRemoved.add(syn);
                    }
                }

                // Remove identified Synonyms from inferredSynonyms
                for (Synonym synonym : inferredSynonymsToBeRemoved) {
                    inferredSynonyms.remove(synonym);
                }
            }

        }
        return inferredSynonyms;
    }

    private List<Synonym> handleInferredGenus(Taxon taxon, boolean doWithMisappliedNames, TaxonName parentName,
            Map<UUID, TaxonName> zooHashMap, List<String> nameCacheList, IZoologicalName taxonName, String genusOfTaxon,
            String specificEpithetOfTaxon, String infraSpecificEpithetOfTaxon, List<Synonym> synonymsOfTaxon,
            List<TaxonRelationship> taxonRelListTaxon) {

        List<Synonym> inferredSynonyms = new ArrayList<>();
        List<Synonym> inferredSynonymsToBeRemoved = new ArrayList<>();
        for (Synonym synonymOfTaxon: synonymsOfTaxon){

            Synonym inferredGenusSynonym = createInferredGenus(taxon,
                    zooHashMap, taxonName, specificEpithetOfTaxon,
                    genusOfTaxon, nameCacheList, parentName, synonymOfTaxon);

            inferredSynonyms.add(inferredGenusSynonym);
            zooHashMap.put(inferredGenusSynonym.getName().getUuid(), inferredGenusSynonym.getName());
            nameCacheList.add(inferredGenusSynonym.getName().getNameCache());
        }

        if (doWithMisappliedNames){

            for (TaxonRelationship taxonRelationship: taxonRelListTaxon){
                Taxon misappliedName = taxonRelationship.getFromTaxon();
                Synonym inferredGenusSynonym = createInferredGenus(taxon, zooHashMap, taxonName,
                        infraSpecificEpithetOfTaxon, genusOfTaxon, nameCacheList, parentName, misappliedName);

                inferredSynonyms.add(inferredGenusSynonym);
                zooHashMap.put(inferredGenusSynonym.getName().getUuid(), inferredGenusSynonym.getName());
                nameCacheList.add(inferredGenusSynonym.getName().getNameCache());
            }
        }

        if (!nameCacheList.isEmpty()){
            List<String> synNotInCDM = taxonDao.taxaByNameNotInDB(nameCacheList);
            if (!synNotInCDM.isEmpty()){
                inferredSynonymsToBeRemoved.clear();

                for (Synonym syn :inferredSynonyms){
                    IZoologicalName name = getZoologicalName(syn.getName().getUuid(), zooHashMap);
                    if (!synNotInCDM.contains(name.getNameCache())){
                        inferredSynonymsToBeRemoved.add(syn);
                    }
                }

                //Remove identified Synonyms from inferredSynonyms
                for (Synonym synonym : inferredSynonymsToBeRemoved) {
                    inferredSynonyms.remove(synonym);
                }
            }
        }
        return inferredSynonyms;
    }

    private List<Synonym> handleInferredEpithtet(Taxon taxon, boolean doWithMisappliedNames, TaxonName parentName,
            Map<UUID, TaxonName> zooHashMap, List<String> nameCacheList, IZoologicalName taxonName,
            String specificEpithetOfTaxon, String infraGenericEpithetOfTaxon, String infraSpecificEpithetOfTaxon,
            List<Synonym> synonymsOfParent, List<TaxonRelationship> taxonRelListParent) {

        List<Synonym> inferredSynonyms = new ArrayList<>();
        List<Synonym> inferredSynonymsToBeRemoved = new ArrayList<>();
        for (Synonym synonymOfParent: synonymsOfParent){

            Synonym inferredEpithetSynonym = createInferredEpithets(taxon,
                    zooHashMap, taxonName, specificEpithetOfTaxon,
                    infraGenericEpithetOfTaxon,
                    infraSpecificEpithetOfTaxon,
                    nameCacheList, parentName,
                    synonymOfParent);

            inferredSynonyms.add(inferredEpithetSynonym);
            zooHashMap.put(inferredEpithetSynonym.getName().getUuid(), inferredEpithetSynonym.getName());
            nameCacheList.add(inferredEpithetSynonym.getName().getNameCache());
        }

        if (doWithMisappliedNames){

            for (TaxonRelationship taxonRelationship: taxonRelListParent){
                 Taxon misappliedName = taxonRelationship.getFromTaxon();

                 Synonym inferredEpithetSynonym = createInferredEpithets(taxon,
                         zooHashMap, taxonName, specificEpithetOfTaxon,
                         infraGenericEpithetOfTaxon,
                         infraSpecificEpithetOfTaxon,
                         nameCacheList, parentName,
                         misappliedName);

                inferredSynonyms.add(inferredEpithetSynonym);
                zooHashMap.put(inferredEpithetSynonym.getName().getUuid(), inferredEpithetSynonym.getName());
                nameCacheList.add(inferredEpithetSynonym.getName().getNameCache());
            }
        }

        if (!nameCacheList.isEmpty()){
            List<String> synNotInCDM = taxonDao.taxaByNameNotInDB(nameCacheList);
            if (!synNotInCDM.isEmpty()){
                inferredSynonymsToBeRemoved.clear();

                for (Synonym syn: inferredSynonyms){
                    TaxonName name = getZoologicalName(syn.getName().getUuid(), zooHashMap);
                    if (!synNotInCDM.contains(name.getNameCache())){
                        inferredSynonymsToBeRemoved.add(syn);
                    }
                }

                // Remove identified Synonyms from inferredSynonyms
                for (Synonym synonym : inferredSynonymsToBeRemoved) {
                    inferredSynonyms.remove(synonym);
                }
            }
        }
        return inferredSynonyms;
    }

    private Synonym createInferredEpithets(Taxon taxon,
            Map<UUID, TaxonName> zooHashMap, IZoologicalName taxonName,
            String specificEpithetOfTaxon, String infragenericEpithetOfTaxon,
            String infraSpecificEpithetOfTaxon, List<String> nameCacheList,
            TaxonName parentName, TaxonBase<?> synonymOfParent) {

        // Determine the idInSource
        String idInSourceSyn = getIdInSource(synonymOfParent);
        String idInSourceTaxon =  getIdInSource(taxon);
        // Determine the sourceReference
        Reference sourceReference = synonymOfParent.getSec();

        if (sourceReference == null){
             logger.warn("The synonym has no sec reference because it is a misapplied name! Take the sec reference of taxon" + taxon.getSec());
             sourceReference = taxon.getSec();
        }

        //name parts
        TaxonName synOfParentName = getZoologicalName(synonymOfParent.getName().getUuid(), zooHashMap);
        String synGenusName = synOfParentName.getGenusOrUninomial();
        String synInfraGenericEpithet = synOfParentName.getInfraGenericEpithet();
        String synSpecificEpithet = null;

        if (synOfParentName.isInfraSpecific()){ //TODO shouldn't it be taxonName.isInfraSpecific()?
            synSpecificEpithet = synOfParentName.getSpecificEpithet();
        }

        /*
        if (synGenusName != null && !synonymsGenus.containsKey(synGenusName)){
            synonymsGenus.put(synGenusName, idInSource);
        }
        */

        //create inferred synonym name
        TaxonName inferredSynName = TaxonNameFactory.NewZoologicalInstance(taxon.getName().getRank());

        // DEBUG TODO: for subgenus or subspecies the infrageneric or infraspecific epithet should be used!!!
        if (specificEpithetOfTaxon == null && infragenericEpithetOfTaxon == null && infraSpecificEpithetOfTaxon == null) {
            logger.error("This specific epithet synonym is NULL" + taxon.getTitleCache());
        }
        inferredSynName.setGenusOrUninomial(synGenusName);

        if (parentName.isInfraGeneric()){
            inferredSynName.setInfraGenericEpithet(synInfraGenericEpithet);
        }
        if (taxonName.isSpecies()){
            inferredSynName.setSpecificEpithet(specificEpithetOfTaxon);
        }else if (taxonName.isInfraSpecific()){
            inferredSynName.setSpecificEpithet(synSpecificEpithet);
            inferredSynName.setInfraSpecificEpithet(infraSpecificEpithetOfTaxon);
        }

        //created inferred synonym
        Synonym inferredEpithetSynonym = Synonym.NewInstance(inferredSynName, null);

        //... set the sourceReference
        inferredEpithetSynonym.setSec(sourceReference);

        /* ... Add the original source
        if (idInSource != null) {
            IdentifiableSource originalSource = IdentifiableSource.NewInstance(idInSource, "InferredEpithetOf", syn.getSec(), null);

            // Add the citation
            Reference citation = getCitation(syn);
            if (citation != null) {
                originalSource.setCitation(citation);
                inferredEpithet.addSource(originalSource);
            }
        }*/
        String taxonId = idInSourceTaxon+ "; " + idInSourceSyn;


        IdentifiableSource originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                taxonId, INFERRED_EPITHET_NAMESPACE, sourceReference, null);

        inferredEpithetSynonym.addSource(originalSource);

        originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                taxonId, INFERRED_EPITHET_NAMESPACE, sourceReference, null);

        inferredSynName.addSource(originalSource);

        //add synonym to taxon
        taxon.addSynonym(inferredEpithetSynonym, SynonymType.INFERRED_EPITHET_OF);

        return inferredEpithetSynonym;
    }

    private Synonym createInferredGenus(Taxon taxon,
            Map<UUID, TaxonName> zooHashMap, IZoologicalName taxonName,
            String specificEpithetOfTaxon, String genusStrOfTaxon,
            List<String> taxonNames, TaxonName parentName,
            TaxonBase synonymOfTaxon) {

        // Determine the idInSource
        String idInSourceSyn = getIdInSource(synonymOfTaxon);
        String idInSourceTaxon = getIdInSource(taxon);
        // Determine the sourceReference
        Reference sourceReference = synonymOfTaxon.getSec();

        //logger.warn(sourceReference.getTitleCache());

        //name parts
        TaxonName synZooName = getZoologicalName(synonymOfTaxon.getName().getUuid(), zooHashMap);
        String synSpeciesEpithetName = synZooName.getSpecificEpithet();
        /* if (synonymsEpithet != null && !synonymsEpithet.contains(synSpeciesEpithetName)){
            synonymsEpithet.add(synSpeciesEpithetName);
        }
        */

        //create inferred synonym name
        TaxonName inferredSynName = TaxonNameFactory.NewZoologicalInstance(taxon.getName().getRank());
        //TODO distinguish
        //           parent is genus and taxon is species,
        //           parent is subgenus and taxon is species,
        //           parent is species and taxon is subspecies and
        //           parent is genus and taxon is subgenus...

        inferredSynName.setGenusOrUninomial(genusStrOfTaxon);
        if (parentName.isInfraGeneric()){
            inferredSynName.setInfraGenericEpithet(parentName.getInfraGenericEpithet());
        }

        if (taxonName.isSpecies()){
            inferredSynName.setSpecificEpithet(synSpeciesEpithetName);
        }else if (taxonName.isInfraSpecific()){
            inferredSynName.setSpecificEpithet(specificEpithetOfTaxon);
            inferredSynName.setInfraSpecificEpithet(synZooName.getInfraSpecificEpithet());
        }

        //create inferred synonym
        Synonym inferredGenus = Synonym.NewInstance(inferredSynName, null);

        // Set the sourceReference
        inferredGenus.setSec(sourceReference);

        // Add the original source
        if (idInSourceSyn != null && idInSourceTaxon != null) {
            IdentifiableSource originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                    idInSourceSyn + "; " + idInSourceTaxon, INFERRED_GENUS_NAMESPACE, sourceReference, null);
            inferredGenus.addSource(originalSource);

            originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                    idInSourceSyn + "; " + idInSourceTaxon, INFERRED_GENUS_NAMESPACE, sourceReference, null);
            inferredSynName.addSource(originalSource);
            originalSource = null;

        }else{
            logger.error("There is an idInSource missing: " + idInSourceSyn + " of Synonym or " + idInSourceTaxon + " of Taxon");
            IdentifiableSource originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                    idInSourceSyn + "; " + idInSourceTaxon, INFERRED_GENUS_NAMESPACE, sourceReference, null);
            inferredGenus.addSource(originalSource);

            originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                    idInSourceSyn + "; " + idInSourceTaxon, INFERRED_GENUS_NAMESPACE, sourceReference, null);
            inferredSynName.addSource(originalSource);
            originalSource = null;
        }

        taxon.addSynonym(inferredGenus, SynonymType.INFERRED_GENUS_OF);

        return inferredGenus;
    }

    private Synonym createPotentialCombination(String idInSourceParent,
            IZoologicalName parentSynZooName, IZoologicalName zooSynName,
            String synParentGenus, String synParentInfragenericName,
            String synParentSpecificEpithet,
            TaxonBase<?> synonymOfTaxon, Map<UUID,TaxonName> zooHashMap) {

        // Set sourceReference
        Reference sourceReference = synonymOfTaxon.getSec();
        if (sourceReference == null){
            logger.warn("The synonym has no sec reference because it is a misapplied name! Take the sec reference of taxon");
            //TODO:Remove
            if (!parentSynZooName.getTaxa().isEmpty()){
                TaxonBase<?> taxon = parentSynZooName.getTaxa().iterator().next();

                sourceReference = taxon.getSec();
            }
        }

        //name parts
        String synTaxonSpecificEpithet = zooSynName.getSpecificEpithet();

        String synTaxonInfraSpecificEpithet = null;
        if (parentSynZooName.isSpecies()){
            synTaxonInfraSpecificEpithet = zooSynName.getInfraSpecificEpithet();
        }

        /*if (epithetName != null && !synonymsEpithet.contains(epithetName)){
            synonymsEpithet.add(epithetName);
        }*/

        //create pot. comb. synonym name
        IZoologicalName inferredSynName = TaxonNameFactory.NewZoologicalInstance(synonymOfTaxon.getName().getRank());

        inferredSynName.setGenusOrUninomial(synParentGenus);
        if (zooSynName.isSpecies()){
              inferredSynName.setSpecificEpithet(synTaxonSpecificEpithet);
              if (parentSynZooName.isInfraGeneric()){
                  inferredSynName.setInfraGenericEpithet(synParentInfragenericName);
              }
        } else if (zooSynName.isInfraSpecific()){
            inferredSynName.setSpecificEpithet(synParentSpecificEpithet);
            inferredSynName.setInfraSpecificEpithet(synTaxonInfraSpecificEpithet);
        }

        if (parentSynZooName.isInfraGeneric()){
            inferredSynName.setInfraGenericEpithet(synParentInfragenericName);
        }

        //create pot. comb. synonym
        Synonym potentialCombinationSynonym = Synonym.NewInstance(inferredSynName, null);

        // Set the sourceReference
        potentialCombinationSynonym.setSec(sourceReference);

        // Determine the idInSource
        String idInSourceSyn= getIdInSource(synonymOfTaxon);

        if (idInSourceParent != null && idInSourceSyn != null) {
            IdentifiableSource originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation, idInSourceSyn + "; " + idInSourceParent, POTENTIAL_COMBINATION_NAMESPACE, sourceReference, null);
            inferredSynName.addSource(originalSource);
            originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation, idInSourceSyn + "; " + idInSourceParent, POTENTIAL_COMBINATION_NAMESPACE, sourceReference, null);
            potentialCombinationSynonym.addSource(originalSource);
        }

        //return
        return potentialCombinationSynonym;
    }

    /**
     * Returns an existing zoological TaxonName or extends an internal hashmap if it does not exist.
     * Very likely only useful for createInferredSynonyms().
     */
    private TaxonName getZoologicalName(UUID uuid, Map <UUID, TaxonName> zooHashMap) {
        TaxonName taxonName = nameDao.findZoologicalNameByUUID(uuid);
        if (taxonName == null) {
            taxonName = zooHashMap.get(uuid);
        }
        return taxonName;
    }


    /**
     * Returns the idInSource for a given {@link TaxonBase}.
     */
    private String getIdInSource(TaxonBase<?> taxonBase) {
        String idInSource = null;
        Set<IdentifiableSource> sources = taxonBase.getSources();
        if (sources.size() == 1) {
            IdentifiableSource source = sources.iterator().next();
            if (source != null) {
                idInSource  = source.getIdInSource();
            }
        } else if (sources.size() > 1) {
            int count = 1;
            idInSource = "";
            for (IdentifiableSource source : sources) {
                idInSource += source.getIdInSource();
                if (count < sources.size()) {
                    idInSource += "; ";
                }
                count++;
            }
        } else if (sources.size() == 0){
            logger.warn("No idInSource for TaxonBase " + taxonBase.getUuid() + " - " + taxonBase.getTitleCache());
        }

        return idInSource;
    }



}
