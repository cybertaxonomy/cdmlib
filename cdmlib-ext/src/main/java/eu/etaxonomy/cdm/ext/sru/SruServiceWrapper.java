// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.sru;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import eu.etaxonomy.cdm.ext.common.ServiceWrapperBase;
import eu.etaxonomy.cdm.ext.common.SchemaAdapterBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;


/**
 * @author a.kohlbecker
 * @date 24.08.2010
 *
 */
public class SruServiceWrapper extends ServiceWrapperBase<ReferenceBase> {
	
	//http://gso.gbv.de/sru/DB=2.1/?version=1.1&operation=searchRetrieve&query=pica.tit%3D%22harry+potter%22&recordSchema=pica
	
	private String SruVersion = "1.1";
	
	
	
	public List<ReferenceBase> doSearchRetrieve(String query, String recordSchema){
		
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		
		SchemaAdapterBase<ReferenceBase> schemaAdapter = schemaAdapterMap.get(recordSchema);
		if(schemaAdapter == null){
			logger.error("No SchemaAdapter found for " + recordSchema);
		}
		
		String SruOperation = "searchRetrieve";
		
		pairs.add(new BasicNameValuePair("operation", SruOperation));
		pairs.add(new BasicNameValuePair("version", SruVersion));
		pairs.add(new BasicNameValuePair("query", query));
		pairs.add(new BasicNameValuePair("recordSchema", recordSchema));
		
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
		} catch (HttpException e) {
			// thrown by executeHttpGet
			logger.error(e);
		} 
		
		return null;
		
	}
	
	
	
}
