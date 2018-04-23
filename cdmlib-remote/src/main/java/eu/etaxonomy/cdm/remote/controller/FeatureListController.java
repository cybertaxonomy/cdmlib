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

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.description.DescriptionBase;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @since 24.03.2009
 */

@Controller
@Api("feature")
@RequestMapping(value = {"/feature"}) //FIXME refactor type mappings
public class FeatureListController extends AbstractIdentifiableListController<DescriptionBase, IDescriptionService>
{
    @Autowired
    private ITermService termService;

    private static final List<String> FEATURE_INIT_STRATEGY = Arrays.asList(new String[]{"representations"});

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Autowired
    @Override
    public void setService(IDescriptionService service) {
        this.service = service;
    }

}
