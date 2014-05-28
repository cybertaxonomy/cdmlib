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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.utils.URIBuilder;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
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

    protected static final String BASE_URL = "http://api.gbif.org";
    private static final String SUB_PATH = "/v0.9/occurrence/search";

    /**
     * Constructs a new GbifQueryServiceWrapper
     */
    public GbifQueryServiceWrapper() {
        setBaseUrl(BASE_URL);
    }

    /**
     * Queries the GBIF API with the given {@link OccurenceQuery}.
     * @return The response as a collection of {@link DerivedUnitFacade}
     */
    public Collection<GbifResponse> query(OccurenceQuery query) throws ClientProtocolException, IOException, URISyntaxException{
        //TODO: workaround for special case for "eventDate" which can have comma separated values
        String yearUri = "";
        if(query.dateFrom!=null){
            yearUri = "&year="+query.dateFrom.get(Calendar.YEAR);
            if(query.dateTo!=null){
                yearUri += ","+query.dateTo.get(Calendar.YEAR);
            }
            //TODO we skip month range query because it only checks for the month range ignoring the year range
            //TODO date is not supported by GBIF
        }
        List<NameValuePair> queryParamsGET = new GbifQueryGenerator().generateQueryParams(query);
        URI uri = createUri(SUB_PATH, queryParamsGET);

        URIBuilder builder = new URIBuilder(uri.toString()+yearUri);

        logger.info("Querying GBIF service with " + builder.build());
        return GbifJsonOccurrenceParser.parseJsonRecords(executeHttpGet(builder.build(), null));
    }

}
