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
import java.net.URI;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.kohlbecker
 * @since 24.08.2010
 *
 */
public abstract class SchemaAdapterBase<T extends CdmBase> {
	
	public static final Logger logger = Logger.getLogger(SchemaAdapterBase.class);
	
	/**
	 * @return the identifier e.g. "info:srw/schema/1/dc-v1.1" for DublinCore
	 */
	public abstract URI getIdentifier();


	/**
	 * @return the shortName e.g. "dc" for DublinCore
	 */
	public abstract String getShortName();

	/**
	 * @param inputStream
	 * @return
	 */
	public abstract List<T> getCmdEntities(InputStream inputStream) throws IOException;

}
