// $Id$
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.wordnik.swagger.annotations.Api;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.config.IncludedTaxonConfiguration;
import eu.etaxonomy.cdm.api.service.dto.IncludedTaxaDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;

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
public class TaxonController extends BaseController<TaxonBase, ITaxonService>
{
    public static final Logger logger = Logger.getLogger(TaxonController.class);

    @Autowired
    private IOccurrenceService occurrenceService;
    @Autowired
    private INameService nameService;
    @Autowired
    private ITaxonNodeService nodeService;

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
    /**
     * Get the set of accepted {@link Taxon} entities for a given
     * {@link TaxonBase} entity identified by the <code>{taxon-uuid}</code>.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;accepted</b>
     *
     * @param request
     * @param response
     * @return a Set of {@link Taxon} entities which are initialized
     *         using the following initialization strategy:
     *         {@link #SYNONYMY_INIT_STRATEGY}
     * @throws IOException
     * @Deprecated use getAcceptedFor() instead
     */
    @Deprecated
    @RequestMapping(value = "accepted/{classification_uuid}", method = RequestMethod.GET)
    public List<Taxon> getAcceptedForWithClassificationFilter(
                @PathVariable("uuid") UUID uuid,
                @PathVariable("classification_uuid") UUID classification_uuid,
                @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                @RequestParam(value = "pageSize", required = false) Integer pageSize,
                HttpServletRequest request,
                HttpServletResponse response)
                throws IOException {


        return getAcceptedFor(uuid, classification_uuid, pageNumber, pageSize, request, response);
    }

    @RequestMapping(value = "accepted", method = RequestMethod.GET)
    public List<Taxon> getAcceptedFor(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "classificationFilter", required = false) UUID classification_uuid,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        if(request != null){
            logger.info("getAccepted() " + requestPathAndQuery(request));
        }

        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
        pagerParams.normalizeAndValidate(response);

        List<Taxon> resultset = new ArrayList<Taxon>();
        try {
            resultset = service.listAcceptedTaxaFor(uuid, classification_uuid, pagerParams.getPageSize(), pagerParams.getPageIndex(), null, getInitializationStrategy());
        } catch (EntityNotFoundException e){
            HttpStatusMessage.UUID_NOT_FOUND.send(response);
        }

        return resultset;
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

}