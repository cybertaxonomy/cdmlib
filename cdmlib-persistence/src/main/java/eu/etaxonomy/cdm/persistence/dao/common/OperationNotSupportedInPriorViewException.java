/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.persistence.dao.common;

public class OperationNotSupportedInPriorViewException extends
		UnsupportedOperationException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7170039485943962416L;
	
	public OperationNotSupportedInPriorViewException(String message) {
		super(message);
	}



}
