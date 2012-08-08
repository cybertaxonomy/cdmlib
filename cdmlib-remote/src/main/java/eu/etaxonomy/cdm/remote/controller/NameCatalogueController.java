
package eu.etaxonomy.cdm.remote.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Hashtable;

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
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.common.DocUtils;

import eu.etaxonomy.cdm.remote.dto.common.ErrorResponse;
import eu.etaxonomy.cdm.remote.dto.common.RemoteResponse;
import eu.etaxonomy.cdm.remote.dto.namecatalogue.NameInformation;
import eu.etaxonomy.cdm.remote.dto.namecatalogue.NameSearch;
import eu.etaxonomy.cdm.remote.dto.namecatalogue.TaxonInformation;
import eu.etaxonomy.cdm.remote.view.HtmlView;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

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
@RequestMapping(value = { "/name_catalogue" })
public class NameCatalogueController extends BaseController<TaxonNameBase, INameService> implements ResourceLoaderAware {

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

    /** Classifcation 'default' key */
    public static final String CLASSIFICATION_DEFAULT = "default";
    
    /** Classifcation 'all' key */
    public static final String CLASSIFICATION_ALL = "all";

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private IClassificationService classificationService;

    /** Hibernate name search initialisation strategy */
    private static final List<String> NAME_SEARCH_INIT_STRATEGY = Arrays.asList(new String[] {
            "combinationAuthorTeam.$", 
            "exCombinationAuthorTeam.$", 
            "basionymAuthorTeam.$",
            "exBasionymAuthorTeam.$", 
            "nameCache", 
            "taxonBases",
    "taxonBases.synonymRelations.type.$"});

    /** Hibernate name information initialisation strategy */
    private static final List<String> NAME_INFORMATION_INIT_STRATEGY = Arrays.asList(new String[] { 
            "taxonBases",
            "status", 
            "nomenclaturalReference.$", 
            "combinationAuthorTeam.$", 
            "exCombinationAuthorTeam.$",
            "basionymAuthorTeam.$", 
            "exBasionymAuthorTeam.$", 
            "relationsToThisName.$",
    "relationsFromThisName.$" });

    /** Hibernate taxon information initialisation strategy */
    private static final List<String> TAXON_INFORMATION_INIT_STRATEGY = Arrays.asList(new String[] {
            "synonymRelations.type.$", 
            "relationsFromThisTaxon.type.$", 
            "relationsToThisTaxon.type.$",
            "taxonNodes", 
    "taxonNodes.classification" });

