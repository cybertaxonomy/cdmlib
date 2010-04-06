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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.api.service.AnnotatableServiceBase;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IFeatureTreeService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;

/**
 * TODO write controller documentation
 * 
 * @author a.kohlbecker
 * @date 24.03.2009
 */

@Controller
@RequestMapping(value = {"/description/*","/description/*/annotation", "/featuretree/*"})
public class DescriptionController extends AnnotatableController<DescriptionBase, IDescriptionService>
{
	@Autowired
	private IFeatureTreeService featureTreeService;
	
	public DescriptionController(){
		super();
		setUuidParameterPattern("^/(?:[^/]+)/([^/?#&\\.]+).*");
	}
	
	private static final List<String> FEATURETREE_INIT_STRATEGY = Arrays.asList(
			new String[]{
				"representations",
				"root.feature.representations",
				"root.children.feature.representations",
				//TODO implement recursive INIT_STRATEGY for children e.g: 
				//     "root.children{<children;limit=0}.feature.representations",
			});
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(IDescriptionService service) {
		this.service = service;
	}
	
	/**
	 * TODO write controller method documentation
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(
			value = {"/featuretree/*"},
			method = RequestMethod.GET)
	public FeatureTree doGetFeatureTree(HttpServletRequest request, HttpServletResponse response)throws IOException {
		UUID featureTreeUuid = readValueUuid(request, null);
		FeatureTree featureTree = featureTreeService.load(featureTreeUuid, FEATURETREE_INIT_STRATEGY);
		if(featureTree == null){
			HttpStatusMessage.UUID_NOT_FOUND.send(response);
		}
		return featureTree;
	}

}
