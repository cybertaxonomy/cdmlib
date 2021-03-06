/**
 * Copyright (C) 2014 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.cache;

public class CdmClientCacheException extends RuntimeException {

    private static final long serialVersionUID = -7933042695466513846L;

    public CdmClientCacheException(String message) {
		super(message);
	}

	public CdmClientCacheException(Exception e) {
		super(e);
	}
}
