package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * The controller class for the namespace 'name_catalogue'.
 * This controller provides search mechanisims for searching by taxon name as well as taxon. 
 * 
 * @author c.mathew
 * @version 1.0
 * @created 15-Apr-2012 
 */

@Controller
@RequestMapping(value = {"/name_catalogue"})
public class NameCatalogueController  extends BaseController<TaxonNameBase, INameService> {

	/** Taxon status strings*/
	public static final String ACCECPTED_NAME_STATUS = "accepted_name";
	public static final String SYNONYM_STATUS = "synonym";
	
	/** Flag strings*/
	public static final String DOUBTFUL_FLAG = "doubtful";
	
	@Autowired
	private ITaxonService taxonService;

	@Autowired
	private IClassificationService classificationService;

	private static final List<String> NAME_SEARCH_INIT_STRATEGY = Arrays.asList(new String []{
			"combinationAuthorTeam.$",
			"exCombinationAuthorTeam.$",
			"basionymAuthorTeam.$",
			"exBasionymAuthorTeam.$",
			"taxonBases"
	});

	private static final List<String> NAME_INFORMATION_INIT_STRATEGY = Arrays.asList(new String []{
			"taxonBases",
			"status",
			"nomenclaturalReference.$",
			"combinationAuthorTeam.$",
			"exCombinationAuthorTeam.$",
			"basionymAuthorTeam.$",
			"exBasionymAuthorTeam.$",
			"relationsToThisName.$",
			"relationsFromThisName.$"	
	});

	private static final List<String> TAXON_INFORMATION_INIT_STRATEGY = Arrays.asList(new String []{
			"synonymRelations",	
			"taxonNodes",
			"taxonNodes.classification"
	});

	private static final List<String> TAXON_NODE_INIT_STRATEGY = Arrays.asList(new String[]{
			"taxon.sec", 
			"taxon.name",			
			"classification",
			"classification.reference.$",
			"classification.reference.authorTeam.$"
	});

