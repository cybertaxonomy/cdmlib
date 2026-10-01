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
import org.hibernate.search.mapper.pojo.bridge.TypeBridge;
import org.hibernate.search.mapper.pojo.bridge.binding.TypeBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.TypeBinder;
import org.hibernate.search.mapper.pojo.bridge.runtime.TypeBridgeWriteContext;

import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * Lucene index type binder which sets the uuids of the accepted taxon for the
 * TaxonBase object into the index.
 * <p>
 * Adds id fields with the uuid and id of the accepted taxon of the
 * current {@link TaxonBase} entity. Id fields should not be analyzed, therefore
 * all fields written here are plain keyword fields.
 * <p>
 * Classification / tree-index fields for synonyms are contributed by
 * {@link SynonymTaxonNodesBridge} so they do not clash with {@code @IndexedEmbedded}
 * on {@link Taxon#getTaxonNodes()}.
 *
 * @author c.mathew
 * @author a.kohlbecker
 * @since 26 Jul 2013
 */
public class AcceptedTaxonBridge implements TypeBinder {

    public static final String FIELD_NAME_PARAM = "fieldName";

    private static final String ID_FIELD = "id";
    private static final String UUID_FIELD = "uuid";
    private static final String PUBLISH_FIELD = "publish";

    public final static String DOC_KEY_UUID_SUFFIX = "." + UUID_FIELD;
    public static final String DOC_KEY_ID_SUFFIX = "." + ID_FIELD;
    public final static String DOC_KEY_PUBLISH_SUFFIX = "." + PUBLISH_FIELD;
    public final static String DOC_KEY_TREEINDEX = "taxonNodes.treeIndex";
    public final static String DOC_KEY_CLASSIFICATION_ID = "taxonNodes.classification.id";
    public final static String ACCEPTED_TAXON = "accTaxon"; //there are probably still some places not using this constant, but for renaming in future we should try to use it everywhere

    @Override
    public void bind(TypeBindingContext context) {

        String prefix = (String)context.param(FIELD_NAME_PARAM);

        // Synonym documents copy accepted-taxon fields; reindex when that association changes.
        // Taxon documents copy their own publish flag into accTaxon.publish.
        if (context.bridgedElement().isAssignableTo(Synonym.class)) {
            context.dependencies()
                    .use("acceptedTaxon.id")
                    .use("acceptedTaxon.uuid")
                    .use("acceptedTaxon.publish");
        } else {
            context.dependencies().use("publish");
        }

        IndexSchemaObjectField accTaxonField = context.indexSchemaElement().objectField(prefix);
        IndexFieldReference<String> idRef = accTaxonField
                .field(ID_FIELD, f -> f.asString().projectable(Projectable.YES)).toReference();
        IndexFieldReference<String> uuidRef = accTaxonField
                .field(UUID_FIELD, f -> f.asString().projectable(Projectable.YES)).toReference();
        // Match HS6 @GenericField boolean encoding (IntPoint 1/0), see QueryFactory#newBooleanQuery
        IndexFieldReference<Integer> publishRef = accTaxonField
                .field(PUBLISH_FIELD, f -> f.asInteger().projectable(Projectable.YES)).toReference();
        IndexObjectFieldReference accTaxonRef = accTaxonField.toReference();

        context.bridge(TaxonBase.class, new Bridge(accTaxonRef, idRef, uuidRef, publishRef));
    }

    private static class Bridge implements TypeBridge<TaxonBase> {

        private final IndexObjectFieldReference accTaxonRef;
        private final IndexFieldReference<String> idRef;
        private final IndexFieldReference<String> uuidRef;
        private final IndexFieldReference<Integer> publishRef;

        Bridge(IndexObjectFieldReference accTaxonRef, IndexFieldReference<String> idRef,
                IndexFieldReference<String> uuidRef, IndexFieldReference<Integer> publishRef) {
            this.accTaxonRef = accTaxonRef;
            this.idRef = idRef;
            this.uuidRef = uuidRef;
            this.publishRef = publishRef;
        }

        @Override
        public void write(DocumentElement target, TaxonBase taxonBase, TypeBridgeWriteContext writeContext) {

            Taxon accTaxon;
            if(taxonBase instanceof Taxon){
                accTaxon = (Taxon)taxonBase;
            }else if (taxonBase instanceof Synonym){
                accTaxon = ((Synonym)taxonBase).getAcceptedTaxon();
            }else{
                throw new RuntimeException("Unhandled taxon base class: " + taxonBase.getClass().getSimpleName());
            }

            if(accTaxon == null) {
                return;
            }

            // in the case of taxon this is just the uuid
            DocumentElement accTaxonElement = target.addObject(accTaxonRef);
            accTaxonElement.addValue(idRef, Integer.toString(accTaxon.getId()));
            accTaxonElement.addValue(uuidRef, accTaxon.getUuid().toString());
            accTaxonElement.addValue(publishRef, accTaxon.isPublish() ? 1 : 0);
        }
    }
}
