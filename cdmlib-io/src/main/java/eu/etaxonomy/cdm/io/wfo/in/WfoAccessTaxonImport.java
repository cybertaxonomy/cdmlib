/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.wfo.in;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.dto.IdentifiedEntityDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.csv.in.CsvImportBase;
import eu.etaxonomy.cdm.io.wfo.in.WfoAccessClassificationImport.TaxStatus;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @since 15.11.2017
 */
@Component
public class WfoAccessTaxonImport<STATE extends WfoAccessImportState>
        extends CsvImportBase<WfoAccessImportConfigurator, STATE, TaxonName>{

    private static final long serialVersionUID = 8721691506017004260L;

    private static final String TAXON_ID = "taxonID";
    private static final String SCIENTIFIC_NAME_ID = "scientificNameID";
    private static final String SCIENTIFIC_NAME = "scientificName";
    private static final String TAXON_RANK = "taxonRank";
    private static final String PARENT_NAME_USAGE_ID = "parentNameUsageID";
    private static final String SCIENTIFIC_NAME_AUTHORSHIP = "scientificNameAuthorship";
    private static final String FAMILY = "family";
    private static final String SUBFAMILY = "subfamily";
    private static final String TRIBE = "tribe";
    private static final String SUBTRIBE = "subtribe";
    private static final String GENUS = "genus";
    private static final String SUBGENUS = "subgenus";
    private static final String SPECIFIC_EPITHET = "specificEpithet";
    private static final String INFRA_SPECIFIC_EPITHET = "infraspecificEpithet";
    private static final String VERBATIM_TAXON_RANK = "verbatimTaxonRank";
    private static final String NOM_STATUS = "nomenclaturalStatus";  //derzeit immer leer
    private static final String NAME_PUBLISHED_IN = "namePublishedIn";
    private static final String COLLATION = "COLLATION";
    private static final String PAGES = "PAGES";
    private static final String DATES = "DATES";
    private static final String NAME_PUBLISHED_IN_ID = "namePublishedInID";
    private static final String TAXONOMIC_STATUS = "taxonomicStatus";
    private static final String ACCEPTED_NAME_USAGE_ID = "acceptedNameUsageID";
    private static final String ORIGINAL_NAME_USAGE_ID = "originalNameUsageID";
    private static final String TAXON_REMARKS = "taxonRemarks";
    private static final String CREATED = "created";
    private static final String MODIFIED = "modified";
    private static final String REFERENCES = "references";
    private static final String PUB_TYPE = "PubType";

    private static final String TPL_DOMAIN = "http://www.theplantlist.org/tpl1.1/record/";
    private static final String TPL_GENUS_DOMAIN = "http://www.theplantlist.org/1.1/browse/A/";


    private Map<String,TaxonName> nameMap;
    private NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();

    @Override
    protected void handleSingleLine(STATE state) {
        TaxonName name = makeName(state);
        if (name == null){
            return;
        }

        Map<String, String> record = state.getCurrentRecord();

        handleIgnoredFields(record);
        testAlwaysEmptyFields(state);


        state.getDeduplicationHelper().replaceAuthorNamesAndNomRef(state, name);

        if (!name.isPersited()){
            state.getResult().addNewRecords(TaxonName.class.getSimpleName(), 1);
        }
        getNameService().saveOrUpdate(name);
        saveHybridNames(state, name);
        makeTaxon(state, name);
    }


    /**
     * @param state
     * @param name
     */
    private void saveHybridNames(STATE state, TaxonName name) {
        for (HybridRelationship hybridRel : name.getHybridChildRelations()){
            TaxonName parent = hybridRel.getParentName();
            if (!parent.isPersited()){
                state.getResult().addNewRecords(TaxonName.class.getSimpleName(), 1);
            }
            getNameService().saveOrUpdate(parent);

        }

    }


    /**
     * @param record
     */
    protected void handleIgnoredFields(Map<String, String> record) {
        if (record.get(CREATED) != null){
            //ignore
        }
        if (record.get(MODIFIED) != null){
            //ignore
        }
        if (record.get(TAXON_REMARKS) != null){
            //ignore
        }
        if (record.get(NAME_PUBLISHED_IN_ID) != null){
            //ignore
        }
    }


    /**
     * @param state
     */
    private void testAlwaysEmptyFields(STATE state) {
        Map<String, String> record = state.getCurrentRecord();
        int location = state.getRow();
        if (record.get(SUBFAMILY) != null){
            String message = "Subfamily not yet handled";
            state.getResult().addWarning(message, location);
        }
        if (record.get(TRIBE) != null){
            String message = "Tribe not yet handled";
            state.getResult().addWarning(message, location);
        }
        if (record.get(SUBTRIBE) != null){
            String message = "Subtribe not yet handled";
            state.getResult().addWarning(message, location);
        }
        if (record.get(SUBGENUS) != null){
            String message = "Subfamily not yet handled";
            state.getResult().addWarning(message, location);
        }
    }


//    /**
//     * @param state
//     * @param name
//     */
//    private void makeNomStatus(STATE state, TaxonName name) {
//        String nomStatusStr = state.getCurrentRecord().get(OUTPUT_NOM_STATUS);
//        if (nomStatusStr == null || nomStatusStr.equalsIgnoreCase("No opinion")){
//            return;
//        }else{
//            NomenclaturalStatusType status = null;
//            try {
//                status = NomenclaturalStatusType.getNomenclaturalStatusTypeByLabel(nomStatusStr);
//            } catch (UnknownCdmTypeException e) {
//                try {
//                    status = NomenclaturalStatusType.getNomenclaturalStatusTypeByAbbreviation(nomStatusStr, name);
//                } catch (UnknownCdmTypeException e1) {
//                    //handle later
//                }
//            }
//            if (status == null){
//                String message = "Nomenclatural status '%s' not recognized.";
//                message = String.format(message, nomStatusStr);
//                state.getResult().addWarning(message, state.getRow());
//            }else{
//                name.addStatus(status, null, null);
//            }
//        }
//    }


    /**
     * @param state
     * @param name
     */
    private void makeReference(STATE state, TaxonName name) {

        Map<String, String> record = state.getCurrentRecord();
        String type = record.get(PUB_TYPE);

        String nomRefTitle = record.get(NAME_PUBLISHED_IN);
        String collation = record.get(COLLATION);
        String pages = record.get(PAGES);
        String dates = record.get(DATES);

        if (CdmUtils.isBlank(nomRefTitle, collation, pages, dates)){
            //TODO
//            state.getResult().addInfo("No nomenclatural reference information given");
            return;
        }

        //TODO pages
        //TODO remove collation from and of nomRefTitle if there

        //Create and set title + in-Reference
        Reference reference;
        if (type == null){
            String message = "Nomenclatural reference type not defined.";
            state.getResult().addWarning(message, state.getRow());
            // TODO check against DB
            reference = ReferenceFactory.newGeneric();
            reference.setAbbrevTitle(nomRefTitle);
        }else if (type.equals("A")){
            reference = ReferenceFactory.newArticle();
            Reference journal = ReferenceFactory.newJournal();
            journal.setAbbrevTitle(nomRefTitle);
            reference.setInJournal(journal);
        }else if (type.equals("B")){
            reference = ReferenceFactory.newBook();
            reference.setAbbrevTitle(nomRefTitle);
        }else{
            String message = "Value for %s not recognized. Use generic reference instead";
            message = String.format(message, PUB_TYPE);
            state.getResult().addWarning(message, state.getRow());
            reference = ReferenceFactory.newGeneric();
            reference.setAbbrevTitle(nomRefTitle);
        }

        String detail;
        String volume;
        if (isNotBlank(collation)){
            String[] split = collation.split(":");
            if (split.length == 1){
                detail = split[0].trim();
                volume = null;
                if (type != null && type.equals("A")){
                    String message = "Collation has no volume part for reference of type article: %s" ;
                    message = String.format(message, collation);
                    state.getResult().addWarning(message, state.getRow());
                }
            }else if (split.length == 2){
                volume = split[0].trim();
                detail = split[1].trim();
            }else{
                String message = "Collation has more then 1 ':' and can not be parsed: %s" ;
                message = String.format(message, collation);
                state.getResult().addWarning(message, state.getRow());
                detail = split[1].trim() + split[2].trim();
                volume = split[0].trim();
            }
        }else{
            detail = null;
            volume = null;
        }

        reference.setVolume(volume);
        name.setNomenclaturalMicroReference(detail);

        //date
        if (isNotBlank(dates)){
            VerbatimTimePeriod tp = TimePeriodParser.parseStringVerbatim(dates);
            if (isNotBlank(tp.getFreeText())){
                String message = "Date could not be parsed: %s" ;
                message = String.format(message, dates);
                state.getResult().addWarning(message, state.getRow());
            }
            reference.setDatePublished(tp);
        }

        //add to name
        name.setNomenclaturalReference(reference);
        name.setNomenclaturalMicroReference(detail);
        //author
        if (state.getConfig().isAddAuthorsToReference()){
            TeamOrPersonBase<?> author = name.getCombinationAuthorship();
            if (author != null){
                reference.setAuthorship(author);
            }
        }

        //source
        addSourceReference(state, reference);
    }


    /**
     * @param state
     * @return
     */
    private TaxonName makeName(STATE state) {
        Map<String, String> record = state.getCurrentRecord();

        String taxonId = record.get(TAXON_ID);

        TaxonName name;
        if (taxonId != null){
            name = getNameMap(state).get(taxonId);
//            name = getExistingTaxonName(state, taxonId);
        }else{
            String message = "TaxonId could not be found. Maybe format needs to be changed to UTF8 without BOM";
            state.getResult().addWarning(message, state.getRow());
            throw new RuntimeException(message);
//            name = null;
        }
        if (name != null){
            checkTplAndIpniIds(state, name);
            state.putExistingWfoId(taxonId);
            return null;
        }else{
            String nameStr = record.get(SCIENTIFIC_NAME);
            String authorStr = record.get(SCIENTIFIC_NAME_AUTHORSHIP);
            boolean isAuct = false;
            if ("auct.".equals(authorStr)){
                isAuct = true;
                authorStr = null;
            }else if ("(Rusby) auct.".equals(authorStr)){
                isAuct = true;
                authorStr = "(Rusby)";
            }
            String fullNameStr = CdmUtils.concat(" ", nameStr, authorStr);
            Rank rank = getRank(state);
            name = (TaxonName)parser.parseFullName(fullNameStr, state.getConfig().getNomenclaturalCode(), rank);
            checkNameParts(state, name);
            if (isAuct){
                Person auct = Person.NewInstance();
                auct.setNomenclaturalTitle("auct.");
                name.setCombinationAuthorship(auct);
            }
            getNameMap(state).put(taxonId, name);

            if (name.isProtectedTitleCache() || name.isProtectedNameCache() || name.isProtectedAuthorshipCache()){
                String message = "Name (%s) could not be fully parsed, but is processed";
                message = String.format(message, name.getTitleCache());
                //TODO
//            state.getResult().addWarning(message, state.getRow());
            }
            makeReference(state, name);
//          makeNomStatus(state, name);

            makeWfoId(state, name);
            makePlantListIdentifier(state, name);
            makeIpniId(state, name);
            addSourceReference(state, name);
            return name;
        }
    }

/**
     * @param state
     * @param name
     */
    private void checkTplAndIpniIds(STATE state, TaxonName name) {
        Map<String, String> record = state.getCurrentRecord();

        String ipniId = record.get(SCIENTIFIC_NAME_ID);
        if (isNotBlank(ipniId)){
            Set<String> ipniIds = name.getIdentifierStrings(DefinedTerm.uuidIpniNameIdentifier);
            if (!ipniIds.contains(ipniId)){
                makeIpniId(state, name);
            }
        }

        String plantListId = record.get(REFERENCES);
        if (isNotBlank(plantListId)){
            Set<String> plantListIds = name.getIdentifierStrings(DefinedTerm.uuidPlantListIdentifier);
            plantListId = makeTplIdPart(plantListId);
            if (!plantListIds.contains(plantListId)){
                makePlantListIdentifier(state, name);
            }
        }
    }


/**
     * @param state
     * @param name
     */
    private void makeIpniId(STATE state, TaxonName name) {
        Map<String, String> record = state.getCurrentRecord();

        String ipniId = record.get(SCIENTIFIC_NAME_ID);
        if (isNotBlank(ipniId)){
            DefinedTerm identifierType = DefinedTerm.IDENTIFIER_NAME_IPNI();
            name.addIdentifier(ipniId, identifierType);
        }
    }

    private void makeWfoId(STATE state, TaxonName name) {
        Map<String, String> record = state.getCurrentRecord();

        String wfoId = record.get(TAXON_ID);
        if (isNotBlank(wfoId)){
            DefinedTerm identifierType = DefinedTerm.IDENTIFIER_NAME_WFO();
            name.addIdentifier(wfoId, identifierType);
        }
    }


//    /**
//     * @param state
//     * @param taxonId
//     * @return
//     */
//    private TaxonName getExistingTaxonName(STATE state, String taxonId) {
//        TaxonName name = nameMap.get(taxonId);
//        return name;
//        DefinedTerm wfoType = DefinedTerm.IDENTIFIER_NAME_WFO();
//        boolean includeCdmEntity = true;
//        Pager<IdentifiedEntityDTO<TaxonName>> identifierResult = getNameService()
//                .findByIdentifier(TaxonName.class, taxonId, wfoType, MatchMode.EXACT, includeCdmEntity,
//                null, null, null);
//        if (identifierResult.getCount() == 0){
//            return null;
//        }else if (identifierResult.getCount() == 1){
//            return identifierResult.getRecords().get(0).getCdmEntity().getEntity();
//        }else {
//            //TODO find best matching depending on expected taxon status
//            String message = "More then 1 name with WFO identifier '%s' found. Take first one";
//            state.getResult().addWarning(message, state.getRow());
//            return identifierResult.getRecords().get(0).getCdmEntity().getEntity();
//        }
//    }


    /**
     * @param state
     * @return
     */
    private Map<String, TaxonName> getNameMap(STATE state) {
        if (nameMap == null){
            refreshNameMap(state);
        }
        return nameMap;
    }


    /**
     * @param state
     * @param name
     */
    private void makePlantListIdentifier(STATE state, TaxonName name) {
        Map<String, String> record = state.getCurrentRecord();

        String references = record.get(REFERENCES);
        if (isNotBlank(references)){
            String typeLabel = "The Plant List 1.1 Identifier";
            DefinedTerm identifierType = this.getIdentiferType(state, DefinedTerm.uuidPlantListIdentifier, typeLabel, typeLabel, "TPL1.1", null);
            if (references.startsWith(TPL_DOMAIN)){
                references = makeTplIdPart(references);
                name.addIdentifier(references, identifierType);
            }else if (references.startsWith(TPL_GENUS_DOMAIN)){
                //do nothing no import
            }else{
                String message = "The plant list identifier does not start with standard formats %s or %s : %s";
                message = String.format(message, TPL_DOMAIN, TPL_GENUS_DOMAIN, references);
                state.getResult().addWarning(message, state.getRow());
            }
        }
    }


    /**
     * @param references
     * @return
     */
    protected String makeTplIdPart(String fullUrl) {
        String result = fullUrl
                .replace(TPL_DOMAIN, "")
//                .replace(TPL_GENUS_DOMAIN, "")
                ;

        return result;
    }


    /**
     * @param state
     * @param name
     */
    private void checkNameParts(STATE state, TaxonName name) {
        Map<String, String> record = state.getCurrentRecord();
        String genusStr = record.get(GENUS);
        String speciesStr = record.get(SPECIFIC_EPITHET);
        String infraSpeciesStr = record.get(INFRA_SPECIFIC_EPITHET);
        String familyStr = record.get(FAMILY);
        if (genusStr != null && !genusStr.equals(name.getGenusOrUninomial())){
            String message = "Atomised genus and parsed genus are not equal: " + genusStr + "; " + name.getGenusOrUninomial() + "; " + name.getTitleCache();
            state.getResult().addWarning(message, state.getRow());
        }else if (speciesStr != null && !speciesStr.equals(name.getSpecificEpithet())){
            String message = "Atomised specific epithet and parsed specific epithet  are not equal";
            state.getResult().addWarning(message, state.getRow());
        }else if (infraSpeciesStr != null && !infraSpeciesStr.equals(name.getInfraSpecificEpithet())){
            String message = "Atomised infraspecific epithet and parsed infraspecific epithet  are not equal";
            state.getResult().addWarning(message, state.getRow());
        }else if (Rank.FAMILY().equals(name.getRank()) && (familyStr == null || !familyStr.equals(name.getGenusOrUninomial()))){
            String message = "Atomised family name and parsed family name are not equal";
            state.getResult().addWarning(message, state.getRow());
        }


    }


    /**
     * @param state
     * @return
     */
    private Rank getRank(STATE state) {
        Map<String, String> record = state.getCurrentRecord();

        String rankStr = record.get(TAXON_RANK);
        String verbatimRankStr = record.get(VERBATIM_TAXON_RANK);

        if (isBlank(rankStr) || isBlank(verbatimRankStr) ){
            String message = "Rank or verbatim rank not defined. Rank not evaluated.";
            state.getResult().addWarning(message, state.getRow());
            return null;
        }else if (rankStr.equals("SPECIES")&& verbatimRankStr.equals("sp.")){
            return Rank.SPECIES();
        }else if (rankStr.equals("family")&& verbatimRankStr.equals("family")){
            return Rank.FAMILY();
        }else if (rankStr.equals("genus")&& verbatimRankStr.equals("genus")){
            return Rank.GENUS();
        }else if (rankStr.equals("SUBSPECIES")&& verbatimRankStr.equals("subsp.")){
            return Rank.SUBSPECIES();
        }else if (rankStr.equals("VARIETY")&& verbatimRankStr.equals("var.")){
            return Rank.VARIETY();
        }else if (rankStr.equals("FORM")&& verbatimRankStr.equals("f.")){
            return Rank.FORM();
        }else{
            String message = "Rank or verbatim rank not identified. Rank: " + rankStr +  ", verbatim rank:" + verbatimRankStr;
            state.getResult().addWarning(message, state.getRow());
            return null;
        }
    }


    /**
     * @param state
     * @param name
     */
    private void addSourceReference(STATE state, IdentifiableEntity<?> entity) {
        entity.addImportSource(null, null, getTransactionalSourceReference(state), "line " + state.getLine());
    }


    @Override
    protected void refreshTransactionStatus(STATE state) {
        super.refreshTransactionStatus(state);
        refreshNameMap(state);
    }


    /**
     * @param state
     */
    protected void refreshNameMap(STATE state) {
        nameMap = new HashMap<>();

        DefinedTerm wfoType = DefinedTerm.IDENTIFIER_NAME_WFO();
        Pager<IdentifiedEntityDTO<TaxonName>> identifiedNamePager = getNameService().findByIdentifier(TaxonName.class,
                "*", wfoType, MatchMode.EXACT, true, null, null, null);

        for (IdentifiedEntityDTO<TaxonName> dto : identifiedNamePager.getRecords()){
            TaxonName name = dto.getCdmEntity().getEntity();
            String identifier = dto.getIdentifier().getIdentifier();
            TaxonName oldName = nameMap.put(identifier, name);
            if (oldName != null){
                String message = "There are multiple names with same WFO identifier %s. ID1=%d, ID2=%d";
                message = String.format(message, identifier, name.getId(), oldName.getId());
                state.getResult().addWarning(message, state.getRow());
            }
        }
    }


    /**
     * @param state
     * @param name
     */
    private void makeTaxon(STATE state, TaxonName name) {
        Map<String, String> record = state.getCurrentRecord();
        TaxonBase<?> cdmTaxon = getCdmTaxon(state, name, TAXON_ID);
        TaxStatus status =  TaxStatus.from(record.get(TAXONOMIC_STATUS));
        if (status == TaxStatus.ACC){
            handleAccepted(state, name, cdmTaxon);
        }else if (status == TaxStatus.SYN){
            handleSynonym(state, name, cdmTaxon);
        }else if (status == TaxStatus.DOUBT){
            handleDoubtful(state, name, cdmTaxon);
        }
    }

    /**
     * @param state
     * @param name
     * @param cdmTaxon
     */
    private void handleAccepted(STATE state, TaxonName name, TaxonBase<?> cdmTaxon) {
        Reference sec = this.getTransactionalSourceReference(state);
        if (cdmTaxon == null){
            Taxon newTaxon = Taxon.NewInstance(name, sec);
            addSourceReference(state, newTaxon);
            getTaxonService().save(newTaxon);
        }else{
            String message = "Accepted name already has taxon. This should not happen anymore.";
            state.getResult().addError(message, state.getRow());
        }
    }

    private void handleDoubtful(STATE state, TaxonName name, TaxonBase<?> cdmTaxon) {
        //TODO this is probably not needed anymore
        Map<String, String> record = state.getCurrentRecord();
        String origName = record.get(ORIGINAL_NAME_USAGE_ID);
        if (isNotBlank(origName)){
            state.putOriginalNameOfDoubful(origName);
        }
        //end not needed

        Reference sec = this.getTransactionalSourceReference(state);
        if (cdmTaxon == null){
            Taxon newTaxon = Taxon.NewInstance(name, sec);
            newTaxon.setDoubtful(true);
            addSourceReference(state, newTaxon);
            getTaxonService().save(newTaxon);
        }else {
            String message = "Doubtful name already has taxon. This should not happen anymore.";
            state.getResult().addError(message, state.getRow());
        }
    }


    private TaxonBase<?> getCdmTaxon(STATE state, TaxonName name, String fieldName) {
        Map<String, String> record = state.getCurrentRecord();
        String taxonId = record.get(fieldName);

        @SuppressWarnings("rawtypes")
        Set<TaxonBase> cdmTaxa = name.getTaxonBases();
        if (cdmTaxa.isEmpty()){
            return null;
        }else if (cdmTaxa.size()==1){
            return cdmTaxa.iterator().next();
        }else{
            String message = "Name %s has more then 1 existing taxon. Can't define existing taxon. Create new one.";
            message = String.format(message, taxonId);
            state.getResult().addError(message, state.getRow());
            return null;
        }
    }

    /**
     * @param state
     * @param name
     * @param cdmTaxon
     */
    private void handleSynonym(STATE state, TaxonName name, TaxonBase<?> cdmTaxon) {
        Reference sec = this.getTransactionalSourceReference(state);
        if (cdmTaxon == null){
            Synonym syn = Synonym.NewInstance(name, sec);
            addSourceReference(state, syn);
            getTaxonService().save(syn);
        }else{
            String message = "Synonym name already has taxon. This should not happen anymore.";
            state.getResult().addError(message, state.getRow());
        }
    }


    /**
     * Transactional save method to retrieve the parent node
     * @param state
     * @param sec
     * @return
     */
    protected TaxonNode getParentNode(STATE state) {
        TaxonNode parentNode = state.getParentNode();
        if (parentNode == null){
            if (state.getConfig().getParentNodeUuid() != null){
                parentNode = getTaxonNodeService().find(state.getConfig().getParentNodeUuid());
                if (parentNode == null){
                    //node does not exist => create new classification
                    Classification classification = makeClassification(state);
                    parentNode = classification.getRootNode();
                    parentNode.setUuid(state.getConfig().getParentNodeUuid());
                }
            }else {
                Classification classification = makeClassification(state);
                state.getConfig().setParentNodeUuid(classification.getRootNode().getUuid());
                parentNode = classification.getRootNode();
            }
            state.setParentNode(parentNode);
        }
        return parentNode;
    }


    /**
     * @param state
     * @param sec
     * @return
     */
    protected Classification makeClassification(STATE state) {
        Reference ref = getTransactionalSourceReference(state);
        String classificationStr = state.getConfig().getClassificationName();
        if (isBlank(classificationStr)){
            classificationStr = "Tropicos import " + UUID.randomUUID();
        }
        Classification classification = Classification.NewInstance(classificationStr, ref, Language.UNDETERMINED());
        return classification;
    }




}
