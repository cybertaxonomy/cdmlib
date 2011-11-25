package eu.etaxonomy.cdm.io.dwca.in;

import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;

public class DwcaImport extends CdmImportBase<DwcaImportConfigurator, DwcaImportState>{
	private static final Logger logger = Logger.getLogger(DwcaImport.class);

	@Override
	protected void doInvoke(DwcaImportState state) {
		URI source = state.getConfig().getSource();
		DwcaZipToStreamConverter<DwcaImportState> streamConverter = DwcaZipToStreamConverter.NewInstance(source);
		IReader<CsvStream> stream = streamConverter.getStreamStream(state);
		while (stream.hasNext()){
			CsvStream csvStream = stream.read();
			while (csvStream.hasNext()){
				CsvStreamItem item = csvStream.read();
				handleCsvItemStream(state, item);
			}
			finalizeStream(csvStream, state);
		}
		return;
	}

	/**
	 * @param state
	 * @param item
	 * @return
	 */
	private void handleCsvItemStream(DwcaImportState state, CsvStreamItem item) {
		IConverter<CsvStreamItem, IReader<CdmBase>> converter = getConverter(item, state);
		if (converter == null){
			state.setSuccess(false);
			return;
		}
		IReader<CdmBase> resultReader = converter.map(item);
		while (resultReader.hasNext()){
			save(resultReader.read(),state);
		}
		return;
	}

	private void finalizeStream(CsvStream csvStream, DwcaImportState state) {
		if (csvStream.getTerm().equals(TermUri.DWC_TAXON)){
			if (state.isTaxaCreated() == false){
				state.setTaxaCreated(true);
			}
		}
		
	}

	private void save(CdmBase cdmBase, DwcaImportState state) {
		if (state.isCheck()){
			//do nothing
		}else{
			if (cdmBase == null){
				logger.warn("cdmBase is null");
			}
			System.out.println(cdmBase.toString());
		}
	}

	private IConverter<CsvStreamItem,IReader<CdmBase>> getConverter(CsvStreamItem item, DwcaImportState state) {
		TermUri namespace = item.term;
		if (namespace.equals(TermUri.DWC_TAXON)){
			if (! state.isTaxaCreated()){
				return new DwcTaxonCsv2CdmTaxonConverter(state);
			}else{
				return new DwcTaxonCsv2CdmTaxonRelationConverter(state);
			}
		}else if (namespace.equals(TermUri.GBIF_VERNACULAR_NAMES)){
			return new GbifVernacularNameCsv2CdmConverter(state);
		}else if (namespace.equals(TermUri.GBIF_DESCRIPTION)){
			return new GbifDescriptionCsv2CdmConverter(state);
		}else{
			String message = "Now converter available for %s";
			logger.error(String.format(message, namespace));
			return null;
		}
	}

	@Override
	protected boolean doCheck(DwcaImportState state) {
		return state.isCheck();
	}

	@Override
	protected boolean isIgnore(DwcaImportState state) {
		return false;
	}
	
}
