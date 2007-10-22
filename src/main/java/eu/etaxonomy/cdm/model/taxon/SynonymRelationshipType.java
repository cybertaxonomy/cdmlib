/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import org.apache.log4j.Logger;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:13
 */
public enum SynonymRelationshipType {
	SYNONYM_OF,
	PRO_PARTE_SYNONYM_OF,
	PARTIAL_SYNONYM_OF,
	HOMOTYPIC_SYNONYM_OF,
	HETEROTYPIC_SYNONYM_OF,
	PRO_PARTE_AND_HOMOTYPIC_SYNONYM_OF,
	PRO_PARTE_AND_HETEROTYPIC_SYNONYM_OF,
	PARTIAL_AND_HOMOTYPIC_SYNONYM_OF,
	PARTIAL_AND_HETEROTYPIC_SYNONYM_OF
}