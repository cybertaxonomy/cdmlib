/**
 * Copyright (C) 2011 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.hibernate.search;

import org.hibernate.search.engine.backend.analysis.AnalyzerNames;
import org.hibernate.search.engine.backend.document.DocumentElement;
import org.hibernate.search.engine.backend.document.IndexFieldReference;
import org.hibernate.search.engine.backend.document.IndexObjectFieldReference;
import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaElement;
import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaObjectField;
import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.engine.backend.types.Searchable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.bridge.TypeBridge;
import org.hibernate.search.mapper.pojo.bridge.binding.TypeBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.TypeBinder;
import org.hibernate.search.mapper.pojo.bridge.runtime.TypeBridgeWriteContext;

import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * This type binder is needed to overcome limitations in hibernate search with polymorphism
 * on associations: the <code>taxon</code> property is declared in {@link TaxonDescription}
 * and would therefore not be indexed when a description base is indexed - directly
 * or, as is the case in the CDM model, as indexed embedded of
 * {@link eu.etaxonomy.cdm.model.description.DescriptionElementBase#getInDescription()}.
 * <p>
 * All fields are written relative to the position at which the description is indexed, so
 * for the <code>inDescription</code> indexed embedded the taxon id for example ends up in
 * <code>inDescription.taxon.id</code>.
 *
 * @author Andreas Kohlbecker
 * @since Dec 19, 2011
 */
public class DescriptionBaseClassBridge implements TypeBinder {

    @Override
    public void bind(TypeBindingContext context) {

        // HS5 ContainedIn chain: TaxonNode → Taxon.descriptions → DescriptionBase → DescriptionElement.
        // Declare the same reindex path so classification / taxon-node / titleCache changes
        // update DescriptionElement documents that embed this description.
        context.dependencies()
                .fromOtherEntity(Taxon.class, "descriptions")
                .use("id")
                .use("uuid")
                .use("titleCache")
                .use("taxonNodes.treeIndex")
                .use("taxonNodes.classification.id");
        context.dependencies()
                .fromOtherEntity(TaxonName.class, "descriptions")
                .use("id");

        IndexSchemaElement schema = context.indexSchemaElement();

        IndexSchemaObjectField taxon = schema.objectField("taxon");
        IdFields taxonId = declareIdFields(taxon, false);
        IndexFieldReference<String> taxonTitleCacheRef = taxon
                .field("titleCache", f -> f.asString().analyzer(AnalyzerNames.DEFAULT).projectable(Projectable.YES))
                .toReference();
        IndexFieldReference<String> taxonTitleCacheSortRef = taxon
                .field("titleCache__sort", f -> f.asString().searchable(Searchable.NO).sortable(Sortable.YES))
                .toReference();
        IndexFieldReference<String> taxonUuidRef = taxon
                .field("uuid", f -> f.asString().projectable(Projectable.YES)).toReference();
        IndexSchemaObjectField taxonNodes = taxon.objectField("taxonNodes");
        IndexFieldReference<String> treeIndexRef = taxonNodes
                .field("treeIndex", f -> f.asString().projectable(Projectable.YES)).multiValued().toReference();
        IndexSchemaObjectField classification = taxonNodes.objectField("classification");
        IdFields classificationId = declareIdFields(classification, true);
        IndexObjectFieldReference classificationRef = classification.toReference();
        IndexObjectFieldReference taxonNodesRef = taxonNodes.toReference();
        IndexObjectFieldReference taxonRef = taxon.toReference();

        IndexSchemaObjectField taxonName = schema.objectField("taxonName");
        IdFields taxonNameId = declareIdFields(taxonName, false);
        IndexObjectFieldReference taxonNameRef = taxonName.toReference();

        context.bridge(Object.class, new Bridge(taxonRef, taxonId, taxonTitleCacheRef, taxonTitleCacheSortRef,
                taxonUuidRef, taxonNodesRef, treeIndexRef, classificationRef, classificationId, taxonNameRef,
                taxonNameId));
    }

    private static IdFields declareIdFields(IndexSchemaObjectField parent, boolean multiValued) {

        var idStep = parent.field("id", f -> f.asString().projectable(Projectable.YES));
        var sortStep = parent.field(NotNullAwareIdBridge.ID_FIELD_NAME + NotNullAwareIdBridge.SORT_FIELD_SUFFIX,
                f -> f.asString().searchable(Searchable.NO).sortable(Sortable.YES));
        var notNullStep = parent.field(
                NotNullAwareIdBridge.ID_FIELD_NAME + NotNullAwareIdBridge.NOT_NULL_FIELD_SUFFIX,
                f -> f.asString());
        if (multiValued) {
            idStep.multiValued();
            sortStep.multiValued();
            notNullStep.multiValued();
        }
        return new IdFields(idStep.toReference(), sortStep.toReference(), notNullStep.toReference());
    }

