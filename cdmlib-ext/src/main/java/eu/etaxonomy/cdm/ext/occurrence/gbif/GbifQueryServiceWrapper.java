// $Id$
/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.occurrence.gbif;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;

import eu.etaxonomy.cdm.ext.common.ServiceWrapperBase;
import eu.etaxonomy.cdm.ext.occurrence.OccurenceQuery;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * This service provides access to GBIF web service API.<br>
 * It sends a {@link GbifQuery} via HTTP GET
 * @author pplitzner
 * @date 13.09.2013
 *
 */
public class GbifQueryServiceWrapper extends ServiceWrapperBase<SpecimenOrObservationBase<?>>{

    private static final String BASE_URL = "http://api.gbif.org";
    private static final String SUB_PATH = "/v0.9/occurrence/search";

    /**
     * Constructs a new GbifQueryServiceWrapper
     */
    public GbifQueryServiceWrapper() {
        setBaseUrl(BASE_URL);
    }

    /**
     * Queries the GBIF API with the given {@link OccurenceQuery}.
     * @return The response as an {@link InputStream}
     */
    public InputStream query(OccurenceQuery query) throws ClientProtocolException, IOException, URISyntaxException{
        List<NameValuePair> queryParamsGET = new GbifQueryGenerator().generateQueryParams(query);
        URI uri = createUri(SUB_PATH, queryParamsGET);

        return executeHttpGet(uri, null);
    }

}
