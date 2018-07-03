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
import java.util.UUID;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.dto.IdentifiedEntityDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.csv.in.CsvImportBase;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
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

    private static final String TAXON_ID = "taxonId";
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

    private Map<String,TaxonName> nameMap;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleSingleLine(STATE state) {
        TaxonName name = makeName(state);
        if (name == null){
            return;
        }

        Map<String, String> record = state.getCurrentRecord();

        makeReference(state, name);
//        makeNomStatus(state, name);

        handleIgnoredFields(record);
        testAlwaysEmptyFields(state);

//        state.getDedupHelper().replaceAuthorNamesAndNomRef(state, name);

//        getNameService().saveOrUpdate(name);
        state.getResult().addNewRecords(TaxonName.class.getSimpleName(), 1);

//        makeTaxon(state, name);

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
                detail = null;
                volume = null;
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
     * Checks if the sourceId (WFO ID) already exists in the database.
     * @param state
     * @param name
     * @param idAttr
     * @param allowDuplicate
     * @param identifierType
     * @return <code>true</code> if sourceId already exists.
     */
    private boolean checkAndAddIdentifier(STATE state, TaxonName name, String idAttr,
            boolean allowDuplicate, DefinedTerm identifierType) {
        String identifier = state.getCurrentRecord().get(idAttr);
        if (identifier == null){
            return false;
        }

        if (! allowDuplicate || state.getConfig().isReportDuplicateIdentifier()){
            //TODO precompute existing per session or, at least, implement count
            Pager<IdentifiedEntityDTO<TaxonName>> existing = getNameService().findByIdentifier(TaxonName.class, identifier, identifierType, MatchMode.EXACT, false, null, null, null);
            if (existing.getCount() > 0){
                //TODO make language configurable
                Language language = Language.DEFAULT();
                if (! allowDuplicate){
                    String message = "The name with the given identifier (%s: %s) exists already in the database. Record is not imported.";
                    message = String.format(message, identifierType.getPreferredRepresentation(Language.DEFAULT()).getText(), identifier);
                    state.getResult().addWarning(message, state.getRow());
                    return true;
                }else{
                    String message = "The name with the given identifier (%s: %s) exists already in the database. Record is imported but maybe needs to be reviewed.";
                    message = String.format(message, identifierType.getPreferredRepresentation(language).getText(), identifier);
                    state.getResult().addWarning(message, state.getRow());
                }
            }
        }

        name.addIdentifier(identifier, identifierType);
        return false;
    }

    private NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();

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
            return name;
        }else{
            String nameStr = record.get(SCIENTIFIC_NAME);
            String authorStr = record.get(SCIENTIFIC_NAME_AUTHORSHIP);
            String fullNameStr = CdmUtils.concat(" ", nameStr, authorStr);
            Rank rank = getRank(state);
            name = (TaxonName)parser.parseFullName(fullNameStr, state.getConfig().getNomenclaturalCode(), rank);
            checkNameParts(state, name);
            getNameMap(state).put(taxonId, name);
        }

        if (name.isProtectedTitleCache() || name.isProtectedNameCache() || name.isProtectedAuthorshipCache()){
            String message = "Name (%s) could not be fully parsed, but is processed";
            message = String.format(message, name.getTitleCache());
            //TODO
//            state.getResult().addWarning(message, state.getRow());
        }
        makePlantListIdentifier(state, name);
        makeIpniId(state, name);

        addSourceReference(state, name);
        return name;
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
            if (!references.startsWith("http://www.theplantlist.org/tpl1.1/record/")){
                String message = "The plant list identifier does not start with standard format http://www.theplantlist.org/tpl1.1/record/ : " + references;
                state.getResult().addWarning(message, state.getRow());
            }else{
                references = references.replace("http://www.theplantlist.org/tpl1.1/record/", "");
            }

            name.addIdentifier(references, identifierType);
        }
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
        //or do we want to allow to define an own sec reference?
        Reference sec = getTransactionalSourceReference(state);
        Taxon taxon = Taxon.NewInstance(name, sec);
        TaxonNode parentNode = getParentNode(state);
        if (parentNode != null){
            TaxonNode newNode = parentNode.addChildTaxon(taxon, null, null);
            if (state.getConfig().isUnplaced()){
                newNode.setUnplaced(true);
            }
        }
        addSourceReference(state, taxon);
        this.getTaxonService().saveOrUpdate(taxon);
        state.getResult().addNewRecord(taxon);
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
