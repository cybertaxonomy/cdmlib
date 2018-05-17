/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.common.UriUtils.HttpMethod;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.kohlbecker
 * @since 24.08.2010
 *
 */
public class ServiceWrapperBase<T extends CdmBase> {

	public static final Logger logger = Logger.getLogger(ServiceWrapperBase.class);

	private URL baseUrl;

	private final boolean followRedirects = true;

	protected Map<String, SchemaAdapterBase<T>> schemaAdapterMap;

	/**
	 * @param baseUrl the baseUrl to set
	 */
	public void setBaseUrl(String baseUrl) {
		try {
			this.baseUrl = new URL(baseUrl);
		} catch (MalformedURLException e) {
			logger.error(e);
		}
	}

	/**
	 * @return the baseUrl
	 */
	public String getBaseUrl() {
		return baseUrl.toString();
	}

	/**
	 * @param schemaAdapterMap the schemaAdapterMap to set
	 */
	public void setSchemaAdapterMap(Map<String, SchemaAdapterBase<T>> schemaAdapterMap) {
		this.schemaAdapterMap = schemaAdapterMap;
	}

	public void addSchemaAdapter(SchemaAdapterBase schemaAdapter){
		if(schemaAdapterMap == null){
			schemaAdapterMap = new HashMap<String, SchemaAdapterBase<T>>();
		}
		schemaAdapterMap.put(schemaAdapter.getShortName(), schemaAdapter);
	}


	/**
	 * @return the schemaAdapterMap
	 */
	public Map<String, SchemaAdapterBase<T>> getSchemaAdapterMap() {
		return schemaAdapterMap;
	}

    /**
     * Send an HTTP GET request to the given URI.
     * @param uri the URI of this HTTP request
     * @param requestHeaders the parameters (name-value pairs) of the connection added to the header of the request
     * @return the response as an {@link InputStream}
     * @throws ClientProtocolException
     * @throws IOException
     */
	protected InputStream executeHttpGet(URI uri, Map<String, String> requestHeaders) throws ClientProtocolException, IOException{
        return executeHttp(uri, requestHeaders, HttpMethod.GET, null);
	}

	/**
	 * Send an HTTP POST request to the given URI.
     * @param uri the URI of this HTTP request
     * @param requestHeaders the parameters (name-value pairs) of the connection added to the header of the request
     * @param entity the {@link HttpEntity} attached to a HTTP POST request
     * @return the response as an {@link InputStream}
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	protected InputStream executeHttpPost(URI uri, Map<String, String> requestHeaders, HttpEntity httpEntity) throws ClientProtocolException, IOException{
	    return executeHttp(uri, requestHeaders, HttpMethod.POST, httpEntity);
	}

    /**
     * @param uri the URI of this HTTP request
     * @param requestHeaders the parameters (name-value pairs) of the connection added to the header of the request
     * @param httpMethod defines if method is POST or GET
     * @param entity the {@link HttpEntity} attached to a HTTP POST request
     * @return the response as an {@link InputStream}
     * @throws IOException
     * @throws ClientProtocolException
     */
    private InputStream executeHttp(URI uri, Map<String, String> requestHeaders, HttpMethod httpMethod, HttpEntity entity) throws IOException, ClientProtocolException {
        logger.debug("sending "+httpMethod+" request: " + uri);

	    HttpResponse response = UriUtils.getResponseByType(uri, requestHeaders, httpMethod, entity);

	    if(UriUtils.isOk(response)){
	        InputStream stream = response.getEntity().getContent();
	        return stream;
	    } else {
	        logger.error("HTTP Reponse code is not = 200 (OK): " + UriUtils.getStatus(response));
	        return null;
	    }
    }

    /**
     * Adds a {@link BasicNameValuePair} to the given {@link List}.
     * @param listOfPairs the list to add the name-value pair to
     * @param name the name
     * @param value the value
     */
	public static void addNameValuePairTo(List<NameValuePair> listOfPairs, String name, String value){
		if(value != null){
		    listOfPairs.add(new BasicNameValuePair(name, value));
		}
	}

	/**
     * Adds a {@link BasicNameValuePair} to the given {@link List}.
     * @param listOfPairs the list to add the name-value pair to
     * @param name the name
     * @param value the String representation of the object (toString())
     */
	public static void addNameValuePairTo(List<NameValuePair> listOfPairs, String name, Object value){
		if(value != null){
		    listOfPairs.add(new BasicNameValuePair(name, value.toString()));
		}
	}


	/**
	 * Creates a {@link URI} based on the {@link ServiceWrapperBase#baseUrl} and the given subPath and qParams
	 * @param subPath the sub path of the URI to be created
	 * @param qparams the parameters added as GET parameters to the URI
	 * @return a URI consisting of the baseURL, the subPath and qParams
	 * @throws URISyntaxException
	 */
	protected URI createUri(String subPath, List<NameValuePair> qparams) throws	URISyntaxException {

		return UriUtils.createUri(baseUrl, subPath, qparams, null);

//		String path = baseUrl.getPath();
//		if(subPath != null){
//			if(!path.endsWith("/")){
//				path += "/";
//			}
//			if(subPath.startsWith("/")){
//				subPath = subPath.substring(1);
//			}
//			path += subPath;
//		}
//
//		URI uri = URIUtils.createURI(baseUrl.getProtocol(),
//				baseUrl.getHost(), baseUrl.getPort(), path, URLEncodedUtils.format(qparams, "UTF-8"), null);
//
//		return uri;
	}


}
