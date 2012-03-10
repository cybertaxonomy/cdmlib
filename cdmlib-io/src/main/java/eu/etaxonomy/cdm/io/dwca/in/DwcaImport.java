/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import java.net.URI;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.IIdentifiableEntityService;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * 
 * @author a.mueller
 *
 */

@Component
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
				TransactionStatus tx = startTransaction();
				handleCsvStreamItem(state, item);
				commitTransaction(tx);
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
	private void handleCsvStreamItem(DwcaImportState state, CsvStreamItem item) {
		IConverter<CsvStreamItem, IReader<CdmBase>, String> converter = getConverter(item, state);
		if (converter == null){
			state.setSuccess(false);
			return;
		}
		IReader<MappedCdmBase> resultReader = converter.map(item);
		while (resultReader.hasNext()){
			
			MappedCdmBase mappedCdmBase = (resultReader.read());
			CdmBase cdmBase = mappedCdmBase.getCdmBase();
			save(cdmBase, state, item.getLocation());
			if (mappedCdmBase.getSourceId() != null && cdmBase.isInstanceOf(IdentifiableEntity.class)){
				IdentifiableEntity<?> entity = CdmBase.deproxy(cdmBase, IdentifiableEntity.class);
				
				String namespace = mappedCdmBase.getNamespace();
				state.putMapping(namespace,mappedCdmBase.getSourceId(), entity);
			}
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

	private void save(CdmBase cdmBase, DwcaImportState state, String location) {
		if (state.isCheck()){
			//do nothing
		}else{
			if (cdmBase == null){
				logger.warn("cdmBase is null");
			}
			//start preliminary for testing
			IIdentifiableEntityService service;
			try {
				if (cdmBase.isInstanceOf(IdentifiableEntity.class)){
					service = getServiceByClass(cdmBase.getClass());
					if (service != null){
						IdentifiableEntity<?> entity = CdmBase.deproxy(cdmBase, IdentifiableEntity.class);
						service.saveOrUpdate(entity);
					}
				}
			} catch (IllegalArgumentException e) {
				fireWarningEvent(e.getMessage(), location, 12);
			}
			
			System.out.println(cdmBase.toString());
			//end preliminary
			
			//TODO
		}
	}

	private IConverter<CsvStreamItem,IReader<CdmBase>, String> getConverter(CsvStreamItem item, DwcaImportState state) {
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
	
	
	/**
	 * Returns an appropriate service to persist data of a certain class.
	 * If an appropriate service can't be found an {@link IllegalArgumentException} is thrown.
	 * 
	 * TODO move to a more general place to make it available to everyone.
	 * 
	 * @param app
	 * @param clazz
	 * @return
	 */
	protected IIdentifiableEntityService getServiceByClass(Class<?> clazz)  throws IllegalArgumentException {
		if (clazz == null){
			//throw exception below
		}else if (TaxonBase.class.isAssignableFrom(clazz)){
			return this.getTaxonService();
		}else if (Classification.class.isAssignableFrom(clazz)){
			return this.getClassificationService();
		}
		String warning = "Can't map class to cdmService: %s";
		warning = String.format(warning, (clazz == null ? "-" : clazz.getName()));
		throw new IllegalArgumentException(warning);
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
