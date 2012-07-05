
package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;

import eu.etaxonomy.cdm.remote.dto.common.ErrorResponse;
import eu.etaxonomy.cdm.remote.dto.common.RemoteResponse;
import eu.etaxonomy.cdm.remote.dto.namecatalogue.NameInformation;
import eu.etaxonomy.cdm.remote.dto.namecatalogue.NameSearch;
import eu.etaxonomy.cdm.remote.dto.namecatalogue.TaxonInformation;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * The controller class for the namespace 'name_catalogue'. This controller
 * provides search mechanisims for searching by taxon name as well as taxon.
 * 
 * @author c.mathew
 * @version 1.0
 * @created 15-Apr-2012
 */

@Controller
@RequestMapping(value = { "/name_catalogue" })
public class NameCatalogueController extends BaseController<TaxonNameBase, INameService> {

        /** Taxon status strings */
        public static final String ACCEPTED_NAME_STATUS = "accepted";
        public static final String SYNONYM_STATUS = "synonym";

        /** Flag strings */
        public static final String DOUBTFUL_FLAG = "doubtful";

        /** Query strings */
        public static final String NAME_SEARCH = "name";
        public static final String TITLE_SEARCH = "title";

        /** Classifcation Key Strings */
        public static final String CLASSIFICATION_DEFAULT = "default";
        public static final String CLASSIFICATION_ALL = "all";

        @Autowired
        private ITaxonService taxonService;

        @Autowired
        private IClassificationService classificationService;

        private static final List<String> NAME_SEARCH_INIT_STRATEGY = Arrays.asList(new String[] {
                "combinationAuthorTeam.$", 
                "exCombinationAuthorTeam.$", 
                "basionymAuthorTeam.$",
                "exBasionymAuthorTeam.$", 
                "nameCache", 
        "taxonBases",
        "taxonBases.synonymRelations.type.$"});

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

        private static final List<String> TAXON_INFORMATION_INIT_STRATEGY = Arrays.asList(new String[] {
                "synonymRelations.type.$", 
                "relationsFromThisTaxon.type.$", 
                "relationsToThisTaxon.type.$",
                "taxonNodes", 
        "taxonNodes.classification" });

        private static final List<String> TAXON_NODE_INIT_STRATEGY = Arrays.asList(new String[] { 
                "taxon.sec",
                "taxon.name", 
                "classification", 
                "classification.reference.$",
        "classification.reference.authorTeam.$" });

