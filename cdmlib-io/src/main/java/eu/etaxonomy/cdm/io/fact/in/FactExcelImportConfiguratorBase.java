/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.in;

import eu.etaxonomy.cdm.common.URI;
import java.util.UUID;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.io.fact.altitude.in.analyze.ExcelFormatAnalyzer;

/**
 * Configurator base class for taxon fact excel imports.
 *
 * @author a.mueller
 * @since 28.05.2020
 */
public abstract class FactExcelImportConfiguratorBase<A extends ExcelFormatAnalyzer<?>>
        extends ExcelImportConfiguratorBase{

    private static final long serialVersionUID = 1649010514975388511L;

    private UUID featureUuid;
    private String featureLabel;

    protected FactExcelImportConfiguratorBase(URI uri, ICdmDataSource destination, IInputTransformer transformer) {
        super(uri, destination, transformer);
    }

    public abstract A getAnalyzer();



    public UUID getFeatureUuid() {
        return featureUuid;
    }
    public void setFeatureUuid(UUID featureUuid) {
        this.featureUuid = featureUuid;
    }

    public String getFeatureLabel() {
        return this.featureLabel;
    }
    public void setFeatureLabel(String featureLabel) {
        this.featureLabel = featureLabel;
    }
}
