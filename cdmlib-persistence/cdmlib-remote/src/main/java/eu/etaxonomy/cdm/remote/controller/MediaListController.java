// $Id$
/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.controller;

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
@RequestMapping(value = {"/media"})
public class MediaListController extends BaseListController<Media, IMediaService> {

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.BaseListController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Override
	@Autowired
	public void setService(IMediaService service) {
		this.service = service; 
	}
}