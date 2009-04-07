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
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.remote.editor.RankPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.kohlbecker
 * @date 20.03.2009
 */
@Controller
public class TaxonomyController extends AbstractListController<TaxonBase, ITaxonService> {
	
	/**
	 * 
	 */
	private static final List<String> TAXON_INIT_STRATEGY = Arrays.asList(new String[]{
			"sec", 
			"relationsToThisTaxon.toTaxon.$",
			"name.rank.representations"});
	
	private static final List<String> PARENT_TAXON_INIT_STRATEGY = Arrays.asList(new String[]{
			"sec", 
			"relationsFromThisTaxon.fromTaxon.$",
			"relationsFromThisTaxon.fromTaxon.name.rank.representations",
			"name.rank.$",
			"name.rank.representations"});

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
	@RequestMapping(
			value = {"/*/taxonomy"},
			params = {"uuid"},
			method = RequestMethod.GET)
	public String findTaxon(
			@RequestParam(value = "uuid", required = true) UUID uuid,
			@RequestParam(value = "rank", required = false) Rank rank,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String msg404 = rank != null ? "Taxon not found within rank "+ rank.getLabel() : "Taxon not found.";
		
		TaxonBase tb = service.load(uuid, TAXON_INIT_STRATEGY);
				
		if(tb != null && Taxon.class.isAssignableFrom(tb.getClass())){			
			Taxon t = (Taxon)tb;
			String relPath = "";
			String basePath = FilenameUtils.removeExtension(request.getServletPath()); 
			basePath += "/" + t.getSec().getUuid().toString();
			if(rank != null){
				basePath += "," + rank.getLabel();
			}
			
			// compose path of parent uuids
			Taxon taxon = t;
			while( taxon != null && (rank == null || taxon.getName().getRank() == null || taxon.getName().getRank().compareTo(rank) <= 0) ) {
				relPath = "/" + taxon.getUuid().toString() + relPath;
				taxon  = taxon.getTaxonomicParent();
				if(taxon != null){
					taxon = (Taxon)service.load(taxon.getUuid(), TAXON_INIT_STRATEGY);
				}
			};
			
			if(relPath.length() > 0){
				URI redirectUri;
				try {
					redirectUri = relativeToFullUri(request, basePath + relPath);
					if(logger.isInfoEnabled()){
						logger.info("redirecting to " + redirectUri);
					}
					response.sendRedirect(redirectUri.toString());
					return "";
				} catch (URISyntaxException e) {
					logger.error(e.getMessage(), e);
				}
			}
		} else {
			msg404 = "The taxon is not accepted";
		}
		response.sendError(HttpServletResponse.SC_NOT_FOUND, msg404);
		return "";
	}
	
	/**
	 * @param request
	 * @return
	 */
	@RequestMapping(
			value = {"/*/taxonomy/*"}, 
			method = RequestMethod.GET)
	public List<Taxon> getRootTaxa(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		List<String> uriParams = readUriParameters(request);
		ReferenceBase secref = null;
		Rank rank = null;
		if(uriParams == null){
			return (List<Taxon>) service.getRootTaxa(rank, null, true, false);
			//response.sendError(HttpServletResponse.SC_NOT_FOUND, "");
			//return null;
		}
		if(uriParams.size() == 1){
			// get secuuid and rank
			secref = readSecByUuid(uriParams.get(0));
			rank = readRankByLabel(uriParams.get(0));
			
			if(secref == null) {
				response.sendError(404 , "SecReference not found by " + stringToUuid(uriParams.get(0)) );
				return null;
			}
			
		}
		if(uriParams.size() > 1){
			response.sendError(400, "Only one uuid parameter expected but found  " + uriParams.size());
			return null;
		}
		return (List<Taxon>) service.getRootTaxa(rank, secref, true, false);
	}



	/**
	 * @param request
	 * @return
	 * @throws IOException 
	 */
	@RequestMapping(
			value = {"/*/taxonomy/*/*", "/*/taxonomy/*/**/*"}, 
			method = RequestMethod.GET)
	public Set<Taxon> getChildTaxa(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		List<String> uriParameters = readUriParameters(request);
		if(uriParameters.size() <= 1){
			response.sendError(400, "At least two uuid parameters expected but found " + uriParameters.size());
			return null;
		}
		try {
			UUID uuid = stringToUuid(uriParameters.get(uriParameters.size() - 1));
			Taxon taxon = (Taxon) service.load(uuid, TAXON_INIT_STRATEGY);
			return taxon.getTaxonomicChildren();
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
			value = {"/*/taxonomy/*/*/path", "/*/taxonomy/*/**/*/path"}, 
			method = RequestMethod.GET)
	public List<Taxon> getPathToRoot(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		List<Taxon> pathToRoot = new ArrayList<Taxon>();
		List<String> uriParameters = readUriParameters(request);
		if(uriParameters.size() <= 1){
			response.sendError(400, "At least two uuid parameters expected but found " + uriParameters.size());
			return null;
		}
		// get rank
		Rank rank = readRankByLabel(uriParameters.get(0));
		
		try {
			UUID uuid = stringToUuid(uriParameters.get(uriParameters.size() - 2));
			Taxon parentTaxon = (Taxon) service.load(uuid, PARENT_TAXON_INIT_STRATEGY);
			while(parentTaxon != null){
				//FIXME orderindex in parentTaxon.getName().getRank() is not set !!!
				// original: if(rank != null && rank.isLower(parentTaxon.getName().getRank())){
				// Preliminary solution below:
				if(rank != null){
					try {
						Rank compareToRank = Rank.getRankByName(parentTaxon.getName().getRank().getLabel());
						if(rank.isLower(compareToRank)){
							break;
						}
					} catch (UnknownCdmTypeException e) {
						logger.error(e);
					}
				}
				pathToRoot.add(parentTaxon);
				parentTaxon = parentTaxon.getTaxonomicParent();
				if(parentTaxon != null){
					parentTaxon = (Taxon)service.load(parentTaxon.getUuid(), PARENT_TAXON_INIT_STRATEGY);
				}
			}
			
			return pathToRoot;
		} catch (ClassCastException cce) {
			logger.warn("The specified instance is not a taxon", cce);
			response.sendError(500, "The specified instance is not a taxon");
			return null;
		}
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

	/**
	 * @param paramStr
	 * @return
	 */
	private ReferenceBase readSecByUuid(String paramStr) {
		UUID secRefuuid; 
		int pos;
		if((pos = paramStr.indexOf(',')) > 0){
			
			secRefuuid = stringToUuid(paramStr.substring(0, pos));
		} else {
			secRefuuid = stringToUuid(paramStr);
		}
		return referenceService.findByUuid(secRefuuid);			
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
			throw new IllegalArgumentException(uuidStr + "is not a uuid");
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