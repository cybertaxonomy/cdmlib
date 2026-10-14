/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaElement;
import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaFieldTemplateOptionsStep;
import org.hibernate.search.engine.backend.types.ObjectStructure;
import org.hibernate.search.engine.backend.types.dsl.IndexFieldTypeFactory;
import org.hibernate.search.engine.backend.types.dsl.IndexFieldTypeFinalStep;

/**
 * Helpers for declaring Hibernate Search 6 dynamic fields whose Lucene names contain
 * dots. In HS6 a dot is an object-field path separator, so every intermediate segment of
 * a path such as <code>relation.{uuid}.from.id</code> must be declared as an object field
 * (typically via {@link IndexSchemaElement#objectFieldTemplate(String, ObjectStructure)})
 * before a value can be written with {@code DocumentElement#addValue(String, Object)}.
 *
 * @author a.mueller
 */
public final class DynamicIndexFields {

    private DynamicIndexFields() {
    }

    /**
     * Declares flattened object-field templates for every ancestor segment of
     * <code>pathOrGlob</code>. Already declared paths in <code>declaredObjectPaths</code>
     * are skipped so overlapping paths (e.g. <code>taxon.id</code> and
     * <code>taxon.titleCache</code>) can share the same <code>taxon</code> object field.
     * <p>
 * Wildcards (<code>*</code>) in intermediate segments must be avoided for object-field
 * templates: in Hibernate Search 6.1 {@code *} matches across dots, so a glob such as
 * <code>relation.*</code> would also match leaf paths like
 * <code>relation.{uuid}.from.id</code> and create them as object fields.
     */
    public static void declareAncestorObjects(IndexSchemaElement schema, String pathOrGlob,
            Set<String> declaredObjectPaths) {

        int lastDot = pathOrGlob.lastIndexOf('.');
        if (lastDot < 0) {
            return;
        }
        String[] parts = pathOrGlob.split("\\.");
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (i > 0) {
                path.append('.');
            }
            path.append(parts[i]);
            String objectPath = path.toString();
            if (!declaredObjectPaths.add(objectPath)) {
                continue;
            }
            schema.objectFieldTemplate(templateName("obj_" + objectPath), ObjectStructure.FLATTENED)
                    .matchingPathGlob(objectPath);
        }
    }

    /**
     * Convenience overload that starts with an empty set of already declared object paths.
     */
    public static void declareAncestorObjects(IndexSchemaElement schema, String pathOrGlob) {
        declareAncestorObjects(schema, pathOrGlob, new HashSet<>());
    }

    /**
     * Declares a dynamic value-field template. Template names must be unique within the
     * index schema element and must not contain dots.
     */
    public static void declareValueTemplate(IndexSchemaElement schema, String pathGlob,
            Function<? super IndexFieldTypeFactory, ? extends IndexFieldTypeFinalStep<?>> type,
            boolean multiValued) {

        IndexSchemaFieldTemplateOptionsStep<?> template = schema
                .fieldTemplate(templateName(pathGlob), type)
                .matchingPathGlob(pathGlob);
        if (multiValued) {
            template.multiValued();
        }
    }

    public static String templateName(String pathGlob) {
        return pathGlob.replace('.', '_').replace('*', '_');
    }
}
