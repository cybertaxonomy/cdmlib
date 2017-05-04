/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.search;

/**
 * Wrapper class for org.apache.lucene.queryparser.classic.ParseException
 * @author pplitzner
 * @since Apr 7, 2017
 *
 */
@SuppressWarnings("serial")
public class LuceneParseException extends Exception {

    /**
     * @param message
     */
    public LuceneParseException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

}
