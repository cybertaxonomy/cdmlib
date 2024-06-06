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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.dto.SpecimenOrObservationBaseDTO;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import io.swagger.annotations.Api;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @since 24.03.2009
 */
@Controller
@Api("occurrence")
@RequestMapping(value = {"/occurrence/{uuid}"})
public class OccurrenceController extends AbstractIdentifiableController<SpecimenOrObservationBase, IOccurrenceService>{

    private static final Logger logger = LogManager.getLogger();

    protected static final List<String> DEFAULT_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "sequences.$",
    });

    public static final List<String> DERIVED_UNIT_INIT_STRATEGY =  Arrays.asList(new String []{
            "derivedFrom.derivatives.*",
            "derivedFrom.originals.*",
            "specimenTypeDesignations.*",
            "specimenTypeDesignations.designationSource.citation.*",
            "specimenTypeDesignations.homotypicalGroup.*",
            "specimenTypeDesignations.typifiedNames",
            "collection.$",
            "derivationEvents.derivatives.*"
    });

    private static final List<String> EXTENSIONS_INIT_STRATEGY =  Arrays.asList(new String []{
            "extensions.type",
    });

    @Autowired
    @Override
    public void setService(IOccurrenceService service) {
        this.service = service;
    }

    @Override
    protected <CDM_BASE extends CdmBase> List<String> complementInitStrategy(Class<CDM_BASE> clazz,
            List<String> pathProperties) {

        if(pathProperties.stream().anyMatch(s -> s.startsWith("specimenTypeDesignations"))) {
            List<String> complemented = new ArrayList<>(pathProperties);
            complemented.add("specimenTypeDesignations.designationSource.citation.*");
            return complemented;
        }
        if(pathProperties.stream().anyMatch(s -> s.startsWith("sources"))) {
            List<String> complemented = new ArrayList<>(pathProperties);
            complemented.add("sources.citation.*");
            return complemented;
        }
        return pathProperties;
    }

    @RequestMapping(value = { "derivedFrom" }, method = RequestMethod.GET)
    public DerivationEvent doGetDerivedFrom(
            @PathVariable("uuid") UUID uuid, HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doGetDerivedFrom()" + requestPathAndQuery(request));

        SpecimenOrObservationBase<?> sob = getCdmBaseInstance(uuid, response, DERIVED_UNIT_INIT_STRATEGY);
        sob = checkExistsAndAccess(sob, NO_UNPUBLISHED, response);
        if(sob instanceof DerivedUnit){
            DerivationEvent derivationEvent = ((DerivedUnit)sob).getDerivedFrom();
            if (derivationEvent != null) {
                return derivationEvent;
            }
        }
        return null;
    }


    @RequestMapping(value = { "rootUnitDTOs" }, method = RequestMethod.GET)
    public Collection<SpecimenOrObservationBaseDTO<?>> doGetRootUnitDTOs(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doGetRootUnitDTOs()" + requestPathAndQuery(request));

        SpecimenOrObservationBase<?> sob = getCdmBaseInstance(uuid, response, "$");
        sob = checkExistsAndAccess(sob, NO_UNPUBLISHED, response);

        Collection<SpecimenOrObservationBaseDTO<?>> fieldUnitDtos = service.findRootUnitDTOs(uuid);
        return fieldUnitDtos;
    }

    @RequestMapping(value = { "extensions" }, method = RequestMethod.GET)
    public Object doGetExtensions(
            @PathVariable("uuid") UUID uuid, HttpServletRequest request,
            // doPage request parameters
            @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            // doList request parameters
            @RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "limit", required = false) Integer limit,
            HttpServletResponse response) throws IOException {

        logger.info("doGetExtensions()" + requestPathAndQuery(request));
        SpecimenOrObservationBase<?> sob = getCdmBaseInstance(uuid, response, EXTENSIONS_INIT_STRATEGY);
        sob = checkExistsAndAccess(sob, NO_UNPUBLISHED, response);

        return pageFromCollection(sob.getExtensions(), pageIndex, pageSize, start, limit, response) ;
    }
}
