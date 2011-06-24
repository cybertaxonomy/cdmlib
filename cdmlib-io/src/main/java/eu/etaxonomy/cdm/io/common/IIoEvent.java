/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

/**
 * Interface 
 * @author a.mueller
 * @created 23.06.2011
 */
public interface IIoEvent {

	public void fire(Object event);
	
}
