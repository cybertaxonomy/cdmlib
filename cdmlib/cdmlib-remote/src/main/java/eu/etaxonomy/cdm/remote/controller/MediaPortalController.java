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

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.IMediaService;
import eu.etaxonomy.cdm.model.media.Media;

/**
 * TODO write controller documentation
 * 
 * @author a.kohlbecker
 * @date 24.03.2009
 */

@Controller
@RequestMapping(value = {"/portal/media/{uuid}"})
public class MediaPortalController extends BaseController<Media, IMediaService>
{

	private static final List<String> MEDIA_INIT_STRATEGY = Arrays.asList(new String []{
			"$",
			"rights.type",
			"rights.agent",
			"representations.parts",
	});

	public MediaPortalController(){
		super();
		setInitializationStrategy(MEDIA_INIT_STRATEGY);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(IMediaService service) {
		this.service = service;
	}

}
