// $Id$
/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.occurrence.bioCase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.ext.common.ServiceWrapperBase;
import eu.etaxonomy.cdm.ext.occurrence.OccurenceQuery;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;

/**
 * This service provides access to BioCASe providers.<br>
 * It sends a {@link OccurenceQuery} via HTTP POST to a given provider
 * @author pplitzner
 * @date 13.09.2013
 *
 */
public class BioCaseQueryServiceWrapper extends ServiceWrapperBase<SpecimenOrObservationBase>{

    private final BioCaseResponseSchemaAdapter schemaAdapter;

    private static final String BASE_URL = "http://ww3.bgbm.org";
    private static final String SUB_PATH = "/biocase/pywrapper.cgi";
    //capability testing
    //http://ww3.bgbm.org/biocase/pywrapper.cgi?dsa=Herbar&capabilities=1
    private static final BasicNameValuePair SUBMIT_PARAM = new BasicNameValuePair("Submit", "Submit");
    private static final String QUERY_PARAM_NAME = "query";
    private static final String DSA_PARAM_NAME = "dsa";
    private static final BasicNameValuePair CAPABILITY_TEST_PARAM = new BasicNameValuePair("capabilities", "1");

    /**
     * Constructs a new BioCaseServiceWrapper with the baseUrl: <i>http://ww3.bgbm.org/biocase/pywrapper.cgi</i>
     */
    public BioCaseQueryServiceWrapper() {
        setBaseUrl(BASE_URL);
        schemaAdapter = new BioCaseResponseSchemaAdapter();
        addSchemaAdapter(schemaAdapter);
    }

    public InputStream query(OccurenceQuery query) throws ClientProtocolException, IOException, URISyntaxException{
        //TODO: remove default herbar querying. Maybe make other query method public
        return query(query, "herbar");
    }


    /**
     * Queries the BioCASE provider with the given {@link OccurenceQuery}.
     * @return The response as an {@link InputStream}
     */
    private InputStream query(OccurenceQuery query, String dsaName) throws ClientProtocolException, IOException, URISyntaxException{
        Document doc = new BioCaseQueryGenerator().generateXMLQuery(query);
        String xmlOutputString = new XMLOutputter(Format.getPrettyFormat()).outputString(doc);

        //POST
        List<NameValuePair> queryParamsPOST = new ArrayList<NameValuePair>();
        queryParamsPOST.add(SUBMIT_PARAM);
        addNameValuePairTo(queryParamsPOST, QUERY_PARAM_NAME, xmlOutputString);
        UrlEncodedFormEntity httpEntity = new UrlEncodedFormEntity(queryParamsPOST);
        //GET
        List<NameValuePair> queryParamsGET = new ArrayList<NameValuePair>();
        addNameValuePairTo(queryParamsGET, DSA_PARAM_NAME, dsaName);
        URI uri = createUri(SUB_PATH, queryParamsGET);


        logger.info("Querying BioCASE service with " + uri + ", POST: " + httpEntity);
        return executeHttpPost(uri, null, httpEntity);
//        InputStream responseStream = executeHttpPost(new URI("http", "ww3.bgbm.org", "/biocase/pywrapper.cgi" , "dsa=Herbar", null), null, httpEntity);
//        try {
//            results = schemaAdapter.getCmdEntities(responseStream);
//        } catch (ClientProtocolException e) {
//            logger.error(e);
//        } catch (IOException e) {
//            logger.error(e);
//        }
//        return results;
    }

    public List<SpecimenOrObservationBase> dummyData(){
        List<SpecimenOrObservationBase> results = new ArrayList<SpecimenOrObservationBase>();
        DerivedUnitFacade unit1 = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
//        newInstance.setTitleCache("Test Specimen", true);
        unit1.setCollectingMethod("Collected from the ground");
        unit1.setAccessionNumber("ACC-12345");
        unit1.setLocality("locality");
        unit1.setCountry(NamedArea.EUROPE());

        DerivedUnitFacade unit2 = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        unit1.setTitleCache("Test Specimen 2", false);
        unit2.setCollectingMethod("Collected from the ground");
        unit2.setAccessionNumber("ACC-67890");
        unit2.setLocality("solid ground close to car park");
        unit2.setCountry(NamedArea.EUROPE());
        results.add(unit1.innerDerivedUnit());
        results.add(unit2.innerDerivedUnit());
        return results;
    }
}
