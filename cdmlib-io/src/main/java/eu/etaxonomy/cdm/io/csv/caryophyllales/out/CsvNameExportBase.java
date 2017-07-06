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
    TransactionStatus txStatus;

    protected void refreshTransaction(){
        commitTransaction(txStatus);
        txStatus = startTransaction();

    }


}
