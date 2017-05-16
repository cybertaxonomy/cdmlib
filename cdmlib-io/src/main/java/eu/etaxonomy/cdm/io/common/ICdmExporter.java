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
public interface ICdmExporter<T extends IExportConfigurator> {

	public abstract ExportResult invoke(T config);



}
