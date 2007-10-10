/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import org.apache.log4j.Logger;

/**
 * The list should be extensible at runtime through configuration. This needs to
 * be investigated.
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:11
 */
public enum Rank {
	EMPIRE,
	DOMAIN,
	SUPER_KINGDOM,
	KINGDOM,
	SUBKINGDOM,
	INFRAKINGDOM,
	SUPERPHYLUM,
	PHYLUM,
	SUBPHYLUM,
	INFRAPHYLUM,
	SUPERDIVISION,
	DIVISION,
	SUBDIVISION,
	INFRADIVISION,
	SUPERCLASS,
	CLASS,
	SUBCLASS,
	INFRACLASS,
	SUPERORDER,
	ORDER,
	SUBORDER,
	INFRAORDER,
	SUPERFAMILY,
	FAMILY,
	SUBFAMILY,
	INFRAFAMILY,
	SUPERTRIBE,
	TRIBE,
	SUBTRIBE,
	INFRATRIBE,
	SUPRAGENERIC_TAXON,
	GENUS,
	SUBGENUS,
	INFRAGENUS,
	SECTION,
	SUBSECTION,
	SERIES,
	SUBSERIES,
	SPECIES_AGGREGATE,
	INFRAGENERIC_TAXON,
	SPECIES,
	SUBSPECIFIC_AGGREGATE,
	SUBSPECIES,
	INFRASPECIES,
	VARIETY,
	BIO_VARIETY,
	PATHO_VARIETY,
	SUB_VARIETY,
	SUB_SUB_VARIETY,
	FORM,
	SPECIAL_FORM,
	SUBFORM,
	SUBSUBFORM,
	INFRASPECIFIC_TAXON,
	CANDIDATE,
	DENOMINATION_CLASS,
	GRAFT_CHIMAERA,
	CULTIVAR_GROUP,
	GREX,
	CONVAR,
	CULTIVAR;

//	private ArrayList m_TaxonName;
//
//	public ArrayList getM_TaxonName(){
//		return m_TaxonName;
//	}
//
//	/**
//	 * 
//	 * @param newVal
//	 */
//	public void setM_TaxonName(ArrayList newVal){
//		m_TaxonName = newVal;
//	}
}