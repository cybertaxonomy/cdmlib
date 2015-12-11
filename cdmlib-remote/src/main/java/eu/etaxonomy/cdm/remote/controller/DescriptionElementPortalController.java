/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller;

import io.swagger.annotations.Api;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Api("portal_descriptionElement")
@Transactional(readOnly=true)
@RequestMapping(value = {
            "/portal/descriptionElement/{uuid}",
            "/portal/descriptionElement/{uuid_list}"
            })
public class DescriptionElementPortalController extends DescriptionElementController
{

}