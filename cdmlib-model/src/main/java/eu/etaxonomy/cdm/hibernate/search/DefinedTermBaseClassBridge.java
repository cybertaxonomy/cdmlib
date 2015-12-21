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

import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.ParameterizedBridge;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Representation;

/**
 * @author Andreas Kohlbecker
 * @date Jun 4, 2012
 *
 */
public class DefinedTermBaseClassBridge extends AbstractClassBridge implements ParameterizedBridge {

    /**
     *
     */
    private static final String INCLUDE_PARENT_TERMS_KEY = "includeParentTerms";
    private boolean includeParentTerms = false;


    /* (non-Javadoc)
     * @see org.hibernate.search.bridge.FieldBridge#set(java.lang.String, java.lang.Object, org.apache.lucene.document.Document, org.hibernate.search.bridge.LuceneOptions)
     */
    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {

        if(value == null){
            return;
        }
        NotNullAwareIdBridge idFieldBridge = new NotNullAwareIdBridge();

        DefinedTermBase term = (DefinedTermBase)value;

        idFieldBridge.set(name + "id", term.getId(), document, idFieldOptions);

        Field uuidField = new StringField(name + "uuid",
                term.getUuid().toString(),
                luceneOptions.getStore());
        document.add(uuidField);

        Field langLabelField = new TextField(name + "label",
              term.getLabel(),
              luceneOptions.getStore());
        langLabelField.setBoost(luceneOptions.getBoost());
        document.add(langLabelField);

        for(Representation representation : term.getRepresentations()){
            addRepresentationField(name, representation, "text", representation.getText(), document, luceneOptions);
            addRepresentationField(name, representation, "label", representation.getLabel(), document, luceneOptions);
            addRepresentationField(name, representation, "abbreviatedLabel", representation.getAbbreviatedLabel(), document, luceneOptions);
        }

        if(includeParentTerms){

            DefinedTermBase parentTerm = term.getPartOf();
            while(parentTerm != null){
                Field setOfParentsField = new StringField(name + "setOfParents",
                        parentTerm.getUuid().toString(),
                        luceneOptions.getStore());
                document.add(setOfParentsField);
                parentTerm = parentTerm.getPartOf();
            }

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
        Field allField = new TextField(name + "representation." + representationField + ".ALL",
                text,
                luceneOptions.getStore());
        allField.setBoost(luceneOptions.getBoost());
        document.add(allField);

        Field langField = new TextField(name + "representation." + representationField + "."+ representation.getLanguage().getUuid().toString(),
                text,
                luceneOptions.getStore());
        allField.setBoost(luceneOptions.getBoost());
        document.add(langField);
    }

    /* (non-Javadoc)
     * @see org.hibernate.search.bridge.ParameterizedBridge#setParameterValues(java.util.Map)
     */
    @Override
    public void setParameterValues(Map<String, String> parameters) {
        if(parameters.containsKey(INCLUDE_PARENT_TERMS_KEY)){
            includeParentTerms = Boolean.parseBoolean(parameters.get(INCLUDE_PARENT_TERMS_KEY));
        }

    }


}