    /** Hibernate taxon node initialisation strategy */
    private static final List<String> TAXON_NODE_INIT_STRATEGY = Arrays.asList(new String[] { 
            "taxon.sec",
            "taxon.name", 
            "classification", 
            "classification.reference.$",
    "classification.reference.authorTeam.$" });

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
        HtmlView hv = new HtmlView();
        Map<String, String> modelMap = new HashMap<String, String>();
        modelMap.put("title", "Name Search API");
        // Convert Apt to Html
        modelMap.put("body", DocUtils.convertAptToHtml(aptInputStream));
        mv.addAllObjects(modelMap);
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
    @RequestMapping(value = { "" }, method = RequestMethod.GET, params = {"query"})
    public ModelAndView doGetNameSearch(@RequestParam(value = "query", required = true) String[] queries,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
    return doGetNameSearch(queries, DEFAULT_SEARCH_TYPE, request, response);
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
    @RequestMapping(value = { "" }, method = RequestMethod.GET, params = {"query", "type"})
    public ModelAndView doGetNameSearch(@RequestParam(value = "query", required = true) String[] queries,
            @RequestParam(value = "type", required = false, defaultValue = DEFAULT_SEARCH_TYPE) String searchType,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        List<RemoteResponse> nsList = new ArrayList<RemoteResponse>();

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

            String queryWOWildcards = getQueryWithoutWildCards(query);
            MatchMode mm = getMatchModeFromQuery(query);
            logger.info("doGetNameSearch()" + request.getServletPath() + " for query \"" + query
                    + "\" without wild cards : " + queryWOWildcards + " and match mode : " + mm);
            List<NonViralName> nameList = new ArrayList<NonViralName>();
            
            // if "name" search then find by name cache
            if (searchType.equals(NAME_SEARCH)) {
                nameList = (List<NonViralName>) service.findNamesByNameCache(queryWOWildcards, mm,
                        NAME_SEARCH_INIT_STRATEGY);
            }

            //if "title" search then find by title cache
            if (searchType.equals(TITLE_SEARCH)) {
                nameList = (List<NonViralName>) service.findNamesByTitleCache(queryWOWildcards, mm,
                        NAME_SEARCH_INIT_STRATEGY);
            }
            
            // if search is successful then get related information , else return error
            if (nameList == null || !nameList.isEmpty()) {
                NameSearch ns = new NameSearch();
                ns.setRequest(query);

                for (NonViralName nvn : nameList) {
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
                            Set<SynonymRelationship> synRelationships = synonym.getSynonymRelations();
                            for (SynonymRelationship sr : synRelationships) {
                                Taxon accTaxon = sr.getAcceptedTaxon();                                                                
                                accTbSet.add(accTaxon);
                            }
                        } else {
                            accTbSet.add(tb);
                        }
                    }
                    // update name search object
                    ns.addToResponseList(nvn.getTitleCache(), nvn.getNameCache(), nvn.getUuid()
                            .toString(), tbSet, accTbSet);
                }
                nsList.add(ns);

            } else {
                ErrorResponse er = new ErrorResponse();
                er.setErrorMessage("No Taxon Name for given query : " + query);
                nsList.add(er);
            }
        }
        mv.addObject(nsList);
        return mv;
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
        HtmlView hv = new HtmlView();
        Map<String, String> modelMap = new HashMap<String, String>();
        modelMap.put("title", "Name Information API");
        // Convert Apt to Html
        modelMap.put("body", DocUtils.convertAptToHtml(aptInputStream));
        mv.addAllObjects(modelMap);
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
    @RequestMapping(value = { "name" }, method = RequestMethod.GET, params = {"nameUuid"})
    public ModelAndView doGetNameInformation(@RequestParam(value = "nameUuid", required = true) String[] nameUuids,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        List<RemoteResponse> niList = new ArrayList<RemoteResponse>();
        // loop through each name uuid
        for (String nameUuid : nameUuids) {
            logger.info("doGetNameInformation()" + request.getServletPath() + " for name uuid \""
                    + nameUuid + "\"");
            // find name by uuid
            NonViralName nvn = (NonViralName) service.findNameByUuid(UUID.fromString(nameUuid),
                    NAME_INFORMATION_INIT_STRATEGY);
            
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
                re.setErrorMessage("No Taxon Name for given UUID : " + nameUuid);
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
        HtmlView hv = new HtmlView();
        Map<String, String> modelMap = new HashMap<String, String>();
        modelMap.put("title", "Taxon Information API");
        // Convert Apt to Html
        modelMap.put("body", DocUtils.convertAptToHtml(aptInputStream));
        mv.addAllObjects(modelMap);
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
    @RequestMapping(value = { "taxon" }, method = RequestMethod.GET,params = {"taxonUuid"})
    public ModelAndView doGetTaxonInformation(
            @RequestParam(value = "taxonUuid", required = true) String[] taxonUuids,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        return doGetTaxonInformation(taxonUuids,CLASSIFICATION_DEFAULT, request, response);    
    }
    
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
     *                
     * @param request Http servlet request.
     * @param response Http servlet response.
     * @return a List of {@link TaxonInformation} objects each corresponding to a
     *         single query. These are built from {@TaxonBase} entities which are 
     *         in turn initialized using the {@link #TAXON_INFORMATION_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(value = { "taxon" }, method = RequestMethod.GET, params = {"taxonUuid", "classification"})
    public ModelAndView doGetTaxonInformation(
            @RequestParam(value = "taxonUuid", required = true) String[] taxonUuids,
            @RequestParam(value = "classification", required = false, defaultValue = CLASSIFICATION_DEFAULT) String classificationType,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        List<RemoteResponse> tiList = new ArrayList<RemoteResponse>();
        // loop through each name uuid
        for (String taxonUuid : taxonUuids) {
            logger.info("doGetTaxonInformation()" + request.getServletPath() + " for taxon uuid \""
                    + taxonUuid);
            // find name by uuid
            TaxonBase tb = taxonService.findTaxonByUuid(UUID.fromString(taxonUuid),
                    TAXON_INFORMATION_INIT_STRATEGY);
            // if search is successful then get related information, else return error
            if (tb != null) {
                TaxonInformation ti = new TaxonInformation();
                ti.setRequest(taxonUuid);
                // check if result (taxon base) is a taxon or synonym
                if (tb.isInstanceOf(Taxon.class)) {
                    Taxon taxon = (Taxon) tb;
                    // build classification map
                    Map classificationMap = buildClassificationMap(taxon, classificationType);
                    if (classificationMap == null) {
                        ErrorResponse er = new ErrorResponse();
                        er.setErrorMessage("Invalid classification type");
                        mv.addObject(er);
                        return mv;
                    }

                    if (classificationMap.isEmpty()) {
                        ErrorResponse er = new ErrorResponse();
                        er.setErrorMessage("No classification available for requested type");
                        mv.addObject(er);
                        return mv;
                    }
                    // update taxon information object with taxon related data
                    ti.setResponseTaxon(tb.getTitleCache(), ACCEPTED_NAME_STATUS, buildFlagMap(tb),
                            classificationMap);
                    Set<SynonymRelationship> synRelationships = taxon.getSynonymRelations();
                    // add synonyms (if exists) to taxon information object
                    for (SynonymRelationship sr : synRelationships) {
                        Synonym syn = sr.getSynonym();
                        String uuid = syn.getUuid().toString();
                        String title = syn.getTitleCache();
                        String status = SYNONYM_STATUS;
                        String relLabel = sr.getType()
                                .getInverseRepresentation(Language.DEFAULT())
                                .getLabel();
                        ti.addToResponseRelatedTaxa(uuid, title, status, "", relLabel);
                    }

                    // build relationship information as,
                    // - relationships from the requested taxon
                    Set<TaxonRelationship> trFromSet = taxon.getRelationsFromThisTaxon();                   
                    for (TaxonRelationship tr : trFromSet) {
                        String titleTo = tr.getRelatedTo().getTitleCache();
                        String uuid = tr.getRelatedTo().getUuid().toString();
                        String status = ACCEPTED_NAME_STATUS;
                        String relLabel = tr.getType().getRepresentation(Language.DEFAULT())
                                .getLabel();
                        ti.addToResponseRelatedTaxa(uuid, titleTo, status, "", relLabel);
                    }

                    // - relationships from the requested taxon
                    Set<TaxonRelationship> trToSet = taxon.getRelationsToThisTaxon();
                    for (TaxonRelationship tr : trToSet) {
                        String titleFrom = tr.getRelatedFrom().getTitleCache();
                        String uuid = tr.getRelatedFrom().getUuid().toString();
                        String status = ACCEPTED_NAME_STATUS;
                        String relLabel = tr.getType()
                                .getInverseRepresentation(Language.DEFAULT())
                                .getLabel();
                        ti.addToResponseRelatedTaxa(uuid, titleFrom, status, "", relLabel);
                    }
                } else if (tb instanceof Synonym) {
                    Synonym synonym = (Synonym) tb;
                 // update taxon information object with synonym related data
                    ti.setResponseTaxon(synonym.getTitleCache(), SYNONYM_STATUS,
                            buildFlagMap(synonym), null);
                    // add accepted taxa (if exists) to taxon information object
                    Set<SynonymRelationship> synRelationships = synonym.getSynonymRelations();
                    for (SynonymRelationship sr : synRelationships) {
                        Taxon accTaxon = sr.getAcceptedTaxon();
                        String uuid = accTaxon.getUuid().toString();
                        String title = accTaxon.getTitleCache();
                        String status = ACCEPTED_NAME_STATUS;
                        String relLabel = sr.getType().getRepresentation(Language.DEFAULT())
                                .getLabel();
                        ti.addToResponseRelatedTaxa(uuid, title, status, "", relLabel);
                    }
                }
                tiList.add(ti);
            } else {
                ErrorResponse re = new ErrorResponse();
                re.setErrorMessage("No Taxon for given UUID : " + taxonUuid);
                tiList.add(re);
            }
        }
        mv.addObject(tiList);
        return mv;
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
    private Map<String, Map> buildClassificationMap(Taxon taxon, String classificationType) {
        // Using TreeMap is important, because we need the sorting of the classification keys 
        // in the map to be stable.
        TreeMap<String, Map> sourceClassificationMap = new TreeMap<String, Map>();
        Set<TaxonNode> taxonNodes = taxon.getTaxonNodes();
        //loop through taxon nodes and build classification map for each classification key
        for (TaxonNode tn : taxonNodes) {
            Map<String, String> classificationMap = new LinkedHashMap<String, String>();
            List<TaxonNode> tnList = classificationService.loadTreeBranchToTaxon(taxon,
                    tn.getClassification(), null, TAXON_NODE_INIT_STRATEGY);
            for (TaxonNode classificationtn : tnList) {

                classificationMap.put(classificationtn.getTaxon().getName().getRank().getTitleCache(),
                        classificationtn.getTaxon().getName().getTitleCache());
            }

            String cname = tn.getClassification().getTitleCache();
            String[] words = cname.split("\\s+");
            // "\\s+" in regular expression language meaning one or
            // more spaces
            StringBuilder builder = new StringBuilder();
            for (String word : words) {
                builder.append(word);
            }
            cname = builder.toString();
            sourceClassificationMap.put(cname, classificationMap);
        }

        // if classification key is 'default' then return the first element of the map
        if(classificationType.equals(CLASSIFICATION_DEFAULT) && !sourceClassificationMap.isEmpty()) {
            return sourceClassificationMap.firstEntry().getValue();
            // if classification key is provided then return the classification corresponding to the key
        } else if(sourceClassificationMap.containsKey(classificationType)) {
            return sourceClassificationMap.get(classificationType);
            // if classification key is 'all' then return the entire map
        } else if(classificationType.equals(CLASSIFICATION_ALL)) {
            return sourceClassificationMap;
        } else {
            return null;
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;

    }
}
