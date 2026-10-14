/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import java.util.Map;

import org.hibernate.search.engine.backend.analysis.AnalyzerNames;
import org.hibernate.search.engine.backend.document.DocumentElement;
import org.hibernate.search.engine.backend.document.IndexObjectFieldReference;
import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaObjectField;
import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.mapper.pojo.bridge.PropertyBridge;
import org.hibernate.search.mapper.pojo.bridge.binding.PropertyBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.PropertyBinder;
import org.hibernate.search.mapper.pojo.bridge.runtime.PropertyBridgeWriteContext;

import eu.etaxonomy.cdm.model.common.LanguageString;

/**
 * Multilingual text representations, for example in TextData, are modeled in the cdm
 * as <code>Map&lt;Language, LanguageString&gt; multilanguageText</code>. This binder
 * stores each of these language specific strings in the Lucene document in two fields,
 * whereas {name} is set by the <code>fieldName</code> parameter and will be most probably
 * 'text' or 'multilanguageText':
 * <ol>
 * <li><code>{name}.ALL</code>: this field contains all strings regardless of the language they are associated with.</li>
 * <li></li><code>{name}.{language-uuid}</code>: contains the strings of the specific language indicated by {language-uuid}.
 * </ol>
 * The fields are written relative to the type holding the property, not relative to the
 * property itself, so the <code>fieldName</code> parameter fully determines the field
 * name prefix.
 *
 * @author Andreas Kohlbecker
 * @since Jun 4, 2012
 */
public class MultilanguageTextFieldBridge implements PropertyBinder {

    public static final String FIELD_NAME_PARAM = "fieldName";

    private static final String ALL_LANGUAGES = "ALL";

    @Override
    public void bind(PropertyBindingContext context) {

        // LanguageString has no inverse association back to TextData/etc.;
        // shallow dependency matches former HS5 ContainedIn-less behaviour.
        context.dependencies().useRootOnly();

        String fieldName = (String)context.param(FIELD_NAME_PARAM);

        // Object field + language leaf templates keep dotted Lucene names while remaining
        // valid when this binder runs on an @IndexedEmbedded path.
        IndexSchemaObjectField field = context.indexSchemaElement().objectField(fieldName);
        field.fieldTemplate(fieldName + "Lang",
                        f -> f.asString().analyzer(AnalyzerNames.DEFAULT).projectable(Projectable.YES))
                .matchingPathGlob("*")
                .multiValued();
        IndexObjectFieldReference fieldRef = field.toReference();

        context.bridge(Map.class, new Bridge(fieldRef));
    }

    @SuppressWarnings("rawtypes") //the binder is applied to Map<Language, LanguageString> properties
    private static class Bridge implements PropertyBridge<Map> {

        private final IndexObjectFieldReference fieldRef;

        Bridge(IndexObjectFieldReference fieldRef) {
            this.fieldRef = fieldRef;
        }

        @Override
        public void write(DocumentElement target, Map multilanguageText, PropertyBridgeWriteContext writeContext) {

            if (multilanguageText == null || multilanguageText.isEmpty()) {
                return;
            }
            DocumentElement field = target.addObject(fieldRef);
            for(Object value : multilanguageText.values()){

                LanguageString languageString = (LanguageString)value;
                if(languageString.getText() == null) {
                    // an empty field is of no use for searching
                    continue;
                }
                field.addValue(ALL_LANGUAGES, languageString.getText());
                field.addValue(languageString.getLanguage().getUuid().toString(),
                        languageString.getText());
            }
        }
    }
}
