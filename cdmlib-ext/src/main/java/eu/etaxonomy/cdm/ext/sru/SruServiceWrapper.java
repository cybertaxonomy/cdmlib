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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import eu.etaxonomy.cdm.ext.common.SchemaAdapterBase;
import eu.etaxonomy.cdm.ext.common.ServiceWrapperBase;
import eu.etaxonomy.cdm.model.reference.Reference;


/**
 * @author a.kohlbecker
 * @since 24.08.2010
 *
 */
public class SruServiceWrapper extends ServiceWrapperBase<Reference> {

	private String sruVersion = "1.1";

	/**
	 * The GRIB sru service is available at "http://gso.gbv.de/sru/DB=1.83/"
	 * The documentation is found at http://bhleurope.gbv.de/#sru from where the following text has been retrieved:
	 * <p>
	 * General information about Search/Retrieve via URL (SRU) is available in
	 * the official SRU specification (http://www.loc.gov/standards/sru/). The SRU-Interface of GRIB supports some
	 * specific search keys. Please do not use the (dc) fields but only the
	 * (pica) search fields:
	 * <dl>
	 * <dt>PPN</dt>
	 * <dd>Internal record id without prefix 'grib:ppn:' (This may change)</dd>
	 * <dt>DST</dt>
	 * <dd>Digitization status (8300-8305: 8300=not digitized, 8301=should be digitized, 8302=will be digitized, 8305=document available)</dd>
	 * <dt>URL</dt>
	 * <dd>URL of a digitized object</dd>
	 * <dt>??? (not defined yet)</td>
	 * <dd>Stable identifier of a record (PICA+ field 006Y)</dd>
	 * </dl>
	 * </p>
	 * @param cqlQuery
	 *            an <b>URL encoded</b> CQL Query string see
	 *            {@link http://www.loc.gov/standards/sru/specs/cql.html} for documentation
	 * @param recordSchema
	 * @return
	 */
	public List<Reference> doSearchRetrieve(String cqlQuery, String recordSchema){

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();

		SchemaAdapterBase<Reference> schemaAdapter = schemaAdapterMap.get(recordSchema);
		if(schemaAdapter == null){
			logger.error("No SchemaAdapter found for " + recordSchema);
		}

		String sruOperation = "searchRetrieve";

		pairs.add(new BasicNameValuePair("operation", sruOperation));
		pairs.add(new BasicNameValuePair("version", sruVersion));
		pairs.add(new BasicNameValuePair("query", cqlQuery));
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
		}

		return null;

	}



}
