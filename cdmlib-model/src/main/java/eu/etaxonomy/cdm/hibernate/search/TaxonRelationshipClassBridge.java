/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import java.util.Set;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.search.engine.backend.document.DocumentElement;
import org.hibernate.search.engine.backend.document.IndexObjectFieldReference;
import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaObjectField;
import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.mapper.pojo.bridge.TypeBridge;
import org.hibernate.search.mapper.pojo.bridge.binding.TypeBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.TypeBinder;
import org.hibernate.search.mapper.pojo.bridge.runtime.TypeBridgeWriteContext;

import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;

/**
 * Adds fields for related to and related from taxon relations. The relationship type
 * uuid is the dynamic leaf of the field name:
 * <ul>
 * <li><code>relation.from.id.{relationship-type-uuid}</code></li>
 * <li><code>relation.to.id.{relationship-type-uuid}</code></li>
 * </ul>
 * The former HS5 layout <code>relation.{uuid}.from.id</code> cannot be expressed in
 * Hibernate Search 6.1 because {@code *} in {@code matchingPathGlob} matches across dots
 * and would turn the leaf into an object field.
 *
 * @author a.kohlbecker
 * @since Sep 24, 2013
 */
public class TaxonRelationshipClassBridge implements TypeBinder {

    private static final Logger logger = LogManager.getLogger();

    public static final String FROM_ID_PREFIX = "relation.from.id.";
    public static final String TO_ID_PREFIX = "relation.to.id.";

    @Override
    public void bind(TypeBindingContext context) {

        // Reindex the taxon when its relationship collections change so relation.* fields stay current
        context.dependencies()
                .use("relationsFromThisTaxon")
                .use("relationsToThisTaxon");

        IndexSchemaObjectField relation = context.indexSchemaElement().objectField("relation");
        DirectionFields fromFields = declareDirectionIdField(relation, "from");
        DirectionFields toFields = declareDirectionIdField(relation, "to");
        IndexObjectFieldReference relationRef = relation.toReference();

        context.bridge(Object.class, new Bridge(relationRef, fromFields, toFields));
    }

    private static DirectionFields declareDirectionIdField(IndexSchemaObjectField relation,
            String direction) {

        IndexSchemaObjectField directionField = relation.objectField(direction);
        IndexSchemaObjectField idField = directionField.objectField("id");
        idField.fieldTemplate(direction + "TypeId", f -> f.asString().projectable(Projectable.YES))
                .matchingPathGlob("*")
                .multiValued();
        IndexObjectFieldReference idRef = idField.toReference();
        IndexObjectFieldReference directionRef = directionField.toReference();
        return new DirectionFields(directionRef, idRef);
    }

    private static final class DirectionFields {
        private final IndexObjectFieldReference directionRef;
        private final IndexObjectFieldReference idRef;

        private DirectionFields(IndexObjectFieldReference directionRef, IndexObjectFieldReference idRef) {
            this.directionRef = directionRef;
            this.idRef = idRef;
        }
    }

    private static final class Bridge implements TypeBridge<Object> {

        private final IndexObjectFieldReference relationRef;
        private final DirectionFields fromFields;
        private final DirectionFields toFields;

        private Bridge(IndexObjectFieldReference relationRef, DirectionFields fromFields,
                DirectionFields toFields) {
            this.relationRef = relationRef;
            this.fromFields = fromFields;
            this.toFields = toFields;
        }

        @Override
        public void write(DocumentElement target, Object entity, TypeBridgeWriteContext writeContext) {
            if (!(entity instanceof Taxon)) {
                logger.error("Unsupported type " + entity.getClass());
                return;
            }
            Taxon taxon = (Taxon) entity;
            boolean hasFrom = taxon.getRelationsToThisTaxon() != null
                    && !taxon.getRelationsToThisTaxon().isEmpty();
            boolean hasTo = taxon.getRelationsFromThisTaxon() != null
                    && !taxon.getRelationsFromThisTaxon().isEmpty();
            if (!hasFrom && !hasTo) {
                return;
            }
            DocumentElement relation = target.addObject(relationRef);
            if (hasFrom) {
                addRelationsFields(relation, fromFields, taxon.getRelationsToThisTaxon(),
                        TaxonRelationship::getFromTaxon);
            }
            if (hasTo) {
                addRelationsFields(relation, toFields, taxon.getRelationsFromThisTaxon(),
                        TaxonRelationship::getToTaxon);
            }
        }

        private static void addRelationsFields(DocumentElement relation, DirectionFields fields,
                Set<TaxonRelationship> relations, Function<TaxonRelationship, Taxon> relatedTaxon) {

            DocumentElement direction = relation.addObject(fields.directionRef);
            DocumentElement idObject = direction.addObject(fields.idRef);
            for (TaxonRelationship rel : relations) {
                Taxon relTaxon = relatedTaxon.apply(rel);
                if (relTaxon == null) {
                    continue;
                }
                String typeUuid = rel.getType() != null ? rel.getType().getUuid().toString() : "NULL";
                idObject.addValue(typeUuid, Integer.toString(relTaxon.getId()));
            }
        }
    }
}
