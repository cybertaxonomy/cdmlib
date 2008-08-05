/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import org.apache.log4j.Logger;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import java.util.UUID;


/**
 * The class representing categories of {@link TaxonRelationship taxon relationships}
 * (like "is congruent to" or "is misapplied name for").
 * <P>
 * A standard (ordered) list of taxon relationship type instances will be
 * automatically created as the project starts. But this class allows to extend
 * this standard list by creating new instances of additional taxon
 * relationship types if needed. 
 * <P>
 * This class corresponds in part to: <ul>
 * <li> TaxonRelationshipTerm according to the TDWG ontology
 * <li> RelationshipType according to the TCS
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:17
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonRelationshipType")
@XmlRootElement(name = "TaxonRelationshipType")
@Entity
public class TaxonRelationshipType extends RelationshipTermBase<TaxonRelationshipType> {
	
	static Logger logger = Logger.getLogger(TaxonRelationshipType.class);

	private static final UUID uuidTaxonomicallyIncludedIn = UUID.fromString("d13fecdf-eb44-4dd7-9244-26679c05df1c");
	private static final UUID uuidMisappliedNameFor = UUID.fromString("1ed87175-59dd-437e-959e-0d71583d8417");
	private static final UUID uuidInvalidDesignationFor = UUID.fromString("605b1d01-f2b1-4544-b2e0-6f08def3d6ed");
	private static final UUID uuidContradiction = UUID.fromString("a8f03491-2ad6-4fae-a04c-2a4c117a2e9b");
	private static final UUID uuidCongruentTo = UUID.fromString("60974c98-64ab-4574-bb5c-c110f6db634d");
	private static final UUID uuidIncludes = UUID.fromString("0501c385-cab1-4fbe-b945-fc747419bb13");
	private static final UUID uuidOverlaps = UUID.fromString("2046a0fd-4fd6-45a1-b707-2b91547f3ec7");
	private static final UUID uuidExcludes = UUID.fromString("4535a63c-4a3f-4d69-9350-7bf02e2c23be");
	private static final UUID uuidDoesNotExclude = UUID.fromString("0e5099bb-87c0-400e-abdc-bcfed5b5eece");
	private static final UUID uuidDoesNotOverlap = UUID.fromString("ecd2382b-3d94-4169-9dd2-2c4ea1d24605");
	private static final UUID uuidNotIncludedIn = UUID.fromString("89dffa4e-e004-4d42-b0d1-ae1827529e43");
	private static final UUID uuidNotCongruentTo = UUID.fromString("6c16c33b-cfc5-4a00-92bd-a9f9e448f389");
	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty taxon relationship type instance.
	 * 
	 * @see 	#TaxonRelationshipType(String, String, String, boolean, boolean)
	 */
	public TaxonRelationshipType() {
		super();
	}
	/** 
	 * Class constructor: creates an additional taxon relationship type
	 * instance with a description (in the {@link common.Language#DEFAULT() default language}), a label,
	 * a label abbreviation and the flags indicating whether this new taxon
	 * relationship type is symmetric and/or transitive.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new taxon relationship type to be created 
	 * @param	label  		 the string identifying the new taxon relationship
	 * 						 type to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new taxon relationship type to be created
	 * @param	symmetric	 the boolean indicating whether the new taxon
	 * 						 relationship type to be created is symmetric
	 * @param	transitive	 the boolean indicating whether the new taxon
	 * 						 relationship type to be created is transitive
	 * @see 				 #TaxonRelationshipType()
	 */
	public TaxonRelationshipType(String term, String label, String labelAbbrev, boolean symmetric, boolean transitive) {
		super(term, label, labelAbbrev, symmetric, transitive);
	}

	//********* METHODS **************************************/

	/**
	 * Returns the taxon relationship type identified through its immutable
	 * universally unique identifier (UUID).
	 * 
	 * @param	uuid	the universally unique identifier
	 * @return  		the taxon relationship type corresponding to the given
	 * 					universally unique identifier
	 */
	public static final TaxonRelationshipType getByUuid(UUID uuid){
		return (TaxonRelationshipType) findByUuid(uuid);
	}


	/**
	 * Returns the taxon relationship type "is taxonomically included in". This
	 * indicates that the {@link TaxonRelationship#getFromTaxon() source taxon}
	 * in such a {@link TaxonRelationship taxon relationship} has the target {@link Taxon taxon}
	 * as immediate next higher parent within the taxonomic tree. Generally
	 * the {@link Taxon#getSec() concept reference} of both taxa are the same
	 * except if the concept reference follows the taxonomical opinion of
	 * another reference.<BR>
	 * This type is neither symmetric nor transitive.
	 */
	public static final TaxonRelationshipType TAXONOMICALLY_INCLUDED_IN(){
		return getByUuid(uuidTaxonomicallyIncludedIn);
	}
	/**
	 * Returns the taxon relationship type "is misapplied name for". This
	 * indicates that the {@link name.TaxonNameBase taxon name} of the {@link TaxonRelationship#getFromTaxon() source taxon}
	 * in such a {@link TaxonRelationship taxon relationship} has been erroneously used by
	 * the {@link TaxonBase#getSec() concept reference} to denominate the same real taxon
	 * as the one meant by the target {@link Taxon taxon}.<BR>
	 * This type is neither symmetric nor transitive.
	 */
	public static final TaxonRelationshipType MISAPPLIED_NAME_FOR(){
		return (TaxonRelationshipType)findByUuid(uuidMisappliedNameFor);
	}
	/**
	 * Returns the taxon relationship type "is invalid designation for". This
	 * indicates that the {@link name.TaxonNameBase taxon name} of the {@link TaxonRelationship#getFromTaxon() source taxon}
	 * in such a {@link TaxonRelationship taxon relationship} has not been
	 * {@link name.NomenclaturalStatusType#isInvalidType() validly published} but was intended to denominate
	 * a real taxon which is the same as the one meant by the target {@link Taxon taxon}.<BR>
	 * According to the nomenclature codes a not validly published taxon name is 
	 * not a taxon name at all.<BR>
	 * This type is neither symmetric nor transitive.
	 */
	public static final TaxonRelationshipType INVALID_DESIGNATION_FOR(){
		return (TaxonRelationshipType)findByUuid(uuidInvalidDesignationFor);
	}
	/**
	 * Returns the (concept) taxon relationship type "is impossible"
	 * (contradiction). This is a concept relationship type which means that the
	 * circumscriptions (the set of organisms/specimens that belong to a {@link Taxon taxon}
	 * according to the -maybe implicit- opinion of the {@link TaxonBase#getSec() concept reference})
	 * of both taxa involved in such a {@link TaxonRelationship taxon relationship} are
	 * beeing compared. In a concept relationship the concept references of the
	 * involved taxa must be distinct. <BR>
	 * The "is impossible" (contradiction) taxon relationship type arises
	 * logically if two or more incompatible (concept) taxon relationships
	 * between the two taxa already exist (for instance with the types
	 * "is congruent to" and "excludes").<BR>
	 * This type is symmetric but not transitive.
	 */
	public static final TaxonRelationshipType CONTRADICTION(){
		return (TaxonRelationshipType)findByUuid(uuidContradiction);
	}
	/**
	 * Returns the (concept) taxon relationship type "is congruent to".
	 * This is a concept relationship type which means that the circumscriptions
	 * (the set of organisms/specimens that belong to a {@link Taxon taxon} according
	 * to the -maybe implicit- opinion of the {@link TaxonBase#getSec() concept reference})
	 * of both taxa involved in such a {@link TaxonRelationship taxon relationship} are
	 * beeing compared. In a concept relationship the concept references of the
	 * involved taxa must be distinct. <BR>
	 * The "is congruent to" taxon relationship type indicates that the
	 * circumscriptions of both taxa involved in the taxon relationship are
	 * identical.<BR>
	 * This type is symmetric and transitive.
	 */
	public static final TaxonRelationshipType CONGRUENT_TO(){
		return (TaxonRelationshipType)findByUuid(uuidCongruentTo);
	}
	/**
	 * Returns the (concept) taxon relationship type "includes".
	 * This is a concept relationship type which means that the circumscriptions
	 * (the set of organisms/specimens that belong to a {@link Taxon taxon} according
	 * to the -maybe implicit- opinion of the {@link TaxonBase#getSec() concept reference})
	 * of both taxa involved in such a {@link TaxonRelationship taxon relationship} are
	 * beeing compared. In a concept relationship the concept references of the
	 * involved taxa must be distinct. <BR>
	 * The "includes" taxon relationship type indicates that each element
	 * belonging to the circumscription of the target taxon involved in the
	 * taxon relationship belongs to the circumscription of the source taxon,
	 * but that some elements belonging to the circumscription of the source
	 * taxon do not belong to the circumscription of the target taxon.<BR>
	 * This type is not symmetric but transitive.
	 */
	public static final TaxonRelationshipType INCLUDES(){
		return (TaxonRelationshipType)findByUuid(uuidIncludes);
	}
	/**
	 * Returns the (concept) taxon relationship type "overlaps".
	 * This is a concept relationship type which means that the circumscriptions
	 * (the set of organisms/specimens that belong to a {@link Taxon taxon} according
	 * to the -maybe implicit- opinion of the {@link TaxonBase#getSec() concept reference})
	 * of both taxa involved in such a {@link TaxonRelationship taxon relationship} are
	 * beeing compared. In a concept relationship the concept references of the
	 * involved taxa must be distinct. <BR>
	 * The "overlaps" taxon relationship type indicates that both
	 * circumscriptions have common elements but that some elements belonging
	 * to the circumscription of the source taxon do not belong to the
	 * circumscription of the target taxon and vice-versa.<BR>
	 * This type is symmetric but not transitive.
	 */
	public static final TaxonRelationshipType OVERLAPS(){
		return (TaxonRelationshipType)findByUuid(uuidOverlaps);
	}
	/**
	 * Returns the (concept) taxon relationship type "excludes".
	 * This is a concept relationship type which means that the circumscriptions
	 * (the set of organisms/specimens that belong to a {@link Taxon taxon} according
	 * to the -maybe implicit- opinion of the {@link TaxonBase#getSec() concept reference})
	 * of both taxa involved in such a {@link TaxonRelationship taxon relationship} are
	 * beeing compared. In a concept relationship the concept references of the
	 * involved taxa must be distinct. <BR>
	 * The "excludes" taxon relationship type indicates that both
	 * circumscriptions have no common elements.<BR>
	 * This type is symmetric but not transitive.
	 */
	public static final TaxonRelationshipType EXCLUDES(){
		return (TaxonRelationshipType)findByUuid(uuidExcludes);
	}
	/**
	 * Returns the (concept) taxon relationship type "does not exclude".
	 * This is a concept relationship type which means that the circumscriptions
	 * (the set of organisms/specimens that belong to a {@link Taxon taxon} according
	 * to the -maybe implicit- opinion of the {@link TaxonBase#getSec() concept reference})
	 * of both taxa involved in such a {@link TaxonRelationship taxon relationship} are
	 * beeing compared. In a concept relationship the concept references of the
	 * involved taxa must be distinct. <BR>
	 * The "does not exclude" taxon relationship type indicates that both
	 * circumscriptions have common elements. This type is a generalisation of
	 * "is congruent to", "includes" and "overlaps".<BR>
	 * This type is symmetric but not transitive.
	 */
	public static final TaxonRelationshipType DOES_NOT_EXCLUDE(){
		return (TaxonRelationshipType)findByUuid(uuidDoesNotExclude);
	}
	/**
	 * Returns the (concept) taxon relationship type "does not overlap".
	 * This is a concept relationship type which means that the circumscriptions
	 * (the set of organisms/specimens that belong to a {@link Taxon taxon} according
	 * to the -maybe implicit- opinion of the {@link TaxonBase#getSec() concept reference})
	 * of both taxa involved in such a {@link TaxonRelationship taxon relationship} are
	 * beeing compared. In a concept relationship the concept references of the
	 * involved taxa must be distinct. <BR>
	 * The "does not overlap" taxon relationship type is a generalisation of
	 * "is congruent to", "includes" and "excludes".<BR>
	 * This type is symmetric but not transitive.
	 */
	public static final TaxonRelationshipType DOES_NOT_OVERLAP(){
		return (TaxonRelationshipType)findByUuid(uuidDoesNotOverlap);
	}
	/**
	 * Returns the (concept) taxon relationship type "is not included in".
	 * This is a concept relationship type which means that the circumscriptions
	 * (the set of organisms/specimens that belong to a {@link Taxon taxon} according
	 * to the -maybe implicit- opinion of the {@link TaxonBase#getSec() concept reference})
	 * of both taxa involved in such a {@link TaxonRelationship taxon relationship} are
	 * beeing compared. In a concept relationship the concept references of the
	 * involved taxa must be distinct. <BR>
	 * The "is not included in" taxon relationship type indicates that at least
	 * one element belonging to the circumscription of the source taxon involved
	 * in the taxon relationship does not belong to the circumscription of the
	 * target taxon. <BR>
	 * This type is neither symmetric nor transitive.
	 */
	public static final TaxonRelationshipType NOT_INCLUDED_IN(){
		return (TaxonRelationshipType)findByUuid(uuidNotIncludedIn);
	}
	/**
	 * Returns the (concept) taxon relationship type "is not congruent to".
	 * This is a concept relationship type which means that the circumscriptions
	 * (the set of organisms/specimens that belong to a {@link Taxon taxon} according
	 * to the -maybe implicit- opinion of the {@link TaxonBase#getSec() concept reference})
	 * of both taxa involved in such a {@link TaxonRelationship taxon relationship} are
	 * beeing compared. In a concept relationship the concept references of the
	 * involved taxa must be distinct. <BR>
	 * The "is not congruent to" taxon relationship type indicates that at least
	 * one element belonging to one of both circumscriptions does not belong to
	 * the other circumscription. This type is a generalisation of
	 * "includes", "overlaps" and "excludes".<BR>
	 * This type is symmetric but not transitive.
	 */
	public static final TaxonRelationshipType NOT_CONGRUENT_TO(){
		return (TaxonRelationshipType)findByUuid(uuidNotCongruentTo);
	}

	//TODO ohter relationshipTypes

}