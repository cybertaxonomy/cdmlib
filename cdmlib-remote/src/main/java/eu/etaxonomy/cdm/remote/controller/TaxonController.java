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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.config.IncludedTaxonConfiguration;
import eu.etaxonomy.cdm.api.service.dto.IncludedTaxaDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.editor.TermBasePropertyEditor;
import io.swagger.annotations.Api;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @date 20.07.2009
 *
 */
@Controller
@Api("taxon")
@RequestMapping(value = {"/taxon/{uuid}"})
public class TaxonController extends AbstractIdentifiableController<TaxonBase, ITaxonService>
{
    public static final Logger logger = Logger.getLogger(TaxonController.class);

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

    protected static final List<String> TAXONNODE_INIT_STRATEGY = Arrays.asList(new String []{
            "taxonNodes"
    });

    public TaxonController(){
        super();
        setInitializationStrategy(Arrays.asList(new String[]{
                "$",
                "name.nomenclaturalReference"
                }
        ));
    }

    @Override
    @Autowired
    public void setService(ITaxonService service) {
        this.service = service;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(MarkerType.class, new TermBasePropertyEditor<MarkerType>(termService));
    }

    protected List<String> getTaxonDescriptionInitStrategy() {
        return getInitializationStrategy();
    }

    protected List<String> getTaxonDescriptionElementInitStrategy() {
        return getInitializationStrategy();
    }

    /**
     * Get the set of accepted {@link Taxon} entities for a given
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
    public Taxon getAcceptedFor(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "classificationFilter", required = false) UUID classification_uuid,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        if(request != null){
            logger.info("getAcceptedFor() " + requestPathAndQuery(request));
        }

        Taxon result = null;
        try {
            result = service.findAcceptedTaxonFor(uuid, classification_uuid, getInitializationStrategy());
        } catch (EntityNotFoundException e){
            HttpStatusMessage.UUID_NOT_FOUND.send(response);
        }

        return result;
    }

    @RequestMapping(value = "classifications", method = RequestMethod.GET)
    public List<Classification> doGetClassifications(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doGetClassifications(): " + request.getRequestURI());
        TaxonBase<?> taxonBase = service.load(uuid);

        if (taxonBase == null){
            HttpStatusMessage.UUID_NOT_FOUND.send(response);
        }

        return service.listClassifications(taxonBase, null, null, getInitializationStrategy());
    }

    @RequestMapping(value = "taxonNodes", method = RequestMethod.GET)
    public Set<TaxonNode>  doGetTaxonNodes(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        TaxonBase<?> tb = service.load(uuid, TAXONNODE_INIT_STRATEGY);
        if(tb instanceof Taxon){
            return ((Taxon)tb).getTaxonNodes();
        } else {
            HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
            return null;
        }
    }

    /**
    *
    * See also {@link AgentController#doGetTaxonNodeAgentRelations(UUID, UUID, Integer, Integer, HttpServletRequest, HttpServletResponse)}
    *
    * @param uuid
    * @param classificationUuid
    * @param pageNumber
    * @param pageSize
    * @param request
    * @param response
    * @return
    * @throws IOException
    *
    */
    @RequestMapping(value = "taxonNodeAgentRelations/{classification_uuid}", method = RequestMethod.GET)
    public Pager<TaxonNodeAgentRelation>  doGetTaxonNodeAgentRelations(
            @PathVariable("uuid") UUID uuid,
            @PathVariable("classification_uuid") UUID classificationUuid,
            @RequestParam(value = "relType_uuid" , required = false) UUID relTypeUuid,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
        pagerParams.normalizeAndValidate(response);

        Pager<TaxonNodeAgentRelation> pager = nodeService.pageTaxonNodeAgentRelations(uuid, classificationUuid,
                null, null, relTypeUuid, pagerParams.getPageSize(), pagerParams.getPageIndex(), null);
        return pager;
    }


    @RequestMapping(value = "specimensOrObservations", method = RequestMethod.GET)
    public ModelAndView doListSpecimensOrObservations(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doListSpecimensOrObservations() - " + request.getRequestURI());

        ModelAndView mv = new ModelAndView();

        TaxonBase<?> tb = service.load(uuid);

        List<OrderHint> orderHints = new ArrayList<OrderHint>();
        orderHints.add(new OrderHint("titleCache", SortOrder.DESCENDING));

        if(tb instanceof Taxon){
            List<SpecimenOrObservationBase<?>> specimensOrObservations = occurrenceService.listByAssociatedTaxon(null, null, (Taxon)tb, null, null, null, orderHints, null);
            mv.addObject(specimensOrObservations);
        } else {
            HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
            return null;
        }

        return mv;
    }

    @RequestMapping(value = "associatedFieldUnits", method = RequestMethod.GET)
    public Pager<SpecimenOrObservationBase> doGetFieldUnits(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "maxDepth", required = false) Integer maxDepth,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doGetFieldUnits() - " + request.getRequestURI());

        ModelAndView mv = new ModelAndView();

