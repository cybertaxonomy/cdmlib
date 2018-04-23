/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.stream;

import eu.etaxonomy.cdm.io.stream.terms.TermUri;

/**
 * @author a.mueller
 \* @since 10.03.2012
 *
 */
public interface INamespace {

	
	/**
	 * Returns the namespace of the items included in the stream
	 * @return the term
	 */
	public TermUri getTerm();
	
}
