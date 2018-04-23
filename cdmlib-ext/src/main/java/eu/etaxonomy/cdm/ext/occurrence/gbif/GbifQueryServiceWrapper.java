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

import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.ext.common.ServiceWrapperBase;
import eu.etaxonomy.cdm.ext.occurrence.OccurenceQuery;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * This service provides access to GBIF web service API.<br>
 * It sends a {@link GbifQuery} via HTTP GET
 * @author pplitzner
 * @since 13.09.2013
 *
 */
public class GbifQueryServiceWrapper extends ServiceWrapperBase<SpecimenOrObservationBase<?>>{

    protected static final String BASE_URL = "http://api.gbif.org";
    private static final String SUB_PATH = "/v1/occurrence/search";

    /**
     * Constructs a new GbifQueryServiceWrapper
     */
    public GbifQueryServiceWrapper() {
        setBaseUrl(BASE_URL);
    }

    /**
     * Queries the GBIF API with the given {@link OccurenceQuery}.
     * @return The response as a collection of {@link GbifResponse}s or <code>null</code>
     * if no connection could be established
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
        if (queryParamsGET != null){
            URI uri = createUri(SUB_PATH, queryParamsGET);
            URIBuilder builder = new URIBuilder(uri.toString()+yearUri);

            if(UriUtils.isServiceAvailable(uri, 10000)){
                logger.info("Querying GBIF service with " + builder.build());
                return GbifJsonOccurrenceParser.parseJsonRecords(executeHttpGet(builder.build(), null));
            }
            else{
                logger.error("Querying " + uri + " got a timeout!");
                return null;
            }
        } else{
            logger.info("Querying GBIF service was skipped because of missing get parameters.");
            return null;
        }
    }

    /**
     * Queries GBIF for the original data set<br>
     * @param gbifResponse the GbifResponse holds the link to the dataset webservice
     * @return a {@link DataSetResponse} holding all relevant information to query the original provider
     * @throws IOException
     * @throws ClientProtocolException
     */
    public DataSetResponse queryOriginalDataSet(GbifResponse gbifResponse) throws ClientProtocolException, IOException{
        //FIXME move ABCD import here and change return type to DerivedUnitFacade/SpecimenOrObservationBase
        GbifDataSetProtocol dataSetProtocol = gbifResponse.getDataSetProtocol();
        if(dataSetProtocol == GbifDataSetProtocol.BIOCASE){
            DataSetResponse response = GbifJsonOccurrenceParser.parseOriginalDataSetUri(executeHttpGet(gbifResponse.getDataSetUri(), null));
            //the unitID is delivered in the "catalogNumber" parameter which is set as the accessionNumber of the facade
            response.setUnitId(gbifResponse.getDerivedUnitFacade().getAccessionNumber());
            return response;
        }else{
            DataSetResponse response = GbifJsonOccurrenceParser.parseOriginalDataSetUri(executeHttpGet(gbifResponse.getDataSetUri(), null));
            //the unitID is delivered in the "catalogNumber" parameter which is set as the accessionNumber of the facade
            response.setUnitId(gbifResponse.getDerivedUnitFacade().getAccessionNumber());
            return response;
        }

    }

}
