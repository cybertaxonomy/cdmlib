/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.excel.taxa;

import java.net.URI;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;

/**
 * @author k.luther
 * @since 21.02.2018
 *
 */
public class TaxonListImportConfigurator extends ExcelImportConfiguratorBase {

    /**
     * @param uri
     * @param destination
     */
    protected TaxonListImportConfigurator(URI uri, ICdmDataSource destination) {
        super(uri, destination);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <STATE extends ImportStateBase> STATE getNewState() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void makeIoClassList() {
        // TODO Auto-generated method stub

    }

}
