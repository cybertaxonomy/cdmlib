/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.common.EnumeratedTermBase;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:42
 */
public class SynonymRelationshipType extends EnumeratedTermBase {
	static Logger logger = Logger.getLogger(SynonymRelationshipType.class);

	@Description("")
	private static final int initializationClassUri = http://rs.tdwg.org/ontology/voc/TaxonConcept#TaxonRelationshipTerm;

	public getInitializationClassUri(){
		return initializationClassUri;
	}

	/**
	 * 
	 * @param initializationClassUri
	 */
	public void setInitializationClassUri(initializationClassUri){
		;
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