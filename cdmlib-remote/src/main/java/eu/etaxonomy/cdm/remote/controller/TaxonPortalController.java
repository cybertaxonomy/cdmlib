/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.etaxonomy.cdm.api.dto.portal.DistributionInfoDto.InfoPart;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto;
import eu.etaxonomy.cdm.api.dto.portal.config.CondensedDistributionConfiguration;
import eu.etaxonomy.cdm.api.dto.portal.config.DistributionInfoConfiguration;
import eu.etaxonomy.cdm.api.dto.portal.config.DistributionOrder;
import eu.etaxonomy.cdm.api.dto.portal.config.TaxonPageDtoConfiguration;
import eu.etaxonomy.cdm.api.filter.TaxonOccurrenceRelationType;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.portal.IPortalService;
import eu.etaxonomy.cdm.api.service.portal.format.CondensedDistributionRecipe;
import eu.etaxonomy.cdm.api.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.UpdatableRoutingDataSource;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.dao.initializer.EntityInitStrategy;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.remote.controller.util.ControllerUtils;
import eu.etaxonomy.cdm.remote.controller.util.IMediaToolbox;
import eu.etaxonomy.cdm.remote.editor.CdmTypePropertyEditor;
import eu.etaxonomy.cdm.remote.editor.DefinedTermBaseList;
import eu.etaxonomy.cdm.remote.editor.MatchModePropertyEditor;
import eu.etaxonomy.cdm.remote.editor.NamedAreaPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.TermBaseListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import io.swagger.annotations.Api;

/**
 * The TaxonPortalController class is a Spring MVC Controller.
 * <p>
 * The syntax of the mapped service URIs contains the the {datasource-name} path element.
 * The available {datasource-name}s are defined in a configuration file which
 * is loaded by the {@link UpdatableRoutingDataSource}. If the
 * UpdatableRoutingDataSource is not being used in the actual application
 * context any arbitrary {datasource-name} may be used.
 * <p>
 * Methods mapped at type level, inherited from super classes ({@link BaseController}):
 * <blockquote>
 * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}</b>
 *
 * Get the {@link TaxonBase} instance identified by the <code>{taxon-uuid}</code>.
 * The returned Taxon is initialized by
 * the following strategy {@link #TAXON_INIT_STRATEGY}
 * </blockquote>
 *
 * @author a.kohlbecker
 * @since 20.07.2009
 */
@Controller
@Api("portal_taxon")
@RequestMapping(value = {"/portal/taxon/{uuid}"})
public class TaxonPortalController extends TaxonController{

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private INameService nameService;

    @Autowired
    private ITaxonNodeService taxonNodeService;

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private ITermService termService;

    @Autowired
    private IMediaToolbox mediaToolbox;

    @Autowired
    private IPortalService portalService;


    public static final EntityInitStrategy TAXON_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String []{
            "$",
            "sources",
            "statusNote",
            "relationsFromThisTaxon.toTaxon.secSource.citation.authorship",
            "relationsFromThisTaxon.toTaxon.secSource.citation.inReference.authorship",
            "relationsToThisTaxon.fromTaxon.secSource.citation.authorship",
            "relationsToThisTaxon.fromTaxon.secSource.citation.inReference.authorship",
            // the name
            "name.$",
            "name.nomenclaturalSource.citation.authorship",
            "name.nomenclaturalSource.citation.inReference.authorship",
            "name.rank.representations",
            "name.status.type.representations",
            "name.status.source.citation",
            "secSource.nameUsedInSource.$",
            "secSource.nameUsedInSource.nomenclaturalSource.citation.authorship",
            "secSource.nameUsedInSource.nomenclaturalSource.citation.inReference.authorship",
            "secSource.citation.authorship.$",
            "secSource.citation.inReference.authorship.$",
            "annotations.$",
            "annotations.annotationType.$",
            "annotations.annotationType.includes.$"
//            "descriptions" // TODO remove

            }));

    public static final EntityInitStrategy TAXON_WITH_CHILDNODES_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String []{
            "taxonNodes.$",
            "taxonNodes.classification.$",
            "taxonNodes.childNodes.$"
            }));

    public static final EntityInitStrategy SIMPLE_TAXON_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String []{
            "$",
            // the name
            "name.$",
            "name.rank.representations",
            "name.status.type.representations",
            "name.status.source.citation",
            "name.nomenclaturalSource.citation.authorship",
            "name.nomenclaturalSource.citation.inReference.authorship",
            "taxonNodes.classification",
            "secSource.nameUsedInSource.$",
            "secSource.citation.authorship.$",
            "secSource.citation.inReference.authorship.$"
          	}));

    public static final EntityInitStrategy SYNONYMY_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String []{
            // initialize homotypical and heterotypical groups; needs synonyms
            "synonyms.$",
            "synonyms.name.status.type.representations",
            "synonyms.name.status.source.citation",
            "synonyms.name.nomenclaturalSource.citation.authorship",
            "synonyms.name.nomenclaturalSource.citation.inReference.authorship",
//            "synonyms.name.homotypicalGroup.typifiedNames.$",
//            "synonyms.name.homotypicalGroup.typifiedNames.taxonBases.$",
            "synonyms.name.combinationAuthorship.$",
            "synonyms.secSource.citation.authorship.$",
            "synonyms.secSource.citation.inReference.authorship.$",
            "synonyms.secSource.nameUsedInSource.$",
            "name.typeDesignations",

            "name.homotypicalGroup.$",
            "name.homotypicalGroup.typifiedNames.$",
            "name.homotypicalGroup.typifiedNames.nomenclaturalSource.citation.authorship",
            "name.homotypicalGroup.typifiedNames.nomenclaturalSource.citation.inReference.authorship",
            "synonyms.annotations.$",
            "synonyms.annotations.annotationType.$",
            "synonyms.annotations.annotationType.includes.$"

//            "name.homotypicalGroup.typifiedNames.taxonBases.$"
    }));


    private static final EntityInitStrategy TAXONRELATIONSHIP_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String []{
            "$",
            "type.inverseRepresentations",
            "fromTaxon.sec",
            "fromTaxon.name",
            "toTaxon.sec",
            "toTaxon.name"
    }));

    public static final EntityInitStrategy NAMERELATIONSHIP_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String []{
            "$",
            "type.inverseRepresentations",
            "source.citation",
            "toName.$",
            "toName.nomenclaturalSource.citation.authorship",
            "toName.nomenclaturalSource.citation.inReference.authorship",
            "fromName.$",
            "fromName.nomenclaturalSource.citation.authorship",
            "fromName.nomenclaturalSource.citation.inReference.authorship",

    }));

    protected static final EntityInitStrategy TAXONDESCRIPTION_INIT_STRATEGY = DescriptionPortalController.DESCRIPTION_INIT_STRATEGY;

    protected static final EntityInitStrategy DESCRIPTION_ELEMENT_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String []{
            "$",
            "sources.citation.authorship",
            "sources.citation.inReference.authorship",
            "sources.nameUsedInSource",
            "multilanguageText",
            "media",
    }));


