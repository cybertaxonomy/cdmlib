/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.hibernate.search.engine.backend.document.DocumentElement;
import org.hibernate.search.engine.backend.document.IndexFieldReference;
import org.hibernate.search.engine.backend.document.IndexObjectFieldReference;
import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaObjectField;
import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.mapper.pojo.bridge.binding.TypeBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.TypeBinder;

/**
 * Lucene index type binder which sets class information for the objects into the index.
 * The fields are written below the object field given by the <code>fieldName</code>
 * parameter, so with <code>fieldName=classInfo</code> the documents get the fields
 * <code>classInfo.name</code> and <code>classInfo.canonicalName</code>.
 *
 * TODO: is this class really needed?
 *  1. the canonical name should for all cdm types be the same as the name
 *  2. the class name is already stored in the document as _hibernate_class
 *
 * @author c.mathew
 * @since 26 Jul 2013
 */
public class ClassInfoBridge implements TypeBinder {

    public static final String FIELD_NAME_PARAM = "fieldName";

    private static final String NAME_FIELD = "name";
    private static final String CANONICAL_NAME_FIELD = "canonicalName";

    @Override
    public void bind(TypeBindingContext context) {

        context.dependencies().useRootOnly();

        IndexSchemaObjectField classInfoField = context.indexSchemaElement()
                .objectField((String)context.param(FIELD_NAME_PARAM));
        IndexFieldReference<String> nameRef = classInfoField
                .field(NAME_FIELD, f -> f.asString().projectable(Projectable.YES)).toReference();
        IndexFieldReference<String> canonicalNameRef = classInfoField
                .field(CANONICAL_NAME_FIELD, f -> f.asString().projectable(Projectable.YES)).toReference();
        IndexObjectFieldReference classInfoRef = classInfoField.toReference();

        context.bridge(Object.class, (target, entity, writeContext) -> {
            DocumentElement classInfo = target.addObject(classInfoRef);
            classInfo.addValue(nameRef, entity.getClass().getName());
            classInfo.addValue(canonicalNameRef, entity.getClass().getCanonicalName());
        });
    }
}
