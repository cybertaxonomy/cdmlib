/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.stream;

import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.io.stream.terms.TermUri;

/**
 * FilteredReader filters a stream. Only a sub-stream of the input items
 * is in the output stream.
 * @author a.mueller
 \* @since 23.11.2011
 *
 */
public class FilteredStream implements IItemStream {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(FilteredStream.class);

	private final IItemStream reader;
	private final ItemFilter<StreamItem> filter;
	private StreamItem next;

	/**
	 * @param list
	 */
	public FilteredStream(IItemStream reader, ItemFilter<StreamItem> filter) {
		this.reader = reader;
		this.filter = filter;
	}

	@Override
	public StreamItem read() {
	    StreamItem result;
	    if (hasNext()){
		    result = next;
		    next = null;
		}else{
		    result = null;
		}
	    return result;
	}

	@Override
	public boolean hasNext(){
	    if (next != null){
	        return true;
	    }
		while (reader.hasNext()){
		    next = reader.read();
		    if (filter == null || ! filter.toBeRemovedFromStream(next)){
		        return true;
		    }
		}
		next = null;
	    return false;
	}

    @Override
    public TermUri getTerm() {
        return reader.getTerm();
    }

    @Override
    public String getItemLocation() {
        return reader.getItemLocation();
    }

    @Override
    public String getStreamLocation() {
        return reader.getStreamLocation();
    }

    @Override
    public void addObservers(Set<IIoObserver> observers) {
        reader.addObservers(observers);
    }

}
