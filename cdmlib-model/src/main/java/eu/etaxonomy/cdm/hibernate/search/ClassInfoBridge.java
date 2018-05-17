/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
/**
 * Lucene index class bridge which sets class information for the objects into the index.
 *
 * TODO: is this class really needed?
 *  1. the canonical name should for all cdm types be he same as the name
 *  2. the class name is already stored in the document as _hibernate_class
 *
 * @author c.mathew
 * @version 1.0
 * @since 26 Jul 2013
 */
public class ClassInfoBridge implements FieldBridge {

    @Override
    public void set(String name, Object value, Document document,
            LuceneOptions luceneOptions) {
        Field nameField = new StringField(name + ".name",
                value.getClass().getName(),
                luceneOptions.getStore());
        nameField.setBoost(luceneOptions.getBoost());
        document.add(nameField);

        Field canonicalNameField = new StringField(name + ".canonicalName",
                value.getClass().getCanonicalName(),
                luceneOptions.getStore());
        canonicalNameField.setBoost(luceneOptions.getBoost());
        document.add(canonicalNameField);

    }
}
