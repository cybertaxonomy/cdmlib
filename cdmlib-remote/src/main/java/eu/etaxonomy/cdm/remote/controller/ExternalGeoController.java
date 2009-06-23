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

import java.awt.Color;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.ext.IEditGeoService;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.kohlbecker
 * @date 18.06.2009
 * 
 */
@Controller
@RequestMapping(value = { "/*/geo/map/distribution/*" })
public class ExternalGeoController extends BaseController<TaxonBase, ITaxonService> {

	@Autowired
	private IEditGeoService geoservice;
	
	public ExternalGeoController() {
		super();
		setUuidParameterPattern("^/(?:[^/]+)/geo/(?:[^/]+)/(?:[^/]+)/([^/?#&\\.]+).*");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.etaxonomy.cdm.remote.controller.BaseController#setService(eu.etaxonomy
	 * .cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(ITaxonService service) {
		this.service = service;
	}

	@RequestMapping(value = { "/*/geo/map/distribution/*" }, method = RequestMethod.GET)
	public ModelAndView doGetDistributionMapUriParams(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		ModelAndView mv = new ModelAndView();
		// get the descriptions for the taxon
		Taxon taxon = getCdmBase(request, response, null, Taxon.class);

		Map<PresenceAbsenceTermBase<?>, Color> presenceAbsenceTermColors = null;
		String uriParams = geoservice.getEditGeoServiceUrlParameterString(taxon, presenceAbsenceTermColors, 0, 0, null,
			"tdwg4");
		mv.addObject(uriParams);
		return mv;
	}

}
