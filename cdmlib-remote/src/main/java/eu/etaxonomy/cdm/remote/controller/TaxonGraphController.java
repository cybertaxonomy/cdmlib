/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.taxonGraph.ITaxonGraphService;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.TaxonGraphException;
import eu.etaxonomy.cdm.persistence.dto.TaxonGraphEdgeDTO;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;
import io.swagger.annotations.Api;

/**
 * @author a.kohlbecker
 * @since Oct 2, 2018
 *
 */
@Controller
@Api("taxonGraph")
@RequestMapping(value = {"/taxonGraph"})
public class TaxonGraphController {


    public static final Logger logger = Logger.getLogger(TaxonGraphController.class);

    @Autowired
    private ITaxonGraphService service;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, value="edges")
    public List<TaxonGraphEdgeDTO> doEdges(
            @RequestParam(value = "fromTaxonUuid", required = false) UUID fromTaxonUuid,
            @RequestParam(value = "toTaxonUuid", required = false) UUID toTaxonUuid,
            HttpServletRequest request) throws TaxonGraphException{

        logger.info("doEdges() " + request.getRequestURL());
        return service.edges(fromTaxonUuid, toTaxonUuid, false);
    }

}
