/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.stream;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.IIdentifiableEntityService;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.io.dwca.in.IConverter;
import eu.etaxonomy.cdm.io.dwca.in.IPartitionableConverter;
import eu.etaxonomy.cdm.io.dwca.in.IReader;
import eu.etaxonomy.cdm.io.dwca.in.MappedCdmBase;
import eu.etaxonomy.cdm.io.dwca.in.StreamPartitioner;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * 
 * @author a.mueller
 *
 */
public abstract class StreamImportBase<CONFIG extends StreamImportConfiguratorBase, STATE extends StreamImportStateBase<CONFIG,StreamImportBase>> extends CdmImportBase<CONFIG, STATE>{
	private static final Logger logger = Logger.getLogger(StreamImportBase.class);


	protected void makeSourceRef(STATE state) {
		Reference<?> sourceRef = state.getConfig().getSourceReference();
		getReferenceService().saveOrUpdate(sourceRef);
	}
	
	
	/**
	 * @param state
	 * @param itemStream
	 */
	protected void handleSingleRecord(STATE state, IItemStream recordStream) {
		recordStream.addObservers(state.getConfig().getObservers());
		
		if (state.getConfig().isUsePartitions()){
			IPartitionableConverter<StreamItem, IReader<CdmBase>, String> partitionConverter = getConverter(recordStream.getTerm(), state);
			if (partitionConverter == null){
				String warning = "No converter available for %s. Continue with next stream.";
				warning = String.format(warning, recordStream.getTerm());
				fireWarningEvent (warning, recordStream.toString(), 12);
				return;
			}
			
			int partitionSize = state.getConfig().getDefaultPartitionSize();
			StreamPartitioner<StreamItem> partitionStream = new StreamPartitioner<StreamItem>(recordStream, 
					partitionConverter, state, partitionSize);//   (csvStream, streamConverter,state 1000);
			
			int i = 1;
			while (partitionStream.hasNext()){
				//FIXME more generic handling of transactions
				TransactionStatus tx = startTransaction();
				
				try {
					IReader<MappedCdmBase> partStream = partitionStream.read();

					fireProgressEvent("Handel " + i + ". partition", i + ". partition");
					logger.info("Handel " + i++ + ". partition");
					String location = "Location: partition stream (TODO)";
					handleResults(state, partStream, location);
					commitTransaction(tx);
				} catch (Exception e) {
					String message = "An exception occurred while handling partition: " + e;
					String codeLocation;
					if (e.getStackTrace().length > 0){
						StackTraceElement el = e.getStackTrace()[0];
						codeLocation = el.getClassName()+ "." + el.getMethodName() + "(" + el.getLineNumber() + ")";
					}else{
						codeLocation = "No stacktrace";
					}
					message = message + " in: " +  codeLocation;
					fireWarningEvent(message , String.valueOf(recordStream.getItemLocation()) , 12);
					this.rollbackTransaction(tx);
				}
				
			}
			logger.debug("Partition stream is empty");
		}else {
				
			while (recordStream.hasNext()){
					TransactionStatus tx = startTransaction();
					
					StreamItem item = recordStream.read();
					handleStreamItem(state, item);
					
					commitTransaction(tx);
			}
		}

		finalizeStream(recordStream, state);
	}
	
	
	/**
	 * @param itemStream
	 * @param state
	 */
	protected void finalizeStream(IItemStream itemStream, STATE state) {
		fireWarningEvent("Stream finished", itemStream.getItemLocation(), 0);
	}
	

	/**
	 * @param state
	 * @param item
	 * @return
	 */
	private void handleStreamItem(STATE state, StreamItem item) {
		IConverter<StreamItem, IReader<CdmBase>, String> converter = getConverter(item.term, state);
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
	private void handleResults(STATE state, IReader<MappedCdmBase> resultReader, String location) {
		while (resultReader.hasNext()){
			
			MappedCdmBase<?> mappedCdmBase = resultReader.read();
			CdmBase cdmBase = mappedCdmBase.getCdmBase();
			save(cdmBase, state, location);
			if (mappedCdmBase.getSourceId() != null && cdmBase.isInstanceOf(IdentifiableEntity.class)){
				IdentifiableEntity<?> entity = CdmBase.deproxy(cdmBase, IdentifiableEntity.class);
				
				String namespace = mappedCdmBase.getNamespace();
				state.putMapping(namespace,mappedCdmBase.getSourceId(), entity);
			}
		}
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

	protected void save(CdmBase cdmBase, STATE state, String location) {
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

	protected abstract IPartitionableConverter<StreamItem,IReader<CdmBase>, String> getConverter(TermUri namespace, STATE state);
	
	
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
		}else if (Reference.class.isAssignableFrom(clazz)){
			return this.getReferenceService();
		}else if (TaxonNameBase.class.isAssignableFrom(clazz)){
			return this.getNameService();
		}else if (DefinedTermBase.class.isAssignableFrom(clazz)){
			return this.getTermService();
		}else if (DescriptionBase.class.isAssignableFrom(clazz)){
			return this.getDescriptionService();
		}else if (SpecimenOrObservationBase.class.isAssignableFrom(clazz)){
			return this.getOccurrenceService();
		}else if (Collection.class.isAssignableFrom(clazz)){
			return this.getCollectionService();
		}else if (AgentBase.class.isAssignableFrom(clazz)){
			return this.getDescriptionService();
		}
		String warning = "Can't map class to API service: %s";
		warning = String.format(warning, (clazz == null ? "-" : clazz.getName()));
		throw new IllegalArgumentException(warning);
	}
	
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmImportBase#getFeature(eu.etaxonomy.cdm.io.common.ImportStateBase, java.util.UUID, java.lang.String, java.lang.String, java.lang.String, eu.etaxonomy.cdm.model.common.TermVocabulary)
	 */
	//Make public to allow to use by converters
	@Override
	public Feature getFeature(STATE state, UUID uuid, String label, String description, String labelAbbrev, TermVocabulary<Feature> voc) {
		return super.getFeature(state, uuid, label, description, labelAbbrev, voc);
	}

	/**
	 * Saves a new term. Immediate saving is required to avoid by Transient-Object-Exceptions.
	 * @param newTerm
	 */
	public void saveNewTerm(DefinedTermBase newTerm) {
		getTermService().save(newTerm);
	}


	
}
