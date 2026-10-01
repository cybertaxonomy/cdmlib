/**
* Copyright (C) 2012 EDIT
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
import org.hibernate.search.mapper.pojo.bridge.TypeBridge;
import org.hibernate.search.mapper.pojo.bridge.binding.TypeBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.TypeBinder;
import org.hibernate.search.mapper.pojo.bridge.runtime.TypeBridgeWriteContext;

import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.Representation;

/**
 * Writes the label and the localized representations of a term into the index. The
 * representations are written as dynamic fields, one field per language plus one field
 * holding the representations of all languages:
 * <ul>
 * <li><code>representation.{text|label|abbreviatedLabel}.ALL</code></li>
 * <li><code>representation.{text|label|abbreviatedLabel}.{language-uuid}</code></li>
 * </ul>
 * With the <code>includeParentTerms</code> parameter set to <code>true</code> the uuids of
 * all transitive parent terms are written to <code>setOfParents</code> in addition.
 * <p>
 * The id and the uuid of the term are not written here, they are already contributed by
 * the mapping of {@link eu.etaxonomy.cdm.model.common.CdmBase}.
 *
 * @author Andreas Kohlbecker
 * @since Jun 4, 2012
 */
public class DefinedTermBaseClassBridge implements TypeBinder {

    public static final String INCLUDE_PARENT_TERMS_PARAM = "includeParentTerms";

    private static final String LABEL_FIELD = "label";
    private static final String SET_OF_PARENTS_FIELD = "setOfParents";
    private static final String ALL_LANGUAGES = "ALL";

    @Override
    public void bind(TypeBindingContext context) {

        //the representations and the parent terms are reached through associations, so as
        //with the former HS5 class bridge changes to them do not trigger a reindexing
        context.dependencies().useRootOnly();

        boolean includeParentTerms = Boolean.parseBoolean(
                (String)context.paramOptional(INCLUDE_PARENT_TERMS_PARAM).orElse("false"));

        IndexSchemaElement schema = context.indexSchemaElement();
        IndexFieldReference<String> labelRef = schema
                .field(LABEL_FIELD, f -> f.asString().analyzer(AnalyzerNames.DEFAULT)).toReference();
        IndexFieldReference<String> setOfParentsRef = schema
                .field(SET_OF_PARENTS_FIELD, f -> f.asString()).multiValued().toReference();

        IndexSchemaObjectField representation = schema.objectField("representation");
        IndexObjectFieldReference textRef = declareRepresentationLeaf(representation, "text");
        IndexObjectFieldReference labelLeafRef = declareRepresentationLeaf(representation, "label");
        IndexObjectFieldReference abbreviatedLabelRef = declareRepresentationLeaf(representation,
                "abbreviatedLabel");
        IndexObjectFieldReference representationRef = representation.toReference();

        context.bridge(Object.class, new Bridge(labelRef, setOfParentsRef, representationRef,
                textRef, labelLeafRef, abbreviatedLabelRef, includeParentTerms));
    }

    private static IndexObjectFieldReference declareRepresentationLeaf(IndexSchemaObjectField representation,
            String leafName) {

        IndexSchemaObjectField leaf = representation.objectField(leafName);
        leaf.fieldTemplate(leafName + "Lang", f -> f.asString().analyzer(AnalyzerNames.DEFAULT))
                .matchingPathGlob("*")
                .multiValued();
        return leaf.toReference();
    }

    private static class Bridge implements TypeBridge<Object> {

        private final IndexFieldReference<String> labelRef;
        private final IndexFieldReference<String> setOfParentsRef;
        private final IndexObjectFieldReference representationRef;
        private final IndexObjectFieldReference textRef;
        private final IndexObjectFieldReference labelLeafRef;
        private final IndexObjectFieldReference abbreviatedLabelRef;
        private final boolean includeParentTerms;

        Bridge(IndexFieldReference<String> labelRef, IndexFieldReference<String> setOfParentsRef,
                IndexObjectFieldReference representationRef, IndexObjectFieldReference textRef,
                IndexObjectFieldReference labelLeafRef, IndexObjectFieldReference abbreviatedLabelRef,
                boolean includeParentTerms) {
            this.labelRef = labelRef;
            this.setOfParentsRef = setOfParentsRef;
            this.representationRef = representationRef;
            this.textRef = textRef;
            this.labelLeafRef = labelLeafRef;
            this.abbreviatedLabelRef = abbreviatedLabelRef;
            this.includeParentTerms = includeParentTerms;
        }

        @Override
        public void write(DocumentElement target, Object value, TypeBridgeWriteContext writeContext) {

            if (value == null) {
                return;
            }
            DefinedTermBase<?> term = (DefinedTermBase<?>)value;

            target.addValue(labelRef, term.getLabel());

            if (!term.getRepresentations().isEmpty()) {
                DocumentElement representation = target.addObject(representationRef);
                DocumentElement text = representation.addObject(textRef);
                DocumentElement labelLeaf = representation.addObject(labelLeafRef);
                DocumentElement abbreviatedLabel = representation.addObject(abbreviatedLabelRef);
                for (Representation rep : term.getRepresentations()) {
                    addRepresentationField(text, rep.getText(), rep);
                    addRepresentationField(labelLeaf, rep.getLabel(), rep);
                    addRepresentationField(abbreviatedLabel, rep.getAbbreviatedLabel(), rep);
                }
            }

            // NamedArea previously declared its own TypeBinding with includeParentTerms=true;
            // keep that behaviour without a second TypeBinding (which duplicates "label" in HS6).
            boolean writeParents = includeParentTerms
                    || term instanceof eu.etaxonomy.cdm.model.location.NamedArea;
            if(writeParents){
                DefinedTermBase<?> parentTerm = term.getPartOf();
                while(parentTerm != null){
                    target.addValue(setOfParentsRef, parentTerm.getUuid().toString());
                    parentTerm = parentTerm.getPartOf();
                }
            }
        }

        private static void addRepresentationField(DocumentElement leaf, String text,
                Representation representation) {

            if(text == null){
                return;
            }
            leaf.addValue(ALL_LANGUAGES, text);
            if (representation.getLanguage() != null){
                leaf.addValue(representation.getLanguage().getUuid().toString(), text);
            }
        }
    }
}
