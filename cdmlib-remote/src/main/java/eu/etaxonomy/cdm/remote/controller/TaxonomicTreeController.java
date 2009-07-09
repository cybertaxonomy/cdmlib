// $Id$
/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.remote.editor.RankPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.kohlbecker
 * @date 20.03.2009
 */
@Controller
public class TaxonomicTreeController extends AbstractListController<TaxonBase, ITaxonService> {
	
	
	private static final List<String> TAXONTREE_INIT_STRATEGY = Arrays.asList(new String[]{
			"reference.authorTeam.titleCache"
	});
	
	private static final List<String> NODE_INIT_STRATEGY = Arrays.asList(new String[]{
			"taxon.sec", 
			"taxon.name.taggedName",
			});
	
	
	//TODO get rid of the bloodyRankLabelMap ------ can be deleted once the FIXME in getPathToRoot is solved
	private static Hashtable<String, String> bloodyRankLabelMap = new Hashtable<String, String>();	
	static{
		bloodyRankLabelMap.put("Subfamily", "Subfamilia");
		bloodyRankLabelMap.put("Family", "Familia");
		bloodyRankLabelMap.put("Suborder", "Subordo");
		bloodyRankLabelMap.put("Order", "Ordo");
		
	}
	// --------------------------------------------

	public static final Logger logger = Logger.getLogger(TaxonomicTreeController.class);

	private ITaxonService service;
	
	
	private ITermService termService;
	
	private IReferenceService referenceService;

	
	private Pattern parameterPattern = Pattern.compile("^/(?:[^/]+)/taxontree/([^?#&\\.]+).*");

	@Autowired
	public void setService(ITaxonService service) {
		this.service = service; 
	}
	
	@Autowired
	public void setTermService(ITermService termService) {
		this.termService = termService;
	}
	
	@Autowired
	public void setReferenceService(IReferenceService referenceService) {
		this.referenceService = referenceService;
	}
	
	@InitBinder
    public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
		binder.registerCustomEditor(Rank.class, new RankPropertyEditor());
	}
	
	/**
	 * @param uuid
	 * @param rank
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException 
	 */
