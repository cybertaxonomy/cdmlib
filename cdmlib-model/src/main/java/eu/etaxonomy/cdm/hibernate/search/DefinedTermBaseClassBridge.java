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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.LuceneOptions;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Representation;

/**
 * @author Andreas Kohlbecker
 * @date Jun 4, 2012
 *
 */
public class DefinedTermBaseClassBridge extends AbstractClassBridge {

    /* (non-Javadoc)
     * @see org.hibernate.search.bridge.FieldBridge#set(java.lang.String, java.lang.Object, org.apache.lucene.document.Document, org.hibernate.search.bridge.LuceneOptions)
     */
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {

        if(value == null){
            return;
        }
        NotNullAwareIdBridge idFieldBridge = new NotNullAwareIdBridge();

        DefinedTermBase term = (DefinedTermBase)value;

        idFieldBridge.set(name + "id", term.getId(), document, idFieldOptions);

        Field uuidField = new Field(name + "uuid",
                term.getUuid().toString(),
                luceneOptions.getStore(),
                luceneOptions.getIndex(),
                luceneOptions.getTermVector());
        document.add(uuidField);

        Field langLabelField = new Field(name + "label",
                term.getLabel(),
                luceneOptions.getStore(),
                luceneOptions.getIndex(),
                luceneOptions.getTermVector());
        langLabelField.setBoost(luceneOptions.getBoost());
        document.add(langLabelField);

        for(Representation representation : term.getRepresentations()){
            addRepresentationField(name, representation, "text", representation.getText(), document, luceneOptions);
            addRepresentationField(name, representation, "label", representation.getLabel(), document, luceneOptions);
            addRepresentationField(name, representation, "abbreviatedLabel", representation.getAbbreviatedLabel(), document, luceneOptions);
        }
    }

    /**
     * @param name
     * @param representation
     * @param text
     * @param document
     * @param luceneOptions
     */
    private void addRepresentationField(String name, Representation representation, String representationField, String text, Document document, LuceneOptions luceneOptions) {
        if(text == null){
            return;
        }
        Field allField = new Field(name + "representation." + representationField + ".ALL",
                text,
                luceneOptions.getStore(),
                luceneOptions.getIndex(),
                luceneOptions.getTermVector());
        allField.setBoost(luceneOptions.getBoost());
        document.add(allField);

        Field langField = new Field(name + "representation." + representationField + "."+ representation.getLanguage().getUuid().toString(),
                text,
                luceneOptions.getStore(),
                luceneOptions.getIndex(),
                luceneOptions.getTermVector());
        allField.setBoost(luceneOptions.getBoost());
        document.add(langField);
    }

}
