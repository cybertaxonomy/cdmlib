/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.controller.dto;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.formula.functions.T;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeConfigurator;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeNotSupportedException;
import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.common.DocUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.remote.controller.IdentifiableListController;
import eu.etaxonomy.cdm.remote.controller.util.ControllerUtils;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.dto.common.ErrorResponse;
import eu.etaxonomy.cdm.remote.dto.occurrencecatalogue.OccurrenceSearch;
import eu.etaxonomy.cdm.remote.dto.occurrencecatalogue.OccurrenceSearch.OccurrenceSearchResponse;
import eu.etaxonomy.cdm.remote.view.HtmlView;

/**
 * The controller class for the namespace 'occurrence_catalogue'. This web service namespace
 * is an add-on to the already existing CDM REST API and provides information relating
 * to scientific names as well as taxa present in the underlying datasource.
 *
 * @author p.kelbert
 * @version 1.1.0
 * @created March-2014
 */

@Controller
@RequestMapping(value = { "/occurrence_catalogue" })
public class OccurrenceCatalogueController extends IdentifiableListController<SpecimenOrObservationBase, IOccurrenceService> implements ResourceLoaderAware{

    private ResourceLoader resourceLoader;

    /** Base scientific name search type */
    public static final String NAME_SEARCH = "occurrence";

    /** Complete scientific name search type */
    public static final String TITLE_SEARCH = "title";

    /** Default name search type */
    public static final String DEFAULT_SEARCH_TYPE = NAME_SEARCH;

    public static final String DEFAULT_PAGE_NUMBER = "0";

    public static final String DEFAULT_PAGE_SIZE = "50";


    /** Default max number of hits for the exact name search */
    public static final String DEFAULT_MAX_NB_FOR_EXACT_SEARCH = "100";

    /** Classifcation 'default' key */
    public static final String CLASSIFICATION_DEFAULT = "default";

    /** Classifcation 'all' key */
    public static final String CLASSIFICATION_ALL = "all";


    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private IOccurrenceService occurrenceService;

    @Autowired
    private IDescriptionService descriptionService;

    @Autowired
    private IClassificationService classificationService;

    @Autowired
    private ICommonService commonService;

    @Autowired
    private ITermService termService;


    private static final List<String> OCCURRENCE_INIT_STRATEGY = Arrays.asList(new String []{    		
    		"sources"
    });

    private static final List<String> FACADE_INIT_STRATEGY = Arrays.asList(new String []{
    		"collector",
    		"collection.institute.name",
    		"fieldNotes",
    		"type",
    		"individualCount",
    		"kindOfUnit.label",
    		"absoluteElevation",
    		"absoluteElevationMaximum",
    		"distanceToGround",
    		"distanceToGroundMax",
    		"gatheringPeriod.start",
    		"gatheringPeriod.end",
    		"exactLocation.latitude",
    		"exactLocation.longitude",
    		"exactLocation.errorRadius",
    		"exactLocation.referenceSystem",
    		"country",
    		"locality"
    });



    public OccurrenceCatalogueController() {
        super();
        setInitializationStrategy(Arrays.asList(new String[] { "$" }));
    }

    /**
     * Returns a documentation page for the Occurrence Search API.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;occurrence_catalogue</b>
     *
     * @param request
     * @param response
     * @return Html page describing the Name Search API
     * @throws IOException
     */
    @RequestMapping(value = { "" }, method = RequestMethod.GET, params = {})
    public ModelAndView doGetNameSearchDocumentation(
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ModelAndView mv = new ModelAndView();
        // Read apt documentation file.
        Resource resource = resourceLoader.getResource("classpath:eu/etaxonomy/cdm/doc/remote/apt/occurrence-catalogue-default.apt");
        // using input stream as this works for both files in the classes directory
        // as well as files inside jars
        InputStream aptInputStream = resource.getInputStream();
        // Build Html View
        Map<String, String> modelMap = new HashMap<String, String>();
        // Convert Apt to Html
        modelMap.put("html", DocUtils.convertAptToHtml(aptInputStream));
        mv.addAllObjects(modelMap);

        HtmlView hv = new HtmlView();
        mv.setView(hv);
        return mv;
    }

