/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import org.apache.log4j.Logger;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * The class representing the categories of parent relationships between a
 * {@link BotanicalName botanical taxon name} used as a parent of a hybrid taxon
 * name and the hybrid taxon name itself. Hybrids and their parents are always
 * plants.  
 * <P>
 * A standard (ordered) list of hybrid relationship type instances will be
 * automatically created as the project starts. But this class allows to extend
 * this standard list by creating new instances of additional hybrid
 * relationship types if needed. 
 * <P>
 * This class corresponds partially to: <ul>
 * <li> TaxonRelationshipTerm according to the TDWG ontology
 * <li> RelationshipType according to the TCS
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:27
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@Entity
public class HybridRelationshipType extends RelationshipTermBase<HybridRelationshipType> {
  
	static Logger logger = Logger.getLogger(HybridRelationshipType.class);

	private static final UUID uuidFirstParent = UUID.fromString("83ae9e56-18f2-46b6-b211-45cdee775bf3");
	private static final UUID uuidSecondParent = UUID.fromString("0485fc3d-4755-4f53-8832-b82774484c43");
	private static final UUID uuidFemaleParent = UUID.fromString("189a3ed9-6860-4943-8be8-a1f60133be2a");
	private static final UUID uuidMaleParent = UUID.fromString("8b7324c5-cc6c-4109-b708-d49b187815c4");

	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty hybrid relationship type instance.
	 * 
	 * @see 	#HybridRelationshipType(String, String, String, boolean, boolean)
	 */
	public HybridRelationshipType() {
		super();
	}
	/** 
	 * Class constructor: creates an additional hybrid relationship type
	 * instance with a description, a label, a label abbreviation and the flags
	 * indicating whether this new hybrid relationship type is symmetric and/or
	 * transitive.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new hybrid relationship type to be created 
	 * @param	label  		 the string identifying the new hybrid relationship
	 * 						 type to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new hybrid relationship type to be created
	 * @param	symmetric	 the boolean indicating whether the new hybrid
	 * 						 relationship type to be created is symmetric
	 * @param	transitive	 the boolean indicating whether the new hybrid
	 * 						 relationship type to be created is transitive
	 * @see 				 #HybridRelationshipType()
	 */
	public HybridRelationshipType(String term, String label, String labelAbbrev, boolean symmetric, boolean transitive) {
		super(term, label, labelAbbrev, symmetric, transitive);
	}


	//********* METHODS **************************************/

	/**
	 * Returns the hybrid relationship type identified through its immutable universally
	 * unique identifier (UUID).
	 * 
	 * @param	uuid	the universally unique identifier
	 * @return  		the hybrid relationship type corresponding to the given
	 * 					universally unique identifier
	 */
	public static final HybridRelationshipType getbyUuid(UUID uuid){
		return (HybridRelationshipType) findByUuid(uuid);
	}

	public static final HybridRelationshipType FIRST_PARENT(){
		return getbyUuid(uuidFirstParent);
	}

	public static final HybridRelationshipType SECOND_PARENT(){
		return getbyUuid(uuidSecondParent);
	}

	public static final HybridRelationshipType FEMALE_PARENT(){
		return getbyUuid(uuidFemaleParent);
	}

	public static final HybridRelationshipType MALE_PARENT(){
		return getbyUuid(uuidMaleParent);
	}

}