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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.dwca.TermUri;


/**
 * @author a.mueller
 *
 */
public class StreamPartitioner<ITEM extends IConverterInput>  implements INamespaceReader<IReader<ITEM>>{
	private static final Logger logger = Logger.getLogger(StreamPartitioner.class);
	
	private int partitionSize;
	private LookAheadStream<ITEM> stream;
	private IConverter<ITEM, IConverterOutput, Object> converter;
	
	public StreamPartitioner(INamespaceReader<ITEM> reader, IConverter converter, Integer size){
		 this.stream = new LookAheadStream<ITEM>(reader);
		 this.converter = converter;
		 this.partitionSize = size;
	}
	
	private List<ITEM> readPartition(){
		List<ITEM> partitionItems = new ArrayList<ITEM>();
		while ( stream.hasNextLookAhead(partitionSize)){
			ITEM next = stream.readLookAhead(partitionSize);
			partitionItems.add(next);
		}
		return partitionItems;
	}

	public boolean hasNext() {
		return stream.hasNext();
	}

	@Override
	public IReader<ITEM> read() {
		List<ITEM> partitionItems = readPartition();
		for (ITEM partitionItem : partitionItems){
			IReader<MappedCdmBase> newItem = converter.map(partitionItem);
		}
		
		while (stream.readLookAhead(partitionSize) != null){
			//TODO
			//should this method return a reader of OUTPUT items instead of a List of input items??
			logger.warn("Unclear what todo here");
		}
		
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public TermUri getTerm() {
		return stream.getTerm();
	}
	
	
	
		
	

}
