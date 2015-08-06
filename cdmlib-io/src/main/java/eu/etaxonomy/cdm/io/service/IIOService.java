// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.service;

import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.SOURCE_TYPE;
import eu.etaxonomy.cdm.io.common.ImportResult;

/**
 * @author cmathew
 * @date 31 Jul 2015
 *
 */
public interface IIOService {

    public ExportResult export(IExportConfigurator configurator);

    /**
     * @param configurator
     * @param importData
     * @param type
     * @return
     */
    public ImportResult importData(IImportConfigurator configurator, byte[] importData, SOURCE_TYPE type);

    /**
     * @param configurator
     * @param importData
     * @return
     */
    public ImportResult importDataFromUri(IImportConfigurator configurator, byte[] importData);

    /**
     * @param configurator
     * @param importData
     * @return
     */
    public ImportResult importDataFromInputStream(IImportConfigurator configurator, byte[] importData);

}
