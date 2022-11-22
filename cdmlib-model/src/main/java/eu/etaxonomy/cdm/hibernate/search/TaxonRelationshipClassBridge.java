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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.hibernate.search.bridge.LuceneOptions;

import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;

/**
 * Adds fields for related to and related from taxon relations.
 *
 * @author a.kohlbecker
 * @since Sep 24, 2013
 */
public class TaxonRelationshipClassBridge extends AbstractClassBridge {

    private static final Logger logger = LogManager.getLogger();

    private static final String FROM = ".from.";
    private static final String TO = ".to.";

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

    private void addRelationsFields(String name, Document document, String directionName,
            Set<TaxonRelationship> relations) {


        for(TaxonRelationship rel : relations){

            Taxon relTaxon;
            if(directionName.equals(FROM)){
                relTaxon = rel.getFromTaxon();
            } else {
                relTaxon = rel.getToTaxon();
            }

            Field relField = new StringField(
                    name + "relation." + (rel.getType() != null ? rel.getType().getUuid().toString() : "NULL") + directionName + "id",
                    Integer.toString(relTaxon.getId()),
                    idFieldOptions.getStore());
            relField.setBoost(idFieldOptions.getBoost());
            document.add(relField);
        }
    }
}