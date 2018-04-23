/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import java.util.Collection;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;

/**
 * Multilingual text representations, for example in TextData, are modeled in the cdm
 * as <code>Map<Language, LanguageString> multilanguageText</code>. This FieldBridge implementation
 * stores each of these language specific strings in the Lucene document in two fields, whereas {name}
 * is set by the name parameter and will be most probably 'text' or 'multilanguageText':
 * <ol>
 * <li><code>{name}.ALL</code>: this field contains all strings regardless of the language they are associated with.</li>
 * <li></li><code>{name}.{language-label}</code>: contains the strings of the specific language indicated by {language-label}.
 * </ol>
 *
 * @author Andreas Kohlbecker
 \* @since Jun 4, 2012
 *
 */
public class MultilanguageTextFieldBridge implements FieldBridge {

    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        // value should be the Map<Language, LanguageString>
        @SuppressWarnings("unchecked")
        Collection<LanguageString> langStrings = ((Map<Language, LanguageString>)value).values();
        for(LanguageString languageString : langStrings){

            if(languageString.getText() == null) {
                // TextField.value cannot be null
                continue;
            }

            Field allField = new TextField(name + ".ALL",
                    languageString.getText(),
                    luceneOptions.getStore());
            allField.setBoost(luceneOptions.getBoost());
            document.add(allField);

            Field langField = new TextField(name + "." + languageString.getLanguage().getUuid(),
                    languageString.getText(),
                    luceneOptions.getStore()
                    );
            allField.setBoost(luceneOptions.getBoost());
            document.add(langField);
        }

    }

}
