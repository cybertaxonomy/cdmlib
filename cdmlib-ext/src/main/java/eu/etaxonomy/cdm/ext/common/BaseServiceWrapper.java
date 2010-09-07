// $Id$
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

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.kohlbecker
 * @date 24.08.2010
 *
 */
public class BaseServiceWrapper<T extends CdmBase> {
	
	public static final Logger logger = Logger.getLogger(BaseServiceWrapper.class);
	
	protected URL baseUrl;
	
	protected boolean followRedirects = true;

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
	
	protected InputStream executeHttpGet(URI uri, Map<String, String> requestHeaders) throws HttpException, IOException{
		
		// Create an instance of HttpClient.
		HttpClient  client = new DefaultHttpClient();

		HttpGet  method = new HttpGet(uri);
	    
        // configure the connection
        for(String key : requestHeaders.keySet()){
        	method.addHeader(key, requestHeaders.get(key));        	
        }
        
		//TODO  method.setFollowRedirects(followRedirects);

        logger.debug("sending GET request: " + uri);
        
        HttpResponse response = client.execute(method);

        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
        	InputStream stream = response.getEntity().getContent();;
        	return stream;
        } else {
        	logger.error("HTTP Reponse code is not = 200 (OK): " + response.getStatusLine().getStatusCode());
        	return null;
        }
	}
	

	protected URI createUri(String subPath, List<NameValuePair> qparams) throws	URISyntaxException {
		
		String path = baseUrl.getPath();
		if(subPath != null){
			if(!path.endsWith("/")){
				path += "/";
			}
			if(subPath.startsWith("/")){
				subPath = subPath.substring(1);
			}
			path += subPath;
		}

		URI uri = URIUtils.createURI(baseUrl.getProtocol(),
				baseUrl.getHost(), baseUrl.getPort(), path, URLEncodedUtils.format(qparams, "UTF-8"), null);

		return uri;
	}


}
