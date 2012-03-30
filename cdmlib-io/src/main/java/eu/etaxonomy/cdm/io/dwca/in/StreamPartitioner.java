/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;


/**
 * @author a.mueller
 *
 */
public class StreamPartitioner<ITEM extends IConverterInput>  implements INamespaceReader<IReader<MappedCdmBase>>{
	private static final Logger logger = Logger.getLogger(StreamPartitioner.class);
	
	private int partitionSize;
	private LookAheadStream<ITEM> inStream;
	private IPartitionableConverter converter;
	private DwcaImportState state;
	private ConcatenatingReader<MappedCdmBase> outStream;
	
	public StreamPartitioner(INamespaceReader<ITEM> input, IPartitionableConverter converter, DwcaImportState state, Integer size){
		 this.inStream = new LookAheadStream<ITEM>(input);
		 this.converter = converter;
		 this.partitionSize = size;
		 this.state = state;
		 initNewOutStream();
	}
	

	private void initNewOutStream(){
		outStream = new ConcatenatingReader<MappedCdmBase>();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.dwca.in.IReader#hasNext()
	 */
	public boolean hasNext() {
		if (this.outStream.hasNext()){
			return true;
		}else{
			return inStream.hasNext();  //TODO what, if converter returns no ouput for inStream.hasNext() ??
			//but be aware that requesting the next object from the next partition crosses the transactional borders 
		}
	}
	
	@Override
	public IReader<MappedCdmBase> read() {
		logger.debug("Start partitioner read");
		handleNextPartition();
		IReader<MappedCdmBase> result = this.outStream;
		
		initNewOutStream();
		logger.debug("End partitioner read");
		return result;
	}
	
	private void handleNextPartition(){

		List<ITEM> lookaheadArray = new ArrayList<ITEM>();
		while (this.inStream.hasNextLookAhead(partitionSize)){
			lookaheadArray.add(this.inStream.readLookAhead());
		}
		
		IReader<ITEM> lookaheadStream = new ListReader<ITEM>(lookaheadArray);
		
		Map<String, Set<String>> foreignKeys = converter.getPartitionForeignKeys(lookaheadStream);
		IImportMapping mapping = state.getMapping();
		InMemoryMapping partialMapping = mapping.getPartialMapping(foreignKeys);
		
		
		state.loadRelatedObjects(partialMapping);
		
		
		while (inStream.isLookingAhead() && inStream.hasNext()){
			IReader<MappedCdmBase> resultReader = converter.map(inStream.read());
			List<MappedCdmBase> resultList = new ArrayList<MappedCdmBase>();  //maybe better let converter return list from the beginning
			while (resultReader.hasNext()){
				MappedCdmBase item = resultReader.read();
				resultList.add(item);
				addItemToRelatedObjects(item);
			}
			outStream.add(new ListReader<MappedCdmBase>(resultList));
		}
			
		return;

	}

	
	/**
	 * Add new items to the local mapping
	 * @param item
	 */
	private void addItemToRelatedObjects(MappedCdmBase<IdentifiableEntity> item) {
		CdmBase cdmBase = item.getCdmBase();
		if (cdmBase.getId() == 0){
			if (cdmBase.isInstanceOf(IdentifiableEntity.class)){
				if (converter.requiredSourceNamespaces().contains(item.getNamespace())){
					state.addRelatedObject(item.getNamespace(), item.getSourceId(),  item.getCdmBase());
				}
			}
		}
	}


	@Override
	public TermUri getTerm() {
		return inStream.getTerm();
	}
	

}
