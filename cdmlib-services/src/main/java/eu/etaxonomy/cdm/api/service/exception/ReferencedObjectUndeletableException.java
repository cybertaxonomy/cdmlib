/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.exception;


/**
 * Exception that is thrown when an object that is to be deleted within a deleteXXX
 * method can not be deleted due to other objects referencing the given object.
 * @author a.mueller
 * @date 12.10.2011
 *
 */
public class ReferencedObjectUndeletableException extends DataChangeNoRollbackException {
	private static final long serialVersionUID = -7232205281413184907L;

	
	public ReferencedObjectUndeletableException(){
		super();
	}
	
	public ReferencedObjectUndeletableException(String message){
		super(message);
	}
}
