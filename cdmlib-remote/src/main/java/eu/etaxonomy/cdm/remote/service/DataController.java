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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.lsid.LSIDException;
import com.ibm.lsid.server.LSIDServerException;

import eu.etaxonomy.cdm.api.service.lsid.LSIDDataService;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.remote.editor.LSIDPropertyEditor;

/**
 * Controller which accepts requests for the data representation of an object
 * with a given lsid. The response is written directly into the request, rather
 * than being passed as part of the ModelAndView since data is supposed to be
 * byte-identical and thus cannot be transformed by the view layer.
 *
 * @author ben
 * @author Ben Szekely (<a href="mailto:bhszekel@us.ibm.com">bhszekel@us.ibm.com</a>)
 * @see com.ibm.lsid.server.servlet.DataServlet
 */
@Controller
@Api(value="lsid_authority_data",
description="Controller which accepts incoming requests to the LSIDDataService.")
public class DataController {

    private LSIDDataService lsidDataService;

    @Autowired
    public void setLsidDataService(LSIDDataService lsidDataService) {
        this.lsidDataService = lsidDataService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LSID.class, new LSIDPropertyEditor());
    }

    /**
     * Handle requests for the data representation of an object with a given lsid. Can return only part of the
     * data if the length and offset are specified in the request as per the specification.
     *
     * @param LSID lsid the lsid to retrieve data for
     * @param Integer start the offset in bytes to read from
     * @param Integer length the number of bytes to return
     * @return ModelAndView (null)
     * @throws LSIDServerException
     * @throws IOException
     */
    @RequestMapping(value = "/authority/data.do",params = {"lsid","start","length"}, method = RequestMethod.GET)
    public ModelAndView getData(@RequestParam("lsid") LSID lsid,
                                @RequestParam("start") Integer start,
                                @RequestParam("length") Integer length,
                                HttpServletResponse response) throws LSIDServerException, IOException  {
        //FIXME #3811 fix null pointer access of "out" reference
//		OutputStream out = null;
//		InputStream data = null;
//		try {
//			data = lsidDataService.getDataByRange(lsid,start,length);
//		    if(data != null) {
//		    	response.setContentType("application/octet-stream");
//		    	byte[] bytes = new byte[1024];
//		    	int numbytes = data.read(bytes);
//		    	while (numbytes != -1) {
//		    		out.write(bytes,0,numbytes);
//		    		numbytes = data.read(bytes);
//		    	}
//		    	out.flush();
//		    }
//		} finally {
//			if (out != null) {
//				out.close();
//			}
//
//			if (data != null) {
//				data.close();
//			}
//		}
        return null;
    }

    /**
     * Handle requests for the data representation of an object with a given lsid.
     *
     * @param LSID lsid the lsid to retrieve data for
     * @return ModelAndView (null)
     * @throws LSIDServerException
     * @throws IOException
     */
    @RequestMapping(value = "/authority/data.do",params = {"lsid"}, method = RequestMethod.GET)
    public ModelAndView getData(@RequestParam("lsid")LSID lsid,
                                HttpServletResponse response) throws LSIDServerException, IOException  {
        OutputStream out = null;
        InputStream data = null;
        try {
            data = lsidDataService.getData(lsid);
            if(data != null) {
                response.setContentType("application/octet-stream");
                out = response.getOutputStream();
                byte[] bytes = new byte[1024];
                int numbytes = data.read(bytes);
                while (numbytes != -1) {
                    out.write(bytes,0,numbytes);
                    numbytes = data.read(bytes);
                }
                out.flush();
            }
        } finally {
            if (out != null) {
                out.close();
            }

            if (data != null) {
                data.close();
            }
        }
        return null;
    }

    /**
     * Handle requests for the data representation of an object without an lsid.
     *
     * @throws LSIDServerException
     */
    @RequestMapping(value = "/authority/data.do", method = RequestMethod.GET)
    public ModelAndView getData() throws LSIDException {
        throw new LSIDException(LSIDException.INVALID_METHOD_CALL, "Must specify HTTP Parameter 'lsid'");
    }

}
