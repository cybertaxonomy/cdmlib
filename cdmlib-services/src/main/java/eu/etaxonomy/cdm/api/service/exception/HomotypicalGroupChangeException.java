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
 * Exception that is thrown when a name that is to be changed or deleted belongs to homotypical group
 * which may lead to inconsistent data. Only throw this exception if the inconsistency can not be
 * resolved automatically.
 * @author a.mueller
 * @date 14.10.2011
 *
 */
public class HomotypicalGroupChangeException extends DataChangeNoRollbackException {
	private static final long serialVersionUID = -294632690489123786L;

	public HomotypicalGroupChangeException(){
		super();
	}
	
	public HomotypicalGroupChangeException(String message){
		super(message);
	}
}
