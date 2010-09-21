// $Id$
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.controller.ext;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.database.UpdatableRoutingDataSource;
import eu.etaxonomy.cdm.ext.geo.IEditGeoService;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.remote.controller.BaseController;
import eu.etaxonomy.cdm.remote.l10n.LocaleContext;

/**
 * The ExternalGeoController class is a Spring MVC Controller.
 * <p>
 * The syntax of the mapped service URIs contains the the {datasource-name} path element.
 * The available {datasource-name}s are defined in a configuration file which
 * is loaded by the {@link UpdatableRoutingDataSource}. If the
 * UpdatableRoutingDataSource is not being used in the actual application
 * context any arbitrary {datasource-name} may be used.
 * <p> 
 * @author a.kohlbecker
 * @date 18.06.2009
 * 
 */
@Controller
@RequestMapping(value = { "ext/edit/mapServiceParameters/" })
public class ExternalGeoController extends BaseController<TaxonBase, ITaxonService> {
	
	public static final Logger logger = Logger.getLogger(ExternalGeoController.class);

	@Autowired
	private IEditGeoService geoservice;
	@Autowired
	private IDescriptionService descriptionService;

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

	/**
	 * Assembles and returns URI parameter Strings for the EDIT Map Service. The distribution areas for the  
	 * {@link Taxon} instance identified by the <code>{taxon-uuid}</code> are found and are translated into 
	 * an valid URI parameter String. Higher level distribiution areas are expanded in order to include all 
	 * nested sub-areas. 
	 * <p>
	 * URI: <b>&#x002F;{datasource-name}&#x002F;geo&#x002F;map&#x002F;distribution&#x002F;{taxon-uuid}</b>
	 * 
	 * @param request
	 * @param response
	 * @return URI parameter Strings for the EDIT Map Service
	 * @throws IOException TODO write controller method documentation
	 */
	@RequestMapping(value = { "taxonDistributionFor/{uuid}" }, method = RequestMethod.GET)
	public ModelAndView doGetDistributionMapUriParams(
			@PathVariable("uuid") UUID uuid,
			HttpServletRequest request, 
			HttpServletResponse response)
			throws IOException {
		
		
		int width = 0;
		int height = 0;
		String bbox = null;
		String backLayer = null;
		
		logger.info("doGetDistributionMapUriParams() " + request.getServletPath());
		ModelAndView mv = new ModelAndView();
		
		// get the descriptions for the taxon
		Taxon taxon = getCdmBaseInstance(Taxon.class, uuid, response, (List<String>)null);
		
		Map<PresenceAbsenceTermBase<?>, Color> presenceAbsenceTermColors = null;
		//languages
		List<Language> langs = LocaleContext.getLanguages();

		Pager<TaxonDescription> page = descriptionService.getTaxonDescriptions(taxon, null, null, null, null, null);
		List<TaxonDescription> taxonDescriptions = page.getRecords();
		String uriParams = geoservice.getDistributionServiceRequestParameterString(taxonDescriptions, presenceAbsenceTermColors, width, height, bbox,
			backLayer, langs);
		mv.addObject(uriParams);
		return mv;
	}
	
	
	/**
	 * Assembles and returns URI parameter Strings for the EDIT Map Service. The distribution areas for the  
	 * {@link Taxon} instance identified by the <code>{taxon-uuid}</code> are found and are translated into 
	 * an valid URI parameter String. Higher level distribiution areas are expanded in order to include all 
	 * nested sub-areas. 
	 * <p>
	 * URI: <b>&#x002F;{datasource-name}&#x002F;geo&#x002F;map&#x002F;distribution&#x002F;{taxon-uuid}</b>
	 * 
	 * @param request
	 * @param response
	 * @return URI parameter Strings for the EDIT Map Service
	 * @throws IOException TODO write controller method documentation
	 */
	@RequestMapping(value = { "taxonOccurrencesFor/{uuid}" }, method = RequestMethod.GET)
	public ModelAndView doGetOccurrenceMapUriParams(
			@PathVariable("uuid") UUID uuid,
			HttpServletRequest request, 
			HttpServletResponse response)
			throws IOException {
		
		Integer width = null;
		Integer height = null;
		String bbox = null;
		String backLayer = null;
		Boolean doReturnImage = null;
		Map<Class<? extends SpecimenOrObservationBase<?>>, Color> specimenOrObservationTypeColors = null;
		
		logger.info("doGetOccurrenceMapUriParams() " + request.getServletPath());
		ModelAndView mv = new ModelAndView();
		
		// get the descriptions for the taxon
		Taxon taxon = getCdmBaseInstance(Taxon.class, uuid, response, (List<String>)null);

		Pager<TaxonDescription> page = descriptionService.getTaxonDescriptions(taxon, null, null, null, null, null);
		List<TaxonDescription> taxonDescriptions = page.getRecords();
		
		String uriParams = geoservice.getOccurrenceServiceRequestParameterString(taxonDescriptions, specimenOrObservationTypeColors, doReturnImage, width , height , bbox , backLayer );
		mv.addObject(uriParams);
		return mv;
	}

}
