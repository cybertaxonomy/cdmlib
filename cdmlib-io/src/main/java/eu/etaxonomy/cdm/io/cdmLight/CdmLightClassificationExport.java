/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmLight;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.dto.CondensedDistribution;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationSetFormatter;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationSetManager;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.compare.name.TypeComparator;
import eu.etaxonomy.cdm.compare.taxon.HomotypicGroupTaxonComparator;
import eu.etaxonomy.cdm.ext.geo.IEditGeoService;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.format.reference.OriginalSourceFormatter;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ExportResult.ExportResultState;
import eu.etaxonomy.cdm.io.common.TaxonNodeOutStreamPartitioner;
import eu.etaxonomy.cdm.io.common.XmlExportState;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
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
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.ExternalLink;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NomenclaturalSource;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TextualTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDtoByRankAndNameComparator;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author k.luther
 * @since 15.03.2017
 */
@Component
public class CdmLightClassificationExport
        extends CdmExportBase<CdmLightExportConfigurator, CdmLightExportState, IExportTransformer, File>{

    private static final long serialVersionUID = 2518643632756927053L;
    private static final String STD_TEAM_CONCATINATION = ", ";
    private static final String FINAL_TEAM_CONCATINATION = " & ";

    private static final String IPNI_NAME_IDENTIFIER = "Ipni Name Identifier";
    private static final String TROPICOS_NAME_IDENTIFIER = "Tropicos Name Identifier";
    private static final String WFO_NAME_IDENTIFIER = "WFO Name Identifier";

    @Autowired
    IEditGeoService geoService;

    public CdmLightClassificationExport() {
        super();
        this.ioName = this.getClass().getSimpleName();
    }

    @Override
    public long countSteps(CdmLightExportState state) {
        TaxonNodeFilter filter = state.getConfig().getTaxonNodeFilter();
        return getTaxonNodeService().count(filter);
    }

    @Override
    protected void doInvoke(CdmLightExportState state) {
        try {

            IProgressMonitor monitor = state.getConfig().getProgressMonitor();
            CdmLightExportConfigurator config = state.getConfig();
            if (config.getTaxonNodeFilter().hasClassificationFilter()) {
                Classification classification = getClassificationService()
                        .load(config.getTaxonNodeFilter().getClassificationFilter().get(0).getUuid());
                state.setRootId(classification.getRootNode().getUuid());

            } else if (config.getTaxonNodeFilter().hasSubtreeFilter()) {
                state.setRootId(config.getTaxonNodeFilter().getSubtreeFilter().get(0).getUuid());
            }
            @SuppressWarnings("unchecked")
            TaxonNodeOutStreamPartitioner<XmlExportState> partitioner = TaxonNodeOutStreamPartitioner.NewInstance(this,
                    state, state.getConfig().getTaxonNodeFilter(), 100, monitor, null);

            handleMetaData(state);
            monitor.subTask("Start partitioning");

            TaxonNode node = partitioner.next();
            while (node != null) {
                handleTaxonNode(state, node);
                node = partitioner.next();
            }
            // get rootNode and create helperObjects
            if (state.getRootId() != null) {
                List<TaxonNodeDto> childrenOfRoot = state.getNodeChildrenMap().get(state.getRootId());

                Comparator<TaxonNodeDto> comp = state.getConfig().getComparator();
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

    private void setOrderIndex(CdmLightExportState state, OrderHelper order) {

        // String sortIndex = StringUtils.isBlank(sort_index)?
        // String.valueOf(order.getOrderIndex()): sort_index+ "_"
        // +String.valueOf(order.getOrderIndex());

        if (order.getTaxonUuid() != null
                && state.getProcessor().hasRecord(CdmLightExportTable.TAXON, order.getTaxonUuid().toString())) {
            String[] csvLine = state.getProcessor().getRecord(CdmLightExportTable.TAXON,
                    order.getTaxonUuid().toString());
            csvLine[CdmLightExportTable.TAXON.getIndex(CdmLightExportTable.SORT_INDEX)] = String
                    .valueOf(order.getOrderIndex());
        }

        if (order.getChildren() == null) {
            return;
        }
        for (OrderHelper helper : order.getChildren()) {
            setOrderIndex(state, helper);
        }
    }

    private List<OrderHelper> createOrderHelper(List<TaxonNodeDto> nodes, CdmLightExportState state) {
        List<TaxonNodeDto> children = nodes;
        // alreadySortedNodes.add(parentUuid);
        if (children == null) {
            return null;
        }
        Comparator<TaxonNodeDto> comp = state.getConfig().getComparator();
        if (comp == null) {
            comp = new TaxonNodeDtoByRankAndNameComparator();
        }
        Collections.sort(children, comp);
        // TODO: nochmal checken!!!
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

    private void handleTaxonNode(CdmLightExportState state, TaxonNode taxonNode) {

        if (taxonNode == null) {
            String message = "TaxonNode for given taxon node UUID not found. ";
            // TODO
            state.getResult().addWarning(message);
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

    private void handleTaxon(CdmLightExportState state, TaxonNode taxonNode) {
        try {

            if (taxonNode == null) {
                state.getResult().addError("The taxonNode was null.", "handleTaxon");
                state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
                return;
            }
            if (taxonNode.getTaxon() == null) {
                state.getResult().addError("There was a taxon node without a taxon: " + taxonNode.getUuid(),
                        "handleTaxon");
                state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
            } else {
                Taxon taxon = HibernateProxyHelper.deproxy(taxonNode.getTaxon(), Taxon.class);

                try {
                    TaxonName name = taxon.getName();
                    handleName(state, name, taxon, true);
                    HomotypicalGroup homotypicGroup = taxon.getHomotypicGroup();
                    int index = 0;
                    int homotypicGroupIndex = 0;
                    handleHomotypicalGroup(state, homotypicGroup, taxon, homotypicGroupIndex);
                    homotypicGroupIndex++;
                    for (Synonym syn : taxon.getSynonymsInGroup(homotypicGroup)) {
                        handleSynonym(state, syn, index);
                        index++;
                    }
                    List<HomotypicalGroup> heterotypicHomotypicGroups = taxon.getHeterotypicSynonymyGroups();
                    for (HomotypicalGroup group: heterotypicHomotypicGroups){
                        handleHomotypicalGroup(state, group, taxon, homotypicGroupIndex);
                        for (Synonym syn : taxon.getSynonymsInGroup(group)) {
                            handleSynonym(state, syn, index);
                            index++;
                        }
                        homotypicGroupIndex++;
                    }

                    index = 0;
                    for (Taxon tax : taxon.getAllProParteSynonyms()) {
                        handleProPartePartialMisapplied(state, tax, taxon, true, false, index);
                        index++;
                    }


                    for (Taxon tax : taxon.getAllMisappliedNames()) {
                        handleProPartePartialMisapplied(state, tax, taxon, false, true, index);
                        index++;
                    }

                    CdmLightExportTable table = CdmLightExportTable.TAXON;
                    String[] csvLine = new String[table.getSize()];

                    csvLine[table.getIndex(CdmLightExportTable.TAXON_ID)] = getId(state, taxon);
                    csvLine[table.getIndex(CdmLightExportTable.NAME_FK)] = getId(state, name);
                    Taxon parent = (taxonNode.getParent() == null) ? null : taxonNode.getParent().getTaxon();
                    csvLine[table.getIndex(CdmLightExportTable.PARENT_FK)] = getId(state, parent);
                    csvLine[table.getIndex(CdmLightExportTable.SEC_REFERENCE_FK)] = getId(state, taxon.getSec());
                    if (taxon.getSec() != null && taxon.getSec().getDatePublished() != null
                            && taxon.getSec().getDatePublished().getFreeText() != null) {
                        String sec_string = taxon.getSec().getTitleCache() + ". "
                                + taxon.getSec().getDatePublished().getFreeText();
                        sec_string = sec_string.replace("..", ".");
                        csvLine[table.getIndex(CdmLightExportTable.SEC_REFERENCE)] = sec_string;
                    } else {
                        csvLine[table.getIndex(CdmLightExportTable.SEC_REFERENCE)] = getTitleCache(taxon.getSec());
                    }
                    if (taxon.getSec() != null) {
                        if (!state.getReferenceStore().contains((taxon.getSec().getUuid()))) {
                            handleReference(state, taxon.getSec());
                        }
                    }
                    csvLine[table.getIndex(CdmLightExportTable.APPENDED_PHRASE)] = taxon.getAppendedPhrase();
                    csvLine[table.getIndex(CdmLightExportTable.CLASSIFICATION_ID)] = getId(state,
                            taxonNode.getClassification());
                    csvLine[table.getIndex(CdmLightExportTable.CLASSIFICATION_TITLE)] = taxonNode.getClassification()
                            .getTitleCache();

                    csvLine[table.getIndex(CdmLightExportTable.PUBLISHED)] = taxon.isPublish() ? "1" : "0";
                    csvLine[table.getIndex(CdmLightExportTable.EXCLUDED)] = taxonNode.isExcluded() ? "1" : "0";
                    Map<Language, LanguageString> notesMap = taxonNode.getStatusNote();
                    String statusNotes = "";
                    if (!notesMap.isEmpty() && notesMap.size() == 1) {
                        statusNotes = notesMap.values().iterator().next().getText();
                    } else if (!notesMap.isEmpty()) {
                        statusNotes = notesMap.get(Language.getDefaultLanguage()) != null
                                ? notesMap.get(Language.getDefaultLanguage()).getText() : null;
                        if (statusNotes == null) {
                            statusNotes = notesMap.values().iterator().next().getText();
                        }
                    }
                    csvLine[table.getIndex(CdmLightExportTable.STATUS_NOTES)] = statusNotes;

                    csvLine[table.getIndex(CdmLightExportTable.UNPLACED)] = taxonNode.isUnplaced() ? "1" : "0";
                    csvLine[table.getIndex(CdmLightExportTable.DOUBTFUL)] = taxonNode.isDoubtful() ? "1" : "0";
                    state.getProcessor().put(table, taxon, csvLine);
                    handleDescriptions(state, taxon);
                } catch (Exception e) {
                    state.getResult().addException(e,
                            "An unexpected problem occurred when trying to export taxon with id " + taxon.getId() + " " + taxon.getTitleCache());
                    state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
                }
            }

            taxonNode.removeNullValueFromChildren();

        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling the taxon node of "
                    + cdmBaseStr(taxonNode.getTaxon()) + ", titleCache:"+ taxonNode.getTaxon().getTitleCache()+": " + e.getMessage());
        }
    }

    private void handleDescriptions(CdmLightExportState state, CdmBase cdmBase) {
        String titleCache = null;
        try {

            if (cdmBase instanceof Taxon) {
                Taxon taxon = HibernateProxyHelper.deproxy(cdmBase, Taxon.class);
                titleCache = taxon.getTitleCache();
                Set<TaxonDescription> descriptions = taxon.getDescriptions();
                List<DescriptionElementBase> simpleFacts = new ArrayList<>();
                List<DescriptionElementBase> specimenFacts = new ArrayList<>();
                List<DescriptionElementBase> distributionFacts = new ArrayList<>();
                List<DescriptionElementBase> taxonInteractionsFacts = new ArrayList<>();
                List<DescriptionElementBase> commonNameFacts = new ArrayList<>();
                List<DescriptionElementBase> usageFacts = new ArrayList<>();
                for (TaxonDescription description : descriptions) {
                    if (description.getElements() != null) {
                        for (DescriptionElementBase element : description.getElements()) {
                            element = CdmBase.deproxy(element);
                            handleAnnotations(element);
                            if (element.getFeature().equals(Feature.COMMON_NAME())) {
                                commonNameFacts.add(element);
                            } else if (element.getFeature().equals(Feature.DISTRIBUTION())) {
                                distributionFacts.add(element);
                            } else if (element instanceof IndividualsAssociation
                                    || isSpecimenFeature(element.getFeature())) {
                                specimenFacts.add(element);
                            } else if (element.getFeature().isSupportsTaxonInteraction()) {
                                taxonInteractionsFacts.add(element);
                            } else {
                                simpleFacts.add(element);
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
                if (!specimenFacts.isEmpty()) {
                    handleSpecimenFacts(state, taxon, specimenFacts);
                }
                if (!simpleFacts.isEmpty()) {
                    handleSimpleFacts(state, taxon, simpleFacts);
                }
                if (!taxonInteractionsFacts.isEmpty()) {
                    handleTaxonInteractionsFacts(state, taxon, taxonInteractionsFacts);
                }
            } else if (cdmBase instanceof TaxonName) {
                TaxonName name = CdmBase.deproxy(cdmBase, TaxonName.class);
                titleCache = name.getTitleCache();
                Set<TaxonNameDescription> descriptions = name.getDescriptions();
                List<DescriptionElementBase> simpleFacts = new ArrayList<>();
                for (TaxonNameDescription description : descriptions) {
                    if (description.getElements() != null) {
                        for (DescriptionElementBase element : description.getElements()) {
                            simpleFacts.add(element);
                        }
                    }
                }
                if (!simpleFacts.isEmpty()) {
                    handleSimpleFacts(state, name, simpleFacts);
                }
            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling description of"
                    + cdmBaseStr(cdmBase) + (titleCache != null? (" " +titleCache) : "")+": " + e.getMessage());
        }
    }

    private void handleAnnotations(DescriptionElementBase element) {
        // TODO Auto-generated method stub
    }

    private void handleMetaData(CdmLightExportState state) {
        CdmLightExportTable table = CdmLightExportTable.METADATA;
        String[] csvLine = new String[table.getSize()];
//        csvLine[table.getIndex(CdmLightExportTable.INSTANCE_ID)] = state.getConfig().getInctanceId();
//        csvLine[table.getIndex(CdmLightExportTable.INSTANCE_NAME)] = state.getConfig().getInstanceName();
        csvLine[table.getIndex(CdmLightExportTable.DATASET_BASE_URL)] = state.getConfig().getBase_url();
        csvLine[table.getIndex(CdmLightExportTable.DATASET_CONTRIBUTOR)] = state.getConfig().getContributor();
        csvLine[table.getIndex(CdmLightExportTable.DATASET_CREATOR)] = state.getConfig().getCreator();
        csvLine[table.getIndex(CdmLightExportTable.DATASET_DESCRIPTION)] = state.getConfig().getDescription();
        csvLine[table.getIndex(CdmLightExportTable.DATASET_DOWNLOAD_LINK)] = state.getConfig().getDataset_download_link();
        csvLine[table.getIndex(CdmLightExportTable.DATASET_KEYWORDS)] = state.getConfig().getKeywords();
        csvLine[table.getIndex(CdmLightExportTable.DATASET_LANDINGPAGE)] = state.getConfig().getDataSet_landing_page();

        csvLine[table.getIndex(CdmLightExportTable.DATASET_LANGUAGE)] = state.getConfig().getLanguage() != null? state.getConfig().getLanguage().getLabel(): null;
        csvLine[table.getIndex(CdmLightExportTable.DATASET_LICENCE)] = state.getConfig().getLicence();
        csvLine[table.getIndex(CdmLightExportTable.DATASET_LOCATION)] = state.getConfig().getLocation();
        csvLine[table.getIndex(CdmLightExportTable.DATASET_RECOMMENDED_CITATTION)] = state.getConfig().getRecommended_citation();
        csvLine[table.getIndex(CdmLightExportTable.DATASET_TITLE)] = state.getConfig().getTitle();
        state.getProcessor().put(table, "", csvLine);
    }

    private boolean isSpecimenFeature(Feature feature) {
        // TODO allow user defined specimen features
        if (feature == null) {
            return false;
        } else if (feature.isSupportsIndividualAssociation()) {
            return true;
        } else {
            return feature.equals(Feature.SPECIMEN()) || feature.equals(Feature.INDIVIDUALS_ASSOCIATION())
                    || feature.equals(Feature.MATERIALS_EXAMINED()) || feature.equals(Feature.OBSERVATION())
                    || feature.equals(Feature.OCCURRENCE());
        }
    }

    private void handleSimpleFacts(CdmLightExportState state, CdmBase cdmBase,
            List<DescriptionElementBase> simpleFacts) {
        String titleCache = null;
        try {
            CdmLightExportTable table;
            if (cdmBase instanceof TaxonName) {
                titleCache = ((TaxonName)cdmBase).getTitleCache();
                table = CdmLightExportTable.NAME_FACT;
            } else {
                if (cdmBase instanceof Taxon){
                    titleCache = ((Taxon)cdmBase).getTitleCache();
                }
                table = CdmLightExportTable.SIMPLE_FACT;
            }
            CdmLightExportTable tableMedia = CdmLightExportTable.MEDIA;
            for (DescriptionElementBase element : simpleFacts) {
                if (element.getModifyingText().isEmpty() && !element.getMedia().isEmpty()) {
                    handleSimpleMediaFact(state, cdmBase, tableMedia, element);
                } else {
                    handleSingleSimpleFact(state, cdmBase, table, element);
                }
            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling simple facts for "
                    + cdmBaseStr(cdmBase) + (titleCache != null? (" " +titleCache) : "")+ ": " + e.getMessage());
        }
    }

    private void handleTaxonInteractionsFacts(CdmLightExportState state, CdmBase cdmBase,
            List<DescriptionElementBase> taxonInteractionsFacts) {
        CdmLightExportTable table = CdmLightExportTable.TAXON_INTERACTION_FACT;
        String titleCache = null;
        if (cdmBase instanceof TaxonBase){
            titleCache = ((TaxonBase)cdmBase).getTitleCache();
        }
        for (DescriptionElementBase element : taxonInteractionsFacts) {

            try {

                String[] csvLine = new String[table.getSize()];

                csvLine[table.getIndex(CdmLightExportTable.FACT_ID)] = getId(state, element);
                handleSource(state, element, table);
                csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = getId(state, cdmBase);
                csvLine[table.getIndex(CdmLightExportTable.TAXON2_FK)] = getId(state,
                        ((TaxonInteraction) element).getTaxon2());
                csvLine[table.getIndex(CdmLightExportTable.DESCRIPTION)] = createMultilanguageString(
                        ((TaxonInteraction) element).getDescription());
                state.getProcessor().put(table, element, csvLine);

            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when handling taxon interaction"
                        + cdmBaseStr(element) + (titleCache != null? (" " +titleCache) : "")+ ": " + e.getMessage());
            }
        }
    }

    private void handleSimpleMediaFact(CdmLightExportState state, CdmBase cdmBase, CdmLightExportTable table,
            DescriptionElementBase element) {
        try {
            String[] csvLine;
            handleSource(state, element, CdmLightExportTable.MEDIA);

            if (element instanceof TextData) {
                TextData textData = (TextData) element;
                csvLine = new String[table.getSize()];
                csvLine[table.getIndex(CdmLightExportTable.FACT_ID)] = getId(state, element);
                if (cdmBase instanceof Taxon) {
                    csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = getId(state, cdmBase);
                    csvLine[table.getIndex(CdmLightExportTable.NAME_FK)] = "";
                } else if (cdmBase instanceof TaxonName) {
                    csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = "";
                    csvLine[table.getIndex(CdmLightExportTable.NAME_FK)] = getId(state, cdmBase);
                }

                String mediaUris = "";
                for (Media media : textData.getMedia()) {
                    String mediaString = extractMediaUris(media.getRepresentations().iterator());
                    if (!StringUtils.isBlank(mediaString)) {
                        mediaUris += mediaString + ";";
                    } else {
                        state.getResult().addWarning("Empty Media object for " + cdmBase.getUserFriendlyTypeName() + " "
                                + cdmBase.getUuid() + " (media: " + media.getUuid() + ")");
                    }
                }
                csvLine[table.getIndex(CdmLightExportTable.MEDIA_URI)] = mediaUris;

            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling single simple fact "
                    + cdmBaseStr(element) + ": " + e.getMessage());
        }

    }

    private void handleSingleSimpleFact(CdmLightExportState state, CdmBase cdmBase, CdmLightExportTable table,
            DescriptionElementBase element) {
        try {
            String[] csvLine;
            handleSource(state, element, CdmLightExportTable.SIMPLE_FACT);

            if (element instanceof TextData) {
                TextData textData = (TextData) element;
                csvLine = new String[table.getSize()];
                csvLine[table.getIndex(CdmLightExportTable.FACT_ID)] = getId(state, element);
                if (cdmBase instanceof Taxon) {
                    csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = getId(state, cdmBase);
                } else if (cdmBase instanceof TaxonName) {
                    csvLine[table.getIndex(CdmLightExportTable.NAME_FK)] = getId(state, cdmBase);
                }
                csvLine[table.getIndex(CdmLightExportTable.FACT_CATEGORY)] = textData.getFeature().getLabel();

                String mediaUris = "";
                for (Media media : textData.getMedia()) {
                    String mediaString = extractMediaUris(media.getRepresentations().iterator());
                    if (!StringUtils.isBlank(mediaString)) {
                        mediaUris += mediaString + ";";
                    } else {
                        state.getResult().addWarning("Empty Media object for uuid: " + cdmBase.getUuid()
                                + " uuid of media: " + media.getUuid());
                    }
                }
                csvLine[table.getIndex(CdmLightExportTable.MEDIA_URI)] = mediaUris;
                if (textData.getFeature().equals(Feature.CITATION())) {
                    state.getProcessor().put(table, textData, csvLine);
                } else if (!textData.getMultilanguageText().isEmpty()) {
                    for (Language language : textData.getMultilanguageText().keySet()) {
                        String[] csvLineLanguage = csvLine.clone();
                        LanguageString langString = textData.getLanguageText(language);
                        String text = langString.getText();
                        if (state.getConfig().isFilterIntextReferences()) {
                            text = filterIntextReferences(langString.getText());
                        }
                        csvLineLanguage[table.getIndex(CdmLightExportTable.FACT_TEXT)] = text;
                        csvLineLanguage[table.getIndex(CdmLightExportTable.LANGUAGE)] = language.getLabel();
                        state.getProcessor().put(table, textData, csvLineLanguage);
                    }
                } else {
                    state.getProcessor().put(table, textData, csvLine);
                }
            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling single simple fact "
                    + cdmBaseStr(element) + ": " + e.getMessage());
        }
    }

    private String filterIntextReferences(String text) {
        /*
         * (<cdm:reference cdmId='fbd19251-efee-4ded-b780-915000f66d41'
         * intextId='1352d42c-e201-4155-a02a-55360d3b563e'>Ridley in Fl. Malay
         * Pen. 3 (1924) 22</cdm:reference>)
         */
        String newText = text.replaceAll("<cdm:reference cdmId='[a-z0-9\\-]*' intextId='[a-z0-9\\-]*'>", "");
        newText = newText.replaceAll("</cdm:reference>", "");

        newText = newText.replaceAll("<cdm:key cdmId='[a-z0-9\\-]*' intextId='[a-z0-9\\-]*'>", "");
        newText = newText.replaceAll("</cdm:key>", "");
        return newText;
    }

    private void handleSpecimenFacts(CdmLightExportState state, Taxon taxon,
            List<DescriptionElementBase> specimenFacts) {
        CdmLightExportTable table = CdmLightExportTable.SPECIMEN_FACT;

        for (DescriptionElementBase element : specimenFacts) {
            try {
                String[] csvLine = new String[table.getSize()];
                csvLine[table.getIndex(CdmLightExportTable.FACT_ID)] = getId(state, element);
                csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = getId(state, taxon);
                handleSource(state, element, table);
                csvLine[table.getIndex(CdmLightExportTable.SPECIMEN_NOTES)] = createAnnotationsString(
                        element.getAnnotations());

                if (element instanceof IndividualsAssociation) {

                    IndividualsAssociation indAssociation = (IndividualsAssociation) element;
                    if (indAssociation.getAssociatedSpecimenOrObservation() == null) {
                        state.getResult()
                                .addWarning("There is an individual association with no specimen associated (Taxon "
                                        + taxon.getTitleCache() + "(" + taxon.getUuid() + "). Could not be exported.");
                        continue;
                    } else {
                        if (!state.getSpecimenStore()
                                .contains((indAssociation.getAssociatedSpecimenOrObservation().getUuid()))) {
                            SpecimenOrObservationBase<?> specimenBase = HibernateProxyHelper.deproxy(
                                    indAssociation.getAssociatedSpecimenOrObservation(),
                                    SpecimenOrObservationBase.class);

                            handleSpecimen(state, specimenBase);
                            csvLine[table.getIndex(CdmLightExportTable.SPECIMEN_FK)] = getId(state,
                                    indAssociation.getAssociatedSpecimenOrObservation());
                        }
                    }
                } else if (element instanceof TextData) {
                    TextData textData = HibernateProxyHelper.deproxy(element, TextData.class);
                    csvLine[table.getIndex(CdmLightExportTable.SPECIMEN_DESCRIPTION)] = createMultilanguageString(
                            textData.getMultilanguageText());
                }
                state.getProcessor().put(table, element, csvLine);
            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when handling single specimen fact "
                        + cdmBaseStr(element) + ": " + e.getMessage());
            }
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

    private void handleSource(CdmLightExportState state, DescriptionElementBase element,
            CdmLightExportTable factsTable) {
        CdmLightExportTable table = CdmLightExportTable.FACT_SOURCES;
        try {
            Set<DescriptionElementSource> sources = element.getSources();

            for (DescriptionElementSource source : sources) {
                if (!(source.getType().equals(OriginalSourceType.Import)
                        && state.getConfig().isFilterImportSources())) {
                    String[] csvLine = new String[table.getSize()];
                    Reference ref = source.getCitation();
                    if ((ref == null) && (source.getNameUsedInSource() == null)) {
                        continue;
                    }
                    if (ref != null) {
                        if (!state.getReferenceStore().contains(ref.getUuid())) {
                            handleReference(state, ref);

                        }
                        csvLine[table.getIndex(CdmLightExportTable.REFERENCE_FK)] = getId(state, ref);
                    }
                    csvLine[table.getIndex(CdmLightExportTable.FACT_FK)] = getId(state, element);

                    csvLine[table.getIndex(CdmLightExportTable.NAME_IN_SOURCE_FK)] = getId(state,
                            source.getNameUsedInSource());
                    csvLine[table.getIndex(CdmLightExportTable.FACT_TYPE)] = factsTable.getTableName();
                    if (StringUtils.isBlank(csvLine[table.getIndex(CdmLightExportTable.REFERENCE_FK)])
                            && StringUtils.isBlank(csvLine[table.getIndex(CdmLightExportTable.NAME_IN_SOURCE_FK)])) {
                        continue;
                    }
                    state.getProcessor().put(table, source, csvLine);
                }

            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling single source "
                    + cdmBaseStr(element) + ": " + e.getMessage());
        }

    }

    private void handleDistributionFacts(CdmLightExportState state, Taxon taxon,
            List<DescriptionElementBase> distributionFacts) {

        CdmLightExportTable table = CdmLightExportTable.GEOGRAPHIC_AREA_FACT;
        Set<Distribution> distributions = new HashSet<>();
        for (DescriptionElementBase element : distributionFacts) {
            try {
                if (element instanceof Distribution) {
                    String[] csvLine = new String[table.getSize()];
                    Distribution distribution = (Distribution) element;
                    distributions.add(distribution);
                    csvLine[table.getIndex(CdmLightExportTable.FACT_ID)] = getId(state, element);
                    handleSource(state, element, table);
                    csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = getId(state, taxon);
                    if (distribution.getArea() != null) {
                        csvLine[table.getIndex(CdmLightExportTable.AREA_LABEL)] = distribution.getArea().getLabel();
                    }
                    if (distribution.getStatus() != null) {
                        csvLine[table.getIndex(CdmLightExportTable.STATUS_LABEL)] = distribution.getStatus().getLabel();
                    }
                    state.getProcessor().put(table, distribution, csvLine);
                } else {
                    state.getResult()
                            .addError("The distribution description for the taxon " + taxon.getUuid()
                                    + " is not of type distribution. Could not be exported. UUID of the description element: "
                                    + element.getUuid());
                }
            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when handling single distribution "
                        + cdmBaseStr(element) + ": " + e.getMessage());
            }
        }
         if(state.getConfig().isCreateCondensedDistributionString()){
             List<Language> langs = new ArrayList<>();
             langs.add(Language.ENGLISH());

             CondensedDistribution conDis = geoService.getCondensedDistribution(
                     //TODO add CondensedDistributionConfiguration to export configuration
                     distributions, true, null, state.getConfig().getCondensedDistributionRecipe().toConfiguration(), langs);
             CdmLightExportTable tableCondensed =
                     CdmLightExportTable.SIMPLE_FACT;
             String[] csvLine = new String[tableCondensed.getSize()];
             //the computed fact has no uuid, TODO: remember the uuid for later reference assignment
             UUID randomUuid = UUID.randomUUID();
             csvLine[tableCondensed.getIndex(CdmLightExportTable.FACT_ID)] =
                     randomUuid.toString();
             csvLine[tableCondensed.getIndex(CdmLightExportTable.TAXON_FK)] =
                     getId(state, taxon);
             csvLine[tableCondensed.getIndex(CdmLightExportTable.FACT_TEXT)] =
                     conDis.toString();
             csvLine[tableCondensed.getIndex(CdmLightExportTable.LANGUAGE)] =Language.ENGLISH().toString();

             csvLine[tableCondensed.getIndex(CdmLightExportTable.FACT_CATEGORY)] =
                     "CondensedDistribution";

             state.getProcessor().put(tableCondensed, taxon, csvLine);
         }
    }

    private void handleCommonNameFacts(CdmLightExportState state, Taxon taxon,
            List<DescriptionElementBase> commonNameFacts) {
        CdmLightExportTable table = CdmLightExportTable.COMMON_NAME_FACT;

        for (DescriptionElementBase element : commonNameFacts) {
            try {
                if (element instanceof CommonTaxonName) {
                    String[] csvLine = new String[table.getSize()];
                    CommonTaxonName commonName = (CommonTaxonName) element;
                    csvLine[table.getIndex(CdmLightExportTable.FACT_ID)] = getId(state, element);
                    handleSource(state, element, table);
                    csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = getId(state, taxon);
                    if (commonName.getName() != null) {
                        csvLine[table.getIndex(CdmLightExportTable.FACT_TEXT)] = commonName.getName();
                    }
                    if (commonName.getLanguage() != null) {
                        csvLine[table.getIndex(CdmLightExportTable.LANGUAGE)] = commonName.getLanguage().getLabel();
                    }
                    if (commonName.getArea() != null) {
                        csvLine[table.getIndex(CdmLightExportTable.AREA_LABEL)] = commonName.getArea().getLabel();
                    }
                    state.getProcessor().put(table, commonName, csvLine);
                } else if (element instanceof TextData){
                    String[] csvLine = new String[table.getSize()];
                    TextData commonName = (TextData) element;
                    csvLine[table.getIndex(CdmLightExportTable.FACT_ID)] = getId(state, element);
                    handleSource(state, element, table);
                    csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = getId(state, taxon);
                    if (commonName.getMultilanguageText() != null) {
                        csvLine[table.getIndex(CdmLightExportTable.FACT_TEXT)] = createMultilanguageString(commonName.getMultilanguageText());
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
        // TODO refresh?
        return identEntity.getTitleCache();
    }

    private String getId(CdmLightExportState state, ICdmBase cdmBase) {
        if (cdmBase == null) {
            return "";
        }
        // TODO make configurable
        return cdmBase.getUuid().toString();
    }

    private void handleSynonym(CdmLightExportState state, Synonym synonym, int index) {
        try {
            if (isUnpublished(state.getConfig(), synonym)) {
                return;
            }
            TaxonName name = synonym.getName();
            handleName(state, name, synonym.getAcceptedTaxon());

            CdmLightExportTable table = CdmLightExportTable.SYNONYM;
            String[] csvLine = new String[table.getSize()];

            csvLine[table.getIndex(CdmLightExportTable.SYNONYM_ID)] = getId(state, synonym);
            csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = getId(state, synonym.getAcceptedTaxon());
            csvLine[table.getIndex(CdmLightExportTable.NAME_FK)] = getId(state, name);
            if (synonym.getSec() != null && !state.getReferenceStore().contains(synonym.getSec().getUuid())) {
                handleReference(state, synonym.getSec());
            }
            csvLine[table.getIndex(CdmLightExportTable.APPENDED_PHRASE)] = synonym.getAppendedPhrase();
            csvLine[table.getIndex(CdmLightExportTable.SYN_SEC_REFERENCE_FK)] = getId(state, synonym.getSec());
            csvLine[table.getIndex(CdmLightExportTable.SYN_SEC_REFERENCE)] = getTitleCache(synonym.getSec());
            csvLine[table.getIndex(CdmLightExportTable.PUBLISHED)] = synonym.isPublish() ? "1" : "0";
            csvLine[table.getIndex(CdmLightExportTable.IS_PRO_PARTE)] = "0";
            csvLine[table.getIndex(CdmLightExportTable.IS_PARTIAL)] = "0";
            csvLine[table.getIndex(CdmLightExportTable.IS_MISAPPLIED)] = "0";
            csvLine[table.getIndex(CdmLightExportTable.SORT_INDEX)] = String.valueOf(index);
            state.getProcessor().put(table, synonym, csvLine);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling synonym "
                    + cdmBaseStr(synonym) + ": " + e.getMessage());
        }
    }

    /**
     * Handles Misapplied names (including pro parte and partial as well as pro
     * parte and partial synonyms
     *
     * @param state
     * @param rel
     */
    private void handleProPartePartialMisapplied(CdmLightExportState state, Taxon taxon, Taxon accepted, boolean isProParte, boolean isMisapplied, int index) {
        try {
            Taxon ppSyonym = taxon;
            if (isUnpublished(state.getConfig(), ppSyonym)) {
                return;
            }
            TaxonName name = ppSyonym.getName();
            handleName(state, name, accepted);

            CdmLightExportTable table = CdmLightExportTable.SYNONYM;
            String[] csvLine = new String[table.getSize()];

            csvLine[table.getIndex(CdmLightExportTable.SYNONYM_ID)] = getId(state, ppSyonym);
            csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = getId(state, accepted);
            csvLine[table.getIndex(CdmLightExportTable.NAME_FK)] = getId(state, name);

            Reference secRef = ppSyonym.getSec();

            if (secRef != null && !state.getReferenceStore().contains(secRef.getUuid())) {
                handleReference(state, secRef);
            }
            csvLine[table.getIndex(CdmLightExportTable.SEC_REFERENCE_FK)] = getId(state, secRef);
            csvLine[table.getIndex(CdmLightExportTable.SEC_REFERENCE)] = getTitleCache(secRef);
            Set<TaxonRelationship> rels = accepted.getTaxonRelations(ppSyonym);
            TaxonRelationship rel = null;
            boolean isPartial = false;
            if (rels.size() == 1){
                rel = rels.iterator().next();

            }else if (rels.size() > 1){
                Iterator<TaxonRelationship> iterator = rels.iterator();
                while (iterator.hasNext()){
                    rel = iterator.next();
                    if (isProParte && rel.getType().isAnySynonym()){
                        break;
                    } else if (isMisapplied && rel.getType().isAnyMisappliedName()){
                        break;
                    }else{
                        rel = null;
                    }
                }
            }
            if (rel != null){
                Reference synSecRef = rel.getCitation();
                if (synSecRef != null && !state.getReferenceStore().contains(synSecRef.getUuid())) {
                    handleReference(state, synSecRef);
                }
                csvLine[table.getIndex(CdmLightExportTable.SYN_SEC_REFERENCE_FK)] = getId(state, synSecRef);
                csvLine[table.getIndex(CdmLightExportTable.SYN_SEC_REFERENCE)] = getTitleCache(synSecRef);
                isProParte = rel.getType().isProParte();
                isPartial = rel.getType().isPartial();

            }else{
                state.getResult().addWarning("An unexpected error occurred when handling "
                        + "pro parte/partial synonym or misapplied name  " + cdmBaseStr(taxon) );
            }

            // pro parte type

            csvLine[table.getIndex(CdmLightExportTable.IS_PRO_PARTE)] = isProParte ? "1" : "0";
            csvLine[table.getIndex(CdmLightExportTable.IS_PARTIAL)] = isPartial ? "1" : "0";
            csvLine[table.getIndex(CdmLightExportTable.IS_MISAPPLIED)] = isMisapplied ? "1" : "0";
            csvLine[table.getIndex(CdmLightExportTable.SORT_INDEX)] = String.valueOf(index);
            state.getProcessor().put(table, ppSyonym, csvLine);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling "
                    + "pro parte/partial synonym or misapplied name  " + cdmBaseStr(taxon) + ": " + e.getMessage());
        }

    }

    private void handleName(CdmLightExportState state, TaxonName name, Taxon acceptedTaxon){
        handleName(state, name, acceptedTaxon, false);
    }

    private void handleName(CdmLightExportState state, TaxonName name, Taxon acceptedTaxon, boolean acceptedName) {
        if (name == null || state.getNameStore().containsKey(name.getId())) {
            return;
        }
        try {
            Rank rank = name.getRank();
            CdmLightExportTable table = CdmLightExportTable.SCIENTIFIC_NAME;
            name = HibernateProxyHelper.deproxy(name);
            state.getNameStore().put(name.getId(), name.getUuid());
            String[] csvLine = new String[table.getSize()];

            csvLine[table.getIndex(CdmLightExportTable.NAME_ID)] = getId(state, name);
            if (name.getLsid() != null) {
                csvLine[table.getIndex(CdmLightExportTable.LSID)] = name.getLsid().getLsid();
            } else {
                csvLine[table.getIndex(CdmLightExportTable.LSID)] = "";
            }

            handleIdentifier(state, name);
            handleDescriptions(state, name);

            csvLine[table.getIndex(CdmLightExportTable.RANK)] = getTitleCache(rank);
            if (rank != null) {
                csvLine[table.getIndex(CdmLightExportTable.RANK_SEQUENCE)] = String.valueOf(rank.getOrderIndex());
                if (rank.isInfraGeneric()) {
                    try {
                        csvLine[table.getIndex(CdmLightExportTable.INFRAGENERIC_RANK)] = name.getRank()
                                .getInfraGenericMarker();
                    } catch (UnknownCdmTypeException e) {
                        state.getResult().addError("Infrageneric marker expected but not available for rank "
                                + name.getRank().getTitleCache());
                    }
                }
                if (rank.isInfraSpecific()) {
                    csvLine[table.getIndex(CdmLightExportTable.INFRASPECIFIC_RANK)] = name.getRank().getAbbreviation();
                }
            } else {
                csvLine[table.getIndex(CdmLightExportTable.RANK_SEQUENCE)] = "";
            }
            if (name.isProtectedTitleCache()) {
                csvLine[table.getIndex(CdmLightExportTable.FULL_NAME_WITH_AUTHORS)] = name.getTitleCache();
            } else {
                // TODO: adapt the tropicos titlecache creation
                csvLine[table.getIndex(CdmLightExportTable.FULL_NAME_WITH_AUTHORS)] = name.getTitleCache();
            }


            if (!state.getConfig().isAddHTML()) {
                csvLine[table.getIndex(CdmLightExportTable.FULL_NAME_WITH_REF)] = name.getFullTitleCache();
            } else {
                List<TaggedText> taggedFullTitleCache = name.getTaggedFullTitle();
                String fullTitleWithHtml = createNameWithItalics(taggedFullTitleCache);
                // TODO: adapt the tropicos titlecache creation
                csvLine[table.getIndex(CdmLightExportTable.FULL_NAME_WITH_REF)] = fullTitleWithHtml.trim();
            }

            csvLine[table.getIndex(CdmLightExportTable.FULL_NAME_NO_AUTHORS)] = name.getNameCache();
            csvLine[table.getIndex(CdmLightExportTable.GENUS_UNINOMIAL)] = name.getGenusOrUninomial();

            csvLine[table.getIndex(CdmLightExportTable.INFRAGENERIC_EPITHET)] = name.getInfraGenericEpithet();
            csvLine[table.getIndex(CdmLightExportTable.SPECIFIC_EPITHET)] = name.getSpecificEpithet();

            csvLine[table.getIndex(CdmLightExportTable.INFRASPECIFIC_EPITHET)] = name.getInfraSpecificEpithet();

            csvLine[table.getIndex(CdmLightExportTable.APPENDED_PHRASE)] = name.getAppendedPhrase();

            csvLine[table.getIndex(CdmLightExportTable.BAS_AUTHORTEAM_FK)] = getId(state, name.getBasionymAuthorship());
            if (name.getBasionymAuthorship() != null) {
                if (state.getAuthorFromStore(name.getBasionymAuthorship().getId()) == null) {
                    handleAuthor(state, name.getBasionymAuthorship());
                }
            }
            csvLine[table.getIndex(CdmLightExportTable.BAS_EX_AUTHORTEAM_FK)] = getId(state,
                    name.getExBasionymAuthorship());
            if (name.getExBasionymAuthorship() != null) {
                if (state.getAuthorFromStore(name.getExBasionymAuthorship().getId()) == null) {
                    handleAuthor(state, name.getExBasionymAuthorship());
                }

            }
            csvLine[table.getIndex(CdmLightExportTable.COMB_AUTHORTEAM_FK)] = getId(state,
                    name.getCombinationAuthorship());
            if (name.getCombinationAuthorship() != null) {
                if (state.getAuthorFromStore(name.getCombinationAuthorship().getId()) == null) {
                    handleAuthor(state, name.getCombinationAuthorship());
                }
            }
            csvLine[table.getIndex(CdmLightExportTable.COMB_EX_AUTHORTEAM_FK)] = getId(state,
                    name.getExCombinationAuthorship());
            if (name.getExCombinationAuthorship() != null) {
                if (state.getAuthorFromStore(name.getExCombinationAuthorship().getId()) == null) {
                    handleAuthor(state, name.getExCombinationAuthorship());
                }

            }

            csvLine[table.getIndex(CdmLightExportTable.AUTHOR_TEAM_STRING)] = name.getAuthorshipCache();

            Reference nomRef = name.getNomenclaturalReference();

            NomenclaturalSource nomenclaturalSource = name.getNomenclaturalSource();
            if (nomenclaturalSource != null &&nomenclaturalSource.getNameUsedInSource() != null){
                handleName(state, nomenclaturalSource.getNameUsedInSource(), null);
                csvLine[table.getIndex(CdmLightExportTable.NAME_USED_IN_SOURCE)] = getId(state, nomenclaturalSource.getNameUsedInSource());
            }

            if (nomRef != null) {
                if (!state.getReferenceStore().contains(nomRef.getUuid())) {
                    handleReference(state, nomRef);
                }
                csvLine[table.getIndex(CdmLightExportTable.REFERENCE_FK)] = getId(state, nomRef);
                csvLine[table.getIndex(CdmLightExportTable.PUBLICATION_TYPE)] = nomRef.getType().name();
                if (nomRef.getVolume() != null) {
                    csvLine[table.getIndex(CdmLightExportTable.VOLUME_ISSUE)] = nomRef.getVolume();
                    csvLine[table.getIndex(CdmLightExportTable.COLLATION)] = createCollatation(name);
                }
                if (nomRef.getDatePublished() != null) {
                    csvLine[table.getIndex(CdmLightExportTable.DATE_PUBLISHED)] = nomRef.getTimePeriodPublishedString();
                    csvLine[table.getIndex(CdmLightExportTable.YEAR_PUBLISHED)] = nomRef.getDatePublished().getYear();
                    csvLine[table.getIndex(CdmLightExportTable.VERBATIM_DATE)] = nomRef.getDatePublished()
                            .getVerbatimDate();
                }
                if (name.getNomenclaturalMicroReference() != null) {
                    csvLine[table.getIndex(CdmLightExportTable.DETAIL)] = name.getNomenclaturalMicroReference();
                }
                nomRef = HibernateProxyHelper.deproxy(nomRef);
                if (nomRef.getInReference() != null) {
                    Reference inReference = nomRef.getInReference();
                    if (inReference.getDatePublished() != null && nomRef.getDatePublished() == null) {
                        csvLine[table.getIndex(CdmLightExportTable.DATE_PUBLISHED)] = inReference
                                .getDatePublishedString();
                        csvLine[table.getIndex(CdmLightExportTable.YEAR_PUBLISHED)] = inReference.getDatePublished()
                                .getYear();
                    }
                    if (nomRef.getVolume() == null && inReference.getVolume() != null) {
                        csvLine[table.getIndex(CdmLightExportTable.VOLUME_ISSUE)] = inReference.getVolume();
                        csvLine[table.getIndex(CdmLightExportTable.COLLATION)] = createCollatation(name);
                    }
                    if (inReference.getInReference() != null) {
                        inReference = inReference.getInReference();
                    }
                    if (inReference.getAbbrevTitle() == null) {
                        csvLine[table.getIndex(CdmLightExportTable.ABBREV_TITLE)] = CdmUtils
                                .Nz(inReference.getTitle());
                    } else {
                        csvLine[table.getIndex(CdmLightExportTable.ABBREV_TITLE)] = CdmUtils
                                .Nz(inReference.getAbbrevTitle());
                    }
                    if (inReference.getTitle() == null) {
                        csvLine[table.getIndex(CdmLightExportTable.FULL_TITLE)] = CdmUtils
                                .Nz(inReference.getAbbrevTitle()!= null? inReference.getAbbrevTitle(): inReference.getTitleCache());
                    } else {
                        csvLine[table.getIndex(CdmLightExportTable.FULL_TITLE)] = CdmUtils.Nz(inReference.getTitle());
                    }

                    TeamOrPersonBase<?> author = inReference.getAuthorship();
                    if (author != null
                            && (nomRef.isOfType(ReferenceType.BookSection) || nomRef.isOfType(ReferenceType.Section))) {
                        csvLine[table.getIndex(CdmLightExportTable.ABBREV_REF_AUTHOR)] = author.isProtectedTitleCache()
                                ? author.getTitleCache() : CdmUtils.Nz(author.getNomenclaturalTitle());
                        csvLine[table.getIndex(CdmLightExportTable.FULL_REF_AUTHOR)] = CdmUtils
                                .Nz(author.getTitleCache());
                    } else {
                        csvLine[table.getIndex(CdmLightExportTable.ABBREV_REF_AUTHOR)] = "";
                        csvLine[table.getIndex(CdmLightExportTable.FULL_REF_AUTHOR)] = "";
                    }
                } else {
                    if (nomRef.getAbbrevTitle() == null) {
                        csvLine[table.getIndex(CdmLightExportTable.ABBREV_TITLE)] = CdmUtils
                                .Nz(nomRef.getTitle()!= null? nomRef.getTitle():nomRef.getAbbrevTitleCache());
                    } else {
                        csvLine[table.getIndex(CdmLightExportTable.ABBREV_TITLE)] = CdmUtils
                                .Nz(nomRef.getAbbrevTitle());
                    }
                    if (nomRef.getTitle() == null) {
                        csvLine[table.getIndex(CdmLightExportTable.FULL_TITLE)] =  CdmUtils
                                .Nz(nomRef.getAbbrevTitle()!= null? nomRef.getAbbrevTitle(): nomRef.getTitleCache());
                    } else {
                        csvLine[table.getIndex(CdmLightExportTable.FULL_TITLE)] = CdmUtils.Nz(nomRef.getTitle());
                    }
                    TeamOrPersonBase<?> author = nomRef.getAuthorship();
                    if (author != null) {
                        csvLine[table.getIndex(CdmLightExportTable.ABBREV_REF_AUTHOR)] = author.isProtectedTitleCache()
                                ? author.getTitleCache() : CdmUtils.Nz(author.getNomenclaturalTitle());
                        csvLine[table.getIndex(CdmLightExportTable.FULL_REF_AUTHOR)] = CdmUtils
                                .Nz(author.getTitleCache());
                    } else {
                        csvLine[table.getIndex(CdmLightExportTable.ABBREV_REF_AUTHOR)] = "";
                        csvLine[table.getIndex(CdmLightExportTable.FULL_REF_AUTHOR)] = "";
                    }

                }
            } else {
                csvLine[table.getIndex(CdmLightExportTable.PUBLICATION_TYPE)] = "";
            }

            /*
             * Collation
             *
             * Detail
             *
             * TitlePageYear
             */
            String protologueUriString = extractProtologueURIs(state, name);

            csvLine[table.getIndex(CdmLightExportTable.PROTOLOGUE_URI)] = protologueUriString;
            Collection<TypeDesignationBase> specimenTypeDesignations = new ArrayList<>();
            List<TextualTypeDesignation> textualTypeDesignations = new ArrayList<>();
            for (TypeDesignationBase<?> typeDesignation : name.getTypeDesignations()) {
                if (typeDesignation.isInstanceOf(TextualTypeDesignation.class)) {

                    if (((TextualTypeDesignation) typeDesignation).isVerbatim() ){
                        Set<IdentifiableSource> sources =  typeDesignation.getSources();
                        boolean isProtologue = false;
                        if (sources != null && !sources.isEmpty()){
                            IdentifiableSource source = sources.iterator().next();
                            if (name.getNomenclaturalReference() != null){
                                isProtologue = source.getCitation() != null? source.getCitation().getUuid().equals(name.getNomenclaturalReference().getUuid()): false;
                            }
                        }
                        if (isProtologue){
                            csvLine[table.getIndex(CdmLightExportTable.PROTOLOGUE_TYPE_STATEMENT)] = ((TextualTypeDesignation) typeDesignation)
                                    .getPreferredText(Language.DEFAULT());
                        }else{
                            textualTypeDesignations.add((TextualTypeDesignation) typeDesignation);
                        }

                    } else {
                        textualTypeDesignations.add((TextualTypeDesignation) typeDesignation);
                    }
                } else if (typeDesignation.isInstanceOf(SpecimenTypeDesignation.class)) {

                    specimenTypeDesignations.add(HibernateProxyHelper.deproxy(typeDesignation, SpecimenTypeDesignation.class));

                }else if (typeDesignation instanceof NameTypeDesignation){
                    specimenTypeDesignations.add(HibernateProxyHelper.deproxy(typeDesignation, NameTypeDesignation.class));
                }
            }
            TypeDesignationSetManager manager = new TypeDesignationSetManager(specimenTypeDesignations, name);
            HTMLTagRules rules = new HTMLTagRules();
            rules.addRule(TagEnum.name, "i");
            String test = manager.print(false, false, false, rules);;
            csvLine[table.getIndex(CdmLightExportTable.TYPE_SPECIMEN)] = manager.print(false, false, false, rules);

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
            csvLine[table.getIndex(CdmLightExportTable.TYPE_STATEMENT)] = stringbuilder.toString();


            if (name.getStatus() == null || name.getStatus().isEmpty()) {
                csvLine[table.getIndex(CdmLightExportTable.NOM_STATUS)] = "";
                csvLine[table.getIndex(CdmLightExportTable.NOM_STATUS_ABBREV)] = "";
            } else {

                String statusStringAbbrev = extractStatusString(state, name, true);
                String statusString = extractStatusString(state, name, false);

                csvLine[table.getIndex(CdmLightExportTable.NOM_STATUS)] = statusString.trim();
                csvLine[table.getIndex(CdmLightExportTable.NOM_STATUS_ABBREV)] = statusStringAbbrev.trim();
            }

            HomotypicalGroup group = HibernateProxyHelper.deproxy(name.getHomotypicalGroup(), HomotypicalGroup.class);

            csvLine[table.getIndex(CdmLightExportTable.HOMOTYPIC_GROUP_FK)] = getId(state, group);
            List<TaxonName> typifiedNames = new ArrayList<>();
            if (acceptedTaxon != null){
                HomotypicGroupTaxonComparator comparator = new HomotypicGroupTaxonComparator(acceptedTaxon);
                List<Synonym> synonymsInGroup = null;
                if (group.equals(acceptedTaxon.getHomotypicGroup())){
                    synonymsInGroup = acceptedTaxon.getHomotypicSynonymsByHomotypicGroup(comparator);
                    typifiedNames.add(name);
                }else{
                    synonymsInGroup = acceptedTaxon.getSynonymsInGroup(group, comparator);
                }

                synonymsInGroup.stream().forEach(synonym -> typifiedNames.add(HibernateProxyHelper.deproxy(synonym.getName(), TaxonName.class)));

            }else{
                typifiedNames.addAll(group.getTypifiedNames());
            }


            Integer seqNumber = typifiedNames.indexOf(name);
            csvLine[table.getIndex(CdmLightExportTable.HOMOTYPIC_GROUP_SEQ)] = String.valueOf(seqNumber);
            state.getProcessor().put(table, name, csvLine);
            handleNameRelationships(state, name);

        } catch (Exception e) {
            state.getResult().addException(e,
                    "An unexpected error occurred when handling the name " + cdmBaseStr(name) + ": " + name.getTitleCache() + ": " + e.getMessage());

            e.printStackTrace();
        }
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

    private void handleNameRelationships(CdmLightExportState state, TaxonName name) {
        Set<NameRelationship> rels = name.getRelationsFromThisName();
        CdmLightExportTable table = CdmLightExportTable.NAME_RELATIONSHIP;
        String[] csvLine = new String[table.getSize()];

        for (NameRelationship rel : rels) {
            NameRelationshipType type = rel.getType();
            TaxonName name2 = rel.getToName();
            name2 = HibernateProxyHelper.deproxy(name2, TaxonName.class);
            if (!state.getNameStore().containsKey(name2.getId())) {
                handleName(state, name2, null);
            }

            csvLine[table.getIndex(CdmLightExportTable.NAME_REL_TYPE)] = type.getLabel();
            csvLine[table.getIndex(CdmLightExportTable.NAME1_FK)] = getId(state, name);
            csvLine[table.getIndex(CdmLightExportTable.NAME2_FK)] = getId(state, name2);
            state.getProcessor().put(table, name, csvLine);
        }

        rels = name.getRelationsToThisName();

        csvLine = new String[table.getSize()];

        for (NameRelationship rel : rels) {
            NameRelationshipType type = rel.getType();
            TaxonName name2 = rel.getFromName();
            name2 = HibernateProxyHelper.deproxy(name2, TaxonName.class);
            if (!state.getNameStore().containsKey(name2.getId())) {
                handleName(state, name2, null);
            }


        }
    }

    private String createCollatation(TaxonName name) {
        String collation = "";
        if (name.getNomenclaturalReference() != null) {
            Reference ref = name.getNomenclaturalReference();
            collation = getVolume(ref);
        }
        if (name.getNomenclaturalMicroReference() != null) {
            if (!StringUtils.isBlank(collation)) {
                collation += ":";
            }
            collation += name.getNomenclaturalMicroReference();
        }

        return collation;
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

    private void handleIdentifier(CdmLightExportState state, CdmBase cdmBase) {
        CdmLightExportTable table = CdmLightExportTable.IDENTIFIER;
        String[] csvLine;
        try {
            if (cdmBase instanceof TaxonName){
                TaxonName name = (TaxonName)cdmBase;

                try{
                    Set<String> IPNIidentifiers = name.getIdentifiers(DefinedTerm.IDENTIFIER_NAME_IPNI());
                    Set<String> tropicosIdentifiers = name.getIdentifiers(DefinedTerm.IDENTIFIER_NAME_TROPICOS());
                    Set<String> WFOIdentifiers = name.getIdentifiers(DefinedTerm.uuidWfoNameIdentifier);
                    if (!IPNIidentifiers.isEmpty()) {
                        csvLine = new String[table.getSize()];
                        csvLine[table.getIndex(CdmLightExportTable.FK)] = getId(state, name);
                        csvLine[table.getIndex(CdmLightExportTable.REF_TABLE)] = "ScientificName";
                        csvLine[table.getIndex(CdmLightExportTable.IDENTIFIER_TYPE)] = IPNI_NAME_IDENTIFIER;
                        csvLine[table.getIndex(CdmLightExportTable.EXTERNAL_NAME_IDENTIFIER)] = extractIdentifier(
                                IPNIidentifiers);
                        state.getProcessor().put(table, name.getUuid() + ", " + IPNI_NAME_IDENTIFIER, csvLine);
                    }
                    if (!tropicosIdentifiers.isEmpty()) {
                        csvLine = new String[table.getSize()];
                        csvLine[table.getIndex(CdmLightExportTable.FK)] = getId(state, name);
                        csvLine[table.getIndex(CdmLightExportTable.REF_TABLE)] = "ScientificName";
                        csvLine[table.getIndex(CdmLightExportTable.IDENTIFIER_TYPE)] = name.getUuid() + ", " + IPNI_NAME_IDENTIFIER;
                        csvLine[table.getIndex(CdmLightExportTable.EXTERNAL_NAME_IDENTIFIER)] = extractIdentifier(
                                tropicosIdentifiers);
                        state.getProcessor().put(table, name.getUuid() + ", " + IPNI_NAME_IDENTIFIER, csvLine);
                    }
                    if (!WFOIdentifiers.isEmpty()) {
                        csvLine = new String[table.getSize()];
                        csvLine[table.getIndex(CdmLightExportTable.FK)] = getId(state, name);
                        csvLine[table.getIndex(CdmLightExportTable.REF_TABLE)] = "ScientificName";
                        csvLine[table.getIndex(CdmLightExportTable.IDENTIFIER_TYPE)] = WFO_NAME_IDENTIFIER;
                        csvLine[table.getIndex(CdmLightExportTable.EXTERNAL_NAME_IDENTIFIER)] = extractIdentifier(
                                WFOIdentifiers);
                        state.getProcessor().put(table, name.getUuid() + ", " + WFO_NAME_IDENTIFIER, csvLine);
                    }
                }catch(Exception e){
                    state.getResult().addWarning("Please check the identifiers for "
                            + cdmBaseStr(cdmBase) + " maybe there is an empty identifier");


                }
            }else{
                if (cdmBase instanceof IdentifiableEntity){
                    IdentifiableEntity<?> identifiableEntity = (IdentifiableEntity<?>) cdmBase;
                    List<Identifier> identifiers = identifiableEntity.getIdentifiers();
                    String tableName = null;
                    if (cdmBase instanceof Reference){
                        tableName = "Reference";
                    }else if (cdmBase instanceof SpecimenOrObservationBase){
                        tableName = "Specimen";
                    }else if (cdmBase instanceof Taxon){
                        tableName = "Taxon";
                    }else if (cdmBase instanceof Synonym){
                        tableName = "Synonym";
                    }else if (cdmBase instanceof TeamOrPersonBase){
                        tableName = "PersonOrTeam";
                    }

                    for (Identifier identifier: identifiers){
                        if (identifier.getType() == null && identifier.getIdentifier() == null){
                            state.getResult().addWarning("Please check the identifiers for "
                                    + cdmBaseStr(cdmBase) + " there is an empty identifier");
                            continue;
                        }

                        csvLine = new String[table.getSize()];
                        csvLine[table.getIndex(CdmLightExportTable.FK)] = getId(state, cdmBase);

                        if (tableName != null){
                            csvLine[table.getIndex(CdmLightExportTable.REF_TABLE)] = tableName;
                            csvLine[table.getIndex(CdmLightExportTable.IDENTIFIER_TYPE)] = identifier.getType() != null? identifier.getType().getLabel():null;
                            csvLine[table.getIndex(CdmLightExportTable.EXTERNAL_NAME_IDENTIFIER)] = identifier.getIdentifier();
                            state.getProcessor().put(table, cdmBase.getUuid() + (identifier.getType() != null? identifier.getType().getLabel():null), csvLine);
                        }
                    }
                    if (cdmBase instanceof Reference ){
                        Reference ref = (Reference)cdmBase;
                        if (ref.getDoi() != null){
                            csvLine = new String[table.getSize()];
                            csvLine[table.getIndex(CdmLightExportTable.FK)] = getId(state, cdmBase);
                            csvLine[table.getIndex(CdmLightExportTable.REF_TABLE)] = tableName;
                            csvLine[table.getIndex(CdmLightExportTable.IDENTIFIER_TYPE)] = "DOI";
                            csvLine[table.getIndex(CdmLightExportTable.EXTERNAL_NAME_IDENTIFIER)] = ref.getDoiString();
                            state.getProcessor().put(table, cdmBase.getUuid() + "DOI", csvLine);
                        }
                    }

                    if (cdmBase instanceof TeamOrPersonBase){
                        TeamOrPersonBase<?> person= HibernateProxyHelper.deproxy(cdmBase, TeamOrPersonBase.class);
                        if (person instanceof Person &&  ((Person)person).getOrcid() != null){
                            csvLine = new String[table.getSize()];
                            csvLine[table.getIndex(CdmLightExportTable.FK)] = getId(state, cdmBase);
                            csvLine[table.getIndex(CdmLightExportTable.REF_TABLE)] = tableName;
                            csvLine[table.getIndex(CdmLightExportTable.IDENTIFIER_TYPE)] = "ORCID";
                            csvLine[table.getIndex(CdmLightExportTable.EXTERNAL_NAME_IDENTIFIER)]=  ((Person)person).getOrcid().asURI();
                            state.getProcessor().put(table, cdmBase.getUuid() + "ORCID", csvLine);
                        }
                    }
                }
            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling identifiers for "
                    + cdmBaseStr(cdmBase) + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractIdentifier(Set<String> identifierSet) {

        String identifierString = "";
        for (String identifier : identifierSet) {
            if (!StringUtils.isBlank(identifierString)) {
                identifierString += ", ";
            }
            identifierString += identifier;
        }
        return identifierString;
    }

    private String extractProtologueURIs(CdmLightExportState state, TaxonName name) {
        if (name.getNomenclaturalSource() != null){
            Set<ExternalLink> links = name.getNomenclaturalSource().getLinks();
            return extractLinkUris(links.iterator());
        }else{
            return null;
        }
    }

    private String extractMediaURIs(CdmLightExportState state, Set<? extends DescriptionBase<?>> descriptionsSet,
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

    private void handleAuthor(CdmLightExportState state, TeamOrPersonBase<?> author) {
        try {
            if (state.getAuthorFromStore(author.getId()) != null) {
                return;
            }
            state.addAuthorToStore(author);
            handleIdentifier(state, author);
            CdmLightExportTable table = CdmLightExportTable.NOMENCLATURAL_AUTHOR;
            String[] csvLine = new String[table.getSize()];
            CdmLightExportTable tableAuthorRel = CdmLightExportTable.NOMENCLATURAL_AUTHOR_TEAM_RELATION;
            String[] csvLineRel = new String[tableAuthorRel.getSize()];
            String[] csvLineMember = new String[table.getSize()];
            csvLine[table.getIndex(CdmLightExportTable.AUTHOR_ID)] = getId(state, author);
            csvLine[table.getIndex(CdmLightExportTable.ABBREV_AUTHOR)] = author.isProtectedTitleCache()
                    ? author.getTitleCache() : author.getNomenclaturalTitle();
            csvLine[table.getIndex(CdmLightExportTable.AUTHOR_TITLE)] = author.getTitleCache();
            author = HibernateProxyHelper.deproxy(author);
            if (author instanceof Person) {
                Person authorPerson = (Person) author;
                csvLine[table.getIndex(CdmLightExportTable.AUTHOR_GIVEN_NAME)] = authorPerson.getGivenName();
                csvLine[table.getIndex(CdmLightExportTable.AUTHOR_FAMILY_NAME)] = authorPerson.getFamilyName();
                csvLine[table.getIndex(CdmLightExportTable.AUTHOR_PREFIX)] = authorPerson.getPrefix();
                csvLine[table.getIndex(CdmLightExportTable.AUTHOR_SUFFIX)] = authorPerson.getSuffix();
            } else {
                // create an entry in rel table and all members in author table,
                // check whether the team members already in author table

                Team authorTeam = (Team) author;
                int index = 0;
                for (Person member : authorTeam.getTeamMembers()) {
                    csvLineRel = new String[tableAuthorRel.getSize()];
                    csvLineRel[tableAuthorRel.getIndex(CdmLightExportTable.AUTHOR_TEAM_FK)] = getId(state, authorTeam);
                    csvLineRel[tableAuthorRel.getIndex(CdmLightExportTable.AUTHOR_FK)] = getId(state, member);
                    csvLineRel[tableAuthorRel.getIndex(CdmLightExportTable.AUTHOR_TEAM_SEQ_NUMBER)] = String
                            .valueOf(index);
                    state.getProcessor().put(tableAuthorRel, authorTeam.getId() + ":" + member.getId(), csvLineRel);

                    if (state.getAuthorFromStore(member.getId()) == null) {
                        state.addAuthorToStore(member);
                        csvLineMember = new String[table.getSize()];
                        csvLineMember[table.getIndex(CdmLightExportTable.AUTHOR_ID)] = getId(state, member);
                        csvLineMember[table.getIndex(CdmLightExportTable.ABBREV_AUTHOR)] = member
                                .isProtectedTitleCache() ? member.getTitleCache() : member.getNomenclaturalTitle();
                        csvLineMember[table.getIndex(CdmLightExportTable.AUTHOR_TITLE)] = member.getTitleCache();
                        csvLineMember[table.getIndex(CdmLightExportTable.AUTHOR_GIVEN_NAME)] = member.getGivenName();
                        csvLineMember[table.getIndex(CdmLightExportTable.AUTHOR_FAMILY_NAME)] = member.getFamilyName();
                        csvLineMember[table.getIndex(CdmLightExportTable.AUTHOR_PREFIX)] = member.getPrefix();
                        csvLineMember[table.getIndex(CdmLightExportTable.AUTHOR_SUFFIX)] = member.getSuffix();
                        state.getProcessor().put(table, member, csvLineMember);
                    }
                    index++;
                }
            }
            state.getProcessor().put(table, author, csvLine);
        } catch (Exception e) {
            state.getResult().addException(e,
                    "An unexpected error occurred when handling author " + cdmBaseStr(author) + ": " + e.getMessage());
        }
    }

    private String extractStatusString(CdmLightExportState state, TaxonName name, boolean abbrev) {
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

    private void handleHomotypicalGroup(CdmLightExportState state, HomotypicalGroup group, Taxon acceptedTaxon, int sortIndex) {
        try {
            state.addHomotypicalGroupToStore(group);
            CdmLightExportTable table = CdmLightExportTable.HOMOTYPIC_GROUP;
            String[] csvLine = new String[table.getSize()];
            csvLine[table.getIndex(CdmLightExportTable.SORT_INDEX)] = String.valueOf(sortIndex);
            csvLine[table.getIndex(CdmLightExportTable.HOMOTYPIC_GROUP_ID)] = getId(state, group);

            List<TaxonName> typifiedNames = new ArrayList<>();
            if (acceptedTaxon != null){
                List<Synonym> synonymsInGroup = acceptedTaxon.getSynonymsInGroup(group);
                if (group.equals(acceptedTaxon.getHomotypicGroup())){
                    typifiedNames.add(acceptedTaxon.getName());
                }
                synonymsInGroup.stream().forEach(synonym -> typifiedNames.add(HibernateProxyHelper.deproxy(synonym.getName(), TaxonName.class)));
            }

//            group.getTypifiedNames().stream().forEach(name -> typifiedNames.add(HibernateProxyHelper.deproxy(name, TaxonName.class)));
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

//            Collections.sort(typifiedNames, new HomotypicalGroupNameComparator(firstname, true));
            String typifiedNamesString = "";
            String typifiedNamesWithSecString = "";
            String typifiedNamesWithoutAccepted = "";
            String typifiedNamesWithoutAcceptedWithSec = "";
            int index = 0;
            for (TaxonName name : typifiedNames) {
                // Concatenated output string for homotypic group (names and
                // citations) + status + some name relations (e.g. “non”)
                // TODO: nameRelations, which and how to display
                Set<TaxonBase> taxonBases = name.getTaxonBases();
                TaxonBase<?> taxonBase;

                String sec = "";
                String nameString = name.getFullTitleCache();
                String doubtful = "";

                if (state.getConfig().isAddHTML()){
                    nameString = createNameWithItalics(name.getTaggedFullTitle()) ;
                }

                String synonymSign = "";
                if (index > 0){
                    if (name.isInvalid()){
                        synonymSign = "\u2212 ";
                    }else{
                        synonymSign = "\u2261 ";
                    }
                }
                boolean isAccepted = false;

                if (taxonBases.size() == 1){
                     taxonBase = HibernateProxyHelper.deproxy(taxonBases.iterator().next());
                     if (taxonBase.getSec() != null){
                         sec = OriginalSourceFormatter.INSTANCE_WITH_YEAR_BRACKETS.format(taxonBase.getSecSource());
                     }
                     if (taxonBase.isDoubtful()){
                         doubtful = "?";
                     }else{
                         doubtful = "";
                     }
                     if (taxonBase instanceof Synonym){
                         if (isNotBlank(sec)){
                             sec = " syn. sec. " + sec + " ";
                         }else {
                             sec = "";
                         }

                         typifiedNamesWithoutAccepted += synonymSign + doubtful + nameString ;
                         typifiedNamesWithoutAcceptedWithSec += synonymSign + doubtful + nameString + sec ;
                     }else{
                         sec = "";
                         if (!(((Taxon)taxonBase).isProparteSynonym() || ((Taxon)taxonBase).isMisapplication())){
                             isAccepted = true;
                         }
                     }
                }else{
                    //there are names used more than once?
                    for (TaxonBase<?> tb: taxonBases){
                        if (tb.getSec() != null){
                            sec = OriginalSourceFormatter.INSTANCE_WITH_YEAR_BRACKETS.format(tb.getSecSource());
                        }
                        if (tb.isDoubtful()){
                            doubtful = "?";
                        }else{
                            doubtful = "";
                        }
                        if (tb instanceof Synonym){
                            if (StringUtils.isNotBlank(sec)){
                                sec = " syn. sec. " + sec + " ";
                            }else {
                                sec = "";
                            }

                            break;
                        }else{
                            sec = "";
                            if (!(((Taxon)tb).isProparteSynonym() || ((Taxon)tb).isMisapplication())){
                                isAccepted = true;
                                break;
                            }

                        }
                    }
                    if (!isAccepted){
                        typifiedNamesWithoutAccepted += synonymSign + doubtful + nameString + "; ";
                        typifiedNamesWithoutAcceptedWithSec += synonymSign + doubtful + nameString + sec;
                        typifiedNamesWithoutAcceptedWithSec = typifiedNamesWithoutAcceptedWithSec.trim() + "; ";
                    }
                }
                typifiedNamesString += synonymSign + doubtful + nameString ;
                typifiedNamesWithSecString += synonymSign + doubtful + nameString + sec;


                csvLine[table.getIndex(CdmLightExportTable.HOMOTYPIC_GROUP_STRING)] = typifiedNamesString.trim();

                csvLine[table.getIndex(CdmLightExportTable.HOMOTYPIC_GROUP_WITH_SEC_STRING)] = typifiedNamesWithSecString.trim();

                if (typifiedNamesWithoutAccepted != null && firstname != null) {
                    csvLine[table.getIndex(CdmLightExportTable.HOMOTYPIC_GROUP_WITHOUT_ACCEPTED)] = typifiedNamesWithoutAccepted.trim();
                } else {
                    csvLine[table.getIndex(CdmLightExportTable.HOMOTYPIC_GROUP_WITHOUT_ACCEPTED)] = "";
                }

                if (typifiedNamesWithoutAcceptedWithSec != null && firstname != null) {
                    csvLine[table.getIndex(CdmLightExportTable.HOMOTYPIC_GROUP_WITHOUT_ACCEPTEDWITHSEC)] = typifiedNamesWithoutAcceptedWithSec.trim();
                } else {
                    csvLine[table.getIndex(CdmLightExportTable.HOMOTYPIC_GROUP_WITHOUT_ACCEPTEDWITHSEC)] = "";
                }
                index++;
            }

            Set<TypeDesignationBase<?>> typeDesigantionSet = group.getTypeDesignations();
            List<TypeDesignationBase<?>> designationList = new ArrayList<>();
            designationList.addAll(typeDesigantionSet);
            Collections.sort(designationList, new TypeComparator());

            List<TaggedText> list = new ArrayList<>();
            if (!designationList.isEmpty()) {
                TypeDesignationSetManager manager = new TypeDesignationSetManager(group);
                list.addAll(new TypeDesignationSetFormatter(false, false, false).toTaggedText(manager));
            }
            String typeTextDesignations = "";
            //The typeDesignationManager does not handle the textual typeDesignations
            for (TypeDesignationBase<?> typeDes: designationList) {
            	if (typeDes instanceof TextualTypeDesignation) {
            		typeTextDesignations = typeTextDesignations + ((TextualTypeDesignation)typeDes).getText(Language.getDefaultLanguage());
            		typeTextDesignations =  typeTextDesignations + "; ";
            	}
            }
            if (typeTextDesignations.equals("; ")) {
            	typeTextDesignations = "";
            }
            if (StringUtils.isNotBlank(typeTextDesignations)) {
            	typeTextDesignations = typeTextDesignations.substring(0, typeTextDesignations.length()-2);
            }
            String specimenTypeString = !list.isEmpty()? createTypeDesignationString(list, true, typifiedNames.get(0).isSpecies() || typifiedNames.get(0).isInfraSpecific()):"";

            if (StringUtils.isNotBlank(specimenTypeString)) {
                if (!specimenTypeString.endsWith(".")) {
                	specimenTypeString = specimenTypeString + ".";
                }
                csvLine[table.getIndex(CdmLightExportTable.TYPE_STRING)] = specimenTypeString;

            } else {
                csvLine[table.getIndex(CdmLightExportTable.TYPE_STRING)] = "";
            }
            if (StringUtils.isNotBlank(typeTextDesignations)) {
                if (!typeTextDesignations.endsWith(".")) {
                	typeTextDesignations = typeTextDesignations + ".";
                }
                csvLine[table.getIndex(CdmLightExportTable.TYPE_CACHE)] = typeTextDesignations;

            } else {
                csvLine[table.getIndex(CdmLightExportTable.TYPE_CACHE)] = "";
            }
            state.getProcessor().put(table, String.valueOf(group.getId()), csvLine);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling homotypic group "
                    + cdmBaseStr(group) + ": " + e.getMessage());
        }
    }

    private String createTypeDesignationString(List<TaggedText> list, boolean isHomotypicGroup, boolean isSpecimenTypeDesignation) {
        StringBuffer homotypicalGroupTypeDesignationString = new StringBuffer();

        for (TaggedText text : list) {
            if (text != null && text.getText() != null
                    && (text.getText().equals("Type:") || text.getText().equals("NameType:") || (text.getType().equals(TagEnum.name) && !isHomotypicGroup))) {
                // do nothing
            } else if (text.getType().equals(TagEnum.reference)) {
                homotypicalGroupTypeDesignationString.append(text.getText());
            }else if (text.getType().equals(TagEnum.name)){
                if (!isSpecimenTypeDesignation){
                    homotypicalGroupTypeDesignationString
                    .append("<i>"+text.getText()+"</i> ");
                }
            }else if (text.getType().equals(TagEnum.typeDesignation) ) {
                if(isSpecimenTypeDesignation){
                    homotypicalGroupTypeDesignationString
                        .append(text.getText().replace(").", "").replace("(", "").replace(")", ""));
                }else{
                    homotypicalGroupTypeDesignationString
                        .append(text.getText());
                }

            } else {
                homotypicalGroupTypeDesignationString.append(text.getText());
            }
        }

        String typeDesignations = homotypicalGroupTypeDesignationString.toString();
        typeDesignations = typeDesignations.trim();

        if (typeDesignations.endsWith(";")){
            typeDesignations = typeDesignations.substring(0, typeDesignations.length()-1);
        }
        typeDesignations += ".";
        typeDesignations = typeDesignations.replace("..", ".");
        typeDesignations = typeDesignations.replace(". .", ".");

        if (typeDesignations.trim().equals(".")) {
            typeDesignations = null;
        }

        return typeDesignations;
    }

    private String getTropicosTitleCache(CdmLightExportState state, TaxonName name) {
        try {
            String basionymStart = "(";
            String basionymEnd = ") ";
            String exAuthorSeperator = " ex ";
            TeamOrPersonBase<?> combinationAuthor = name.getCombinationAuthorship();
            TeamOrPersonBase<?> exCombinationAuthor = name.getExCombinationAuthorship();
            TeamOrPersonBase<?> basionymAuthor = name.getBasionymAuthorship();
            TeamOrPersonBase<?> exBasionymAuthor = name.getExBasionymAuthorship();

            String combinationAuthorString = "";
            if (combinationAuthor != null) {
                combinationAuthor = HibernateProxyHelper.deproxy(combinationAuthor);
                if (combinationAuthor instanceof Team) {
                    combinationAuthorString = createTropicosTeamTitle(combinationAuthor);
                } else {
                    Person person = HibernateProxyHelper.deproxy(combinationAuthor, Person.class);
                    combinationAuthorString = createTropicosAuthorString(person);
                }
            }
            String exCombinationAuthorString = "";
            if (exCombinationAuthor != null) {
                exCombinationAuthor = HibernateProxyHelper.deproxy(exCombinationAuthor);
                if (exCombinationAuthor instanceof Team) {
                    exCombinationAuthorString = createTropicosTeamTitle(exCombinationAuthor);
                } else {
                    Person person = HibernateProxyHelper.deproxy(exCombinationAuthor, Person.class);
                    exCombinationAuthorString = createTropicosAuthorString(person);
                }
            }

            String basionymAuthorString = "";
            if (basionymAuthor != null) {
                basionymAuthor = HibernateProxyHelper.deproxy(basionymAuthor);
                if (basionymAuthor instanceof Team) {
                    basionymAuthorString = createTropicosTeamTitle(basionymAuthor);
                } else {
                    Person person = HibernateProxyHelper.deproxy(basionymAuthor, Person.class);
                    basionymAuthorString = createTropicosAuthorString(person);
                }
            }

            String exBasionymAuthorString = "";

            if (exBasionymAuthor != null) {
                exBasionymAuthor = HibernateProxyHelper.deproxy(exBasionymAuthor);
                if (exBasionymAuthor instanceof Team) {
                    exBasionymAuthorString = createTropicosTeamTitle(exBasionymAuthor);

                } else {
                    Person person = HibernateProxyHelper.deproxy(exBasionymAuthor, Person.class);
                    exBasionymAuthorString = createTropicosAuthorString(person);
                }
            }
            String completeAuthorString = name.getNameCache() + " ";

            completeAuthorString += (!CdmUtils.isBlank(exBasionymAuthorString)
                    || !CdmUtils.isBlank(basionymAuthorString)) ? basionymStart : "";
            completeAuthorString += (!CdmUtils.isBlank(exBasionymAuthorString))
                    ? (CdmUtils.Nz(exBasionymAuthorString) + exAuthorSeperator) : "";
            completeAuthorString += (!CdmUtils.isBlank(basionymAuthorString)) ? CdmUtils.Nz(basionymAuthorString) : "";
            completeAuthorString += (!CdmUtils.isBlank(exBasionymAuthorString)
                    || !CdmUtils.isBlank(basionymAuthorString)) ? basionymEnd : "";
            completeAuthorString += (!CdmUtils.isBlank(exCombinationAuthorString))
                    ? (CdmUtils.Nz(exCombinationAuthorString) + exAuthorSeperator) : "";
            completeAuthorString += (!CdmUtils.isBlank(combinationAuthorString)) ? CdmUtils.Nz(combinationAuthorString)
                    : "";

            return completeAuthorString;
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling tropicos title cache for "
                    + cdmBaseStr(name) + ": " + e.getMessage());
            return null;
        }
    }

    private String createTropicosTeamTitle(TeamOrPersonBase<?> combinationAuthor) {
        String combinationAuthorString;
        Team team = HibernateProxyHelper.deproxy(combinationAuthor, Team.class);
        Team tempTeam = Team.NewInstance();
        for (Person teamMember : team.getTeamMembers()) {
            combinationAuthorString = createTropicosAuthorString(teamMember);
            Person tempPerson = Person.NewTitledInstance(combinationAuthorString);
            tempTeam.addTeamMember(tempPerson);
        }
        combinationAuthorString = tempTeam.generateTitle();
        return combinationAuthorString;
    }

    private String createTropicosAuthorString(Person teamMember) {
        String nomAuthorString = "";
        String[] splittedAuthorString = null;
        if (teamMember == null) {
            return nomAuthorString;
        }

        if (teamMember.getGivenName() != null) {
            String givenNameString = teamMember.getGivenName().replaceAll("\\.", "\\. ");
            splittedAuthorString = givenNameString.split("\\s");
            for (String split : splittedAuthorString) {
                if (!StringUtils.isBlank(split)) {
                    nomAuthorString += split.substring(0, 1);
                    nomAuthorString += ".";
                }
            }
        }
        if (teamMember.getFamilyName() != null) {
            String familyNameString = teamMember.getFamilyName().replaceAll("\\.", "\\. ");
            splittedAuthorString = familyNameString.split("\\s");
            for (String split : splittedAuthorString) {
                nomAuthorString += " " + split;
            }
        }
        if (isBlank(nomAuthorString.trim())) {
            if (teamMember.getTitleCache() != null) {
                String titleCacheString = teamMember.getTitleCache().replaceAll("\\.", "\\. ");
                splittedAuthorString = titleCacheString.split("\\s");
            } else {
                splittedAuthorString = new String[0];
            }

            int index = 0;
            for (String split : splittedAuthorString) {
                if (index < splittedAuthorString.length - 1 && (split.length() == 1 || split.endsWith("."))) {
                    nomAuthorString += split;
                } else {
                    nomAuthorString = nomAuthorString + " " + split;
                }
                index++;
            }
        }
        return nomAuthorString.trim();
    }

    private void handleReference(CdmLightExportState state, Reference reference) {
        try {
            state.addReferenceToStore(reference);
            CdmLightExportTable table = CdmLightExportTable.REFERENCE;
            reference = HibernateProxyHelper.deproxy(reference);

            handleIdentifier(state, reference);
            String[] csvLine = new String[table.getSize()];
            csvLine[table.getIndex(CdmLightExportTable.REFERENCE_ID)] = getId(state, reference);
            // TODO short citations correctly
            String shortCitation = OriginalSourceFormatter.INSTANCE.format(reference, null); // Should be Author(year) like in Taxon.sec
            csvLine[table.getIndex(CdmLightExportTable.BIBLIO_SHORT_CITATION)] = shortCitation;
            // TODO get preferred title
            csvLine[table.getIndex(CdmLightExportTable.REF_TITLE)] = reference.isProtectedTitleCache()
                    ? reference.getTitleCache() : reference.getTitle();
            csvLine[table.getIndex(CdmLightExportTable.ABBREV_REF_TITLE)] = reference.isProtectedAbbrevTitleCache()
                    ? reference.getAbbrevTitleCache() : reference.getAbbrevTitle();
            csvLine[table.getIndex(CdmLightExportTable.DATE_PUBLISHED)] = reference.getDatePublishedString();
            // TBC
            csvLine[table.getIndex(CdmLightExportTable.EDITION)] = reference.getEdition();
            csvLine[table.getIndex(CdmLightExportTable.EDITOR)] = reference.getEditor();
            csvLine[table.getIndex(CdmLightExportTable.ISBN)] = reference.getIsbn();
            csvLine[table.getIndex(CdmLightExportTable.ISSN)] = reference.getIssn();
            csvLine[table.getIndex(CdmLightExportTable.ORGANISATION)] = reference.getOrganization();
            csvLine[table.getIndex(CdmLightExportTable.PAGES)] = reference.getPages();
            csvLine[table.getIndex(CdmLightExportTable.PLACE_PUBLISHED)] = reference.getPlacePublished();
            csvLine[table.getIndex(CdmLightExportTable.PUBLISHER)] = reference.getPublisher();
            csvLine[table.getIndex(CdmLightExportTable.REF_ABSTRACT)] = reference.getReferenceAbstract();
            csvLine[table.getIndex(CdmLightExportTable.SERIES_PART)] = reference.getSeriesPart();
            csvLine[table.getIndex(CdmLightExportTable.VOLUME)] = reference.getVolume();
            csvLine[table.getIndex(CdmLightExportTable.YEAR)] = reference.getYear();

            if (reference.getAuthorship() != null) {
                csvLine[table.getIndex(CdmLightExportTable.AUTHORSHIP_TITLE)] = createFullAuthorship(reference);
                csvLine[table.getIndex(CdmLightExportTable.AUTHOR_FK)] = getId(state, reference.getAuthorship());
            }

            csvLine[table.getIndex(CdmLightExportTable.IN_REFERENCE)] = getId(state, reference.getInReference());
            if (reference.getInReference() != null
                    && !state.getReferenceStore().contains(reference.getInReference().getUuid())) {
                handleReference(state, reference.getInReference());
            }
            if (reference.getInstitution() != null) {
                csvLine[table.getIndex(CdmLightExportTable.INSTITUTION)] = reference.getInstitution().getTitleCache();
            }
            if (reference.getLsid() != null) {
                csvLine[table.getIndex(CdmLightExportTable.LSID)] = reference.getLsid().getLsid();
            }
            if (reference.getSchool() != null) {
                csvLine[table.getIndex(CdmLightExportTable.SCHOOL)] = reference.getSchool().getTitleCache();
            }
            if (reference.getUri() != null) {
                csvLine[table.getIndex(CdmLightExportTable.URI)] = reference.getUri().toString();
            }
            csvLine[table.getIndex(CdmLightExportTable.REF_TYPE)] = reference.getType().getKey();

            state.getProcessor().put(table, reference, csvLine);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling reference "
                    + cdmBaseStr(reference) + ": " + e.getMessage());
        }
    }

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
            fullAuthorship = authorTeam.getCacheStrategy().getTitleCache(authorTeam);
        }
        return fullAuthorship;
    }

    private void handleSpecimen(CdmLightExportState state, SpecimenOrObservationBase<?> specimen) {
        try {
            state.addSpecimenToStore(specimen);
            CdmLightExportTable table = CdmLightExportTable.SPECIMEN;
            String specimenId = getId(state, specimen);
            String[] csvLine = new String[table.getSize()];

            /*
             * SpecimenCitation = “El Salvador, Municipio La Libertad, San
             * Diego, El Amatal, 14.4.1993, GonzÃ¡lez 159” [Auch ohne Punkt] ->
             * FieldUnit TitleCache HerbariumAbbrev = “B” [wie gehabt]
             * HerbariumCode
             *
             */

            csvLine[table.getIndex(CdmLightExportTable.SPECIMEN_ID)] = specimenId;
            csvLine[table.getIndex(CdmLightExportTable.SPECIMEN_CITATION)] = specimen.getTitleCache();
            Collection<FieldUnit> fieldUnits = this.getOccurrenceService().findFieldUnits(specimen.getUuid(), null);
            if (fieldUnits.size() == 1) {
                Iterator<FieldUnit> iterator = fieldUnits.iterator();
                if (iterator.hasNext()){
                    FieldUnit fieldUnit = iterator.next();
                    csvLine[table.getIndex(CdmLightExportTable.FIELDUNIT_CITATION)] = fieldUnit.getTitleCache();
                }
            }
            if (specimen.isInstanceOf(DerivedUnit.class)){
                DerivedUnit derivedUnit = (DerivedUnit) specimen;
                if (!StringUtils.isBlank(derivedUnit.getBarcode())){
                    csvLine[table.getIndex(CdmLightExportTable.BARCODE)] = derivedUnit.getBarcode();
                }
                if (!StringUtils.isBlank(derivedUnit.getAccessionNumber())){
                    csvLine[table.getIndex(CdmLightExportTable.ACCESSION_NUMBER)] = derivedUnit.getAccessionNumber();
                }
                if (!StringUtils.isBlank(derivedUnit.getCatalogNumber())){
                    csvLine[table.getIndex(CdmLightExportTable.CATALOGUE_NUMBER)] = derivedUnit.getCatalogNumber();
                }
            }

            csvLine[table.getIndex(CdmLightExportTable.PREFERREDSTABLE_ID)] = specimen.getPreferredStableUri() != null? specimen.getPreferredStableUri().toString(): null;
            csvLine[table.getIndex(CdmLightExportTable.SPECIMEN_IMAGE_URIS)] = extractMediaURIs(state,
                    specimen.getDescriptions(), Feature.IMAGE());
            if (specimen instanceof DerivedUnit) {
                DerivedUnit derivedUnit = (DerivedUnit) specimen;
                if (derivedUnit.getCollection() != null) {
                    csvLine[table.getIndex(CdmLightExportTable.HERBARIUM_ABBREV)] = derivedUnit.getCollection()
                            .getCode();
                }

                if (specimen instanceof MediaSpecimen) {
                    MediaSpecimen mediaSpecimen = (MediaSpecimen) specimen;
                    Iterator<MediaRepresentation> it = mediaSpecimen.getMediaSpecimen().getRepresentations().iterator();
                    String mediaUris = extractMediaUris(it);
                    csvLine[table.getIndex(CdmLightExportTable.MEDIA_SPECIMEN_URL)] = mediaUris;

                }

                if (derivedUnit.getDerivedFrom() != null) {
                    for (SpecimenOrObservationBase<?> original : derivedUnit.getDerivedFrom().getOriginals()) {
                        // TODO: What to do if there are more then one
                        // FieldUnit??
                        if (original instanceof FieldUnit) {
                            FieldUnit fieldUnit = (FieldUnit) original;
                            csvLine[table.getIndex(CdmLightExportTable.COLLECTOR_NUMBER)] = fieldUnit.getFieldNumber();

                            GatheringEvent gathering = fieldUnit.getGatheringEvent();
                            if (gathering != null) {
                                if (gathering.getLocality() != null) {
                                    csvLine[table.getIndex(CdmLightExportTable.LOCALITY)] = gathering.getLocality()
                                            .getText();
                                }
                                if (gathering.getCountry() != null) {
                                    csvLine[table.getIndex(CdmLightExportTable.COUNTRY)] = gathering.getCountry()
                                            .getLabel();
                                }
                                csvLine[table.getIndex(CdmLightExportTable.COLLECTOR_STRING)] = createCollectorString(
                                        state, gathering, fieldUnit);

                                if (gathering.getGatheringDate() != null) {
                                    csvLine[table.getIndex(CdmLightExportTable.COLLECTION_DATE)] = gathering
                                            .getGatheringDate().toString();
                                }
                                if (!gathering.getCollectingAreas().isEmpty()) {
                                    int index = 0;
                                    csvLine[table.getIndex(CdmLightExportTable.FURTHER_AREAS)] = "0";
                                    for (NamedArea area : gathering.getCollectingAreas()) {
                                        if (index == 0) {
                                            csvLine[table.getIndex(CdmLightExportTable.AREA_CATEGORY1)] = area.getLevel() != null?area
                                                    .getLevel().getLabel():"";
                                            csvLine[table.getIndex(CdmLightExportTable.AREA_NAME1)] = area.getLabel();
                                        }
                                        if (index == 1) {
                                            csvLine[table.getIndex(CdmLightExportTable.AREA_CATEGORY2)] = area.getLevel() != null?area
                                                    .getLevel().getLabel():"";
                                            csvLine[table.getIndex(CdmLightExportTable.AREA_NAME2)] = area.getLabel();
                                        }
                                        if (index == 2) {
                                            csvLine[table.getIndex(CdmLightExportTable.AREA_CATEGORY3)] = area.getLevel() != null?area
                                                    .getLevel().getLabel():"";
                                            csvLine[table.getIndex(CdmLightExportTable.AREA_NAME3)] = area.getLabel();
                                        }
                                        if (index == 3) {
                                            csvLine[table.getIndex(CdmLightExportTable.FURTHER_AREAS)] = "1";
                                            break;
                                        }
                                        index++;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    state.getResult().addError("The specimen with uuid " + specimen.getUuid()
                            + " is not an DerivedUnit. Could not be exported.");
                }
            }

            state.getProcessor().put(table, specimen, csvLine);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling specimen "
                    + cdmBaseStr(specimen) + ": " + e.getMessage());
        }
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

    private String createCollectorString(CdmLightExportState state, GatheringEvent gathering, FieldUnit fieldUnit) {
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
    protected boolean doCheck(CdmLightExportState state) {
        return false;
    }

    @Override
    protected boolean isIgnore(CdmLightExportState state) {
        return false;
    }

}
