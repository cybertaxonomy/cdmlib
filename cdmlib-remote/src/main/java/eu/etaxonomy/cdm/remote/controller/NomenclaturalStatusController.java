/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import io.swagger.annotations.Api;

/**
 * @author a.kohlbecker
 * @since Oct 11, 2016
 */
@Controller
@Api("nomenclaturalStatus")
@RequestMapping(value = {"/nomenclaturalStatus/{uuid}"})
public class NomenclaturalStatusController extends AbstractController<TaxonName, INameService> {

    private static final Logger logger = Logger.getLogger(NomenclaturalStatusController.class);

    @Override
    @Autowired
    public void setService(INameService service) {
        this.service = service;
    }

    @RequestMapping(method = RequestMethod.GET)
    public NomenclaturalStatus doGetMethod(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        String servletPath = request.getServletPath();

        logger.info("doGet() - " + requestPathAndQuery(request));

        NomenclaturalStatus nomstatus = service.loadNomenclaturalStatus(uuid, Arrays.asList("$", "source.citation.inReference"));
        if(nomstatus == null){
            HttpStatusMessage.UUID_NOT_FOUND.send(response);
        }
        return nomstatus;
    }
}