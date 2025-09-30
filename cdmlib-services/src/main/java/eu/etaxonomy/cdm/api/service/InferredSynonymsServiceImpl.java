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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.name.IZoologicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.NamedSource;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;

/**
 * @author muellera
 * @since 11.09.2025
 */
@Service
@Transactional(readOnly = true)
public class InferredSynonymsServiceImpl
        implements IInferredSynonymsService {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private ITaxonNameDao nameDao;

    @Autowired
    private ITaxonDao taxonDao;

    @Autowired
    private IClassificationDao classificationDao;

    @Override
    public List<Synonym> createAllInferredSynonyms(UUID taxonUuid, UUID classificationUuid,
            boolean doWithMisappliedNames, boolean includeUnpublished, Set<String> excludedNameCaches,
            boolean doPersist){

        List <Synonym> inferredSynonyms = new ArrayList<>();
        if (doPersist) {
            inferredSynonyms.addAll(createInferredSynonyms(taxonUuid, classificationUuid,
                    SynonymType.INFERRED_EPITHET_OF, doWithMisappliedNames, includeUnpublished, excludedNameCaches));
            inferredSynonyms.addAll(createInferredSynonyms(taxonUuid, classificationUuid,
                    SynonymType.INFERRED_GENUS_OF, doWithMisappliedNames, includeUnpublished, excludedNameCaches));
            inferredSynonyms.addAll(createInferredSynonyms(taxonUuid, classificationUuid,
                    SynonymType.POTENTIAL_COMBINATION_OF, doWithMisappliedNames, includeUnpublished, excludedNameCaches));
        }else {
            inferredSynonyms.addAll(computeInferredSynonyms(taxonUuid, classificationUuid,
                    SynonymType.INFERRED_EPITHET_OF, doWithMisappliedNames, includeUnpublished, excludedNameCaches));
            inferredSynonyms.addAll(computeInferredSynonyms(taxonUuid, classificationUuid,
                    SynonymType.INFERRED_GENUS_OF, doWithMisappliedNames, includeUnpublished, excludedNameCaches));
            inferredSynonyms.addAll(computeInferredSynonyms(taxonUuid, classificationUuid,
                    SynonymType.POTENTIAL_COMBINATION_OF, doWithMisappliedNames, includeUnpublished, excludedNameCaches));
        }

        return inferredSynonyms;
    }

    @Override
    @Transactional(readOnly = false)
    public List<Synonym> createInferredSynonyms(UUID taxonUuid, UUID classificationUuid,
            SynonymType inferredSynonymType, boolean doWithMisappliedNames,
            boolean includeUnpublished, Set<String> excludedNameCaches){

        List<Synonym> result = computeInferredSynonyms(taxonUuid, classificationUuid,
                inferredSynonymType,doWithMisappliedNames, includeUnpublished, excludedNameCaches);
        taxonDao.saveAll(result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Synonym> computeInferredSynonyms(UUID taxonUuid, UUID classificationUuid,
            SynonymType inferredSynonymType, boolean doWithMisappliedNames,
            boolean includeUnpublished, Set<String> excludedNameCaches){

        Taxon taxon = (Taxon)taxonDao.findByUuid(taxonUuid);
        Classification classification = classificationDao.findByUuid(classificationUuid);
        if (excludedNameCaches == null) {
            excludedNameCaches = new HashSet<>();
        }

        TaxonNode node = taxon.getTaxonNode(classification);

        //do not handle root taxa, TODO this could also be a wrong classificationUuid or any other error
        if (node == null || node.isTopmostNode()){
            return new ArrayList<>();
        }

        //handle only species and species having genus, infragenus or species as parent
        Taxon parentTaxon = node.getParent().getTaxon();
        TaxonName parentName = CdmBase.deproxy(parentTaxon.getName());
        IZoologicalName taxonName = taxon.getName();

        if (! ( parentName.isGenus() && (taxonName.isSpecies() || taxonName.isInfraGeneric())  //TODO also allow infraspecies?
                || parentName.isInfraGeneric() && taxonName.isSpecies() //TODO also allow infraspecies?
                || parentName.isSpecies() && taxonName.isInfraSpecific()
                )){
            return new ArrayList<>();
        }

        if (! (parentName.getGenusOrUninomial() != null && parentName.getGenusOrUninomial().equals(taxonName.getGenusOrUninomial()))) {
            logger.warn("Parent and child do not have same genus. Child: " + taxonName.getTitleCache());
            return new ArrayList<>();
        }

        //add existing synonym name caches to the excludedNameCaches
        for (TaxonName parentSynName: parentTaxon.getSynonymNames()) {
            excludedNameCaches.add(parentSynName.getNameCache());
        }
        for (TaxonName taxonSynName: taxon.getSynonymNames()) {
            excludedNameCaches.add(taxonSynName.getNameCache());
        }

        //get wrapped synonyms but not existing inferred synonyms
        List<SynonymWrapper> synonymsOfParent = filteredSynonyms(parentTaxon);
        List<SynonymWrapper> synonymsOfTaxon = filteredSynonyms(taxon);

        //add misapplied names
        if (doWithMisappliedNames){
            //add to nameCache
            for(TaxonRelationship rel: parentTaxon.getMisappliedNameRelations()) {
                synonymsOfParent.add(new SynonymWrapper(rel));
                excludedNameCaches.add(rel.getFromTaxon().getName().getNameCache());
            }
            for(TaxonRelationship rel: taxon.getMisappliedNameRelations()) {
                synonymsOfTaxon.add(new SynonymWrapper(rel));
                excludedNameCaches.add(rel.getFromTaxon().getName().getNameCache());
            }
        }

        //inferred epithet
        if (inferredSynonymType.equals(SynonymType.INFERRED_EPITHET_OF)){

            List<Synonym> result = handleInferredEpithet(taxon, taxonName,
                    synonymsOfParent, excludedNameCaches
                    );
            return result;

        //inferred genus
        } else if (inferredSynonymType.equals(SynonymType.INFERRED_GENUS_OF)){

            return handleInferredGenus(taxon, taxonName, parentName,
                    synonymsOfTaxon, excludedNameCaches
                    );

        //potential combination
        }else if (inferredSynonymType.equals(SynonymType.POTENTIAL_COMBINATION_OF)){

            return handlePotentialCombination(taxon, synonymsOfTaxon, synonymsOfParent,
                    excludedNameCaches
                    );
        }else {
            //TODO handle better
            logger.error("The synonym type is not defined.");
            return new ArrayList<>();
        }
    }

    private class SynonymWrapper{
        Synonym synonym;
        TaxonRelationship rel;

        public SynonymWrapper(Synonym synonym) {this.synonym = synonym;}
        public SynonymWrapper(TaxonRelationship rel) {this.rel = rel;}

        TaxonName getName(){
            return synonym != null? synonym.getName() : rel.getFromTaxon().getName();
        }

        Reference getSec() {
            if (synonym != null){
                return synonym.getSec();
            }else {
                NamedSource source = rel.getSource();
                if (source != null) {
                    return source.getCitation();
                }else {
                    //TODO
                    return rel.getFromTaxon().getSec();
                }

            }
        }

        public String getIdInSource() {
            if (synonym != null) {
                return InferredSynonymsServiceImpl.this.getIdInSource(synonym);
            } else {
//                NamedSource source = rel.getSource();
//                if (source != null) {
//                    return source.getIdInSource();
//                }else {
                    return InferredSynonymsServiceImpl.this.getIdInSource(rel.getFromTaxon());
//                }
            }
        }
    }

    /**
     * Returns all synonyms not being inferred synonyms.
     */
    private List<SynonymWrapper> filteredSynonyms(Taxon taxon) {
        return taxon.getSynonyms()
            .stream()
            .filter(s->!s.getType().isInferredSynonym())
            .map(s->new SynonymWrapper(s))
            .collect(Collectors.toList());
    }

    private List<Synonym> handlePotentialCombination(Taxon taxon,
            List<SynonymWrapper> synonymsOfTaxon, List<SynonymWrapper> synonymsOfParent,
//            Set<TaxonRelationship> taxonRelListTaxon, Set<TaxonRelationship> taxonRelListParent,
            Set<String> excludedNameCaches
            ) {

        List<Synonym> inferredSynonyms = new ArrayList<>();

        List<SynonymWrapper> synonymsAndMisappliedsOfParent = new ArrayList<>();
        List<SynonymWrapper> childSynonyms = new ArrayList<>();
        synonymsAndMisappliedsOfParent.addAll(synonymsOfParent);
        childSynonyms.addAll(synonymsOfTaxon);

        //for all synonyms of the parent...
        for (SynonymWrapper synonymOfParent: synonymsAndMisappliedsOfParent){

            //for all synonyms of the taxon
            for (SynonymWrapper synonymOfTaxon: childSynonyms){

                Synonym potentialCombination = createPotentialCombination(
                        synonymOfParent, synonymOfTaxon, excludedNameCaches);

                if(potentialCombination != null) {
                    potentialCombination.getTitleCache();
                    inferredSynonyms.add(potentialCombination);
                    excludedNameCaches.add(potentialCombination.getName().getNameCache());
                    taxon.addSynonym(potentialCombination, SynonymType.POTENTIAL_COMBINATION_OF);
                }
            }
        }

        return inferredSynonyms;
    }

    private List<Synonym> handleInferredGenus(Taxon taxon, IZoologicalName taxonName, TaxonName parentName,
            List<SynonymWrapper> synonymsOfTaxon, Set<String> excludedNameCaches
            ) {

        List<Synonym> inferredSynonyms = new ArrayList<>();
        for (SynonymWrapper synonymOfTaxon: synonymsOfTaxon){

            Synonym inferredGenusSynonym = createInferredGenus(taxon,
                    taxonName, excludedNameCaches, parentName, synonymOfTaxon);

            if (inferredGenusSynonym != null) {
                inferredGenusSynonym.getTitleCache();
                inferredSynonyms.add(inferredGenusSynonym);
                excludedNameCaches.add(inferredGenusSynonym.getName().getNameCache());
            }
        }

        return inferredSynonyms;
    }

    private List<Synonym> handleInferredEpithet(Taxon taxon, IZoologicalName taxonName,
            List<SynonymWrapper> synonymsOfParent,
            Set<String> excludedNameCaches
            ) {

        List<Synonym> inferredSynonyms = new ArrayList<>();
        for (SynonymWrapper synonymOfParent: synonymsOfParent){

            Synonym inferredEpithetSynonym = createInferredEpithets(taxon,
                    taxonName,
                    synonymOfParent, excludedNameCaches
                    );

            if(inferredEpithetSynonym != null) {
                inferredEpithetSynonym.getTitleCache();
                inferredSynonyms.add(inferredEpithetSynonym);
                excludedNameCaches.add(inferredEpithetSynonym.getName().getNameCache());
            }
        }

        return inferredSynonyms;
    }

    private Synonym createInferredEpithets(Taxon taxon,
            IZoologicalName taxonName,
            SynonymWrapper synonymOfParent, Set<String> excludedNameCaches
            ) {

        // Determine the idInSource
        String idInSourceSyn = synonymOfParent.getIdInSource();
        String idInSourceTaxon =  getIdInSource(taxon);
        // Determine the sourceReference
        Reference sourceReference = synonymOfParent.getSec();

        if (sourceReference == null){
             logger.warn("The synonym has no sec reference because it is a misapplied name! Take the sec reference of taxon" + taxon.getSec());
             sourceReference = taxon.getSec();
        }

        //name parts
        TaxonName synOfParentName = synonymOfParent.getName();
        String synGenusName = synOfParentName.getGenusOrUninomial();
        String synInfraGenericEpithet = synOfParentName.getInfraGenericEpithet();
        String synSpecificEpithet = synOfParentName.getSpecificEpithet();

        //create inferred synonym name
        TaxonName inferredSynName = TaxonNameFactory.NewZoologicalInstance(taxon.getName().getRank());

        inferredSynName.setGenusOrUninomial(synGenusName);
        if (taxonName.isInfraGeneric()) {
            inferredSynName.setInfraGenericEpithet(taxonName.getInfraGenericEpithet());
        } else {
            inferredSynName.setInfraGenericEpithet(synInfraGenericEpithet);
        }
        if (taxonName.isSpecies()){
            inferredSynName.setSpecificEpithet(taxonName.getSpecificEpithet());
        }else if (taxonName.isInfraSpecific()){
            inferredSynName.setSpecificEpithet(synSpecificEpithet);
            inferredSynName.setInfraSpecificEpithet(taxonName.getInfraSpecificEpithet());
        }

        String nameCache = inferredSynName.getNameCache();
        if(excludedNameCaches.contains(nameCache)){
            return null;
        }

        //created inferred synonym
        Synonym inferredEpithetSynonym = Synonym.NewInstance(inferredSynName, sourceReference);

        /* ... Add the original source*/
        if (idInSourceTaxon != null || idInSourceSyn != null) {
            String taxonId = idInSourceTaxon + "; " + idInSourceSyn;
            IdentifiableSource originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                    taxonId, INFERRED_EPITHET_NAMESPACE, sourceReference, null);
            inferredEpithetSynonym.addSource(originalSource);

            originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                    taxonId, INFERRED_EPITHET_NAMESPACE, sourceReference, null);
            inferredSynName.addSource(originalSource);
        }

        //add synonym to taxon
        taxon.addSynonym(inferredEpithetSynonym, SynonymType.INFERRED_EPITHET_OF);

        return inferredEpithetSynonym;
    }

    private Synonym createInferredGenus(Taxon taxon,
            IZoologicalName taxonName,
            Set<String> excludedNameCaches, TaxonName parentName,
            SynonymWrapper synonymOfTaxon) {

        // Determine the idInSource
        String idInSourceSyn = synonymOfTaxon.getIdInSource();
        String idInSourceTaxon = getIdInSource(taxon);
        // Determine the sourceReference
        Reference sourceReference = synonymOfTaxon.getSec();

        //name parts
        TaxonName synZooName = synonymOfTaxon.getName();

        //create inferred synonym name
        TaxonName inferredSynName = TaxonNameFactory.NewZoologicalInstance(taxonName.getRank());
        //TODO distinguish
        //           parent is genus and taxon is species,
        //           parent is genus and taxon is subgenus,
        //           parent is subgenus and taxon is species,
        //           parent is species and taxon is subspecies

        inferredSynName.setGenusOrUninomial(taxonName.getGenusOrUninomial());
        //TODO is this really correct in all cases?
        String infraGenEpi = taxonName.isInfraGeneric()? synZooName.getInfraGenericEpithet():parentName.getInfraGenericEpithet();
        inferredSynName.setInfraGenericEpithet(infraGenEpi);
        if (taxonName.isSpecies()){
            inferredSynName.setSpecificEpithet(synZooName.getSpecificEpithet());
        }else if (taxonName.isInfraSpecific()){
            inferredSynName.setSpecificEpithet(taxonName.getSpecificEpithet());
            inferredSynName.setInfraSpecificEpithet(synZooName.getInfraSpecificEpithet());
        }
        String nameCache = inferredSynName.getNameCache();
        if (excludedNameCaches.contains(nameCache)) {
            return null;
        }

        //create inferred synonym
        Synonym inferredGenus = Synonym.NewInstance(inferredSynName, null);

        // Set the sourceReference
        inferredGenus.setSec(sourceReference);

        // Add the original source
        if (idInSourceSyn == null || idInSourceTaxon == null) {
            logger.debug("There is an idInSource missing: " + idInSourceSyn + " of Synonym or " + idInSourceTaxon + " of Taxon");
        }
        IdentifiableSource originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                idInSourceSyn + "; " + idInSourceTaxon, INFERRED_GENUS_NAMESPACE, sourceReference, null);
        inferredGenus.addSource(originalSource);

        originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation,
                idInSourceSyn + "; " + idInSourceTaxon, INFERRED_GENUS_NAMESPACE, sourceReference, null);
        inferredSynName.addSource(originalSource);

        taxon.addSynonym(inferredGenus, SynonymType.INFERRED_GENUS_OF);

        return inferredGenus;
    }

    private Synonym createPotentialCombination(SynonymWrapper parentSyn,
            SynonymWrapper synonymOfTaxon, Set<String> excludedNameCaches) {

        TaxonName parentSynName = parentSyn.getName();
        TaxonName synName = synonymOfTaxon.getName();

        //create pot. comb. synonym name
        IZoologicalName inferredSynName = TaxonNameFactory.NewZoologicalInstance(synonymOfTaxon.getName().getRank());

        inferredSynName.setGenusOrUninomial(parentSynName.getGenusOrUninomial());
        if (synName.isInfraGeneric()) {
            inferredSynName.setInfraGenericEpithet(synName.getInfraGenericEpithet());
        }else {
            inferredSynName.setInfraGenericEpithet(parentSynName.getInfraGenericEpithet());
        }
        if (synName.isSpecies()){
              inferredSynName.setSpecificEpithet(synName.getSpecificEpithet());
        } else if (synName.isInfraSpecific()){
            inferredSynName.setSpecificEpithet(parentSynName.getSpecificEpithet());
            inferredSynName.setInfraSpecificEpithet(synName.getInfraSpecificEpithet());
        }


        if (excludedNameCaches.contains(inferredSynName.getNameCache())) {
            return null;
        }

        //create pot. comb. synonym
        Synonym potentialCombinationSynonym = Synonym.NewInstance(inferredSynName, null);

        // Set the sourceReference
        Reference sourceReference = synonymOfTaxon.getSec();
        potentialCombinationSynonym.setSec(sourceReference);

        // Determine the idInSource
        String idInSourceParentSyn = parentSyn.getIdInSource();
        String idInSourceSyn= synonymOfTaxon.getIdInSource();

        if (idInSourceSyn == null || idInSourceParentSyn == null) {
            logger.debug("There is an idInSource missing: " + idInSourceSyn + " of taxon synonym or " + idInSourceParentSyn + " of parent synonyms.");
        }
        IdentifiableSource originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation, idInSourceSyn + "; " + idInSourceParentSyn, POTENTIAL_COMBINATION_NAMESPACE, sourceReference, null);
        inferredSynName.addSource(originalSource);
        originalSource = IdentifiableSource.NewInstance(OriginalSourceType.Transformation, idInSourceSyn + "; " + idInSourceParentSyn, POTENTIAL_COMBINATION_NAMESPACE, sourceReference, null);
        potentialCombinationSynonym.addSource(originalSource);

        //return
        return potentialCombinationSynonym;
    }

    @Override
    public Set<String> getDistinctNameCaches() {
        List<String> existingNameCaches = nameDao.distinctNameCaches(null, Rank.SUBGENUS(), null);
        return new HashSet<>(existingNameCaches);
    }

    /**
     * Returns the idInSource for a given {@link TaxonBase}.
     */
    private String getIdInSource(TaxonBase<?> taxonBase) {
        String idInSource = "";
        Set<IdentifiableSource> sources = taxonBase.getSources();

        if (sources.size() > 0) {
            int count = 1;
            idInSource = "";
            for (IdentifiableSource source : sources) {
                if (!isFauEuSource(source)) {
                    continue;
                }
                idInSource += source.getIdInSource();
                if (count < sources.size()) {
                    idInSource += "; ";
                }
                count++;
            }
        } else if (sources.size() == 0){
            String titleCache = taxonBase.isInstanceOf(IdentifiableEntity.class)? (CdmBase.deproxy(taxonBase, IdentifiableEntity.class).getTitleCache()) : "";
            logger.debug("No idInSource for TaxonBase " + taxonBase.getUuid() + " - " + titleCache);
        }

        return CdmUtils.Ne(idInSource);
    }

    public boolean isFauEuSource(IdentifiableSource source) {
//        UUID FauEu2025uuid = UUID.fromString("f27a5e67-d065-4b79-8d41-eabd3ae0edd0");
        //see PesiTransformer.uuidSourceRefFaunaEuropaea_fromSql
        UUID fauEuDbUuid = UUID.fromString("6786d863-75d4-4796-b916-c1c3dff4cb70");
        if (source.getType() != OriginalSourceType.Import
                || source.getCitation() == null
                || !source.getCitation().getUuid().equals(fauEuDbUuid)) {
            return false;
        }else {
            return true;
        }
   }
}