//	@RequestMapping(
//			value = {"/*/taxontree/"},
//			params = {"uuid"},
//			method = RequestMethod.GET)
//	public String findTaxon(
//			@RequestParam(value = "uuid", required = true) UUID uuid,
//			@RequestParam(value = "rankUuid", required = false) UUID rankUuid,
//			@RequestParam(value = "viewUuid", required = false) UUID viewUuid,
//			HttpServletRequest request, HttpServletResponse response) throws IOException {
//		
//		String msg404 = rank != null ? "Taxon not found within rank "+ rank.getLabel() : "Taxon not found.";
//		
//		TaxonBase tb = service.load(uuid, TAXON_INIT_STRATEGY);
//				
//		if(tb != null && Taxon.class.isAssignableFrom(tb.getClass())){			
//			Taxon t = (Taxon)tb;
//			String relPath = "";
//			String basePath = FilenameUtils.removeExtension(request.getServletPath()); 
//			basePath += "/" + t.getSec().getUuid().toString();
//			if(rank != null){
//				basePath += "," + rank.getLabel();
//			}
//			
//			// compose path of parent uuids
//			Taxon taxon = t;
//			while( taxon != null && (rank == null || taxon.getName().getRank() == null || taxon.getName().getRank().compareTo(rank) <= 0) ) {
//				relPath = "/" + taxon.getUuid().toString() + relPath;
//				taxon  = taxon.getTaxonomicParent();
//				if(taxon != null){
//					taxon = (Taxon)service.load(taxon.getUuid(), TAXON_INIT_STRATEGY);
//				}
//			};
//			
//			if(relPath.length() > 0){
//				URI redirectUri;
//				try {
//					redirectUri = relativeToFullUri(request, basePath + relPath);
//					if(logger.isInfoEnabled()){
//						logger.info("redirecting to " + redirectUri);
//					}
//					response.sendRedirect(redirectUri.toString());
//					return "";
//				} catch (URISyntaxException e) {
//					logger.error(e.getMessage(), e);
//				}
//			}
//		} else {
//			msg404 = "The taxon is not accepted";
//		}
//		response.sendError(HttpServletResponse.SC_NOT_FOUND, msg404);
//		return "";
//	}
	
	
	
	@RequestMapping(value = { "/*/taxontree" }, method = RequestMethod.GET)
	public List<TaxonomicTree> getTaxonomicTrees(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		logger.info("getTaxonomicTrees()");
		return service.listTaxonomicTrees(null, null, null, TAXONTREE_INIT_STRATEGY);
	}
	
	
	/**
	 * &#x002F;*&#x002F;taxontree&#x002F;{viewUuid},{rankUuid}&#x002F;
	 * @param request
	 * @return
	 */
	@RequestMapping(
			value = {"/*/taxontree/?*"},
			method = RequestMethod.GET)
	public List<TaxonNode> getRootTaxa(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("getRootTaxa()");
		List<String> uriParams = readUriParameters(request);
		TaxonomicTree tree = null;
		Rank rank = null;
		if(uriParams.size() == 1){
			// get view and rank
			tree = readTreeByUuid(uriParams.get(0));
			rank = readRankByUuid(uriParams.get(0));
			
			if(tree == null) {
				response.sendError(404 , "TaxonomicTree not found using " + stringToUuid(uriParams.get(0)) );
				return null;
			}
		}
		if(uriParams.size() > 1){
			response.sendError(400, "A maximum of two uuid parameter expected but found  " + uriParams.size());
			return null;
		}
		return service.loadRankSpecificRootNodes(tree, rank, NODE_INIT_STRATEGY);
	}


	/**
	 * @param request
	 * @return
	 * @throws IOException 
	 */
	@RequestMapping(
			value = {"/*/taxontree/*/?*", "/*/taxontree/*/**/?*"}, 
			method = RequestMethod.GET)
	public List<TaxonNode> getChildTaxa(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("getChildTaxa()");
		List<String> uriParams = readUriParameters(request);
		if(uriParams.size() <= 1){
			response.sendError(400, "At least two uuid parameters expected but found " + uriParams.size());
			return null;
		}
		
		TaxonomicTree tree = readTreeByUuid(uriParams.get(0));
		if(tree == null){
			response.sendError(500, "The specified instance identified by " + uriParams.get(0) + " is not a taxonomicTree");
			return null;
		}
		Rank rank = readRankByUuid(uriParams.get(0));
		//TODO rank is being ignored
		try {
			UUID uuid = stringToUuid(uriParams.get(uriParams.size() - 1));
			Taxon taxon = (Taxon) service.load(uuid);
			List<TaxonNode> childs = service.loadChildNodesOfTaxon(taxon, tree, NODE_INIT_STRATEGY);
			return childs;
		} catch (ClassCastException cce) {
			response.sendError(500, "The specified instance is not a taxon");
			return null;
		}
	}
	
	/**
	 * @param request
	 * @return
	 * @throws IOException 
	 */
	@RequestMapping(
			value = {"/*/taxontree/*/*/path", "/*/taxontree/*/**/*/path"}, 
			method = RequestMethod.GET)
	public List<TaxonNode> getPathToRoot(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("getPathToRoot()");
		List<Taxon> pathToRoot = new ArrayList<Taxon>();
		List<String> uriParams = readUriParameters(request);
		if(uriParams.size() <= 1){
			response.sendError(400, "At least two uuid parameters expected but found " + uriParams.size());
			return null;
		}
		
		TaxonomicTree tree = readTreeByUuid(uriParams.get(0));
		Rank rank = readRankByUuid(uriParams.get(0));
		UUID taxonUuid = stringToUuid(uriParams.get(uriParams.size() - 2));
		Taxon taxon = (Taxon) service.load(taxonUuid);

		return service.loadTreeBranchToTaxon(taxon, tree, rank, NODE_INIT_STRATEGY);
	}
	
	/**
	 * reads  <code>{secuuid},{rank label}/..</code> from <code>/{database key}/taxonomy/{secuuid},{rank label}/..<code>
	 * @param request
	 * @return
	 */
	protected List<String> readUriParameters(HttpServletRequest request) {
		
		List<String> parameters = null;
		String path = request.getServletPath();
		if(path != null) {
			Matcher uuidMatcher = parameterPattern .matcher(path);
			if(uuidMatcher.matches() && uuidMatcher.groupCount() > 0){
				String[] pa = uuidMatcher.group(1).split("/");
				parameters = Arrays.asList(pa);
			}
		}
		return parameters;
	}
	
	/**
	 * @param string
	 * @return
	 */
	private Rank readRankByLabel(String paramStr) throws IllegalArgumentException{
		int pos;
		if((pos = paramStr.indexOf(',')) > 0){
			String rankLabel = paramStr.substring(pos + 1);
			try {
				return Rank.getRankByName(rankLabel);
			} catch (UnknownCdmTypeException e) {
				throw new IllegalArgumentException("400Not a valid rank name");
			}
		}
		return null;
	}
	
	private Rank readRankByUuid(String paramStr) throws IllegalArgumentException{
		int pos;
		if((pos = paramStr.indexOf(',')) > 0){
			String uuidStr = paramStr.substring(pos + 1);
			UUID uuid = UUID.fromString(uuidStr);
			DefinedTermBase dt =  termService.findByUuid(uuid);
			if(dt instanceof Rank){
				return (Rank)dt;
			} else {
			   new IllegalArgumentException("Term is not a Rank");
			}
		}
		return null;
	}

	/**
	 * @param paramStr
	 * @return
	 */
	private TaxonomicTree readTreeByUuid(String paramStr) {
		UUID viewUuid; 
		int pos;
		if((pos = paramStr.indexOf(',')) > 0){
			
			viewUuid = stringToUuid(paramStr.substring(0, pos));
		} else {
			viewUuid = stringToUuid(paramStr);
		}
		return service.getTaxonomicTreeByUuid(viewUuid);			
	}

	/**
	 * @param uuidStr
	 * @return
	 */
	private UUID stringToUuid(String uuidStr) {
		
		try {
			UUID uuid = UUID.fromString(uuidStr);
			return uuid;
		} catch (Exception e) {
			throw new IllegalArgumentException(uuidStr + " is not a uuid");
		}
	}
	
	private URI relativeToFullUri(HttpServletRequest request,
			String relativePath) throws URISyntaxException {
			return new URI(
					request.getScheme(), 
					null, //user info
					request.getServerName(), 
					request.getServerPort(),  
					relativePath, 
					null,
					null); 
	}
	
}