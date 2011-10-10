// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common.monitor;

/**
 * @author a.mueller
 * @date 10.10.2011
 *
 */
public class CancelException extends Exception {
	private static final long serialVersionUID = 4474587438264892095L;

	private static final String message = "Monitored task has been cancelled.";
	
	/**
	 * 
	 */
	public CancelException() {
		super(message);
	}
	


}