	public NameCatalogueController(){
		super();
		setInitializationStrategy(Arrays.asList(new String[]{"$"})); //TODO still needed????
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(INameService service) {
		this.service = service;
	}

	/**
	 * Returns a list of taxon names matching the <code>{query}</code> string pattern. 
	 * Each of these taxon names is accompanied by a list of name uuids and
	 * a list of taxon uuids.
	 * <p>
	 * URI: <b>&#x002F;{datasource-name}&#x002F;name_catalogue</b>
	 *
	 * @param query
	 * 			The taxon name pattern(s) to query for. The query can contain wildcard characters ('*'). 
	 *  		The query can be performed with no wildcard or with the wildcard at the begin and / or end 
	 *  		depending on the search pattern.
	 * @param request
	 * @param response
	 * @return a List of {@link NameSearch} objects each corresponding to a single query. These are built from 
	 * 			{@TaxonNameBase} entities which are in turn initialized using the {@link #NAME_SEARCH_INIT_STRATEGY}
	 * @throws IOException
	 */
	@RequestMapping(value = {""},
			method = RequestMethod.GET)
	public ModelAndView doGetNameSearch(@RequestParam(value = "query", required = true) String[] queries,
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		ModelAndView mv = new ModelAndView();
		List <RemoteResponse> nsList = new ArrayList<RemoteResponse>();
		for(String query : queries ) {

			String queryWOWildcards = getQueryWithoutWildCards(query);
			MatchMode mm = getMatchModeFromQuery(query);
			logger.info("doGetNameSearch()" + request.getServletPath() + " for query \"" + query + "\" without wild cards : " + queryWOWildcards + " and match mode : " + mm);
			List<NonViralName> nameList = (List<NonViralName>)service.findNamesByTitleCache(queryWOWildcards, mm, NAME_SEARCH_INIT_STRATEGY);
			if(nameList == null || !nameList.isEmpty()) {
				NameSearch ns = new NameSearch();	
				ns.setRequest(query);		

				for (NonViralName nvn : nameList)
				{
					String titleCacheString = nvn.getTitleCache();	    
					ns.addToResponseList(titleCacheString, nvn.getUuid().toString(), nvn.getTaxonBases());
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
	 * Returns information related to the taxon name matching the given <code>{nameUuid}</code>. 
	 * The information includes the name string, relationships, rank, list of related lsids / taxon uuids, etc. 
	 * <p>
	 * URI: <b>&#x002F;{datasource-name}&#x002F;name_catalogue</b>
	 *
	 * @param query
	 * 			The taxon name pattern(s) to query for. The query can contain wildcard characters ('*'). 
	 *  		The query can be performed with no wildcard or with the wildcard at the begin and / or end 
	 *  		depending on the search pattern.
	 * @param request
	 * @param response
	 * @return a List of {@link NameSearch} objects each corresponding to a single query. These are built from 
	 * 			{@TaxonNameBase} entities which are in turn initialized using the {@link #NAME_SEARCH_INIT_STRATEGY}
	 * @throws IOException
	 */
	@RequestMapping(value = {"name"},
			method = RequestMethod.GET)
	public ModelAndView doGetNameInformation(@RequestParam(value = "nameUuid", required = true) String[] nameUuids,
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		ModelAndView mv = new ModelAndView();
		List <RemoteResponse> niList = new ArrayList<RemoteResponse>();
		for(String nameUuid : nameUuids ) {
			logger.info("doGetNameInformation()" + request.getServletPath() + " for name uuid \"" + nameUuid + "\"");
			NonViralName nvn = (NonViralName)service.findNameByUuid(UUID.fromString(nameUuid), NAME_INFORMATION_INIT_STRATEGY);
			if(nvn != null) {				
				NameInformation ni = new NameInformation();
				ni.setRequest(nameUuid);
				Reference ref = (Reference) nvn.getNomenclaturalReference();
				String citation = "";
				String citation_details = "";
				if(ref != null) {
					citation = ref.getTitleCache();
				}
				ni.setResponse(nvn.getTitleCache(), 
						nvn.getRank().getTitleCache(), 
						nvn.getStatus(), 
						citation,
						nvn.getRelationsFromThisName(),
						nvn.getRelationsToThisName(),
						nvn.getTaxonBases());
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

	@RequestMapping(value = {"taxon"},
			method = RequestMethod.GET)
	public ModelAndView doGetTaxonInformation(@RequestParam(value = "taxonUuid", required = true) String[] taxonUuids,
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		ModelAndView mv = new ModelAndView();
		List <RemoteResponse> tiList = new ArrayList<RemoteResponse>();
		for(String taxonUuid : taxonUuids ) {
			logger.info("doGetTaxonInformation()" + request.getServletPath() + " for taxon uuid \"" + taxonUuid);
			TaxonBase tb= taxonService.findTaxonByUuid(UUID.fromString(taxonUuid), TAXON_INFORMATION_INIT_STRATEGY);
			if(tb != null) {
				TaxonInformation ti = new TaxonInformation();
				ti.setRequest(taxonUuid);

				if(tb.isInstanceOf(Taxon.class)) {
					Taxon taxon = (Taxon)tb;
					ti.setResponseTaxon(tb.getTitleCache(), 
							ACCECPTED_NAME_STATUS, 
							buildFlagMap(tb),
							buildClassificationMap(taxon));					
					Set<Synonym> synonyms = taxon.getSynonyms();
					for(Synonym syn: synonyms) {
						String uuid = syn.getUuid().toString();
						String name = syn.getTitleCache();
						String status = SYNONYM_STATUS; 
						ti.addToResponseRelatedTaxa(taxonUuid, name, status, "");
					}
				} else if(tb instanceof Synonym) {
					Synonym synonym = (Synonym)tb;
					ti.setResponseTaxon(tb.getTitleCache(), 
							SYNONYM_STATUS, 
							buildFlagMap(tb),
							null);
					Set<Taxon> acceptedTaxa = synonym.getAcceptedTaxa();
					for(Taxon taxon : acceptedTaxa) {
						String uuid = taxon.getUuid().toString();
						String name = taxon.getTitleCache();
						String status = ACCECPTED_NAME_STATUS;
						ti.addToResponseRelatedTaxa(taxonUuid, name, status, "");
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
		if(query.startsWith("*") && query.endsWith("*")) {
			return MatchMode.ANYWHERE;
		} else if(query.startsWith("*")) {			
			return MatchMode.END;
		} else if(query.endsWith("*")) {
			return MatchMode.BEGINNING;
		} else {
			return MatchMode.EXACT;
		}
	}

	private String getQueryWithoutWildCards(String query) {

		String newQuery = query;
		
		if(query.startsWith("*")) {			
			newQuery = newQuery.substring(1, newQuery.length());			
		}

		if(query.endsWith("*")) {
			newQuery = newQuery.substring(0, newQuery.length()-1);
		}
		
		return newQuery.trim();
	}

	private Map<String, String> buildFlagMap(TaxonBase tb) {
		Map<String, String> flags = new Hashtable<String, String>();		
		flags.put(DOUBTFUL_FLAG, Boolean.toString(tb.isDoubtful()));		
		return flags;
	}

	private Map<String, Map> buildClassificationMap(Taxon taxon) {
		Map<String, Map> classificationMap = new Hashtable<String, Map>();
		Set<TaxonNode> taxonNodes = taxon.getTaxonNodes();

		for(TaxonNode tn : taxonNodes) {
			Map<String, String> classification = new LinkedHashMap<String, String>();
			List<TaxonNode> tnList = classificationService.loadTreeBranchToTaxon(taxon, tn.getClassification(), null, TAXON_NODE_INIT_STRATEGY);
			for(TaxonNode classificationtn : tnList) {

				classification.put(classificationtn.getTaxon().getName().getRank().getTitleCache(), 
						classificationtn.getTaxon().getName().getTitleCache());
			}

			String cname = tn.getClassification().getTitleCache();
			String [] words = cname.split("\\s+");
			//"\\s+" in regular expression language meaning one or more spaces
			StringBuilder builder = new StringBuilder();	 
			for (String word : words){
				builder.append(word);
			}	      	
			cname = builder.toString();		
			classificationMap.put(cname, classification);
		}
		return classificationMap;
	}


}
