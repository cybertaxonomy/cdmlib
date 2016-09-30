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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;

import eu.etaxonomy.cdm.ext.common.ServiceWrapperBase;
import eu.etaxonomy.cdm.ext.occurrence.OccurenceQuery;

/**
 * Generates the query URL string for GBIF queries.
 * @author pplitzner
 * @date 13.09.2013
 *
 */
public class GbifQueryGenerator {

    /**
     * Generates the query URL string for GBIF queries.
     * @param query the {@link OccurenceQuery}
     * @param queryParamsGET
     * @return the query URL string
     */
    public List<NameValuePair> generateQueryParams(OccurenceQuery query){
        List<NameValuePair> queryParamsGET = new ArrayList<NameValuePair>();
        // only look for preserved specimens

        if (checkForValidQuery(query)) {
            //ServiceWrapperBase.addNameValuePairTo(queryParamsGET, "basisOfRecord", "PRESERVED_SPECIMEN");

            ServiceWrapperBase.addNameValuePairTo(queryParamsGET, "limit", "100");
            if (query.tripleIds != null){
                for (String[] tripleId:query.tripleIds){
                    ServiceWrapperBase.addNameValuePairTo(queryParamsGET, "CatalogNumber",tripleId[0]);
                    ServiceWrapperBase.addNameValuePairTo(queryParamsGET, "InstitutionCode",tripleId[1]);
                    ServiceWrapperBase.addNameValuePairTo(queryParamsGET, "CollectionCode", tripleId[2]);
                }
            }else{
                if((query.accessionNumber!=null && !query.accessionNumber.isEmpty()) ){
                    ServiceWrapperBase.addNameValuePairTo(queryParamsGET, "CatalogNumber", query.accessionNumber);


                }
                if(query.collector!=null && !query.collector.isEmpty()){
                    ServiceWrapperBase.addNameValuePairTo(queryParamsGET, "recordedBy", query.collector);
                }
                if(query.collectorsNumber!=null && !query.collectorsNumber.isEmpty()){
                    // TODO refine parameter
                    ServiceWrapperBase.addNameValuePairTo(queryParamsGET, "fieldNumber", query.collectorsNumber);
                    ServiceWrapperBase.addNameValuePairTo(queryParamsGET, "recordNumber", query.collectorsNumber);
                }
                if(query.country!=null && !query.country.isEmpty()){
                    ServiceWrapperBase.addNameValuePairTo(queryParamsGET, "country", query.country);
                }
                /**
                       Date Range: January – June 1899
                       http://api.gbif.org/v0.9/occurrence/search?basisOfRecord=preserved_specimen&scientificName=Campanula%20persicifolia&year=1899&month=1,6
                       Exact Date: june 20th 1901
                       http://api.gbif.org/v0.9/occurrence/search?basisOfRecord=preserved_specimen&scientificName=Campanula%20persicifolia&eventDate=1901-6-20
                       Date range exact: Jan. 02.1899 to june 03. 1902
                       http://api.gbif.org/v0.9/occurrence/search?basisOfRecord=preserved_specimen&scientificName=Campanula%20persicifolia&eventDate=1899-1-2,1902-6-3
                 */
                // FIXME: currently ONLY year is handled by the query (see GbifServiceWrapper)
                if(query.herbarium!=null && !query.herbarium.isEmpty()){
                    // TODO refine parameter
                    ServiceWrapperBase.addNameValuePairTo(queryParamsGET, "institutionCode", query.herbarium);
                }
                if(query.locality!=null && !query.locality.isEmpty()){
                    //TODO not yet available at GBIF
        //            ServiceWrapperBase.addNameValuePairTo(queryParamsGET, param, query.locality);
                }
                if(query.taxonName!=null && !query.taxonName.isEmpty()){
                    ServiceWrapperBase.addNameValuePairTo(queryParamsGET, "scientificName", query.taxonName);
                }
            }
            return queryParamsGET;
        }
        return null;
    }

    /**
     * @param query
     */
    private boolean checkForValidQuery(OccurenceQuery query) {
       boolean valid = false;
       if (query.tripleIds != null) {
           for (String[] tripleId:query.tripleIds){
               if (tripleId[0] != null || tripleId[1] != null || tripleId[2] != null){
                   valid = true;
               }
           }
       } else if((query.accessionNumber!=null && !query.accessionNumber.isEmpty()) ){
           valid = true;
       } else if (query.collector!=null && !query.collector.isEmpty()){
           valid = true;
       }else if(query.collectorsNumber!=null && !query.collectorsNumber.isEmpty()){
           valid = true;
       } else if (query.country!=null && !query.country.isEmpty()){
           valid = true;
       } else if(query.herbarium!=null && !query.herbarium.isEmpty()){
           valid = true;
       } else if (query.taxonName!=null && !query.taxonName.isEmpty()){
          valid = true;
       } else if (query.locality!=null && !query.locality.isEmpty()){
           //TODO not yet available at GBIF
       }

       return valid;
    }


}