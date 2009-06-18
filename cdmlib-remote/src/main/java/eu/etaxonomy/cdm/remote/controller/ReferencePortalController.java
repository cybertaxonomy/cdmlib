// $Id: ReferenceController.java 5561 2009-04-07 12:25:33Z a.kohlbecker $
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
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.api.service.AnnotatableServiceBase;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;

/**
 * @author a.kohlbecker
 * @date 24.03.2009
 */

// currently unused @Controller
@RequestMapping(value = {"/*/portal/reference/*"})
public class ReferencePortalController extends BaseController<ReferenceBase, IReferenceService>
{
	
	private static final List<String> REFERENCE_INIT_STRATEGY = Arrays.asList(new String []{
			"$",
			"authorTeam.$"});
	
	public ReferencePortalController(){
		super();
		setUuidParameterPattern("^/(?:[^/]+)/portal/reference/([^/?#&\\.]+).*");
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(IReferenceService service) {
		this.service = service;
	}
	
	@Override
	@RequestMapping(method = RequestMethod.GET)
	public ReferenceBase doGet(HttpServletRequest request, HttpServletResponse response)throws IOException {
		
		ReferenceBase rb;
		try {
			UUID uuid = readValueUuid(request, null);
			Assert.notNull(uuid, HttpStatusMessage.UUID_NOT_FOUND.toString());
			
			rb = service.load(uuid, REFERENCE_INIT_STRATEGY);
			Assert.notNull(rb, HttpStatusMessage.UUID_NOT_FOUND.toString());
		} catch (IllegalArgumentException iae) {
			HttpStatusMessage.fromString(iae.getMessage()).send(response);
			return null;
		}

		return rb;
	}

}
