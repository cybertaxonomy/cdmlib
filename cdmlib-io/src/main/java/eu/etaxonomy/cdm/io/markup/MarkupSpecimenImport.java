/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.markup;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @created 30.05.2012
 * 
 */
public class MarkupSpecimenImport  {
	private static final Logger logger = Logger.getLogger(MarkupSpecimenImport.class);

	private MarkupDocumentImport docImport;
	

	public MarkupSpecimenImport(MarkupDocumentImport docImport) {
		super();
		this.docImport = docImport;
	}


}
