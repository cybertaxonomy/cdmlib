/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.search.engine.backend.document.DocumentElement;
import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaElement;
import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.engine.backend.types.Searchable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.bridge.binding.PropertyBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.PropertyBinder;

/**
 * This {@link PropertyBinder} allows to efficiently query for associated entities
 * which are not null. For the entity id it contributes three fields to the document:
 * <ol>
 * <li><code>{name}</code>: the id itself, as a not analyzed and stored string</li>
 * <li><code>{name}__sort</code>: a doc value field with the same content, which is
 * required by Lucene sorts and by the join queries used in the CDM searches</li>
 * <li><code>{name}__notNull</code>: holds the term {@link #NOT_NULL_VALUE} whenever the
 * id is set. All associated entities which are not null can therefore be queried by
 * searching for <code>+{name}__notNull:1</code> which is much more efficient than using
 * range queries. The double-underscore suffix (instead of the former
 * <code>{name}.notNull</code>) is required because Hibernate Search 6 treats dots as
 * object-field path separators and rejects a value field sibling under the same name as
 * the id value field.</li>
 * </ol>
 * The class level binders of this package contribute the same set of fields for the ids
 * they write, using {@link #declareIdFields(IndexSchemaElement, String, boolean)} and
 * {@link #writeIdFields(DocumentElement, String, int)}.
 *
 * @author a.kohlbecker
 * @since Sep 21, 2012
 */
public class NotNullAwareIdBridge implements PropertyBinder {

    public static final String NOT_NULL_VALUE = "1";
    public static final String NOT_NULL_FIELD_NAME = "notNull";
    public static final String SORT_FIELD_SUFFIX = "__sort";
    public static final String NOT_NULL_FIELD_SUFFIX = "__" + NOT_NULL_FIELD_NAME;

    /**
     * The name of the id field, relative to the type the id belongs to. Not to be
     * confused with the document id, which Hibernate Search stores in an internal field.
     */
    public static final String ID_FIELD_NAME = "id";

    public static String notNullField(String idFieldName) {
        return idFieldName + NOT_NULL_FIELD_SUFFIX;
    }

    public static String sortField(String idFieldName) {
        return idFieldName + SORT_FIELD_SUFFIX;
    }

    @Override
    public void bind(PropertyBindingContext context) {

        context.dependencies().useRootOnly();

        declareIdFields(context.indexSchemaElement(), ID_FIELD_NAME, false);

        context.bridge(Integer.class, (target, id, writeContext) -> {
            if (id != null) {
                writeIdFields(target, ID_FIELD_NAME, id);
            }
        });
    }

    /**
     * Declares the index fields described in {@link NotNullAwareIdBridge} for all id
     * paths matching <code>idFieldGlob</code>.
     * <p>
     * The fields are declared as dynamic fields because their names contain dots, which
     * Hibernate Search 6 only accepts for dynamic fields. Ids written under a path which
     * is already declared statically - by an <code>@IndexedEmbedded</code> for example -
     * end up in that static field instead.
     *
     * @param idFieldGlob
     *            the path of the id field relative to <code>schema</code>, optionally
     *            containing the <code>*</code> wildcard
     * @param multiValued
     *            whether more than one id may be written to the same path, as is the case
     *            for ids read from a collection
     */
    public static void declareIdFields(IndexSchemaElement schema, String idFieldGlob, boolean multiValued) {
        declareIdFields(schema, idFieldGlob, multiValued, new HashSet<>());
    }

    /**
     * Same as {@link #declareIdFields(IndexSchemaElement, String, boolean)} but reuses
     * <code>declaredObjectPaths</code> so overlapping dotted paths can share ancestor
     * object-field templates.
     */
    public static void declareIdFields(IndexSchemaElement schema, String idFieldGlob,
            boolean multiValued, Set<String> declaredObjectPaths) {

        DynamicIndexFields.declareAncestorObjects(schema, idFieldGlob, declaredObjectPaths);
        DynamicIndexFields.declareValueTemplate(schema, idFieldGlob,
                f -> f.asString().projectable(Projectable.YES), multiValued);
        DynamicIndexFields.declareValueTemplate(schema, sortField(idFieldGlob),
                f -> f.asString().searchable(Searchable.NO).sortable(Sortable.YES), multiValued);
        DynamicIndexFields.declareValueTemplate(schema, notNullField(idFieldGlob),
                f -> f.asString(), multiValued);
    }

    /**
     * Writes the index fields described in {@link NotNullAwareIdBridge}. The fields must
     * have been declared by {@link #declareIdFields(IndexSchemaElement, String, boolean)}.
     */
    public static void writeIdFields(DocumentElement target, String idFieldName, int id) {

        String value = Integer.toString(id);
        target.addValue(idFieldName, value);
        target.addValue(sortField(idFieldName), value);
        target.addValue(notNullField(idFieldName), NOT_NULL_VALUE);
    }
}
