package eu.etaxonomy.cdm.io.csv.caryophyllales.out;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.csv.redlist.demo.CsvDemoBase;
import eu.etaxonomy.cdm.io.csv.redlist.demo.CsvDemoExportConfigurator;
import eu.etaxonomy.cdm.io.csv.redlist.demo.CsvDemoExportState;
import eu.etaxonomy.cdm.io.csv.redlist.out.CsvExportBaseRedlist;
import eu.etaxonomy.cdm.io.csv.redlist.out.CsvTaxExportConfiguratorRedlist;
import eu.etaxonomy.cdm.io.csv.redlist.out.CsvTaxExportStateRedlist;
import eu.etaxonomy.cdm.io.csv.redlist.out.CsvTaxRecordRedlist;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

public class CsvNameExportBase extends CdmExportBase<CsvNameExportConfigurator, CsvNameExportState, IExportTransformer> implements ICdmExport<CsvNameExportConfigurator, CsvNameExportState>{

	public CsvNameExportBase() {
		super();
		this.ioName = this.getClass().getSimpleName();
	}

	
	@Override
	protected void doInvoke(CsvNameExportState state) {
		CsvNameExportConfigurator config = state.getConfig();
		TransactionStatus txStatus = startTransaction(true);
		
		PrintWriter writer = null;
		ByteArrayOutputStream byteArrayOutputStream;
		
			byteArrayOutputStream = config.getByteOutputStream();
			writer = new PrintWriter(byteArrayOutputStream); 
	
			List<HashMap<String, String>> result = getNameService().getNameRecords();
			NameRecord nameRecord;
			int count = 0;
			boolean isFirst = true;
			for (HashMap<String,String> record:result){
				if (count > 0){
					isFirst = false;
				}
				nameRecord = new NameRecord(record, isFirst);
				nameRecord.print(writer, config);
					
			}
			writer.flush();
		
			writer.close();
			
		
		commitTransaction(txStatus);
		return;


	}

	

	@Override
	protected boolean doCheck(CsvNameExportState state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean isIgnore(CsvNameExportState state) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	

}
