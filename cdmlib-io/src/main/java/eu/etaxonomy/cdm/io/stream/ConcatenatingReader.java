/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.stream;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author a.mueller
 \* @since 19.03.2012
 *
 */
public class ConcatenatingReader<TYPE> implements IReader<TYPE> {

	private Queue<IReader<TYPE>> innerReaders = new LinkedList<IReader<TYPE>>();
	private IReader<TYPE> currentReader;
	
	/**
	 * 
	 */
	public ConcatenatingReader() {
		super();
	}

	/**
	 * 
	 */
	public ConcatenatingReader(IReader<TYPE> startingReader) {
		super();
		add(startingReader);
	}

	
	@Override
	public TYPE read() {
		findNextReaderWithItem();
		if (currentReader == null){
			return null;
		}else{
			return currentReader.read();
		}
	}

	/**
	 * 
	 */
	private void findNextReaderWithItem() {
		if (currentReader == null){
			currentReader = innerReaders.poll();
		}
		while (currentReader != null && ! currentReader.hasNext()){
			currentReader = innerReaders.poll();
		}
	}

	@Override
	public boolean hasNext() {
		findNextReaderWithItem();
		return  (currentReader != null);
	}
	
	public void add(IReader<TYPE> newReader){
		innerReaders.add(newReader);
	}

}
