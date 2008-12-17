/**
 * 
 */
package eu.etaxonomy.cdm.io.excel.common;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IIoConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.excel.taxa.NormalExplicitImporter;
import eu.etaxonomy.cdm.io.jaxb.JaxbImportConfigurator;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.babadshanjan
 * @created 17.12.2008
 * @version 1.0
 */
public abstract class ExcelImporterBase extends CdmIoBase<IImportConfigurator> {

	private static final Logger logger = Logger.getLogger(ExcelImporterBase.class);


	public URI string2Uri() {
		return null;
	}
	
	
	/** Reads data from an Excel file and stores them into a CDM DB.
     * 
     * @param config
     * @param stores (not used)
     */
	@Override
	protected boolean doInvoke(IImportConfigurator config,
			Map<String, MapWrapper<? extends CdmBase>> stores) {
		
		boolean success = true;
        URI uri = null;
		JaxbImportConfigurator jaxbImpConfig = (JaxbImportConfigurator)config;
    	String dbname = jaxbImpConfig.getDestination().getDatabase();
    	
    	String urlFileName = (String)config.getSource();
		logger.debug("urlFileName: " + urlFileName);
    	try {
    		uri = new URI(urlFileName);
			logger.debug("uri: " + uri.toString());
    	} catch (URISyntaxException ex) {
			logger.error("File name problem");
			return false;
    	}
    	
    	return success;
	}
	
    	
	@Override
	protected boolean doCheck(IImportConfigurator config) {
		boolean result = true;
		logger.warn("No check implemented for Excel import");
		return result;
	}
}
