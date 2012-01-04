// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;

/**
 * This class represents the result of a delete action.
 * 
 * @author a.mueller
 * @date 04.01.2012
 *
 */
public class DeletionResult {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DeletionResult.class);
	
	private DeleteStatus status;
	
	private Exception exception;
	
	public enum DeleteStatus{
		OK(0),
		ABORT(1),
		ERROR(3),
		;
		
		int index;
		private DeleteStatus(int index){
			this.index = index;
		}
	}

	/**
	 * The resuting status of a delete action.
	 * 
	 * @see DeleteStatus
	 * @return
	 */
	public DeleteStatus getStatus() {
		return status;
	}
	public void setStatus(DeleteStatus status) {
		this.status = status;
	}

	/**
	 * The highest exception that occurred during delete (if any).
	 * @return
	 */
	public Exception getException() {
		return exception;
	}
	public void setException(Exception exception) {
		this.exception = exception;
	}
	
}