        TaxonBase<?> tb = service.load(uuid);

        List<OrderHint> orderHints = new ArrayList<OrderHint>();
        orderHints.add(new OrderHint("titleCache", SortOrder.ASCENDING));

        if(tb instanceof Taxon){
            PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
            pagerParams.normalizeAndValidate(response);

            return occurrenceService.pageFieldUnitsByAssociatedTaxon(null, (Taxon) tb, null, pagerParams.getPageSize(), pagerParams.getPageIndex(), orderHints, null);
        }
        return null;
    }

    @RequestMapping(value = "taggedName", method = RequestMethod.GET)
    public ModelAndView doGetTaggedName(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request) {
        logger.info("doGetDescriptionElementsByType() - " + request.getRequestURI());

        ModelAndView mv = new ModelAndView();

        TaxonBase<?> tb = service.load(uuid, Arrays.asList(new String[] {"name"}));
        mv.addObject(nameService.getTaggedName(tb.getName().getUuid()));
        return mv;
    }

    /**
     * This webservice endpoint returns all taxa which are congruent or included in the taxon represented by the given taxon uuid.
     * The result also returns the path to these taxa represented by the uuids of the taxon relationships types and doubtful information.
     * If classificationUuids is set only taxa of classifications are returned which are included in the given classifications.
     * Also the path to these taxa may not include taxa from other classifications.
     *
     * @param taxonUUIDString
     * @param classificationStringList
     * @param includeDoubtful
     * @param onlyCongruent
     * @param response
     * @param request
     * @return
     * @throws IOException
     */

    @RequestMapping(value = { "includedTaxa" }, method = { RequestMethod.GET })
    public ModelAndView doGetIncludedTaxa(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value="classificationFilter", required=false) final List<String> classificationStringList,
            @RequestParam(value="includeDoubtful", required=false) final boolean includeDoubtful,
            @RequestParam(value="onlyCongruent", required=false) final boolean onlyCongruent,
            HttpServletResponse response,
            HttpServletRequest request) throws IOException {
            ModelAndView mv = new ModelAndView();
            /**
             * List<UUID> classificationFilter,
             * boolean includeDoubtful,
             * boolean onlyCongruent)
             */
            List<UUID> classificationFilter = null;
            if( classificationStringList != null ){
                classificationFilter = new ArrayList<UUID>();
                for(String classString :classificationStringList){
                    classificationFilter.add(UUID.fromString(classString));
                }
            }
            final IncludedTaxonConfiguration configuration = new IncludedTaxonConfiguration(classificationFilter, includeDoubtful, onlyCongruent);
            IncludedTaxaDTO listIncludedTaxa = service.listIncludedTaxa(uuid, configuration);
            mv.addObject(listIncludedTaxa);
            return mv;
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
            HttpServletRequest request,
            HttpServletResponse response)throws IOException {

        if(request != null){
            logger.info("doGetDescriptions()" + requestPathAndQuery(request));
        }

        Taxon t = getCdmBaseInstance(Taxon.class, uuid, response, (List<String>)null);
        Set<MarkerType> markerTypesSet = new HashSet<MarkerType>();
        if (markerTypes != null) {
            markerTypesSet.addAll(markerTypes);
        }

        Pager<TaxonDescription> p = descriptionService.pageTaxonDescriptions(t, null, null, markerTypesSet, null, null, getTaxonDescriptionInitStrategy());

        return p;
    }

    @RequestMapping(value = "descriptions/elementsByType/{classSimpleName}", method = RequestMethod.GET)
    public ModelAndView doGetDescriptionElementsByType(
            @PathVariable("uuid") UUID uuid,
            @PathVariable("classSimpleName") String classSimpleName,
            @RequestParam(value = "markerTypes", required = false) List<MarkerType> markerTypes,
            @RequestParam(value = "count", required = false, defaultValue = "false") Boolean doCount,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doGetDescriptionElementsByType() - " + requestPathAndQuery(request));

        ModelAndView mv = new ModelAndView();

        List<DescriptionElementBase> allElements = new ArrayList<DescriptionElementBase>();
        List<DescriptionElementBase> elements;
        int count = 0;

        List<String> initStrategy = doCount ? null : getTaxonDescriptionElementInitStrategy();

        Taxon t = getCdmBaseInstance(Taxon.class, uuid, response, (List<String>)null);

        Set<MarkerType> markerTypesSet = new HashSet<MarkerType>();
        if (markerTypes == null) {
            markerTypesSet.addAll(markerTypes);
        }

        List<TaxonDescription> taxonDescriptions = descriptionService.listTaxonDescriptions(t, null, null, markerTypesSet, null, null, null);
        try {
            Class type;
            type = Class.forName("eu.etaxonomy.cdm.model.description."
                    + classSimpleName);
            if (taxonDescriptions != null) {
                for (TaxonDescription description : taxonDescriptions) {
                    elements = descriptionService.listDescriptionElements(description, null, type, null, 0, initStrategy);
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

    // TODO ================================================================================ //

}
