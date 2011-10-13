// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

/**
 * Base class for all exceptions occurring during delete.
 * @author a.mueller
 * @date 13.10.2011
 *
 */
public class DeleteException extends Exception {
	private static final long serialVersionUID = -5279586708452619581L;


	public DeleteException() {
	}
	
	public DeleteException(String message) {
		super(message);
	}


}
