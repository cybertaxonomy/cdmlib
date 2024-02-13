/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.coldp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.name.TypeDesignationSetComparator;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationSetContainer;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.format.reference.OriginalSourceFormatter;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.cdmLight.OrderHelper;
import eu.etaxonomy.cdm.io.coldp.ColDpExportTransformer.ColDpNameRelType;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ExportResult.ExportResultState;
import eu.etaxonomy.cdm.io.common.TaxonNodeOutStreamPartitioner;
import eu.etaxonomy.cdm.io.common.XmlExportState;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.media.ExternalLink;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TextualTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.reference.IOriginalSource;
import eu.etaxonomy.cdm.model.reference.ISourceable;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.IdentifierType;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDtoByRankAndNameComparator;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy;

/**
 * Classification or taxon tree exporter into COL-DP format.
 * @author a.mueller
 * @since 2023-07-17
 */
@Component
public class ColDpClassificationExport
        extends CdmExportBase<ColDpExportConfigurator,ColDpExportState,IExportTransformer,File>{

    private static final long serialVersionUID = 4288364478648729869L;

    public ColDpClassificationExport() {
        this.ioName = this.getClass().getSimpleName();
    }

    @Override
    public long countSteps(ColDpExportState state) {
        TaxonNodeFilter filter = state.getConfig().getTaxonNodeFilter();
        return getTaxonNodeService().count(filter);
    }

    @Override
    protected void doInvoke(ColDpExportState state) {

        try {
            IProgressMonitor monitor = state.getConfig().getProgressMonitor();
            ColDpExportConfigurator config = state.getConfig();

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

//            handleMetaData(state);  //FIXME metadata;
            monitor.subTask("Start partitioning");

            TaxonNode node = partitioner.next();
            while (node != null) {
                handleTaxonNode(state, node);
                node = partitioner.next();
            }

            // get rootNode and create helperObjects
            if (state.getRootId() != null) {
                List<TaxonNodeDto> childrenOfRoot = state.getNodeChildrenMap().get(state.getRootId());

                Comparator<TaxonNodeDto> comp = state.getConfig().getTaxonNodeComparator();
                //FIXME comparator
                if (comp == null) {
                    comp = new TaxonNodeDtoByRankAndNameComparator();
                }
                if (childrenOfRoot != null) {
                    Collections.sort(childrenOfRoot, comp);
                    OrderHelper helper = new OrderHelper(state.getRootId());
                    helper.setOrderIndex(state.getActualOrderIndexAndUpdate());
                    state.getOrderHelperMap().put(state.getRootId(), helper);

                    for (TaxonNodeDto child : childrenOfRoot) {
                        OrderHelper childHelper = new OrderHelper(child.getTaxonUuid());
                        helper.addChild(childHelper);
                        childHelper.setOrderIndex(state.getActualOrderIndexAndUpdate());
                        childHelper.addChildren(
                                createOrderHelper(state.getNodeChildrenMap().get(child.getUuid()), state));
                    }
                }

                state.getNodeChildrenMap().clear();
                for (OrderHelper order : state.getOrderHelperMap().values()) {
                    setOrderIndex(state, order);
                }
            }

            state.getProcessor().createFinalResult(state);
        } catch (Exception e) {
            state.getResult().addException(e,
                    "An unexpected error occurred in main method doInvoke() " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setOrderIndex(ColDpExportState state, OrderHelper order) {

        if (order.getTaxonUuid() != null
                && state.getProcessor().hasRecord(ColDpExportTable.TAXON, order.getTaxonUuid().toString())) {
            String[] csvLine = state.getProcessor().getRecord(ColDpExportTable.TAXON,
                    order.getTaxonUuid().toString());
            csvLine[ColDpExportTable.TAXON.getIndex(ColDpExportTable.TAX_SEQ_INDEX)] = String
                    .valueOf(order.getOrderIndex());
        }

        if (order.getChildren() == null) {
            return;
        }
        for (OrderHelper helper : order.getChildren()) {
            setOrderIndex(state, helper);
        }
    }

    private List<OrderHelper> createOrderHelper(List<TaxonNodeDto> nodes, ColDpExportState state) {
        List<TaxonNodeDto> children = nodes;
        // alreadySortedNodes.add(parentUuid);
        if (children == null) {
            return null;
        }
        Comparator<TaxonNodeDto> comp = state.getConfig().getTaxonNodeComparator();
        if (comp == null) {
            comp = new TaxonNodeDtoByRankAndNameComparator();
        }
        Collections.sort(children, comp);
        // TODO 3 taxon ordering: nochmal checken!!! - s.auch seq index
        OrderHelper helperChild;
        List<OrderHelper> childrenHelper = new ArrayList<>();
        for (TaxonNodeDto child : children) {
            helperChild = new OrderHelper(child.getTaxonUuid());
            helperChild.setOrderIndex(state.getActualOrderIndexAndUpdate());

            if (state.getNodeChildrenMap().get(child.getUuid()) != null) {
                children = state.getNodeChildrenMap().get(child.getUuid());
                helperChild.addChildren(createOrderHelper(children, state));
            }
            childrenHelper.add(helperChild);
        }
        return childrenHelper;
    }

    private void handleTaxonNode(ColDpExportState state, TaxonNode taxonNode) {

        if (taxonNode == null) {
            String message = "TaxonNode for given taxon node UUID not found. ";
            // TODO 3 taxon node not found
            state.getResult().addError(message);
        } else {
            try {
                TaxonNode root = taxonNode;
                List<TaxonNodeDto> childNodes;
                if (root.hasChildNodes()) {
                    childNodes = new ArrayList<>();
                    for (TaxonNode child : root.getChildNodes()) {
                    	if (child != null) {
                    		childNodes.add(new TaxonNodeDto(child));
                    	}
                    }
                    state.getNodeChildrenMap().put(root.getUuid(), childNodes);

                    // add root to node map
                }
                TaxonNodeDto rootDto = new TaxonNodeDto(root);
                UUID parentUuid = root.getParent() != null ? root.getParent().getUuid()
                        : state.getClassificationUUID(root);
                List<TaxonNodeDto> children = state.getNodeChildrenMap().get(parentUuid);
                if (children != null && !children.contains(rootDto)) {
                    state.getNodeChildrenMap().get(parentUuid).add(rootDto);
                } else if (state.getNodeChildrenMap().get(parentUuid) == null) {
                    List<TaxonNodeDto> rootList = new ArrayList<>();
                    rootList.add(rootDto);
                    state.getNodeChildrenMap().put(parentUuid, rootList);
                }
                if (root.hasTaxon()) {
                    handleTaxon(state, root);
                }
            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when handling taxonNode "
                        + taxonNode.getUuid() + ": " + e.getMessage() + e.getStackTrace());
            }
        }
    }

    private void handleTaxon(ColDpExportState state, TaxonNode taxonNode) {
        //check null
        if (taxonNode == null) {
            state.getResult().addError("The taxonNode was null.", "handleTaxon");
            state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
            return;
        }
        //check no taxon
        if (taxonNode.getTaxon() == null) {
            state.getResult().addError("There was a taxon node without a taxon: " + taxonNode.getUuid(),
                    "handleTaxon");
            state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
            return;
        }

        try {

            //handle taxon
            Taxon taxon = CdmBase.deproxy(taxonNode.getTaxon());
            try {
                //accepted name
                TaxonName name = taxon.getName();
                handleName(state, name, taxon, true);

                if (state.getConfig().isDoSynonyms()) {
                    //homotypic group / synonyms
                    HomotypicalGroup homotypicGroup = taxon.getHomotypicGroup();
                    int index = 0;
                    handleHomotypicalGroup(state, homotypicGroup, taxon);
                    for (Synonym syn : taxon.getSynonymsInGroup(homotypicGroup)) {
                        handleSynonym(state, syn, index);
                        index++;
                    }

                    List<HomotypicalGroup> heterotypicHomotypicGroups = taxon.getHeterotypicSynonymyGroups();
                    for (HomotypicalGroup group: heterotypicHomotypicGroups){
                        handleHomotypicalGroup(state, group, taxon);
                        for (Synonym syn : taxon.getSynonymsInGroup(group)) {
                            handleSynonym(state, syn, index);
                            index++;
                        }
                    }
                }

                //TODO 2 pro parte synonyms and misapplications
//                //pro parte synonyms
//                index = 0;
//                for (Taxon tax : taxon.getAllProParteSynonyms()) {
//                    handleProPartePartialMisapplied(state, tax, taxon, true, false, index);
//                    index++;
//                }
//
//                //misapplications
//                for (Taxon tax : taxon.getAllMisappliedNames()) {
//                    handleProPartePartialMisapplied(state, tax, taxon, false, true, index);
//                    index++;
//                }

                //taxon table
                ColDpExportTable table = ColDpExportTable.TAXON;
                String[] csvLine = new String[table.getSize()];

                csvLine[table.getIndex(ColDpExportTable.ID)] = getId(state, taxon);

                //alternative IDs
                handleAlternativeId(state, csvLine, table, taxon);

                //TODO 9 sourceID //handled in referenceID
                csvLine[table.getIndex(ColDpExportTable.SOURCE_ID)] = null;

                Taxon parent = (taxonNode.getParent() == null) ? null : taxonNode.getParent().getTaxon();
                csvLine[table.getIndex(ColDpExportTable.TAX_PARENT_ID)] = getId(state, parent);

                //TODO 5 seq index

                //TODO 9 branchLength
                csvLine[table.getIndex(ColDpExportTable.TAX_BRANCH_LENGTH)] = null;

                //nameID
                csvLine[table.getIndex(ColDpExportTable.TAX_NAME_ID)] = getId(state, name);

                //TODO 6 namePhrase
                csvLine[table.getIndex(ColDpExportTable.TAX_NAMEPHRASE)] = taxon.getAppendedPhrase();

                //secundum reference
                csvLine[table.getIndex(ColDpExportTable.TAX_SEC_ID)] = getId(state, taxon.getSec());
                if (taxon.getSec() != null
                        && (!state.getReferenceStore().contains((taxon.getSec().getUuid())))) {
                    handleReference(state, taxon.getSec());
                }

                //TODO 4 SCRUTINIZER
                //TODO 4 SCRUTINIZER ID
                //TODO 4 SCRUTINIZER Date

                //TODO 2 taxon provisional, still an open issue?
                csvLine[table.getIndex(ColDpExportTable.TAX_PROVISIONAL)] = taxonNode.isDoubtful() ? "1" : "0";

                //reference ID
                handleReference(state, csvLine, table, taxon);

                //TODO 3 taxon extinct

                //TODO 7 taxon temporalRangeStart
                csvLine[table.getIndex(ColDpExportTable.TAX_TEMPORAL_RANGE_END)] = null;
                //TODO 7 taxon temporalRangeEnd
                csvLine[table.getIndex(ColDpExportTable.TAX_TEMPORAL_RANGE_END)] = null;

                //TODO 5 taxon environment
                csvLine[table.getIndex(ColDpExportTable.TAX_ENVIRONMENT)] = null;

                //TODO 6 taxon species, section, subgenus, genus, ... - can be null if parent ID is given

                //remarks
                csvLine[table.getIndex(ColDpExportTable.REMARKS)] = getRemarks(taxon);

                //TODO 1 taxon only published

                //process taxon line
                state.getProcessor().put(table, taxon, csvLine);

                //descriptions
                handleDescriptions(state, taxon);

                //media
                handleMedia(state, taxon);

            } catch (Exception e) {
                e.printStackTrace();
                state.getResult().addException(e,
                        "An unexpected problem occurred when trying to export taxon with id " + taxon.getId() + " " + taxon.getTitleCache());
                state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling the taxon node of "
                    + cdmBaseStr(taxonNode.getTaxon()) + ", titleCache:"+ taxonNode.getTaxon().getTitleCache()+": " + e.getMessage());
        }
    }

    private void handleAlternativeId(ColDpExportState state, String[] csvLine, ColDpExportTable table, IdentifiableEntity<?> entity) {
        String alternativeIdStr = null;

        for (Identifier identifier : entity.getIdentifiers()) {
            //TODO 4 alternativeID filter Identifiers
            IdentifierType type = identifier.getType();
            String url = identifier.getUrl();
            if (isCuriType(type)){
                //TODO 4 alternativeID handle other identifier types, e.g. those providing an URI pattern
                alternativeIdStr = handleCuriTypes(alternativeIdStr, type, identifier);
            } else if (url != null && !url.isEmpty() && isUrl(url)) {
                alternativeIdStr = CdmUtils.concat(",", alternativeIdStr, url);
            }else {
                //TODO 4 alternativeID log failing identifier
            }
        }

        csvLine[table.getIndex(ColDpExportTable.ALTERNATIVE_ID)] = alternativeIdStr;
    }

    //TODO 4 curiType handle as Map<UUID,prefix>
    private boolean isCuriType(IdentifierType type) {
        return (type != null &&
                (type.equals(IdentifierType.IDENTIFIER_NAME_WFO())
                        || type.equals(IdentifierType.IDENTIFIER_NAME_IPNI())
                        || type.equals(IdentifierType.IDENTIFIER_NAME_TROPICOS())
                        || type.equals(IdentifierType.IDENTIFIER_NAME_IF())
                ));
    }

    /**
     * Handle CURI (compact URI) types - see https://github.com/CatalogueOfLife/coldp/blob/master/README.md#identifiers
     *
     * @param alternativeIdStr
     * @param type
     * @param identifier
     * @return
     */
    private String handleCuriTypes(String alternativeIdStr, IdentifierType type, Identifier identifier) {
        String prefix = null;
        String value = identifier.getIdentifier();
        if (type.equals(IdentifierType.IDENTIFIER_NAME_WFO())) {
            prefix = "wfo";
            //TODO 2 handle wfo- prefix (according to docu the below is correct, but probably unwanted
            value = value.replace("wfo-", "");
        } else if (type.equals(IdentifierType.IDENTIFIER_NAME_IPNI())) {
            prefix = "ipni";
        } else if (type.equals(IdentifierType.IDENTIFIER_NAME_TROPICOS())) {
            prefix = "tropicos";
        } else if (type.equals(IdentifierType.IDENTIFIER_NAME_IF())) {
            prefix = "if";
        }
        alternativeIdStr = CdmUtils.concat(",", alternativeIdStr, CdmUtils.concat(":", prefix, value));
        return alternativeIdStr;
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

    private void handleMedia(ColDpExportState state, Taxon taxon) {
        Set<Media> mediaSet = new HashSet<>();

        //TODO 3 media collect media form other places

        //collect from taxon image gallery
        Set<? extends DescriptionBase<?>> descriptions = taxon.getDescriptions();
        for (DescriptionBase<?> description : descriptions){
            for (DescriptionElementBase o : description.getElements()){
                DescriptionElementBase el = CdmBase.deproxy(o);
                if (el.getMedia().size() > 0){
                    for (Media media: el.getMedia()){
                        mediaSet.add(media);
                    }
                }
            }
        }

        //handle single media
        for (Media media : mediaSet) {
            for (MediaRepresentation repr : media.getRepresentations()){
                for (MediaRepresentationPart part : repr.getParts()){
                        handleMediaRepresentation(state, taxon, part);
                }
            }
        }
    }

    private void handleMediaRepresentation(ColDpExportState state, Taxon taxon, MediaRepresentationPart part) {
        if (part == null || state.getMediaStore().contains(part.getUuid())) {
            return;
        }

        state.addMediaToStore(part);
        ColDpExportTable table = ColDpExportTable.MEDIA;
        String[] csvLine = new String[table.getSize()];
        part = HibernateProxyHelper.deproxy(part);

        csvLine[table.getIndex(ColDpExportTable.TAXON_ID)] = getId(state, taxon);

        //TODO 9 media sourceID //handled in referenceID
        csvLine[table.getIndex(ColDpExportTable.SOURCE_ID)] = null;

        //url
        if (part.getUri() != null) {
            csvLine[table.getIndex(ColDpExportTable.MEDIA_URL)] = part.getUri().toString();
        }

        //TODO 3 media type, still open?
        csvLine[table.getIndex(ColDpExportTable.TYPE)] = part.getMediaRepresentation().getMimeType();

        //TODO 3 media format
        csvLine[table.getIndex(ColDpExportTable.MEDIA_FORMAT)] = null;

        Media media = part.getMediaRepresentation().getMedia();

        //title
        csvLine[table.getIndex(ColDpExportTable.MEDIA_TITLE)] = getTitleCache(media);

        //created
        csvLine[table.getIndex(ColDpExportTable.MEDIA_CREATED)] = toIsoDate(media.getMediaCreated());

        //creator  //TODO 3 media format creator
        csvLine[table.getIndex(ColDpExportTable.MEDIA_CREATOR)] = getTitleCache(media.getArtist());

        //TODO 5 media license
        csvLine[table.getIndex(ColDpExportTable.MEDIA_LICENSE)] = null;

        //TODO 5 media link
        csvLine[table.getIndex(ColDpExportTable.LINK)] = null;

        state.getProcessor().put(table, part, csvLine);
    }

    private String toIsoDate(TimePeriod mediaCreated) {
        if (mediaCreated == null) {
            return null;
        }
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

    private void handleDescriptions(ColDpExportState state, Taxon taxon) {
        if (!state.getConfig().isDoFactualData()) {
            return;
        }
        String titleCache = null;
        try {
            taxon = HibernateProxyHelper.deproxy(taxon);
            titleCache = taxon.getTitleCache();
            Set<TaxonDescription> descriptions = taxon.getDescriptions();
            List<DescriptionElementBase> distributionFacts = new ArrayList<>();
            List<DescriptionElementBase> taxonInteractionsFacts = new ArrayList<>();
            List<DescriptionElementBase> commonNameFacts = new ArrayList<>();
            for (TaxonDescription description : descriptions) {
                if (description.getElements() != null &&
                        description.isPublish() || state.getConfig().isIncludeUnpublishedFacts()){
                    for (DescriptionElementBase element : description.getElements()) {
                        element = CdmBase.deproxy(element);
                        if (element.getFeature().equals(Feature.COMMON_NAME())) {
                            commonNameFacts.add(element);
                        } else if (element.getFeature().equals(Feature.DISTRIBUTION())) {
                            distributionFacts.add(element);
                        } else if (element.getFeature().isSupportsTaxonInteraction()) {
                            taxonInteractionsFacts.add(element);
                        } else {
                            //ignore
                        }
                    }
                }
            }
            if (!commonNameFacts.isEmpty()) {
                handleCommonNameFacts(state, taxon, commonNameFacts);
            }
            if (!distributionFacts.isEmpty()) {
                handleDistributionFacts(state, taxon, distributionFacts);
            }
            if (!taxonInteractionsFacts.isEmpty()) {
                handleTaxonInteractionsFacts(state, taxon, taxonInteractionsFacts);
            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling description of "
                    + cdmBaseStr(taxon) + (titleCache != null? (" " +titleCache) : "")+": " + e.getMessage());
        }
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

    private void handleTaxonInteractionsFacts(ColDpExportState state, CdmBase cdmBase,
            List<DescriptionElementBase> taxonInteractionsFacts) {

        ColDpExportTable table = ColDpExportTable.SPECIES_INTERACTION;
        String titleCache = null;
        if (cdmBase instanceof TaxonBase){
            titleCache = ((TaxonBase)cdmBase).getTitleCache();
        }
        for (DescriptionElementBase element : taxonInteractionsFacts) {

            try {

                String[] csvLine = new String[table.getSize()];

//                csvLine[table.getIndex(ColDpExportTable.TAXON_ID)] = getId(state, element);
                handleSource(state, element, table);
                csvLine[table.getIndex(ColDpExportTable.TAXON_ID)] = getId(state, cdmBase);
                csvLine[table.getIndex(ColDpExportTable.REL_TAXON_TAXON_ID)] = getId(state,
                        ((TaxonInteraction) element).getTaxon2());
                //TODO 5 taxon interaction Scentific name for species interaction
                //TODO 2 taxon interaction, why does this handling from other remarks handling?
                csvLine[table.getIndex(ColDpExportTable.REMARKS)] = createMultilanguageString(
                        ((TaxonInteraction) element).getDescription());
                state.getProcessor().put(table, element, csvLine);

            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when handling taxon interaction"
                        + cdmBaseStr(element) + (titleCache != null? (" " +titleCache) : "")+ ": " + e.getMessage());
            }
        }
    }

    private void handleSimpleMediaFact(ColDpExportState state, Taxon taxon, ColDpExportTable table,
            DescriptionElementBase element) {
        try {
            String[] csvLine;
            handleSource(state, element, ColDpExportTable.MEDIA);

            if (element instanceof TextData) {
                TextData textData = (TextData) element;
                csvLine = new String[table.getSize()];
                csvLine[table.getIndex(ColDpExportTable.TAXON_ID)] = getId(state, taxon);

                String mediaUris = "";
                for (Media media : textData.getMedia()) {
                    String mediaString = extractMediaUris(media.getRepresentations().iterator());
                    if (!StringUtils.isBlank(mediaString)) {
                        mediaUris += mediaString + ";";
                    } else {
                        state.getResult().addWarning("Empty Media object for " + taxon.getUserFriendlyTypeName() + " "
                                + taxon.getUuid() + " (media: " + media.getUuid() + ")");
                    }
                }
                csvLine[table.getIndex(ColDpExportTable.MEDIA_URL)] = mediaUris;
                //TODO 2 media title,  type  (MIME-type), what is meant here?
                csvLine[table.getIndex(ColDpExportTable.MEDIA_TITLE)] = mediaUris;
            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling single simple fact "
                    + cdmBaseStr(element) + ": " + e.getMessage());
        }

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

    private void handleSource(ColDpExportState state, DescriptionElementBase element,
            ColDpExportTable factsTable) {
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

    private void handleDistributionFacts(ColDpExportState state, Taxon taxon,
            List<DescriptionElementBase> distributionFacts) {

        ColDpExportTable table = ColDpExportTable.DISTRIBUTION;
        Set<Distribution> distributions = new HashSet<>();
        for (DescriptionElementBase element : distributionFacts) {
            try {
                //TODO 3 distribution also check feature
                if (element instanceof Distribution) {
                    String[] csvLine = new String[table.getSize()];
                    Distribution distribution = (Distribution) element;
                    distributions.add(distribution);

                    csvLine[table.getIndex(ColDpExportTable.TAXON_ID)] = getId(state, taxon);

                    NamedArea area = CdmBase.deproxy(distribution.getArea());
                    if (area != null) {
                        String gazetteer = "text";
                        String areaID = null;
                        if (area.getClass().equals(Country.class)) {
                            Country country = CdmBase.deproxy(area, Country.class);
                            areaID = country.getIdInVocabulary();
                            gazetteer = "iso";
                        } else if (NamedArea.isTdwgArea(area)) {
                            gazetteer = "tdwg";
                            areaID = area.getIdInVocabulary();
                        }

                        //areaID
                        csvLine[table.getIndex(ColDpExportTable.DIST_AREA_ID)] = areaID;

                        //area
                        //TODO 3 distribution area, does this always use the full English? Looks like this is not true for countries.
                        csvLine[table.getIndex(ColDpExportTable.DIST_AREA)] = area.getLabel();

                        //gazetteer
                        csvLine[table.getIndex(ColDpExportTable.DIST_GAZETTEER)] = gazetteer;
                    }

                    if (distribution.getStatus() != null) {
                        csvLine[table.getIndex(ColDpExportTable.DIST_STATUS)]
                                = state.getTransformer().getCacheByPresenceAbsenceTerm(distribution.getStatus());
                    }

                    //reference ID
                    handleReference(state, csvLine, table, distribution);

                    //remarks
                    csvLine[table.getIndex(ColDpExportTable.REMARKS)] = getRemarks(distribution);

                    state.getProcessor().put(table, distribution, csvLine);
                } else {
                    state.getResult()
                            .addError("The distribution description for the taxon " + taxon.getUuid()
                                    + " is not of type distribution. Could not be exported. UUID of the description element: "
                                    + element.getUuid());
                }
            } catch (Exception e) {
                e.printStackTrace();
                state.getResult().addException(e, "An unexpected error occurred when handling single distribution "
                        + cdmBaseStr(element) + ": " + e.getMessage());
            }
        }
    }

    private void handleCommonNameFacts(ColDpExportState state, Taxon taxon,
            List<DescriptionElementBase> commonNameFacts) {
        ColDpExportTable table = ColDpExportTable.VERNACULAR_NAME;

        for (DescriptionElementBase element : commonNameFacts) {
            try {
                if (element instanceof CommonTaxonName) {
                    String[] csvLine = new String[table.getSize()];
                    CommonTaxonName commonName = (CommonTaxonName) element;
                    csvLine[table.getIndex(ColDpExportTable.TAXON_ID)] = getId(state, taxon);
                    if (commonName.getName() != null) {
                        csvLine[table.getIndex(ColDpExportTable.VERN_NAME)] = commonName.getName();
                    }

                    //transliteration - we do not have this yet
                    csvLine[table.getIndex(ColDpExportTable.VERN_TRANSLITERATION)] = commonName.getTransliteration();

                    if (commonName.getLanguage() != null) {
                        //TODO 2 common name char(3) lang
                        csvLine[table.getIndex(ColDpExportTable.VERN_LANGUAGE)] = commonName.getLanguage().getIso639_2();
                    }

                    //TODO 2 common name country

                    if (commonName.getArea() != null) {
                        csvLine[table.getIndex(ColDpExportTable.VERN_AREA)] = commonName.getArea().getLabel();
                    }

                    //sex - we do not have this yet
                    csvLine[table.getIndex(ColDpExportTable.VERN_SEX)] = null;

                    //referenceID
                    handleReference(state, csvLine, table, commonName);

                    state.getProcessor().put(table, commonName, csvLine);
                } else if (element instanceof TextData){
                    String[] csvLine = new String[table.getSize()];
                    TextData commonName = (TextData) element;
                    handleSource(state, element, table);
                    csvLine[table.getIndex(ColDpExportTable.TAXON_ID)] = getId(state, taxon);
                    if (commonName.getMultilanguageText() != null) {
                        csvLine[table.getIndex(ColDpExportTable.VERN_NAME)] = createMultilanguageString(commonName.getMultilanguageText());
                    }
                    state.getProcessor().put(table, commonName, csvLine);
                } else {
                    state.getResult()
                            .addError("The common name description for the taxon " + taxon.getUuid()
                                    + " is not of type common name. Could not be exported. UUID of the description element: "
                                    + element.getUuid());
                }
            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when handling single common name "
                        + cdmBaseStr(element) + " - "+taxon.getTitleCache()+ ": " + e.getMessage());
            }
        }
    }

    private String getTitleCache(IIdentifiableEntity identEntity) {
        if (identEntity == null) {
            return "";
        }
        // TODO 3 titleCache refresh?
        return identEntity.getTitleCache();
    }

    private String getId(ColDpExportState state, ICdmBase cdmBase) {
        if (cdmBase == null) {
            return "";
        }
        // TODO 4 id type, make configurable
        return cdmBase.getUuid().toString();
    }

    private void handleSynonym(ColDpExportState state, Synonym synonym, int index) {
        try {
            if (isUnpublished(state.getConfig(), synonym)) {
                return;
            }
            TaxonName name = synonym.getName();
            handleName(state, name, synonym.getAcceptedTaxon());

            ColDpExportTable table = ColDpExportTable.SYNONYM;
            String[] csvLine = new String[table.getSize()];

            csvLine[table.getIndex(ColDpExportTable.ID)] = getId(state, synonym);

            //TODO 9 synonym sourceID //handled in referenceID and sec_ID
            csvLine[table.getIndex(ColDpExportTable.SOURCE_ID)] = null;

            csvLine[table.getIndex(ColDpExportTable.TAXON_ID)] = getId(state, synonym.getAcceptedTaxon());
            csvLine[table.getIndex(ColDpExportTable.TAX_NAME_ID)] = getId(state, name);
            if (synonym.getSec() != null && !state.getReferenceStore().contains(synonym.getSec().getUuid())) {
                handleReference(state, synonym.getSec());
                csvLine[table.getIndex(ColDpExportTable.REFERENCE_ID)] = getId(state, synonym.getSec());
            }
            csvLine[table.getIndex(ColDpExportTable.TAX_NAMEPHRASE)] = synonym.getAppendedPhrase();
            csvLine[table.getIndex(ColDpExportTable.TAX_SEC_ID)] = getId(state, synonym.getSec());

            state.getProcessor().put(table, synonym, csvLine);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling synonym "
                    + cdmBaseStr(synonym) + ": " + e.getMessage());
        }
    }

//    /**
//     * Handles misapplied names (including pro parte and partial as well as pro
//     * parte and partial synonyms
//     */
//    private void handleProPartePartialMisapplied(ColDpExportState state, Taxon taxon, Taxon accepted, boolean isProParte, boolean isMisapplied, int index) {
//        try {
//            Taxon ppSyonym = taxon;
//            if (isUnpublished(state.getConfig(), ppSyonym)) {
//                return;
//            }
//            TaxonName name = ppSyonym.getName();
//            handleName(state, name, accepted);
//
//            ColDpExportTable table = ColDpExportTable.SYNONYM;
//            String[] csvLine = new String[table.getSize()];
//
//            csvLine[table.getIndex(ColDpExportTable.SYNONYM_ID)] = getId(state, ppSyonym);
//            csvLine[table.getIndex(ColDpExportTable.TAXON_FK)] = getId(state, accepted);
//            csvLine[table.getIndex(ColDpExportTable.NAME_FK)] = getId(state, name);
//
//            Reference secRef = ppSyonym.getSec();
//
//            if (secRef != null && !state.getReferenceStore().contains(secRef.getUuid())) {
//                handleReference(state, secRef);
//            }
//            csvLine[table.getIndex(ColDpExportTable.SEC_REFERENCE_FK)] = getId(state, secRef);
//            csvLine[table.getIndex(ColDpExportTable.SEC_REFERENCE)] = getTitleCache(secRef);
//            Set<TaxonRelationship> rels = accepted.getTaxonRelations(ppSyonym);
//            TaxonRelationship rel = null;
//            boolean isPartial = false;
//            if (rels.size() == 1){
//                rel = rels.iterator().next();
//
//            }else if (rels.size() > 1){
//                Iterator<TaxonRelationship> iterator = rels.iterator();
//                while (iterator.hasNext()){
//                    rel = iterator.next();
//                    if (isProParte && rel.getType().isAnySynonym()){
//                        break;
//                    } else if (isMisapplied && rel.getType().isAnyMisappliedName()){
//                        break;
//                    }else{
//                        rel = null;
//                    }
//                }
//            }
//            if (rel != null){
//                Reference synSecRef = rel.getCitation();
//                if (synSecRef != null && !state.getReferenceStore().contains(synSecRef.getUuid())) {
//                    handleReference(state, synSecRef);
//                }
//                csvLine[table.getIndex(ColDpExportTable.SYN_SEC_REFERENCE_FK)] = getId(state, synSecRef);
//                csvLine[table.getIndex(ColDpExportTable.SYN_SEC_REFERENCE)] = getTitleCache(synSecRef);
//                isProParte = rel.getType().isProParte();
//                isPartial = rel.getType().isPartial();
//
//            }else{
//                state.getResult().addWarning("An unexpected error occurred when handling "
//                        + "pro parte/partial synonym or misapplied name  " + cdmBaseStr(taxon) );
//            }
//
//            // pro parte type
//
//            csvLine[table.getIndex(ColDpExportTable.IS_PRO_PARTE)] = isProParte ? "1" : "0";
//            csvLine[table.getIndex(ColDpExportTable.IS_PARTIAL)] = isPartial ? "1" : "0";
//            csvLine[table.getIndex(ColDpExportTable.IS_MISAPPLIED)] = isMisapplied ? "1" : "0";
//            csvLine[table.getIndex(ColDpExportTable.SORT_INDEX)] = String.valueOf(index);
//            state.getProcessor().put(table, ppSyonym, csvLine);
//        } catch (Exception e) {
//            state.getResult().addException(e, "An unexpected error occurred when handling "
//                    + "pro parte/partial synonym or misapplied name  " + cdmBaseStr(taxon) + ": " + e.getMessage());
//        }
//    }

    private void handleName(ColDpExportState state, TaxonName name, Taxon acceptedTaxon){
        handleName(state, name, acceptedTaxon, false);
    }

    private void handleName(ColDpExportState state, TaxonName name, Taxon acceptedTaxon, boolean acceptedName) {
        if (name == null || state.getNameStore().containsKey(name.getId())) {
            return;
        }
        try {
            //TODO is there a better way to handle configurable columns? #10451
            ColDpExportTable table = state.getConfig().isIncludeFullName() ? ColDpExportTable.NAME_WITH_FULLNAME :
                    ColDpExportTable.NAME;

            String[] csvLine = new String[table.getSize()];

            Rank rank = name.getRank();
            name = HibernateProxyHelper.deproxy(name);
            state.getNameStore().put(name.getId(), name.getUuid());

            csvLine[table.getIndex(ColDpExportTable.ID)] = getId(state, name);

            //TODO 3 name, handle LSIDs
//            if (name.getLsid() != null) {
//                csvLine[table.getIndex(ColDpExportTable.LSID)] = name.getLsid().getLsid();
//            } else {
//                csvLine[table.getIndex(ColDpExportTable.LSID)] = "";
//            }
            handleAlternativeId(state, csvLine, table, name);

            //TODO 9 name sourceID
            csvLine[table.getIndex(ColDpExportTable.SOURCE_ID)] = null;

            //basionymID
            TaxonName basionym = name.getBasionym();  //TODO 5 basionym, order in case there are >1 basionyms
            if (basionym != null) {
                if (!state.getNameStore().containsKey(basionym.getId())) {
                    handleName(state, basionym, null);
                }
                csvLine[table.getIndex(ColDpExportTable.NAME_BASIONYM_ID)] = getId(state, basionym);
            }

            //scientificName
            if (name.isProtectedTitleCache()) {
                //TODO 7 make it configurable if we should always take titleCache if titleCache is protected, as nameCache may not necessarily have complete data if titleCache is protected as it is considered to be irrelevant or at least preliminary
                String message = "";
                if (!isBlank(name.getNameCache())){
                    csvLine[table.getIndex(ColDpExportTable.NAME_SCIENTIFIC_NAME)] = name.getNameCache();
                    message = "ScientificName: Name cache " + name.getNameCache() + " used for name with protected titleCache " +  name.getTitleCache();
                }else {
                    csvLine[table.getIndex(ColDpExportTable.NAME_SCIENTIFIC_NAME)] = name.getTitleCache();
                    message = "ScientificName: Name has protected titleCache and no explicit nameCache: " +  name.getTitleCache();
                }
                state.getResult().addWarning(message);  //TODO 7 add location to warning
            } else {
                csvLine[table.getIndex(ColDpExportTable.NAME_SCIENTIFIC_NAME)] = name.getNameCache();
            }
            if (state.getConfig().isIncludeFullName()) {
                String authorshipCache = name.getAuthorshipCache();
                String normalizedAuthor = normalizeAuthor(state, authorshipCache);
                String titleCache = name.getTitleCache();
                if (titleCache != null) {
                    if (state.getConfig().isNormalizeAuthorsToIpniStandard()) {
                        titleCache = titleCache.replace(authorshipCache, normalizedAuthor);
                    }
                }
                csvLine[table.getIndex(ColDpExportTable.NAME_FULLNAME)] = titleCache;
            }

            //authorship
            String authorshipCache = name.getAuthorshipCache();
            csvLine[table.getIndex(ColDpExportTable.NAME_AUTHORSHIP)] = normalizeAuthor(state, name.getAuthorshipCache());
            //combinationAuthorship
            csvLine[table.getIndex(ColDpExportTable.NAME_COMBINATION_AUTHORSHIP)] = teamToString(state, name.getCombinationAuthorship());
            //combinationExAuthorship
            csvLine[table.getIndex(ColDpExportTable.NAME_COMBINATION_EX_AUTHORSHIP)] = teamToString(state, name.getExCombinationAuthorship());
            //combinationAuthorshipYear
            csvLine[table.getIndex(ColDpExportTable.NAME_COMBINATION_AUTHORSHIP_YEAR)] = name.getNomenclaturalReference() == null ? null : name.getNomenclaturalReference().getYear();
            //basionymAuthorship
            csvLine[table.getIndex(ColDpExportTable.NAME_BASIONYM_AUTHORSHIP)] = teamToString(state, name.getBasionymAuthorship());
            //basionymExAuthorship
            csvLine[table.getIndex(ColDpExportTable.NAME_BASIONYM_EX_AUTHORSHIP)] = teamToString(state, name.getExBasionymAuthorship());
            //basionymAuthorshipYear
            csvLine[table.getIndex(ColDpExportTable.NAME_BASIONYM_AUTHORSHIP_YEAR)] =
                    basionym == null? null :
                    basionym.getNomenclaturalReference() == null ? null :
                    basionym.getNomenclaturalReference().getYear();

            //rank
            csvLine[table.getIndex(ColDpExportTable.RANK)] = state.getTransformer().getCacheByRank(rank);

            //uninomial / genus
            if (name.isGenusOrSupraGeneric()) {
                csvLine[table.getIndex(ColDpExportTable.NAME_UNINOMIAL)] = name.getGenusOrUninomial();
            }else {
                csvLine[table.getIndex(ColDpExportTable.NAME_GENUS)] = name.getGenusOrUninomial();
            }

            csvLine[table.getIndex(ColDpExportTable.NAME_INFRAGENERIC_EPITHET)] = name.getInfraGenericEpithet();
            csvLine[table.getIndex(ColDpExportTable.NAME_SPECIFIC_EPITHET)] = name.getSpecificEpithet();
            csvLine[table.getIndex(ColDpExportTable.NAME_INFRASPECIFIC_EPITHET)] = name.getInfraSpecificEpithet();

            //TODO 3 name cultivar epithet, group epithet
            csvLine[table.getIndex(ColDpExportTable.NAME_CULTIVAR_EPITHET)] = name.getCultivarEpithet();

            //code
            csvLine[table.getIndex(ColDpExportTable.NAME_CODE)] = state.getTransformer().getCacheByNomenclaturalCode(name.getNameType());

            //TODO 5 name status, is this handling correct?
            //Also according to documentation we should put more detailed status information to the remarks field
            //   or an URI from the NOMEN ontology could be used
            if (name.getStatus() == null || name.getStatus().isEmpty()) {
                csvLine[table.getIndex(ColDpExportTable.NAME_STATUS)] = "acceptable";
            } else {
                NomenclaturalStatus status = name.getStatus().iterator().next();
                if (name.getStatus().size() > 1) {
                    String message = "There is >1 name status for " + name.getTitleCache();
                    state.getResult().addWarning(message);
                }
                String statusTypeString = state.getTransformer().getCacheByNomStatus(status.getType());
                csvLine[table.getIndex(ColDpExportTable.NAME_STATUS)] = statusTypeString;
                if (statusTypeString == null) {
                    String message = "Name status " + status.getType() + " not yet handled for name " + name.getTitleCache();
                    state.getResult().addWarning(message);
                }
            }

            //nom. ref.
            Reference nomRef = name.getNomenclaturalReference();
            if (nomRef != null) {
                csvLine[table.getIndex(ColDpExportTable.REFERENCE_ID)] = getId(state, nomRef);
                handleReference(state, nomRef);

                //publishedInYear
                if (nomRef.getDatePublished() != null) {
                    csvLine[table.getIndex(ColDpExportTable.NAME_PUBLISHED_IN_YEAR)] = nomRef.getDatePublished().getYear();
                }
                nomRef = HibernateProxyHelper.deproxy(nomRef);
                if (nomRef.getInReference() != null) {
                    Reference inReference = nomRef.getInReference();
                    if (inReference.getDatePublished() != null && nomRef.getDatePublished() == null) {
                        csvLine[table.getIndex(ColDpExportTable.NAME_PUBLISHED_IN_YEAR)]
                                = inReference.getDatePublished().getYear();
                    }
                }
            }

            //publishedInPage
            if (name.getNomenclaturalMicroReference() != null) {
                csvLine[table.getIndex(ColDpExportTable.NAME_PUBLISHED_IN_PAGE)] = name.getNomenclaturalMicroReference();
            }

            //publishedInPageLink
            String protologueUriString = extractProtologueURIs(state, name);
            csvLine[table.getIndex(ColDpExportTable.NAME_PUBLISHED_IN_PAGE_LINK)] = protologueUriString;

            //TODO 2 name links - do we have this in CDM?

            //remarks
            csvLine[table.getIndex(ColDpExportTable.REMARKS)] = getRemarks(name);

            handleTypeMaterial(state, name);


            state.getProcessor().put(table, name, csvLine);
            //TODO 1 nameRelationships - is this still an open issue? Do tests exist?
            handleNameRelationships(state, name);

        } catch (Exception e) {
            state.getResult().addException(e,
                    "An unexpected error occurred when handling the name " + cdmBaseStr(name) + ": " + name.getTitleCache() + ": " + e.getMessage());

            e.printStackTrace();
        }
    }

    //TODO 3 merge with WfoBackboneExport
    private String normalizeAuthor(ColDpExportState state, String authorship) {
        if (authorship == null) {
            return null;
        }else if (state.getConfig().isNormalizeAuthorsToIpniStandard()) {
            return TeamDefaultCacheStrategy.removeWhitespaces(authorship);
        }else {
            return authorship.replace("\\s+", " ").trim();
        }
    }

    private String teamToString(ColDpExportState state, TeamOrPersonBase<?> author) {
        if (author == null) {
            return null;
        }
        String nomCache = author.getNomenclaturalTitleCache();
        if (StringUtils.isEmpty(nomCache)){
            return null;
        }else {
            if (state.getConfig().isNormalizeAuthorsToIpniStandard()) {
                nomCache = TeamDefaultCacheStrategy.removeWhitespaces(nomCache);
            }
            return nomCache
                    .replace(", ", "|")
                    .replace(",", "|")
                    .replace(" & ", "|")
                    .replace("&", "|")
                    .replace(" et ", "|");
        }
    }

    private void handleTypeMaterial(ColDpExportState state, TaxonName name) {
        try {
            ColDpExportTable table = ColDpExportTable.TYPE_MATERIAL;
            String[] csvLine = new String[table.getSize()];

            Collection<TypeDesignationBase> specimenTypeDesignations = new ArrayList<>();
            List<TextualTypeDesignation> textualTypeDesignations = new ArrayList<>();

            for (TypeDesignationBase<?> typeDesignation : name.getTypeDesignations()) {
                if (typeDesignation.isInstanceOf(TextualTypeDesignation.class)) {
                    if (((TextualTypeDesignation) typeDesignation).isVerbatim() ){
                        Set<IdentifiableSource> sources = typeDesignation.getSources();
                        boolean isProtologue = false;
                        if (sources != null && !sources.isEmpty()){
                            IdentifiableSource source = sources.iterator().next();
                            if (name.getNomenclaturalReference() != null){
                                isProtologue = source.getCitation() != null? source.getCitation().getUuid().equals(name.getNomenclaturalReference().getUuid()): false;
                            }
                        }
                        if (isProtologue){
                            csvLine[table.getIndex(ColDpExportTable.TYPE_CITATION)] = ((TextualTypeDesignation) typeDesignation)
                                    .getPreferredText(Language.DEFAULT());
                        }else{
                            //TODO 2 specimen type textual type designation, still open?
                            textualTypeDesignations.add((TextualTypeDesignation) typeDesignation);
                        }
                    } else {
                        ///TODO 2 specimen type textual type designation, still open?
                        textualTypeDesignations.add((TextualTypeDesignation) typeDesignation);
                    }
                } else if (typeDesignation.isInstanceOf(SpecimenTypeDesignation.class)) {
                    SpecimenTypeDesignation specimenType = HibernateProxyHelper.deproxy(typeDesignation, SpecimenTypeDesignation.class);
                    specimenTypeDesignations.add(specimenType);
                    handleSpecimenType(state, specimenType, csvLine, table, name);
                }else if (typeDesignation instanceof NameTypeDesignation){
                    //ignore
//                  specimenTypeDesignations.add(HibernateProxyHelper.deproxy(typeDesignation, NameTypeDesignation.class));
                }
            }

            TypeDesignationSetContainer typeContainer = new TypeDesignationSetContainer(specimenTypeDesignations, name, TypeDesignationSetComparator.ORDER_BY.TYPE_STATUS);
            HTMLTagRules rules = new HTMLTagRules();
            //rules.addRule(TagEnum.name, "i");
            csvLine[table.getIndex(ColDpExportTable.TYPE_CITATION)] = typeContainer.print(false, false, false, rules);

            //TODO 2 type material what is this second type computation? Is it only about sources?
            StringBuilder stringbuilder = new StringBuilder();
            int i = 1;
            for (TextualTypeDesignation typeDesignation : textualTypeDesignations) {
                stringbuilder.append(typeDesignation.getPreferredText(Language.DEFAULT()));
                if (typeDesignation.getSources() != null && !typeDesignation.getSources().isEmpty() ){
                    stringbuilder.append( " [");
                    int index = 1;
                    for (IdentifiableSource source: typeDesignation.getSources()){
                        if (source.getCitation() != null){
                            stringbuilder.append(OriginalSourceFormatter.INSTANCE.format(source));
                        }
                        if (index < typeDesignation.getSources().size()) {
                            stringbuilder.append( ", ");
                        }
                        index++;
                    }
                    stringbuilder.append( "]");
                }
                if (i < textualTypeDesignations.size()) {
                    stringbuilder.append( "; ");
                } else {
                    stringbuilder.append(".");
                }
                i++;
            }
            csvLine[table.getIndex(ColDpExportTable.TYPE_CITATION)] = stringbuilder.toString();
        } catch (Exception e) {
            state.getResult().addException(e,
                    "An unexpected error occurred when handling the type designations for name " + cdmBaseStr(name) + ": " + name.getTitleCache() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    //TODO 2 specimen type what is the difference to handleSpecimenType()
//    private void handleSpecimenType_(ColDpExportState state, SpecimenTypeDesignation specimenType) {
//
//        if (specimenType.getTypeSpecimen() != null){
//            DerivedUnit specimen =  specimenType.getTypeSpecimen();
//            if(specimen != null && !state.getSpecimenStore().contains( specimen.getUuid())){
//               handleSpecimen(state, specimen);
//            }
//        }
//
//        String[] csvLine = new String[table.getSize()];
//        //TYPE_ID, SPECIMEN_FK, TYPE_VERBATIM_CITATION, TYPE_STATUS, TYPE_DESIGNATED_BY_STRING, TYPE_DESIGNATED_BY_REF_FK};
//        //Specimen_Fk und den Typusangaben (Art des Typus [holo, lecto, etc.], Quelle, Designation-Quelle, +
//        Set<TaxonName> typifiedNames = specimenType.getTypifiedNames();
//        for (TaxonName name: typifiedNames){
//            csvLine[table.getIndex(ColDpExportTable.TYPE_STATUS)] = specimenType.getTypeStatus() != null? specimenType.getTypeStatus().getDescription(): "";
//            csvLine[table.getIndex(ColDpExportTable.TYPE_ID)] = getId(state, specimenType);
//            csvLine[table.getIndex(ColDpExportTable.TYPIFIED_NAME_FK)] = getId(state, name);
//            csvLine[table.getIndex(ColDpExportTable.SPECIMEN_FK)] = getId(state, specimenType.getTypeSpecimen());
//            if (specimenType.getSources() != null && !specimenType.getSources().isEmpty()){
//                String sourceString = "";
//                int index = 0;
//                for (IdentifiableSource source: specimenType.getSources()){
//                    if (source.getCitation()!= null){
//                        sourceString = sourceString.concat(source.getCitation().getCitation());
//                    }
//                    index++;
//                    if (index != specimenType.getSources().size()){
//                        sourceString.concat(", ");
//                    }
//                }
//                csvLine[table.getIndex(ColDpExportTable.TYPE_INFORMATION_REF_STRING)] = sourceString;
//            }
//            if (specimenType.getDesignationSource() != null && specimenType.getDesignationSource().getCitation() != null && !state.getReferenceStore().contains(specimenType.getDesignationSource().getCitation().getUuid())){
//                handleReference(state, specimenType.getDesignationSource().getCitation());
//                csvLine[table.getIndex(ColDpExportTable.TYPE_DESIGNATED_BY_REF_FK)] = specimenType.getDesignationSource() != null ? getId(state, specimenType.getDesignationSource().getCitation()): "";
//            }
//
//            state.getProcessor().put(table, specimenType, csvLine);
//        }
//    }

    private void handleSpecimenType(ColDpExportState state, SpecimenTypeDesignation specimenType, String[] csvLine, ColDpExportTable table, TaxonName name) {

        try {

            DerivedUnit specimen = specimenType.getTypeSpecimen();
            if (specimenType.getTypeSpecimen() == null){
                return;
                //handleSpecimen(state, specimen);
            }

            //ID - TODO 3 specimen type best use dwc:occurrenceID
            csvLine[table.getIndex(ColDpExportTable.ID)] = getId(state, specimen);

            //TODO 9 specimenType sourceID //handled in referenceID
            csvLine[table.getIndex(ColDpExportTable.SOURCE_ID)] = null;

            //nameID
            Set<TaxonName> typifiedNames = specimenType.getTypifiedNames();
            if (typifiedNames.size() > 1){
                //TODO 3 specimen type we should
                state.getResult().addWarning("Please check the specimen type  "
                        + cdmBaseStr(specimenType) + " there are more then one typified name.");
            }
    //        if (typifiedNames.iterator().hasNext()){
    //            TaxonName name = typifiedNames.iterator().next();
                csvLine[table.getIndex(ColDpExportTable.TYPE_NAMEID)] = getId(state, name);
    //        }

            //TODO 3 specimen type citation - also done in calling method
            csvLine[table.getIndex(ColDpExportTable.TYPE_CITATION)] = specimen.getTitleCache();


            //TODO 3 specimen type type status transformation
            csvLine[table.getIndex(ColDpExportTable.TYPE_STATUS)] = specimenType.getTypeStatus() != null? specimenType.getTypeStatus().getDescription(): "";

            //TODO 2 specimen type referenceID -  see description, should be designation ref id or original name refid
            if (specimenType.getDesignationSource() != null && specimenType.getDesignationSource().getCitation() != null && !state.getReferenceStore().contains(specimenType.getDesignationSource().getCitation().getUuid())){
                //TODO 3 specimen type, should be source, not reference; why?
                handleReference(state, specimenType.getDesignationSource().getCitation());
                csvLine[table.getIndex(ColDpExportTable.REFERENCE_ID)] = specimenType.getDesignationSource() != null ? getId(state, specimenType.getDesignationSource().getCitation()): "";
            }

            //institution code
            if (specimen.getCollection() != null) {
                //TODO 3 specimen type institution code not available handling
                csvLine[table.getIndex(ColDpExportTable.TYPE_INSTITUTION_CODE)] = specimen.getCollection().getCode();
            }

            //catalog number
            if (specimen.isInstanceOf(DerivedUnit.class)){
                DerivedUnit derivedUnit = specimen;
                String catalogNumber = null;
                if (!StringUtils.isBlank(derivedUnit.getBarcode())){
                    catalogNumber = derivedUnit.getBarcode();
                } else if (!StringUtils.isBlank(derivedUnit.getAccessionNumber())){
                    catalogNumber = derivedUnit.getAccessionNumber();
                } else if (!StringUtils.isBlank(derivedUnit.getCatalogNumber())){
                    catalogNumber = derivedUnit.getCatalogNumber();
                }
                csvLine[table.getIndex(ColDpExportTable.TYPE_CATALOG_NUMBER)] = catalogNumber;
            }

            //TODO 9 specimen type associatedSequences - not yet implemented
            csvLine[table.getIndex(ColDpExportTable.TYPE_ASSOC_SEQ)] = null;

            //sex
            if (specimen.getSex() != null) {
                //TODO 7 specimen type transform sex, we usually don't have specimen with sex
                csvLine[table.getIndex(ColDpExportTable.TYPE_SEX)] = specimen.getSex().getLabel();
            }

            //link
            if (specimen.getPreferredStableUri() != null) {
                csvLine[table.getIndex(ColDpExportTable.LINK)] = specimen.getPreferredStableUri().toString();
            }

            //remarks
            //TODO 3 specimen type gather remarks on type designation, field unit(s) and specimen
            csvLine[table.getIndex(ColDpExportTable.REMARKS)] = getRemarks(specimen);

            //field unit
            //TODO 3 specimen type performance due to service call
            Collection<FieldUnit> fieldUnits = this.getOccurrenceService().findFieldUnits(specimen.getUuid(), null);
            if (!fieldUnits.isEmpty()) {
                if (fieldUnits.size() > 1) {
                    state.getResult().addWarning("There are >1 field units for specimen  "
                            + cdmBaseStr(specimen) + ".");
                }

                FieldUnit fieldUnit = fieldUnits.iterator().next();

                GatheringEvent gathering = fieldUnit.getGatheringEvent();
                if (gathering != null) {
                    handleGatheringEvent(state, csvLine, table, fieldUnit, gathering);
                }
            }

            state.getProcessor().put(table, specimenType, csvLine);
        }catch (Exception e) {
            state.getResult().addException(e,
                    "An unexpected error occurred when handling type material for name " + cdmBaseStr(name) + ": " + name.getTitleCache() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleGatheringEvent(ColDpExportState state, String[] csvLine, ColDpExportTable table,
            FieldUnit fieldUnit, GatheringEvent gathering) {
        //locality
        if (gathering.getLocality() != null) {
            csvLine[table.getIndex(ColDpExportTable.TYPE_LOCALITY)]
                    = gathering.getLocality().getText();
        }
        //TODO 3 gathering include areas, see CDM-light implementation and COL-DP description

        //country
        if (gathering.getCountry() != null) {
            csvLine[table.getIndex(ColDpExportTable.TYPE_COUNTRY)]
                    = gathering.getCountry().getLabel();
        }

        //exact location
        Point point = gathering.getExactLocation();
        if (point != null) {
            //latitude
            if (point.getLatitude() != null) {
                //TODO 3 gathering rounding
                csvLine[table.getIndex(ColDpExportTable.TYPE_LATITUDE)]
                        = point.getLatitude().toString();
            }
            //longitude
            if (point.getLongitude() != null) {
                //TODO 3 gathering rounding
                csvLine[table.getIndex(ColDpExportTable.TYPE_LONGITUDE)]
                        = point.getLongitude().toString();
            }
        }

        //altitude
        if (gathering.getAbsoluteElevation() != null) {
            //TODO 3 type specimen include max/text/depth
            csvLine[table.getIndex(ColDpExportTable.TYPE_ALTITUDE)]
                    = gathering.getAbsoluteElevation().toString();
        }

        //host - does more or less not exist in CDM
        csvLine[table.getIndex(ColDpExportTable.TYPE_HOST)] = null;

        //date
        if (gathering.getGatheringDate() != null) {
            //TODO 3 specimen type ISO 8601
            csvLine[table.getIndex(ColDpExportTable.TYPE_DATE)] = gathering
                    .getGatheringDate().toString();
        }


        csvLine[table.getIndex(ColDpExportTable.TYPE_COLLECTOR)]
                = createCollectorString(state, gathering, fieldUnit);
        //field number not yet in COL-DP
        //csvLine[table.getIndex(ColDpExportTable.COLLECTOR_NUMBER)] = fieldUnit.getFieldNumber();
    }

    private String createNameWithItalics(List<TaggedText> taggedName) {

        String fullTitleWithHtml = "";
        for (TaggedText taggedText: taggedName){
            if (taggedText.getType().equals(TagEnum.name)){
                fullTitleWithHtml += "<i>" + taggedText.getText() + "</i> ";
            }else if (taggedText.getType().equals(TagEnum.separator)){
                fullTitleWithHtml = fullTitleWithHtml.trim() + taggedText.getText() ;
            }else{
                fullTitleWithHtml += taggedText.getText() + " ";
            }
        }
        return fullTitleWithHtml;
    }

    private void handleNameRelationships(ColDpExportState state, TaxonName name) {
        ColDpExportTable table = ColDpExportTable.NAME_RELATION;
        ColDpExportTransformer transformer = (ColDpExportTransformer)state.getTransformer();
        try {
            //relations in which "name" is the from name
            Set<NameRelationship> fromRels = name.getRelationsFromThisName();
            for (NameRelationship rel : fromRels) {
                ColDpNameRelType coldpType = transformer.getColDpNameRelTypeByNameRelationType(rel.getType());
                if (coldpType == null) {
                    handleNoColDpNameRelType(state, rel.getType(), name);
                    continue;
                }else if (coldpType.getDirection() == 0) {
                    continue;  //the relation is handled the other way round if necessary
                }

                TaxonName name2 = CdmBase.deproxy(rel.getToName());
                handleRelNameCommonData(state, table, rel, name, name2, coldpType);
            }

            //relations in which "name" is the toName
            Set<NameRelationship> toRels = name.getRelationsToThisName();
            for (NameRelationship rel : toRels) {
                ColDpNameRelType coldpType = transformer.getColDpNameRelTypeByNameRelationType(rel.getType());
                if (coldpType == null) {
                    handleNoColDpNameRelType(state, rel.getType(), name);
                    continue;
                }else if (coldpType.getDirection() == 1) {
                    continue;  //the relation is handled the other way round if necessary
                }

                TaxonName name2 = CdmBase.deproxy(rel.getFromName());
                handleRelNameCommonData(state, table, rel, name, name2, coldpType);
            }
        } catch (Exception e) {
            state.getResult().addException(e,
                    "An unexpected error occurred when handling name relationships for name "
                            + cdmBaseStr(name) + ": " + name.getTitleCache() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleNoColDpNameRelType(ColDpExportState state, NameRelationshipType nameRelType, TaxonName taxonName) {
        String warning;
        if (nameRelType == null) {
            warning = "Name relationship has not type for name " + taxonName.getTitleCache();
        } else {
            //TODO misspelling, alternative name, blocking name for, avoids homonym of, unspecific "non"
            warning = "Name relationship type not yet handled by COL-DP: " + nameRelType.getTitleCache() + "; name: " + taxonName.getTitleCache();
        }
        state.getResult().addWarning(warning);
    }

    private void handleRelNameCommonData(ColDpExportState state, ColDpExportTable table,
            NameRelationship rel, TaxonName name, TaxonName relatedName, ColDpNameRelType coldpType) {

        String[] csvLine = new String[table.getSize()];

        if (!state.getNameStore().containsKey(relatedName.getId())) {
            handleName(state, relatedName, null);
        }
        csvLine[table.getIndex(ColDpExportTable.REL_NAME_NAMEID)] = getId(state, name);
        csvLine[table.getIndex(ColDpExportTable.REL_NAME_REL_NAMEID)] = getId(state, relatedName);

        csvLine[table.getIndex(ColDpExportTable.TYPE)] = coldpType.getLabel();
        csvLine[table.getIndex(ColDpExportTable.SOURCE_ID)] = null;
        if (rel.getCitation() != null) {
            handleReference(state, rel.getCitation());
            csvLine[table.getIndex(ColDpExportTable.REFERENCE_ID)] = getId(state, rel.getCitation());
        }

        csvLine[table.getIndex(ColDpExportTable.REMARKS)] = getRemarks(rel);
        state.getProcessor().put(table, rel.getUuid().toString(), csvLine);
    }

    private String getVolume(Reference reference) {
        if (reference.getVolume() != null) {
            return reference.getVolume();
        } else if (reference.getInReference() != null) {
            if (reference.getInReference().getVolume() != null) {
                return reference.getInReference().getVolume();
            }
        }
        return null;
    }

//    private void handleIdentifier(ColDpExportState state, CdmBase cdmBase) {
//        ColDpExportTable table = ColDpExportTable.IDENTIFIER;
//        String[] csvLine;
//        try {
//            if (cdmBase instanceof TaxonName){
//                TaxonName name = (TaxonName)cdmBase;
//
//                try{
//                    List<Identifier> identifiers = name.getIdentifiers();
//
//                    //first check which kind of identifiers are available and then sort and create table entries
//                    Map<IdentifierType, Set<Identifier>> identifierTypes = new HashMap<>();
//                    for (Identifier identifier: identifiers){
//                        IdentifierType type = identifier.getType();
//                        if (identifierTypes.containsKey(type)){
//                            identifierTypes.get(type).add(identifier);
//                        }else{
//                            Set<Identifier> tempList = new HashSet<>();
//                            tempList.add(identifier);
//                            identifierTypes.put(type, tempList);
//                        }
//                    }
//
//                    for (IdentifierType type:identifierTypes.keySet()){
//                        Set<Identifier> identifiersByType = identifierTypes.get(type);
//                        csvLine = new String[table.getSize()];
//                        csvLine[table.getIndex(ColDpExportTable.FK)] = getId(state, name);
//                        csvLine[table.getIndex(ColDpExportTable.REF_TABLE)] = "ScientificName";
//                        csvLine[table.getIndex(ColDpExportTable.IDENTIFIER_TYPE)] = type.getLabel();
//                        csvLine[table.getIndex(ColDpExportTable.EXTERNAL_NAME_IDENTIFIER)] = extractIdentifier(
//                                identifiersByType);
//                        state.getProcessor().put(table, name.getUuid() + ", " + type.getLabel(), csvLine);
//                    }
//
//
////                    Set<String> IPNIidentifiers = name.getIdentifiers(DefinedTerm.IDENTIFIER_NAME_IPNI());
////                    Set<String> tropicosIdentifiers = name.getIdentifiers(DefinedTerm.IDENTIFIER_NAME_TROPICOS());
////                    Set<String> WFOIdentifiers = name.getIdentifiers(DefinedTerm.uuidWfoNameIdentifier);
////                    if (!IPNIidentifiers.isEmpty()) {
////                        csvLine = new String[table.getSize()];
////                        csvLine[table.getIndex(CdmLightExportTable.FK)] = getId(state, name);
////                        csvLine[table.getIndex(CdmLightExportTable.REF_TABLE)] = "ScientificName";
////                        csvLine[table.getIndex(CdmLightExportTable.IDENTIFIER_TYPE)] = IPNI_NAME_IDENTIFIER;
////                        csvLine[table.getIndex(CdmLightExportTable.EXTERNAL_NAME_IDENTIFIER)] = extractIdentifier(
////                                IPNIidentifiers);
////                        state.getProcessor().put(table, name.getUuid() + ", " + IPNI_NAME_IDENTIFIER, csvLine);
////                    }
////                    if (!tropicosIdentifiers.isEmpty()) {
////                        csvLine = new String[table.getSize()];
////                        csvLine[table.getIndex(CdmLightExportTable.FK)] = getId(state, name);
////                        csvLine[table.getIndex(CdmLightExportTable.REF_TABLE)] = "ScientificName";
////                        csvLine[table.getIndex(CdmLightExportTable.IDENTIFIER_TYPE)] = TROPICOS_NAME_IDENTIFIER;
////                        csvLine[table.getIndex(CdmLightExportTable.EXTERNAL_NAME_IDENTIFIER)] = extractIdentifier(
////                                tropicosIdentifiers);
////                        state.getProcessor().put(table, name.getUuid() + ", " + IPNI_NAME_IDENTIFIER, csvLine);
////                    }
////                    if (!WFOIdentifiers.isEmpty()) {
////                        csvLine = new String[table.getSize()];
////                        csvLine[table.getIndex(CdmLightExportTable.FK)] = getId(state, name);
////                        csvLine[table.getIndex(CdmLightExportTable.REF_TABLE)] = "ScientificName";
////                        csvLine[table.getIndex(CdmLightExportTable.IDENTIFIER_TYPE)] = WFO_NAME_IDENTIFIER;
////                        csvLine[table.getIndex(CdmLightExportTable.EXTERNAL_NAME_IDENTIFIER)] = extractIdentifier(
////                                WFOIdentifiers);
////                        state.getProcessor().put(table, name.getUuid() + ", " + WFO_NAME_IDENTIFIER, csvLine);
////                    }
//                }catch(Exception e){
//                    state.getResult().addWarning("Please check the identifiers for "
//                            + cdmBaseStr(cdmBase) + " maybe there is an empty identifier");
//
//
//                }
//            }else{
//                if (cdmBase instanceof IdentifiableEntity){
//                    IdentifiableEntity<?> identifiableEntity = (IdentifiableEntity<?>) cdmBase;
//                    List<Identifier> identifiers = identifiableEntity.getIdentifiers();
//                    String tableName = null;
//                    if (cdmBase instanceof Reference){
//                        tableName = "Reference";
//                    }else if (cdmBase instanceof SpecimenOrObservationBase){
//                        tableName = "Specimen";
//                    }else if (cdmBase instanceof Taxon){
//                        tableName = "Taxon";
//                    }else if (cdmBase instanceof Synonym){
//                        tableName = "Synonym";
//                    }else if (cdmBase instanceof TeamOrPersonBase){
//                        tableName = "PersonOrTeam";
//                    }
//
//                    for (Identifier identifier: identifiers){
//                        if (identifier.getType() == null && identifier.getIdentifier() == null){
//                            state.getResult().addWarning("Please check the identifiers for "
//                                    + cdmBaseStr(cdmBase) + " there is an empty identifier");
//                            continue;
//                        }
//
//                        csvLine = new String[table.getSize()];
//                        csvLine[table.getIndex(ColDpExportTable.FK)] = getId(state, cdmBase);
//
//                        if (tableName != null){
//                            csvLine[table.getIndex(ColDpExportTable.REF_TABLE)] = tableName;
//                            csvLine[table.getIndex(ColDpExportTable.IDENTIFIER_TYPE)] = identifier.getType() != null? identifier.getType().getLabel():null;
//                            csvLine[table.getIndex(ColDpExportTable.EXTERNAL_NAME_IDENTIFIER)] = identifier.getIdentifier();
//                            state.getProcessor().put(table, cdmBase.getUuid() + (identifier.getType() != null? identifier.getType().getLabel():null), csvLine);
//                        }
//                    }
//                    if (cdmBase instanceof Reference ){
//                        Reference ref = (Reference)cdmBase;
//                        if (ref.getDoi() != null){
//                            csvLine = new String[table.getSize()];
//                            csvLine[table.getIndex(ColDpExportTable.FK)] = getId(state, cdmBase);
//                            csvLine[table.getIndex(ColDpExportTable.REF_TABLE)] = tableName;
//                            csvLine[table.getIndex(ColDpExportTable.IDENTIFIER_TYPE)] = "DOI";
//                            csvLine[table.getIndex(ColDpExportTable.EXTERNAL_NAME_IDENTIFIER)] = ref.getDoiString();
//                            state.getProcessor().put(table, cdmBase.getUuid() + "DOI", csvLine);
//                        }
//                    }
//
//                    if (cdmBase instanceof TeamOrPersonBase){
//                        TeamOrPersonBase<?> person= HibernateProxyHelper.deproxy(cdmBase, TeamOrPersonBase.class);
//                        if (person instanceof Person &&  ((Person)person).getOrcid() != null){
//                            csvLine = new String[table.getSize()];
//                            csvLine[table.getIndex(ColDpExportTable.FK)] = getId(state, cdmBase);
//                            csvLine[table.getIndex(ColDpExportTable.REF_TABLE)] = tableName;
//                            csvLine[table.getIndex(ColDpExportTable.IDENTIFIER_TYPE)] = "ORCID";
//                            csvLine[table.getIndex(ColDpExportTable.EXTERNAL_NAME_IDENTIFIER)]=  ((Person)person).getOrcid().asURI();
//                            state.getProcessor().put(table, cdmBase.getUuid() + "ORCID", csvLine);
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            state.getResult().addException(e, "An unexpected error occurred when handling identifiers for "
//                    + cdmBaseStr(cdmBase) + ": " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    private String extractIdentifier(Set<Identifier> identifierSet) {
//
//        String identifierString = "";
//        for (Identifier identifier : identifierSet) {
//            if (!StringUtils.isBlank(identifierString)) {
//                identifierString += ", ";
//            }
//            identifierString += identifier.getIdentifier();
//        }
//        return identifierString;
//    }

    private String extractProtologueURIs(ColDpExportState state, TaxonName name) {
        if (name.getNomenclaturalSource() != null){
            Set<ExternalLink> links = name.getNomenclaturalSource().getLinks();
            return extractLinkUris(links.iterator());
        }else{
            return null;
        }
    }

    private String extractMediaURIs(ColDpExportState state, Set<? extends DescriptionBase<?>> descriptionsSet,
            Feature feature) {

        String mediaUriString = "";
        Set<DescriptionElementBase> elements = new HashSet<>();
        for (DescriptionBase<?> description : descriptionsSet) {
            try {
                if (!description.getElements().isEmpty()) {
                    elements = description.getElements();

                    for (DescriptionElementBase element : elements) {
                        Feature entityFeature = HibernateProxyHelper.deproxy(element.getFeature());
                        if (entityFeature.equals(feature)) {
                            if (!element.getMedia().isEmpty()) {
                                List<Media> media = element.getMedia();
                                for (Media mediaElement : media) {
                                    Iterator<MediaRepresentation> it = mediaElement.getRepresentations().iterator();
                                    mediaUriString = extractMediaUris(it);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when extracting media URIs for "
                        + cdmBaseStr(description) + ": " + e.getMessage());
            }
        }
        return mediaUriString;
    }


    private String extractStatusString(ColDpExportState state, TaxonName name, boolean abbrev) {
        try {
            Set<NomenclaturalStatus> status = name.getStatus();
            if (status.isEmpty()) {
                return "";
            }
            String statusString = "";
            for (NomenclaturalStatus nameStatus : status) {
                if (nameStatus != null) {
                    if (abbrev) {
                        if (nameStatus.getType() != null) {
                            statusString += nameStatus.getType().getIdInVocabulary();
                        }
                    } else {
                        if (nameStatus.getType() != null) {
                            statusString += nameStatus.getType().getTitleCache();
                        }
                    }
                    if (!abbrev) {

                        if (nameStatus.getRuleConsidered() != null
                                && !StringUtils.isBlank(nameStatus.getRuleConsidered())) {
                            statusString += ": " + nameStatus.getRuleConsidered();
                        }
                        if (nameStatus.getCitation() != null) {
                            String shortCitation = OriginalSourceFormatter.INSTANCE.format(nameStatus.getCitation(), null);
                            statusString += " (" + shortCitation + ")";
                        }
//                        if (nameStatus.getCitationMicroReference() != null
//                                && !StringUtils.isBlank(nameStatus.getCitationMicroReference())) {
//                            statusString += " " + nameStatus.getCitationMicroReference();
//                        }
                    }
                    statusString += " ";
                }
            }
            return statusString;
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when extracting status string for "
                    + cdmBaseStr(name) + ": " + e.getMessage());
            return "";
        }
    }

    private void handleHomotypicalGroup(ColDpExportState state, HomotypicalGroup group, Taxon acceptedTaxon) {
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

////            Collections.sort(typifiedNames, new HomotypicalGroupNameComparator(firstname, true));
//            String typifiedNamesString = "";
//            String typifiedNamesWithSecString = "";
//            String typifiedNamesWithoutAccepted = "";
//            String typifiedNamesWithoutAcceptedWithSec = "";
//            int index = 0;
//            for (TaxonName name : typifiedNames) {
//                // Concatenated output string for homotypic group (names and
//                // citations) + status + some name relations (e.g. “non”)
//                // TODO 3 commented code - can this be deleted?  -- : nameRelations, which and how to display
//                Set<TaxonBase> taxonBases = name.getTaxonBases();
//                TaxonBase<?> taxonBase;
//
//                String sec = "";
//                String nameString = name.getFullTitleCache();
//                String doubtful = "";
//
//                if (state.getConfig().isAddHTML()){
//                    nameString = createNameWithItalics(name.getTaggedFullTitle()) ;
//                }
//
//                Set<NameRelationship> related = name.getNameRelations();
//                List<NameRelationship> relatedList = new ArrayList<>(related);
//
//                Collections.sort(relatedList, (nr1, nr2)-> {
//                        return nr1.getType().compareTo(nr2.getType());});
//
//                List<NameRelationship> nonNames = new ArrayList<>();
//                List<NameRelationship> otherRelationships = new ArrayList<>();
//
//                for (NameRelationship rel: relatedList){
//                    //no inverse relations
//                    if (rel.getFromName().equals(name)){
//                     // alle Homonyme und inverse blocking names
//                        if (rel.getType().equals(NameRelationshipType.LATER_HOMONYM())
//                                || rel.getType().equals(NameRelationshipType.TREATED_AS_LATER_HOMONYM())
//                                || (rel.getType().equals(NameRelationshipType.BLOCKING_NAME_FOR()))
//                                || (rel.getType().equals(NameRelationshipType.UNSPECIFIC_NON()))
//                                || (rel.getType().equals(NameRelationshipType.AVOIDS_HOMONYM_OF()))
//                                ){
//                            nonNames.add(rel);
//                        }else if (!rel.getType().isBasionymRelation()){
//                            otherRelationships.add(rel);
//                        }
//                    }
//                    if (state.getConfig().isShowInverseNameRelationsInHomotypicGroup()) {
//			if (rel.getToName().equals(name)){
//                            // alle Homonyme und inverse blocking names
////                               if (rel.getType().equals(NameRelationshipType.LATER_HOMONYM())
////                                       || rel.getType().equals(NameRelationshipType.TREATED_AS_LATER_HOMONYM())
////                                       || (rel.getType().equals(NameRelationshipType.BLOCKING_NAME_FOR()))
////                                       || (rel.getType().equals(NameRelationshipType.UNSPECIFIC_NON()))
////                                       || (rel.getType().equals(NameRelationshipType.AVOIDS_HOMONYM_OF()))
////                                       ){
////                                   nonNames.add(rel);
////                               }else if (!rel.getType().isBasionymRelation()){
//                                   otherRelationships.add(rel);
////                               }
//                           }
//                    }
//                }
//
//                String nonRelNames = "";
//                String relNames = "";
//
//                if (nonNames.size() > 0){
//                    nonRelNames += " [";
//                }
//                for (NameRelationship relName: nonNames){
//                    String label = "non ";
//                    TaxonName relatedName = null;
//                    if (relName.getFromName().equals(name)){
//                        relatedName = relName.getToName();
//                        if (state.getConfig().isAddHTML()){
//                        	nonRelNames += label + createNameWithItalics(relatedName.getTaggedName())+ " ";
//                        }else{
//                        	nonRelNames += label + relatedName.getTitleCache();
//                        }
//                    }
////                    else{
////                        label = relName.getType().getInverseLabel() + " ";
////                        relatedName = relName.getFromName();
////                        nonRelNames += label + relatedName.getTitleCache() + " ";
////                    }
//                }
//                nonRelNames.trim();
//                if (nonNames.size() > 0){
//                    nonRelNames = StringUtils.strip(nonRelNames, null);
//                    nonRelNames += "] ";
//                }
//
//                //other relationships
//                if (otherRelationships.size() > 0){
//                    relNames += " [";
//                }
//                for (NameRelationship rel: otherRelationships){
//                    String label = "";
//                    TaxonName relatedName = null;
//                    if (rel.getFromName().equals(name)){
//                        label = rel.getType().getLabel() + " ";
//                        relatedName = rel.getToName();
//                        if (state.getConfig().isAddHTML()){
//                            relNames += label + createNameWithItalics(relatedName.getTaggedName())+ " ";
//                        }else{
//                            relNames += label + relatedName.getTitleCache();
//                        }
//                    }
//                    else {
//                        label = rel.getType().getInverseLabel() + " ";
//                        relatedName = rel.getFromName();
//                        if (state.getConfig().isAddHTML()){
//                            relNames += label + createNameWithItalics(relatedName.getTaggedName())+ " ";
//                        }else{
//                            relNames += label + relatedName.getTitleCache();
//                        }
//                    }
//                }
//                relNames.trim();
//                if (otherRelationships.size() > 0){
//                    relNames = StringUtils.stripEnd(relNames, null);
//                    relNames += "] ";
//                }
//
//
//                String synonymSign = "";
//                if (index > 0){
//                    if (name.isInvalid()){
//                        synonymSign = "\u2212 ";
//                    }else{
//                        synonymSign = "\u2261 ";
//                    }
//                }else{
//                    if (name.isInvalid() ){
//                        synonymSign = "\u2212 ";
//                    }else{
//                        synonymSign = "\u003D ";
//                    }
//                }
//                boolean isAccepted = false;
//
//                if (taxonBases.size() == 1){
//                     taxonBase = HibernateProxyHelper.deproxy(taxonBases.iterator().next());
//
//                     if (taxonBase.getSec() != null){
//                         sec = OriginalSourceFormatter.INSTANCE_WITH_YEAR_BRACKETS.format(taxonBase.getSecSource());
//                     }
//                     if (taxonBase.isDoubtful()){
//                         doubtful = "?";
//                     }else{
//                         doubtful = "";
//                     }
//                     if (taxonBase instanceof Synonym){
//                         if (isNotBlank(sec)){
//                             sec = " syn. sec. " + sec + " ";
//                         }else {
//                             sec = "";
//                         }
//
//                         typifiedNamesWithoutAccepted += synonymSign + doubtful + nameString + nonRelNames + relNames;
//                         typifiedNamesWithoutAcceptedWithSec += synonymSign + doubtful + nameString + sec + nonRelNames + relNames;
//                     }else{
////                         sec = "";
//                         if (!(((Taxon)taxonBase).isProparteSynonym() || ((Taxon)taxonBase).isMisapplication())){
//                             isAccepted = true;
//                         }else {
//                             synonymSign = "\u003D ";
//                         }
//
//                     }
//                     if (taxonBase.getAppendedPhrase() != null){
//                         if (state.getConfig().isAddHTML()){
//                             String taxonString = createNameWithItalics(taxonBase.getTaggedTitle()) ;
//                             taxonString = taxonString.replace("sec "+sec, "");
//                             String nameCacheWithItalics = createNameWithItalics(name.getTaggedName());
//                             nameString = nameString.replace(nameCacheWithItalics, taxonString);
//                         }
//                     }
//                }else{
//                    //there are names used more than once?
//                    for (TaxonBase<?> tb: taxonBases){
//                        if (tb.getSec() != null){
//                            sec = OriginalSourceFormatter.INSTANCE_WITH_YEAR_BRACKETS.format(tb.getSecSource());
//                        }
//                        if (tb.isDoubtful()){
//                            doubtful = "?";
//                        }else{
//                            doubtful = "";
//                        }
//                        if (tb instanceof Synonym ){
//                            if (!((Synonym)tb).getAcceptedTaxon().equals(acceptedTaxon)) {
//                                continue;
//                            }
//                            if (StringUtils.isNotBlank(sec)){
//                                sec = " syn. sec. " + sec + " ";
//                            }else {
//                                sec = "";
//                            }
//
//                            break;
//                        }else{
//                            sec = "";
//                            if (!(((Taxon)tb).isProparteSynonym() || ((Taxon)tb).isMisapplication())){
//                                isAccepted = true;
//                                break;
//                            }else {
//                                synonymSign = "\u003D ";
//                            }
//                        }
//                    }
//                    if (!isAccepted){
//                        typifiedNamesWithoutAccepted += synonymSign + doubtful + nameString + "; ";
//                        typifiedNamesWithoutAcceptedWithSec += synonymSign + doubtful + nameString + sec;
//                        typifiedNamesWithoutAcceptedWithSec = typifiedNamesWithoutAcceptedWithSec.trim() + "; ";
//                    }
//                }
//                typifiedNamesString += synonymSign + doubtful + nameString + nonRelNames + relNames;
//                typifiedNamesWithSecString += synonymSign + doubtful + nameString + sec + nonRelNames + relNames;
//
//
//                 index++;
//            }

//            state.getProcessor().put(table, String.valueOf(group.getId()), csvLine);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling homotypic group "
                    + cdmBaseStr(group) + ": " + e.getMessage());
        }
    }

    private void handleReference(ColDpExportState state, String[] csvLine, ColDpExportTable table,
            ISourceable<?> sourceable) {

        String referenceID = null;
        for (IOriginalSource osb : sourceable.getSources()) {
            if (osb.getCitation() != null && osb.getType().isPrimarySource()) {
                referenceID = CdmUtils.concat(";", getId(state, osb.getCitation()));
                handleReference(state, osb.getCitation());
            }
        }
        csvLine[table.getIndex(ColDpExportTable.REFERENCE_ID)] = referenceID;
    }

    private void handleReference(ColDpExportState state, Reference reference) {
        try {
            if (state.getReferenceStore().contains(reference.getUuid())) {
                return;
            }

            state.addReferenceToStore(reference);
            ColDpExportTable table = ColDpExportTable.REFERENCE;
            String[] csvLine = new String[table.getSize()];
            reference = HibernateProxyHelper.deproxy(reference);

            // TODO 1 reference short citations correctly
//            String shortCitation = OriginalSourceFormatter.INSTANCE_WITH_YEAR_BRACKETS.format(reference, null); // Should be Author(year) like in Taxon.sec
//            csvLine[table.getIndex(ColDpExportTable.BIBLIO_SHORT_CITATION)] = shortCitation;

            csvLine[table.getIndex(ColDpExportTable.ID)] = getId(state, reference);

            //alternativeID
            handleAlternativeId(state, csvLine, table, reference);

            //TODO 9 reference sourceID
            csvLine[table.getIndex(ColDpExportTable.SOURCE_ID)] = null;

            //citation
            csvLine[table.getIndex(ColDpExportTable.REF_CITATION)] = reference.getCitation();

            //type
            csvLine[table.getIndex(ColDpExportTable.TYPE)] = state.getTransformer().getCacheByReferenceType(reference);

            //author
            if (reference.getAuthorship() != null) {
                TeamOrPersonBase<?> author = reference.getAuthorship();
                //TODO 3 reference author formatting fine tuning
                csvLine[table.getIndex(ColDpExportTable.REF_AUTHOR)] = author.getTitleCache();
            }

            //editor
            if (reference.getEditor() != null) {  //if in future this is not a String
                //TODO 3 reference editor formatting fine tuning, not yet relevant
                csvLine[table.getIndex(ColDpExportTable.REF_AUTHOR)] = reference.getEditor();
            }

            // TODO 1 reference get preferred title
            csvLine[table.getIndex(ColDpExportTable.REF_TITLE)] = reference.isProtectedTitleCache()
                    ? reference.getTitleCache() : reference.getTitle();
//          csvLine[table.getIndex(ColDpExportTable.ABBREV_REF_TITLE)] = reference.isProtectedAbbrevTitleCache()
//                    ? reference.getAbbrevTitleCache() : reference.getAbbrevTitle();

            //inRef
            if (reference.getInReference() != null && reference.getInSeries() != reference.getInReference()) {
                //TODO 2 reference exclude series as inRef
                Reference inRef = reference.getInReference();

                //containerAuthor
                if (inRef.getAuthorship() != null) {
                    TeamOrPersonBase<?> containerAuthor = inRef.getAuthorship();
                    //TODO 1 reference inRef-author formatting fine tuning
                    csvLine[table.getIndex(ColDpExportTable.REF_CONTAINER_AUTHOR)] = containerAuthor.getTitleCache();
                }

                // TODO 1 reference get preferred inRef-title
                csvLine[table.getIndex(ColDpExportTable.REF_CONTAINER_TITLE)] = inRef.isProtectedTitleCache()
                        ? inRef.getTitleCache() : inRef.getTitle();
//              csvLine[table.getIndex(ColDpExportTable.ABBREV_REF_TITLE)] = inRef.isProtectedAbbrevTitleCache()
//                        ? inRef.getAbbrevTitleCache() : inRef.getAbbrevTitle();

            }

            //issued TODO 2 reference issued formatting
            csvLine[table.getIndex(ColDpExportTable.REF_ISSUED)] = reference.getDatePublishedString();

            //accessed TODO 2 reference accessed also for source and formatting
            if (reference.getAccessed() != null) {
                csvLine[table.getIndex(ColDpExportTable.REF_ACCESSED)] = reference.getAccessed().toDate().toString();
            }

            if (reference.getInSeries() != null) {
                Reference series = reference.getInReference();

                //containerEditor
                if (series.getEditor() != null) {
//                    TeamOrPersonBase<?> containerAuthor = series.getAuthorship();
                    //TODO 2 reference collection editor
                    csvLine[table.getIndex(ColDpExportTable.REF_COLLECTION_EDITOR)] = series.getEditor();
                }

                // TODO 2 reference get preferred title
                //collection title
                csvLine[table.getIndex(ColDpExportTable.REF_COLLECTION_TITLE)] = series.isProtectedTitleCache()
                        ? series.getTitleCache() : series.getTitle();
//              csvLine[table.getIndex(ColDpExportTable.ABBREV_REF_TITLE)] = inRef.isProtectedAbbrevTitleCache()
//                        ? inRef.getAbbrevTitleCache() : inRef.getAbbrevTitle();

            }

            //volume
            csvLine[table.getIndex(ColDpExportTable.REF_VOLUME)] = getVolume(reference);

            //issue TODO 2 reference issue (we currently do not handle issues separately, but could be parsed in some cases
            csvLine[table.getIndex(ColDpExportTable.REF_ISSUE)] = null;

            //edition
            csvLine[table.getIndex(ColDpExportTable.REF_EDITION)] = reference.getEdition();

            //volume
            csvLine[table.getIndex(ColDpExportTable.REF_PAGE)] = reference.getPages();

            //publisher TODO 3 reference publisher2
            csvLine[table.getIndex(ColDpExportTable.REF_PUBLISHER)] = reference.getPublisher();

            //publisherPlace TODO 3 reference publisherPlace2
            csvLine[table.getIndex(ColDpExportTable.REF_PUBLISHER_PLACE)] = reference.getPlacePublished();

            //TODO 7 reference version does not exist yet in CDM
            csvLine[table.getIndex(ColDpExportTable.REF_VERSION)] = null; //reference.getVersion();

            //isbn
            csvLine[table.getIndex(ColDpExportTable.REF_ISBN)] = reference.getIsbn();

            //issn
            csvLine[table.getIndex(ColDpExportTable.REF_ISSN)] = reference.getIssn();

            //doi
            csvLine[table.getIndex(ColDpExportTable.REF_DOI)] = reference.getDoiString();

            //TODO 2 reference link link (=> external link)
//            csvLine[table.getIndex(ColDpExportTable.LINK)] = null;
            if (reference.getUri() != null) {
                csvLine[table.getIndex(ColDpExportTable.LINK)] = reference.getUri().toString();
            }

            csvLine[table.getIndex(ColDpExportTable.REMARKS)] = getRemarks(reference);

            state.getProcessor().put(table, reference, csvLine);
        } catch (Exception e) {
            e.printStackTrace();
            state.getResult().addException(e, "An unexpected error occurred when handling reference "
                    + cdmBaseStr(reference) + ": " + e.getMessage());
        }
    }

    //TODO 2 fullAuthorship - still needed?
    private String createFullAuthorship(Reference reference) {
        TeamOrPersonBase<?> authorship = reference.getAuthorship();
        String fullAuthorship = "";
        if (authorship == null) {
            return null;
        }
        authorship = HibernateProxyHelper.deproxy(authorship);
        if (authorship instanceof Person) {
            fullAuthorship = ((Person) authorship).getTitleCache();

        } else if (authorship instanceof Team) {

            Team authorTeam = (Team)authorship;
            fullAuthorship = authorTeam.cacheStrategy().getTitleCache(authorTeam);
        }
        return fullAuthorship;
    }

    private String extractMediaUris(Iterator<MediaRepresentation> it) {

        String mediaUriString = "";
        boolean first = true;
        while (it.hasNext()) {
            MediaRepresentation rep = it.next();
            List<MediaRepresentationPart> parts = rep.getParts();
            for (MediaRepresentationPart part : parts) {
                if (first) {
                    if (part.getUri() != null) {
                        mediaUriString += part.getUri().toString();
                        first = false;
                    }
                } else {
                    if (part.getUri() != null) {
                        mediaUriString += ", " + part.getUri().toString();
                    }
                }
            }
        }

        return mediaUriString;
    }

    private String extractLinkUris(Iterator<ExternalLink> it) {

        String linkUriString = "";
        boolean first = true;
        while (it.hasNext()) {
            ExternalLink link = it.next();
            if (first) {
                if (link.getUri() != null) {
                    linkUriString += link.getUri().toString();
                    first = false;
                }
            } else {
                if (link.getUri() != null) {
                    linkUriString += ", " + link.getUri().toString();
                }
            }
        }
        return linkUriString;
    }

    private String createCollectorString(ColDpExportState state, GatheringEvent gathering, FieldUnit fieldUnit) {
        try {
            String collectorString = "";
            AgentBase<?> collectorA = CdmBase.deproxy(gathering.getCollector());
            if (gathering.getCollector() != null) {
                if (collectorA instanceof TeamOrPersonBase && state.getConfig().isHighLightPrimaryCollector()) {

                    Person primaryCollector = fieldUnit.getPrimaryCollector();
                    if (collectorA instanceof Team) {
                        Team collectorTeam = (Team) collectorA;
                        boolean isFirst = true;
                        for (Person member : collectorTeam.getTeamMembers()) {
                            if (!isFirst) {
                                collectorString += "; ";
                            }
                            if (member.equals(primaryCollector)) {
                                // highlight
                                collectorString += "<b>" + member.getTitleCache() + "</b>";
                            } else {
                                collectorString += member.getTitleCache();
                            }
                        }
                    }
                } else {
                    collectorString = collectorA.getTitleCache();
                }
            }
            return collectorString;
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when creating collector string for "
                    + cdmBaseStr(fieldUnit) + ": " + e.getMessage());
            return "";
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
    protected boolean doCheck(ColDpExportState state) {
        return false;
    }

    @Override
    protected boolean isIgnore(ColDpExportState state) {
        return false;
    }
}