/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import javax.persistence.Entity;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:55
 * http://rs.tdwg.org/ontology/voc/TaxonConcept#TaxonRelationshipTerm
 */
@Entity
public class SynonymRelationshipType extends RelationshipTermBase {
	static Logger logger = Logger.getLogger(SynonymRelationshipType.class);

	public SynonymRelationshipType() {
		super();
	}

	public SynonymRelationshipType(String term, String label, boolean symmetric, boolean transitive) {
		super(term, label, symmetric, transitive);
		// TODO Auto-generated constructor stub
	}

	public static final SynonymRelationshipType SYNONYM_OF(){
		return null;
	}

	public static final SynonymRelationshipType PRO_PARTE_SYNONYM_OF(){
		return null;
	}

	public static final SynonymRelationshipType PARTIAL_SYNONYM_OF(){
		return null;
	}

	public static final SynonymRelationshipType HOMOTYPIC_SYNONYM_OF(){
		return null;
	}

	public static final SynonymRelationshipType HETEROTYPIC_SYNONYM_OF(){
		return null;
	}

}