//	private static final List<String> NAMEDESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
//			"uuid",
//			"feature",
//			"elements.$",
//			"elements.multilanguageText",
//			"elements.media",
//	});

    protected static final EntityInitStrategy TAXONDESCRIPTION_MEDIA_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String []{
            "elements.media"

    }));

    private static final EntityInitStrategy TYPEDESIGNATION_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String []{
            "typeSpecimen.$",
            "citation.authorship.$",
            "typeName",
            "typeStatus"
    }));

    protected static final EntityInitStrategy TAXONNODE_WITH_CHILDNODES_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String []{
            "childNodes.taxon",
    }));

    protected static final EntityInitStrategy TAXONNODE_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String []{
            "taxonNodes.classification",
            "taxonNodes.parent",
            "taxonNodes.statusNote.*",
            "taxonNodes.taxon.name",
            "taxonNodes.taxon.secSource.citation",
            "taxonNodes.taxon.secSource.nameUsedInSource.$",
            "taxonNodes.taxon.secSource.citation.authorship.$",
            "taxonNodes.taxon.secSource.citation.inReference.authorship.$",
            "taxonNodes.source.citation.authorship",
            "taxonNodes.source.citation.inReference.authorship",
            "acceptedTaxon.taxonNodes.classification",
            "secSource.nameUsedInSource"
    }));

    @Override
    protected <CDM_BASE extends CdmBase> List<String> complementInitStrategy(Class<CDM_BASE> clazz,
            List<String> pathProperties) {

        if(pathProperties != null) {
            List<String> complemented = new ArrayList<>(pathProperties);
            if(pathProperties.contains("name")) {
                // pathProperties for web service request for portal/taxon/{uuid}/name
                complemented.add("name.nomenclaturalSource.citation.authorship");
                complemented.add("name.nomenclaturalSource.citation.inReference.authorship");
                return complemented;
            }
        }
        return pathProperties;
    }

    public TaxonPortalController(){
        super();
        setInitializationStrategy(TAXON_INIT_STRATEGY.getPropertyPaths());
    }

    @Autowired
    @Override
    public void setService(ITaxonService service) {
        this.service = service;
    }

    @InitBinder
    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(NamedArea.class, new NamedAreaPropertyEditor());
        binder.registerCustomEditor(MatchMode.class, new MatchModePropertyEditor());
        binder.registerCustomEditor(Class.class, new CdmTypePropertyEditor());
        binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor());
        binder.registerCustomEditor(DefinedTermBaseList.class, new TermBaseListPropertyEditor<>(termService));
    }

    /**
     * TODO documentation
     *
     * @param taxonUuid the taxon uuid
     * @param subtreeUuid the taxon node subtree filter
     * @throws IOException
     */
    @RequestMapping(
            value = {"page"},
            method = RequestMethod.GET)
    @ResponseBody
    public TaxonPageDto doGetTaxonPage(@PathVariable("uuid") UUID taxonUuid,
            @RequestParam(value = "subtree", required = false) UUID subtreeUuid,
            @RequestParam(value = "featureTree", required = false) UUID featureTreeUuid,
            @RequestParam(value = "nameRelationsDirect", required = false) Set<UUID> directNameRelations,
            @RequestParam(value = "nameRelationsInverse", required = false) Set<UUID> inverseNameRelations,
            @RequestParam(value = "etAlPos", required = false) Integer etAlPosition,
            @RequestParam(value = "doSynonyms", required = false) boolean doSynonyms,
            @RequestParam(value = "doFacts", required = false) boolean doFacts,
            @RequestParam(value = "doSpecimens", required = false) boolean doSpecimens,
            @RequestParam(value = "doKeys", required = false) boolean doKeys,
            @RequestParam(value = "doMedia", required = false) boolean doMedia,
            @RequestParam(value = "doTaxonNodes", required = false) boolean doTaxonNodes,
            @RequestParam(value = "doTaxonRelations", required = false) boolean doTaxonRelations,
            @RequestParam(value = "taxOccRelFilter", required = false) String taxOccRelFilter,
            @RequestParam(value = "annotationTypes", required = false) Set<UUID> annotationTypes,
            @RequestParam(value = "markerTypes", required = false) Set<UUID> markerTypes,
            @RequestParam(value = "dtoLoading", required = false, defaultValue = "true") boolean dtoLoading,


            //TODO annotation type filter

            //distributionInfoConfig
            @RequestParam(value = "part", required = false)  Set<InfoPart> partSet,
            @RequestParam(value = "subAreaPreference", required = false) boolean preferSubAreas,
            @RequestParam(value = "statusOrderPreference", required = false) boolean statusOrderPreference,
            @RequestParam(value = "fallbackAreaMarkerType", required = false) DefinedTermBaseList<MarkerType> fallbackAreaMarkerTypeList,
            @RequestParam(value = "alternativeRootAreaMarkerType", required = false) DefinedTermBaseList<MarkerType> alternativeRootAreaMarkerTypeList,
            @RequestParam(value = "areaTree", required = false ) UUID areaTreeUuid,
            //TODO still needs to be used
            @RequestParam(value = "statusTree", required = false ) UUID statusTreeUuid,
            @RequestParam(value = "omitLevels", required = false) Set<UUID> omitLevels,
            @RequestParam(value = "statusColors", required = false) String statusColorsString,
            @RequestParam(value = "distributionOrder", required = false, defaultValue="LABEL") DistributionOrder distributionOrder,
//          @RequestParam(value = "neverUseFallbackAreaAsParent", required = false) boolean neverUseFallbackAreaAsParent,
            @RequestParam(value = "recipe", required = false, defaultValue="EuroPlusMed") CondensedDistributionRecipe recipe,

            //TODO configuration data
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        //maybe become a parameter, but first test if
        Boolean neverUseFallbackAreaAsParent = null;   //currently default is true in configuration

        boolean includeUnpublished = NO_UNPUBLISHED;
        EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes = bindAssociationFilter(taxOccRelFilter);
        if(request != null){
            logger.info("doGetTaxonPage() " + requestPathAndQuery(request));
        }

        //TODO for now hardcoded
        alternativeRootAreaMarkerTypeList = new DefinedTermBaseList<>();
        UUID alternativeRootAreaMarkerTypeUuid = MarkerType.uuidAlternativeRootArea;
        MarkerType defaultAlternativeRootAreaMarkerType = (MarkerType)termService.find(alternativeRootAreaMarkerTypeUuid);
        if (defaultAlternativeRootAreaMarkerType != null) {
            alternativeRootAreaMarkerTypeList.add(defaultAlternativeRootAreaMarkerType);
        }

        //TODO is this current state of art?
//        ModelAndView mv = new ModelAndView();

        //check taxon exists and not filtered
        Taxon taxon = getCdmBaseInstance(Taxon.class, taxonUuid, response, getTaxonNodeInitStrategy().getPropertyPaths());
        TaxonNode subtree = getSubtreeOrError(subtreeUuid, taxonNodeService, response);
        taxon = checkExistsSubtreeAndAccess(taxon, subtree, NO_UNPUBLISHED, response);

        if (partSet == null) {
            partSet = EnumSet.of(InfoPart.condensedDistribution, InfoPart.mapUriParams, InfoPart.tree);
        }
        //TODO null check needed?
        if (annotationTypes == null) {
            annotationTypes = new HashSet<>();
        }
        if (markerTypes == null) {
            markerTypes = new HashSet<>();
        }

//      //TODO is this performant?
//      IVocabularyService vocabularyService = null;
//      Map<PresenceAbsenceTerm, Color> distributionStatusColors = DistributionServiceUtilities.buildStatusColorMap(
//              statusColorsString, termService, vocabularyService);

        TaxonPageDtoConfiguration config = new TaxonPageDtoConfiguration();

        config.setTaxonUuid(taxonUuid);
        config.setFeatureTree(featureTreeUuid);
        config.setEtAlPosition(etAlPosition);
        config.setWithFacts(doFacts);
        config.setWithKeys(doKeys);
        config.setWithMedia(doMedia);
        config.setWithSpecimens(doSpecimens);
        config.setWithSynonyms(doSynonyms);
        config.setWithTaxonNodes(doTaxonNodes);
        config.setWithTaxonRelationships(doTaxonRelations);
        config.setAnnotationTypes(annotationTypes);
        config.setMarkerTypes(markerTypes);
        config.setDirectNameRelTyes(directNameRelations);
        config.setInverseNameRelTyes(inverseNameRelations);
        config.setUseDtoLoading(dtoLoading);

        //filter
        config.setIncludeUnpublished(includeUnpublished);
        config.setSpecimenAssociationFilter(taxonOccurrenceRelTypes);

        Set<MarkerType> fallbackAreaMarkerTypes = new HashSet<>();
        if(!CdmUtils.isNullSafeEmpty(fallbackAreaMarkerTypeList)){
            fallbackAreaMarkerTypes = fallbackAreaMarkerTypeList.asSet();
        }

        Set<MarkerType> alternativeRootAreaMarkerTypes = new HashSet<>();
        if(!CdmUtils.isNullSafeEmpty(alternativeRootAreaMarkerTypeList)){
            alternativeRootAreaMarkerTypes = alternativeRootAreaMarkerTypeList.asSet();
        }


        //default distribution info config
        if (omitLevels ==null) {
            omitLevels = new HashSet<>();
        }
        DistributionInfoConfiguration distributionConfig = config.getDistributionInfoConfiguration();
        distributionConfig.setIncludeUnpublished(includeUnpublished);
        distributionConfig.setInfoParts(EnumSet.copyOf(partSet));
        distributionConfig.setPreferSubAreas(preferSubAreas);
        distributionConfig.setStatusOrderPreference(statusOrderPreference);
        distributionConfig.setAreaTree(areaTreeUuid);
        distributionConfig.setStatusTree(statusTreeUuid);
        distributionConfig.setOmitLevels(omitLevels);
        distributionConfig.setStatusColorsString(statusColorsString);
        distributionConfig.setDistributionOrder(distributionOrder);
        if (neverUseFallbackAreaAsParent != null) {
            distributionConfig.setNeverUseFallbackAreaAsParent(neverUseFallbackAreaAsParent);
        }
        if (recipe != null) {
            CondensedDistributionConfiguration condensedConfig = recipe.toConfiguration();
            condensedConfig.alternativeRootAreaMarkers = getUuids(alternativeRootAreaMarkerTypes);
            distributionConfig.setCondensedDistributionConfiguration(condensedConfig);
        }
        distributionConfig.setFallbackAreaMarkerTypes(fallbackAreaMarkerTypes); //was (remove if current implementation works): fallbackAreaMarkerTypes.stream().map(mt->mt.getUuid()).collect(Collectors.toSet());
        distributionConfig.setAlternativeRootAreaMarkerTypes(alternativeRootAreaMarkerTypes);

        //IUCN distribution info config
        DistributionInfoConfiguration iucnDistributionConfig = new DistributionInfoConfiguration();
        iucnDistributionConfig.setIncludeUnpublished(includeUnpublished);
        config.putDistributionInfoConfiguration(Feature.uuidIucnStatus, iucnDistributionConfig);
        EnumSet<InfoPart> iucnPartSet = EnumSet.of(InfoPart.condensedDistribution);
        iucnDistributionConfig.setInfoParts(iucnPartSet);

        iucnDistributionConfig.setPreferSubAreas(preferSubAreas);
        iucnDistributionConfig.setStatusOrderPreference(statusOrderPreference);
        iucnDistributionConfig.setAreaTree(areaTreeUuid);
        //TODO IUCN status tree?
        iucnDistributionConfig.setOmitLevels(omitLevels);
//        distributionConfig.setStatusColorsString(statusColorsString);
        iucnDistributionConfig.setDistributionOrder(distributionOrder);
        CondensedDistributionRecipe iucnRecipe = CondensedDistributionRecipe.IUCN;
        if (iucnRecipe != null) {
            CondensedDistributionConfiguration condensedConfig = iucnRecipe.toConfiguration();
            condensedConfig.alternativeRootAreaMarkers = getUuids(alternativeRootAreaMarkerTypes);
            iucnDistributionConfig.setCondensedDistributionConfiguration(condensedConfig);
        }
        iucnDistributionConfig.setFallbackAreaMarkerTypes(fallbackAreaMarkerTypes);
        iucnDistributionConfig.setAlternativeRootAreaMarkerTypes(alternativeRootAreaMarkerTypes);

        TaxonPageDto dto = portalService.taxonPageDto(config);
        return dto;
    }


    /**
     * TODO documentation
     *
     * @param taxonUuid the taxon uuid
     * @param subtreeUuid the taxon node subtree filter
     * @throws IOException
     */
    @RequestMapping(
            value = {"page"},
            method = RequestMethod.POST,
            consumes = "application/json")

    @ResponseBody
    public TaxonPageDto doGetTaxonPagePOST(@PathVariable("uuid") UUID taxonUuid,
            InputStream requestDto,


            //TODO configuration data
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        //maybe become a parameter, but first test if
        Boolean neverUseFallbackAreaAsParent = null;   //currently default is true in configuration
        String param_value = request.getParameter("annotationTypes");
        Map<String, String[]> param_map = request.getParameterMap();
        StringBuilder jsonStringBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(requestDto));
            String line = null;
            while ((line = in.readLine()) != null) {
                jsonStringBuilder.append(line);
            }
        } catch (Exception e) {
            logger.error("doGetTaxonPage() - parsing error" + e.getMessage());
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonStringBuilder.toString());
        JSONObject jsonObject = new JSONObject(jsonStringBuilder.toString());
        boolean includeUnpublished = NO_UNPUBLISHED;

        if(request != null){
            logger.info("doGetTaxonPage() " + requestPathAndQuery(request));
        }

        //TODO for now hardcoded
        DefinedTermBaseList alternativeRootAreaMarkerTypeList = new DefinedTermBaseList<>();
        UUID alternativeRootAreaMarkerTypeUuid = MarkerType.uuidAlternativeRootArea;
        MarkerType defaultAlternativeRootAreaMarkerType = (MarkerType)termService.find(alternativeRootAreaMarkerTypeUuid);
        if (defaultAlternativeRootAreaMarkerType != null) {
            alternativeRootAreaMarkerTypeList.add(defaultAlternativeRootAreaMarkerType);
        }
