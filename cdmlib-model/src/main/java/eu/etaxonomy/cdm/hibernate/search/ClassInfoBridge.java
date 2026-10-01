/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.hibernate.search.engine.backend.document.IndexFieldReference;
import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.mapper.pojo.bridge.binding.TypeBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.TypeBinder;

/**
 * Indexes the entity's fully qualified class name as a projectable keyword field.
 * Replaces Hibernate Search 5 {@code _hibernate_class} /
 * {@code ProjectionConstants.OBJECT_CLASS}, which HS6 no longer writes.
 * <p>
 * With {@code fieldName=classInfo} the document field is simply {@code classInfo}.
 * For CDM entity types {@link Class#getName()} equals {@link Class#getCanonicalName()},
 * so a separate canonical-name field is unnecessary.
 *
 * @author c.mathew
 * @since 26 Jul 2013
 */
public class ClassInfoBridge implements TypeBinder {

    public static final String FIELD_NAME_PARAM = "fieldName";

    @Override
    public void bind(TypeBindingContext context) {

        context.dependencies().useRootOnly();

        String fieldName = (String) context.param(FIELD_NAME_PARAM);
        IndexFieldReference<String> classInfoRef = context.indexSchemaElement()
                .field(fieldName, f -> f.asString().projectable(Projectable.YES))
                .toReference();

        context.bridge(Object.class, (target, entity, writeContext) ->
                target.addValue(classInfoRef, entity.getClass().getName()));
    }
}
