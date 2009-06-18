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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.FeatureTree;

/**
 * @author a.kohlbecker
 * @date 24.03.2009
 */
@Controller
@RequestMapping(value = {"/*/description", "/*/featuretree"})
public class DescriptionListController extends BaseListController<DescriptionBase, IDescriptionService> {
	
	private static final List<String> FEATURETREE_INIT_STRATEGY = Arrays.asList(
			new String[]{
				"representations",
				"root.feature.representations",
				"root.children.feature.representations",
			});

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.BaseListController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Override
	@Autowired
	public void setService(IDescriptionService service) {
		this.service = service;
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/*/featuretree")
	public List<FeatureTree> doGetFeatureTrees(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		List<FeatureTree> obj = service.getFeatureTreesAll(FEATURETREE_INIT_STRATEGY);
		return obj;
	}
}