        public NameCatalogueController() {
                super();
                setInitializationStrategy(Arrays.asList(new String[] { "$" })); // TODO
                                                                                // still
                                                                                // needed????
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
         * Returns a list of taxon names matching the <code>{query}</code>
         * string pattern. Each of these taxon names is accompanied by a list of
         * name uuids and a list of taxon uuids.
         * <p>
         * URI: <b>&#x002F;{datasource-name}&#x002F;name_catalogue</b>
         * 
         * @param query
         *                The taxon name pattern(s) to query for. The query can
         *                contain wildcard characters ('*'). The query can be
         *                performed with no wildcard or with the wildcard at the
         *                begin and / or end depending on the search pattern.
         * @param type
         *                The type of name to query. This could any of, - name :
         *                scientific name corresponding to 'name cache' in CDM -
         *                title : complete name corresponding to 'title cache'
         *                in CDM
         * @param request
         * @param response
         * @return a List of {@link NameSearch} objects each corresponding to a
         *         single query. These are built from         {@TaxonNameBase
         * 
         * 
         * } entities which are in turn initialized using
         *         the {@link #NAME_SEARCH_INIT_STRATEGY}
         * @throws IOException
         */
        @RequestMapping(value = { "" }, method = RequestMethod.GET)
        public ModelAndView doGetNameSearch(@RequestParam(value = "query", required = true) String[] queries,
                        @RequestParam(value = "type", required = false, defaultValue = NAME_SEARCH) String searchType,
                        HttpServletRequest request, HttpServletResponse response) throws IOException {
                ModelAndView mv = new ModelAndView();
                List<RemoteResponse> nsList = new ArrayList<RemoteResponse>();

                if (!searchType.equals(NAME_SEARCH) && !searchType.equals(TITLE_SEARCH)) {
                        ErrorResponse er = new ErrorResponse();
                        er.setErrorMessage("searchType parameter can only be set as" + NAME_SEARCH + " or "
                                        + TITLE_SEARCH);
                        mv.addObject(er);
                        return mv;
                }

                for (String query : queries) {

                        String queryWOWildcards = getQueryWithoutWildCards(query);
                        MatchMode mm = getMatchModeFromQuery(query);
                        logger.info("doGetNameSearch()" + request.getServletPath() + " for query \"" + query
                                        + "\" without wild cards : " + queryWOWildcards + " and match mode : " + mm);
                        List<NonViralName> nameList = new ArrayList<NonViralName>();
                        if (searchType.equals(NAME_SEARCH)) {
                                nameList = (List<NonViralName>) service.findNamesByNameCache(queryWOWildcards, mm,
                                                NAME_SEARCH_INIT_STRATEGY);
                        }

                        if (searchType.equals(TITLE_SEARCH)) {
                                nameList = (List<NonViralName>) service.findNamesByTitleCache(queryWOWildcards, mm,
                                                NAME_SEARCH_INIT_STRATEGY);
                        }
                        if (nameList == null || !nameList.isEmpty()) {
                                NameSearch ns = new NameSearch();
                                ns.setRequest(query);

                                for (NonViralName nvn : nameList) {
                                        
                                        Set<TaxonBase> tbSet = nvn.getTaxonBases();
                                        Set<TaxonBase> accTbSet = new HashSet<TaxonBase>();
                                        for (TaxonBase tb : tbSet) {
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
         * Returns information related to the taxon name matching the given
         * <code>{nameUuid}</code>. The information includes the name string,
         * relationships, rank, list of related lsids / taxon uuids, etc.
         * <p>
         * URI: <b>&#x002F;{datasource-name}&#x002F;name_catalogue</b>
         * 
         * @param query
         *                The taxon name pattern(s) to query for. The query can
         *                contain wildcard characters ('*'). The query can be
         *                performed with no wildcard or with the wildcard at the
         *                begin and / or end depending on the search pattern.
         * @param request
         * @param response
         * @return a List of {@link NameSearch} objects each corresponding to a
         *         single query. These are built from         {@TaxonNameBase
         * 
         * 
         * } entities which are in turn initialized using
         *         the {@link #NAME_SEARCH_INIT_STRATEGY}
         * @throws IOException
         */
        @RequestMapping(value = { "name" }, method = RequestMethod.GET)
        public ModelAndView doGetNameInformation(@RequestParam(value = "nameUuid", required = true) String[] nameUuids,
                        HttpServletRequest request, HttpServletResponse response) throws IOException {
                ModelAndView mv = new ModelAndView();
                List<RemoteResponse> niList = new ArrayList<RemoteResponse>();
                for (String nameUuid : nameUuids) {
                        logger.info("doGetNameInformation()" + request.getServletPath() + " for name uuid \""
                                        + nameUuid + "\"");
                        NonViralName nvn = (NonViralName) service.findNameByUuid(UUID.fromString(nameUuid),
                                        NAME_INFORMATION_INIT_STRATEGY);
                        if (nvn != null) {
                                NameInformation ni = new NameInformation();
                                ni.setRequest(nameUuid);
                                Reference ref = (Reference) nvn.getNomenclaturalReference();
                                String citation = "";
                                String citation_details = "";
                                if (ref != null) {
                                        citation = ref.getTitleCache();
                                }
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

        @RequestMapping(value = { "taxon" }, method = RequestMethod.GET)
        public ModelAndView doGetTaxonInformation(
                        @RequestParam(value = "taxonUuid", required = true) String[] taxonUuids,
                        @RequestParam(value = "classification", required = false, defaultValue = CLASSIFICATION_DEFAULT) String classificationType,
                        HttpServletRequest request, HttpServletResponse response) throws IOException {
                ModelAndView mv = new ModelAndView();
                List<RemoteResponse> tiList = new ArrayList<RemoteResponse>();
                for (String taxonUuid : taxonUuids) {
                        logger.info("doGetTaxonInformation()" + request.getServletPath() + " for taxon uuid \""
                                        + taxonUuid);
                        TaxonBase tb = taxonService.findTaxonByUuid(UUID.fromString(taxonUuid),
                                        TAXON_INFORMATION_INIT_STRATEGY);

                        if (tb != null) {
                                TaxonInformation ti = new TaxonInformation();
                                ti.setRequest(taxonUuid);

                                if (tb.isInstanceOf(Taxon.class)) {
                                        Taxon taxon = (Taxon) tb;
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
                                        ti.setResponseTaxon(tb.getTitleCache(), ACCEPTED_NAME_STATUS, buildFlagMap(tb),
                                                        classificationMap);
                                        Set<SynonymRelationship> synRelationships = taxon.getSynonymRelations();
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

                                        Set<TaxonRelationship> trFromSet = taxon.getRelationsFromThisTaxon();

                                        for (TaxonRelationship tr : trFromSet) {
                                                // String titleFrom =
                                                // tr.getRelatedFrom().getTitleCache();

                                                String titleTo = tr.getRelatedTo().getTitleCache();
                                                String uuid = tr.getRelatedTo().getUuid().toString();
                                                String status = ACCEPTED_NAME_STATUS;
                                                String relLabel = tr.getType().getRepresentation(Language.DEFAULT())
                                                                .getLabel();
                                                // System.out.println("From : "
                                                // + titleFrom + ", To : " +
                                                // titleTo + "type " +
                                                // tr.getType().getTitleCache());
                                                ti.addToResponseRelatedTaxa(uuid, titleTo, status, "", relLabel);
                                        }

                                        Set<TaxonRelationship> trToSet = taxon.getRelationsToThisTaxon();
                                        for (TaxonRelationship tr : trToSet) {
                                                // String titleTo =
                                                // tr.getRelatedTo().getTitleCache();

                                                String titleFrom = tr.getRelatedFrom().getTitleCache();
                                                String uuid = tr.getRelatedFrom().getUuid().toString();
                                                String status = ACCEPTED_NAME_STATUS;
                                                String relLabel = tr.getType()
                                                                .getInverseRepresentation(Language.DEFAULT())
                                                                .getLabel();
                                                // System.out.println("From : "
                                                // + titleFrom + ", To : " +
                                                // titleFrom + "type " +
                                                // tr.getType().getTitleCache());
                                                ti.addToResponseRelatedTaxa(uuid, titleFrom, status, "", relLabel);
                                        }
                                } else if (tb instanceof Synonym) {
                                        Synonym synonym = (Synonym) tb;
                                        ti.setResponseTaxon(synonym.getTitleCache(), SYNONYM_STATUS,
                                                        buildFlagMap(synonym), null);
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

        private Map<String, String> buildFlagMap(TaxonBase tb) {
                Map<String, String> flags = new Hashtable<String, String>();
                flags.put(DOUBTFUL_FLAG, Boolean.toString(tb.isDoubtful()));
                return flags;
        }

        private Map<String, Map> buildClassificationMap(Taxon taxon, String classificationType) {
                TreeMap<String, Map> sourceClassificationMap = new TreeMap<String, Map>();
                Set<TaxonNode> taxonNodes = taxon.getTaxonNodes();

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
                
                if(classificationType.equals(CLASSIFICATION_DEFAULT) && !sourceClassificationMap.isEmpty()) {
                        return sourceClassificationMap.firstEntry().getValue();
                } else if(sourceClassificationMap.containsKey(classificationType)) {
                        return sourceClassificationMap.get(classificationType);
                } else if(classificationType.equals(CLASSIFICATION_ALL)) {
                        return sourceClassificationMap;
                } else {
                        return null;
                }
        }
}
