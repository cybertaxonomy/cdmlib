/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.service;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.SOURCE_TYPE;
import eu.etaxonomy.cdm.io.common.ImportResult;
import eu.etaxonomy.cdm.io.distribution.excelupdate.ExcelDistributionUpdateConfigurator;
import eu.etaxonomy.cdm.io.reference.ris.in.RisReferenceImportConfigurator;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportConfiguratorBase;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Abcd206ImportConfigurator;

/**
 * @author cmathew
 * @since 31 Jul 2015
 */
public interface IIOService {

    public ExportResult export(IExportConfigurator configurator);

    public UUID monitImportData(IImportConfigurator configurator, byte[] importData, SOURCE_TYPE type);

    public ImportResult importData(IImportConfigurator configurator, byte[] importData, SOURCE_TYPE type);

    public ImportResult importDataFromUri(IImportConfigurator configurator, byte[] importData);

    public ImportResult importDataFromInputStream(IImportConfigurator configurator, byte[] importData);

    ImportResult importDataFromStream(SpecimenImportConfiguratorBase configurator);

    ImportResult importDataFromStream(List<Abcd206ImportConfigurator> configurators);

    ImportResult updateDistributionData(ExcelDistributionUpdateConfigurator configurator);

    UUID monitExportData(IExportConfigurator configurator);

    ImportResult importRISData(RisReferenceImportConfigurator configurator);

}
