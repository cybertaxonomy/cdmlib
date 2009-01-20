package eu.etaxonomy.cdm.persistence.dao;

import org.springframework.dao.InvalidDataAccessApiUsageException;

public class QueryParseException extends InvalidDataAccessApiUsageException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6534946789729197016L;

	public QueryParseException(Exception e, String queryString) {
		super(queryString, e);
	}

}
