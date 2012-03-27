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
		DwcaZipToStreamConverter<DwcaImportState> dwcaStreamConverter = DwcaZipToStreamConverter.NewInstance(source);
		IReader<CsvStream> stream = dwcaStreamConverter.getStreamStream(state);
		while (stream.hasNext()){
  			CsvStream csvStream = stream.read();
			
  			if (state.getConfig().isUsePartitions()){
  				IPartitionableConverter<CsvStreamItem, IReader<CdmBase>, String> partitionConverter = getConverter(csvStream.getTerm(), state);
  				if (partitionConverter == null){
  					String warning = "No converter available for %s. Continue with next stream.";
  					warning = String.format(warning, csvStream.getTerm());
  					fireWarningEvent (warning, csvStream.read().getLocation(), 12);
  					continue;
  				}
  				
  				int partitionSize = state.getConfig().getDefaultPartitionSize();
				StreamPartitioner<CsvStreamItem> partitionStream = new StreamPartitioner<CsvStreamItem>(csvStream, 
						partitionConverter, state, partitionSize);//   (csvStream, streamConverter,state 1000);
	  			
				int i = 1;
	  			while (partitionStream.hasNext()){
	  				//FIXME more generic handling of transactions
	  				TransactionStatus tx = startTransaction();
	  				
	  				try {
						IReader<MappedCdmBase> partStream = partitionStream.read();
						logger.warn("Handel " + i++ + ". partition");
						String location = "Location: partition stream (TODO)";
						handleResults(state, partStream, location);
						commitTransaction(tx);
					} catch (Exception e) {
						String message = "An exception occurred while handling partition: " + e;
						StackTraceElement el = e.getStackTrace()[0];
						String codeLocation = el.getClassName()+ "." + el.getMethodName() + "(" + el.getLineNumber() + ")";
						message = message + " in: " +  codeLocation;
						fireWarningEvent(message , csvStream.read().getLocation() , 12);
						this.rollbackTransaction(tx);
					}
	  				
	  			}
			}else {
		  			
		  		while (csvStream.hasNext()){
						TransactionStatus tx = startTransaction();
						
						CsvStreamItem item = csvStream.read();
						handleCsvStreamItem(state, item);
						
						commitTransaction(tx);
					}
			}

  			finalizeStream(csvStream, state);
		}
		state.finish();
		return;
	}

//	private void handlePartitionedStreamItem(DwcaImportState state,  StreamPartitioner<CsvStreamItem> partitionStream) {
//		IPartitionableConverter<CsvStreamItem, IReader<CdmBase>, String> converter = getConverter(partitionStream.getTerm(), state);
//		if (converter == null){
//			state.setSuccess(false);
//			return;
//		}
//		
//		IReader<CsvStreamItem> lookaheadStream = partitionStream.getLookaheadReader();
//		Map<String, Set<String>> foreignKeys = converter.getPartitionForeignKeys(lookaheadStream);
//		IImportMapping mapping = state.getMapping();
//		IImportMapping partialMapping = mapping.getPartialMapping(foreignKeys);
//		state.loadRelatedObjects(partialMapping);
//		
//		ConcatenatingReader<MappedCdmBase> reader = new ConcatenatingReader<MappedCdmBase>();
// 		
//		IReader<CsvStreamItem> inputStream = partitionStream.read();
//		while (inputStream.hasNext()){
//			IReader<MappedCdmBase> resultReader = converter.map(inputStream.read());
//			reader.add(resultReader);
//		}
//			
//		while (reader.hasNext()){
//			MappedCdmBase mappedCdmBase = (reader.read());
//			CdmBase cdmBase = mappedCdmBase.getCdmBase();
//			//locate
//			//TODO find a way to define the location
//			String location = "partitionStream";
//			//save
//			save(cdmBase, state, location);
//			//store in mapping
//			if (mappedCdmBase.getSourceId() != null && cdmBase.isInstanceOf(IdentifiableEntity.class)){
//				IdentifiableEntity<?> entity = CdmBase.deproxy(cdmBase, IdentifiableEntity.class);
//				String namespace = mappedCdmBase.getNamespace();
//				//TODO also store in partition mapping
//				state.putMapping(namespace,mappedCdmBase.getSourceId(), entity);
//			}
//		}
//		return;
//	}

	/**
	 * @param state
	 * @param item
	 * @return
	 */
	private void handleCsvStreamItem(DwcaImportState state, CsvStreamItem item) {
		IConverter<CsvStreamItem, IReader<CdmBase>, String> converter = getConverter(item.term, state);
		if (converter == null){
			state.setSuccess(false);
			return;
		}
		IReader<MappedCdmBase> resultReader = converter.map(item);
		handleResults(state, resultReader, item.getLocation());
		return;
	}

	/**
	 * @param state
	 * @param item
	 * @param resultReader
	 */
	private void handleResults(DwcaImportState state, IReader<MappedCdmBase> resultReader, String location) {
		while (resultReader.hasNext()){
			
			MappedCdmBase mappedCdmBase = (resultReader.read());
			CdmBase cdmBase = mappedCdmBase.getCdmBase();
			save(cdmBase, state, location);
			if (mappedCdmBase.getSourceId() != null && cdmBase.isInstanceOf(IdentifiableEntity.class)){
				IdentifiableEntity<?> entity = CdmBase.deproxy(cdmBase, IdentifiableEntity.class);
				
				String namespace = mappedCdmBase.getNamespace();
				state.putMapping(namespace,mappedCdmBase.getSourceId(), entity);
			}
		}
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
			
//			System.out.println(cdmBase.toString());
			//end preliminary
			
			//TODO
		}
	}

	private IPartitionableConverter<CsvStreamItem,IReader<CdmBase>, String> getConverter(TermUri namespace, DwcaImportState state) {
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
			String message = "No converter available for %s";
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
