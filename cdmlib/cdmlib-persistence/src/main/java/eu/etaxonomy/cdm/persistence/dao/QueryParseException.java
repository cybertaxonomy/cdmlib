/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.persistence.dao;

import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * @author b.clark
 */
public class QueryParseException extends InvalidDataAccessApiUsageException {
	private static final long serialVersionUID = 6534946789729197016L;

	public QueryParseException(Exception e, String queryString) {
		super(queryString, e);
	}

}
