/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.stream.excel;

import java.util.HashMap;
import java.util.List;

import eu.etaxonomy.cdm.io.common.ObservableBase;
import eu.etaxonomy.cdm.io.stream.IItemStream;
import eu.etaxonomy.cdm.io.stream.StreamItem;
import eu.etaxonomy.cdm.io.stream.terms.TermUri;

/**
 * @author a.oppermann
 * @since 24.05.2013
 *
 */
public class ExcelMapReader extends ObservableBase implements IItemStream {

    private static final long serialVersionUID = -6409544362742718356L;

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

	@Override
	public StreamItem read() {
		HashMap<String, String> map = innerStream.get(index++);
		StreamItem item = convertMapToItem(map);
		return item;
	}

	private StreamItem convertMapToItem(HashMap<String, String> map) {
		StreamItem result = new StreamItem(termUri, map, streamLocation);
		return result;
	}

	@Override
	public boolean hasNext() {
		return innerStream.size() > index;
	}

	@Override
	public TermUri getTerm() {
		return termUri;
	}

	@Override
	public String getItemLocation() {
		//FIXME better dont use ExcelUtils !!
		return "FIXME";
	}

	@Override
	public String getStreamLocation() {
		return streamLocation;
	}
}
