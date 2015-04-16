package eu.etaxonomy.cdm.io.csv.caryophyllales.out;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;





import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
@Component
public class CsvNameExportBase extends CdmExportBase<CsvNameExportConfigurator, CsvNameExportState, IExportTransformer> implements ICdmExport<CsvNameExportConfigurator, CsvNameExportState>{
	private static final Logger logger = Logger.getLogger(CsvNameExportBase.class);
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
			try {
				writer = new PrintWriter(config.getDestination());
				
				List<HashMap<String, String>> result = getNameService().getNameRecords();
				NameRecord nameRecord;
				int count = 0;
				boolean isFirst = true;
				for (HashMap<String,String> record:result){
					if (count > 0){
						isFirst = false;
					}
					count++;
					nameRecord = new NameRecord(record, isFirst);
					nameRecord.print(writer, config);
						
				}
				writer.flush();
			
				writer.close();
			
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		commitTransaction(txStatus);
		return;


	}

	

	

	@Override
	protected boolean doCheck(CsvNameExportState state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}

	@Override
	protected boolean isIgnore(CsvNameExportState state) {
		return false;
	}

	
	
	

}
