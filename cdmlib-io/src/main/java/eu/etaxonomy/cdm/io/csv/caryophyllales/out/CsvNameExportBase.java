/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/
package eu.etaxonomy.cdm.io.csv.caryophyllales.out;

import java.io.File;

import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;

public abstract class CsvNameExportBase
        extends CdmExportBase<CsvNameExportConfigurator, CsvNameExportState, IExportTransformer, File>
        implements ICdmExport<CsvNameExportConfigurator, CsvNameExportState>{

    private static final long serialVersionUID = -8141111132821035857L;

    final String NOT_DESIGNATED = "not designated";

    protected TransactionStatus txStatus;

    protected void refreshTransaction(){
        commitTransaction(txStatus);
        txStatus = startTransaction();
    }


}
