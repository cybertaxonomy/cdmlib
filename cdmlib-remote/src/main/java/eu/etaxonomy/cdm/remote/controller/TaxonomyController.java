// $Id$
/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.kohlbecker
 * @date 20.03.2009
 */
@Controller
public class TaxonomyController extends AbstractListController<TaxonBase, ITaxonService> {
	
	public static final Logger logger = Logger.getLogger(TaxonomyController.class);

	private ITaxonService service;
	
	private IReferenceService referenceService;
	
	private Pattern parameterPattern = Pattern.compile("^/(?:[^/]+)/taxonomy/([^?#&\\.]+).*");

	@Autowired
	public void setService(ITaxonService service) {
		this.service = service; 
	}
	
	@Autowired
	public void setReferenceService(IReferenceService referenceService) {
		this.referenceService = referenceService;
	}

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

	private UUID stringToUuid(String uuidStr) {
		try {
			UUID uuid = UUID.fromString(uuidStr);
			return uuid;
		} catch (Exception e) {
			throw new IllegalArgumentException(uuidStr + "is not a uuid");
		}
	}
	
	@RequestMapping(
			value = {"/*/taxonomy", "/*/taxonomy/*"}, 
			method = RequestMethod.GET)
	public List<Taxon> getRootTaxa(HttpServletRequest request) {
		List<String> uriParams = readUriParameters(request);
		ReferenceBase secref = null;
		Rank rank = null;
		
		if(uriParams.size() == 1){
			int pos;
			if((pos = uriParams.get(0).indexOf(',')) > 0){
				secref = referenceService.findByUuid(stringToUuid(uriParams.get(0).substring(0, pos)));
				try {
					rank = Rank.getRankByName(uriParams.get(0).substring(pos + 1));
				} catch (UnknownCdmTypeException e) {
					logger.error("404 rank not found");
				}
			} else {
				secref = referenceService.findByUuid(stringToUuid(uriParams.get(0)));				
			}
			
			if(secref == null) {
				logger.error("404 reference not found");
			}
		}
		if(uriParams.size() > 1){
			throw new IllegalArgumentException("Only one uuid parameter expected but found " + uriParams.size() );
		}
		return (List<Taxon>) service.getRootTaxa(rank, secref, true, false);
	}

	@RequestMapping(
			value = {"/*/taxonomy/*/*", "/*/taxonomy/*/**/*"}, 
			method = RequestMethod.GET)
	public Set<Taxon> getChildTaxa(HttpServletRequest request) {
		List<String> uriParameters = readUriParameters(request);
		if(uriParameters.size() <= 1){
			throw new IllegalArgumentException("At least two uuid parameters expected but found " + uriParameters.size() );
		}
		try {
			Taxon parentTaxon = (Taxon) service.findByUuid(stringToUuid(uriParameters.get(uriParameters.size() - 1)));
			return parentTaxon.getTaxonomicChildren();
		} catch (ClassCastException cce) {
			logger.warn("The specified instance is not a taxon");
		}
		return null;
	}
}