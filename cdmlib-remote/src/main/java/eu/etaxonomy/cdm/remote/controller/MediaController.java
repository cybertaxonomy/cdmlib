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
import java.net.URI;
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
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IMediaService;
import eu.etaxonomy.cdm.common.media.ImageInfo;
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
    public ModelAndView doGetMediaMetaData(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> result;

        ModelAndView mv = new ModelAndView();
        try{
            Media media = getCdmBaseInstance(uuid, response, MEDIA_INIT_STRATEGY);

            Set<MediaRepresentation> representations = media.getRepresentations();
            //get first representation and retrieve the according metadata

            Object[] repArray = representations.toArray();
            Object mediaRep = repArray[0];
            URI uri = null;
            try {
                if (mediaRep instanceof MediaRepresentation){
                    MediaRepresentation medRep = (MediaRepresentation) mediaRep;
                    uri = medRep.getParts().get(0).getUri();
                    ImageInfo imageInfo = ImageInfo.NewInstanceWithMetaData(uri, 3000);
                    result = imageInfo.getMetaData();
                    if(result != null) {
                        mv.addObject(result);
                    }
                }
            } catch (HttpException e) {
                logger.info(e.getMessage());
                HttpStatusMessage.create("Reading media file from " + uri.toString() + " failed due to (" + e.getMessage() + ")", 400).send(response);
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
        return mv;

    }

}
