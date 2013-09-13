/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.common;

public class SourceConnectionException extends Exception {
	private static final long serialVersionUID = -3846939002083939654L;

	public SourceConnectionException() {
		super();
	}

	public SourceConnectionException(String message) {
		super(message);
	}

	public SourceConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public SourceConnectionException(Throwable cause) {
		super(cause);
	}

}
