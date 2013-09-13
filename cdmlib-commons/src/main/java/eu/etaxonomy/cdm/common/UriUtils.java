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
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
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
			throw new RuntimeException("Protocol not handled yet: " + uri.getScheme()); 
		}
	}
	
	/**
	 * Retrieves the size of the resource defined by the given uri in bytes 
	 * 
	 * @param uri the resource
	 * @param requestHeaders additional headers. May be <code>null</code>
	 * @return the size of the resource in bytes
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws HttpException
	 */
	public static long getResourceLength(URI uri, Map<String, String> requestHeaders) throws ClientProtocolException, IOException, HttpException{
		if(requestHeaders == null){
			requestHeaders = new HashMap<String, String>();
		}
		
		if (uri.getScheme().equals("http") || uri.getScheme().equals("https")){
			HttpResponse response = UriUtils.getResponse(uri, requestHeaders);
			if(UriUtils.isOk(response)){
	        	Header[] contentLengths = response.getHeaders("Content-Length");
	        	
	        	if(contentLengths == null || contentLengths.length == 0){
	        		throw new HttpException("Could not retrieve Content-Length");
	        	}
	        	
	        	if(contentLengths.length > 1){
	        		throw new HttpException("Multiple Conten-Length headers sent");
	        	}
	        	
	        	Header contentLength = contentLengths[0];
	        	String value = contentLength.getValue();
	        	
	        	return Long.valueOf(value);
	        	
	        } else {
	        	throw new HttpException("HTTP Reponse code is not = 200 (OK): " + UriUtils.getStatus(response));
	        }
		}else if (uri.getScheme().equals("file")){
			File file = new File(uri);
			return file.length();
		}else{
			throw new RuntimeException("Protocol not handled yet: " + uri.getScheme()); 
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
        if(requestHeaders != null){
	       	for(String key : requestHeaders.keySet()){
	        	method.addHeader(key, requestHeaders.get(key));        	
	        }
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
		String query = null;
		if(! qparams.isEmpty()){
			query = URLEncodedUtils.format(qparams, "UTF-8");
		}
		
		URI uri = URIUtils.createURI(baseUrl.getProtocol(),
				baseUrl.getHost(), baseUrl.getPort(), path, query, fragment);

		return uri;
	}
	
	/**
	 * Tests internet connectivity by testing HEAD request for 4 known URL's.<BR>
	 * If non of them is available <code>false</code> is returned. Otherwise true.<BR> 
	 * @param firstUriToTest if not <code>null</code> this URI is tested before testing the standard URLs.
	 * @return true if internetconnectivity is given.
	 */
	public static boolean isInternetAvailable(URI firstUriToTest){
		boolean result = false;
		if (firstUriToTest != null && isServiceAvailable(firstUriToTest)){
			return true;
		}
		
		URI uri = URI.create("http://www.cnn.com/");
		if (isServiceAvailable(uri)){
			return true;
		}
		uri = URI.create("http://www.bahn.de/");
		if (isServiceAvailable(uri)){
			return true;
		}
		uri = URI.create("http://www.google.com/");
		if (isServiceAvailable(uri)){
			return true;
		}
		uri = URI.create("http://www.facebook.com/");
		if (isServiceAvailable(uri)){
			return true;
		}
		
		return result;
	}
	
	/**
	 * Performs HEAD request for the given URI.<BR>
	 * If any exception occurs <code>false</code> is returned. Otherwise true. <BR>
	 * @param serviceUri the URI to test.
	 * @return true if service is available.
	 */
	public static boolean isServiceAvailable(URI serviceUri){
		boolean result = false;
		
        //Http 
		HttpClient  client = new DefaultHttpClient();
		HttpUriRequest request = new HttpHead(serviceUri);
		 
		try {
			// Execute the request
			HttpResponse response = client.execute(request);
			// Examine the response status
			if (logger.isDebugEnabled()){
				logger.debug(response.getStatusLine());
			}
			 result = true;
			 
		} catch (UnknownHostException e1) {
			logger.info("Unknwon Host: " +e1.getMessage());
		} catch (ClientProtocolException e2) {
			logger.info("ClientProtocolException: " + e2.getMessage());
		} catch (IOException e3) {
			logger.info("IOException: " + e3.getMessage());
		}
	     
	     // When HttpClient instance is no longer needed, 
	     // shut down the connection manager to ensure
	     // immediate deallocation of all system resources
		//needed ?
//	     client.getConnectionManager().shutdown();   

		return result;
	}
	
	/**
	 * Tests reachability of a root server by trying to resolve a host name.
	 * @param hostNameToResolve the host name to resolve. If <code>null</code> 
	 * a default host name is tested.
	 * @return
	 */
	public static boolean isRootServerAvailable(String hostNameToResolve){
		try {
			if (hostNameToResolve == null){
				hostNameToResolve = "cnn.com";
			}
		    InetAddress inetHost = InetAddress.getByName(hostNameToResolve);
		    logger.debug("The hosts IP address is: " + inetHost.getHostAddress());
			return true;
		 } catch(UnknownHostException ex) {
			 logger.info("Unrecognized host");
		     return false;
		 }
	}
	
	//from http://www.javabeginners.de/Netzwerk/File_zu_URL.php
	public static URL fileToURL(File file){
        URL url = null;
        try {
        	// Sonderzeichen (z.B. Leerzeichen) bleiben erhalten
            url = new URL("file://" + file.getPath());
            // Sonderzeichen (z.B. Leerzeichen) werden codiert
            url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    } 
	
	//from http://blogs.sphinx.at/java/erzeugen-von-javaiofile-aus-javaneturl/	
	public static File urlToFile(URL url) {
        URI uri;
        try {
            // this is the step that can fail, and so
            // it should be this step that should be fixed
            uri = url.toURI();
        } catch (URISyntaxException e) {
            // OK if we are here, then obviously the URL did
            // not comply with RFC 2396. This can only
            // happen if we have illegal unescaped characters.
            // If we have one unescaped character, then
            // the only automated fix we can apply, is to assume
            // all characters are unescaped.
            // If we want to construct a URI from unescaped
            // characters, then we have to use the component
            // constructors:
            try {
                uri = new URI(url.getProtocol(), url.getUserInfo(), url
                        .getHost(), url.getPort(), url.getPath(), url
                        .getQuery(), url.getRef());
            } catch (URISyntaxException e1) {
                throw new IllegalArgumentException("broken URL: " + url);
            }
        }
        return new File(uri);
    }
}