    private static final class IdFields {
        private final IndexFieldReference<String> idRef;
        private final IndexFieldReference<String> sortRef;
        private final IndexFieldReference<String> notNullRef;

        private IdFields(IndexFieldReference<String> idRef, IndexFieldReference<String> sortRef,
                IndexFieldReference<String> notNullRef) {
            this.idRef = idRef;
            this.sortRef = sortRef;
            this.notNullRef = notNullRef;
        }

        private void write(DocumentElement target, int id) {
            String value = Integer.toString(id);
            target.addValue(idRef, value);
            target.addValue(sortRef, value);
            target.addValue(notNullRef, NotNullAwareIdBridge.NOT_NULL_VALUE);
        }
    }

    private static final class Bridge implements TypeBridge<Object> {

        private final IndexObjectFieldReference taxonRef;
        private final IdFields taxonId;
        private final IndexFieldReference<String> taxonTitleCacheRef;
        private final IndexFieldReference<String> taxonTitleCacheSortRef;
        private final IndexFieldReference<String> taxonUuidRef;
        private final IndexObjectFieldReference taxonNodesRef;
        private final IndexFieldReference<String> treeIndexRef;
        private final IndexObjectFieldReference classificationRef;
        private final IdFields classificationId;
        private final IndexObjectFieldReference taxonNameRef;
        private final IdFields taxonNameId;

        private Bridge(IndexObjectFieldReference taxonRef, IdFields taxonId,
                IndexFieldReference<String> taxonTitleCacheRef,
                IndexFieldReference<String> taxonTitleCacheSortRef,
                IndexFieldReference<String> taxonUuidRef,
                IndexObjectFieldReference taxonNodesRef, IndexFieldReference<String> treeIndexRef,
                IndexObjectFieldReference classificationRef, IdFields classificationId,
                IndexObjectFieldReference taxonNameRef, IdFields taxonNameId) {
            this.taxonRef = taxonRef;
            this.taxonId = taxonId;
            this.taxonTitleCacheRef = taxonTitleCacheRef;
            this.taxonTitleCacheSortRef = taxonTitleCacheSortRef;
            this.taxonUuidRef = taxonUuidRef;
            this.taxonNodesRef = taxonNodesRef;
            this.treeIndexRef = treeIndexRef;
            this.classificationRef = classificationRef;
            this.classificationId = classificationId;
            this.taxonNameRef = taxonNameRef;
            this.taxonNameId = taxonNameId;
        }

        @Override
        public void write(DocumentElement target, Object entity, TypeBridgeWriteContext writeContext) {
            if (entity instanceof TaxonDescription) {
                writeTaxonDescription(target, (TaxonDescription) entity);
            }
            if (entity instanceof TaxonNameDescription) {
                TaxonName taxonName = ((TaxonNameDescription) entity).getTaxonName();
                if (taxonName != null) {
                    DocumentElement taxonNameElement = target.addObject(taxonNameRef);
                    taxonNameId.write(taxonNameElement, taxonName.getId());
                }
            }
        }

        private void writeTaxonDescription(DocumentElement target, TaxonDescription entity) {

            Taxon taxon = entity.getTaxon();
            if (taxon == null) {
                return;
            }

            DocumentElement taxonElement = target.addObject(taxonRef);
            taxonId.write(taxonElement, taxon.getId());
            String titleCache = taxon.getTitleCache();
            taxonElement.addValue(taxonTitleCacheRef, titleCache);
            taxonElement.addValue(taxonTitleCacheSortRef, titleCache);
            taxonElement.addValue(taxonUuidRef, taxon.getUuid().toString());

            if (taxon.getTaxonNodes().isEmpty()) {
                return;
            }
            DocumentElement taxonNodes = taxonElement.addObject(taxonNodesRef);
            DocumentElement classification = null;
            for (TaxonNode node : taxon.getTaxonNodes()) {
                if (node.treeIndex() != null) {
                    taxonNodes.addValue(treeIndexRef, node.treeIndex());
                }
                if (node.getClassification() != null) {
                    if (classification == null) {
                        classification = taxonNodes.addObject(classificationRef);
                    }
                    classificationId.write(classification, node.getClassification().getId());
                }
            }
        }
    }
}
