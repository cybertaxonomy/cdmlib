/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.excel.taxa;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;

/**
 * @author k.luther
 * @since 21.02.2018
 */
public class TaxonListImportConfigurator extends ExcelImportConfiguratorBase {

    private static final long serialVersionUID = 866658153336029112L;

    protected TaxonListImportConfigurator(URI uri, ICdmDataSource destination) {
        super(uri, destination);

    }
    public static TaxonListImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
        return new TaxonListImportConfigurator(uri, destination);
    }

    @Override
    public <STATE extends ImportStateBase> STATE getNewState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void makeIoClassList() {
        // TODO Auto-generated method stub
    }
}