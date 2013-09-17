// $Id$
/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.biocase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import eu.etaxonomy.cdm.ext.common.ServiceWrapperBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * @author pplitzner
 * @date 13.09.2013
 *
 */
public class BioCaseQueryServiceWrapper extends ServiceWrapperBase<SpecimenOrObservationBase>{
//    String content =
//          "<entry xmlns='http://www.w3.org/2005/Atom'>"
//          + "<content>" + comment + "</content>"
//          + "<category scheme='http://schemas.google.com/g/2005#kind'"
//          + " term='http://schemas.google.com/photos/2007#comment'/>"
//          + "</entry>";
//
//      try {
//          StringEntity entity = new StringEntity(content);
//          entity.setContentType(new BasicHeader("Content-Type",
//              "application/atom+xml"));
//          postRequest.setEntity(entity);

    /**
     * Constructs a new BioCaseServiceWrapper with the baseUrl: <i>http://ww3.bgbm.org/biocase/pywrapper.cgi</i>
     */
    public BioCaseQueryServiceWrapper() {
        setBaseUrl("http://ww3.bgbm.org/biocase/pywrapper.cgi");
        addSchemaAdapter(new BioCaseResponseSchemaAdapter());
    }

    public InputStream query() throws ClientProtocolException, IOException, URISyntaxException{
//        Document query = new BioCaseQueryGenerator().generateQuery();
//        String xmlOutputString = new XMLOutputter(Format.getPrettyFormat()).outputString(query);

        List<NameValuePair> queryParamsPOST = new ArrayList<NameValuePair>();
        addNameValuePairTo(queryParamsPOST, "Submit", "Submit");
        addNameValuePairTo(queryParamsPOST, "query", new BioCaseQueryGenerator().generateStringQuery());
        UrlEncodedFormEntity httpEntity = new UrlEncodedFormEntity(queryParamsPOST);
//        httpEntity.setContentType(new BasicHeader("Content-Type", "application/xml"));
        List<NameValuePair> queryParamsGET = new ArrayList<NameValuePair>();
        addNameValuePairTo(queryParamsGET, "dsa", "herbar");
        URI uri = createUri("/biocase/pywrapper.cgi", queryParamsGET);

        InputStream responseStream = executeHttpPost(uri, null, httpEntity);
//        InputStream responseStream = executeHttpPost(new URI("http", "ww3.bgbm.org", "/biocase/pywrapper.cgi" , "dsa=Herbar", null), null, httpEntity);
        new BioCaseResponseSchemaAdapter().getCmdEntities(responseStream);
        return responseStream;
    }
}
