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
public class StreamPartitioner<ITEM extends IConverterInput>  implements INamespaceReader<MappedCdmBase>{
	private static final Logger logger = Logger.getLogger(StreamPartitioner.class);
	
	private int partitionSize;
	private LookAheadStream<ITEM> inStream;
	private IPartitionableConverter converter;
	private DwcaImportState state;
	private ConcatenatingReader<MappedCdmBase> outStream = new ConcatenatingReader<MappedCdmBase>();
		
	
	public StreamPartitioner(INamespaceReader<ITEM> input, IPartitionableConverter converter, DwcaImportState state, Integer size){
		 this.inStream = new LookAheadStream<ITEM>(input);
		 this.converter = converter;
		 this.partitionSize = size;
		 this.state = state;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.dwca.in.IReader#hasNext()
	 */
	public boolean hasNext() {
		if (this.outStream.hasNext()){
			return true;
		}else{
			return inStream.hasNext();  //TODO what, if converter returns no ouput for inStream.hasNext() ??
		}
	}
	
	@Override
	public MappedCdmBase read() {
		if (! this.outStream.hasNext()){
			handleNextPartition();
		}
		return outStream.read();
	}
	
	private void handleNextPartition(){

		List<ITEM> lookaheadArray = new ArrayList<ITEM>();
		while (this.inStream.hasNextLookAhead(partitionSize)){
			lookaheadArray.add(this.inStream.readLookAhead());
		}
		
		IReader<ITEM> lookaheadStream = new ListReader<ITEM>(lookaheadArray);
		
		Map<String, Set<String>> foreignKeys = converter.getPartitionForeignKeys(lookaheadStream);
		IImportMapping mapping = state.getMapping();
		IImportMapping partialMapping = mapping.getPartialMapping(foreignKeys);
		state.loadRelatedObjects(partialMapping);
		
		
		while (inStream.isLookingAhead() && inStream.hasNext()){
			IReader<MappedCdmBase> resultReader = converter.map(inStream.read());
			outStream.add(resultReader);
		}
			
		return;

	}

	
	@Override
	public TermUri getTerm() {
		return inStream.getTerm();
	}
	

}
