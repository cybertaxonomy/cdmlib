// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import io.swagger.annotations.Api;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * @author a.kohlbecker
 * @date 22.07.2010
 *
 */
@Controller
@Api("termVocabulary")
@RequestMapping(value = {"/termVocabulary"})
public class VocabularyListController extends AbstractIdentifiableListController<TermVocabulary, IVocabularyService> {

    public static final Logger logger = Logger.getLogger(VocabularyListController.class);

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.AbstractListController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Autowired
    @Override
    public void setService(IVocabularyService service) {
        this.service = service;
    }


}
