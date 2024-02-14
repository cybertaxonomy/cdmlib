/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.dto.SpecimenOrObservationBaseDTO;
import eu.etaxonomy.cdm.api.filter.TaxonOccurrenceRelationType;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.config.FindOccurrencesConfigurator;
import eu.etaxonomy.cdm.api.service.config.IncludedTaxonConfiguration;
import eu.etaxonomy.cdm.api.service.dto.IncludedTaxaDTO;
import eu.etaxonomy.cdm.api.service.dto.TaxonRelationshipsDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.exception.UnpublishedException;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.persistence.dao.initializer.EntityInitStrategy;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.dto.common.StringResultDTO;
import eu.etaxonomy.cdm.remote.editor.TermBasePropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import io.swagger.annotations.Api;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @since 20.07.2009
 */
@Controller
@Api("taxon")
@RequestMapping(value = {"/taxon/{uuid}"})
public class TaxonController extends AbstractIdentifiableController<TaxonBase, ITaxonService>{

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private IOccurrenceService occurrenceService;

    @Autowired
    private INameService nameService;

    @Autowired
    private ITaxonNodeService nodeService;

    @Autowired
    private IDescriptionService descriptionService;

    @Autowired
    private ITermService termService;

    protected static final EntityInitStrategy TAXONNODE_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String []{
            "taxonNodes.classification",
            "taxonNodes.parent",
            "taxonNodes.parent.childNodes",   //currently needed to compute sortindex //TODO improve
            "taxonNodes.taxon.name",
            "taxonNodes.taxon.secSource.citation",
            "taxonNodes.statusNote",
            "acceptedTaxon.taxonNodes.classification"
    }));

    public TaxonController(){
        super();
        setInitializationStrategy(Arrays.asList(new String[]{
                "$",
                "name.nomenclaturalSource.citation"
                }
        ));
    }

    @Override
    @Autowired
    public void setService(ITaxonService service) {
        this.service = service;
    }

    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(MarkerType.class, new TermBasePropertyEditor<>(termService));
    }

    public static EnumSet<TaxonOccurrenceRelationType> bindAssociationFilter(String taxOccRelFilter) {
        //TODO implement as binder
        EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes = EnumSet.noneOf(TaxonOccurrenceRelationType.class);;
        if (!StringUtils.isEmpty(taxOccRelFilter) && !"ALL".equalsIgnoreCase(taxOccRelFilter)) {
            for (String split: taxOccRelFilter.split(",")){
                TaxonOccurrenceRelationType relType = TaxonOccurrenceRelationType.of(split);
                if(relType != null) {
                    taxonOccurrenceRelTypes.add(relType);
                }
            }
        }
        if (taxonOccurrenceRelTypes.isEmpty()) {
            taxonOccurrenceRelTypes = TaxonOccurrenceRelationType.All();

        }
        return taxonOccurrenceRelTypes;
    }

    protected List<String> getTaxonDescriptionInitStrategy() {
        return getInitializationStrategy(); // return Arrays.asList("$", "")
    }

    protected List<String> getTaxonDescriptionElementInitStrategy() {
        return getInitializationStrategy();
    }

    @RequestMapping(params="subtree", method = RequestMethod.GET)
    public TaxonBase<?> doGet(@PathVariable("uuid") UUID uuid,
            @RequestParam(value = "subtree", required = true) UUID subtreeUuid,  //if subtree does not exist the base class method is used, therefore required
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        if(request != null) {
            logger.info("doGet() " + requestPathAndQuery(request));
        }
        //TODO do we want to allow Synonyms at all? Maybe needs initialization
        EntityInitStrategy initStrategy = new EntityInitStrategy(getInitializationStrategy());
        initStrategy.extend(null, getTaxonNodeInitStrategy(), false);
        TaxonBase<?> taxonBase = getCdmBaseInstance(uuid, response, initStrategy.getPropertyPaths());
        //TODO we should move subtree check down to service or persistence
        TaxonNode subtree = getSubtreeOrError(subtreeUuid, nodeService, response);
        taxonBase = checkExistsSubtreeAndAccess(taxonBase, subtree, NO_UNPUBLISHED, response);
        return taxonBase;
    }

    /**
     * Checks if a {@link TaxonBase taxonBase} is public and belongs to a {@link TaxonNode subtree}
     * as accepted taxon or synonym.
     * If not the according {@link HttpStatusMessage http messages} are send to response.
     * <BR>
     * Not (yet) checked is the relation to a subtree via a concept relationship.
     * @param taxonBase
     * @param includeUnpublished
     * @param response
     * @return
     * @throws IOException
     */
    protected <S extends TaxonBase<?>> S checkExistsSubtreeAndAccess(S taxonBase,
            TaxonNode subtree, boolean includeUnpublished,
            HttpServletResponse response) throws IOException {
        taxonBase = checkExistsAndAccess(taxonBase, NO_UNPUBLISHED, response);
        if (subtree == null){
            return taxonBase;
        }else if(taxonBase != null){
            //TODO synonyms maybe can not be initialized
            Taxon taxon = taxonBase.isInstanceOf(Synonym.class)?
                    CdmBase.deproxy(taxonBase, Synonym.class).getAcceptedTaxon():
                    CdmBase.deproxy(taxonBase, Taxon.class);
            //check if taxon has any node that is a descendant of subtree
            for (TaxonNode taxonNode :taxon.getTaxonNodes()){
                if (subtree.isAncestor(taxonNode)){
                    return taxonBase;
                }
            }
            HttpStatusMessage.ACCESS_DENIED.send(response);
        }
        return null;
    }


    /**
     * Get the accepted {@link Taxon} for a given
     * {@link TaxonBase} entity identified by the <code>{taxon-uuid}</code>.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;accepted</b>
     *
     * @param request
     * @param response
     * @return a set on a list of {@link Taxon} entities which are initialized
     *         using the following initialization strategy:
     *         {@link #DEFAULT_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(value = "accepted", method = RequestMethod.GET)
    public Taxon doGetAcceptedFor(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "classificationFilter", required = false) UUID classification_uuid,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        if(request != null){
            logger.info("doGetAcceptedFor() " + requestPathAndQuery(request));
        }

        try {
            boolean includeUnpublished = NO_UNPUBLISHED;
            Taxon result = service.findAcceptedTaxonFor(uuid, classification_uuid, includeUnpublished, getInitializationStrategy());
            result = checkExistsAndAccess(result, includeUnpublished, response);

            return result;
        } catch (EntityNotFoundException e){
            HttpStatusMessage.UUID_NOT_FOUND.send(response, e.getMessage());
        } catch (UnpublishedException e) {
            HttpStatusMessage.ACCESS_DENIED.send(response, e.getMessage());
        }
        return null;

    }

    @RequestMapping(value = "classifications", method = RequestMethod.GET)
    public List<Classification> doGetClassifications(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        boolean includeUnpublished = NO_UNPUBLISHED;

        logger.info("doGetClassifications(): " + request.getRequestURI());
        TaxonBase<?> taxonBase = service.load(uuid);
        taxonBase = checkExistsAndAccess(taxonBase, includeUnpublished, response);

        return service.listClassifications(taxonBase, null, null, getInitializationStrategy());
    }

    @RequestMapping(value = "taxonNodes", method = RequestMethod.GET)
    public Collection<TaxonNodeDto>  doGetTaxonNodes(
            @PathVariable("uuid") UUID taxonUuid,
            @RequestParam(value = "subtree", required = false) UUID subtreeUuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doGetTaxonNodes" + requestPathAndQuery(request));
        //this should be done by treeIndex as parameter, but as first implementation we get the node and then the treeinde
        String subTreeIndex = null;
        if (subtreeUuid != null){
        	TaxonNode subtree = getSubtreeOrError(subtreeUuid, nodeService, response);
        	subTreeIndex = subtree != null? subtree.treeIndex(): null;
        }
        try {
        	return nodeService.getTaxonNodeDtosFromTaxon(taxonUuid, subTreeIndex);
        }catch(Exception e) {
        		 HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
                 return null;
        }





    }

    protected  EntityInitStrategy getTaxonNodeInitStrategy() {
        return TAXONNODE_INIT_STRATEGY;
    }

   /**
    * See also {@link AgentController#doGetTaxonNodeAgentRelations(UUID, UUID, Integer, Integer, HttpServletRequest, HttpServletResponse)}
    */
    @RequestMapping(value = "taxonNodeAgentRelations/{classification_uuid}", method = RequestMethod.GET)
    public Pager<TaxonNodeAgentRelation>  doGetTaxonNodeAgentRelations(
            @PathVariable("uuid") UUID uuid,
            @PathVariable("classification_uuid") UUID classificationUuid,
            @RequestParam(value = "relType_uuid" , required = false) UUID relTypeUuid,
            @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        PagerParameters pagerParams = new PagerParameters(pageSize, pageIndex);
        pagerParams.normalizeAndValidate(response);

        Pager<TaxonNodeAgentRelation> pager = nodeService.pageTaxonNodeAgentRelations(uuid, classificationUuid,
                null, null, relTypeUuid, pagerParams.getPageSize(), pagerParams.getPageIndex(), null);
        return pager;
    }

    @RequestMapping(value = "specimensOrObservationsCount", method = RequestMethod.GET)
    public StringResultDTO doCountSpecimensOrObservations(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "taxOccRelFilter", required = false) String taxOccRelFilter,
            HttpServletRequest request,
            HttpServletResponse response) {

        logger.info("doListSpecimensOrObservations() - " + request.getRequestURI());
        boolean includeUnpublished = NO_UNPUBLISHED;
        EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes = bindAssociationFilter(taxOccRelFilter);

        List<OrderHint> orderHints = new ArrayList<>();
        orderHints.add(new OrderHint("titleCache", SortOrder.DESCENDING));

        FindOccurrencesConfigurator config = new FindOccurrencesConfigurator();
        config.setIncludeUnpublished(includeUnpublished);
        config.setAssociatedTaxonUuid(uuid);
        config.setTaxonOccurrenceRelTypes(taxonOccurrenceRelTypes);
        long countSpecimen = occurrenceService.countOccurrences(config);
        return new StringResultDTO(String.valueOf(countSpecimen));
    }

    @RequestMapping(value = "rootUnitDTOs", method = RequestMethod.GET)
    public List<SpecimenOrObservationBaseDTO> doListRooUnitDTOs(
            @PathVariable("uuid") UUID uuid,
            //TODO or should it be required
            @RequestParam(value = "taxOccRelFilter", required = false) String taxOccRelFilter,
            HttpServletRequest request,
            HttpServletResponse response) {

        // OccurrenceListController.doListlistRootUnitDTOsByAssociatedTaxon()
        logger.info("rootUnitDTOs() - " + request.getRequestURI());

        boolean includeUnpublished = NO_UNPUBLISHED;

        EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes = bindAssociationFilter(taxOccRelFilter);

        List<SpecimenOrObservationBaseDTO> rootUnitDtos = occurrenceService.listRootUnitDTOsByAssociatedTaxon(
                uuid, null, includeUnpublished,
                taxonOccurrenceRelTypes,
                OccurrenceController.DERIVED_UNIT_INIT_STRATEGY);
        return rootUnitDtos;
    }

    @RequestMapping(value = "specimensOrObservations", method = RequestMethod.GET)
    public List<SpecimenOrObservationBase<?>> doListSpecimensOrObservations(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doListSpecimensOrObservations() - " + request.getRequestURI());

        boolean includeUnpublished = NO_UNPUBLISHED;
        EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes = TaxonOccurrenceRelationType.All();

        TaxonBase<?> tb = service.load(uuid);
        List<OrderHint> orderHints = new ArrayList<>();
        orderHints.add(new OrderHint("titleCache", SortOrder.DESCENDING));
        if(tb instanceof Taxon){
            List<SpecimenOrObservationBase<?>> specimensOrObservations = occurrenceService.listByAssociatedTaxon(
                    null, null, (Taxon)tb, includeUnpublished,
                    taxonOccurrenceRelTypes,
                    null, null, null, orderHints, null);
            return specimensOrObservations;
        } else {
            HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
            return null;
        }
    }

    @RequestMapping(value = "associatedRootUnits", method = RequestMethod.GET)
    public Pager<SpecimenOrObservationBase> doGetAssociatedRootUnits(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "maxDepth", required = false) Integer maxDepth,
            @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doGetAssociatedRootUnits() - " + request.getRequestURI());

        boolean includeUnpublished = NO_UNPUBLISHED;
        EnumSet<TaxonOccurrenceRelationType> taxonOccurrenceRelTypes = TaxonOccurrenceRelationType.All();

        TaxonBase<?> taxonBase = service.load(uuid);
        taxonBase = checkExistsAndAccess(taxonBase, includeUnpublished, response);

        List<OrderHint> orderHints = new ArrayList<>();
        orderHints.add(new OrderHint("titleCache", SortOrder.ASCENDING));

        if(taxonBase instanceof Taxon){
            PagerParameters pagerParams = new PagerParameters(pageSize, pageIndex);
            pagerParams.normalizeAndValidate(response);

            return occurrenceService.pageRootUnitsByAssociatedTaxon(null, null, (Taxon) taxonBase,
                    includeUnpublished,
                    taxonOccurrenceRelTypes,
                    maxDepth, pagerParams.getPageSize(), pagerParams.getPageIndex(), orderHints, null);
        }else{
            // FIXME proper HTTP code response
            return null;
        }
    }

    @RequestMapping(value = "taggedName", method = RequestMethod.GET)
    public ModelAndView doGetTaggedName(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request) {
        logger.info("doGetDescriptionElementsByType() - " + request.getRequestURI());

        ModelAndView mv = new ModelAndView();

        TaxonBase<?> tb = service.load(uuid, NO_UNPUBLISHED, Arrays.asList(new String[] {"name"}));
        mv.addObject(nameService.getTaggedName(tb.getName().getUuid()));
        return mv;
    }

    /**
     * This webservice endpoint returns all taxa which are congruent or included in the taxon represented by the given taxon uuid.
     * The result also returns the path to these taxa represented by the uuids of the taxon relationships types and doubtful information.
     * If classificationUuids is set only taxa of classifications are returned which are included in the given classifications.
     * Also the path to these taxa may not include taxa from other classifications.
     */
    @RequestMapping(value = { "includedTaxa" }, method = { RequestMethod.GET })
    public IncludedTaxaDTO doGetIncludedTaxa(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value="classificationFilter", required=false) final List<String> classificationStringList,
            @RequestParam(value="includeDoubtful", required=false) final boolean includeDoubtful,
            @RequestParam(value="onlyCongruent", required=false) final boolean onlyCongruent,
            HttpServletResponse response,
            HttpServletRequest request) {


        if(request != null){
            logger.info("doGetIncludedTaxa()" + requestPathAndQuery(request));
        }

        List<UUID> classificationFilter = null;
        if( classificationStringList != null ){
            classificationFilter = new ArrayList<>();
            for(String classString :classificationStringList){
                classificationFilter.add(UUID.fromString(classString));
            }
        }
        IncludedTaxonConfiguration configuration =
                new IncludedTaxonConfiguration(classificationFilter, includeDoubtful, onlyCongruent);
        IncludedTaxaDTO listIncludedTaxa = service.listIncludedTaxa(uuid, configuration);
        return listIncludedTaxa;
    }

    // TODO ================================================================================ //
    // move all description and descriptionElement related methods into the according
    // Description Controllers

    /**
     * Get the list of {@link TaxonDescription}s of the
     * {@link Taxon} instance identified by the <code>{taxon-uuid}</code>.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;descriptions</b>
     *
     * @param request
     * @param response
     * @return a List of {@link TaxonDescription} entities which are initialized
     *         using the following initialization strategy:
     *         {@link #TAXONDESCRIPTION_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(
            value = {"descriptions"},
            method = RequestMethod.GET)
    public Pager<TaxonDescription> doGetDescriptions(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "markerTypes", required = false) List<MarkerType> markerTypes,
            @RequestParam(value = "descriptionTypes", required = false) List<DescriptionType> descriptionTypes,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException {

        if(request != null){
            logger.info("doGetDescriptions()" + requestPathAndQuery(request));
        }

        Taxon taxon = getCdmBaseInstance(Taxon.class, uuid, response, (List<String>)null);
        taxon = checkExistsAndAccess(taxon, NO_UNPUBLISHED, response);

        Set<MarkerType> markerTypesSet = new HashSet<>();
        if (markerTypes != null) {
            markerTypesSet.addAll(markerTypes);
        }
        Set<DescriptionType> descriptionTypesSet = new HashSet<>();
        if (descriptionTypes != null) {
            descriptionTypesSet.addAll(descriptionTypes);
        }

        List<String> taxonDescriptionInitStrategy = getTaxonDescriptionInitStrategy();
        Pager<TaxonDescription> p = descriptionService.pageTaxonDescriptions(taxon, null, null, markerTypesSet, descriptionTypesSet, null, null, taxonDescriptionInitStrategy);

        return p;
    }

    @RequestMapping(value = "descriptions/elementsByType/{classSimpleName}", method = RequestMethod.GET)
    public ModelAndView doGetDescriptionElementsByType(
            @PathVariable("uuid") UUID uuid,
            @PathVariable("classSimpleName") String classSimpleName,
            @RequestParam(value = "markerTypes", required = false) List<MarkerType> markerTypes,
            @RequestParam(value = "descriptionTypes", required = false) List<DescriptionType> descriptionTypes,
            @RequestParam(value = "count", required = false, defaultValue = "false") Boolean doCount,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doGetDescriptionElementsByType() - " + requestPathAndQuery(request));

        boolean includeUnpublished = NO_UNPUBLISHED;

        ModelAndView mv = new ModelAndView();

        List<DescriptionElementBase> allElements = new ArrayList<>();
        List<DescriptionElementBase> elements;
        int count = 0;

        List<String> initStrategy = doCount ? null : getTaxonDescriptionElementInitStrategy();

        Taxon taxon = getCdmBaseInstance(Taxon.class, uuid, response, (List<String>)null);

        taxon = checkExistsAndAccess(taxon, includeUnpublished, response);


        Set<MarkerType> markerTypesSet = new HashSet<>();
        if (markerTypes != null) {
            markerTypesSet.addAll(markerTypes);
        }
        Set<DescriptionType> descriptionTypesSet = new HashSet<>();
        if (descriptionTypes != null) {
            descriptionTypesSet.addAll(descriptionTypes);
        }

        List<TaxonDescription> taxonDescriptions = descriptionService.listTaxonDescriptions(
                taxon, null, null, markerTypesSet, descriptionTypesSet, null, null, null);
        try {
            Class type;
            type = Class.forName("eu.etaxonomy.cdm.model.description."
                    + classSimpleName);
            if (taxonDescriptions != null) {
                for (TaxonDescription description : taxonDescriptions) {
                    Class<? extends DescriptionBase<?>> descriptionType = null;
                    elements = descriptionService.listDescriptionElements(
                            description, descriptionType, null, type, includeUnpublished, null, 0, initStrategy);
                    allElements.addAll(elements);
                    count += elements.size();
                }

            }
        } catch (ClassNotFoundException e) {
            HttpStatusMessage.create(e.getLocalizedMessage(), 400).send(response);
        }
        if(doCount){
            mv.addObject(count);
        } else {
            mv.addObject(allElements);
        }
        return mv;
    }

    @RequestMapping(value = "taxonRelationshipsDTO", method = RequestMethod.GET)
    public TaxonRelationshipsDTO doGetTaxonRelationshipsDTO(
            @PathVariable("uuid") UUID taxonUuid,
            @RequestParam(value = "directTypes", required = false) UuidList directTypeUuids,
            @RequestParam(value = "inversTypes", required = false) UuidList inversTypeUuids,
            @RequestParam(value = "direction", required = false) Direction direction,
            @RequestParam(value="groupMisapplications", required=false, defaultValue="false") final boolean groupMisapplications,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        boolean includeUnpublished = NO_UNPUBLISHED;

        logger.info("doGetTaxonRelationshipDTOs(): " + request.getRequestURI());
        TaxonBase<?> taxonBase = service.load(taxonUuid);
        checkExistsAccessType(taxonBase, includeUnpublished, Taxon.class, response);

        Set<TaxonRelationshipType> directTypes = getTermsByUuidSet(TaxonRelationshipType.class, directTypeUuids);
        Set<TaxonRelationshipType> inversTypes = getTermsByUuidSet(TaxonRelationshipType.class, inversTypeUuids);

//        Set<TaxonRelationshipType> inversTypes = null;
//        if (directTypeUuids != null && !directTypeUuids.isEmpty()){
//            types = new HashSet<>();
//            List<TaxonRelationshipType> typeList = termService.find(TaxonRelationshipType.class, new HashSet<>(directTypeUuids));
//            types.addAll(typeList);
//            //TODO should we handle missing uuids as error response
////            HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
//        }



//        boolean deduplicateMisapplications = true;
        Integer pageSize = null;
        Integer pageNumber = null;
        return service.listTaxonRelationships(taxonUuid, directTypes, inversTypes, direction, groupMisapplications,
                includeUnpublished, pageSize, pageNumber);
    }

    /**
     * @param directTypeUuids
     * @return
     */
    protected <T extends DefinedTermBase<T>> Set<T> getTermsByUuidSet(Class<T> clazz, UuidList directTypeUuids) {
        Set<T> directTypes = null;

        if (directTypeUuids != null && !directTypeUuids.isEmpty()){
            directTypes = new HashSet<>();
            List<T> typeList = termService.find(clazz, new HashSet<>(directTypeUuids));
            directTypes.addAll(typeList);
            //TODO should we handle missing uuids as error response
//            HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
        }
        return directTypes;
    }

    // TODO ================================================================================ //

}
