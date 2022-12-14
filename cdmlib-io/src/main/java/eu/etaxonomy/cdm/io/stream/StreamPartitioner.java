/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.stream.mapping.IImportMapping;
import eu.etaxonomy.cdm.io.stream.mapping.InMemoryMapping;
import eu.etaxonomy.cdm.io.stream.terms.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;

/**
 * @author a.mueller
 */
public class StreamPartitioner<ITEM extends IConverterInput>
            implements INamespaceReader<IReader<MappedCdmBase<? extends CdmBase>>>{

    private static final Logger logger = LogManager.getLogger();

	private final int partitionSize;
	private final LookAheadStream<ITEM> inStream;
	private final IPartitionableConverter converter;
	private final StreamImportStateBase<StreamImportConfiguratorBase, StreamImportBase> state;
	private ConcatenatingReader<MappedCdmBase<? extends CdmBase>> outStream;

	public StreamPartitioner(INamespaceReader<ITEM> input, IPartitionableConverter converter,
	        StreamImportStateBase<StreamImportConfiguratorBase, StreamImportBase> state, Integer size){
		 this.inStream = new LookAheadStream<>(input);
		 this.converter = converter;
		 this.partitionSize = size;
		 this.state = state;
		 initNewOutStream();
	}

	private void initNewOutStream(){
		outStream = new ConcatenatingReader<>();
	}

	@Override
    public boolean hasNext() {
		if (this.outStream.hasNext()){
			return true;
		}else{
			return inStream.hasNext();  //TODO what, if converter returns no ouput for inStream.hasNext() ??
			//but be aware that requesting the next object from the next partition crosses the transactional borders
		}
	}

	@Override
	public IReader<MappedCdmBase<? extends CdmBase>> read() {
		logger.debug("Start partitioner read");
		handleNextPartition();
		IReader<MappedCdmBase<? extends CdmBase>> result = this.outStream;

		initNewOutStream();
		logger.debug("End partitioner read");
		return result;
	}

	private void handleNextPartition(){
	    List<ITEM> lookaheadArray = new ArrayList<>();
		while (this.inStream.hasNextLookAhead(partitionSize)){
			lookaheadArray.add(this.inStream.readLookAhead());
		}

		IReader<ITEM> lookaheadStream = new ListReader<>(lookaheadArray);

		Map<String, Set<String>> foreignKeys = converter.getPartitionForeignKeys(lookaheadStream);
		IImportMapping mapping = state.getMapping();
		InMemoryMapping partialMapping = mapping.getPartialMapping(foreignKeys);
		Reference sourceRef = state.getCurrentIO().getReferenceService().find(state.getConfig().getSourceRefUuid());
		partialMapping.putMapping(TermUri.CDM_SOURCE_REFERENCE.toString(), state.getConfig().getSourceRefUuid().toString(), sourceRef);

		state.loadRelatedObjects(partialMapping);

		while (inStream.isLookingAhead() && inStream.hasNext()){
			IReader<MappedCdmBase<? extends CdmBase>> resultReader = converter.map(inStream.read());
			List<MappedCdmBase<? extends CdmBase>> resultList = new ArrayList<>();  //maybe better let converter return list from the beginning
			while (resultReader.hasNext()){
				MappedCdmBase<? extends CdmBase> item = resultReader.read();
				resultList.add(item);
				addItemToRelatedObjects(item);
			}
			outStream.add(new ListReader<>(resultList));
		}

		return;
	}

	/**
	 * Add new items to the local mapping
	 */
	private void addItemToRelatedObjects(MappedCdmBase<? extends CdmBase> item) {
		CdmBase cdmBase = item.getCdmBase();
		if (cdmBase.getId() == 0 || cdmBase.isInstanceOf(DefinedTermBase.class)){
			if (cdmBase.isInstanceOf(IdentifiableEntity.class)){
			    IdentifiableEntity<?> identifiableEntity = CdmBase.deproxy(cdmBase, IdentifiableEntity.class);
			    Set<String> requiredSourceNamespaces = converter.requiredSourceNamespaces();
				if (requiredSourceNamespaces.contains(item.getNamespace())){
					state.addRelatedObject(item.getNamespace(), item.getSourceId(), identifiableEntity);
				}
			}else{
			    if (logger.isTraceEnabled()){logger.trace("Non identifiable are not added to related objects");}
			}
		}
	}

	@Override
	public TermUri getTerm() {
		return inStream.getTerm();
	}
}