/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.bci;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.lsid.MalformedLSIDException;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.ext.common.SchemaAdapterBase;
import eu.etaxonomy.cdm.ext.common.ServiceWrapperBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;


/**
* This service allows to query the Biodiversity collection index {@link http://www.biodiversitycollectionsindex.org}
* @author a.mueller
* @since Aug 16, 2010
* @version 1.0
 *
 */
@Component
public class BciServiceWrapper extends ServiceWrapperBase<Collection> implements IBciServiceWrapper{
	private static final Logger logger = Logger.getLogger(BciServiceWrapper.class);

	 private enum ServiceType{
		 AUTHOR,
		 NAME,
		 PUBLICATION,
	 }


//	private URL serviceUrl;

// ******************************** CONSTRUCTOR **************************************


//	/**
//	 * Creates new instance of this factory and connects it to the given
//	 * CDM Community Stores access point.
//	 *
//	 * Typically, there is no need to instantiate this class.
//	 */
//	protected IpniService(URL webserviceUrl){
//		this.serviceUrl = webserviceUrl;
//	}

// ****************************** METHODS ****************************************************/

	/**
	 *
	 * @param restRequest
	 * @return
	 */
	@Override
    public List<Collection> getCollectionsByCode(String code, ICdmRepository appConfig){

		SchemaAdapterBase<Collection> schemaAdapter = schemaAdapterMap.get("recordSchema");
		if(schemaAdapter == null){
			logger.error("No SchemaAdapter found for " + "recordSchema");
		}

		String SruOperation = "searchRetrieve";

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("code", SruOperation));

		Map<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put("Accept-Charset", "UTF-8");

		try {
			URI requestUri = createUri(null, pairs);


			InputStream stream = executeHttpGet(requestUri, requestHeaders);
			return schemaAdapter.getCmdEntities(stream);

		} catch (IOException e) {
			// thrown by doHttpGet
			logger.error(e);
		} catch (URISyntaxException e) {
			// thrown by createUri
			logger.error(e);
		}

//		return null;



		code = normalizeParameter(code);
		String request = code;

		return (List)queryService(request, appConfig, getServiceUrl(IBciServiceWrapper.LOOKUP_CODE_REST), ServiceType.AUTHOR);
	}


	/**
	 *
	 * @param restRequest
	 * @return
	*/
	private List<? extends IdentifiableEntity> queryService(String request, ICdmRepository appConfig, URL serviceUrl, ServiceType serviceType){
		try {
            // create the request url
            URL newUrl = new URL(serviceUrl.getProtocol(),
                                                     serviceUrl.getHost(),
                                                     serviceUrl.getPort(),
                                                     serviceUrl.getPath()
                                                     + "" + request);
            // open a connection
            HttpURLConnection connection = (HttpURLConnection) newUrl.openConnection();
            // set the accept property to XML so we can use jdom to handle the content
            //connection.setRequestProperty("Accept", "text/xml");


            logger.info("Firing request for URL: " + newUrl);

            int responseCode = connection.getResponseCode();

            // get the content at the resource
            InputStream content = (InputStream) connection.getContent();

            // build the result
            List<? extends IdentifiableEntity> result;
            if (serviceType.equals(ServiceType.AUTHOR)){
            	result = buildCollectionList(content, appConfig);
            }else if (serviceType.equals(ServiceType.NAME)){
            	//
            	result = null;
            }else{
            	//
            	result = null;
            }
            if(responseCode == HttpURLConnection.HTTP_OK){
                    return result;
            }else if(responseCode == HttpURLConnection.HTTP_MULT_CHOICE){
                    return result;
            }else{
                //TODO error handling
            	logger.error("No Http_OK");
            }

        } catch (IOException e) {
                logger.error("No content for request: " + request);
        }

        // error
        return null;
    }


	private List<Collection> buildCollectionList(InputStream content, ICdmRepository appConfig) throws IOException {
		List<Collection> result = new ArrayList<Collection>();
		BufferedReader reader = new BufferedReader (new InputStreamReader(content));

		String headerLine = reader.readLine();

		String line = reader.readLine();
		while (StringUtils.isNotBlank(line)){
			Collection collection = getCollectionFromLine(line, appConfig);
			result.add(collection);
			line = reader.readLine();
		}

		return result;
	}


	private Collection getCollectionFromLine(String line, ICdmRepository appConfig) {
		//urn:lsid:biocol.org:col:15727	http://biocol.org/urn:lsid:biocol.org:col:15727	University of Bergen Herbarium
		String[] splits = line.split("\t");
		if (splits.length != 3){
			logger.warn("Unknwon BCI line format: " + line);
			return null;
		}
		String lsidString = splits[0];
		String urlString = splits[1];
		String collectionName = splits[2];

		Collection result = Collection.NewInstance();

		//LSID
		LSID lsid = null;
		try {
			lsid = new LSID(lsidString);
		} catch (MalformedLSIDException e) {
			logger.warn("Malformed LSID " + lsidString, e);
		}

		result.setLsid(lsid);
		String id = getCollectionId(lsid);

		result.setName(collectionName);

		//id, citation
		Reference citation = getBciCitation(appConfig);
		result.addSource(OriginalSourceType.Lineage, id, null, citation, null);


		return result;
	}


	private String getCollectionId(LSID lsid) {
		String result = lsid == null? null : lsid.getObject();
		return result;
	}


	private Reference getBciCitation(ICdmRepository appConfig) {
		Reference bciReference;
		if (appConfig != null){
			bciReference = appConfig.getReferenceService().find(uuidBci);
			if (bciReference == null){
				bciReference = getNewBciReference();
				bciReference.setUuid(uuidBci);
				appConfig.getReferenceService().save(bciReference);
			}
		}else{
			bciReference = getNewBciReference();
		}
		return bciReference;
	}

	/**
	 * @return
	 */
	private Reference getNewBciReference() {
		Reference bciReference;
		bciReference = ReferenceFactory.newDatabase();
		bciReference.setTitleCache("Biodiversity Collection Index (BCI))");
		return bciReference;
	}


	/**
	 * @param parameter
	 */
	private String normalizeParameter(String parameter) {
		String result = CdmUtils.Nz(parameter).replace(" ", "+");
		return result;
	}



	/**
	 * The service url
	 *
	 * @return the serviceUrl
	 */
	@Override
    public URL getServiceUrl(String url) {
		URL serviceUrl;
		try {
			serviceUrl = new URL(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException("This should not happen", e);
		}
		return serviceUrl;
	}



}
