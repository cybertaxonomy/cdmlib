// $Id$
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
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Representation;

/**
 *
 *
 * @author Andreas Kohlbecker
 * @date Jun 4, 2012
 *
 * * @deprecated no not use, this class was only created for testing purposes
 *
 */
@Deprecated
public class LanguageFieldBridge implements FieldBridge {


    /* (non-Javadoc)
     * @see org.hibernate.search.bridge.FieldBridge#set(java.lang.String, java.lang.Object, org.apache.lucene.document.Document, org.hibernate.search.bridge.LuceneOptions)
     */
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {

        if(value == null){
            return;
        }

        Language language = (Language)value;

        Field idField = new Field(name + ".id",
                String.valueOf(language.getId()),
                luceneOptions.getStore(),
                luceneOptions.getIndex(),
                luceneOptions.getTermVector());
        idField.setBoost(luceneOptions.getBoost());
        document.add(idField);

        Field uuidField = new Field(name + ".uuid",
                language.getUuid().toString(),
                luceneOptions.getStore(),
                luceneOptions.getIndex(),
                luceneOptions.getTermVector());
        uuidField.setBoost(luceneOptions.getBoost());
        document.add(uuidField);

        Field langLabelField = new Field(name + ".label",
                language.getLabel(),
                luceneOptions.getStore(),
                luceneOptions.getIndex(),
                luceneOptions.getTermVector());
        langLabelField.setBoost(luceneOptions.getBoost());
        document.add(langLabelField);
    }

}
