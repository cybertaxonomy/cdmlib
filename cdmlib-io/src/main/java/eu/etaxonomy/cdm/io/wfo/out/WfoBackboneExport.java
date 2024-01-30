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

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
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
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.IdentifierType;

/**
 * Classification or taxon tree exporter into WFO Backbone format.
 *
 * @author a.mueller
 * @since 2023-12-08
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
                    : getWfoId(state, parentNode.getTaxon().getName(), true);
            if (wfoId != null) {
                state.putTaxonNodeWfoId(parentNode, wfoId);
                state.putTaxonWfoId(parentNode.getTaxon(), wfoId);
                state.putNameWfoId(parentNode.getTaxon().getName(), wfoId);
            }

            return wfoId;
        }
    }

    private Set<Rank> allowedRanks = new HashSet<>();
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
            return true;
        }else {
            if (allowedRanks.isEmpty()) {
                allowedRanks.add(Rank.FAMILY());
                allowedRanks.add(Rank.SUBFAMILY());
                allowedRanks.add(Rank.TRIBE());
                allowedRanks.add(Rank.SUBTRIBE());
                allowedRanks.add(Rank.GENUS());
                allowedRanks.add(Rank.SUBGENUS());
                allowedRanks.add(Rank.SECTION_BOTANY());
                allowedRanks.add(Rank.SPECIES());
                allowedRanks.add(Rank.SUBSPECIES());
                allowedRanks.add(Rank.VARIETY());
                allowedRanks.add(Rank.SUBVARIETY());
                allowedRanks.add(Rank.FORM());
                allowedRanks.add(Rank.SUBFORM());
                allowedRanks.add(Rank.INFRASPECIFICTAXON());
            }
            if (!allowedRanks.contains(rank)) {
                //TODO 3 warn if this happens as such names should not have a wfo-id neither
                return true;
            }
        }
        return false;
    }

    /**
     * @return the WFO-ID of the taxon
     */
    private String handleTaxon(WfoBackboneExportState state, TaxonNode taxonNode, String parentWfoId) {

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
            wfoId = handleName(state, table, csvLine, name);

            //... parentNameUsageID
            csvLine[table.getIndex(WfoBackboneExportTable.TAX_PARENT_ID)] = parentWfoId;

            //... higher taxa
            csvLine[table.getIndex(WfoBackboneExportTable.TAX_SUBFAMILY)] = null;
            csvLine[table.getIndex(WfoBackboneExportTable.TAX_TRIBE)] = null;
            csvLine[table.getIndex(WfoBackboneExportTable.TAX_SUBTRIBE)] = null;
            //TODO 2 is subgenus handling correct?
            csvLine[table.getIndex(WfoBackboneExportTable.TAX_SUBGENUS)] = name.isInfraGeneric()? name.getInfraGenericEpithet() : null ;

            //... tax status, TODO 2 are there other status for accepted or other reasons for being ambiguous
            String taxonStatus = taxon.isDoubtful()? "ambiguous" : "Accepted";
            csvLine[table.getIndex(WfoBackboneExportTable.TAX_STATUS)] = taxonStatus;

            //secundum reference
            csvLine[table.getIndex(WfoBackboneExportTable.TAX_NAME_ACCORDING_TO_ID)] = getId(state, taxon.getSec());
            if (taxon.getSec() != null
                    && (!state.getReferenceStore().contains((taxon.getSec().getUuid())))) {
                handleReference(state, taxon.getSec());
            }

            //TODO 2 remarks, what exactly
            csvLine[table.getIndex(WfoBackboneExportTable.TAXON_REMARKS)] = getRemarks(name);

            handleSynonyms(state, taxon);

            //TODO 2 taxon provisional, still an open issue?
//                csvLine[table.getIndex(WfoBackboneExportTable.TAX_PROVISIONAL)] = taxonNode.isDoubtful() ? "1" : "0";

            //TODO 1 taxon only published

            //process taxon line
            state.getProcessor().put(table, taxon, csvLine);

        } catch (Exception e) {
            e.printStackTrace();
            state.getResult().addException(e,
                    "An unexpected problem occurred when trying to export taxon with id " + taxon.getId() + " " + taxon.getTitleCache());
            state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
            return null;
        }

        return wfoId;
    }

    private void handleSynonyms(WfoBackboneExportState state, Taxon taxon) {

        if (!state.getConfig().isDoSynonyms()) {
            return;
        }

        //homotypic group / synonyms
        HomotypicalGroup homotypicGroup = taxon.getHomotypicGroup();
        handleHomotypicalGroup(state, homotypicGroup, taxon);
        for (Synonym syn : taxon.getSynonymsInGroup(homotypicGroup)) {
            handleSynonym(state, syn);
        }

        List<HomotypicalGroup> heterotypicHomotypicGroups = taxon.getHeterotypicSynonymyGroups();
        for (HomotypicalGroup group: heterotypicHomotypicGroups){
            handleHomotypicalGroup(state, group, taxon);
            for (Synonym syn : taxon.getSynonymsInGroup(group)) {
                handleSynonym(state, syn);
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

    private String toIsoDate(TimePeriod mediaCreated) {
        //TODO 2 date, what if end or freetext exist?
        Partial partial = mediaCreated.getStart();
        if (partial == null || !partial.isSupported(DateTimeFieldType.year())
                || !partial.isSupported(DateTimeFieldType.monthOfYear()) && partial.isSupported(DateTimeFieldType.dayOfMonth())) {
            //TODO 2 date, log warning, also if mediaCreated.getEnd() != null or so
            return null;
        } else {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendYear(4, 4).appendLiteral('-')
                    .appendMonthOfYear(2).appendLiteral('-')
                    .appendDayOfMonth(2)
                    .toFormatter();
            return partial.toString(formatter);
        }
    }

    /**
     * transforms the given date to an iso date
     */
    protected String toIsoDate(DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendYear(4, 4).appendLiteral('-')
                .appendMonthOfYear(2).appendLiteral('-')
                .appendDayOfMonth(2)
                .toFormatter();
        return formatter.print(dateTime);
    }

    private String getRemarks(AnnotatableEntity entity) {
        String remarks = null;
        for (Annotation a : entity.getAnnotations()) {
            //TODO 3 handle other annotation types
            if (AnnotationType.EDITORIAL().equals(a.getAnnotationType())
                    && CdmUtils.isNotBlank(a.getText())){
                remarks = CdmUtils.concat(";", remarks, a.getText());
            }
        }
        return remarks;
    }

//    private void handleMetaData(ColDpExportState state) {
//        ColDpExportTable table = ColDpExportTable.METADATA;
//        String[] csvLine = new String[table.getSize()];
////        csvLine[table.getIndex(CdmLightExportTable.INSTANCE_ID)] = state.getConfig().getInctanceId();
////        csvLine[table.getIndex(CdmLightExportTable.INSTANCE_NAME)] = state.getConfig().getInstanceName();
//        csvLine[table.getIndex(ColDpExportTable.DATASET_BASE_URL)] = state.getConfig().getBase_url();
//        csvLine[table.getIndex(ColDpExportTable.DATASET_CONTRIBUTOR)] = state.getConfig().getContributor();
//        csvLine[table.getIndex(ColDpExportTable.DATASET_CREATOR)] = state.getConfig().getCreator();
//        csvLine[table.getIndex(ColDpExportTable.DATASET_DESCRIPTION)] = state.getConfig().getDescription();
//        csvLine[table.getIndex(ColDpExportTable.DATASET_DOWNLOAD_LINK)] = state.getConfig().getDataset_download_link();
//        csvLine[table.getIndex(ColDpExportTable.DATASET_KEYWORDS)] = state.getConfig().getKeywords();
//        csvLine[table.getIndex(ColDpExportTable.DATASET_LANDINGPAGE)] = state.getConfig().getDataSet_landing_page();
//
//        csvLine[table.getIndex(ColDpExportTable.DATASET_LANGUAGE)] = state.getConfig().getLanguage() != null? state.getConfig().getLanguage().getLabel(): null;
//        csvLine[table.getIndex(ColDpExportTable.DATASET_LICENCE)] = state.getConfig().getLicence();
//        csvLine[table.getIndex(ColDpExportTable.DATASET_LOCATION)] = state.getConfig().getLocation();
//        csvLine[table.getIndex(ColDpExportTable.DATASET_RECOMMENDED_CITATTION)] = state.getConfig().getRecommended_citation();
//        csvLine[table.getIndex(ColDpExportTable.DATASET_TITLE)] = state.getConfig().getTitle();
//        state.getProcessor().put(table, "", csvLine);
//    }

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
            if (ann.getAnnotationType() == null || !ann.getAnnotationType().equals(AnnotationType.TECHNICAL())) {
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

    private void handleSource(WfoBackboneExportState state, DescriptionElementBase element,
            WfoBackboneExportTable factsTable) {
//        ColDpExportTable table = ColDpExportTable.FACT_SOURCES;
//        try {
//            Set<DescriptionElementSource> sources = element.getSources();
//
//            for (DescriptionElementSource source : sources) {
//                if (!(source.getType().equals(OriginalSourceType.Import)
//                        && state.getConfig().isExcludeImportSources())) {
//                    String[] csvLine = new String[table.getSize()];
//                    Reference ref = source.getCitation();
//                    if ((ref == null) && (source.getNameUsedInSource() == null)) {
//                        continue;
//                    }
//                    if (ref != null) {
//                        if (!state.getReferenceStore().contains(ref.getUuid())) {
//                            handleReference(state, ref);
//
//                        }
//                        csvLine[table.getIndex(ColDpExportTable.REFERENCE_FK)] = getId(state, ref);
//                    }
//                    csvLine[table.getIndex(ColDpExportTable.FACT_FK)] = getId(state, element);
//
//                    csvLine[table.getIndex(ColDpExportTable.NAME_IN_SOURCE_FK)] = getId(state,
//                            source.getNameUsedInSource());
//                    csvLine[table.getIndex(ColDpExportTable.FACT_TYPE)] = factsTable.getTableName();
//                    if (StringUtils.isBlank(csvLine[table.getIndex(ColDpExportTable.REFERENCE_FK)])
//                            && StringUtils.isBlank(csvLine[table.getIndex(ColDpExportTable.NAME_IN_SOURCE_FK)])) {
//                        continue;
//                    }
//                    state.getProcessor().put(table, source, csvLine);
//                }
//            }
//        } catch (Exception e) {
//            state.getResult().addException(e, "An unexpected error occurred when handling single source "
//                    + cdmBaseStr(element) + ": " + e.getMessage());
//        }
    }

    private String getTitleCache(IIdentifiableEntity identEntity) {
        if (identEntity == null) {
            return "";
        }
        // TODO 3 titleCache refresh?
        return identEntity.getTitleCache();
    }

    private String getId(WfoBackboneExportState state, ICdmBase cdmBase) {
        if (cdmBase == null) {
            return "";
        }
        // TODO 4 id type, make configurable
        return cdmBase.getUuid().toString();
    }

    private void handleSynonym(WfoBackboneExportState state, Synonym synonym) {
        try {
            if (isUnpublished(state.getConfig(), synonym)) {
                return;
            }

            WfoBackboneExportTable table = WfoBackboneExportTable.CLASSIFICATION;
            String[] csvLine = new String[table.getSize()];

            TaxonName name = synonym.getName();
            handleName(state, table, csvLine, name);

            //accepted name id
            if (synonym.getAcceptedTaxon()!= null && synonym.getAcceptedTaxon().getName() != null) {
                TaxonName acceptedName = synonym.getAcceptedTaxon().getName();
                String acceptedWfoId = getWfoId(state, acceptedName, false);
                if (acceptedWfoId == null) {
                    String message = "WFO-ID for accepted name is missing. This should not happen. Synonym: " + name.getTitleCache() + "; Accepted name: " + acceptedName.getTitleCache();
                    state.getResult().addError(message, "handleName");
                    state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
                }
                csvLine[table.getIndex(WfoBackboneExportTable.TAX_ACCEPTED_NAME_ID)] = acceptedWfoId;
            }

            state.getProcessor().put(table, synonym, csvLine);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling synonym "
                    + cdmBaseStr(synonym) + ": " + e.getMessage());
        }
    }

    private String handleName(WfoBackboneExportState state, WfoBackboneExportTable table, String[] csvLine,
            TaxonName name) {

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

            Rank rank = name.getRank();
            state.getNameStore().put(name.getId(), name.getUuid());

            //taxon ID
            wfoId = getWfoId(state, name, true);
            if (isBlank(wfoId)) {
                String message = "No WFO-ID given for taxon " + name.getTitleCache() + ". Taxon ignored";
                state.getResult().addError(message);
                state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
                return null;
            }else {
                csvLine[table.getIndex(WfoBackboneExportTable.TAXON_ID)] = wfoId;
            }

            //TODO 9 add IPNI ID if exists, scientific name ID
            csvLine[table.getIndex(WfoBackboneExportTable.NAME_SCIENTIFIC_NAME_ID)] = null;

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
            String rankStr = state.getTransformer().getCacheByRank(rank);
            if (rankStr == null) {
                String message = rank == null ? "No rank" : ("Rank not supported by WFO:" + rank.getLabel())
                        + "Taxon not handled in export: " + name.getTitleCache();
                state.getResult().addWarning(message);  //TODO 2 warning sufficient for missing rank? + location
                return wfoId;
            }
            csvLine[table.getIndex(WfoBackboneExportTable.RANK)] = rankStr;

            //authorship
            //TODO 3 handle empty authorship cache warning
            csvLine[table.getIndex(WfoBackboneExportTable.NAME_AUTHORSHIP)] = name.getAuthorshipCache();

            //family (use familystr if provided, otherwise try to compute from the family taxon
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

            //TODO 3 verbatimTaxonRank, is this needed at all?
            csvLine[table.getIndex(WfoBackboneExportTable.NAME_VERBATIM_RANK)] = rankStr;

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
                    //TODO 1 handle basionym is in file assertion
                }
                String basionymId = getWfoId(state, originalName, false);
                csvLine[table.getIndex(WfoBackboneExportTable.NAME_ORIGINAL_NAME_ID)] = basionymId;
            }

            //TODO 2 created
            csvLine[table.getIndex(WfoBackboneExportTable.CREATED)] = null;

            //TODO 2 modified
            csvLine[table.getIndex(WfoBackboneExportTable.MODIFIED)] = null;

            //TODO 1 URL to taxon
            csvLine[table.getIndex(WfoBackboneExportTable.REFERENCES)] = null;

            //TODO 3 excluded info
            csvLine[table.getIndex(WfoBackboneExportTable.EXCLUDE)] = null;

            //TODO 1 related names like orth. var., original spelling,

        } catch (Exception e) {
            state.getResult().addException(e,
                    "An unexpected error occurred when handling the name " + cdmBaseStr(name) + ": " + name.getTitleCache() + ": " + e.getMessage());

            e.printStackTrace();
        }

        return wfoId;
    }

    private String getWfoId(WfoBackboneExportState state, TaxonName name, boolean warnIfNotExists) {
        Identifier wfoId = name.getIdentifier(IdentifierType.uuidWfoNameIdentifier);
        if (wfoId == null && warnIfNotExists) {
            String message = "No wfo-id given for name: " + name.getTitleCache()+"/"+ name.getUuid();
            state.getResult().addWarning(message);  //TODO 5 data location
        }
        return wfoId == null ? null : wfoId.getIdentifier();
    }

    private String makeNameStatus(WfoBackboneExportState state, TaxonName name) {
        try {

            //TODO 1 what is with dubium
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
                //TODO 2 handle original spellings for name status
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

    private void handleReference(WfoBackboneExportState state, Reference reference) {
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

            //TODO 1 uri (doi, uri or ext_link
//            csvLine[table.getIndex(WfoBackboneExportTable.REF_DOI)] = reference.getDoiString();
//
//            //TODO 2 reference link link (=> external link)
////            csvLine[table.getIndex(ColDpExportTable.LINK)] = null;
//            if (reference.getUri() != null) {
//                csvLine[table.getIndex(WfoBackboneExportTable.LINK)] = reference.getUri().toString();
//            }

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