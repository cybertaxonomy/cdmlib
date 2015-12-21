// $Id$
/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.hibernate.search.bridge.LuceneOptions;

import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;

/**
 * Adds fields for related to and related from taxon relations.
 *
 *
 * @author a.kohlbecker
 * @date Sep 24, 2013
 *
 */
public class TaxonRelationshipClassBridge extends AbstractClassBridge {

    public static final Logger logger = Logger.getLogger(TaxonRelationshipClassBridge.class);

    private static final String FROM = ".from.";
    private static final String TO = ".to.";

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.hibernate.search.AbstractClassBridge#set(java.lang.String, java.lang.Object, org.apache.lucene.document.Document, org.hibernate.search.bridge.LuceneOptions)
     */
    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        if(value instanceof Taxon){

            String fieldName = name;
            if(!fieldName.isEmpty()){
                fieldName += ".";
            }

            Taxon taxon = (Taxon)value;

            String directionName = FROM;
            addRelationsFields(fieldName, document, directionName, taxon.getRelationsToThisTaxon());

            directionName = TO;
            addRelationsFields(fieldName, document, directionName, taxon.getRelationsFromThisTaxon());

        } else {
            logger.error("Unsupported type " + value.getClass());
        }

    }

    /**
     * @param name
     * @param document
     * @param directionName
     * @param relations
     */
    private void addRelationsFields(String name, Document document, String directionName,
            Set<TaxonRelationship> relations) {
        Taxon relTaxon;


        for(TaxonRelationship rel : relations){

            if(directionName.equals(FROM)){
                relTaxon = rel.getFromTaxon();
            } else {
                relTaxon = rel.getToTaxon();
            }

            Field relfield = new StringField(
                    name + "relation." + rel.getType().getUuid().toString() + directionName + "id",
                    Integer.toString(relTaxon.getId()),
                    idFieldOptions.getStore());
            relfield.setBoost(idFieldOptions.getBoost());
            document.add(relfield);
        }
    }

}
