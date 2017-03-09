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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.search.DocumentSearchResult;
import eu.etaxonomy.cdm.common.DocUtils;
import eu.etaxonomy.cdm.hibernate.search.AcceptedTaxonBridge;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.remote.controller.AbstractController;
import eu.etaxonomy.cdm.remote.dto.common.ErrorResponse;
import eu.etaxonomy.cdm.remote.dto.common.RemoteResponse;
import eu.etaxonomy.cdm.remote.dto.namecatalogue.AcceptedNameSearch;
import eu.etaxonomy.cdm.remote.dto.namecatalogue.NameInformation;
import eu.etaxonomy.cdm.remote.dto.namecatalogue.NameSearch;
import eu.etaxonomy.cdm.remote.dto.namecatalogue.TaxonInformation;
import eu.etaxonomy.cdm.remote.view.HtmlView;
import io.swagger.annotations.Api;

/**
 * The controller class for the namespace 'name_catalogue'. This web service namespace
 * is an add-on to the already existing CDM REST API and provides information relating
 * to scientific names as well as taxa present in the underlying datasource.
 *
 * @author c.mathew
 * @version 1.1.0
 * @created 15-Apr-2012
 */

@Controller
@Api("name_catalogue")
@RequestMapping(value = { "/name_catalogue" })
public class NameCatalogueController extends AbstractController<TaxonNameBase, INameService> implements ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    /** Taxonomic status 'accepted' string */
    public static final String ACCEPTED_NAME_STATUS = "accepted";

    /** Taxonpmic status 'synonym' string */
    public static final String SYNONYM_STATUS = "synonym";

    /** Flag 'doubtful' strings */
    public static final String DOUBTFUL_FLAG = "doubtful";

    /** Base scientific name search type */
    public static final String NAME_SEARCH = "name";

    /** Complete scientific name search type */
    public static final String TITLE_SEARCH = "title";

    /** Default name search type */
    public static final String DEFAULT_SEARCH_TYPE = NAME_SEARCH;

    /** Default max number of hits for the exact name search  */
    public static final String DEFAULT_MAX_NB_FOR_EXACT_SEARCH = "100";

    /** Classifcation 'default' key */
    public static final String CLASSIFICATION_DEFAULT = "default";

    /** Classifcation 'all' key */
    public static final String CLASSIFICATION_ALL = "all";

    /** Classification to include uuids key */
    public static final String INCLUDE_CLUUIDS = "cluuids";

    /** Fuzzy Name Cache search */
    public static final String FUZZY_NAME_CACHE = "name";

    /** Fuzzy Atomised Name search */
    public static final String FUZZY_ATOMISED = "atomised";

    private static final String DWC_DATASET_ID = "http://rs.tdwg.org/dwc/terms/datasetID";

    private static final DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MM-yyyy");

    @Autowired
    private ITaxonService taxonService;


    @Autowired
    private IClassificationService classificationService;

    @Autowired
    private ICommonService commonService;

    /** Hibernate name search initialisation strategy */
    private static final List<String> NAME_SEARCH_INIT_STRATEGY = Arrays.asList(new String[] {
            "combinationAuthorship.$",
            "exCombinationAuthorship.$",
            "basionymAuthorship.$",
            "exBasionymAuthorship.$",
            "nameCache",
            "taxonBases"});

    /** Hibernate accepted name search initialisation strategy */
    private static final List<String> ACC_NAME_SEARCH_INIT_STRATEGY = Arrays.asList(new String[] {
            "nameCache",
            "taxonBases",
            "taxonBases.acceptedTaxon.name.nameCache",
            "taxonBases.acceptedTaxon.name.rank.titleCache",
            "taxonBases.acceptedTaxon.taxonNodes.classification",
            "taxonBases.taxonNodes.classification",
            "taxonBases.relationsFromThisTaxon.type.$"});

    /** Hibernate name information initialisation strategy */
    private static final List<String> NAME_INFORMATION_INIT_STRATEGY = Arrays.asList(new String[] {
            "taxonBases",
            "status",
            "nomenclaturalReference.$",
            "combinationAuthorship.$",
            "exCombinationAuthorship.$",
            "basionymAuthorship.$",
            "exBasionymAuthorship.$",
            "relationsToThisName.fromName.$",
            "relationsToThisName.nomenclaturalReference.$",
            "relationsToThisName.type.$",
            "relationsFromThisName.toName.$",
            "relationsFromThisName.nomenclaturalReference.$",
            "relationsFromThisName.type.$"});

    /** Hibernate taxon information initialisation strategy */
    private static final List<String> TAXON_INFORMATION_INIT_STRATEGY = Arrays.asList(new String[] {
            "name.titleCache",
            "name.rank.titleCache",

            "sec.updated",
            "sec.titleCache",
            "sources.citation.sources.idNamespace",
            "sources.citation.sources.idInSource",

            "synonyms.name.rank.titleCache",
            "synonyms.sec.updated",
            "synonyms.sec.titleCache",
            "synonyms.sources.citation.sources.idNamespace",
            "synonyms.sources.citation.sources.idInSource",
            "synonyms.type.inverseRepresentations",
            "acceptedTaxon.name.rank.titleCache",
            "acceptedTaxon.sec.titleCache",
            "acceptedTaxon.sources.citation.sources.idNamespace",
            "acceptedTaxon.sources.citation.sources.idInSource",

            "relationsFromThisTaxon.type.inverseRepresentations",
            "relationsFromThisTaxon.toTaxon.name.rank.titleCache",
            "relationsFromThisTaxon.toTaxon.sec.updated",
            "relationsFromThisTaxon.toTaxon.sec.titleCache",
            "relationsFromThisTaxon.toTaxon.sources.citation.sources.idNamespace",
            "relationsFromThisTaxon.toTaxon.sources.citation.sources.idInSource",

            "relationsToThisTaxon.type.inverseRepresentations",
            "relationsToThisTaxon.fromTaxon.name.rank.titleCache",
            "relationsToThisTaxon.fromTaxon.sec.updated",
            "relationsToThisTaxon.fromTaxon.sec.titleCache",
            "relationsToThisTaxon.fromTaxon.sources.citation.sources.idNamespace",
            "relationsToThisTaxon.fromTaxon.sources.citation.sources.idInSource",

            "taxonNodes",
            "taxonNodes.classification" });

    /** Hibernate taxon node initialisation strategy */
    private static final List<String> TAXON_NODE_INIT_STRATEGY = Arrays.asList(new String[] {
            "taxon.sec",
            "taxon.name",
            "classification",
            "classification.reference.$",
            "classification.reference.authorship.$" });

    /** Hibernate classification vocabulary initialisation strategy */
    private static final List<String> VOC_CLASSIFICATION_INIT_STRATEGY = Arrays.asList(new String[] {
            "classification",
            "classification.reference.$",
            "classification.reference.authorship.$" });

    /** Hibernate classification vocabulary initialisation strategy */
    private static final List<String> COMMON_INIT_STRATEGY = Arrays.asList(new String[] {});

    public NameCatalogueController() {
        super();
        setInitializationStrategy(Arrays.asList(new String[] { "$" }));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu
     * .etaxonomy.cdm.api.service.IService)
     */
    @Autowired
    @Override
    public void setService(INameService service) {
        this.service = service;
    }

    /**
     * Returns a documentation page for the Name Search API.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;name_catalogue</b>
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
        Resource resource = resourceLoader.getResource("classpath:eu/etaxonomy/cdm/doc/remote/apt/name-catalogue-default.apt");
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
     * Returns a list of scientific names matching the <code>{query}</code>
     * string pattern. Each of these scientific names is accompanied by a list of
     * name uuids, a list of taxon uuids, a list of accepted taxon uuids, etc.
     * <p>
     * Endpoint documentation can be found <a href="{@docRoot}/../remote/name-catalogue-default.html">here</a>
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;name_catalogue</b>
     *
     * @param queries
     *                The base scientific name pattern(s) to query for. The query can
     *                contain wildcard characters ('*'). The query can be
     *                performed with no wildcard or with the wildcard at the
     *                begin and / or end depending on the search pattern.
     * @param request Http servlet request.
     * @param response Http servlet response.
     * @return a list of {@link NameSearch} objects each corresponding to a
     *         single query. These are built from {@link TaxonNameBase}
     *         entities which are in turn initialized using
     *         the {@link #NAME_SEARCH_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(value = { "" }, method = {RequestMethod.GET,RequestMethod.POST} , params = {"query"})
    public ModelAndView doGetNameSearch(@RequestParam(value = "query", required = true) String[] queries,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
    return doGetNameSearch(queries, DEFAULT_SEARCH_TYPE, DEFAULT_MAX_NB_FOR_EXACT_SEARCH, request, response);
    }

    /**
     * Returns a list of scientific names matching the <code>{query}</code>
     * string pattern. Each of these scientific names is accompanied by a list of
     * name uuids, a list of taxon uuids and a list of accepted taxon uuids.
     * <p>
     * Endpoint documentation can be found <a href="{@docRoot}/../remote/name-catalogue-default.html">here</a>
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;name_catalogue</b>
     *
     * @param query
     *             	The scientific name pattern(s) to query for. The query can
     *             	contain wildcard characters ('*'). The query can be
     *              performed with no wildcard or with the wildcard at the
     *              begin and / or end depending on the search pattern.
     * @param type
     *              The type of name to query. This be either
     *              "name" : scientific name corresponding to 'name cache' in CDM or
     *              "title" : complete name corresponding to 'title cache' in CDM
     * @param hits
     *            	Maximum number of responses to be returned.
     * @param request Http servlet request.
     * @param response Http servlet response.
     * @return a List of {@link NameSearch} objects each corresponding to a
     *         single query. These are built from {@link TaxonNameBase} entities
     *         which are in turn initialized using the {@link #NAME_SEARCH_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(value = { "" }, method = {RequestMethod.GET,RequestMethod.POST}, params = {"query", "type"})
    public ModelAndView doGetNameSearch(@RequestParam(value = "query", required = true) String[] queries,
            @RequestParam(value = "type", required = false, defaultValue = DEFAULT_SEARCH_TYPE) String searchType,
            @RequestParam(value = "hits", required = false, defaultValue = DEFAULT_MAX_NB_FOR_EXACT_SEARCH) String hits,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        List<RemoteResponse> nsList = new ArrayList<RemoteResponse>();

        int h = 100;
        try {
            h = Integer.parseInt(hits);
        } catch(NumberFormatException nfe) {
            ErrorResponse er = new ErrorResponse();
            er.setErrorMessage("hits parameter is not a number");
            mv.addObject(er);
            return mv;
        }

        // search through each query
        for (String query : queries) {
            if(query.equals("")) {
                ErrorResponse er = new ErrorResponse();
                er.setErrorMessage("Empty query field");
                nsList.add(er);
                continue;
            }
            // remove wildcards if any
            String queryWOWildcards = getQueryWithoutWildCards(query);
            // convert first char to upper case
            char[] stringArray = queryWOWildcards.toCharArray();
            stringArray[0] = Character.toUpperCase(stringArray[0]);
            queryWOWildcards = new String(stringArray);

            boolean wc = false;

            if(getMatchModeFromQuery(query) == MatchMode.BEGINNING) {
                wc = true;
            }
            logger.info("doGetNameSearch()" + request.getRequestURI() + " for query \"" + query);

            List<DocumentSearchResult> nameSearchList = new ArrayList<DocumentSearchResult>();
            try {
                nameSearchList = service.findByNameExactSearch(
                        queryWOWildcards,
                        wc,
                        null,
                        false,
                        h);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
                ErrorResponse er = new ErrorResponse();
                er.setErrorMessage("Could not parse name : " + query);
                nsList.add(er);
                continue;
            }


            // if search is successful then get related information , else return error
            if (nameSearchList == null || !nameSearchList.isEmpty()) {
                NameSearch ns = new NameSearch();
                ns.setRequest(query);

                for (DocumentSearchResult searchResult : nameSearchList) {
                    for(Document doc : searchResult.getDocs()) {

                    // we need to retrieve both taxon uuid of name queried and
                    // the corresponding accepted taxa.
                    // reason to return accepted taxa also, is to be able to get from
                    // scientific name to taxon concept in two web service calls.
                    List<String> tbUuidList = new ArrayList<String>();//nvn.getTaxonBases();
                    List<String> accTbUuidList = new ArrayList<String>();
                    String[] tbUuids = doc.getValues("taxonBases.uuid");
                    String[] tbClassNames = doc.getValues("taxonBases.classInfo.name");
                    for(int i=0;i<tbUuids.length;i++) {
                        if(tbClassNames[i].equals("eu.etaxonomy.cdm.model.taxon.Taxon")) {
                            accTbUuidList.add(tbUuids[i]);
                        }
                    }
                    // update name search object
                    ns.addToResponseList(doc.getValues("titleCache")[0],
                            doc.getValues("nameCache")[0],
                            searchResult.getMaxScore(),
                            doc.getValues("uuid")[0].toString(),
                            doc.getValues("taxonBases.uuid"),
                            mergeSynAccTaxonUuids(doc.getValues("taxonBases.accTaxon.uuids")));
                    }
                }
                nsList.add(ns);

            } else {
                ErrorResponse er = new ErrorResponse();
                er.setErrorMessage("No Taxon Name matches : " + query);
                nsList.add(er);
            }
        }

        mv.addObject(nsList);
        return mv;
    }

    /**
     * Returns a documentation page for the Fuzzy Name Search API.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;name_catalogue/accepted</b>
     *
     * @param request Http servlet request.
     * @param response Http servlet response.
     * @return Html page describing the Fuzzy Name Search API
     * @throws IOException
     */
    @RequestMapping(value = { "fuzzy" }, method = RequestMethod.GET, params = {})
    public ModelAndView doGetNameFuzzySearchDocumentation(
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ModelAndView mv = new ModelAndView();
        // Read apt documentation file.
        Resource resource = resourceLoader.getResource("classpath:eu/etaxonomy/cdm/doc/remote/apt/name-catalogue-fuzzy.apt");
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
     * Returns a list of scientific names similar to the <code>{query}</code>
     * string pattern. Each of these scientific names is accompanied by a list of
     * name uuids, a list of taxon uuids and a list of accepted taxon uuids.
     * The underlying (Lucene FuzzyQuery) string distance metric used is based on a
     * fail-fast Levenshtein distance algorithm (is aborted if it is discovered that
     * the mimimum distance between the words is greater than some threshold)
     * <p>
     * Endpoint documentation can be found <a href="{@docRoot}/../remote/name-catalogue-fuzzy.html">here</a>
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;name_catalogue/fuzzy</b>
     *
     * @param query
     *                The scientific name pattern(s) to query for. Any wildcard characters in the
     *                query are removed.
     * @param accuracy
     *                Similarity measure (between 0 and 1) to impose on the matching algorithm.
     *                Briefly described, this is equivalent to the edit distance between the two words, divided by
     *                the length of the shorter of the compared terms.
     * @param hits
     *            Maximum number of responses to be returned.
     * @param type
     *            The type of fuzzy search to call. This can be either
     *              "name" : fuzzy searches scientific names corresponding to 'name cache' in CDM or
     *              "atomised" : parses the query into atomised elements and fuzzy searches the individual elements in the CDM
     * @param request Http servlet request.
     * @param response Http servlet response.
     * @return a List of {@link NameSearch} objects each corresponding to a
     *         single query. These are built from {@link TaxonNameBase} entities
     *         which are in turn initialized using the {@link #NAME_SEARCH_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(value = { "fuzzy" }, method = RequestMethod.GET, params = {"query"})
    public ModelAndView doGetNameFuzzySearch(@RequestParam(value = "query", required = true) String[] queries,
            @RequestParam(value = "accuracy", required = false, defaultValue = "0.6") String accuracy,
            @RequestParam(value = "hits", required = false, defaultValue = "10") String hits,
            @RequestParam(value = "type", required = false, defaultValue = FUZZY_NAME_CACHE) String type,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        List<RemoteResponse> nsList = new ArrayList<RemoteResponse>();
        float acc = 0.5f;
        int h = 10;
        try {
            acc = Float.parseFloat(accuracy);
            h = Integer.parseInt(hits);
        } catch(NumberFormatException nfe) {
            ErrorResponse er = new ErrorResponse();
            er.setErrorMessage("accuracy or hits parameter is not a number");
            mv.addObject(er);
            return mv;
        }

        if(acc < 0.0 || acc >= 1.0) {
            ErrorResponse er = new ErrorResponse();
            er.setErrorMessage("accuracy should be >= 0.0 and < 1.0");
            mv.addObject(er);
            return mv;
        }
        // search through each query
        for (String query : queries) {
            if(query.equals("")) {
                ErrorResponse er = new ErrorResponse();
                er.setErrorMessage("Empty query field");
                nsList.add(er);
                continue;
            }
            // remove wildcards if any
            String queryWOWildcards = getQueryWithoutWildCards(query);
            // convert first char to upper case
            char[] stringArray = queryWOWildcards.toCharArray();
            stringArray[0] = Character.toUpperCase(stringArray[0]);
            queryWOWildcards = new String(stringArray);
            logger.info("doGetNameSearch()" + request.getRequestURI() + " for query \"" + queryWOWildcards + " with accuracy " + accuracy);
            //List<NonViralName> nameList = new ArrayList<NonViralName>();
            List<DocumentSearchResult> nameSearchList = new ArrayList<DocumentSearchResult>();
            try {
                if(type.equals(FUZZY_ATOMISED)) {
                    nameSearchList = service.findByNameFuzzySearch(
                            queryWOWildcards,
                            acc,
                            null,
                            false,
                            h);
                } else {
                    nameSearchList = service.findByFuzzyNameCacheSearch(
                            queryWOWildcards,
                            acc,
                            null,
                            false,
                            h);
                }
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
                ErrorResponse er = new ErrorResponse();
                er.setErrorMessage("Could not parse name : " + queryWOWildcards);
                nsList.add(er);
                continue;
            }


            // if search is successful then get related information , else return error
            if (nameSearchList == null || !nameSearchList.isEmpty()) {
                NameSearch ns = new NameSearch();
                ns.setRequest(query);

                for (DocumentSearchResult searchResult : nameSearchList) {
                    for(Document doc : searchResult.getDocs()) {

                    // we need to retrieve both taxon uuid of name queried and
                    // the corresponding accepted taxa.
                    // reason to return accepted taxa also, is to be able to get from
                    // scientific name to taxon concept in two web service calls.
                    List<String> tbUuidList = new ArrayList<String>();//nvn.getTaxonBases();
                    List<String> accTbUuidList = new ArrayList<String>();
                    String[] tbUuids = doc.getValues("taxonBases.uuid");
                    String[] tbClassNames = doc.getValues("taxonBases.classInfo.name");
                    for(int i=0;i<tbUuids.length;i++) {
                        if(tbClassNames[i].equals("eu.etaxonomy.cdm.model.taxon.Taxon")) {
                            accTbUuidList.add(tbUuids[i]);
                        }
                    }
                    // update name search object
                    ns.addToResponseList(doc.getValues("titleCache")[0],
                            doc.getValues("nameCache")[0],
                            searchResult.getMaxScore(),
                            doc.getValues("uuid")[0].toString(),
                            doc.getValues("taxonBases.uuid"),
                            mergeSynAccTaxonUuids(doc.getValues("taxonBases.accTaxon.uuids")));
                    }
                }
                nsList.add(ns);

            } else {
                ErrorResponse er = new ErrorResponse();
                er.setErrorMessage("No Taxon Name matches : " + query + ", for given accuracy");
                nsList.add(er);
            }
        }

        mv.addObject(nsList);
        return mv;
    }

    private String[] mergeSynAccTaxonUuids(String[] accTaxonUuids) {
        List<String> accTaxonUuidList = new ArrayList<String>();
        for(String accTaxonUuid : accTaxonUuids) {
            for(String uuidListAsString : accTaxonUuid.split(AcceptedTaxonBridge.ACCEPTED_TAXON_UUID_LIST_SEP)) {
                accTaxonUuidList.add(uuidListAsString);
            }
        }
        return accTaxonUuidList.toArray(new String[0]);

    }
    /**
     * Returns a documentation page for the Name Information API.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;name_catalogue/name</b>
     *
     * @param request Http servlet request.
     * @param response Http servlet response.
     * @return Html page describing the Name Information API
     * @throws IOException
     */
    @RequestMapping(value = { "name" }, method = RequestMethod.GET, params = {})
    public ModelAndView doGetNameInformationDocumentation(
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ModelAndView mv = new ModelAndView();
        // Read apt documentation file.
        Resource resource = resourceLoader.getResource("classpath:eu/etaxonomy/cdm/doc/remote/apt/name-catalogue-name-info.apt");
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
     * Returns information related to the scientific name matching the given
     * <code>{nameUuid}</code>. The information includes the name string,
     * relationships, rank, list of related lsids / taxon uuids, etc.
     * <p>
     * Endpoint documentation can be found <a href="{@docRoot}/../remote/name-catalogue-name-info.html">here</a>
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;name_catalogue/name</b>
     *
     * @param nameUuids uuid(s) of the scientific name to search for.
     * @param request Http servlet request.
     * @param response Http servlet response.
     * @return a List of {@link NameInformation} objects each corresponding to a
     *         single name uuid. These are built from {@link TaxonNameBase} entities
     *         which are in turn initialized using the {@link #NAME_INFORMATION_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(value = { "name" }, method = {RequestMethod.GET,RequestMethod.POST}, params = {"nameUuid"})
    public ModelAndView doGetNameInformation(@RequestParam(value = "nameUuid", required = true) String[] nameUuids,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        List<RemoteResponse> niList = new ArrayList<RemoteResponse>();
        // loop through each name uuid
        for (String nameUuid : nameUuids) {
            logger.info("doGetNameInformation()" + request.getRequestURI() + " for name uuid \""
                    + nameUuid + "\"");
            // find name by uuid
            TaxonNameBase<?,?> nvn = service.load(UUID.fromString(nameUuid),NAME_INFORMATION_INIT_STRATEGY);

            // if search is successful then get related information, else return error
            if (nvn != null) {
                NameInformation ni = new NameInformation();
                ni.setRequest(nameUuid);
                Reference ref = (Reference) nvn.getNomenclaturalReference();
                String citation = "";
                String citation_details = "";
                if (ref != null) {
                    citation = ref.getTitleCache();
                }
                // update name information object
                ni.setResponse(nvn.getTitleCache(), nvn.getNameCache(), nvn.getRank().getTitleCache(),
                        nvn.getStatus(), citation, nvn.getRelationsFromThisName(),
                        nvn.getRelationsToThisName(), nvn.getTaxonBases());
                niList.add(ni);
            } else {
                ErrorResponse re = new ErrorResponse();

                if(isValid(nameUuid)) {
                    re.setErrorMessage("No Name for given UUID : " + nameUuid);
                } else {
                    re.setErrorMessage(nameUuid + " not a valid UUID");
                }
                niList.add(re);
            }
        }
        mv.addObject(niList);
        return mv;
    }

    /**
     * Returns a documentation page for the Taxon Information API.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;name_catalogue/taxon</b>
     *
     * @param request Http servlet request.
     * @param response Http servlet response.
     * @return Html page describing the Taxon Information API
     * @throws IOException
     */
    @RequestMapping(value = { "taxon" }, method = RequestMethod.GET, params = {})
    public ModelAndView doGetTaxonInformation(
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ModelAndView mv = new ModelAndView();
        // Read apt documentation file.
        Resource resource = resourceLoader.getResource("classpath:eu/etaxonomy/cdm/doc/remote/apt/name-catalogue-taxon-info.apt");
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
     * Returns information related to the taxon matching the given
     * <code>{taxonUuid}</code>. The information includes the name cache, title cache
     * relationship type, taxonomic status, information , etc.
     * <p>
     * Endpoint documentation can be found <a href="{@docRoot}/../remote/name-catalogue-taxon-info.html">here</a>
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;name_catalogue</b>
     *
     * @param taxonUuid
     *                 The taxon uuid to query for. The classification returned corresponds
     *                 to the first in the alphabetically sorted list of classifications
     *                 currently available in the database.
     *
     * @param request Http servlet request.
     * @param response Http servlet response.
     * @return a List of {@link TaxonInformation} objects each corresponding to a
     *         single query. These are built from {@TaxonBase} entities which are
     *         in turn initialized using the {@link #TAXON_INFORMATION_INIT_STRATEGY}
     * @throws IOException
     */
//    @RequestMapping(value = { "taxon" }, method = RequestMethod.GET,params = {"taxonUuid"})
//    public ModelAndView doGetTaxonInformation(
//            @RequestParam(value = "taxonUuid", required = true) String[] taxonUuids,
//            HttpServletRequest request, HttpServletResponse response) throws IOException {
//        return doGetTaxonInformation(taxonUuids,CLASSIFICATION_DEFAULT, new String[]{},request, response);
//    }

    /**
     * Returns information related to the taxon matching the given
     * <code>{taxonUuid}</code>. The information includes the name cache, title cache
     * relationship type, taxonomic status, information , etc.
     * <p>
     * Endpoint documentation can be found <a href="{@docRoot}/../remote/name-catalogue-taxon-info.html">here</a>
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;name_catalogue/taxon</b>
     *
     * @param taxonUuid
     *                 The taxon uuid to query for.
     * @param classification
     *                 [Optional] String representing the taxonomic classification to use for
     *                 building the classification tree. Defaults to the first in the alphabetically
     *                 sorted list of classifications currently available in the database.
     * @param include
     *                 Array of data types to be included in addition to the normal response
     *
     * @param request Http servlet request.
     * @param response Http servlet response.
     * @return a List of {@link TaxonInformation} objects each corresponding to a
     *         single query. These are built from {@TaxonBase} entities which are
     *         in turn initialized using the {@link #TAXON_INFORMATION_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(value = { "taxon" }, method = {RequestMethod.GET,RequestMethod.POST}, params = {"taxonUuid"})
    public ModelAndView doGetTaxonInformation(
            @RequestParam(value = "taxonUuid", required = true) String[] taxonUuids,
            @RequestParam(value = "classification", required = false, defaultValue = CLASSIFICATION_DEFAULT) String classificationType,
            @RequestParam(value = "include", required = false, defaultValue = "") String[] includes,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        List<RemoteResponse> tiList = new ArrayList<RemoteResponse>();
        // loop through each name uuid
        for (String taxonUuid : taxonUuids) {
            logger.info("doGetTaxonInformation()" + request.getRequestURI() + " for taxon uuid \""
                    + taxonUuid);
            // find name by uuid
            TaxonBase<?> tb = taxonService.findTaxonByUuid(UUID.fromString(taxonUuid),
                    TAXON_INFORMATION_INIT_STRATEGY);

            // if search is successful then get related information, else return error
            if (tb != null) {
                TaxonInformation ti = new TaxonInformation();
                ti.setRequest(taxonUuid);
                // check if result (taxon base) is a taxon or synonym
                if (tb.isInstanceOf(Taxon.class)) {
                    Taxon taxon = (Taxon) tb;
                    // build classification map
                    boolean includeUuids = Arrays.asList(includes).contains(INCLUDE_CLUUIDS);
                    Map<String,Map> classificationMap = getClassification(taxon, classificationType, includeUuids);

                    logger.info("taxon uuid " + taxon.getUuid().toString() + " original hash code : " + System.identityHashCode(taxon) + ", name class " + taxon.getName().getClass().getName());
                    // update taxon information object with taxon related data
                    INonViralName nvn = CdmBase.deproxy(taxon.getName());

                    String secTitle = "" ;
                    String modified = "";
                    if(taxon.getSec() != null) {
                        secTitle = taxon.getSec().getTitleCache();
                        DateTime dt = taxon.getUpdated();
                        modified = fmt.print(dt);
                    }

                    Set<IdentifiableSource> sources = taxon.getSources();
                    String[] didname = getDatasetIdName(sources);

                    String lsidString = null;
                    if( taxon.getLsid() != null) {
                        lsidString = taxon.getLsid().toString();
                    }

                    ti.setResponseTaxon(tb.getTitleCache(),
                            nvn.getTitleCache(),
                            nvn.getRank().getTitleCache(),
                            ACCEPTED_NAME_STATUS,
                            buildFlagMap(tb),
                            classificationMap,
                            "",
                            didname[0],
                            didname[1],
                            secTitle,
                            modified,
                            lsidString
                     );


                    Set<Synonym> syns = taxon.getSynonyms();
                    // add synonyms (if exists) to taxon information object
                    for (Synonym syn : syns) {
                        String uuid = syn.getUuid().toString();
                        String title = syn.getTitleCache();
                        TaxonNameBase<?,?> synnvn = syn.getName();
                        String name = synnvn.getTitleCache();
                        String rank = (synnvn.getRank() == null)? "" : synnvn.getRank().getTitleCache();
                        String status = SYNONYM_STATUS;
                        String relLabel = syn.getType()
                                .getInverseRepresentation(Language.DEFAULT())
                                .getLabel();

                        secTitle = "" ;
                        modified = "";
                        if(syn.getSec() != null) {
                            secTitle = syn.getSec().getTitleCache();
                            DateTime dt = syn.getUpdated();
                            modified = fmt.print(dt);
                        }

                        sources = syn.getSources();
                        didname = getDatasetIdName(sources);

                        ti.addToResponseRelatedTaxa(uuid,
                                title,
                                name,
                                rank,
                                status,
                                relLabel,
                                "",
                                didname[0],
                                didname[1],
                                secTitle,
                                modified);
                    }

                    // build relationship information as,
                    // - relationships from the requested taxon
                    Set<TaxonRelationship> trFromSet = taxon.getRelationsFromThisTaxon();
                    for (TaxonRelationship tr : trFromSet) {
                        String titleTo = tr.getToTaxon().getTitleCache();
                        TaxonNameBase tonvn = tr.getToTaxon().getName();
                        String name = tonvn.getTitleCache();
                        String rank = tonvn.getRank().getTitleCache();
                        String uuid = tr.getToTaxon().getUuid().toString();
                        String status = ACCEPTED_NAME_STATUS;
                        String relLabel = tr.getType().getRepresentation(Language.DEFAULT())
                                .getLabel();

                        secTitle = "" ;
                        modified = "";
                        if(tr.getToTaxon().getSec() != null) {
                            secTitle = tr.getToTaxon().getSec().getTitleCache();
                            DateTime dt = tr.getToTaxon().getUpdated();
                            modified = fmt.print(dt);
                        }

                        sources = tr.getToTaxon().getSources();
                        didname = getDatasetIdName(sources);

                        ti.addToResponseRelatedTaxa(uuid,
                                titleTo,
                                name,
                                rank,
                                status,
                                relLabel,
                                "",
                                didname[0],
                                didname[1],
                                secTitle,
                                modified);
                        //logger.info("titleTo : " + titleTo + " , name : " + name);
                    }

                    // - relationships to the requested taxon
                    Set<TaxonRelationship> trToSet = taxon.getRelationsToThisTaxon();
                    for (TaxonRelationship tr : trToSet) {
                        String titleFrom = tr.getFromTaxon().getTitleCache();
                        TaxonNameBase fromnvn = tr.getFromTaxon().getName();
                        String name = fromnvn.getTitleCache();
                        String rank = fromnvn.getRank().getTitleCache();
                        String uuid = tr.getFromTaxon().getUuid().toString();
                        String status = ACCEPTED_NAME_STATUS;
                        String relLabel = tr.getType()
                                .getInverseRepresentation(Language.DEFAULT())
                                .getLabel();

                        if(tr.getFromTaxon().getSec() != null) {
                            secTitle = tr.getFromTaxon().getSec().getTitleCache();
                            DateTime dt = tr.getFromTaxon().getSec().getUpdated();
                            modified = fmt.print(dt);
                        }

                        sources = tr.getFromTaxon().getSources();
                        didname = getDatasetIdName(sources);

                        secTitle = (tr.getFromTaxon().getSec() == null) ? "" : tr.getFromTaxon().getSec().getTitleCache();
                        ti.addToResponseRelatedTaxa(uuid,
                                titleFrom,
                                name,
                                rank,
                                status,
                                relLabel,
                                "",
                                didname[0],
                                didname[1],
                                secTitle,
                                modified);
                        //logger.info("titleFrom : " + titleFrom + " , name : " + name);
                    }
                } else if (tb instanceof Synonym) {
                    Synonym synonym = (Synonym) tb;
                    TaxonNameBase nvn = synonym.getName();
                 // update taxon information object with synonym related data
                    DateTime dt = synonym.getUpdated();
                    String modified = fmt.print(dt);

                    Set<IdentifiableSource> sources = synonym.getSources();
                    String[] didname = getDatasetIdName(sources);

                    String secTitle = (synonym.getSec() == null) ? "" : synonym.getSec().getTitleCache();
                    ti.setResponseTaxon(synonym.getTitleCache(),
                            nvn.getTitleCache(),
                            nvn.getRank().getTitleCache(),
                            SYNONYM_STATUS,
                            buildFlagMap(synonym),
                            new TreeMap<String,Map>(),
                            "",
                            didname[0],
                            didname[1],
                            secTitle,
                            modified, null);
                    // add accepted taxa (if exists) to taxon information object

                    Taxon accTaxon = synonym.getAcceptedTaxon();
                    if (accTaxon != null){
                        String uuid = accTaxon.getUuid().toString();
                        logger.info("acc taxon uuid " + accTaxon.getUuid().toString() + " original hash code : " + System.identityHashCode(accTaxon) + ", name class " + accTaxon.getName().getClass().getName());
                        String title = accTaxon.getTitleCache();
                        logger.info("taxon title cache : " + accTaxon.getTitleCache());

                        TaxonNameBase<?,?> accnvn = accTaxon.getName();
                        String name = accnvn.getTitleCache();
                        String rank = accnvn.getRank().getTitleCache();
                        String status = ACCEPTED_NAME_STATUS;
                        String relLabel = synonym.getType().getRepresentation(Language.DEFAULT())
                                .getLabel();
                        dt = accTaxon.getUpdated();
                        modified = fmt.print(dt);

                        sources = accTaxon.getSources();
                        didname = getDatasetIdName(sources);

                        secTitle = (accTaxon.getSec() == null) ? "" : accTaxon.getSec().getTitleCache();
                        ti.addToResponseRelatedTaxa(uuid,
                                title,
                                name,
                                rank,
                                status,
                                relLabel,
                                "",
                                didname[0],
                                didname[1],
                                secTitle,
                                modified);
                    }
                }
                tiList.add(ti);
            } else {
                ErrorResponse re = new ErrorResponse();
                if(isValid(taxonUuid)) {
                    re.setErrorMessage("No Taxon for given UUID : " + taxonUuid);
                } else {
                    re.setErrorMessage(taxonUuid + " not a valid UUID");
                }
                tiList.add(re);
            }
        }
        mv.addObject(tiList);
        return mv;
    }

    /**
     * Returns a documentation page for the Accepted Name Search API.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;name_catalogue/accepted</b>
     *
     * @param request Http servlet request.
     * @param response Http servlet response.
     * @return Html page describing the Accepted Name Search API
     * @throws IOException
     */
    @RequestMapping(value = { "accepted" }, method = RequestMethod.GET, params = {})
    public ModelAndView doGetAcceptedNameSearchDocumentation(
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ModelAndView mv = new ModelAndView();
        // Read apt documentation file.
        Resource resource = resourceLoader.getResource("classpath:eu/etaxonomy/cdm/doc/remote/apt/name-catalogue-accepted.apt");
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
     * Returns a list of accepted names of input scientific names matching the <code>{query}</code>
     * string pattern. Each of these scientific names is accompanied by a list of
     * name uuids, a list of taxon uuids and a list of accepted taxon uuids.
     * <p>
     * Endpoint documentation can be found <a href="{@docRoot}/../remote/name-catalogue-default.html">here</a>
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;name_catalogue</b>
     *
     * @param query
     *                The scientific name pattern(s) to query for. The query can
     *                contain wildcard characters ('*'). The query can be
     *                performed with no wildcard or with the wildcard at the
     *                begin and / or end depending on the search pattern.
     * @param type
     *                The type of name to query. This be either
     *                "name" : scientific name corresponding to 'name cache' in CDM or
     *                "title" : complete name corresponding to 'title cache' in CDM
     * @param request Http servlet request.
     * @param response Http servlet response.
     * @return a List of {@link NameSearch} objects each corresponding to a
     *         single query. These are built from {@link TaxonNameBase} entities
     *         which are in turn initialized using the {@link #NAME_SEARCH_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(value = { "accepted" }, method = {RequestMethod.GET,RequestMethod.POST}, params = {"query"})
    public ModelAndView doGetAcceptedNameSearch(@RequestParam(value = "query", required = true) String[] queries,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        return doGetAcceptedNameSearch(queries, DEFAULT_SEARCH_TYPE, request, response);
    }
    /**
     * Returns a list of accepted names of input scientific names matching the <code>{query}</code>
     * string pattern. Each of these scientific names is accompanied by a list of
     * name uuids, a list of taxon uuids and a list of accepted taxon uuids.
     * <p>
     * Endpoint documentation can be found <a href="{@docRoot}/../remote/name-catalogue-default.html">here</a>
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;name_catalogue</b>
     *
     * @param query
     *                The scientific name pattern(s) to query for. The query can
     *                contain wildcard characters ('*'). The query can be
     *                performed with no wildcard or with the wildcard at the
     *                begin and / or end depending on the search pattern.
     * @param type
     *                The type of name to query. This be either
     *                "name" : scientific name corresponding to 'name cache' in CDM or
     *                "title" : complete name corresponding to 'title cache' in CDM
     * @param request Http servlet request.
     * @param response Http servlet response.
     * @return a List of {@link NameSearch} objects each corresponding to a
     *         single query. These are built from {@link TaxonNameBase} entities
     *         which are in turn initialized using the {@link #NAME_SEARCH_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(value = { "accepted" }, method = {RequestMethod.GET,RequestMethod.POST}, params = {"query", "type"})
    public ModelAndView doGetAcceptedNameSearch(@RequestParam(value = "query", required = true) String[] queries,
            @RequestParam(value = "type", required = false, defaultValue = DEFAULT_SEARCH_TYPE) String searchType,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        List<RemoteResponse> ansList = new ArrayList<RemoteResponse>();
        logger.info("doGetAcceptedNameSearch()");

        // if search type is not known then return error
        if (!searchType.equals(NAME_SEARCH) && !searchType.equals(TITLE_SEARCH)) {
            ErrorResponse er = new ErrorResponse();
            er.setErrorMessage("searchType parameter can only be set as" + NAME_SEARCH + " or "
                    + TITLE_SEARCH);
            mv.addObject(er);
            return mv;
        }

        // search through each query
        for (String query : queries) {

            //String queryWOWildcards = getQueryWithoutWildCards(query);
            //MatchMode mm = getMatchModeFromQuery(query);
            logger.info("doGetAcceptedNameSearch()" + request.getRequestURI() + " for query \"" + query);
            List<NonViralName> nameList = new ArrayList<NonViralName>();

            // if "name" search then find by name cache
            if (searchType.equals(NAME_SEARCH)) {
                nameList = service.findNamesByNameCache(query, MatchMode.EXACT,
                        ACC_NAME_SEARCH_INIT_STRATEGY);
            }

            //if "title" search then find by title cache
            if (searchType.equals(TITLE_SEARCH)) {
                nameList = service.findNamesByTitleCache(query, MatchMode.EXACT,
                        ACC_NAME_SEARCH_INIT_STRATEGY);
            }

            // if search is successful then get related information , else return error
            if (nameList == null || !nameList.isEmpty()) {
                AcceptedNameSearch ans = new AcceptedNameSearch();
                ans.setRequest(query);

                for (INonViralName nvn : nameList) {
                    // we need to retrieve both taxon uuid of name queried and
                    // the corresponding accepted taxa.
                    // reason to return accepted taxa also, is to be able to get from
                    // scientific name to taxon concept in two web service calls.
                    Set<TaxonBase> tbSet = nvn.getTaxonBases();
                    Set<TaxonBase> accTbSet = new HashSet<TaxonBase>();
                    for (TaxonBase tb : tbSet) {
                        // if synonym then get accepted taxa.
                        if (tb instanceof Synonym) {
                            Synonym synonym = (Synonym) tb;
                            Taxon accTaxon = synonym.getAcceptedTaxon();
                            if (accTaxon != null) {
                                INonViralName accNvn = CdmBase.deproxy(accTaxon.getName());
                                Map<String, Map> classificationMap = getClassification(accTaxon, CLASSIFICATION_DEFAULT, false);
                                ans.addToResponseList(accNvn.getNameCache(),accNvn.getAuthorshipCache(), accNvn.getRank().getTitleCache(), classificationMap);
                            }
                        } else {
                            Taxon taxon = (Taxon)tb;
                            Set<TaxonRelationship> trFromSet = taxon.getRelationsFromThisTaxon();
                            boolean isConceptRelationship = true;
                            if(trFromSet.size() == 1) {
                                for (TaxonRelationship tr : trFromSet) {
                                    if(!tr.getType().isConceptRelationship()) {
                                        // this is not a concept relationship, so it does not have an
                                        // accepted name
                                        isConceptRelationship = false;

                                    }
                                }
                            }
                            if(isConceptRelationship) {
                                Map classificationMap = getClassification(taxon, CLASSIFICATION_DEFAULT, false);
                                ans.addToResponseList(nvn.getNameCache(), nvn.getAuthorshipCache(),nvn.getRank().getTitleCache(), classificationMap);
                            }

                        }
                    }
                    // update name search object

                }
                ansList.add(ans);

            } else {
                ErrorResponse er = new ErrorResponse();
                er.setErrorMessage("No Taxon Name for given query : " + query);
                ansList.add(er);
            }
        }

        mv.addObject(ansList);
        return mv;
    }

    /**
     * Returns a list of all available classifications (with associated referenc information) and the default classification.
     * <p>
     * Endpoint documentation can be found <a href="{@docRoot}/../remote/name-catalogue-classification-info.html">here</a>
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;name_catalogue/voc/classification</b>
     *
     * @param request Http servlet request.
     * @param response Http servlet response.
     * @return a List of {@link Classification} objects represebted as strings.
     *         These are initialized using the {@link #VOC_CLASSIFICATION_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(value = { "voc/classification" }, method = RequestMethod.GET, params = {})
    public ModelAndView doGetClassificationMap(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Map> cmapList = new ArrayList<Map>();
        Map<String, String> classifications = new HashMap<String, String>();
        ModelAndView mv = new ModelAndView();
        List<Classification> clist = getClassificationList(100);
        boolean isFirst = true;
        Iterator itr = clist.iterator();
        // loop through all classifications and populate map with
        // (classificationKey, reference) elements
        while(itr.hasNext()) {
            Classification c = (Classification) itr.next();
            String refTitleCache = "";
            String classificationKey = removeInternalWhitespace(c.getTitleCache());
            if(c.getReference() != null) {
                refTitleCache = c.getReference().getTitleCache();
                c.getAllNodes();
            }
            // default is the first element of the list
            // always created with the same sorting (DESCENDING)
            if(isFirst) {
                Map<String, String> defaultMap = new HashMap<String, String>();
                defaultMap.put("default", classificationKey);
                cmapList.add(defaultMap);
                isFirst = false;
            }
            classifications.put(classificationKey, refTitleCache);

        }
        Map<String, Map> cmap = new HashMap<String, Map>();
        cmap.put("classification",classifications);
        cmapList.add(cmap);
        mv.addObject(cmapList);
        return mv;
    }

    /**
     * Returns the Dataset ID / Name of the given original source.
     * FIXME: Very hacky and needs to be revisited. Mainly for deciding on which objects to use during import.
     * FIXME: dataset id is mapped to a DWC term - is that right?
     *
     * @param sources Set of sources attached to a taxa / synonym
     *
     *
     * @return String array where [0] is the datsetID and [1] is the datsetName
     */
    private String[] getDatasetIdName(Set<IdentifiableSource> sources) {
        String didname[] = {"",""};
        Iterator<IdentifiableSource> itr = sources.iterator();
        while(itr.hasNext()) {
            IdentifiableSource source = itr.next();
            Reference ref = source.getCitation();
            Set<IdentifiableSource> ref_sources = ref.getSources();
            Iterator<IdentifiableSource> ref_itr = ref_sources.iterator();
            while(ref_itr.hasNext()) {
                IdentifiableSource ref_source = ref_itr.next();
                if(ref_source.getIdNamespace().equals(DWC_DATASET_ID)) {
                    didname[0] = ref_source.getIdInSource();
                    break;
                }
            }
            if(!didname[0].isEmpty()) {
                didname[1] = ref.getTitleCache();
                break;
            }
        }
        return didname;
    }

    /**
     * Returns the match mode by parsing the input string of wildcards.
     *
     * @param query
     *             String to parse.
     *
     * @return {@link MatchMode} depending on the the position of the wildcard (*)
     */
    private MatchMode getMatchModeFromQuery(String query) {
        if (query.startsWith("*") && query.endsWith("*")) {
            return MatchMode.ANYWHERE;
        } else if (query.startsWith("*")) {
            return MatchMode.END;
        } else if (query.endsWith("*")) {
            return MatchMode.BEGINNING;
        } else {
            return MatchMode.EXACT;
        }
    }

    /**
     * Removes wildcards from the input string.
     *
     * @param query
     *             String to parse.
     *
     * @return input string with wildcards removed
     */
    private String getQueryWithoutWildCards(String query) {

        String newQuery = query;

        if (query.startsWith("*")) {
            newQuery = newQuery.substring(1, newQuery.length());
        }

        if (query.endsWith("*")) {
            newQuery = newQuery.substring(0, newQuery.length() - 1);
        }

        return newQuery.trim();
    }

    /**
     * Build map with taxon flag key-value pairs.
     */
    private Map<String, String> buildFlagMap(TaxonBase tb) {
        Map<String, String> flags = new Hashtable<String, String>();
        flags.put(DOUBTFUL_FLAG, Boolean.toString(tb.isDoubtful()));
        return flags;
    }

    /**
     * Build classification map.
     */
    private Map<String, Map> getClassification(Taxon taxon, String classificationType, boolean includeUuids) {
        // Using TreeMap is important, because we need the sorting of the classification keys
        // in the map to be stable.
        TreeMap<String, Map> sourceClassificationMap = buildClassificationMap(taxon, includeUuids);

        // if classification key is 'default' then return the default element of the map
        if(classificationType.equals(CLASSIFICATION_DEFAULT) && !sourceClassificationMap.isEmpty()) {
            List<Classification> clist = getClassificationList(1);
            String defaultKey = removeInternalWhitespace(clist.get(0).getTitleCache());
            return sourceClassificationMap.get(defaultKey);
            // if classification key is provided then return the classification corresponding to the key
        } else if(sourceClassificationMap.containsKey(classificationType)) {
            return sourceClassificationMap.get(classificationType);
            // if classification key is 'all' then return the entire map
        } else if(classificationType.equals(CLASSIFICATION_ALL)) {
            return sourceClassificationMap;
        } else {
            return new TreeMap<String,Map>();
        }
    }

    /**
     * Build classification map.
     */
    private TreeMap<String, Map> buildClassificationMap(Taxon taxon, boolean includeUuid) {
        // Using TreeMap is important, because we need the sorting of the classification keys
        // in the map to be stable.
        TreeMap<String, Map> sourceClassificationMap = new TreeMap<String, Map>();
        Set<TaxonNode> taxonNodes = taxon.getTaxonNodes();
        //loop through taxon nodes and build classification map for each classification key
        for (TaxonNode tn : taxonNodes) {
            Map<String, Object> classificationMap = new LinkedHashMap<String, Object>();
            List<TaxonNode> tnList = classificationService.loadTreeBranchToTaxon(taxon,
                    tn.getClassification(), null, TAXON_NODE_INIT_STRATEGY);
            for (TaxonNode classificationtn : tnList) {
                if(includeUuid) {
                    // creating map object with <name, uuid> elements
                    Map<String, String> clMap = new HashMap<String, String>();
                    clMap.put("name",classificationtn.getTaxon().getName().getTitleCache());
                    clMap.put("uuid",classificationtn.getTaxon().getUuid().toString());
                    classificationMap.put(classificationtn.getTaxon().getName().getRank().getTitleCache(), clMap);
                } else {
                    classificationMap.put(classificationtn.getTaxon().getName().getRank().getTitleCache(),
                            classificationtn.getTaxon().getName().getTitleCache());
                }
            }
            String cname = removeInternalWhitespace(tn.getClassification().getTitleCache());
            logger.info("Building classification map " + cname);
            sourceClassificationMap.put(cname, classificationMap);
        }
        return sourceClassificationMap;
    }

    private String removeInternalWhitespace(String withWSpace) {
        String[] words = withWSpace.split("\\s+");
        // "\\s+" in regular expression language meaning one or
        // more spaces
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            builder.append(word);
        }
        return builder.toString();
    }

    private List<Classification> getClassificationList(int limit) {
        List<OrderHint> orderHints = new ArrayList<OrderHint>();
        orderHints.add(new OrderHint("titleCache", SortOrder.DESCENDING));
        List<Classification> clist = classificationService.listClassifications(limit, 0, orderHints, VOC_CLASSIFICATION_INIT_STRATEGY);
        return clist;
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
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;

    }
}