//
//        //TODO is this current state of art?
////        ModelAndView mv = new ModelAndView();
//
//        //check taxon exists and not filtered
        Taxon taxon = getCdmBaseInstance(Taxon.class, taxonUuid, response, getTaxonNodeInitStrategy().getPropertyPaths());
        UUID subtreeUuid = jsonNode.has("subtreeUuid")? UUID.fromString(jsonNode.get("subtreeUuid").textValue()): null;

        TaxonNode subtree = getSubtreeOrError(subtreeUuid, taxonNodeService, response);
        taxon = checkExistsSubtreeAndAccess(taxon, subtree, NO_UNPUBLISHED, response);

        String partSetString = jsonNode.has("part")? jsonNode.get("part").textValue(): null;

        EnumSet<InfoPart> partSet = null;
        if (partSetString == null) {
           partSet = EnumSet.of(InfoPart.condensedDistribution, InfoPart.mapUriParams, InfoPart.tree);
        }else {
            partSet = EnumSet.copyOf(Arrays.stream(partSetString.split(",")).map(str->InfoPart.valueOf(str)).collect(Collectors.toSet()));
        }
//        //TODO null check needed?
        Set<UUID> annotationTypes = new HashSet<>();
        String annotationTypeString = jsonNode.has("annotationTypes")? jsonNode.get("annotationTypes").textValue(): null;
        if (annotationTypeString != null) {
            annotationTypes = (Arrays.stream(annotationTypeString.split(",")).map(a->UUID.fromString(a)).collect(Collectors.toSet()));
        }

        Set<UUID> markerTypes = new HashSet<>();
        String markerTypeString = jsonNode.has("markerTypes")? jsonNode.get("markerTypes").textValue(): null;
        if (markerTypeString != null) {
            markerTypes = (Arrays.stream(markerTypeString.split(",")).map(a->UUID.fromString(a)).collect(Collectors.toSet()));
        }
