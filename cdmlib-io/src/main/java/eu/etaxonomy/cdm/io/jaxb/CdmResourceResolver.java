/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.jaxb;

import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;

/**
 * Copied from CATE (@author ben.clark). Not used at this point.
 */
public class CdmResourceResolver implements LSResourceResolver {

    private static final Logger logger = LogManager.getLogger();

    private final CatalogResolver catalogResolver;

	public CdmResourceResolver() throws IOException {
		Properties properties = new Properties();
		if(logger.isInfoEnabled()) {
		    logger.info("Loading /CatalogManager.properties");
		}
		properties.load(this.getClass().getResourceAsStream("/CatalogManager.properties"));

		String[] catalogFileNames = properties.getProperty("catalogs").split(",");

		CatalogManager catalogManager = new CatalogManager();
		catalogManager.setRelativeCatalogs(true);
		catalogResolver = new CatalogResolver(catalogManager);

		for(String catalogFileName : catalogFileNames) {
			if(logger.isInfoEnabled()) {
			    logger.info("Parsing " + catalogFileName);
			}
			catalogResolver.getCatalog().parseCatalog(this.getClass().getResource(catalogFileName));
		}
	}

	@Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {

		if(logger.isInfoEnabled()) {
		    logger.info("Resolving " + namespaceURI + " with systemId " + systemId + ", trying " + systemId);
		}
		InputSource inputSource = catalogResolver.resolveEntity( publicId, systemId );
		if ( inputSource == null ) {
			if(logger.isInfoEnabled()) {
			    logger.info("Not found in filesystem: Looking in jar files for /schema/cdm/" + systemId);
			}
			inputSource = new InputSource(this.getClass().getResourceAsStream("/schema/cdm/" + systemId));

			if(inputSource == null) {
				if(logger.isWarnEnabled()) {
					logger.warn(namespaceURI + " not found");
				}
				return null;
			}
		}

		if(logger.isInfoEnabled()) {
		   // log.info("Resource found");
		}
		LsInputImpl lsInput = new LsInputImpl();
		lsInput.setByteStream( inputSource.getByteStream() );
		lsInput.setCharacterStream( inputSource.getCharacterStream() );
		lsInput.setPublicId( inputSource.getPublicId() );
		lsInput.setSystemId( inputSource.getSystemId() );
		lsInput.setEncoding( inputSource.getEncoding() );
		return lsInput;
	}

}