    /**
     * Returns a list of occurrences matching the <code>{query}</code>
     * Taxon UUID.
     * <p>
     * Endpoint documentation can be found <a href="{@docRoot}/../remote/occurrence-catalogue-default.html">here</a>
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;occurrence_catalogue</b>
     *
     * @param query
     *  The UUID of the taxon to query for. The query can
     *  not contain wildcard characters.
     *
     * @param request Http servlet request.
     * @param response Http servlet response.
     * @return a List of {@link OccurrenceSearch} objects each corresponding to a
     * single query. These are built from {@link SpecimenOrObservationBase} entities
     * which are in turn initialized using the {@link #OCCURRENCE_INIT_STRATEGY} and {@link #FACADE_INIT_STRATEGY}
     * Redirect the query with the default page size and the default page number.
     * @throws IOException
     */
    @RequestMapping(value = { "" }, method = RequestMethod.GET, params = {"taxonUuid"})
    public ModelAndView doGetOccurrenceSearch(@RequestParam(value = "taxonUuid", required = true) String query,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        return doGetOccurrenceSearch(query, DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE, request, response);
    }




    /**
     * Returns a list of occurrences matching the <code>{query}</code>
     * Taxon UUID.
     * <p>
     * Endpoint documentation can be found <a href="{@docRoot}/../remote/occurrence-catalogue-default.html">here</a>
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;occurrence_catalogue</b>
     *
     * @param query
     * 	The UUID of the taxon to query for. The query can
     * 	not contain wildcard characters.
     *
     * @param pageNumber
     * 	The number of the page to be returned.
     *  * @param pageSize
     *  The number of responses per page to be returned.
     * @param request Http servlet request.
     * @param response Http servlet response.
     * @return a List of {@link OccurrenceSearch} objects each corresponding to a
     * single query. These are built from {@link SpecimenOrObservationBase} entities
     * which are in turn initialized using the {@link #OCCURRENCE_INIT_STRATEGY} and {@link #FACADE_INIT_STRATEGY}
     * @throws IOException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequestMapping(value = { "" }, method = RequestMethod.GET, params = {"taxonUuid","pageNumber","pageSize"})
    public ModelAndView doGetOccurrenceSearch(
            @RequestParam(value = "taxonUuid", required = true) String taxonUuid,
            @RequestParam(value = "pageNumber", required = false, defaultValue = DEFAULT_PAGE_NUMBER) String pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = DEFAULT_PAGE_SIZE) String pageSize,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("doGetOccurrenceSearch, " + "taxonUuid : " + taxonUuid + " - pageSize : " + pageSize + " - " + "pageNumber : " + pageNumber);
        ModelAndView mv = new ModelAndView();
        List nsList = new ArrayList<T>();

        Integer pS = null;
        Integer pN = null;

        try {
            pN=Integer.valueOf(pageNumber);
        } catch (Exception e) {
            pN=Integer.valueOf(DEFAULT_PAGE_NUMBER);
            logger.info("pagenumber is not a number");
        }
        try {
            pS=Integer.valueOf(pageSize);
        } catch (Exception e) {
            pS=Integer.valueOf(DEFAULT_PAGE_SIZE);
            logger.info("pagesize is not a number");
        }

        PagerParameters pagerParams = new PagerParameters(pS, pN);
        pagerParams.normalizeAndValidate(response);

        //FIXME : Does not seem to be used ?
//        List<Feature> features = new ArrayList<Feature>();
//        features.add(Feature.OBSERVATION());
//        features.add(Feature.OCCURRENCE());
//        features.add(Feature.INDIVIDUALS_ASSOCIATION());
//        features.add(Feature.MATERIALS_EXAMINED());
//        features.add(Feature.SPECIMEN());

        List<DerivedUnit> records = new ArrayList<DerivedUnit>();

        OccurrenceSearch ns = new OccurrenceSearch();
        ns.setRequest(taxonUuid);

        Pager<DerivedUnit> specimenOrObs = null;
        int total=0;
        OccurrenceSearch os = new OccurrenceSearch();
        // search through each query
        if(taxonUuid.equals("") || !isValid(taxonUuid)) {
            ErrorResponse er = new ErrorResponse();
            er.setErrorMessage("Empty taxon uuid field");
            nsList.add(er);
        } else {       

            Taxon taxon = (Taxon) taxonService.find(UUID.fromString(taxonUuid));
            pagerParams.normalizeAndValidate(response);
            List<OrderHint> orderHints = null;
            
            
            // TODO load the full strategy once the method gets debuged
//            Set<TaxonRelationshipEdge> includeRelationships = ControllerUtils.loadIncludeRelationships(null, null, termService);
//            specimenOrObs= service.pageByAssociatedTaxon(null, includeRelationships, associatedTaxon,
//                    null, pagerParams.getPageSize(), pagerParams.getPageIndex(),
//                    orderHints, getInitializationStrategy());
            
          // Only derived units are requested in the pager method since we expect
          // occurrences to be of type DerivedUnit
          // The only possibility for an occurrence to be of type FieldUnit is the 
          // describedSpecimenOrObservation in the DescriptionBase class, which ideally
          // should not be used for storing occurrences
          specimenOrObs= service.pageByAssociatedTaxon(DerivedUnit.class, 
        		  null, 
        		  taxon,
        		  null, 
        		  pagerParams.getPageSize(), 
        		  pagerParams.getPageIndex(),
        		  orderHints, 
        		  OCCURRENCE_INIT_STRATEGY);            
            
          //FIXME We already have the list of occurrences in specimenOrObs pager
//            total = (service.listByAssociatedTaxon(null, includeRelationships, associatedTaxon,
//                    null, null, null, orderHints, null)).size();

            records = specimenOrObs.getRecords();

            DerivedUnit derivedUnit=null;
            List<DerivedUnitFacade> facades = new ArrayList<DerivedUnitFacade>();
            for (SpecimenOrObservationBase<?> specimen:records){
                
                               
                //FIXME SpecimenOrObservationBase is not an instanceof GatheringEvent ?
//                if (specimen instanceof GatheringEvent){
//                    GatheringEvent gath = CdmBase.deproxy(specimen, GatheringEvent.class);
//                    nsList.add(ns.createOccurrence( query, specimen, gath,  associatedTaxon.getTitleCache() ));
//                }
                
                //if (specimen.isInstanceOf(DerivedUnit.class)){
                    derivedUnit = CdmBase.deproxy(specimen, DerivedUnit.class);
                    DerivedUnitFacade derivedUnitFacade =null;
                    try {
                    	//TODO : This is a potential performance hurdle (especially for large number
                    	//       of derived units). 
                    	//       A possible solution is to already initialise the required derived unit facade 
                    	//       in the 'pageByAssociatedTaxon' call. This can be done either via the initialisation
                    	//       strategy technique or by writing a new optimised hql query for this.
                    	//       This will ensure that the DerivedUnits are loaded with required fields already
                    	//       initialised and will not require the loading and initialisation of each DerivedUnit
                    	//       object as is done in the 'getDerivedUnitFacade' method.
                        derivedUnitFacade = occurrenceService.getDerivedUnitFacade(derivedUnit, FACADE_INIT_STRATEGY);                                            
                        os.addToResponse(taxon.getTitleCache(), taxonUuid, derivedUnitFacade);
                    } catch (DerivedUnitFacadeNotSupportedException e) {
                       derivedUnitFacade=null;
                    }
//                    if (derivedUnitFacade !=null) {
//                        facades.add(derivedUnitFacade);
//                    }

                    //FIXME:Do we need this, isnt this already covered in pageByAssociatedTaxon
//                    Set<DescriptionBase> descriptions = specimen.getDescriptions();
//                    for (DescriptionBase<?> dbase:descriptions) {
//                        facades.addAll(occurrenceService.listDerivedUnitFacades(dbase, FACADE_INIT_STRATEGY));
//                    }
                }
            //}
            
            // FIXME : Isn't it easier to just add each occurrence to the list when we 
            //         loop over derived unit facade objects (above)
//                for (DerivedUnitFacade facade:facades) {
//                    nsList.add(ns.createOccurrence(taxonUuid, 
//                    		derivedUnit, 
//                    		facade, 
//                    		specimen,
//                    		associatedTaxon.getTitleCache()));
//                }
            
        }


        if (os.getResponse().isEmpty()){
            mv.addObject(nsList);
        } else {
            DefaultPagerImpl<OccurrenceSearchResponse> dpi = 
            		new DefaultPagerImpl<OccurrenceSearchResponse>(specimenOrObs.getCurrentIndex(), 
            					specimenOrObs.getCount(), 
            					specimenOrObs.getPageSize(), 
            					os.getResponse());
            mv.addObject(dpi);
        }

        return mv;
    }


    private boolean isValid(String uuid){
        if( uuid == null) {
            return false;
        }
        try {
            // we have to convert to object and back to string because the built in fromString does not have
            // good validation logic.

            UUID fromStringUUID = UUID.fromString(uuid);
            String toStringUUID = fromStringUUID.toString();

            System.out.println("input uuid : " + uuid + " , parsed uuid : " + toStringUUID);
            return toStringUUID.equals(uuid);
        } catch(IllegalArgumentException e) {
            return false;
        }
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.BaseListController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Override
    @Autowired
    public void setService(IOccurrenceService service) {
        this.service = service;
    }

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		 this.resourceLoader = resourceLoader;
		
	}
}
