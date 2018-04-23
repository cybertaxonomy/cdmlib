/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.search;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;

/**
 * Helper class, for printing out information on {@link DocIdSet}s
 *
 * @author a.kohlbecker
 * @since Sep 24, 2013
 *
 */
public class DocIdBitSetPrinter {

    public static final Logger logger = Logger.getLogger(DocIdBitSetPrinter.class);

    public static String docsAsString(DocIdSet docset, int maxdocs){

        StringBuilder sb = new StringBuilder();
        try {
            DocIdSetIterator it = docset.iterator();
            int i = 0;
            while(i < maxdocs){
                int docId = it.nextDoc();
                if(docId == DocIdSetIterator.NO_MORE_DOCS){
                    break;
                }
                sb.append(docId).append(" ");
            }
        } catch (IOException e) {
            logger.error("Error while reding doc ids from DocIdSet", e);
        }

        return sb.toString();
    }

}
