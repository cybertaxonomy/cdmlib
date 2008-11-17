/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.common;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;

/**
 * @author a.babadshanjan
 * @created 16.11.2008
 */
public interface IExportConfigurator extends IIoConfigurator {

	public static enum CHECK{
		CHECK_ONLY,
		EXPORT_WITHOUT_CHECK,
		CHECK_AND_EXPORT,
	}
	
	public static enum DO_REFERENCES{
		NONE,
		NOMENCLATURAL,
		CONCEPT_REFERENCES,
		ALL
	}

	public abstract boolean isValid();

	/**
	 * A String representation of the destination (e.g. CDM JAXB XML)
	 * @return
	 */
	public abstract String getDestinationNameString();

	public abstract CHECK getCheck();
	
	public Class<ICdmIoExport>[] getIoClassList();

	/**
	 * Returns a <code>CdmApplicationController</code> created by the values of this configuration.
	 * If a controller was already created before the last created controller is returned.
	 * @return
	 */
	public CdmApplicationController getCdmAppController();
}
