// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

/**
 * @author n.hoffmann
 * @created Sep 23, 2010
 * @version 1.0
 */
public class UriUtils {
	private static final Logger logger = Logger.getLogger(UriUtils.class);
		
	/**
	 * see {@link #getInputStream(URI, Map)}
	 * 
	 * @param uri
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 */
	public static InputStream getInputStream(URI uri) throws IOException, HttpException{
		return getInputStream(uri, null);
	}
	
	/**
	 * Retrieves an {@link InputStream input stream} of the resource located at the given uri.
	 * 
	 * @param uri
	 * @return
	 * @throws IOException
	 * @throws HttpException 
	 */
	public static InputStream getInputStream(URI uri, Map<String, String> requestHeaders) throws IOException, HttpException{
		
		if(requestHeaders == null){
			requestHeaders = new HashMap<String, String>();
		}
		
		if (uri.getScheme().equals("http") || uri.getScheme().equals("https")){
			HttpResponse response = UriUtils.getResponse(uri, requestHeaders);
			if(UriUtils.isOk(response)){
	        	InputStream stream = response.getEntity().getContent();
	        	return stream;
	        } else {
	        	throw new HttpException("HTTP Reponse code is not = 200 (OK): " + UriUtils.getStatus(response));
	        }
		}else if (uri.getScheme().equals("file")){
			File file = new File(uri);
			return new FileInputStream(file);
		}else{
			throw new RuntimeException("Protokoll not yet handled: " + uri.getScheme()); 
		}
	}
	
	/**
	 * 
	 * @param response
	 * @return
	 */
	public static boolean isOk(HttpResponse response){
		return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
	}
	
	/**
	 * 
	 * @param response
	 * @return
	 * @throws IOException
	 */
	public static InputStream getContent(HttpResponse response) throws IOException{
		return response.getEntity().getContent();
	}
	
	public static String getStatus(HttpResponse response){
		StatusLine statusLine = response.getStatusLine();
		return "(" + statusLine.getStatusCode() + ")" + statusLine.getReasonPhrase();
	}
	
	/**
	 * Returns a {@link HttpResponse} object for given uri
	 * 
	 * @param uri
	 * @param requestHeaders
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static HttpResponse getResponse(URI uri, Map<String, String> requestHeaders) throws ClientProtocolException, IOException{
		// Create an instance of HttpClient.
		HttpClient  client = new DefaultHttpClient();
		
		HttpGet  method = new HttpGet(uri);
	    
        // configure the connection
        for(String key : requestHeaders.keySet()){
        	method.addHeader(key, requestHeaders.get(key));        	
        }
        
		//TODO  method.setFollowRedirects(followRedirects);

        logger.debug("sending GET request: " + uri);
        
        return client.execute(method);
	}
	
	public static URI createUri(URL baseUrl, String subPath, List<NameValuePair> qparams, String fragment) throws	URISyntaxException {
		
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
		
		if(qparams == null){
			qparams = new ArrayList<NameValuePair>(0);
		}

		URI uri = URIUtils.createURI(baseUrl.getProtocol(),
				baseUrl.getHost(), baseUrl.getPort(), path, URLEncodedUtils.format(qparams, "UTF-8"), fragment);

		return uri;
	}
}
