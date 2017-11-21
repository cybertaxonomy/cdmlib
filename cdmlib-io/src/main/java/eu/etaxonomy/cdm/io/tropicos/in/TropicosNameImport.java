/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.tropicos.in;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.dto.IdentifiedEntityDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.csv.in.CsvImportBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @date 15.11.2017
 */
@Component
public class TropicosNameImport<STATE extends TropicosNameImportState>
        extends CsvImportBase<TropicosNameImportConfigurator, STATE, TaxonName>{

    private static final long serialVersionUID = -4111479364751713088L;

    private static final String INPUT_FULLNAME_WITH_AUTHORS = "FullnameWithAuthors";
    private static final String INPUT_FULL_NAME_NO_AUTHORS = "FullnameNoAuthors";
    private static final String INPUT_SOURCE_ID = "SourceID";

    private static final String OUTPUT_NAME_ID = "OutputNameID";
    private static final String OUTPUT_HOW_MATCHED = "OutputHowMatched";
    private static final String OUTPUT_FULL_NAME_WITH_AUTHORS = "OutputFullNameWithAuthors";
    private static final String OUTPUT_ABBREV_TITLE = "OutputAbbreviatedTitle";
    private static final String OUTPUT_COLLATION = "OutputCollation";
    private static final String OUTPUT_VOLUME = "OutputVolume";
    private static final String OUTPUT_ISSUE = "OutputIssue";
    private static final String OUTPUT_PAGE = "OutputPage";
    private static final String OUTPUT_TITLE_PAGE_YEAR = "OutputTitlePageYear";
    private static final String OUTPUT_YEAR_PUBLISHED = "OutputYearPublished";
    private static final String OUTPUT_NOM_STATUS = "OutputNomenclatureStatus";
    private static final String OUTPUT_BHL_LINK = "OutputBHLLink";
    private static final String OUTPUT_BATCH_ID = "OutputBatchID";
    private static final String NOM_PUB_TYPE = "NomPubType";
    private static final String IPNI_ID = "IPNI-ID";


    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleSingleLine(STATE state) {
        TaxonName name = makeName(state);
        if (name == null){
            return;
        }
        if (checkAndAddIdentifier(state, name, INPUT_SOURCE_ID,
                state.getConfig().isAllowWfoDuplicates(), DefinedTerm.IDENTIFIER_NAME_WFO())){
            return;
        }
        if (checkAndAddIdentifier(state, name, IPNI_ID,
                state.getConfig().isAllowIpniDuplicates(), DefinedTerm.IDENTIFIER_NAME_IPNI())){
            return;
        }
        if (checkAndAddIdentifier(state, name, OUTPUT_NAME_ID,
                state.getConfig().isAllowTropicosDuplicates(), DefinedTerm.IDENTIFIER_NAME_TROPICOS())){
            return;
        }

        Map<String, String> record = state.getCurrentRecord();

        makeReference(state, name);
        makeProtologue(state, name);
        makeNomStatus(state, name);

        if (record.get(OUTPUT_HOW_MATCHED) != null){
            //ignore
        }
        if (record.get(OUTPUT_ISSUE) != null){
            //ignore
        }
        if (record.get(OUTPUT_BATCH_ID) != null){
            //ignore
        }
        if (record.get(OUTPUT_COLLATION) != null){
            //ignore
        }
        state.getDedupHelper().replaceAuthorNamesAndNomRef(state, name);

        getNameService().saveOrUpdate(name);
        makeTaxon(state, name);
    }


    /**
     * @param state
     * @param name
     */
    private void makeNomStatus(STATE state, TaxonName name) {
        String nomStatusStr = state.getCurrentRecord().get(OUTPUT_NOM_STATUS);
        if (nomStatusStr == null || nomStatusStr.equalsIgnoreCase("No opinion")){
            return;
        }else{
            NomenclaturalStatusType status = null;
            try {
                status = NomenclaturalStatusType.getNomenclaturalStatusTypeByLabel(nomStatusStr);
            } catch (UnknownCdmTypeException e) {
                try {
                    status = NomenclaturalStatusType.getNomenclaturalStatusTypeByAbbreviation(nomStatusStr, name);
                } catch (UnknownCdmTypeException e1) {
                    //handle later
                }
            }
            if (status == null){
                String message = "Nomenclatural status '%s' not recognized.";
                message = String.format(message, nomStatusStr);
                state.getResult().addWarning(message, state.getRow());
            }else{
                name.addStatus(status, null, null);
            }
        }
    }

    /**
     * @param state
     * @param name
     */
    private void makeProtologue(STATE state, TaxonName name) {
        String bhlLink = state.getCurrentRecord().get(OUTPUT_BHL_LINK);
        if (bhlLink == null){
            return;
        }

        TextData textData = TextData.NewInstance(Feature.PROTOLOGUE());
        this.getNameDescription(name, state).addElement(textData);
        URI uri;
        try {
            uri = new URI(bhlLink);
            textData.addMedia(Media.NewInstance(uri, null, null, null));

        } catch (URISyntaxException e) {
            String message = "(BHL) Link could not be recognized as valid URI. Link was not added to %s: %s";
            message = String.format(message, name.getTitleCache(), bhlLink);
            state.getResult().addWarning(message, state.getRow());
        }
    }

    //TODO implementation must be improved when matching of taxon names with existing names is implemented
    //=> the assumption that the only description is the description added by this import
    //is wrong then, but once protologues are handled differently we don't need this anymore
    //anyway
    private TaxonNameDescription getNameDescription(TaxonName name, STATE state) {
        Set<TaxonNameDescription> descriptions = name.getDescriptions();
        if (descriptions.size()>1){
            throw new IllegalStateException("Implementation does not yet support names with multiple descriptions");
        }else if (descriptions.size()==1){
            return descriptions.iterator().next();
        }else{
            TaxonNameDescription desc = TaxonNameDescription.NewInstance(name);
            desc.addSource(OriginalSourceType.Import, null, "NameDescription", getTransactionalSourceReference(state), null);
            return desc;
        }
    }

    /**
     * @param state
     * @param name
     */
    private void makeReference(STATE state, TaxonName name) {


        Map<String, String> record = state.getCurrentRecord();
        String type = record.get(NOM_PUB_TYPE);
        String abbrevTitle = record.get(OUTPUT_ABBREV_TITLE);
        String volume = record.get(OUTPUT_VOLUME);
        String detail = record.get(OUTPUT_PAGE);
        String titlePageYear = record.get(OUTPUT_TITLE_PAGE_YEAR);
        String yearPublished = record.get(OUTPUT_YEAR_PUBLISHED);
        if (CdmUtils.isBlank(abbrevTitle, volume, detail, titlePageYear, yearPublished)){
            //TODO
//            state.getResult().addInfo("No nomenclatural reference information given");
            return;
        }

        Reference reference;
        if (type == null){
            //TODO check against DB
            reference = ReferenceFactory.newGeneric();
            reference.setAbbrevTitle(abbrevTitle);
        }else if (type.equals("A")){
            reference = ReferenceFactory.newArticle();
            Reference journal = ReferenceFactory.newJournal();
            journal.setAbbrevTitle(abbrevTitle);
            reference.setInJournal(journal);
        }else if (type.equals("B")){
            reference = ReferenceFactory.newBook();
            reference.setAbbrevTitle(abbrevTitle);
        }else{
            String message = "Value for %s not recognized. Use generic reference instead";
            message = String.format(message, NOM_PUB_TYPE);
            state.getResult().addWarning(message, state.getRow());
            reference = ReferenceFactory.newGeneric();
            reference.setAbbrevTitle(abbrevTitle);
        }
        reference.setVolume(volume);

        name.setNomenclaturalReference(reference);
        name.setNomenclaturalMicroReference(detail);


        if (titlePageYear != null){
            if (yearPublished == null){
                TimePeriod tp = TimePeriodParser.parseString(titlePageYear);
                reference.setDatePublished(tp);
            }else{
                TimePeriod tp = TimePeriodParser.parseString(yearPublished);
                reference.setDatePublished(tp);
            }
        }else if (yearPublished != null){
            TimePeriod tp = TimePeriodParser.parseString(yearPublished);
            reference.setDatePublished(tp);
        }
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

        //TODO check for existing WFO ID
        Pager<IdentifiedEntityDTO<TaxonName>> existing = getNameService().findByIdentifier(TaxonName.class, identifier, identifierType, MatchMode.EXACT, false, null, null, null);
        if (! allowDuplicate && existing.getCount() > 0){
            String message = "The name with the given identifier (%s: %s) exists already in the database. Record is not imported.";
            //TODO make language configurable
            message = String.format(message, identifierType.getPreferredRepresentation(Language.DEFAULT()).getText(), identifier);
            state.getResult().addWarning(message, state.getRow());
            return true;
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
        String fullNameStr = record.get(OUTPUT_FULL_NAME_WITH_AUTHORS);
        String nameStr = record.get(INPUT_FULL_NAME_NO_AUTHORS);
        String inputFullNameStr = record.get(INPUT_FULLNAME_WITH_AUTHORS);
        if (inputFullNameStr != null && fullNameStr != null){
            if (inputFullNameStr.replaceAll("\\s", "").equals(fullNameStr.replaceAll("\\s", ""))){
                String message = "Full input (%s) and full output (%s) name are not equal. Record is, however, processed.";
                message = String.format(message, inputFullNameStr, fullNameStr);
                state.getResult().addWarning(message, state.getRow());
            }
        }else if (inputFullNameStr != null && fullNameStr == null){
            fullNameStr = inputFullNameStr;
        }

        INonViralName name;
        if (fullNameStr == null && nameStr == null){
            String message = "No name given. No record will be imported.";
            state.getResult().addWarning(message, state.getRow());
            return null;
        }else if (fullNameStr != null){
            name = parser.parseFullName(fullNameStr, state.getConfig().getNomenclaturalCode(), null);
            if (nameStr != null && !nameStr.equals(name.getNameCache())){
                String message = "Name with authors (%s) and without authors (%s) is not consistent";
                message = String.format(message, fullNameStr, nameStr);
                state.getResult().addWarning(message, state.getRow());
            }
        }else{
            name = parser.parseSimpleName(nameStr, state.getConfig().getNomenclaturalCode(), null);
        }
        if (name.isProtectedTitleCache() || name.isProtectedNameCache() || name.isProtectedAuthorshipCache()){
            String message = "Name (%s) could not be fully parsed, but is processed";
            message = String.format(message, name.getTitleCache());
            state.getResult().addWarning(message, state.getRow());
        }
        addSourceReference(state, (TaxonName)name);
        return (TaxonName)name;
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
    }


    /**
     * @param state
     * @param name
     */
    private void makeTaxon(STATE state, TaxonName name) {
        if (!state.getConfig().isCreateTaxa()){
            return;
        }else{
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
