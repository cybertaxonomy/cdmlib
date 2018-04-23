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

import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.io.stream.terms.TermUri;

/**
 * @author a.oppermann
 * @since 14.05.2013
 *
 */
public interface IItemStream extends INamespaceReader<StreamItem>{

	/**
	 * Returns the {@link TermUri} of this stream.
	 * @return
	 */
    @Override
    public TermUri getTerm();


	/**
	 * Returns the location of the current item in the original source. E.g. the line number in a csv file or the row number in an excel worksheet.
	 * {@link #getStreamLocation()}
	 * @return
	 */
	public String getItemLocation();

	/**
	 * Returns the location of the complete stream the original source.
	 * E.g. taxon.txt in a DwC-A file or the worksheet name in an Excel import.
	 * @see #getItemLocation()
	 * @return
	 */
	public String getStreamLocation();


	/**
	 * Add the observers for this item stream.
	 * @param observers
	 */
	public void addObservers(Set<IIoObserver> observers);
}
