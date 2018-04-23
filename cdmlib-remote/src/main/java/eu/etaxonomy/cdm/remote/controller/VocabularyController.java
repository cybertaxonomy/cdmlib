/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import io.swagger.annotations.Api;

/**
 * @author a.kohlbecker
 * @since 22.07.2010
 *
 */
@Controller
@Api("termVocabulary")
@RequestMapping(value = {"/termVocabulary/{uuid}"})
public class VocabularyController extends AbstractIdentifiableController<TermVocabulary, IVocabularyService> {

    public static final Logger logger = Logger.getLogger(VocabularyController.class);


    @Autowired
    @Override
    public void setService(IVocabularyService service) {
        this.service = service;
    }


    @RequestMapping(value="terms", method=RequestMethod.GET, params="orderBy")
    public Pager<DefinedTermBase> terms(
            @PathVariable("uuid")UUID uuid,
            @RequestParam(name="orderBy", defaultValue="BY_ORDER_INDEX_ASC", required=true) OrderHintPreset orderBy,
            HttpServletResponse response,
            HttpServletRequest request) throws IOException {

        logger.info("terms() " + requestPathAndQuery(request));

        TermVocabulary<?> vocabulary = getCdmBaseInstance(uuid, response, (List<String>)null);

        Pager<DefinedTermBase> pager = service.getTerms(vocabulary, null, 0, orderBy.checkSuitableFor(vocabulary.getClass()).orderHints(), getInitializationStrategy());

        return pager;
    }

}
