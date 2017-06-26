/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.common;


/**
 *
 * @author a.babadshanjan
 * @created 17.11.2008
 */
public interface ICdmExport<CONFIG extends IExportConfigurator, STATE extends ExportStateBase>
            extends ICdmIO<STATE>{


	public ExportDataWrapper createExportData();

	public byte[] getByteArray();

	public abstract void invoke(STATE state);

}
