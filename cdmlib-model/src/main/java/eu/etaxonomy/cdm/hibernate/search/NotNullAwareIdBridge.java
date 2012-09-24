// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.TwoWayFieldBridge;

/**
 * This {@link TwoWayFieldBridge} allows to efficiently query for associated
 * entities which are not null. This field bridge works the following way:
 * <p>
 * It adds the id field to the document as if it would be done without the
 * intervention of this class, all field attributes are preserved, additionally
 * this field bridge also adds a field named <code>id.notNull</code> and stores
 * the term "true" for this field. So all associated entities which are not null
 * can now be queried by searching for <code>+id.notNull:true</code> which is
 * much more efficient than using range queries.
 * <p>
 * The <code>id.notNull</code> is stored with the following attributes :
 * {@link Store.NO},{@link Index.NOT_ANALYZED}, {@link TermVector.NO}.
 *
 * @author a.kohlbecker
 * @date Sep 21, 2012
 *
 */
public class NotNullAwareIdBridge implements TwoWayFieldBridge {

    public static final String NOT_NULL_VALUE = "1";
    public static final String NOT_NULL_FIELD_NAME = "notNull";

    public static String NULL_STRING = "";


    /**
     * @param name
     * @return
     */
    public static String notNullField(String name) {
        return name + "." + NOT_NULL_FIELD_NAME;
    }

    /* (non-Javadoc)
     * @see org.hibernate.search.bridge.FieldBridge#set(java.lang.String, java.lang.Object, org.apache.lucene.document.Document, org.hibernate.search.bridge.LuceneOptions)
     */
    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {

        Field field = new Field(name,
                String.valueOf(value.toString()),
                luceneOptions.getStore(), luceneOptions.getIndex(),
                luceneOptions.getTermVector());
        field.setBoost(luceneOptions.getBoost());
        document.add(field);

        Field notNullField = new Field(notNullField(name),
                String.valueOf(NOT_NULL_VALUE),
                Store.NO,
                Index.NOT_ANALYZED,
                TermVector.NO);
        document.add(notNullField);
    }

    @Override
    public Object get(String name, Document document) {
        return document.get(name);
    }

    /* (non-Javadoc)
     * @see org.hibernate.search.bridge.TwoWayFieldBridge#objectToString(java.lang.Object)
     */
    @Override
    public String objectToString(Object object) {
        if(object == null){
            return NULL_STRING;
        } else {
            return object.toString();
        }
    }

}
