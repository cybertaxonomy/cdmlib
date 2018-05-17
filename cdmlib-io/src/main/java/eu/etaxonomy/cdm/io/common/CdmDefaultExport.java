/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * This is an exporter that invokes the application aware defaultExport when invoked itself
 * @author a.babadshanjan
 * @since 17.11.2008
 */
public class CdmDefaultExport<T extends IExportConfigurator>
            extends CdmDefaultIOBase<IExportConfigurator>
            implements ICdmExporter<T> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmDefaultExport.class);

	@Override
    public ExportResult invoke(T config){
		ICdmDataSource source = config.getSource();
		return invoke(config, source);
	}


	/**
	 * @param config
	 * @param source
	 * @return
	 */
	public ExportResult invoke(IExportConfigurator config, ICdmDataSource source) {
	    ExportResult result = ExportResult.NewInstance(config.getResultType());
		boolean createNew = false;
		boolean omitTermLoading = false;
		if (startApplicationController(config, source, omitTermLoading, createNew) == false){
			String message = "Application conext could not be started";
		    result.addError(message);
		    return result;
		}else{
			CdmApplicationAwareDefaultExport<IExportConfigurator> defaultExport =
				(CdmApplicationAwareDefaultExport<IExportConfigurator>)getCdmAppController().getBean("defaultExport");
			return defaultExport.invoke(config);
		}
	}

}
