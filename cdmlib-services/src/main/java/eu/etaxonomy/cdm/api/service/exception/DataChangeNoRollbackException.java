/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.exception;

import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

/**
 * Base class for all exceptions occurring during data change actions within the service layer.
 * NOTE: This exception needs to be thrown before any data is saved to the session as it rather forces 
 * commit then rollback. This is intended behavior to avoid full transaction rollback within longer
 * transactions. This way we do not need an explicit method to check if a data change method will
 * succeed or fail.<BR>
 * To avoid rollback the class on purpose does not inherit from RuntimeException
 * as RuntimeException leads to rollback when using {@link DefaultTransactionAttribute#rollbackOn(Throwable)}
 * which is used by spring as default transaction attribute.
 * @author a.mueller
 \* @since 13.10.2011
 *
 */
public class DataChangeNoRollbackException extends Exception {
	private static final long serialVersionUID = -5279586708452619581L;


	public DataChangeNoRollbackException() {
	}
	
	public DataChangeNoRollbackException(String message) {
		super(message);
	}


}
