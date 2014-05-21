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
        ServiceWrapperBase.addNameValuePairTo(queryParamsGET, "basisOfRecord", "PRESERVED_SPECIMEN");
        if(query.accessionNumber!=null || query.accessionNumber.isEmpty()){
//            ServiceWrapperBase.addNameValuePairTo(queryParamsGET, DSA_PARAM_NAME, query.accessionNumber);
        }
        if(query.collector!=null || query.collector.isEmpty()){
            ServiceWrapperBase.addNameValuePairTo(queryParamsGET, "recordedBy", query.collector);
        }
        if(query.collectorsNumber!=null || query.collectorsNumber.isEmpty()){
//            ServiceWrapperBase.addNameValuePairTo(queryParamsGET, DSA_PARAM_NAME, query.collectorsNumber);
        }
        if(query.country!=null || query.country.isEmpty()){
            ServiceWrapperBase.addNameValuePairTo(queryParamsGET, "country", query.country);
        }
        if(query.date!=null){
//            ServiceWrapperBase.addNameValuePairTo(queryParamsGET, DSA_PARAM_NAME, query.date);
        }
        if(query.herbarium!=null || query.herbarium.isEmpty()){
//            ServiceWrapperBase.addNameValuePairTo(queryParamsGET, DSA_PARAM_NAME, query.herbarium);
        }
        if(query.locality!=null || query.locality.isEmpty()){
            //TODO not yet available at GBIF
//            ServiceWrapperBase.addNameValuePairTo(queryParamsGET, param, query.locality);
        }
        if(query.taxonName!=null || query.taxonName.isEmpty()){
            ServiceWrapperBase.addNameValuePairTo(queryParamsGET, "scientificName", query.taxonName);
        }
        return queryParamsGET;
    }

}