/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.common.EnumeratedTermBase;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * This list should be extensible at runtime through configuration. This needs to
 * be investigated. http://rs.tdwg.org/ontology/voc/TaxonRank
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:46
 */
@Entity
public class Rank extends EnumeratedTermBase {
	static Logger logger = Logger.getLogger(Rank.class);
	private static String ontologyClassUri = "http://rs.tdwg.org/ontology/voc/TaxonRank#TaxonRankTerm";
	private ArrayList m_TaxonNameBase;

	public ArrayList getM_TaxonNameBase(){
		return this.m_TaxonNameBase;
	}

	/**
	 * 
	 * @param m_TaxonNameBase    m_TaxonNameBase
	 */
	public void setM_TaxonNameBase(ArrayList m_TaxonNameBase){
		this.m_TaxonNameBase = m_TaxonNameBase;
	}

	public String getOntologyClassUri(){
		return this.ontologyClassUri;
	}

	/**
	 * 
	 * @param ontologyClassUri    ontologyClassUri
	 */
	public void setOntologyClassUri(String ontologyClassUri){
		this.ontologyClassUri = ontologyClassUri;
	}

	public static final Rank EMPIRE(){
		return null;
	}

	public static final Rank DOMAIN(){
		return null;
	}

	public static final Rank SUPER_KINGDOM(){
		return null;
	}

	public static final Rank KINGDOM(){
		return null;
	}

	public static final Rank SUBKINGDOM(){
		return null;
	}

	public static final Rank INFRAKINGDOM(){
		return null;
	}

	public static final Rank SUPERPHYLUM(){
		return null;
	}

	public static final Rank PHYLUM(){
		return null;
	}

	public static final Rank SUBPHYLUM(){
		return null;
	}

	public static final Rank INFRAPHYLUM(){
		return null;
	}

	public static final Rank SUPERDIVISION(){
		return null;
	}

	public static final Rank DIVISION(){
		return null;
	}

	public static final Rank SUBDIVISION(){
		return null;
	}

	public static final Rank INFRADIVISION(){
		return null;
	}

	public static final Rank SUPERCLASS(){
		return null;
	}

	public static final Rank CLASS(){
		return null;
	}

	public static final Rank SUBCLASS(){
		return null;
	}

	public static final Rank INFRACLASS(){
		return null;
	}

	public static final Rank SUPERORDER(){
		return null;
	}

	public static final Rank ORDER(){
		return null;
	}

	public static final Rank SUBORDER(){
		return null;
	}

	public static final Rank INFRAORDER(){
		return null;
	}

	public static final Rank SUPERFAMILY(){
		return null;
	}

	public static final Rank FAMILY(){
		return null;
	}

	public static final Rank SUBFAMILY(){
		return null;
	}

	public static final Rank INFRAFAMILY(){
		return null;
	}

	public static final Rank SUPERTRIBE(){
		return null;
	}

	public static final Rank TRIBE(){
		return null;
	}

	public static final Rank SUBTRIBE(){
		return null;
	}

	public static final Rank INFRATRIBE(){
		return null;
	}

	public static final Rank SUPRAGENERIC_TAXON(){
		return null;
	}

	public static final Rank GENUS(){
		return null;
	}

	public static final Rank SUBGENUS(){
		return null;
	}

	public static final Rank INFRAGENUS(){
		return null;
	}

	public static final Rank SECTION(){
		return null;
	}

	public static final Rank SUBSECTION(){
		return null;
	}

	public static final Rank SERIES(){
		return null;
	}

	public static final Rank SUBSERIES(){
		return null;
	}

	public static final Rank SPECIES_AGGREGATE(){
		return null;
	}

	public static final Rank INFRAGENERIC_TAXON(){
		return null;
	}

	public static final Rank SPECIES(){
		return null;
	}

	public static final Rank SUBSPECIFIC_AGGREGATE(){
		return null;
	}

	public static final Rank SUBSPECIES(){
		return null;
	}

	public static final Rank INFRASPECIES(){
		return null;
	}

	public static final Rank VARIETY(){
		return null;
	}

	public static final Rank BIO_VARIETY(){
		return null;
	}

	public static final Rank PATHO_VARIETY(){
		return null;
	}

	public static final Rank SUBVARIETY(){
		return null;
	}

	public static final Rank SUBSUBVARIETY(){
		return null;
	}

	public static final Rank CONVAR(){
		return null;
	}

	public static final Rank FORM(){
		return null;
	}

	public static final Rank SPECIAL_FORM(){
		return null;
	}

	public static final Rank SUBFORM(){
		return null;
	}

	public static final Rank SUBSUBFORM(){
		return null;
	}

	public static final Rank INFRASPECIFIC_TAXON(){
		return null;
	}

	public static final Rank CANDIDATE(){
		return null;
	}

	public static final Rank DENOMINATION_CLASS(){
		return null;
	}

	public static final Rank GREX(){
		return null;
	}

	public static final Rank GRAFT_CHIMAERA(){
		return null;
	}

	public static final Rank CULTIVAR_GROUP(){
		return null;
	}

	public static final Rank CULTIVAR(){
		return null;
	}

}