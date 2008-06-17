package eu.etaxonomy.cdm.jaxb;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;

public class CdmResourceResolver implements LSResourceResolver {
	private final CatalogResolver catalogResolver;
	private static Log log = LogFactory.getLog(CdmResourceResolver.class);

	public CdmResourceResolver() throws IOException {
		Properties properties = new Properties();
		if(log.isInfoEnabled()) {
		    log.info("Loading /CatalogManager.properties");
		}
		properties.load(this.getClass().getResourceAsStream("/CatalogManager.properties"));
		
		String[] catalogFileNames = properties.getProperty("catalogs").split(",");
		
		CatalogManager catalogManager = new CatalogManager();
		catalogManager.setRelativeCatalogs(true);
		catalogResolver = new CatalogResolver(catalogManager);
		
		for(String catalogFileName : catalogFileNames) {
			if(log.isInfoEnabled()) {
			    log.info("Parsing " + catalogFileName);
			}
			catalogResolver.getCatalog().parseCatalog(this.getClass().getResource(catalogFileName));
		}
	}

	public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
		
		if(log.isInfoEnabled()) {
		    log.info("Resolving " + namespaceURI + " with systemId " + systemId + ", trying " + systemId);
		}
		InputSource inputSource = catalogResolver.resolveEntity( publicId, systemId );
		if ( inputSource == null ) {
			if(log.isInfoEnabled()) {
			    log.info("Not found in filesystem: Looking in jar files for /eu/etaxonomy/cdm/model/" + systemId);
			}
			inputSource = new InputSource(this.getClass().getResourceAsStream("/eu/etaxonomy/cdm/model/" + systemId));
			
			if(inputSource == null) {
				if(log.isWarnEnabled()) {
					log.warn(namespaceURI + " not found");
				}
				return null;
			}
		}
		
		if(log.isInfoEnabled()) {
		    log.info("Resource found");
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
