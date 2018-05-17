package eu.etaxonomy.cdm.io.stream.excel;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.dwca.in.DwcTaxonCsv2CdmTaxonRelationConverter;
import eu.etaxonomy.cdm.io.dwca.in.DwcTaxonStreamItem2CdmTaxonConverter;
import eu.etaxonomy.cdm.io.dwca.in.DwcaDataImportBase;
import eu.etaxonomy.cdm.io.excel.stream.ExcelRecordStream;
import eu.etaxonomy.cdm.io.excel.stream.ExcelStreamImportState;
import eu.etaxonomy.cdm.io.excel.stream.ExcelToStreamConverter;
import eu.etaxonomy.cdm.io.stream.IPartitionableConverter;
import eu.etaxonomy.cdm.io.stream.IReader;
import eu.etaxonomy.cdm.io.stream.StreamItem;
import eu.etaxonomy.cdm.io.stream.terms.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.oppermann
 * @since 08.05.2013
 */
@Component
public class ExcelStreamImport
        extends DwcaDataImportBase<ExcelStreamImportConfigurator, ExcelStreamImportState>{

    private static final long serialVersionUID = -1067536880817966304L;

    private static final Logger logger = Logger.getLogger(ExcelStreamImport.class);


	@Override
	protected void doInvoke(ExcelStreamImportState state) {

		makeSourceRef(state);
		URI source = state.getConfig().getSource();
		ExcelToStreamConverter<ExcelStreamImportState> excelStreamConverter = ExcelToStreamConverter.NewInstance(source);

		try {
			IReader<ExcelRecordStream> worksheetStream = excelStreamConverter.getWorksheetStream(state);

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
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (HttpException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return;
	}

	@Override
	protected IPartitionableConverter<StreamItem, IReader<CdmBase>, String> getConverter(
			TermUri namespace, ExcelStreamImportState state) {

		if (namespace.equals(TermUri.DWC_TAXON)){
			if (! state.isTaxaCreated()){
				return new DwcTaxonStreamItem2CdmTaxonConverter(state);
			}else{
				return new DwcTaxonCsv2CdmTaxonRelationConverter(state);
			}
		}else{
			String message = "No converter available for %s";
			logger.error(String.format(message, namespace));
			return null;
		}
	}

	@Override
	protected boolean doCheck(ExcelStreamImportState state) {
		return state.isCheck();
	}

	@Override
	protected boolean isIgnore(ExcelStreamImportState state) {
		return false;  //we only have 1 import class for excel stream import
	}

}
