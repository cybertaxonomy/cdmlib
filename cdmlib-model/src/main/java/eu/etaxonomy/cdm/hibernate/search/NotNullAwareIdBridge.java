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
import org.apache.lucene.document.StringField;
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

        /*
         * DocumentBuilderIndexedEntity<T>.buildDocumentFields(Object, Document, PropertiesMetadata, Map<String,String>, Set<String>)
         * is adding the idField a second time even if it has already been set by an idFieldBrige. this might be fixed in a
         * more recent version of hibernate! TODO after hibernate update: check if we can remove this extra condition.
         * We are avoiding this by checking the document:
         *
         */
        if(name.endsWith("id") && document.getField(name) != null) { // id already set?
            return;
        }

        Field field = new StringField(
                name,
                String.valueOf(value.toString()),
                luceneOptions.getStore());
        document.add(field);

        Field notNullField = new StringField(
                notNullField(name),
                String.valueOf(NOT_NULL_VALUE),
                Store.NO
                );
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
