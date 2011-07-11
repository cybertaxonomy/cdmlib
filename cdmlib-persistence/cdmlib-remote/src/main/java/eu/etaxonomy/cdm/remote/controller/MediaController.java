// $Id$
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IMediaService;
import eu.etaxonomy.cdm.common.media.ImageInfo;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @date 24.03.2009
 */

@Controller
@RequestMapping(value = {"/media/{uuid}"})
public class MediaController extends AnnotatableController<Media, IMediaService>
{

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
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

    @RequestMapping(value = {"metadata"})
    public ModelAndView doGetMediaMetaData(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> result;
        Media media = getCdmBaseInstance(uuid, response, MEDIA_INIT_STRATEGY);

        Set<MediaRepresentation> representations = media.getRepresentations();
        //get first representation and retrieve the according metadata

        Object[] repArray = representations.toArray();
        Object mediaRep = repArray[0];
        ModelAndView mv = new ModelAndView();
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
            HttpStatusMessage.fromString("Reading media file from " + uri.toString() + " failed").setStatusCode(400).send(response);
        }
        return mv;

    }

}
