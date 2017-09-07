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

import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.io.stream.StreamItem;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 *
 * @author a.mueller
 *
 */

@Component
public class DwcaImport
            extends DwcaDataImportBase<DwcaImportConfigurator, DwcaImportState>{

    private static final long serialVersionUID = -4340782092317841321L;
    private static final Logger logger = Logger.getLogger(DwcaImport.class);

	@Override
	protected void doInvoke(DwcaImportState state) {
		URI source = state.getConfig().getSource();
		makeSourceRef(state);

		DwcaZipToStreamConverter<DwcaImportState> dwcaStreamConverter = DwcaZipToStreamConverter.NewInstance(source);
		IReader<CsvStream> zipEntryStream = dwcaStreamConverter.getEntriesStream(state);
		while (zipEntryStream.hasNext()){
			CsvStream csvStream = zipEntryStream.read();
			try {
				handleSingleRecord(state, csvStream);
			} catch (Exception e) {
				String message = "Exception (%s) occurred while handling zip entry %s";
				message = String.format(message, e.getMessage(), csvStream.toString());
				fireWarningEvent (message, csvStream.toString(), 14);
			}
		}
		if (!state.getConfig().isKeepMappingForFurtherImports()){
		    state.finish();
		}
		return;
	}

	@Override
	protected IPartitionableConverter<StreamItem,IReader<CdmBase>, String>
	                getConverter(TermUri namespace, DwcaImportState state) {

	    if (namespace.equals(TermUri.DWC_TAXON)){
			if (state.getConfig().isDoTaxa() && ! state.isTaxaCreated()){
				return new DwcTaxonStreamItem2CdmTaxonConverter<>(state);
			}else{
				return new DwcTaxonCsv2CdmTaxonRelationConverter(state);
			}
//		}else if (namespace.equals(TermUri.GBIF_VERNACULAR_NAMES)){
//			return new GbifVernacularNameCsv2CdmConverter(state);
//		}else if (namespace.equals(TermUri.GBIF_DESCRIPTION)){
//			return new GbifDescriptionCsv2CdmConverter(state);
//		}else if (namespace.equals(TermUri.GBIF_DISTRIBUTION)){
//			return new GbifDistributionCsv2CdmConverter(state);
		}else if (namespace.equals(TermUri.GBIF_REFERENCE)){
			return new GbifReferenceCsv2CdmConverter(state);
//		}else if (namespace.equals(TermUri.GBIF_TYPES_AND_SPECIMEN)){
//			return new GbifTypesAndSpecimen2CdmConverter(state);
//		}else if (namespace.equals(TermUri.EOL_AGENT)){
//			return new EolAgent2CdmConverter(state);
		}else{
			String message = "No converter available for %s";
			logger.error(String.format(message, namespace));
			return null;
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

	@Override
	protected boolean doCheck(DwcaImportState state) {
		return state.isCheck();
	}

	@Override
	protected boolean isIgnore(DwcaImportState state) {
		return false;
	}


}
