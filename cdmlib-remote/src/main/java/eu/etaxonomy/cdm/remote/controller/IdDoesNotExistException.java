/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import eu.etaxonomy.cdm.model.common.LSID;

public class IdDoesNotExistException extends Exception {

    private static final long serialVersionUID = -6839922198349083678L;

    public IdDoesNotExistException(LSID identifier) {
		// TODO Auto-generated constructor stub
	}

}
