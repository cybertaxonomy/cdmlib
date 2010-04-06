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
import java.util.Arrays;
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

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITaxonTreeService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.database.UpdatableRoutingDataSource;
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
 * The TaxonomicTreeController class is a Spring MVC Controller.
 * <p>
 * The syntax of the mapped service URIs contains the the {datasource-name} path element.
 * The available {datasource-name}s are defined in a configuration file which
 * is loaded by the {@link UpdatableRoutingDataSource}. If the
 * UpdatableRoutingDataSource is not being used in the actual application
 * context any arbitrary {datasource-name} may be used.
 * <p>
 * @author a.kohlbecker
 * @date 20.03.2009
 * 
 * TODO this controller should be a portal controller!!
 */
@Controller
public class TaxonomicTreeController extends AbstractListController<TaxonomicTree,ITaxonTreeService> {
	
	
	private static final List<String> TAXONTREE_INIT_STRATEGY = Arrays.asList(new String[]{
			"reference.authorTeam.titleCache"
	});
	
	private static final List<String> NODE_INIT_STRATEGY = Arrays.asList(new String[]{
			"taxon.sec", 
			"taxon.name.taggedName",
			"taxon.name.titleCache"
			});
	

	public static final Logger logger = Logger.getLogger(TaxonomicTreeController.class);

	private ITaxonService taxonService;
	
	private ITaxonTreeService service;
	
	private ITermService termService;
	
	private Pattern parameterPattern = Pattern.compile("^/portal/taxontree/([^?#&\\.]+).*");

	@Autowired
	public void setService(ITaxonTreeService service) {
		this.service = service; 
	}
	
	@Autowired
	public void setTermService(ITermService termService) {
		this.termService = termService;
	}
	
	@Autowired
	public void setTaxonService(ITaxonService taxonService) {
		this.taxonService = taxonService;
	}

	
	@InitBinder
    public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
		binder.registerCustomEditor(Rank.class, new RankPropertyEditor());
	}
	
	
	/**
	 * Lists all available {@link TaxonomicTree}s.
	 * <p>
	 * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxontree</b>
	 * 
	 * @param request
	 * @param response
	 * @return a list of {@link TaxonomicTree}s initialized by
	 *         the {@link #TAXONTREE_INIT_STRATEGY}
	 * @throws IOException
	 */
	@RequestMapping(value = { "/portal/taxontree" }, method = RequestMethod.GET)
	public List<TaxonomicTree> getTaxonomicTrees(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		logger.info("getTaxonomicTrees()");
		return service.list(null, null, null,null, TAXONTREE_INIT_STRATEGY);
	}
	
	
	/**
	 * Lists all {@link TaxonNode}s of the specified {@link TaxonomicTree} for
	 * a given {@link Rank}. If a branch does not contain a TaxonNode with a TaxonName
	 * at the given Rank the node associated with the next lower Rank is taken
	 * as root node. If the rank is null the absolute root nodes will be
	 * returned.
	 * <p>
	 * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxontree&#x002F;{tree-uuid},{rank-uuid}</b>
	 * <p>
     * <b>URI elements:</b>
     * <ul>
     * <li><b>{tree-uuid}</b> identifies the {@link TaxonomicTree} by its UUID.
     * <li><b>{rank-uuid}</b> identifies the {@link Rank} by its UUID. May be left out.
     * </ul>
	 * 
	 * @param response
	 * @param request
	 * @return a List of {@link TaxonNode} entities initialized by
	 *         the {@link #NODE_INIT_STRATEGY}
	 */
	@RequestMapping(
			value = {"/portal/taxontree/?*"},
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
	 * Lists all child-{@link TaxonNode}s of the specified {@link Taxon} in the {@link TaxonomicTree}. The
	 * a given {@link Rank} is ignored in this method but for consistency reasons it has been allowed to included it into the URI. 
	 * <p>
	 * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxontree&#x002F;{tree-uuid},{rank-uuid}&#x002F;{taxon-uuid}</b>
	 * <p>
     * <b>URI elements:</b>
     * <ul>
     * <li><b>{tree-uuid}</b> identifies the {@link TaxonomicTree} by its UUID - <i>required</i>.
     * <li><b>{rank-uuid}</b> identifies the {@link Rank} but is is ignored.
     * <li><b>{taxon-uuid}</b> identifies the {@link Taxon} by its UUID. - <i>required</i>.
     * </ul>
	 * 
	 * @param response
	 * @param request
	 * @return a List of {@link TaxonNode} entities initialized by
	 *         the {@link #NODE_INIT_STRATEGY}
	 */
	@RequestMapping(
			value = {"/portal/taxontree/*/?*", "/portal/taxontree/*/**/?*"}, 
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
			Taxon taxon = (Taxon) taxonService.load(uuid);
			List<TaxonNode> childs = service.loadChildNodesOfTaxon(taxon, tree, NODE_INIT_STRATEGY);
			return childs;
		} catch (ClassCastException cce) {
			response.sendError(500, "The specified instance is not a taxon");
			return null;
		}
	}
	
	/**
	 * Provides path of {@link TaxonNode}s from the base node to the node of the specified taxon.
	 * <p>
	 * URI:<b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxontree&#x002F;{tree-uuid},{rank-uuid}&#x002F;{taxon-uuid}&#x002F;path</b>
	 * <p>
     * <b>URI elements:</b>
     * <ul>
     * <li><b>{tree-uuid}</b> identifies the {@link TaxonomicTree} by its UUID - <i>required</i>.
     * <li><b>{rank-uuid}</b> identifies the {@link Rank} but is is ignored.
     * <li><b>{taxon-uuid}</b> identifies the {@link Taxon} by its UUID. - <i>required</i>.
     * </ul>
	 * 
	 * @param response
	 * @param request
	 * @return a List of {@link TaxonNode} entities initialized by
	 *         the {@link #NODE_INIT_STRATEGY}
	 */
	@RequestMapping(
			value = {"/portal/taxontree/*/*/path", "/portal/taxontree/*/**/*/path"}, 
			method = RequestMethod.GET)
	public List<TaxonNode> getPathToRoot(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("getPathToRoot()");

		List<String> uriParams = readUriParameters(request);
		if(uriParams.size() <= 1){
			response.sendError(400, "At least two uuid parameters expected but found " + uriParams.size());
			return null;
		}
		
		TaxonomicTree tree = readTreeByUuid(uriParams.get(0));
		Rank rank = readRankByUuid(uriParams.get(0));
		UUID taxonUuid = stringToUuid(uriParams.get(uriParams.size() - 2));
		Taxon taxon = (Taxon) taxonService.load(taxonUuid);
		return service.loadTreeBranchToTaxon(taxon, tree, rank, NODE_INIT_STRATEGY);
	}
	
	/**
	 * reads  <code>{secuuid},{rank label}/..</code> from <code>/{database key}/taxonomy/{secuuid},{rank label}/..<code>
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
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
	 * @param paramStr
	 * @return
	 * @throws IllegalArgumentException
	 */
	protected Rank readRankByLabel(String paramStr) throws IllegalArgumentException{
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
	
	/**
	 * @param paramStr
	 * @return
	 * @throws IllegalArgumentException
	 */
	private Rank readRankByUuid(String paramStr) throws IllegalArgumentException{
		int pos;
		if((pos = paramStr.indexOf(',')) > 0){
			String uuidStr = paramStr.substring(pos + 1);
			UUID uuid = UUID.fromString(uuidStr);
			DefinedTermBase dt =  termService.find(uuid);
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
	
	/**
	 * @param request
	 * @param relativePath
	 * @return
	 * @throws URISyntaxException
	 */
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