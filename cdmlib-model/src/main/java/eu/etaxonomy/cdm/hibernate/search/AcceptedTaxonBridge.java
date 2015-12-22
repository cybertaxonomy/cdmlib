/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * Lucene index class bridge which sets the uuids of the accepted taxon for the
 * TaxonBase object into the index.
 * <p>
 * Adds multivalued id fields with the uuid and id of the accepted taxa of the
 * current {@link TaxonBase} entity. Id fields should not be analyzed, therefore
 * this FieldBridge ignores the settings {@link org.hibernate.annotations.Index}
 * annotation and always sets this to <code>NOT_ANALYZED</code>.
 *
 *
 * @author c.mathew
 * @author a.kohlbecker
 * @version 1.0
 * @created 26 Jul 2013
 */
public class AcceptedTaxonBridge implements FieldBridge { // TODO inherit from AbstractClassBridge since this base class provides presets for id fields?

    public final static String ACCEPTED_TAXON_UUID_LIST_SEP = ",";
    private final static String DOC_KEY_UUID_SUFFIX = ".uuids"; // TODO do not use the plural for the field name
    private static final String DOC_KEY_ID_SUFFIX = ".id";

    @Override
    public void set(String name, Object value, Document document,
            LuceneOptions luceneOptions) {
        String accTaxonUuids = "";

        // in the case of taxon this is just the uuid
        if(value instanceof Taxon) {
            accTaxonUuids = ((Taxon)value).getUuid().toString();
            Field canonicalNameIdField = new StringField(name + DOC_KEY_ID_SUFFIX,
                    Integer.toString(((Taxon)value).getId()),
                    luceneOptions.getStore()
                    );
            document.add(canonicalNameIdField);
        }
        // in the case of synonym this is the accepted taxon in the synonym
        // relationships
        if (value instanceof Synonym) {
            StringBuilder sb = new StringBuilder();
            Synonym synonym = (Synonym) value;
            Set<SynonymRelationship> synRelationships = synonym.getSynonymRelations();
            for (SynonymRelationship sr : synRelationships) {
                Taxon accTaxon = sr.getAcceptedTaxon();
                sb.append(accTaxon.getUuid().toString());
                sb.append(ACCEPTED_TAXON_UUID_LIST_SEP);

                // adding the accTaxon id as multivalue field:
                Field canonicalNameIdField = new StringField(name + DOC_KEY_ID_SUFFIX,
                        Integer.toString(accTaxon.getId()),
                        luceneOptions.getStore()
                        );
                document.add(canonicalNameIdField);
            }
            accTaxonUuids = sb.toString();
            if(accTaxonUuids.length() > 0) {
                accTaxonUuids = accTaxonUuids.substring(0, accTaxonUuids.length()-1);
            }
        }

        // TODO can't we also add the uuid as multivalue field?

        // the id field is shorter and should be sufficient
        Field canonicalNameUuidField = new StringField(name + DOC_KEY_UUID_SUFFIX,
                accTaxonUuids,
                luceneOptions.getStore()
                );
        //TODO  do we really need to set the boost for an id field?
        canonicalNameUuidField.setBoost(luceneOptions.getBoost());
        document.add(canonicalNameUuidField);




    }
}
