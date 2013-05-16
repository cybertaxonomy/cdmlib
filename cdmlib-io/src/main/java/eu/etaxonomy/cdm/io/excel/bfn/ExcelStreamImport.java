package eu.etaxonomy.cdm.io.excel.bfn;

import java.net.URI;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.io.dwca.in.CsvStream;
import eu.etaxonomy.cdm.io.dwca.in.IConverter;
import eu.etaxonomy.cdm.io.dwca.in.MappedCdmBase;
import eu.etaxonomy.cdm.io.dwca.in.DwcaImportState;
import eu.etaxonomy.cdm.io.dwca.in.DwcaZipToStreamConverter;
import eu.etaxonomy.cdm.io.dwca.in.IPartitionableConverter;
import eu.etaxonomy.cdm.io.dwca.in.IReader;
import eu.etaxonomy.cdm.io.dwca.in.StreamPartitioner;
import eu.etaxonomy.cdm.io.stream.IItemStream;
import eu.etaxonomy.cdm.io.stream.StreamImportBase;
import eu.etaxonomy.cdm.io.stream.StreamItem;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * 
 * @author a.oppermann
 * @date 08.05.2013
 *
 */
public class ExcelStreamImport extends StreamImportBase<ExcelStreamImportConfigurator, ExcelStreamImportState>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExcelStreamImport.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected void doInvoke(ExcelStreamImportState state) {
		URI source = state.getConfig().getSource();
		makeSourceRef(state);
		
		ExcelToStreamConverter<ExcelStreamImportState> excelStreamConverter = ExcelToStreamConverter.NewInstance(source);
		IReader<ExcelRecordStream> worksheetStream = excelStreamConverter.getEntriesStream(state);
		
		
		while (worksheetStream.hasNext()){
			ExcelRecordStream recordStream = worksheetStream.read();
			try {
				handleSingleRecord(state, recordStream);
			} catch (Exception e) {
				String message = "Exception (%s) occurred while handling worksheet stream %s";
				message = String.format(message, e.getMessage(), recordStream.toString());
				fireWarningEvent (message, recordStream.toString(), 14);
			}
		}
		state.finish();
		return;
		
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.dwca.in.StreamImportBase#getConverter(eu.etaxonomy.cdm.io.dwca.TermUri, eu.etaxonomy.cdm.io.dwca.in.StreamImportStateBase)
	 */
	@Override
	protected IPartitionableConverter<StreamItem, IReader<CdmBase>, String> getConverter(
			TermUri namespace, ExcelStreamImportState state) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(ExcelStreamImportState state) {
		return state.isCheck();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean isIgnore(ExcelStreamImportState state) {
		return false;  //we only have 1 import class for excel stream import
	}


	
	//get ExcelFile
	
	//read spreadsheet
	//read collumns 
	//read rows
	//read cells
	//analyze content
	//map content to cdm model
	//pass objects to stream scheme of dwca importer
}