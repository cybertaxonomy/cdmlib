/**
 * 
 */
package eu.etaxonomy.cdm.io.excel.taxa;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.excel.common.ExcelImporterBase;
import eu.etaxonomy.cdm.io.jaxb.CdmImporter;
import eu.etaxonomy.cdm.io.jaxb.JaxbImportConfigurator;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.babadshanjan
 * @created 17.12.2008
 * @version 1.0
 */
public class NormalExplicitImporter extends ExcelImporterBase {

	private static final Logger logger = Logger.getLogger(NormalExplicitImporter.class);

	@Override
	protected boolean isIgnore(IImportConfigurator config) {
		return false;
	}
}
