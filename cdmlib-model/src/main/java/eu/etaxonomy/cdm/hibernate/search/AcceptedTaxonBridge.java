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
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * Lucene index class bridge which sets the uuids of the accepted taxon for the 
 * TaxonBase object into the index.
 *
 * @author c.mathew
 * @version 1.0
 * @created 26 Jul 2013
 */
public class AcceptedTaxonBridge implements FieldBridge {

	public final static String ACCEPTED_TAXON_UUID_LIST_SEP = ",";
	private final static String DOC_KEY_SUFFIX = ".uuids";
	
	@Override
	public void set(String name, Object value, Document document,
			LuceneOptions luceneOptions) {
		String accTaxonUuids = "";
		
		// in the case of taxon this is just the uuid
		if(value instanceof Taxon) {
			accTaxonUuids = ((Taxon)value).getUuid().toString();
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
            }
            accTaxonUuids = sb.toString();
            if(accTaxonUuids.length() > 0) {
            	accTaxonUuids = accTaxonUuids.substring(0, accTaxonUuids.length()-1);
            }
        }

        Field canonicalNameField = new Field(name + DOC_KEY_SUFFIX,
        		accTaxonUuids,
        		luceneOptions.getStore(),
        		luceneOptions.getIndex(),
        		luceneOptions.getTermVector());
        canonicalNameField.setBoost(luceneOptions.getBoost());
        document.add(canonicalNameField);

	}
}
