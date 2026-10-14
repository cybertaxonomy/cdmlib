/**
* Copyright (C) 2026 EDIT
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

import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * Copies the accepted taxon's classification tree indexes into the Synonym document as
 * <code>taxonNodes.treeIndex</code> and <code>taxonNodes.classification.id</code>. Taxon
 * already receives these paths via {@code @IndexedEmbedded} on {@code taxonNodes}; Synonym
 * does not, so this binder is applied only to {@link Synonym}.
 */
public class SynonymTaxonNodesBridge implements TypeBinder {

    @Override
    public void bind(TypeBindingContext context) {

        // Reindex synonyms when the accepted taxon's node set changes
        context.dependencies().use("acceptedTaxon.taxonNodes");

        IndexSchemaObjectField taxonNodes = context.indexSchemaElement().objectField("taxonNodes");
        IndexFieldReference<String> treeIndexRef = taxonNodes
                .field("treeIndex", f -> f.asString().projectable(Projectable.YES)).multiValued().toReference();
        IndexSchemaObjectField classification = taxonNodes.objectField("classification");
        IndexFieldReference<String> classificationIdRef = classification
                .field("id", f -> f.asString().projectable(Projectable.YES)).multiValued().toReference();
        IndexObjectFieldReference classificationRef = classification.toReference();
        IndexObjectFieldReference taxonNodesRef = taxonNodes.toReference();

        context.bridge(Synonym.class, (target, synonym, writeContext) -> {
            Taxon accTaxon = synonym.getAcceptedTaxon();
            if (accTaxon == null || accTaxon.getTaxonNodes().isEmpty()) {
                return;
            }
            DocumentElement taxonNodesElement = target.addObject(taxonNodesRef);
            DocumentElement classificationElement = null;
            for (TaxonNode node : accTaxon.getTaxonNodes()) {
                if (node.treeIndex() != null) {
                    taxonNodesElement.addValue(treeIndexRef, node.treeIndex());
                }
                if (node.getClassification() != null) {
                    if (classificationElement == null) {
                        classificationElement = taxonNodesElement.addObject(classificationRef);
                    }
                    classificationElement.addValue(classificationIdRef,
                            Integer.toString(node.getClassification().getId()));
                }
            }
        });
    }
}