//
////      //TODO is this performant?
////      IVocabularyService vocabularyService = null;
////      Map<PresenceAbsenceTerm, Color> distributionStatusColors = DistributionServiceUtilities.buildStatusColorMap(
////              statusColorsString, termService, vocabularyService);
//
        TaxonPageDtoConfiguration config = new TaxonPageDtoConfiguration();
//
        config.setTaxonUuid(taxonUuid);
        UUID featureTreeUuid = jsonNode.has("featureTreeUuid")?UUID.fromString(jsonNode.get("featureTreeUuid").textValue()): null;
        config.setFeatureTree(featureTreeUuid);
        Integer etAlPosition = jsonNode.has("etAlPosition")?jsonNode.get("etAlPosition").intValue(): null;
        config.setEtAlPosition(etAlPosition);
        Boolean doFacts = jsonNode.has("doFacts")? (jsonNode.get("doFacts").intValue()> 0): false;
        config.setWithFacts(doFacts);
        Boolean doKeys = jsonNode.has("doKeys")? (jsonNode.get("doKeys").intValue()>0): false;
        config.setWithKeys(doKeys);
        Boolean doMedia = jsonNode.has("doMedia")? (jsonNode.get("doMedia").intValue()>0): false;
        config.setWithMedia(doMedia);
        Boolean doSpecimens = jsonNode.has("doSpecimens")? (jsonNode.get("doSpecimens").intValue()>0): false;
        config.setWithSpecimens(doSpecimens);
        Boolean doSynonyms = jsonNode.has("doSynonyms")? (jsonNode.get("doSynonyms").intValue()>0): false;
        config.setWithSynonyms(doSynonyms);
        Boolean doTaxonNodes = jsonNode.has("doTaxonNodes")? (jsonNode.get("doTaxonNodes").intValue()>0): false;
        config.setWithTaxonNodes(doTaxonNodes);
        Boolean doTaxonRelations = jsonNode.has("doTaxonRelations")? (jsonNode.get("doTaxonRelations").intValue()>0): false;
        config.setWithTaxonRelationships(doTaxonRelations);

        config.setAnnotationTypes(annotationTypes);
        config.setMarkerTypes(markerTypes);
//
//
//        //filter
        config.setIncludeUnpublished(includeUnpublished);

        String taxOccRelFilter = jsonNode.has("taxOccRelFilter")? jsonNode.get("taxOccRelFilter").textValue(): null;
        EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes = bindAssociationFilter(taxOccRelFilter);

        config.setSpecimenAssociationFilter(taxonOccurrenceRelTypes);
