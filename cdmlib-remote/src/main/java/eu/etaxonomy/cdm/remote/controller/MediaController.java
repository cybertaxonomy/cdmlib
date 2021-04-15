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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
//import org.mortbay.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IMediaService;
import eu.etaxonomy.cdm.api.service.MediaServiceImpl;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.media.CdmImageInfo;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.remote.exception.NoRecordsMatchException;
import io.swagger.annotations.Api;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @since 24.03.2009
 */
@Controller
@Api("media")
@RequestMapping(value = {"/media/{uuid}"})
public class MediaController extends AbstractIdentifiableController<Media, IMediaService>{

    private static final Logger logger = Logger.getLogger(MediaController.class);

    @Autowired
    @Override
    public void setService(IMediaService service) {
        this.service = service;
    }
    private static final List<String> MEDIA_INIT_STRATEGY = Arrays.asList(new String []{
            "*",
            "representations",
            "representations.parts"
    });


    @RequestMapping(value = {"metadata"}, method = RequestMethod.GET)
    public Map<String, String> doGetMediaMetaData(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "applyFilterPreset", defaultValue = "true") Boolean applyFilterPreset,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        Map<String, String> result;
        try{
            Media media = getCdmBaseInstance(uuid, response, MEDIA_INIT_STRATEGY);

            Set<MediaRepresentation> representations = media.getRepresentations();
            if(media.getRepresentations().isEmpty()) {
                return null;
            }
            MediaRepresentation mediaRepresentation = representations.iterator().next();
            URI uri = null;
            try {
                if(applyFilterPreset) {
                        result = service.readResourceMetadataFiltered(mediaRepresentation);

                } else {
                    uri = mediaRepresentation.getParts().get(0).getUri();
                    CdmImageInfo cdmImageInfo = CdmImageInfo.NewInstanceWithMetaData(uri, MediaServiceImpl.IMAGE_READ_TIMEOUT);
                    result = cdmImageInfo.getMetaData();
                }
            } catch (IOException | HttpException e) {
                logger.info(e.getMessage());
                if(uri != null) {
                    // may happen when applyFilterPreset == false
                    HttpStatusMessage.create("Reading media file from " + uri.toString() + " failed due to (" + e.getMessage() + ")", 400).send(response);
                } else {
                    // may happen when applyFilterPreset == true
                    HttpStatusMessage.create("Reading media file failed due to (" + e.getMessage() + ")", 400).send(response);
                }
                return null;
            }

        } catch (NoRecordsMatchException e){
           /* IGNORE */
           /* java.lang.IllegalStateException: STREAM is thrown by the servlet container
            * if the model and view is returned after the http error has been send
            * so we return null here
            */
            return null;

        }
        return result;

    }

}
