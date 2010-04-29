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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.api.service.ITaxonTreeService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.remote.editor.RankPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;

/**
 * @author n.hoffmann
 * @created Apr 8, 2010
 * @version 1.0
 */
@Controller
public class ClassificationListController extends BaseListController<TaxonomicTree,ITaxonTreeService> {
	private static final Logger logger = Logger
			.getLogger(ClassificationListController.class);

	private static final List<String> CLASSIFICATION_INIT_STRATEGY = Arrays.asList(new String[]{
			"reference.authorTeam.titleCache"
	});
	
	private List<String> NODE_INIT_STRATEGY(){
		return Arrays.asList(new String[]{
			"taxon.sec", 
			"taxon.name.taggedName",
//			"taxon.name.combinationAuthorTeam.*",
//			"taxon.name.exCombinationAuthorTeam.*",
//			"taxon.name.basionymAuthorTeam.*",
//			"taxon.name.exBasionymAuthorTeam.*",
			"taxon.name.titleCache",
			"taxonomicTree"
	});}
	
	private ITaxonTreeService service;
	
	private ITermService termService;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.AbstractListController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Override
	@Autowired
	public void setService(ITaxonTreeService service) {
		this.service = service;
	}
	
	@Autowired
	public void setTermService(ITermService termService) {
		this.termService = termService;
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
	@RequestMapping(value = { "/classification/" }, method = RequestMethod.GET)
	public List<TaxonomicTree> getClassifications(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		logger.info("getClassifications()");
		return service.list(null, null, null,null, CLASSIFICATION_INIT_STRATEGY);
	}
	
	/**
	 * @param treeUuid
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(
			value = {"/classification/{classificationUuid}/childNodes/"},
			method = RequestMethod.GET)
	public List<TaxonNode> getChildNodes(
			@PathVariable("classificationUuid") UUID classificationUuid,
			HttpServletResponse response
			) throws IOException {
		
		return getChildNodesAtRank(classificationUuid, null, response);
	}
	
	@RequestMapping(
			value = {"/classification/{classificationUuid}/childNodesAt/{rankUuid}/"},
			method = RequestMethod.GET)
	public List<TaxonNode> getChildNodesAtRank(
			@PathVariable("classificationUuid") UUID classificationUuid,
			@PathVariable("rankUuid") UUID rankUuid,
			HttpServletResponse response
			) throws IOException {
		
		logger.info("getChildNodesAtRank()");
		TaxonomicTree tree = null;
		Rank rank = null;
		if(classificationUuid != null){
			// get view and rank
			tree = service.find(classificationUuid);
			
			if(tree == null) {
				response.sendError(404 , "Classification not found using " + classificationUuid );
				return null;
			}
		}
		rank = findRank(rankUuid);
		
		return service.loadRankSpecificRootNodes(tree, rank, NODE_INIT_STRATEGY());
	}
	
	private Rank findRank(UUID rankUuid) {
		Rank rank = null;
		if(rankUuid != null){
			DefinedTermBase definedTermBase =  termService.find(rankUuid);
			if(definedTermBase instanceof Rank){
				rank = (Rank) definedTermBase;
			} else {
			   new IllegalArgumentException("DefinedTermBase is not a Rank");
			}
		}
		return rank;
	}
	
	
}
