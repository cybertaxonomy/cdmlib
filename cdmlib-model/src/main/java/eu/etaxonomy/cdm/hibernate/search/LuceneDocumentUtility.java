/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DocValuesType;

/**
 * @author a.kohlbecker
 * @since Nov 27, 2015
 *
 */
public class LuceneDocumentUtility {

    public static final Logger logger = Logger.getLogger(LuceneDocumentUtility.class);

    /**
     * @param field
     * @param document
     */
    public static void setOrReplaceDocValueField(Field field, Document document) {

        if(field.fieldType().docValuesType().equals(DocValuesType.NONE)) {
            throw new IllegalArgumentException("Supplied field is not a DocValuesField");
        }

        if(document.getField(field.name()) != null) {
            // need to manage update by first removing the field
            if(logger.isTraceEnabled()) {
                logger.trace("update DocValueField " + field.name() + " in " + document.get("_hibernate_class") + " " + document.get("uuid"));
            }
            document.removeField(field.name());
        }
        document.add(field);
    }

}
