/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.stream;

/**
 * @author a.mueller
 \* @since 23.11.2011
 *
 */
public interface IReader<TYPE extends Object> extends IConverterOutput<IReader<TYPE>>{

	/**
	 * Returns the next Object of this reader.
	 * Returns <code>null</code> if no object is left.
	 * @return
	 */
	public TYPE read();
	
	/**
	 * Checks the availablity of a next object in the stream.
	 * @return True if there is a next object, false otherwise.
	 */
	boolean hasNext();
	
	
}
