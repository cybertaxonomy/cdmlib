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

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.remote.editor.RankPropertyEditor;

/**
 * The ClassificationController class is a Spring MVC Controller.
 * @author a.kohlbecker
 * @date 20.03.2009
 */
@Controller
public class ClassificationPortalListController extends IdentifiableListController<Classification,IClassificationService> {
	
	
	private static final List<String> CLASSIFICATION_INIT_STRATEGY = Arrays.asList(new String[]{
			"reference.authorTeam"
	});
	
	private static final List<String> NODE_INIT_STRATEGY = Arrays.asList(new String[]{
			"taxon.sec", 
			"taxon.name"
			});
	

	public static final Logger logger = Logger.getLogger(ClassificationPortalListController.class);

	private ITaxonService taxonService;
	
	private IClassificationService service;
	
	private ITermService termService;
	
	@Autowired
	public void setService(IClassificationService service) {
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
	@Override
    public void initBinder(WebDataBinder binder) {
		super.initBinder(binder);
		binder.registerCustomEditor(Rank.class, new RankPropertyEditor());
	}
	
	
	/**
	 * Lists all available {@link Classification}s.
	 * <p>
	 * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;classification</b>
	 * 
	 * @param request
	 * @param response
	 * @return a list of {@link Classification}s initialized by
	 *         the {@link #CLASSIFICATION_INIT_STRATEGY}
	 * @throws IOException
	 */
	@RequestMapping(value = { "/portal/classification" }, method = RequestMethod.GET)
	public List<Classification> getClassifications(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		logger.info("getClassification() " + request.getServletPath());
		return service.list(null, null, null,null, CLASSIFICATION_INIT_STRATEGY);
	}
	
	
	/**
	 * @param treeUuid
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(
			value = {"/portal/classification/{treeUuid}/childNodes"},
			method = RequestMethod.GET)
	public List<TaxonNode> getChildNodes(
			@PathVariable("treeUuid") UUID treeUuid,
			HttpServletRequest request,
			HttpServletResponse response
			) throws IOException {
		
		return getChildNodesAtRank(treeUuid, null, request, response);
	}
	
	
	@RequestMapping(
			value = {"/portal/classification/{treeUuid}/childNodesAt/{rankUuid}"},
			method = RequestMethod.GET)
	public List<TaxonNode> getChildNodesAtRank(
			@PathVariable("treeUuid") UUID treeUuid,
			@PathVariable("rankUuid") UUID rankUuid,
			HttpServletRequest request,
			HttpServletResponse response
			) throws IOException {
		
		logger.info("getChildNodesAtRank() " + request.getServletPath());
		Classification tree = null;
		Rank rank = null;
		if(treeUuid != null){
			// get view and rank
			tree = service.find(treeUuid);
			
			if(tree == null) {
				response.sendError(404 , "Classification not found using " + treeUuid );
				return null;
			}
		}
		rank = findRank(rankUuid);
		
		return service.loadRankSpecificRootNodes(tree, rank, NODE_INIT_STRATEGY);
	}

	


	/**
	 * Lists all child-{@link TaxonNode}s of the specified {@link Taxon} in the {@link Classification}. The
	 * a given {@link Rank} is ignored in this method but for consistency reasons it has been allowed to included it into the URI. 
	 * <p>
	 * URI: <b>&#x002F;portal&#x002F;classification&#x002F;{treeUuid}&#x002F;childNodesOf&#x002F;{taxonUuid}</b>
	 * <p>
     * <b>URI elements:</b>
     * <ul>
     * <li><b>{tree-uuid}</b> identifies the {@link Classification} by its UUID - <i>required</i>.
     * <li><b>{taxon-uuid}</b> identifies the {@link Taxon} by its UUID. - <i>required</i>.
     * </ul>
	 * 
	 * @param response
	 * @param request
	 * @return a List of {@link TaxonNode} entities initialized by
	 *         the {@link #NODE_INIT_STRATEGY}
	 */
	@RequestMapping(
			value = {"/portal/classification/{treeUuid}/childNodesOf/{taxonUuid}"}, 
			method = RequestMethod.GET)
	public List<TaxonNode> getChildNodesOfTaxon(
			@PathVariable("treeUuid") UUID treeUuid,
			@PathVariable("taxonUuid") UUID taxonUuid,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		logger.info("getChildNodesOfTaxon() " + request.getServletPath());
		
		Classification tree = service.find(treeUuid);
		Taxon taxon = (Taxon) taxonService.load(taxonUuid);
		List<TaxonNode> childs = service.loadChildNodesOfTaxon(taxon, tree, NODE_INIT_STRATEGY);
		return childs;
		
	}
	
	/**
	 * Provides path of {@link TaxonNode}s from the base node to the node of the specified taxon.
	 * <p>
	 * URI:<b>&#x002F;portal&#x002F;classification&#x002F;{treeUuid}&#x002F;pathFrom&#x002F;{taxonUuid}&#x002F;toRank&#x002F;{rankUuid}</b>
	 * <p>
     * <b>URI elements:</b>
     * <ul>
     * <li><b>{treeUuid}</b> identifies the {@link Classification} by its UUID - <i>required</i>.
     * <li><b>{taxonUuid}</b> identifies the {@link Rank}
     * <li><b>{rankUuid}</b> identifies the {@link Taxon} by its UUID. - <i>required</i>.
     * </ul>
	 * 
	 * @param response
	 * @param request
	 * @return a List of {@link TaxonNode} entities initialized by
	 *         the {@link #NODE_INIT_STRATEGY}
	 */
	@RequestMapping(
			value = {"/portal/classification/{treeUuid}/pathFrom/{taxonUuid}/toRank/{rankUuid}"}, 
			method = RequestMethod.GET)
	public List<TaxonNode> getPathFromTaxonToRank(
			@PathVariable("treeUuid") UUID treeUuid,
			@PathVariable("taxonUuid") UUID taxonUuid,
			@PathVariable("rankUuid") UUID rankUuid,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		logger.info("getPathFromTaxonToRank() " + request.getServletPath());
		
		Classification tree = service.find(treeUuid);
		Rank rank = findRank(rankUuid);
		Taxon taxon = (Taxon) taxonService.load(taxonUuid);
		
		return service.loadTreeBranchToTaxon(taxon, tree, rank, NODE_INIT_STRATEGY);
	}
	
	/**
	 * Provides path of {@link TaxonNode}s from the base node to the node of the specified taxon.
	 * <p>
	 * URI:<b>&#x002F;portal&#x002F;classification&#x002F;{treeUuid}&#x002F;pathFrom&#x002F;{taxonUuid}</b>
	 * <p>
     * <b>URI elements:</b>
     * <ul>
     * <li><b>{treeUuid}</b> identifies the {@link Classification} by its UUID - <i>required</i>.
     * <li><b>{rankUuid}</b> identifies the {@link Taxon} by its UUID. - <i>required</i>.
     * </ul>
	 * 
	 * @param response
	 * @param request
	 * @return a List of {@link TaxonNode} entities initialized by
	 *         the {@link #NODE_INIT_STRATEGY}
	 */
	@RequestMapping(
			value = {"/portal/classification/{treeUuid}/pathFrom/{taxonUuid}"}, 
			method = RequestMethod.GET)
	public List<TaxonNode> getPathFromTaxon(
			@PathVariable("treeUuid") UUID treeUuid,
			@PathVariable("taxonUuid") UUID taxonUuid,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		return getPathFromTaxonToRank(treeUuid, taxonUuid, null, request, response);
	}

	
	private Rank findRank(UUID rankUuid) {
		Rank rank = null;
		if(rankUuid != null){
			DefinedTermBase dt =  termService.find(rankUuid);
			if(dt instanceof Rank){
				rank = (Rank)dt;
			} else {
			   new IllegalArgumentException("DefinedTermBase is not a Rank");
			}
		}
		return rank;
	}

	
}