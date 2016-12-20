/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.service;

import io.swagger.annotations.Api;

import java.util.StringTokenizer;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.lsid.LSIDException;
import com.ibm.lsid.MetadataResponse;
import com.ibm.lsid.server.LSIDServerException;

import eu.etaxonomy.cdm.api.service.lsid.LSIDMetadataService;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.remote.editor.LSIDPropertyEditor;

/**
 * Controller which accepts requests for the metadata representation of an object
 * with a given lsid.
 *
 * @author ben
 * @author Ben Szekely (<a href="mailto:bhszekel@us.ibm.com">bhszekel@us.ibm.com</a>)
 * @see com.ibm.lsid.server.servlet.MetadataServlet
 */
@Controller
@Api(value="lsid_authority_metadata",
description="Controller which accepts incoming requests to the LSIDMetadataService.")
public class MetadataController {

    private LSIDMetadataService lsidMetadataService;

    @Autowired
    public void setLsidMetadataService(LSIDMetadataService lsidMetadataService) {
        this.lsidMetadataService = lsidMetadataService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LSID.class, new LSIDPropertyEditor());
    }

    /**
     * Handle requests for the metadata representation of an object with a given lsid. Will return metadata in any format supported
     * from a list of formats if specified.
     *
     * @param lsid the lsid to get metadata for
     * @param formats a comma separated list of acceptable formats to the client
     * @return ModelAndView containing the metadata response as an object with key 'metadataResponse', view name 'Metadata.rdf'
     * @throws LSIDServerException
     */
    @RequestMapping(value = "/authority/metadata.do", params = "lsid", method = RequestMethod.GET)
    public ModelAndView getMetadata(@RequestParam("lsid") LSID lsid,
                                    @RequestParam(value = "acceptedFormats", required = false) String formats) throws LSIDServerException  {
        String[] acceptedFormats = null;
        if (formats != null) {
            StringTokenizer st = new StringTokenizer(formats,",",false);
            Vector<String> v = new Vector<String>();
            while (st.hasMoreTokens()) {
                v.add(st.nextToken());
            }
            acceptedFormats = new String[v.size()];
            v.toArray(acceptedFormats);
        }

        if (acceptedFormats != null) {
            boolean found = false;
            for (int i=0;i<acceptedFormats.length;i++) {
                    if (acceptedFormats[i].equals(MetadataResponse.RDF_FORMAT )) {
                        found = true;
                    }
                    break;
            }
            if (!found) {
                    throw new LSIDServerException(LSIDServerException.NO_METADATA_AVAILABLE_FOR_FORMATS,"No metadata found for given format");
            }
        }

        IIdentifiableEntity identifiableEntity = lsidMetadataService.getMetadata(lsid);
        ModelAndView modelAndView = new ModelAndView("Metadata.rdf");
        modelAndView.addObject(identifiableEntity);
        return modelAndView;
    }

    @RequestMapping(value = "/authority/metadata.do", params = "!lsid", method = RequestMethod.GET)
    public ModelAndView getMetadata() throws LSIDException {
        throw new LSIDException(LSIDException.INVALID_METHOD_CALL, "Must specify HTTP Parameter 'lsid'");
    }

}
