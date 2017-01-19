/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common.events;

/**
 * @author a.mueller
 * @date 24.06.2011
 *
 */
public class IoProgressEvent extends IoEventBase {

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s (%s, %s)",	getMessage(), getThrowingClass().getSimpleName(), getLocation());
	}
	
	
}
