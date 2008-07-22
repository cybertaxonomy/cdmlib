/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * The class representing categories of {@link SynonymRelationship synonym relationships}
 * (like "pro parte synonym of" or "heterotypic synonym of").
 * <P>
 * A standard (ordered) list of synonym relationship type instances will be
 * automatically created as the project starts. But this class allows to extend
 * this standard list by creating new instances of additional synonym
 * relationship types if needed. 
 * <P>
 * This class corresponds in part to: <ul>
 * <li> TaxonRelationshipTerm according to the TDWG ontology
 * <li> RelationshipType according to the TCS
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:55
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SynonymRelationshipType")
@Entity
public class SynonymRelationshipType extends RelationshipTermBase<SynonymRelationshipType> {
	
	static Logger logger = Logger.getLogger(SynonymRelationshipType.class);

	private static final UUID uuidSynonymOf = UUID.fromString("1afa5429-095a-48da-8877-836fa4fe709e");
	private static final UUID uuidProParteSynonymOf = UUID.fromString("130b752d-2eff-4a62-a132-104ed8d13e5e");
	private static final UUID uuidPartialSynonymOf = UUID.fromString("8b0d1d34-cc00-47cb-999d-b67f98d1af6e");
	private static final UUID uuidHomotypicSynonymOf = UUID.fromString("294313a9-5617-4ed5-ae2d-c57599907cb2");
	private static final UUID uuidHeterotypicSynonymOf = UUID.fromString("4c1e2c59-ca55-41ac-9a82-676894976084");

	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty synonym relationship type instance.
	 * 
	 * @see 	#SynonymRelationshipType(String, String, String, boolean, boolean)
	 */
	public SynonymRelationshipType() {
		super();
	}

	/** 
	 * Class constructor: creates an additional synonym relationship type
	 * instance with a description, a label, a label abbreviation and the flags
	 * indicating whether this new synonym relationship type is symmetric and/or
	 * transitive.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new synonym relationship type to be created 
	 * @param	label  		 the string identifying the new synonym relationship
	 * 						 type to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new synonym relationship type to be created
	 * @param	symmetric	 the boolean indicating whether the new synonym
	 * 						 relationship type to be created is symmetric
	 * @param	transitive	 the boolean indicating whether the new synonym
	 * 						 relationship type to be created is transitive
	 * @see 				 #SynonymRelationshipType()
	 */
	public SynonymRelationshipType(String term, String label, String labelAbbrev, boolean symmetric, boolean transitive) {
		super(term, label, labelAbbrev, symmetric, transitive);
	}


	public static final SynonymRelationshipType getByUuid(UUID uuid){
		return (SynonymRelationshipType) findByUuid(uuid);
	}
	
	public static final SynonymRelationshipType SYNONYM_OF(){
		return getByUuid(uuidSynonymOf);
	}

	public static final SynonymRelationshipType PRO_PARTE_SYNONYM_OF(){
		return getByUuid(uuidProParteSynonymOf);
	}

	public static final SynonymRelationshipType PARTIAL_SYNONYM_OF(){
		return getByUuid(uuidPartialSynonymOf);
	}

	/**
	 * Returns the synonym relationship type "is homotypic synonym of". This
	 * indicates that the {@link name.TaxonNameBase taxon name} used as a {@link Synonym synonym}
	 * and the taxon name used as the ("accepted/correct") {@link Taxon taxon} belong
	 * to the same {@link name.HomotypicalGroup homotypical group}.
	 */
	public static final SynonymRelationshipType HOMOTYPIC_SYNONYM_OF(){
		return getByUuid(uuidHomotypicSynonymOf);
	}

	/**
	 * Returns the synonym relationship type "is heterotypic synonym of". This
	 * indicates that the {@link name.TaxonNameBase taxon name} used as a {@link Synonym synonym}
	 * and the taxon name used as the ("accepted/correct") {@link Taxon taxon} do not
	 * belong to the same {@link name.HomotypicalGroup homotypical group}.
	 */
	public static final SynonymRelationshipType HETEROTYPIC_SYNONYM_OF(){
		return getByUuid(uuidHeterotypicSynonymOf);
	}

}