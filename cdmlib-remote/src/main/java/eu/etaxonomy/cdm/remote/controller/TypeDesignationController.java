/**
* Copyright (C) 2018 EDIT
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
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.dto.MediaDTO;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import io.swagger.annotations.Api;

/**
 * @author a.kohlbecker
 * @since Jul 31, 2018
 *
 */
@Controller
@Api(value = "typeDesignation")
@RequestMapping(value = {"/typedesignation/{uuid}"})
public class TypeDesignationController extends AbstractController<TaxonName, INameService> {


    private static final Logger logger = Logger.getLogger(TypeDesignationController.class);

    /**
     * {@inheritDoc}
     */
    @Override
    @Autowired
    public void setService(INameService service) {
        this.service = service;
    }

    @RequestMapping(value="media", method = RequestMethod.GET)
    public Collection<MediaDTO> doGetMedia(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {


        logger.info("doGetMediaUris() - " + requestPathAndQuery(request));
        ArrayList<MediaDTO> dtos = new ArrayList<>();
        TypeDesignationBase<?> td = service.loadTypeDesignation(uuid, Arrays.asList("typeSpecimen.mediaSpecimen.representations.mediaRepresentationParts"));
        if(td instanceof SpecimenTypeDesignation){
            SpecimenTypeDesignation std = (SpecimenTypeDesignation)td;
            DerivedUnit du = HibernateProxyHelper.deproxy(std.getTypeSpecimen(), DerivedUnit.class);
            if(du != null && du instanceof MediaSpecimen) {
                Media media = ((MediaSpecimen)du).getMediaSpecimen();
                if(media != null){
                    for(MediaRepresentation mrp : media.getRepresentations()){
                        for(MediaRepresentationPart p : mrp.getParts()){
                            if(p.getUri() != null){
                                MediaDTO dto = new MediaDTO(media.getUuid());
                                dto.setUri(p.getUri().toString());
                                dtos.add(dto);
                            }
                        }
                    }
                }
            }

        }
        return dtos;
    }

    @RequestMapping(method = RequestMethod.GET)
    public TypeDesignationBase<?> doGetMethod(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        String servletPath = request.getServletPath();
        String propertyName = FilenameUtils.getBaseName(servletPath);

        logger.info("doGet() - " + requestPathAndQuery(request));

        TypeDesignationBase<?> dtb = service.loadTypeDesignation(uuid, Arrays.asList("$"));
        if(dtb == null){
            HttpStatusMessage.UUID_NOT_FOUND.send(response);
        }
        return dtb;
    }

}
