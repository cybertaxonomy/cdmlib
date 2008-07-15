/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.location.Continent;

import org.apache.log4j.Logger;
import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * The class representing the categories of taxon name relationships between
 * two {@link TaxonNameBase taxon names}. These name relationship types are
 * based on the concrete {@link NomenclaturalCode nomenclatural code} governing
 * the taxon names involved in the name relationship or on decisions taken by
 * the corresponding authorities; they do not depend on the use made of these
 * taxon names in a particular reference or on a particular taxonomic treatment.
 * Most relationships are to be understood as 'is .... of': for instance 
 * Linum radiola L. is a replaced synonym of Radiola linoides Roth or
 * Astragalus rhizanthus Boiss. is a later homonym of
 * Astragalus rhizanthus Royle.  
 * <P>
 * A standard (ordered) list of name relationship type instances will be
 * automatically created as the project starts. But this class allows to extend
 * this standard list by creating new instances of additional name relationship
 * types if needed. 
 * <P>
 * This class corresponds partially to: <ul>
 * <li> TaxonRelationshipTerm and NomenclaturalNoteTypeTerm according to the TDWG ontology
 * <li> RelationshipType and NomenclaturalNoteType according to the TCS
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:38
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@Entity
public class NameRelationshipType extends RelationshipTermBase<NameRelationshipType> {
	static Logger logger = Logger.getLogger(NameRelationshipType.class);

	private static final UUID uuidOrthographicVariant = UUID.fromString("eeaea868-c4c1-497f-b9fe-52c9fc4aca53");
	private static final UUID uuidLaterHomonym = UUID.fromString("80f06f65-58e0-4209-b811-cb40ad7220a6");
	private static final UUID uuidTreatedAsLaterHomonym = UUID.fromString("2990a884-3302-4c8b-90b2-dfd31aaa2778");
	private static final UUID uuidAlternativeName = UUID.fromString("049c6358-1094-4765-9fae-c9972a0e7780");
	private static final UUID uuidBasionym = UUID.fromString("25792738-98de-4762-bac1-8c156faded4a");
	private static final UUID uuidReplacedSynonym = UUID.fromString("71c67c38-d162-445b-b0c2-7aba56106696");
	private static final UUID uuidConservedAgainst = UUID.fromString("e6439f95-bcac-4ebb-a8b5-69fa5ce79e6a");
	private static final UUID uuidValidatedByName = UUID.fromString("a176c9ad-b4c2-4c57-addd-90373f8270eb");
	private static final UUID uuidLaterValidatedByName = UUID.fromString("a25ee4c1-863a-4dab-9499-290bf9b89639");
	private static final UUID uuidBlockingNameFor = UUID.fromString("1dab357f-2e12-4511-97a4-e5153589e6a6");
	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty name relationship type instance.
	 * 
	 * @see 	#NameRelationshipType(String, String, String, boolean, boolean)
	 */
	public NameRelationshipType() {
		super();
	}
	
	/** 
	 * Class constructor: creates an additional name relationship type
	 * instance with a description, a label, a label abbreviation and the flags
	 * indicating whether this new name relationship type is symmetric and/or
	 * transitive.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new name relationship type to be created 
	 * @param	label  		 the string identifying the new name relationship
	 * 						 type to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new name relationship type to be created
	 * @param	symmetric	 the boolean indicating whether the new name
	 * 						 relationship type to be created is symmetric
	 * @param	transitive	 the boolean indicating whether the new name
	 * 						 relationship type to be created is transitive
	 * @see 				 #NameRelationshipType()
	 */
	public NameRelationshipType(String term, String label, String labelAbbrev, boolean symmetric, boolean transitive) {
		super(term, label, labelAbbrev, symmetric, transitive);
	}


	//********* METHODS **************************************/

	/**
	 * Returns the name relationship type identified through its immutable universally
	 * unique identifier (UUID).
	 * 
	 * @param	uuid	the universally unique identifier
	 * @return  		the name relationship type corresponding to the given
	 * 					universally unique identifier
	 */
	public static final NameRelationshipType getByUuid(UUID uuid){
		return (NameRelationshipType)findByUuid(uuid);
	}

