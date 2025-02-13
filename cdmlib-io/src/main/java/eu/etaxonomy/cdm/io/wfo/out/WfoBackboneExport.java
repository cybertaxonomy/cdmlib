/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.wfo.out;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.format.reference.NomenclaturalSourceFormatter;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ExportResult.ExportResultState;
import eu.etaxonomy.cdm.io.common.TaxonNodeOutStreamPartitioner;
import eu.etaxonomy.cdm.io.common.XmlExportState;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IDescribable;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.model.term.IdentifierType;
import eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy;

/**
 * Classification or taxon tree exporter into WFO Backbone format.
 *
 * @see https://dev.e-taxonomy.eu/redmine/issues/10446
 *
 * @author a.mueller
 * @since 2023-12-08
 */
/**
 * @author muellera
 * @since 01.02.2024
 */
@Component
public class WfoBackboneExport
        extends CdmExportBase<WfoBackboneExportConfigurator,WfoBackboneExportState,IExportTransformer,File>{

    private static final long serialVersionUID = -4560488499411723333L;

    public WfoBackboneExport() {
        this.ioName = this.getClass().getSimpleName();
    }

    @Override
    public long countSteps(WfoBackboneExportState state) {
        TaxonNodeFilter filter = state.getConfig().getTaxonNodeFilter();
        return getTaxonNodeService().count(filter);
    }

    @Override
    protected void doInvoke(WfoBackboneExportState state) {

        try {
            IProgressMonitor monitor = state.getConfig().getProgressMonitor();
            WfoBackboneExportConfigurator config = state.getConfig();

            //set root node
            if (config.getTaxonNodeFilter().hasClassificationFilter()) {
                Classification classification = getClassificationService()
                        .load(config.getTaxonNodeFilter().getClassificationFilter().get(0).getUuid());
                state.setRootId(classification.getRootNode().getUuid());
            } else if (config.getTaxonNodeFilter().hasSubtreeFilter()) {
                state.setRootId(config.getTaxonNodeFilter().getSubtreeFilter().get(0).getUuid());
            }

            @SuppressWarnings({ "unchecked", "rawtypes" })
            TaxonNodeOutStreamPartitioner<XmlExportState> partitioner = TaxonNodeOutStreamPartitioner.NewInstance(this,
                    state, state.getConfig().getTaxonNodeFilter(), 100, monitor, null);

//          handleMetaData(state);  //FIXME metadata;
            monitor.subTask("Start partitioning");

            //test configurator
            String baseUrl = state.getConfig().getSourceLinkBaseUrl();
            if (isBlank(baseUrl)){
                String message = "No base url provided.";
                state.getResult().addWarning(message);
            } else if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")){
                String message = "Source link base url is not a http based url.";
                state.getResult().addWarning(message);
            }

            //taxon nodes
            TaxonNode node = partitioner.next();
            while (node != null) {
                handleTaxonNode(state, node);
                node = partitioner.next();
            }

            state.getProcessor().createFinalResult(state);
        } catch (Exception e) {
            state.getResult().addException(e,
                    "An unexpected error occurred in main method doInvoke() " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleTaxonNode(WfoBackboneExportState state, TaxonNode taxonNode) {

        if (taxonNode == null) {
            // TODO 5 taxon node not found
            String message = "TaxonNode for given taxon node UUID not found.";
            state.getResult().addError(message);
        } else {
            try {
                boolean exclude = filterTaxon(state, taxonNode);

                //handle taxon
                String parentWfoId = getParentWfoId(state, taxonNode);
                if (taxonNode.hasTaxon()) {
                    if (exclude) {
                        state.putTaxonNodeWfoId(taxonNode, parentWfoId); //always use parent instead
                    } else {
                        String wfoId = handleTaxon(state, taxonNode, parentWfoId);
                        state.putTaxonNodeWfoId(taxonNode, wfoId);
                    }
                }
            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when handling taxonNode "
                        + taxonNode.getUuid() + ": " + e.getMessage() + e.getStackTrace());
            }
        }
    }

    private String getParentWfoId(WfoBackboneExportState state, TaxonNode taxonNode) {
        TaxonNode parentNode = taxonNode.getParent();
        if (parentNode == null) {
            return null;
        }
        String wfoId = state.getTaxonNodeWfoId(parentNode);
        if (wfoId != null) {
            return wfoId;
        }else {
            wfoId = parentNode.getTaxon() == null ? null
                    : getWfoId(state, parentNode.getTaxon().getName(), false);
            if (wfoId != null) {
                state.putTaxonNodeWfoId(parentNode, wfoId);
                state.putTaxonWfoId(parentNode.getTaxon(), wfoId);
                state.putNameWfoId(parentNode.getTaxon().getName(), wfoId);
            }

            return wfoId;
        }
    }

    /**
     * @return <code>true</code> if the taxon should not be used in the export
     */
    private boolean filterTaxon(WfoBackboneExportState state, TaxonNode taxonNode) {
        Taxon taxon = taxonNode.getTaxon();
        if (taxon == null) {
            return true;
        }
        TaxonName taxonName = taxon.getName();
        if (taxonName == null) {
            return true;
        }else if (taxonName.isHybridFormula()) {
            return true;
        }else {
            String wfoId = getWfoId(state, taxonName, false);
            if (wfoId == null) {
                return true;
            }
        }

        Rank rank = taxonName.getRank();
        if (rank == null) {
            //TODO 3 is missing rank handling correct?
            return true;
        }else {
            if (rank.isSpeciesAggregate()){
                return true;
            }
        }
        return false;
    }

    /**
     * @return the WFO-ID of the taxon
     */
    private String handleTaxon(WfoBackboneExportState state, TaxonNode taxonNode, String parentWfoId) {

        final int classificationId = getClassificationId(taxonNode);

        //check null
        if (taxonNode == null) {
            state.getResult().addError("The taxonNode was null.", "handleTaxon");
            state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
            return null;
        }
        //check no taxon
        if (taxonNode.getTaxon() == null) {
            state.getResult().addError("There was a taxon node without a taxon: " + taxonNode.getUuid(),
                    "handleTaxon");
            state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
            return null;
        }

        //deproxy, just in case
        Taxon taxon = CdmBase.deproxy(taxonNode.getTaxon());
        String wfoId = null;

        //handle taxon
        try {

            //classification csvLine
            WfoBackboneExportTable table = WfoBackboneExportTable.CLASSIFICATION;
            String[] csvLine = new String[table.getSize()];

            //accepted name
            TaxonName name = taxon.getName();
            wfoId = handleName(state, table, csvLine, name, null, classificationId, true);

            //... parentNameUsageID
            csvLine[table.getIndex(WfoBackboneExportTable.TAX_PARENT_ID)] = parentWfoId;

            //... higher taxa
            //not necessary to fill according to #10652#note-2
            csvLine[table.getIndex(WfoBackboneExportTable.TAX_SUBFAMILY)] = null;
            csvLine[table.getIndex(WfoBackboneExportTable.TAX_TRIBE)] = null;
            csvLine[table.getIndex(WfoBackboneExportTable.TAX_SUBTRIBE)] = null;
            csvLine[table.getIndex(WfoBackboneExportTable.TAX_SUBGENUS)] = null;

            //... tax status, TODO 2 are there other status for accepted or other reasons for being ambiguous
            boolean isUnplaced = taxonNode.getStatus() != null && taxonNode.getStatus() == TaxonNodeStatus.UNPLACED;
            String taxonStatus = (taxon.isDoubtful() || isUnplaced) ? "ambiguous" : "Accepted";
            csvLine[table.getIndex(WfoBackboneExportTable.TAX_STATUS)] = taxonStatus;

            //remarks
            csvLine[table.getIndex(WfoBackboneExportTable.TAXON_REMARKS)] = getRemarks(state, taxon, taxon.getName());

            //TODO 7 URL to taxon, take it from a repository information (currently not yet possible, but maybe we could use a CDM preference instead)
            if (isNotBlank(state.getConfig().getSourceLinkBaseUrl())) {
                String taxonSourceLink = makeTaxonSourceLink(state, taxon);
                csvLine[table.getIndex(WfoBackboneExportTable.REFERENCES)] = taxonSourceLink;
            }

            //excluded info
            csvLine[table.getIndex(WfoBackboneExportTable.EXCLUDE)] = makeExcluded(state, taxonNode);

            handleTaxonBase(state, table, csvLine, taxon);

            handleSynonyms(state, taxon, classificationId);

            //process taxon line
            state.getProcessor().put(table, taxon, csvLine);

            return wfoId;

        } catch (Exception e) {
            e.printStackTrace();
            state.getResult().addException(e,
                    "An unexpected problem occurred when trying to export taxon with id " + taxon.getId() + " " + taxon.getTitleCache());
            state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
            return null;
        }
    }

    private int getClassificationId(TaxonNode taxonNode) {
        //check null just in case
        return taxonNode.getClassification() == null ? 0 : taxonNode.getClassification().getId();
    }

    private String makeExcluded(@SuppressWarnings("unused") WfoBackboneExportState state, TaxonNode taxonNode) {
        TaxonNodeStatus status = taxonNode.getStatus();
        if (status == null || (status != TaxonNodeStatus.EXCLUDED && !status.isKindOf(TaxonNodeStatus.EXCLUDED))) {
            return null;
        }else {
            Language lang = Language.getDefaultLanguage();  //TODO 7 language for status note

            String result = status == TaxonNodeStatus.EXCLUDED ? "Excluded" :
                 status == TaxonNodeStatus.EXCLUDED_TAX? "Taxonomically out of scope" : status.getLabel();
            String note = taxonNode.preferredPlacementNote(lang);
            result = CdmUtils.concat(": ", result, note);
            return result;
        }
    }

    private void handleTaxonBase(WfoBackboneExportState state, WfoBackboneExportTable table, String[] csvLine,
            TaxonBase<?> taxonBase) {

        //secundum reference
        Reference secRef = taxonBase.getSec();
        csvLine[table.getIndex(WfoBackboneExportTable.TAX_NAME_ACCORDING_TO_ID)] = getId(state, secRef);
        if (secRef != null
                && (!state.getReferenceStore().contains((secRef.getUuid())))) {
            handleReference(state, taxonBase.getSecSource());
        }

        //created (not needed according to #10652#note-2)
        csvLine[table.getIndex(WfoBackboneExportTable.CREATED)] = null;

        //modified (not needed according to #10652#note-2)
        csvLine[table.getIndex(WfoBackboneExportTable.MODIFIED)] = null;

    }

    private String makeTaxonSourceLink(WfoBackboneExportState state, Taxon taxon) {
        if (state.getConfig().isUseNameLink()) {
            return makeNameSourceLink(state, taxon.getName());
        } else {
            return getBaseUrl(state) + "cdm_dataportal/taxon/" + taxon.getUuid() ;
        }
    }

    private String makeSynonymSourceLink(WfoBackboneExportState state, Synonym synonym) {
        if (state.getConfig().isUseNameLink()) {
            return makeNameSourceLink(state, synonym.getName());
        } else {
            return getBaseUrl(state) + "cdm_dataportal/taxon/" +
                synonym.getAcceptedTaxon().getUuid()
                    + "/synonymy?highlight=" + synonym.getUuid();
        }
    }

    private String makeNameSourceLink(WfoBackboneExportState state, TaxonName name) {
        return getBaseUrl(state) + "cdm_dataportal/name/" + name.getUuid();
    }

    private String getBaseUrl(WfoBackboneExportState state) {
        String baseUrl = state.getConfig().getSourceLinkBaseUrl();
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        return baseUrl;
    }

    private void handleSynonyms(WfoBackboneExportState state, Taxon taxon, final int classificationId) {

        if (!state.getConfig().isDoSynonyms()) {
            return;
        }

        //homotypic group / synonyms
        HomotypicalGroup homotypicGroup = taxon.getHomotypicGroup();
        handleHomotypicalGroup(state, homotypicGroup, taxon);
        for (Synonym syn : taxon.getSynonymsInGroup(homotypicGroup)) {
            handleSynonym(state, syn, true, classificationId);
        }

        List<HomotypicalGroup> heterotypicHomotypicGroups = taxon.getHeterotypicSynonymyGroups();
        for (HomotypicalGroup group: heterotypicHomotypicGroups){
            handleHomotypicalGroup(state, group, taxon);
            for (Synonym syn : taxon.getSynonymsInGroup(group)) {
                handleSynonym(state, syn, false, classificationId);
            }
        }
    }

    private boolean isUrl(String url) {
        try {
            if (url.startsWith("http")) {
                URI.fromString(url);
                return true;
            }
        } catch (Exception e) {
            //exception should return false
        }
        return false;
    }

    //TODO 4 is remark handling correct?
    private String getRemarks(WfoBackboneExportState state, TaxonBase<?> taxonBase, TaxonName name) {

        String remarks = null;

        Set<UUID> includedAnnotationTypes = new HashSet<>();
        //TODO 5 make annotation types configurable
        includedAnnotationTypes.add(AnnotationType.uuidEditorial);

        Set<UUID> includedTaxonFactTypes = new HashSet<>();  //make taxon remark facts configurable
        includedTaxonFactTypes.add(Feature.uuidNotes);
        Set<UUID> includedNameFactTypes = null;  //TODO 7 make name facts configurable

        String nameAnnotations = getAnnotations(state, name, includedAnnotationTypes);
        String nameFacts = getFacts(state, name, includedNameFactTypes);
        String taxonAnnotations = getAnnotations(state, taxonBase, includedAnnotationTypes);
        String taxonFacts = taxonBase == null || !taxonBase.isInstanceOf(Taxon.class)? null :
            getFacts(state, CdmBase.deproxy(taxonBase, Taxon.class), includedTaxonFactTypes);

        remarks = CdmUtils.concat("; ", nameAnnotations, nameFacts, taxonAnnotations, taxonFacts);

        return remarks;
    }

    //TODO 4 move to a more general place for reusing and/or make it more performant
    private String getFacts(@SuppressWarnings("unused") WfoBackboneExportState state,
            IDescribable<?> entity,
            Set<UUID> includedFeatures) {

        if (entity == null) {
            return null;
        }
        String result = null;
        for (DescriptionBase<?> db : entity.getDescriptions()) {
            if (db.isPublish()) {
                for(DescriptionElementBase deb : db.getElements()){
                    UUID featureUuid = deb.getFeature()==null ? null: deb.getFeature().getUuid();
                    if (includedFeatures == null ||
                            includedFeatures.contains(featureUuid)){
                        //TODO 9 other fact types
                        if (deb.isInstanceOf(TextData.class)) {
                            TextData td = CdmBase.deproxy(deb, TextData.class);
                            //TODO 7 handle locale
                            LanguageString text = td.getPreferredLanguageString(Language.DEFAULT());
                            if (text != null) {
                                result = CdmUtils.concat(";", result, text.getText());
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private String getAnnotations(@SuppressWarnings("unused") WfoBackboneExportState state,
            AnnotatableEntity entity,
            Set<UUID> includedAnnotationTypes) {

        if (entity == null) {
            return null;
        }
        String result = null;
        for (Annotation a : entity.getAnnotations()) {
            UUID typeUuid = a.getAnnotationType()==null ? null:  a.getAnnotationType().getUuid();
            if (includedAnnotationTypes == null ||
                    includedAnnotationTypes.contains(typeUuid)){
                result = CdmUtils.concat(";", result, a.getText());
            }
        }
        return result;
    }

    private String createMultilanguageString(Map<Language, LanguageString> multilanguageText) {
        String text = "";
        int index = multilanguageText.size();
        for (LanguageString langString : multilanguageText.values()) {
            text += langString.getText();
            if (index > 1) {
                text += "; ";
            }
            index--;
        }
        return text;
    }

    private String createAnnotationsString(Set<Annotation> annotations) {
        StringBuffer strBuff = new StringBuffer();

        for (Annotation ann : annotations) {
            if (ann.getAnnotationType() == null || !ann.getAnnotationType().equals(AnnotationType.INTERNAL())) {
                strBuff.append(ann.getText());
                strBuff.append("; ");
            }
        }

        if (strBuff.length() > 2) {
            return strBuff.substring(0, strBuff.length() - 2);
        } else {
            return null;
        }
    }

    private String getId(WfoBackboneExportState state, ICdmBase cdmBase) {
        if (cdmBase == null) {
            return "";
        }
        // TODO 4 id type, make configurable
        return cdmBase.getUuid().toString();
    }

    private void handleSynonym(WfoBackboneExportState state, Synonym synonym,
            boolean isHomotypic, final int classificationId) {
        try {
            if (isUnpublished(state.getConfig(), synonym)) {
                return;
            }

            WfoBackboneExportTable table = WfoBackboneExportTable.CLASSIFICATION;
            String[] csvLine = new String[table.getSize()];

            TaxonName name = synonym.getName();

            //accepted name id
            String acceptedWfoId = null;
            if (synonym.getAcceptedTaxon()!= null && synonym.getAcceptedTaxon().getName() != null) {
                TaxonName acceptedName = synonym.getAcceptedTaxon().getName();
                acceptedWfoId = getWfoId(state, acceptedName, false);
                if (acceptedWfoId == null) {
                    String message = "WFO-ID for accepted name is missing. This should not happen. Synonym: " + name.getTitleCache() + "; Accepted name: " + acceptedName.getTitleCache();
                    state.getResult().addError(message, "handleName");
                    state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
                }
                csvLine[table.getIndex(WfoBackboneExportTable.TAX_ACCEPTED_NAME_ID)] = acceptedWfoId;
            }

            String wfoId = handleName(state, table, csvLine, name, acceptedWfoId, classificationId, isHomotypic);
            if (wfoId == null) {
                return;
            }


            //status
            csvLine[table.getIndex(WfoBackboneExportTable.TAX_STATUS)] = isHomotypicStr(isHomotypic);

            //TODO 5 URL to taxon, take it from a repository information (currently not yet possible, but maybe we could use a CDM preference instead)
            if (isNotBlank(state.getConfig().getSourceLinkBaseUrl())) {
                String taxonSourceLink = makeSynonymSourceLink(state, synonym);
                csvLine[table.getIndex(WfoBackboneExportTable.REFERENCES)] = taxonSourceLink;
            }

            handleTaxonBase(state, table, csvLine, synonym);

            state.getProcessor().put(table, synonym, csvLine);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling synonym "
                    + cdmBaseStr(synonym) + ": " + e.getMessage());
        }
    }

    private String isHomotypicStr(boolean isHomotypic) {
        return isHomotypic ? "homotypicSynonym" : "heterotypicSynonym";
    }

    private String handleName(WfoBackboneExportState state, WfoBackboneExportTable table, String[] csvLine,
            TaxonName name, String acceptedNameWfoId, final int classificationId, boolean isHomotypic) {

        name = CdmBase.deproxy(name);
        if (name == null || state.getNameStore().containsKey(name.getId())) {
            if (name == null) {
                state.getResult().addError("No name was given for taxon.", "handleName");
                state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
            }
            return null;
        }
        String wfoId = null;
        try {

            state.getNameStore().put(name.getId(), name.getUuid());

            //taxon ID/WFO ID
            wfoId = getWfoId(state, name, false);
            if (isBlank(wfoId)) {
                String message = "No WFO-ID given for taxon name " + name.getTitleCache() + ". Taxon/Synonym/Name ignored.";
                state.getResult().addError(message);
                state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
                return null;
            }else {
                csvLine[table.getIndex(WfoBackboneExportTable.TAXON_ID)] = wfoId;
            }

            boolean warnIfNotExists = false;
            csvLine[table.getIndex(WfoBackboneExportTable.NAME_SCIENTIFIC_NAME_ID)] = getIpniId(state, name, warnIfNotExists);

            //localID
            csvLine[table.getIndex(WfoBackboneExportTable.NAME_LOCAL_ID)] = getId(state, name);

            //scientificName
            if (name.isProtectedTitleCache()) {
                //TODO 7 make it configurable if we should always take titleCache if titleCache is protected, as nameCache may not necessarily
                //     have complete data if titleCache is protected as it is considered to be irrelevant or at least preliminary
                String message = "";
                if (StringUtils.isNotEmpty(name.getNameCache())) {
                    csvLine[table.getIndex(WfoBackboneExportTable.NAME_SCIENTIFIC_NAME)] = name.getNameCache();
                    message = "ScientificName: Name cache " + name.getNameCache() + " used for name with protected titleCache " +  name.getTitleCache();
                }else {
                    csvLine[table.getIndex(WfoBackboneExportTable.NAME_SCIENTIFIC_NAME)] = name.getTitleCache();
                    message = "ScientificName: Name has protected titleCache and no explicit nameCache: " +  name.getTitleCache();
                }
                state.getResult().addWarning(message);  //TODO 7 add location to warning
            } else {
                csvLine[table.getIndex(WfoBackboneExportTable.NAME_SCIENTIFIC_NAME)] = name.getNameCache();
            }

            //rank
            Rank rank = name.getRank();
            String rankStr = state.getTransformer().getCacheByRank(rank);
            if (rankStr == null) {
                String message = rank == null ? "No rank" : ("Rank not supported by WFO: " + rank.getLabel())
                        + "Taxon not handled in export: " + name.getTitleCache();
                state.getResult().addWarning(message, wfoId);
            }
            csvLine[table.getIndex(WfoBackboneExportTable.RANK)] = rankStr;
            //verbatimTaxonRank (only needed for exceptional ranks, see #10652#note-2)
            String verbatimRankStr = rank == null ? "no rank given" : rankStr == null ? rank.getLabel() : rankStr.startsWith("infra") ? rankStr : null;
            csvLine[table.getIndex(WfoBackboneExportTable.NAME_VERBATIM_RANK)] = verbatimRankStr;

            //authorship
            String authorshipCache = normalizedAuthor(state, name);
            if (CdmUtils.isBlank(authorshipCache)) {
                String message = "Taxon name has no authorship: " + name.getTitleCache();
                state.getResult().addWarning(message, wfoId);
            }
            csvLine[table.getIndex(WfoBackboneExportTable.NAME_AUTHORSHIP)] = authorshipCache;

            //family (use familyStr if provided, otherwise try to compute from the family taxon)
            String familyStr = state.getFamilyStr();
            if (StringUtils.isBlank(familyStr)){
                if (Rank.FAMILY().equals(name.getRank())){
                    familyStr = name.getNameCache();
                }
                if (StringUtils.isNotBlank(familyStr)) {
                    state.setFamilyStr(familyStr);
                }else {
                    String message = "Obligatory family information is missing";
                    state.getResult().addWarning(message);
                }
            }
            csvLine[table.getIndex(WfoBackboneExportTable.TAX_FAMILY)] = state.getFamilyStr();

            //name parts
            csvLine[table.getIndex(WfoBackboneExportTable.TAX_GENUS)] = name.isSupraGeneric()? null : name.getGenusOrUninomial();
            csvLine[table.getIndex(WfoBackboneExportTable.NAME_SPECIFIC_EPITHET)] = name.getSpecificEpithet();
            csvLine[table.getIndex(WfoBackboneExportTable.NAME_INFRASPECIFIC_EPITHET)] = name.getInfraSpecificEpithet();

            //name status
            csvLine[table.getIndex(WfoBackboneExportTable.NAME_STATUS)] = makeNameStatus(state, name);

            //nom. ref
            String nomRef = NomenclaturalSourceFormatter.INSTANCE().format(name.getNomenclaturalSource());
            csvLine[table.getIndex(WfoBackboneExportTable.NAME_PUBLISHED_IN)] = nomRef;

            //originalNameID
            TaxonName originalName = name.getBasionym();  //TODO 5 basionym, order in case there are >1 basionyms
            if (originalName == null) {
                originalName = name.getReplacedSynonyms().stream().findFirst().orElse(null);
            }

            if (originalName != null) {
                if (!state.getNameStore().containsKey(originalName.getId())) {
                    boolean originalNameIsAlsoTaxonBase = isNameAlsoPartOfExportedTaxonBase(originalName, classificationId);
                    if (!originalNameIsAlsoTaxonBase) {
                        String message = "Original name/basionym exists but is not a taxon/synonym in the "
                                + "classification. New name: " + name.getTitleCache() + ", originalName: " + originalName.getTitleCache() ;
                        state.getResult().addWarning(message, wfoId);
                    }
                }
                String basionymId = getWfoId(state, originalName, false);
                csvLine[table.getIndex(WfoBackboneExportTable.NAME_ORIGINAL_NAME_ID)] = basionymId;
            }

            //original spelling
            TaxonName originalSpelling = name.getOriginalSpelling();
            acceptedNameWfoId = acceptedNameWfoId == null? wfoId : acceptedNameWfoId;
            if (originalSpelling != null) {
                handleOrthographicVariants(state, table, originalSpelling, name, acceptedNameWfoId, classificationId, isHomotypic);
            }

            //orth. var.
            Set<TaxonName> orthVars = getOrthographicVariants(name);
            for (TaxonName orthVar : orthVars) {
                handleOrthographicVariants(state, table, orthVar, name, acceptedNameWfoId, classificationId, isHomotypic);
            }

         } catch (Exception e) {
            state.getResult().addException(e,
                    "An unexpected error occurred when handling the name " + cdmBaseStr(name) + ": " + name.getTitleCache() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return wfoId;
    }

    /**
     * Normalizes the author string by, for example, normalizing to IPNI standard
     * (no whitespaces) and removing double whitespaces
     */
    private String normalizedAuthor(WfoBackboneExportState state, TaxonName name) {
        if (name == null) {
            return null;
        } else if (state.getConfig().isNormalizeAuthorsToIpniStandard()) {
            return TeamDefaultCacheStrategy.removeWhitespaces(name.getAuthorshipCache());
        } else {
            String result = name.getAuthorshipCache();
            if (result == null) {
                return null;
            }else {
                return result.replaceAll("\\s+", " ").trim();
            }
        }
    }

    private Set<TaxonName> getOrthographicVariants(TaxonName name) {
        Set<TaxonName> result = new HashSet<>();
        Set<NameRelationship> rels = name.getRelationsToThisName();
        for(NameRelationship rel : rels) {
            //TODO 2 orth. var. handle also other type and other direction for orth. var.
            if(rel.getType().getUuid().equals(NameRelationshipType.uuidOrthographicVariant)) {
                result.add(rel.getFromName());
            }
        }
        return result;
    }

    /**
     * Handle names not being handled via taxonbase.
     */
    private void handleOrthographicVariants(WfoBackboneExportState state, WfoBackboneExportTable table,
            TaxonName name, TaxonName mainName, String acceptedNameWfoId, final int classificationId,
            boolean isHomotypic) {

        //check, if orth. var. name is not used elsewhere as synonym or accepted name
        boolean isAlsoTaxonBase = isNameAlsoPartOfExportedTaxonBase(name, classificationId);
        if (isAlsoTaxonBase) {
            return;
        }

        //create record
        String[] csvLine = new String[table.getSize()];

        //handle name related values
        String wfoID = handleName(state, table, csvLine, name, acceptedNameWfoId, classificationId, isHomotypic);
        if (wfoID == null) {
            String message = "Original spelling, orthographic variant or misspelling "
                    + "'" + name + "' for name '" + mainName +"' does not have a WFO-ID"
                    + " and therefore can not be exported";
            state.getResult().addWarning(message);
            return;
        }

        //acceptedNameId
        csvLine[table.getIndex(WfoBackboneExportTable.TAX_ACCEPTED_NAME_ID)] = acceptedNameWfoId;

        //authorship, take from main name if it does not exist
        //TODO 3 take from csvLine of both names (AM: 2025-01-03 don't know what is meant here)
        if (isBlank(normalizedAuthor(state, name))) {
            csvLine[table.getIndex(WfoBackboneExportTable.NAME_AUTHORSHIP)]
                    = normalizedAuthor(state, mainName);
        }

        //nom. ref, take from main name if it does not exist
        //TODO 3 take from csvLine of both names (AM: 2025-01-03 don't know what is meant here)
        String nomRef = NomenclaturalSourceFormatter.INSTANCE().format(name.getNomenclaturalSource());
        if (isBlank(nomRef)) {
            nomRef = NomenclaturalSourceFormatter.INSTANCE().format(mainName.getNomenclaturalSource());
            csvLine[table.getIndex(WfoBackboneExportTable.NAME_PUBLISHED_IN)] = nomRef;
        }

        csvLine[table.getIndex(WfoBackboneExportTable.TAX_STATUS)] = isHomotypicStr(isHomotypic);

        csvLine[table.getIndex(WfoBackboneExportTable.NAME_STATUS)] = "orthografia";

        //nameAccordingTo
        //for now we do not add a secundum references to pure names
        csvLine[table.getIndex(WfoBackboneExportTable.TAX_NAME_ACCORDING_TO_ID)] = null;

        //remarks
        getRemarks(state, null, name);

        //references
        //TODO 5 references, take baseUrl from a repository information (currently not yet possible, but maybe we could use a CDM preference instead)
        //TODO 5 if we know without doubt that original spelling/orth. var. is shown in synonymy we could also link to synonymy
        if (isNotBlank(state.getConfig().getSourceLinkBaseUrl())) {
            String taxonSourceLink = makeNameSourceLink(state, name);
            csvLine[table.getIndex(WfoBackboneExportTable.REFERENCES)] = taxonSourceLink;
        }

        //created, modified not needed (#10652#note-2)

        //process original spelling
        state.getProcessor().put(table, name, csvLine);
    }

    private boolean isNameAlsoPartOfExportedTaxonBase(TaxonName name, int classificationId) {
        //TODO 7 maybe better compare with root node treeindex of exported root node
        //       instead of classificationId to check if it is even in the exported group

        Set<TaxonNode> taxonBasesInClassificationForName = name.getTaxonBases().stream().map(tb->tb.acceptedTaxon()).flatMap(acc->acc.getTaxonNodes().stream())
                .filter(tn->getClassificationId(tn) == classificationId)
                .collect(Collectors.toSet());

        return !taxonBasesInClassificationForName.isEmpty();
    }

    private String getWfoId(WfoBackboneExportState state, TaxonName name, boolean warnIfNotExists) {
        Identifier wfoId = name.getIdentifier(IdentifierType.uuidWfoNameIdentifier);
        if (wfoId == null && warnIfNotExists) {
            String message = "No wfo-id given for name: " + name.getTitleCache()+"/"+ name.getUuid();
            state.getResult().addWarning(message);  //TODO 5 data location
        }
        return wfoId == null ? null : wfoId.getIdentifier();
    }

    private String getIpniId(WfoBackboneExportState state, TaxonName name, boolean warnIfNotExists) {
        Identifier ipniId = name.getIdentifier(IdentifierType.uuidIpniNameIdentifier);
        if (ipniId == null && warnIfNotExists) {
            String message = "No ipni-id given for name: " + name.getTitleCache()+"/"+ name.getUuid();
            state.getResult().addWarning(message);  //TODO 5 data location
        }
        String ipniIdStr = ipniId == null ? null : ipniId.getIdentifier();
        if (ipniIdStr != null && ipniIdStr.matches(IdentifierType.IPNI_NAME_ID_PATTERN)){
            ipniIdStr = "urn:lsid:ipni.org:names:" + ipniIdStr;
        }
        return ipniIdStr;
    }

    private String makeNameStatus(WfoBackboneExportState state, TaxonName name) {
        try {

            //TODO 2 what is with dubium
            if (name.isLegitimate()) {
                if (name.isConserved()) {
                    return "Conserved";
                }else {
                    return "Valid";
                }
            } else if (name.isRejected()) {
                return "Rejected";
            } else if (name.isIllegitimate()) {
                return "Illegitimate";
            } else if (name.isInvalid()) {
                if (name.isOrthographicVariant()) {
                    return "orthografia";
                }else {
                    return "invalid";
                }
            } else {
                String message = "Unhandled name status case for name: " + name.getTitleCache() +
                        ". Status not handled correctly.";
                state.getResult().addWarning(message);
                return "undefined";
            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when extracting status string for "
                    + cdmBaseStr(name) + ": " + e.getMessage());
            return "";
        }
    }

    private void handleHomotypicalGroup(WfoBackboneExportState state, HomotypicalGroup group, Taxon acceptedTaxon) {
        try {

            List<TaxonName> typifiedNames = new ArrayList<>();
            if (acceptedTaxon != null){
                List<Synonym> synonymsInGroup = acceptedTaxon.getSynonymsInGroup(group);
                if (group.equals(acceptedTaxon.getHomotypicGroup())){
                    typifiedNames.add(acceptedTaxon.getName());
                }
                synonymsInGroup.stream().forEach(synonym -> typifiedNames.add(CdmBase.deproxy(synonym.getName())));
            }


            TaxonName firstname = null;
            for (TaxonName name: typifiedNames){
                Iterator<Taxon> taxa = name.getTaxa().iterator();
                while(taxa.hasNext()){
                    Taxon taxon = taxa.next();
                    if(!(taxon.isMisapplication() || taxon.isProparteSynonym())){
                        firstname = name;
                        break;
                    }
                }
            }

        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling homotypic group "
                    + cdmBaseStr(group) + ": " + e.getMessage());
        }
    }

    private void handleReference(WfoBackboneExportState state, OriginalSourceBase source) {

        if (source == null || source.getCitation() == null) {
            return;
        }
        Reference reference = source.getCitation();

        //TODO 5 allow handle sec detail in ref table (if allowed by the export guide)
        //     this is also important for adding external links which are source specific (see below)
        try {
            if (state.getReferenceStore().contains(reference.getUuid())) {
                return;
            }
            reference = CdmBase.deproxy(reference);

            state.addReferenceToStore(reference);
            WfoBackboneExportTable table = WfoBackboneExportTable.REFERENCE;
            String[] csvLine = new String[table.getSize()];

            csvLine[table.getIndex(WfoBackboneExportTable.IDENTIFIER)] = getId(state, reference);

            //TODO 2 correct?, ref biblio citation
            csvLine[table.getIndex(WfoBackboneExportTable.REF_BIBLIO_CITATION)] = reference.getCitation();

            //ref uri  TODO 6 make ref URIs configurable
            if (reference.getDoi() != null) {
                csvLine[table.getIndex(WfoBackboneExportTable.REF_URI)] = reference.getDoiString();
            }else if (reference.getUri() != null) {
                csvLine[table.getIndex(WfoBackboneExportTable.REF_URI)] = reference.getUri().toString();
            }else{
                //TODO 5 not yet added as it is source specific but uuid is reference specific
//                String uri = null;
//                for (ExternalLink link : source.getLinks()) {
//                    uri = CdmUtils.concat("; ", uri,  link.getUri() == null ? null : link.getUri().toString());
//                }
//                csvLine[table.getIndex(WfoBackboneExportTable.REF_URI)] = uri;
            }

            state.getProcessor().put(table, reference, csvLine);
        } catch (Exception e) {
            e.printStackTrace();
            state.getResult().addException(e, "An unexpected error occurred when handling reference "
                    + cdmBaseStr(reference) + ": " + e.getMessage());
        }
    }

    /**
     * Returns a string representation of the {@link CdmBase cdmBase} object for
     * result messages.
     */
    private String cdmBaseStr(CdmBase cdmBase) {
        if (cdmBase == null) {
            return "-no object available-";
        } else {
            return cdmBase.getClass().getSimpleName() + ": " + cdmBase.getUuid();
        }
    }

    @Override
    protected boolean doCheck(WfoBackboneExportState state) {
        return false;
    }

    @Override
    protected boolean isIgnore(WfoBackboneExportState state) {
        return false;
    }
}