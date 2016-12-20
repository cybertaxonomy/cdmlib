/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.controller.ext;

import io.swagger.annotations.Api;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.database.UpdatableRoutingDataSource;
import eu.etaxonomy.cdm.ext.dc.DublinCoreSchemaAdapter;
import eu.etaxonomy.cdm.ext.sru.SruServiceWrapper;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * The ExternalGeoController class is a Spring MVC Controller.
 * <p>
 * The syntax of the mapped service URIs contains the the {datasource-name} path element.
 * The available {datasource-name}s are defined in a configuration file which
 * is loaded by the {@link UpdatableRoutingDataSource}. If the
 * UpdatableRoutingDataSource is not being used in the actual application
 * context any arbitrary {datasource-name} may be used.
 * <p>
 * @author a.kohlbecker
 * @date 18.06.2009
 *
 */
//@Controller // http://gso.gbv.de/sru/DB=2.1/ is defunctional!!! thus this controller is disabled
@Api(value="ext_bhl-e", description="Provides searche via the SRU (search and retrieve) API of BHL-E ")
@RequestMapping(value = { "/ext/bhl-e/" })
public class ExternalBhleController {

    public static final Logger logger = Logger.getLogger(ExternalBhleController.class);

    public static String baseUrl = "http://gso.gbv.de/sru/DB=2.1/";

    SruServiceWrapper sruServiceWrapper;

    /**
     *
     */
    public ExternalBhleController() {
        sruServiceWrapper = new SruServiceWrapper();
        sruServiceWrapper.setBaseUrl(baseUrl);
        sruServiceWrapper.addSchemaAdapter(new DublinCoreSchemaAdapter());
    }


    @RequestMapping(value = { "grib/sru" }, method = RequestMethod.GET)
    public ModelAndView doSearchRetrieve(
            @RequestParam(value = "query", required = true) String cqlQuery,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        logger.info("doSearchRetrieve( " + "query=\"" + ObjectUtils.toString(cqlQuery) + "\")");
        ModelAndView mv = new ModelAndView();

        List<Reference> referenceList = sruServiceWrapper.doSearchRetrieve(cqlQuery, "dc");

        mv.addObject(referenceList);
        return mv;
    }

}