	/**
	 * Returns the boolean value indicating whether the nomenclatural status
	 * type of the {@link common.RelationshipBase#getRelatedFrom() first taxon name}
	 * involved in a name relationship with this name relationship type should
	 * be "invalid" (true) or not (false). Returns false if this name
	 * relationship status type is null.
	 *
	 * @see  #isLegitimateType()
	 * @see  #isIllegitimateType()
	 * @see  NomenclaturalStatusType#isInvalidType()
	 * @see  common.RelationshipBase#getRelatedFrom()
	 */
	@Transient
	public boolean isInvalidType(){
		if (this.equals(VALIDATED_BY_NAME()) || 
				this.equals(LATER_VALIDATED_BY_NAME())
			){
			return true;	
		}else{
			return false;
		}
	}

	/**
	 * Returns the boolean value indicating whether the nomenclatural status
	 * type of the {@link common.RelationshipBase#getRelatedFrom() first taxon name}
	 * involved in a name relationship with this name relationship type should
	 * be "legitimate" (true) or not (false). Returns false if this name
	 * relationship status type is null.
	 *
	 * @see  #isInvalidType()
	 * @see  #isIllegitimateType()
	 * @see  NomenclaturalStatusType#isLegitimateType()
	 * @see  common.RelationshipBase#getRelatedFrom()
	 */
	@Transient
	public boolean isLegitimateType(){
		if (this.equals(BASIONYM()) || 
				this.equals(REPLACED_SYNONYM()) || 
				this.equals(ALTERNATIVE_NAME()) ||
				this.equals(CONSERVED_AGAINST())
			){
			return true;	
		}else{
			return false;
		}
	}

	/**
	 * Returns the boolean value indicating whether the nomenclatural status
	 * type of the {@link common.RelationshipBase#getRelatedFrom() first taxon name}
	 * involved in a name relationship with this name relationship type should
	 * be "illegitimate" (true) or not (false). Returns false if this name
	 * relationship status type is null.
	 *
	 * @see  #isInvalidType()
	 * @see  #isLegitimateType()
	 * @see  NomenclaturalStatusType#isIllegitimateType()
	 * @see  common.RelationshipBase#getRelatedFrom()
	 */
	@Transient
	public boolean isIllegitimateType(){
		//TODO: implement isX method. Maybe as persistent class attribute?
		//TODO: RejectedInFavour,
		if (this.equals(LATER_HOMONYM()) || 
				this.equals(TREATED_AS_LATER_HOMONYM())
			){
			return true;	
		}else{
			return false;
		}
	}

	
	public static final NameRelationshipType ORTHOGRAPHIC_VARIANT(){
		  return getByUuid(uuidOrthographicVariant);
	}
	public static final NameRelationshipType LATER_HOMONYM(){
	  return getByUuid(uuidLaterHomonym);
	}
	public static final NameRelationshipType TREATED_AS_LATER_HOMONYM(){
	  return getByUuid(uuidTreatedAsLaterHomonym);
	}
	public static final NameRelationshipType ALTERNATIVE_NAME(){
	  return getByUuid(uuidAlternativeName);
	}
	public static final NameRelationshipType BASIONYM(){
	  return getByUuid(uuidBasionym);
	}
	public static final NameRelationshipType REPLACED_SYNONYM(){
	  return getByUuid(uuidReplacedSynonym);
	}
	public static final NameRelationshipType CONSERVED_AGAINST(){
	  return getByUuid(uuidConservedAgainst);
	}
	public static final NameRelationshipType VALIDATED_BY_NAME(){
	  return getByUuid(uuidValidatedByName);
	}
	public static final NameRelationshipType LATER_VALIDATED_BY_NAME(){
	  return getByUuid(uuidLaterValidatedByName);
	}
	public static final NameRelationshipType BLOCKING_NAME_FOR(){
	  return getByUuid(uuidBlockingNameFor);
	}

}