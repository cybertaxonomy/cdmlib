// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.excel.bfn;

import java.util.Set;

import eu.etaxonomy.cdm.io.common.IIoObservable;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.io.stream.IItemStream;
import eu.etaxonomy.cdm.io.stream.StreamItem;

/**
 * @author a.oppermann
 * @date 16.05.2013
 *
 */
public class ExcelRecordStream  implements IItemStream{

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.dwca.in.IReader#read()
	 */
	@Override
	public StreamItem read() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.dwca.in.IReader#hasNext()
	 */
	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.stream.IItemStream#getTerm()
	 */
	@Override
	public TermUri getTerm() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.stream.IItemStream#getItemLocation()
	 */
	@Override
	public String getItemLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.stream.IItemStream#getStreamLocation()
	 */
	@Override
	public String getStreamLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.stream.IItemStream#addObservers(java.util.Set)
	 */
	@Override
	public void addObservers(Set<IIoObserver> observers) {
		// TODO Auto-generated method stub
		
	}

}
