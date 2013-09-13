// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.excel.stream;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.io.common.ObservableBase;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.io.stream.IItemStream;
import eu.etaxonomy.cdm.io.stream.StreamItem;

/**
 * @author a.oppermann
 * @date 24.05.2013
 *
 */
public class ExcelMapReader extends ObservableBase implements IItemStream {

	private List<HashMap<String, String>> innerStream;
	int index = 0;
	private TermUri termUri;
	private String streamLocation;
	
	/**
	 * @param innerStream
	 */
	public ExcelMapReader(List<HashMap<String, String>> innerStream, TermUri termUri, String streamLocation) {
		super();
		this.innerStream = innerStream;
		index = 0;
		this.termUri = termUri;
		this.streamLocation = streamLocation;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.dwca.in.IReader#read()
	 */
	@Override
	public StreamItem read() {
		HashMap<String, String> map = innerStream.get(index++);
		StreamItem item = convertMapToItem(map);
		return item;
	}

	/**
	 * @param map
	 * @return
	 */
	private StreamItem convertMapToItem(HashMap<String, String> map) {
		StreamItem result = new StreamItem(termUri, map, streamLocation);
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.dwca.in.IReader#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return innerStream.size() > index;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.stream.IItemStream#getTerm()
	 */
	@Override
	public TermUri getTerm() {
		return termUri;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.stream.IItemStream#getItemLocation()
	 */
	@Override
	public String getItemLocation() {
		//FIXME better dont use ExcelUtils !!
		return "FIXME";
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.stream.IItemStream#getStreamLocation()
	 */
	@Override
	public String getStreamLocation() {
		return streamLocation;
	}


}
