/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.dwca.TermUri;


/**
 * @author a.mueller
 *
 */
public class LookAheadStream<ITEM> implements INamespaceReader<ITEM>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(LookAheadStream.class);

	private final Queue<ITEM> fifo = new LinkedBlockingQueue<ITEM>();

	private final INamespaceReader<ITEM> stream;

	public LookAheadStream(INamespaceReader<ITEM> stream) {
		super();
		this.stream = stream;
		if (stream == null){
			throw new RuntimeException("Stream may not be null.");
		}
	}

	@Override
    public ITEM read(){
		if (! fifo.isEmpty()){
			return fifo.remove();
		}else{
			return stream.read();
		}
	}

	public ITEM readLookAhead(int max){
	    if (max > fifo.size()){
	        ITEM result = stream.read();
	        fifo.add(result);
	        return result;
	    }else{
	        return null;
	    }
	}

	public ITEM readLookAhead(){
		ITEM result = stream.read();
	    fifo.add(result);
	    return result;
	}

	public boolean hasNextLookAhead(int max){
		if (fifo.size() < max && stream.hasNext()){
			return true;
		}else{
			return false;
		}
	}

	public int sizeLookAhead(){
		return fifo.size();
	}

	public boolean isLookingAhead(){
		return ! fifo.isEmpty();
	}

	@Override
	public boolean hasNext() {
		return ! fifo.isEmpty() || stream.hasNext();
	}

	@Override
	public TermUri getTerm() {
		return stream.getTerm();
	}

}
