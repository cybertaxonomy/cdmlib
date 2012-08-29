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
 * @author Andreas Kohlbecker
 * @date Jun 4, 2012
 *
 */
public class DefinedTermBaseClassBridge implements FieldBridge {

    /* (non-Javadoc)
     * @see org.hibernate.search.bridge.FieldBridge#set(java.lang.String, java.lang.Object, org.apache.lucene.document.Document, org.hibernate.search.bridge.LuceneOptions)
     */
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {

        if(value == null){
            return;
        }
        PaddedIntegerBridge idFieldBridge = new PaddedIntegerBridge();

        DefinedTermBase term = (DefinedTermBase)value;
        for(Representation representation : term.getRepresentations()){

            Field idField = new Field(name + "id",
                    idFieldBridge.objectToString(term.getId()),
                    luceneOptions.getStore(),
                    luceneOptions.getIndex(),
                    luceneOptions.getTermVector());
            document.add(idField);

            Field uuidField = new Field(name + "uuid",
                    term.getUuid().toString(),
                    luceneOptions.getStore(),
                    luceneOptions.getIndex(),
                    luceneOptions.getTermVector());
            document.add(uuidField);

            Field allField = new Field(name + "representation.ALL",
                    representation.getText(),
                    luceneOptions.getStore(),
                    luceneOptions.getIndex(),
                    luceneOptions.getTermVector());
                    allField.setBoost(luceneOptions.getBoost());
            document.add(allField);

            Field langField = new Field(name + "representation." + representation.getLanguage().getUuid().toString(),
                    representation.getText(),
                    luceneOptions.getStore(),
                    luceneOptions.getIndex(),
                    luceneOptions.getTermVector());
                    allField.setBoost(luceneOptions.getBoost());
            document.add(langField);
        }

    }

}