//
        Set<MarkerType> fallbackAreaMarkerTypes = new HashSet<>();

        String fallbackAreaMarkerTypesString = jsonNode.has("fallbackAreaMarkerTypes")? jsonNode.get("fallbackAreaMarkerTypes").textValue(): null;
        if (fallbackAreaMarkerTypesString != null) {
            fallbackAreaMarkerTypes = (Arrays.stream(fallbackAreaMarkerTypesString.split(",")).map(a->(MarkerType.getTermByUUID(UUID.fromString(a), MarkerType.class) )).collect(Collectors.toSet()));
        }

        Set<MarkerType> alternativeRootAreaMarkerTypes = new HashSet<>();
        String alternativeRootAreaMarkerTypesString = jsonNode.has("alternativeRootAreaMarkerTypes")? jsonNode.get("alternativeRootAreaMarkerTypes").textValue(): null;
        if (alternativeRootAreaMarkerTypesString != null) {
            alternativeRootAreaMarkerTypes = (Arrays.stream(alternativeRootAreaMarkerTypesString.split(",")).map(a->(MarkerType.getTermByUUID(UUID.fromString(a), MarkerType.class) )).collect(Collectors.toSet()));
        }

        Set<UUID> omitLevels = new HashSet<>();
        String omitLevelsString = jsonNode.has("omitLevels")? jsonNode.get("omitLevels").textValue(): null;
        if (StringUtils.isNotBlank(omitLevelsString)) {
            omitLevels = (Arrays.stream(omitLevelsString.split(",")).map(a->UUID.fromString(a)).collect(Collectors.toSet()));
        }
        DistributionInfoConfiguration distributionConfig = config.getDistributionInfoConfiguration();
        distributionConfig.setIncludeUnpublished(includeUnpublished);
        distributionConfig.setInfoParts(EnumSet.copyOf(partSet));
        Boolean preferSubAreas = jsonNode.has("preferSubAreas")? (jsonNode.get("preferSubAreas").intValue()>0): false;
        distributionConfig.setPreferSubAreas(preferSubAreas);
        Boolean statusOrderPreference = jsonNode.has("statusOrderPreference")? (jsonNode.get("statusOrderPreference").intValue()>0): false;
        distributionConfig.setStatusOrderPreference(statusOrderPreference);
        UUID areaTreeUuid = jsonNode.has("areaTreeUuid")? UUID.fromString(jsonNode.get("areaTreeUuid").textValue()): null;
        distributionConfig.setAreaTree(areaTreeUuid);
        UUID statusTreeUuid = jsonNode.has("statusTreeUuid")? UUID.fromString(jsonNode.get("statusTreeUuid").textValue()): null;
        distributionConfig.setStatusTree(statusTreeUuid);
        distributionConfig.setOmitLevels(omitLevels);
        String statusColorsString = jsonNode.has("statusColorsString")? jsonNode.get("statusColorsString").textValue(): null;
        distributionConfig.setStatusColorsString(statusColorsString);
        DistributionOrder distributionOrder = jsonNode.has("distributionOrder")? DistributionOrder.valueOf(jsonNode.get("statusTreeUuid").textValue()): null;
        distributionConfig.setDistributionOrder(distributionOrder);
        neverUseFallbackAreaAsParent = jsonNode.has("neverUseFallbackAreaAsParent")? (jsonNode.get("neverUseFallbackAreaAsParent").intValue()>0): null;
        if (neverUseFallbackAreaAsParent != null) {
            distributionConfig.setNeverUseFallbackAreaAsParent(neverUseFallbackAreaAsParent);
        }
        CondensedDistributionRecipe recipe = jsonNode.has("recipe")? CondensedDistributionRecipe.valueOf(jsonNode.get("recipe").textValue()): null;
        if (recipe != null) {
            CondensedDistributionConfiguration condensedConfig = recipe.toConfiguration();
            condensedConfig.alternativeRootAreaMarkers = getUuids(alternativeRootAreaMarkerTypes);
            distributionConfig.setCondensedDistributionConfiguration(condensedConfig);
        }
        distributionConfig.setFallbackAreaMarkerTypes(fallbackAreaMarkerTypes); //was (remove if current implementation works): fallbackAreaMarkerTypes.stream().map(mt->mt.getUuid()).collect(Collectors.toSet());
        distributionConfig.setAlternativeRootAreaMarkerTypes(alternativeRootAreaMarkerTypes);
//
//        //IUCN distribution info config
        DistributionInfoConfiguration iucnDistributionConfig = new DistributionInfoConfiguration();
        iucnDistributionConfig.setIncludeUnpublished(includeUnpublished);
        config.putDistributionInfoConfiguration(Feature.uuidIucnStatus, iucnDistributionConfig);
        EnumSet<InfoPart> iucnPartSet = EnumSet.of(InfoPart.condensedDistribution);
        iucnDistributionConfig.setInfoParts(iucnPartSet);

        iucnDistributionConfig.setPreferSubAreas(preferSubAreas);
        iucnDistributionConfig.setStatusOrderPreference(statusOrderPreference);
        iucnDistributionConfig.setAreaTree(areaTreeUuid);
//        //TODO IUCN status tree?
        iucnDistributionConfig.setOmitLevels(omitLevels);
