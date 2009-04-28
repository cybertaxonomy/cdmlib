// $Id: TaxonController.java 5473 2009-03-25 13:42:07Z a.kohlbecker $
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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.kohlbecker
 *
 */

@Controller
@RequestMapping(value = {"/*/portal/taxon/*", "/*/portal/taxon/*/*"})
public class TaxonPortalController extends BaseController<TaxonBase, ITaxonService>
{
	public static final Logger logger = Logger.getLogger(TaxonPortalController.class);
	
	private static final List<String> TAXON_INIT_STRATEGY = Arrays.asList(new String []{
			"*",
			// taxon relations 
			"relationsToThisName.fromTaxon.name.taggedName",
			// the name
			"name.$",
			"name.taggedName",
			"name.rank.representations",
			"name.status.type.representations",
			
			// descriptions
			"descriptions.elements.$",
			"descriptions.elements.area",
			"descriptions.elements.area.$",
			"descriptions.elements.multilanguageText",
			"descriptions.elements.media.representations.parts",
			
//			// typeDesignations
//			"name.typeDesignations.$",
//			"name.typeDesignations.citation.authorTeam",
//			"name.typeDesignations.typeName.$",
//			"name.typeDesignations.typeStatus.representations",
//			"name.typeDesignations.typeSpecimen.media.representations.parts"
			
			});
	
	private static final List<String> SYNONYMY_INIT_STRATEGY = Arrays.asList(new String []{
			// initialize homotypical and heterotypical groups; needs synonyms
			"synonymRelations.$",
			"synonymRelations.synonym.$",
			"synonymRelations.synonym.name.taggedName",
			"synonymRelations.synonym.name.homotypicalGroup.typifiedNames.$",
			"synonymRelations.synonym.name.homotypicalGroup.typifiedNames.name.taggedName",
			"synonymRelations.synonym.name.homotypicalGroup.typifiedNames.taxonBases.$",
			"synonymRelations.synonym.name.homotypicalGroup.typifiedNames.taxonBases.name.taggedName",
			
			"name.homotypicalGroup.$",
			"name.homotypicalGroup.typifiedNames.$",
			"name.homotypicalGroup.typifiedNames.name.taggedName",
			"name.homotypicalGroup.typifiedNames.taxonBases.$",
			"name.homotypicalGroup.typifiedNames.taxonBases.name.taggedName"
	});
	
	public TaxonPortalController(){
		super();
		setUuidParameterPattern("^/(?:[^/]+)/portal/taxon/([^/?#&\\.]+).*");
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(ITaxonService service) {
		this.service = service;
	}
	
	@Override
	@RequestMapping(method = RequestMethod.GET)
	public TaxonBase doGet(HttpServletRequest request, HttpServletResponse response)throws IOException {
		TaxonBase tb = getCdmBase(request, response, TAXON_INIT_STRATEGY, TaxonBase.class);
		return tb;
	}

	
	@RequestMapping(
			value = {"/*/portal/taxon/*/synonymy"},
			method = RequestMethod.GET)
	public ModelAndView doGetSynonymy(HttpServletRequest request, HttpServletResponse response)throws IOException {
		ModelAndView mv = new ModelAndView();
		TaxonBase tb = getCdmBase(request, response, null, Taxon.class);
		Taxon taxon = (Taxon)tb;
		Map<String, List<?>> synonymy = new Hashtable<String, List<?>>();
		synonymy.put("homotypicSynonymsByHomotypicGroup", service.getHomotypicSynonymsByHomotypicGroup(taxon, SYNONYMY_INIT_STRATEGY));
		synonymy.put("heterotypicSynonymyGroups", service.getHeterotypicSynonymyGroups(taxon, SYNONYMY_INIT_STRATEGY));
		mv.addObject(synonymy);
		return mv;
	}

//	/**
//	 * @param request
//	 * @param response
//	 * @return
//	 * @throws IOException
//	 */
//	private <T> T getTaxon(HttpServletRequest request, HttpServletResponse response, List<String> initStrategy, Class<T> clazz) throws IOException {
//		TaxonBase tb = null;
//		try {
//			UUID uuid = readValueUuid(request);
//			Assert.notNull(uuid, HttpStatusMessage.UUID_NOT_FOUND.toString());
//			tb = service.load(uuid, initStrategy);
//			Assert.notNull(tb, HttpStatusMessage.UUID_NOT_FOUND.toString());
//		} catch (IllegalArgumentException iae) {
//			HttpStatusMessage.fromString(iae.getMessage()).send(response);
//		}
//		T t;
//		try {
//			t = (T)tb;
//			return t;
//		} catch (Exception e) {
//			HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
//			return null;
//		}
//	}
}
