// $Id$
/**
* Copyright (C) 2009 EDIT
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

import javax.servlet.http.HttpServletResponse;

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

/**
 * @author a.kohlbecker
 * @date 03.06.2010
 *
 */
@Controller
@RequestMapping(value = {"/classification/{uuid}"})
public class ClassificationController extends AnnotatableController<TaxonomicTree,ITaxonTreeService> {


	private ITermService termService;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.BaseController#setService(eu.etaxonomy.cdm.api.service.IService)
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
	@Override
    public void initBinder(WebDataBinder binder) {
		super.initBinder(binder);
		binder.registerCustomEditor(Rank.class, new RankPropertyEditor());
	}
	
	private List<String> NODE_INIT_STRATEGY(){
		return Arrays.asList(new String[]{
			"taxon.sec", 
			"taxon.name",
			"taxonomicTree"
	});}
	
	/**
	 * @param classificationUuid
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(
			value = {"childNodes"},
			method = RequestMethod.GET)
	public List<TaxonNode> getChildNodes(
			@PathVariable("uuid") UUID classificationUuid,
			HttpServletResponse response
			) throws IOException {
		
		return getChildNodesAtRank(classificationUuid, null, response);
	}
	
	@RequestMapping(
			value = {"childNodesAt/{rankUuid}"},
			method = RequestMethod.GET)
	public List<TaxonNode> getChildNodesAtRank(
			@PathVariable("uuid") UUID classificationUuid,
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
