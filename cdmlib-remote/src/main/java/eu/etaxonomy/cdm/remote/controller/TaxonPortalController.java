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
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.kohlbecker
 *
 */

@Controller
@RequestMapping(value = {"/*/portal/taxon/*"})
public class TaxonPortalController extends BaseController<TaxonBase, ITaxonService>
{
	public static final Logger logger = Logger.getLogger(TaxonPortalController.class);
	
	private static final List<String> TAXON_INIT_STRATEGY = Arrays.asList(new String []{
			"*",
			"name.*",
			"name.nomenclaturalReference.authorTeam.*",
			"name.homotypicalGroup.typifiedNames.*",
			"name.status.type.representations",
			"name.rank.representations",
			"synonymRelations.*"});
	
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
		
		TaxonBase tb;
		try {
			UUID uuid = readValueUuid(request);
			Assert.notNull(uuid, HttpStatusMessage.UUID_NOT_FOUND.toString());
			
			tb = service.load(uuid, TAXON_INIT_STRATEGY);
			Assert.notNull(tb, HttpStatusMessage.UUID_NOT_FOUND.toString());
		} catch (IllegalArgumentException iae) {
			HttpStatusMessage.fromString(iae.getMessage()).send(response);
			return null;
		}

		return tb;
	}
}
