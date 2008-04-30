/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.fetch;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @created 30.04.2008
 * @version 1.0
 */
public class Fetch {
	private static final Logger logger = Logger.getLogger(Fetch.class);
	 
	private static final int FETCH_DESCRIPTIONS = 2^1;
	private static final int FETCH_CHILDTAXA = 2^2;
	private static final int FETCH_PARENT_TAXA = 2^3;
	private static final int FETCH_ANNOTATIONS = 2^4;
	private static final int FETCH_MARKER = 2^5;
	private static final int FETCH_SYNONYMS = 2^6;
	


	
	
}
