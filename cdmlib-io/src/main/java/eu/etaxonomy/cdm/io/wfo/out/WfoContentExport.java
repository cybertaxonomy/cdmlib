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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.format.description.CategoricalDataFormatter;
import eu.etaxonomy.cdm.format.reference.NomenclaturalSourceFormatter;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ExportResult.ExportResultState;
import eu.etaxonomy.cdm.io.common.TaxonNodeOutStreamPartitioner;
import eu.etaxonomy.cdm.io.common.XmlExportState;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
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
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
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
 * Classification or taxon tree exporter into WFO Content format.
 *
 * @author a.mueller
 * @since 2024-01-30
 */
@Component
public class WfoContentExport
        extends CdmExportBase<WfoContentExportConfigurator,WfoContentExportState,IExportTransformer,File>{

    private static final long serialVersionUID = -4560488499411723333L;

    public WfoContentExport() {
        this.ioName = this.getClass().getSimpleName();
    }

    @Override
    public long countSteps(WfoContentExportState state) {
        TaxonNodeFilter filter = state.getConfig().getTaxonNodeFilter();
        return getTaxonNodeService().count(filter);
    }

    @Override
    protected void doInvoke(WfoContentExportState state) {

        try {
            IProgressMonitor monitor = state.getConfig().getProgressMonitor();
            WfoContentExportConfigurator config = state.getConfig();

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

    private void handleTaxonNode(WfoContentExportState state, TaxonNode taxonNode) {

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
                    state.putTaxonNodeWfoId(taxonNode, parentWfoId);
                }
            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when handling taxonNode "
                        + taxonNode.getUuid() + ": " + e.getMessage() + e.getStackTrace());
            }
        }
    }

    private String getParentWfoId(WfoContentExportState state, TaxonNode taxonNode) {
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
    private boolean filterTaxon(WfoContentExportState state, TaxonNode taxonNode) {
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
                allowedRanks.add(Rank.SUBSECTION_BOTANY());
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
    private String handleTaxon(WfoContentExportState state, TaxonNode taxonNode, String parentWfoId) {

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
            WfoContentExportTable table = WfoContentExportTable.CLASSIFICATION;
            String[] csvLine = new String[table.getSize()];

            //accepted name
            TaxonName name = taxon.getName();
            wfoId = handleName(state, table, csvLine, name);

            //... parentNameUsageID
            csvLine[table.getIndex(WfoContentExportTable.TAX_PARENT_ID)] = parentWfoId;

            handleDescriptions(state, taxon);

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

    private void handleDescriptions(WfoContentExportState state, Taxon taxon) {

        //filtered descriptions
        Set<TaxonDescription> descriptions = taxon.getDescriptions().stream().filter(d->d.isPublish()).collect(Collectors.toSet());
        Stream<DescriptionElementBase> debStream = descriptions.stream().flatMap(d->d.getElements().stream());

        SetMap<Feature,DescriptionElementBase> feature2DescriptionsMap = new SetMap<>();
        debStream.forEach(deb->feature2DescriptionsMap.putItem(deb.getFeature(), deb));

        feature2DescriptionsMap.entrySet().stream().forEach(e->{
            Feature feature = e.getKey();
            e.getValue().forEach(deb->{
                deb = CdmBase.deproxy(deb);
                if (Feature.uuidDistribution.equals(feature.getUuid()) && deb.getClass().equals(Distribution.class)) {
                    handleDistribution(state, (Distribution)e.getValue(), taxon);
                }else if (Feature.uuidCommonName.equals(feature.getUuid()) && deb.getClass().equals(CommonTaxonName.class)){
                    handleCommonName(state, (CommonTaxonName)e.getValue(), taxon);
                }else if (Feature.uuidImage.equals(feature.getUuid())) {
                    //TODO 2 handle media
                }else if (Feature.uuidHabitat.equals(feature.getUuid())) {
                    handleMeasurementOrFact(state, "http://kew.org/wcs/terms/habitat", deb, taxon);
                }else if (Feature.uuidLifeform.equals(feature.getUuid())) {
                    handleMeasurementOrFact(state, "http://kew.org/wcs/terms/lifeform", deb, taxon);
                }else if (Feature.uuidIucnStatus.equals(feature.getUuid())) {
                    handleMeasurementOrFact(state, "http://kew.org/wcs/terms/hreatStatus", deb, taxon);
                }else {
                    //general description
                    handleDescription(state, deb, taxon);
                }
            });
        });
    }

    private void handleDescription(WfoContentExportState state, DescriptionElementBase deb, Taxon taxon) {

        try {
            WfoContentExportTable table = WfoContentExportTable.DESCRIPTION;
            //TODO i18n
            List<Language> languages = new ArrayList<>();
            languages.add(Language.ENGLISH());
            languages.add(Language.FRENCH());
            languages.add(Language.SPANISH_CASTILIAN());
            languages.add(Language.GERMAN());

            String[] csvLine = new String[table.getSize()];

            //TODO 3 description types still need fine-tuning
            String type = getDescriptionTpe(state, deb);
            if (type == null) {
                return;
            }

            //description
            String text = null;
            if (deb instanceof TextData) {
                TextData td = (TextData)deb;

                //TODO i18n
                LanguageString ls = td.getPreferredLanguageString(languages, INCLUDE_UNPUBLISHED);
                if (ls != null) {
                    text = ls.getText();
                    //language TODO
                }
            } else if (deb instanceof CategoricalData) {
//            DefaultCategoricalDescriptionBuilder builder = new DefaultCategoricalDescriptionBuilder();
//            text = builder.build((CategoricalData)deb, languages);
                //TODO which formatter to use
                CategoricalDataFormatter formatter = CategoricalDataFormatter.NewInstance(null);
                text = formatter.format(deb);
            } else {
                //TODO other types or only message?
            }
            csvLine[table.getIndex(WfoContentExportTable.DESC_DESCRIPTION)] = text;

            //audience TODO
            csvLine[table.getIndex(WfoContentExportTable.AUDIENCE)] = null;

            //rights holder
            handleRightsHolder(state, deb, csvLine, table, taxon);

            //created TODO
            handleCreated(state, deb, csvLine, table, taxon);

            //creator
            handleCreator(state, deb, csvLine, table, taxon);

            //source
            handleSource(state, deb, table);

            //rights
            handleRights(state, null, csvLine, table, taxon);

            //license
            handleLicense(state, null, csvLine, table, taxon);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling single description "
                    + cdmBaseStr(deb) + ": " + e.getMessage());
        }
    }

    private String getDescriptionTpe(WfoContentExportState state, DescriptionElementBase deb) {
        try {
            return state.getTransformer().getCacheByFeature(deb.getFeature());
        } catch (UndefinedTransformerMethodException e) {
//            e.printStackTrace();
            return null; //should not happen
        }
    }

    private void handleCreator(WfoContentExportState state, DescriptionElementBase deb, String[] csvLine,
            WfoContentExportTable table, Taxon taxon) {
        // TODO Auto-generated method stub
    }

    private void handleCreated(WfoContentExportState state, DescriptionElementBase deb, String[] csvLine,
            WfoContentExportTable table, Taxon taxon) {
        // TODO created
    }

    private void handleRightsHolder(WfoContentExportState state, DescriptionElementBase deb,
            String[] csvLine, WfoContentExportTable table, Taxon taxon) {
        // TODO rightsholder
        csvLine[table.getIndex(WfoContentExportTable.RIGHTS_HOLDER)] = null;

    }

    private void handleMeasurementOrFact(WfoContentExportState state, String string, DescriptionElementBase deb,
            Taxon taxon) {

    }

    private void handleCommonName(WfoContentExportState state, CommonTaxonName commonName, Taxon taxon) {
        WfoContentExportTable table = WfoContentExportTable.VERNACULAR_NAME;
        //TODO i18n
        List<Language> languages = null;
        try {
            if (commonName instanceof CommonTaxonName) {
                String[] csvLine = new String[table.getSize()];
//                Distribution distribution = (Distribution) element;
//                distributions.add(distribution);

                csvLine[table.getIndex(WfoContentExportTable.CN_VERNACULAR_NAME)] = commonName.getName();

                //language
                if (commonName.getLanguage() != null) {
                    csvLine[table.getIndex(WfoContentExportTable.CN_VERNACULAR_NAME)] = commonName.getLanguage().getPreferredLabel(languages);
                }

                //countryCode
                NamedArea area = commonName.getArea();
                if (area != null && area.getVocabulary() != null && area.getVocabulary().getUuid().equals(NamedArea.uuidCountryVocabulary)) {
                    String countryCode = ((Country)area).getIso3166_A2();
                    csvLine[table.getIndex(WfoContentExportTable.CN_COUNTRY_CODE)] = countryCode;
                }

                //rights
                handleRights(state, commonName, csvLine, table, taxon);

                //license
                handleLicense(state, commonName, csvLine, table, taxon);

                //source
                handleSource(state, commonName, table);

                state.getProcessor().put(table, commonName, csvLine);
            } else {
                //TODO 1 is this handled elsewhere?
                state.getResult()
                        .addError("The common name for the taxon " + taxon.getUuid()
                                + " is not of type CommonTaxonName. Could not be exported. UUID of the common name: "
                                + commonName.getUuid());
            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling single common name "
                    + cdmBaseStr(commonName) + ": " + e.getMessage());
        }
    }

    private void handleLicense(WfoContentExportState state, CommonTaxonName commonName,
            String[] csvLine, WfoContentExportTable table, Taxon taxon) {
        // TODO 1 handle License

    }

    private void handleRights(WfoContentExportState state, CommonTaxonName commonName,
            String[] csvLine, WfoContentExportTable table, Taxon taxon) {
        // TODO 1 handle rights
    }

    private void handleDistribution(WfoContentExportState state, Distribution distribution, Taxon taxon) {
        WfoContentExportTable table = WfoContentExportTable.DISTRIBUTION;
        //TODO i18n
        List<Language> languages = null;
            try {
                if (distribution instanceof Distribution) {
                    String[] csvLine = new String[table.getSize()];
//                    Distribution distribution = (Distribution) element;
//                    distributions.add(distribution);
                    NamedArea area = distribution.getArea();

                    csvLine[table.getIndex(WfoContentExportTable.DIST_LOCALITY)] = area.getPreferredLabel(languages);

                    //TDWG area
                    if (area.getVocabulary() !=null && area.getVocabulary().getUuid().equals(NamedArea.uuidTdwgAreaVocabulary)) {
                        String tdwgCode = area.getIdInVocabulary();
                        csvLine[table.getIndex(WfoContentExportTable.DIST_LOCATION_ID)] = tdwgCode;
                    }

                    //countryCode
                    if (area.getVocabulary() !=null && area.getVocabulary().getUuid().equals(NamedArea.uuidCountryVocabulary)) {
                        String countryCode = ((Country)area).getIso3166_A2();
                        csvLine[table.getIndex(WfoContentExportTable.DIST_COUNTRY_CODE)] = countryCode;
                    }

                    if (distribution.getStatus() != null) {
                        PresenceAbsenceTerm status = distribution.getStatus();
                        csvLine[table.getIndex(WfoContentExportTable.DIST_ESTABLISHMENT_MEANS)] = status.getPreferredLabel(languages);
                    }
                    //source
                    handleSource(state, distribution, table);

                    //occurrencRemarks
                    //TODO 5 occurrence remarks correct?
                    csvLine[table.getIndex(WfoContentExportTable.DIST_OCCURRENCE_REMARKS)] = createAnnotationsString(distribution.getAnnotations());

                    state.getProcessor().put(table, distribution, csvLine);
                } else {
                    //TODO 1 is this handled elsewhere?
                    state.getResult()
                            .addError("The distribution description for the taxon " + taxon.getUuid()
                                    + " is not of type distribution. Could not be exported. UUID of the description element: "
                                    + distribution.getUuid());
                }
            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when handling single distribution "
                        + cdmBaseStr(distribution) + ": " + e.getMessage());
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

    private void handleSource(WfoContentExportState state, DescriptionElementBase element,
            WfoContentExportTable factsTable) {
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

    private String getId(WfoContentExportState state, ICdmBase cdmBase) {
        if (cdmBase == null) {
            return "";
        }
        // TODO 4 id type, make configurable
        return cdmBase.getUuid().toString();
    }

    private String handleName(WfoContentExportState state, WfoContentExportTable table, String[] csvLine,
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

            state.getNameStore().put(name.getId(), name.getUuid());

            //taxonID
            wfoId = getWfoId(state, name, true);
            if (isBlank(wfoId)) {
                String message = "No WFO-ID given for taxon " + name.getTitleCache() + ". Taxon ignored";
                state.getResult().addError(message);
                state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
                return null;
            }else {
                csvLine[table.getIndex(WfoContentExportTable.TAXON_ID)] = wfoId;
            }

            //scientificNameID
            //TODO 9 add IPNI ID if exists
            boolean warnIfNotExists = false;
            csvLine[table.getIndex(WfoContentExportTable.NAME_SCIENTIFIC_NAME_ID)] = getIpniId(state, name, warnIfNotExists);

            //scientificName
            if (name.isProtectedTitleCache()) {
                //TODO 7 make it configurable if we should always take titleCache if titleCache is protected, as nameCache may not necessarily
                //     have complete data if titleCache is protected as it is considered to be irrelevant or at least preliminary
                String message = "";
                if (StringUtils.isNotEmpty(name.getNameCache())) {
                    csvLine[table.getIndex(WfoContentExportTable.NAME_SCIENTIFIC_NAME)] = name.getNameCache();
                    message = "ScientificName: Name cache " + name.getNameCache() + " used for name with protected titleCache " +  name.getTitleCache();
                }else {
                    csvLine[table.getIndex(WfoContentExportTable.NAME_SCIENTIFIC_NAME)] = name.getTitleCache();
                    message = "ScientificName: Name has protected titleCache and no explicit nameCache: " +  name.getTitleCache();
                }
                state.getResult().addWarning(message);  //TODO 7 add location to warning
            } else {
                csvLine[table.getIndex(WfoContentExportTable.NAME_SCIENTIFIC_NAME)] = name.getNameCache();
            }

            //rank
            Rank rank = name.getRank();
            String rankStr = state.getTransformer().getCacheByRank(rank);
            if (rankStr == null) {
                String message = rank == null ? "No rank" : ("Rank not supported by WFO:" + rank.getLabel())
                        + "Taxon not handled in export: " + name.getTitleCache();
                state.getResult().addWarning(message);  //TODO 2 warning sufficient for missing rank? + location
                return wfoId;
            }
            csvLine[table.getIndex(WfoContentExportTable.RANK)] = rankStr;

            //scientificNameAuthorship
            //TODO 3 handle empty authorship cache warning
            csvLine[table.getIndex(WfoContentExportTable.NAME_AUTHORSHIP)] = name.getAuthorshipCache();

            //family
            //TODO 2 family handling
            csvLine[table.getIndex(WfoContentExportTable.TAX_FAMILY)] = state.getFamilyStr();

            //name parts
            csvLine[table.getIndex(WfoContentExportTable.TAX_GENUS)] = name.isSupraGeneric()? null : name.getGenusOrUninomial();
            csvLine[table.getIndex(WfoContentExportTable.NAME_SPECIFIC_EPITHET)] = name.getSpecificEpithet();
            csvLine[table.getIndex(WfoContentExportTable.NAME_INFRASPECIFIC_EPITHET)] = name.getInfraSpecificEpithet();

            //nom. ref
            String nomRef = NomenclaturalSourceFormatter.INSTANCE().format(name.getNomenclaturalSource());
            csvLine[table.getIndex(WfoContentExportTable.NAME_PUBLISHED_IN)] = nomRef;

        } catch (Exception e) {
            state.getResult().addException(e,
                    "An unexpected error occurred when handling the name " + cdmBaseStr(name) + ": " + name.getTitleCache() + ": " + e.getMessage());

            e.printStackTrace();
        }

        return wfoId;
    }

    private String getIpniId(WfoContentExportState state, TaxonName name, boolean warnIfNotExists) {
        Identifier ipniId = name.getIdentifier(IdentifierType.uuidIpniNameIdentifier);
        if (ipniId == null && warnIfNotExists) {
            String message = "No ipni-id given for name: " + name.getTitleCache()+"/"+ name.getUuid();
            state.getResult().addWarning(message);  //TODO 5 data location
        }
        return ipniId == null ? null : ipniId.getIdentifier();
    }

    private String getWfoId(WfoContentExportState state, TaxonName name, boolean warnIfNotExists) {
        Identifier wfoId = name.getIdentifier(IdentifierType.uuidWfoNameIdentifier);
        if (wfoId == null && warnIfNotExists) {
            String message = "No wfo-id given for name: " + name.getTitleCache()+"/"+ name.getUuid();
            state.getResult().addWarning(message);  //TODO 5 data location
        }
        return wfoId == null ? null : wfoId.getIdentifier();
    }

    private String makeNameStatus(WfoContentExportState state, TaxonName name) {
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

    private void handleHomotypicalGroup(WfoContentExportState state, HomotypicalGroup group, Taxon acceptedTaxon) {
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

    private void handleReference(WfoContentExportState state, Reference reference) {
        try {
            if (state.getReferenceStore().contains(reference.getUuid())) {
                return;
            }
            reference = CdmBase.deproxy(reference);

            state.addReferenceToStore(reference);
            WfoContentExportTable table = WfoContentExportTable.REFERENCE;
            String[] csvLine = new String[table.getSize()];

            csvLine[table.getIndex(WfoContentExportTable.IDENTIFIER)] = getId(state, reference);

            //TODO 2 correct?, ref biblio citation
            csvLine[table.getIndex(WfoContentExportTable.REF_BIBLIO_CITATION)] = reference.getCitation();

            //TODO 1 uri (doi, uri or ext_link
//            csvLine[table.getIndex(WfoContentExportTable.REF_DOI)] = reference.getDoiString();
//
//            //TODO 2 reference link link (=> external link)
////            csvLine[table.getIndex(ColDpExportTable.LINK)] = null;
//            if (reference.getUri() != null) {
//                csvLine[table.getIndex(WfoContentExportTable.LINK)] = reference.getUri().toString();
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
    protected boolean doCheck(WfoContentExportState state) {
        return false;
    }

    @Override
    protected boolean isIgnore(WfoContentExportState state) {
        return false;
    }
}