//        distributionConfig.setStatusColorsString(statusColorsString);
        iucnDistributionConfig.setDistributionOrder(distributionOrder);
        CondensedDistributionRecipe iucnRecipe = CondensedDistributionRecipe.IUCN;
        if (iucnRecipe != null) {
            CondensedDistributionConfiguration condensedConfig = iucnRecipe.toConfiguration();
            condensedConfig.alternativeRootAreaMarkers = getUuids(alternativeRootAreaMarkerTypes);
            iucnDistributionConfig.setCondensedDistributionConfiguration(condensedConfig);
        }
        iucnDistributionConfig.setFallbackAreaMarkerTypes(fallbackAreaMarkerTypes);
        iucnDistributionConfig.setAlternativeRootAreaMarkerTypes(alternativeRootAreaMarkerTypes);

        config.setIncludeUnpublished(includeUnpublished);
        config.setTaxonUuid(taxonUuid);

        TaxonPageDto dto = portalService.taxonPageDto(config);
        return dto;
    }

    private Set<UUID> getUuids(Set<? extends CdmBase> entities) {
        return entities.stream().map(e->e.getUuid()).collect(Collectors.toSet());
    }

    /**
     * Get the synonymy for a taxon identified by the <code>{taxon-uuid}</code>.
     * The synonymy consists
     * of two parts: The group of homotypic synonyms of the taxon and the
     * heterotypic synonymy groups of the taxon. The synonymy is ordered
     * historically by the type designations and by the publication date of the
     * nomenclatural reference
     * <p>
     * URI:
     * <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;synonymy</b>
     *
     *
     * @param request
     * @param response
     * @return a Map with two entries which are mapped by the following keys:
     *         "homotypicSynonymsByHomotypicGroup", "heterotypicSynonymyGroups",
     *         containing lists of {@link Synonym}s which // TODO Auto-generated catch block
                    e.printStackTrace();are initialized using the
     *         following initialization strategy: {@link #SYNONYMY_INIT_STRATEGY}
     *
     * @throws IOException
     */
    @RequestMapping(
            value = {"synonymy"},
            method = RequestMethod.GET)
    public ModelAndView doGetSynonymy(@PathVariable("uuid") UUID taxonUuid,
            @RequestParam(value = "subtree", required = false) UUID subtreeUuid,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException {

        boolean includeUnpublished = NO_UNPUBLISHED;
        if(request != null){
            logger.info("doGetSynonymy() " + requestPathAndQuery(request));
        }
        ModelAndView mv = new ModelAndView();

        Taxon taxon = getCdmBaseInstance(Taxon.class, taxonUuid, response, getTaxonNodeInitStrategy().getPropertyPaths());
        TaxonNode subtree = getSubtreeOrError(subtreeUuid, taxonNodeService, response);
        taxon = checkExistsSubtreeAndAccess(taxon, subtree, NO_UNPUBLISHED, response);

        Map<String, List<?>> synonymy = new Hashtable<>();

        //new
        List<List<Synonym>> synonymyGroups = service.getSynonymsByHomotypicGroup(taxon, SYNONYMY_INIT_STRATEGY.getPropertyPaths());
        if(!includeUnpublished){
            synonymyGroups = removeUnpublishedSynonyms(synonymyGroups);
        }

        synonymy.put("homotypicSynonymsByHomotypicGroup", synonymyGroups.get(0));
        synonymyGroups.remove(0);
        synonymy.put("heterotypicSynonymyGroups", synonymyGroups);

        //old
//        synonymy.put("homotypicSynonymsByHomotypicGroup", service.getHomotypicSynonymsByHomotypicGroup(taxon, SYNONYMY_INIT_STRATEGY));
//        synonymy.put("heterotypicSynonymyGroups", service.getHeterotypicSynonymyGroups(taxon, SYNONYMY_INIT_STRATEGY));

        mv.addObject(synonymy);
        return mv;
    }


    /**
     * @param synonymyGroups
     */
    private List<List<Synonym>> removeUnpublishedSynonyms(List<List<Synonym>> synonymyGroups) {
        List<List<Synonym>> result = new ArrayList<>();
        boolean isHomotypicToAccepted = true;

        for (List<Synonym> oldList : synonymyGroups){
            List<Synonym> newList = new ArrayList<>();
            for (Synonym oldSyn : oldList){
                if (oldSyn.isPublish()){
                    newList.add(oldSyn);
                }
            }
            if (isHomotypicToAccepted || !newList.isEmpty()){
                result.add(newList);
            }
            isHomotypicToAccepted = false;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getTaxonDescriptionInitStrategy() {
        return TAXONDESCRIPTION_INIT_STRATEGY.getPropertyPaths();
    }

    @Override
    protected List<String> getTaxonDescriptionElementInitStrategy() {
        return DESCRIPTION_ELEMENT_INIT_STRATEGY.getPropertyPaths();
    }

    @Override
    protected  EntityInitStrategy getTaxonNodeInitStrategy() {
        return TAXONNODE_INIT_STRATEGY;
    }

    /**
     * Get the list of {@link TaxonRelationship}s for the given
     * {@link TaxonBase} instance identified by the <code>{taxon-uuid}</code>.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;taxonRelationships</b>
     *
     * @param request
     * @param response
     * @return a List of {@link TaxonRelationship} entities which are initialized
     *         using the following initialization strategy:
     *         {@link #TAXONRELATIONSHIP_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(
            value = {"taxonRelationships"},
            method = RequestMethod.GET)
    public List<TaxonRelationship> doGetTaxonRelations(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request, HttpServletResponse response)throws IOException {

        boolean includeUnpublished = NO_UNPUBLISHED;
        logger.info("doGetTaxonRelations()" + requestPathAndQuery(request));
        Taxon taxon = getCdmBaseInstance(Taxon.class, uuid, response, (List<String>)null);
        taxon = checkExistsAndAccess(taxon, includeUnpublished, response);

        List<TaxonRelationship> toRelationships = service.listToTaxonRelationships(taxon, null,
                includeUnpublished, null, null, null, TAXONRELATIONSHIP_INIT_STRATEGY.getPropertyPaths());
        List<TaxonRelationship> fromRelationships = service.listFromTaxonRelationships(taxon, null,
                includeUnpublished, null, null, null, TAXONRELATIONSHIP_INIT_STRATEGY.getPropertyPaths());

        List<TaxonRelationship> allRelationships = new ArrayList<>(toRelationships.size() + fromRelationships.size());
        allRelationships.addAll(toRelationships);
        allRelationships.addAll(fromRelationships);

        return allRelationships;
    }

    /**
     * Get the list of {@link NameRelationship}s of the Name associated with the
     * {@link TaxonBase} instance identified by the <code>{taxon-uuid}</code>.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;nameRelationships</b>
     *
     * @param request
     * @param response
     * @return a List of {@link NameRelationship} entities which are initialized
     *         using the following initialization strategy:
     *         {@link #NAMERELATIONSHIP_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(
            value = {"toNameRelationships"},
            method = RequestMethod.GET)
    public List<NameRelationship> doGetToNameRelations(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request, HttpServletResponse response)throws IOException {
        logger.info("doGetNameRelations()" + request.getRequestURI());
        boolean includeUnpublished = NO_UNPUBLISHED;

        TaxonBase<?> taxonBase = getCdmBaseInstance(TaxonBase.class, uuid, response, (List<String>)null);
        taxonBase = checkExistsAndAccess(taxonBase, includeUnpublished, response);

        List<NameRelationship> list = nameService.listNameRelationships(taxonBase.getName(), Direction.relatedTo, null, null, 0, null, NAMERELATIONSHIP_INIT_STRATEGY.getPropertyPaths());
        return list;
    }

    /**
     * Get the list of {@link NameRelationship}s of the Name associated with the
     * {@link TaxonBase} instance identified by the <code>{taxon-uuid}</code>.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;nameRelationships</b>
     *
     * @param request
     * @param response
     * @return a List of {@link NameRelationship} entities which are initialized
     *         using the following initialization strategy:
     *         {@link #NAMERELATIONSHIP_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(
            value = {"fromNameRelationships"},
            method = RequestMethod.GET)
    public List<NameRelationship> doGetFromNameRelations(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException {
        logger.info("doGetNameFromNameRelations()" + requestPathAndQuery(request));

        boolean includeUnpublished = NO_UNPUBLISHED;

        TaxonBase<?> taxonBase = getCdmBaseInstance(TaxonBase.class, uuid, response, SIMPLE_TAXON_INIT_STRATEGY.getPropertyPaths());
        taxonBase = checkExistsAndAccess(taxonBase, includeUnpublished, response);

        List<NameRelationship> list = nameService.listNameRelationships(taxonBase.getName(), Direction.relatedFrom, null, null, 0, null, NAMERELATIONSHIP_INIT_STRATEGY.getPropertyPaths());

        return list;
    }


//	@RequestMapping(value = "specimens", method = RequestMethod.GET)
//	public ModelAndView doGetSpecimens(
//			@PathVariable("uuid") UUID uuid,
//			HttpServletRequest request,
//			HttpServletResponse response) throws IOException, ClassNotFoundException {
//		logger.info("doGetSpecimens() - " + request.getRequestURI());
//
//		ModelAndView mv = new ModelAndView();
//
//		List<DerivedUnitFacade> derivedUnitFacadeList = new ArrayList<>();
//
//		// find speciemens in the TaxonDescriptions
//		List<TaxonDescription> taxonDescriptions = doGetDescriptions(uuid, request, response);
//		if (taxonDescriptions != null) {
//
//			for (TaxonDescription description : taxonDescriptions) {
//				derivedUnitFacadeList.addAll( occurrenceService.listDerivedUnitFacades(description, null) );
//			}
//		}
//		// TODO find specimens in the NameDescriptions ??
//
//		// TODO also find type specimens
//
//		mv.addObject(derivedUnitFacadeList);
//
//		return mv;
//	}

    /**
     * Get the {@link Media} attached to the {@link Taxon} instance
     * identified by the <code>{taxon-uuid}</code>.
     *
     * Usage &#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-
     * uuid}&#x002F;media&#x002F;{mime type
     * list}&#x002F;{size}[,[widthOrDuration}][,{height}]&#x002F;
     *
     * Whereas
     * <ul>
     * <li><b>{mime type list}</b>: a comma separated list of mime types, in the
     * order of preference. The forward slashes contained in the mime types must
     * be replaced by a colon. Regular expressions can be used. Each media
     * associated with this given taxon is being searched whereas the first
     * matching mime type matching a representation always rules.</li>
     * <li><b>{size},{widthOrDuration},{height}</b>: <i>not jet implemented</i>
     * valid values are an integer or the asterisk '*' as a wildcard</li>
     * </ul>
     *
     * @param request
     * @param response
     * @return a List of {@link Media} entities which are initialized
     *         using the following initialization strategy:
     *         {@link #TAXONDESCRIPTION_INIT_STRATEGY}
     * @throws IOException
     */
//    @RequestMapping(
//        value = {"media"},
//        method = RequestMethod.GET)
//    public List<Media> doGetMedia(
//            @PathVariable("uuid") UUID uuid,
//            @RequestParam(value = "type", required = false) Class<? extends MediaRepresentationPart> type,
//            @RequestParam(value = "mimeTypes", required = false) String[] mimeTypes,
//            @RequestParam(value = "relationships", required = false) UuidList relationshipUuids,
//            @RequestParam(value = "relationshipsInvers", required = false) UuidList relationshipInversUuids,
//            @RequestParam(value = "includeTaxonDescriptions", required = true) Boolean  includeTaxonDescriptions,
//            @RequestParam(value = "includeOccurrences", required = true) Boolean  includeOccurrences,
//            @RequestParam(value = "includeTaxonNameDescriptions", required = true) Boolean  includeTaxonNameDescriptions,
//            @RequestParam(value = "widthOrDuration", required = false) Integer  widthOrDuration,
//            @RequestParam(value = "height", required = false) Integer height,
//            @RequestParam(value = "size", required = false) Integer size,
//            HttpServletRequest request, HttpServletResponse response) throws IOException {
//
//        logger.info("doGetMedia() " + requestPathAndQuery(request));
//
//        List<String> initStrategy = null;
//
//        EntityMediaContext<Taxon> taxonMediaContext = loadMediaForTaxonAndRelated(uuid, relationshipUuids,
//                relationshipInversUuids, includeTaxonDescriptions, includeOccurrences, includeTaxonNameDescriptions,
//                response, initStrategy, MediaPortalController.MEDIA_INIT_STRATEGY.getPropertyPaths());
//
//        List<Media> mediafilteredForPreferredRepresentations = mediaToolbox.processAndFilterPreferredMediaRepresentations(type, mimeTypes, widthOrDuration, height, size,
//                taxonMediaContext.media);
//
//        return mediafilteredForPreferredRepresentations;
//    }

    @RequestMapping(
            value = {"media"},
            method = RequestMethod.GET)
    public List<Media> doGetMedia(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "type", required = false) Class<? extends MediaRepresentationPart> type,
            @RequestParam(value = "mimeTypes", required = false) String[] mimeTypes,
            @RequestParam(value = "relationships", required = false) UuidList relationshipUuids,
            @RequestParam(value = "relationshipsInvers", required = false) UuidList relationshipInversUuids,
            @RequestParam(value = "includeTaxonDescriptions", required = true) Boolean  includeTaxonDescriptions,
            @RequestParam(value = "includeOccurrences", required = true) Boolean  includeOccurrences,
            @RequestParam(value = "taxOccRelFilter", required = false) String taxOccRelFilter,
            @RequestParam(value = "includeOriginals", required = false) Boolean  includeOriginals,
            @RequestParam(value = "includeTaxonNameDescriptions", required = true) Boolean  includeTaxonNameDescriptions,
            @RequestParam(value = "widthOrDuration", required = false) Integer  widthOrDuration,
            @RequestParam(value = "height", required = false) Integer height,
            @RequestParam(value = "size", required = false) Integer size,
            HttpServletRequest request, HttpServletResponse response) throws IOException {

        logger.info("doGetMedia() " + requestPathAndQuery(request));

        EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes = bindAssociationFilter(taxOccRelFilter);

        List<String> initStrategy = null;

        EntityMediaContext<Taxon> taxonMediaContext = loadMediaForTaxonAndRelated(uuid, relationshipUuids,
            relationshipInversUuids, includeTaxonDescriptions, includeOccurrences,
            taxonOccurrenceRelTypes, includeOriginals, includeTaxonNameDescriptions,
            response, initStrategy, MediaPortalController.MEDIA_INIT_STRATEGY.getPropertyPaths());

        List<Media> mediafilteredForPreferredRepresentations = mediaToolbox.processAndFilterPreferredMediaRepresentations(
            type, mimeTypes, widthOrDuration, height, size, taxonMediaContext.media);

        return mediafilteredForPreferredRepresentations;
    }

    public  EntityMediaContext<Taxon> loadMediaForTaxonAndRelated(UUID taxonUuid,
            UuidList relationshipUuids, UuidList relationshipInversUuids,
            Boolean includeTaxonDescriptions, Boolean includeOccurrences,
            EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes,
            Boolean includeTaxonNameDescriptions,
            HttpServletResponse response,
            List<String> taxonInitStrategy, List<String> mediaInitStrategy) throws IOException {

        return loadMediaForTaxonAndRelated(taxonUuid,
                relationshipUuids, relationshipInversUuids,
                includeTaxonDescriptions, includeOccurrences, taxonOccurrenceRelTypes,
                false, includeTaxonNameDescriptions,
                response, taxonInitStrategy, mediaInitStrategy);
    }

    public  EntityMediaContext<Taxon> loadMediaForTaxonAndRelated(UUID taxonUuid,
            UuidList relationshipUuids, UuidList relationshipInversUuids,
            Boolean includeTaxonDescriptions, Boolean includeOccurrences,
            EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes,
            Boolean includeOriginals, Boolean includeTaxonNameDescriptions,
            HttpServletResponse response,
            List<String> taxonInitStrategy, List<String> mediaInitStrategy) throws IOException {

        boolean includeUnpublished = NO_UNPUBLISHED;

        Taxon taxon = getCdmBaseInstance(Taxon.class, taxonUuid, response, taxonInitStrategy);
        taxon = checkExistsAndAccess(taxon, includeUnpublished, response);

        Set<TaxonRelationshipEdge> includeRelationships = ControllerUtils.loadIncludeRelationships(relationshipUuids, relationshipInversUuids, termService);

        List<Media> media = listMediaForTaxon(taxon, includeRelationships,
                includeTaxonDescriptions, includeOccurrences, includeOriginals, includeTaxonNameDescriptions, includeUnpublished, mediaInitStrategy);

        EntityMediaContext<Taxon> entityMediaContext = new EntityMediaContext<>(taxon, media);

        return entityMediaContext;
    }

    @RequestMapping(
            value = {"subtree/media"},
            method = RequestMethod.GET)
    public List<Media> doGetSubtreeMedia(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "type", required = false) Class<? extends MediaRepresentationPart> type,
            @RequestParam(value = "mimeTypes", required = false) String[] mimeTypes,
            @RequestParam(value = "relationships", required = false) UuidList relationshipUuids,
            @RequestParam(value = "relationshipsInvers", required = false) UuidList relationshipInversUuids,
            @RequestParam(value = "includeTaxonDescriptions", required = true) Boolean  includeTaxonDescriptions,
            @RequestParam(value = "includeOccurrences", required = true) Boolean  includeOccurrences,
            @RequestParam(value = "includeTaxonNameDescriptions", required = true) Boolean  includeTaxonNameDescriptions,
            @RequestParam(value = "widthOrDuration", required = false) Integer  widthOrDuration,
            @RequestParam(value = "height", required = false) Integer height,
            @RequestParam(value = "size", required = false) Integer size,
            HttpServletRequest request, HttpServletResponse response)throws IOException {

        boolean includeUnpublished = NO_UNPUBLISHED;

        logger.info("doGetSubtreeMedia() " + requestPathAndQuery(request));

        List<String> initStrategy = TAXON_WITH_CHILDNODES_INIT_STRATEGY.getPropertyPaths();

        Taxon taxon = getCdmBaseInstance(Taxon.class, uuid, response, initStrategy);
        taxon = checkExistsAndAccess(taxon, includeUnpublished, response);

        Set<TaxonRelationshipEdge> includeRelationships = ControllerUtils.loadIncludeRelationships(relationshipUuids, relationshipInversUuids, termService);

        boolean includeOriginals = false; //or when unifying methods, do we want this as webservice parameter, too?
        List<Media> media = listMediaForTaxon(taxon, includeRelationships,
                includeTaxonDescriptions, includeOccurrences, includeOriginals, includeTaxonNameDescriptions, includeUnpublished, null);
        media = addTaxonomicChildrenMedia(includeTaxonDescriptions, includeOccurrences, includeOriginals,
                includeTaxonNameDescriptions, taxon,
                includeRelationships, media, includeUnpublished);

        List<Media> mediafilteredForPreferredRepresentations = mediaToolbox.processAndFilterPreferredMediaRepresentations(type, mimeTypes, widthOrDuration, height, size,
                media);

        return mediafilteredForPreferredRepresentations;
    }

    public List<Media> addTaxonomicChildrenMedia(Boolean includeTaxonDescriptions, Boolean includeOccurrences,
            boolean includeOriginals, Boolean includeTaxonNameDescriptions, Taxon taxon,
            Set<TaxonRelationshipEdge> includeRelationships, List<Media> media, boolean includeUnpublished) {

        //TODO use treeindex
        //looking for all medias of direct children
        TaxonNode node;
        if (taxon.getTaxonNodes().size()>0){
            Set<TaxonNode> nodes = taxon.getTaxonNodes();
            Iterator<TaxonNode> iterator = nodes.iterator();
            //TaxonNode holen
            node = iterator.next();
            //Check if TaxonNode belongs to the current tree

            node = taxonNodeService.load(node.getUuid(), TAXONNODE_WITH_CHILDNODES_INIT_STRATEGY.getPropertyPaths());
            List<TaxonNode> children = node.getChildNodes();
            Taxon childTaxon;
            for (TaxonNode child : children){
                childTaxon = child.getTaxon();
                if(childTaxon != null) {
                    childTaxon = (Taxon)taxonService.load(childTaxon.getUuid(), NO_UNPUBLISHED, null);
                    media.addAll(listMediaForTaxon(childTaxon, includeRelationships,
                            includeTaxonDescriptions, includeOccurrences, includeOriginals,
                            includeTaxonNameDescriptions, includeUnpublished, MediaPortalController.MEDIA_INIT_STRATEGY.getPropertyPaths()));
                }
            }
        }
        return media;
    }

    private List<Media> listMediaForTaxon(Taxon taxon, Set<TaxonRelationshipEdge> includeRelationships,
            Boolean includeTaxonDescriptions, Boolean includeOccurrences, Boolean includeOriginals,
            Boolean includeTaxonNameDescriptions, boolean includeUnpublished, List<String> propertyPath) {

        List<Media> media = service.listMedia(taxon, includeRelationships,
                false, includeTaxonDescriptions, includeOccurrences, includeOriginals,
                includeTaxonNameDescriptions, includeUnpublished, propertyPath);

        return media;
    }

    public class EntityMediaContext<T extends IdentifiableEntity> {

        private T entity;
        private List<Media> media;

        public EntityMediaContext(T entity, List<Media> media) {
            this.entity = HibernateProxyHelper.deproxy(entity);
            this.media = media;
        }

        public T getEntity() {
            return entity;
        }
        public List<Media> getMedia() {
            return media;
        }

        /**
         * @param addTaxonomicChildrenMedia
         */
        public void setMedia(List<Media> media) {
            this.media = media;

        }
    }

// ---------------------- code snippet preserved for possible later use --------------------
//	@RequestMapping(
//			value = {"//*/portal/taxon/*/descriptions"}, // mapped as absolute path, see CdmAntPathMatcher
//			method = RequestMethod.GET)
//	public List<TaxonDescription> doGetDescriptionsbyFeatureTree(HttpServletRequest request, HttpServletResponse response)throws IOException {
//		TaxonBase tb = getCdmBase(request, response, null, Taxon.class);
//		if(tb instanceof Taxon){
//			//T O D O this is a quick and dirty implementation -> generalize
//			UUID featureTreeUuid = readValueUuid(request, termTreeUuidPattern);
//
//			FeatureTree featureTree = descriptionService.getFeatureTreeByUuid(featureTreeUuid);
//			Pager<TaxonDescription> p = descriptionService.getTaxonDescriptions((Taxon)tb, null, null, null, null, TAXONDESCRIPTION_INIT_STRATEGY);
//			List<TaxonDescription> descriptions = p.getRecords();
//
//			if(!featureTree.isDescriptionSeparated()){
//
//				TaxonDescription superDescription = TaxonDescription.NewInstance();
//				//put all descriptionElements in superDescription and make it invisible
//				for(TaxonDescription description: descriptions){
//					for(DescriptionElementBase element: description.getElements()){
//						superDescription.addElement(element);
//					}
//				}
//				List<TaxonDescription> separatedDescriptions = new ArrayList<TaxonDescription>(descriptions.size());
//				separatedDescriptions.add(superDescription);
//				return separatedDescriptions;
//			}else{
//				return descriptions;
//			}
//		} else {
//			response.sendError(HttpServletResponse.SC_NOT_FOUND, "invalid type; Taxon expected but " + tb.getClass().getSimpleName() + " found.");
//			return null;
//		}
//	}

}
