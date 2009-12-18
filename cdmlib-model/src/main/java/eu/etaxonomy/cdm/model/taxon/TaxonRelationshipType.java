/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import java.util.List;
import java.util.Map;
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
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class TaxonRelationshipType extends RelationshipTermBase<TaxonRelationshipType> {
	
	static Logger logger = Logger.getLogger(TaxonRelationshipType.class);

	@Deprecated //will be removed in future versions. Use Taxonomic Tree/TaxonNode instead
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
	
	
	private static final UUID uuidCongruentToOrIncludes = UUID.fromString("b55cb3a2-6e20-4ca3-95bc-12b59d3235b0");
	private static final UUID uuidIncludedInOrIncludes = UUID.fromString("c3ed5089-6779-4051-bb24-f5ea0eca80d5");
	private static final UUID uuidCongruentIncludedInOrIncludes = UUID.fromString("0170cd83-93ad-43c2-9ad1-7ac879300e2f");
	private static final UUID uuidCongruentToOrOverlaps = UUID.fromString("78355cfa-5200-432f-8e00-82b97afad0ed");
	private static final UUID uuidIncludesOrOverlaps = UUID.fromString("f1ec567b-3c73-436b-8625-b4fd53588abb");
	
	private static final UUID uuidCongruentToOrIncludesOrOverlaps = UUID.fromString("2d923b1a-6c0f-414c-ac9b-bbc502e18078");
	private static final UUID uuidIncludedInOrIncludesOrOverlaps = UUID.fromString("43466aa9-e431-4f37-8bca-febfd9f63716");
	private static final UUID uuidCongruentToOrExcludes = UUID.fromString("758e6cf3-05a0-49ed-9496-d8c4a9fd02ae");
	private static final UUID uuidIncludesOrExcludes = UUID.fromString("6ee440bc-fd3d-4da2-ad85-906d35a94731");
	private static final UUID uuidCongruentToOrIncludesOrExcludes = UUID.fromString("d5c6953d-aa53-46f8-aafc-ebc6428ad5d0");
	private static final UUID uuidIncludedInOrIncludesOrExcludes = UUID.fromString("43d8492c-8bd5-4f38-a633-f1ad910a34dd");
	private static final UUID uuidOverlapsOrExcludes = UUID.fromString("623ecdeb-ff1f-471d-a8dc-0d75b2fe8d94");
	private static final UUID uuidCongruentToOrOverlapsOrExcludes = UUID.fromString("6fabef72-5264-44f1-bfc0-8e2e141375f2");
	private static final UUID uuidIncludesOrOverlapsOrExcludes = UUID.fromString("b7153c89-cc6c-4f8c-bf74-216f10feac46");
	
	@Deprecated //will be removed in future versions. Use Taxonomic Tree/TaxonNode instead
	private static TaxonRelationshipType TAXONOMICALLY_INCLUDED_IN;

	private static TaxonRelationshipType MISAPPLIED_NAME_FOR;
	private static TaxonRelationshipType INVALID_DESIGNATION_FOR;
	private static TaxonRelationshipType CONTRADICTION;
	private static TaxonRelationshipType CONGRUENT_TO;
	private static TaxonRelationshipType INCLUDED_OR_INCLUDES_OR_EXCLUDES;
	private static TaxonRelationshipType OVERLAPS_OR_EXCLUDES;
	private static TaxonRelationshipType CONGRUENT_OR_OVERLAPS_OR_EXCLUDES;
	private static TaxonRelationshipType INCLUDES_OR_OVERLAPS_OR_EXCLUDES;
	private static TaxonRelationshipType CONGRUENT_OR_INCLUDES_OR_OVERLAPS;
	private static TaxonRelationshipType INCLUDED_OR_INCLUDES_OR_OVERLAPS;
	private static TaxonRelationshipType CONGRUENT_OR_EXCLUDE;
	private static TaxonRelationshipType INCLUDES_OR_EXCLUDES;
	private static TaxonRelationshipType CONGRUENT_OR_INCLUDES_OR_EXCLUDES;
	private static TaxonRelationshipType INCLUDES_OR_OVERLAPS;
	private static TaxonRelationshipType CONGRUENT_OR_OVERLAPS;
	private static TaxonRelationshipType CONGRUENT_OR_INCLUDED_OR_INCLUDES;
	private static TaxonRelationshipType INCLUDED_OR_INCLUDES;
	private static TaxonRelationshipType CONGRUENT_OR_INCLUDES;
	private static TaxonRelationshipType NOT_CONGRUENT_TO;
	private static TaxonRelationshipType NOT_INCLUDED_IN;
	private static TaxonRelationshipType DOES_NOT_OVERLAP;
	private static TaxonRelationshipType DOES_NOT_EXCLUDE;
	private static TaxonRelationshipType EXCLUDES;
	private static TaxonRelationshipType OVERLAPS;
	private static TaxonRelationshipType INCLUDES;
	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty taxon relationship type instance.
	 * 
	 * @see 	#TaxonRelationshipType(String, String, String, boolean, boolean)
	 */
	public TaxonRelationshipType() {
	}
	/** 
	 * Class constructor: creates an additional taxon relationship type
	 * instance with a description (in the {@link eu.etaxonomy.cdm.model.common.Language#DEFAULT() default language}), a label,
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
		return TAXONOMICALLY_INCLUDED_IN;
	}
	/**
	 * Returns the taxon relationship type "is misapplied name for". This
	 * indicates that the {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name} of the {@link TaxonRelationship#getFromTaxon() source taxon}
	 * in such a {@link TaxonRelationship taxon relationship} has been erroneously used by
	 * the {@link TaxonBase#getSec() concept reference} to denominate the same real taxon
	 * as the one meant by the target {@link Taxon taxon}.<BR>
	 * This type is neither symmetric nor transitive.
	 */
	public static final TaxonRelationshipType MISAPPLIED_NAME_FOR(){
		return MISAPPLIED_NAME_FOR;
	}
	/**
	 * Returns the taxon relationship type "is invalid designation for". This
	 * indicates that the {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name} of the {@link TaxonRelationship#getFromTaxon() source taxon}
	 * in such a {@link TaxonRelationship taxon relationship} has
	 * {@link eu.etaxonomy.cdm.model.name.NomenclaturalStatusType#isInvalidType() not been validly published} but was intended to denominate
	 * a real taxon which is the same as the one meant by the target {@link Taxon taxon}.<BR>
	 * According to the nomenclature codes a not validly published taxon name is 
	 * not a taxon name at all.<BR>
	 * This type is neither symmetric nor transitive.
	 */
	public static final TaxonRelationshipType INVALID_DESIGNATION_FOR(){
		return INVALID_DESIGNATION_FOR;
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
		return CONTRADICTION;
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
		return CONGRUENT_TO;
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
		return INCLUDES;
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
		return OVERLAPS;
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
		return EXCLUDES;
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
	 * {@link #CONGRUENT_TO() "is congruent to"}, {@link #INCLUDES() "includes"} and {@link #OVERLAPS() "overlaps"}.<BR>
	 * This type is symmetric but not transitive.
	 * 
	 * @see		#EXCLUDES()
	 */
	public static final TaxonRelationshipType DOES_NOT_EXCLUDE(){
		return DOES_NOT_EXCLUDE;
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
	 * {@link #CONGRUENT_TO() "is congruent to"}, {@link #INCLUDES() "includes"} and {@link #EXCLUDES() "excludes"}.<BR>
	 * This type is symmetric but not transitive.
	 * 
	 * @see		#OVERLAPS()
	 */
	public static final TaxonRelationshipType DOES_NOT_OVERLAP(){
		return DOES_NOT_OVERLAP;
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
	 * 
	 * @see		#INCLUDES()
	 */
	public static final TaxonRelationshipType NOT_INCLUDED_IN(){
		return NOT_INCLUDED_IN;
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
	 * {@link #INCLUDES() "includes"}, {@link #OVERLAPS() "overlaps"} and {@link #EXCLUDES() "excludes"}.<BR>
	 * This type is symmetric but not transitive.
	 * 
	 * @see		#CONGRUENT_TO()
	 */
	public static final TaxonRelationshipType NOT_CONGRUENT_TO(){
		return NOT_CONGRUENT_TO;
	}

	public static final TaxonRelationshipType CONGRUENT_OR_INCLUDES(){
		return CONGRUENT_OR_INCLUDES;
	}
	public static final TaxonRelationshipType INCLUDED_OR_INCLUDES(){
		return INCLUDED_OR_INCLUDES;
	}
	public static final TaxonRelationshipType CONGRUENT_OR_INCLUDED_OR_INCLUDES(){
		return CONGRUENT_OR_INCLUDED_OR_INCLUDES;
	}
	public static final TaxonRelationshipType CONGRUENT_OR_OVERLAPS(){
		return CONGRUENT_OR_OVERLAPS;
	}
	
	public static final TaxonRelationshipType INCLUDES_OR_OVERLAPS(){
		return INCLUDES_OR_OVERLAPS;
	}

	public static final TaxonRelationshipType CONGRUENT_OR_INCLUDES_OR_EXCLUDES(){
		return CONGRUENT_OR_INCLUDES_OR_EXCLUDES;
	}
	
	public static final TaxonRelationshipType INCLUDES_OR_EXCLUDES(){
		return INCLUDES_OR_EXCLUDES;
	}
	
	public static final TaxonRelationshipType CONGRUENT_OR_EXCLUDES(){
		return CONGRUENT_OR_EXCLUDE;
	}
	
	public static final TaxonRelationshipType INCLUDED_OR_INCLUDES_OR_OVERLAPS(){
		return INCLUDED_OR_INCLUDES_OR_OVERLAPS;
	}
	
	public static final TaxonRelationshipType CONGRUENT_OR_INCLUDES_OR_OVERLAPS(){
		return CONGRUENT_OR_INCLUDES_OR_OVERLAPS;
	}
	
	public static final TaxonRelationshipType INCLUDES_OR_OVERLAPS_OR_EXCLUDES(){
		return  INCLUDES_OR_OVERLAPS_OR_EXCLUDES;
	}
	
	public static final TaxonRelationshipType CONGRUENT_OR_OVERLAPS_OR_EXCLUDES(){
		return CONGRUENT_OR_OVERLAPS_OR_EXCLUDES;
	}
	
	public static final TaxonRelationshipType OVERLAPS_OR_EXCLUDES(){
		return OVERLAPS_OR_EXCLUDES;
	}
	
	public static final TaxonRelationshipType INCLUDED_OR_INCLUDES_OR_EXCLUDES(){
		return INCLUDED_OR_INCLUDES_OR_EXCLUDES;
	}
	
	protected void setDefaultTerms(TermVocabulary<TaxonRelationshipType> termVocabulary) {
		TaxonRelationshipType.CONGRUENT_OR_EXCLUDE = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidCongruentToOrExcludes);
		TaxonRelationshipType.CONGRUENT_OR_INCLUDED_OR_INCLUDES = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidCongruentIncludedInOrIncludes);
		TaxonRelationshipType.CONGRUENT_OR_INCLUDES = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidCongruentToOrIncludes);
		TaxonRelationshipType.CONGRUENT_OR_INCLUDES_OR_EXCLUDES = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidCongruentToOrIncludesOrExcludes);
		TaxonRelationshipType.CONGRUENT_OR_INCLUDES_OR_OVERLAPS = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidCongruentToOrIncludesOrOverlaps);
		TaxonRelationshipType.CONGRUENT_OR_OVERLAPS = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidCongruentToOrOverlaps);
		TaxonRelationshipType.CONGRUENT_OR_OVERLAPS_OR_EXCLUDES = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidCongruentToOrOverlapsOrExcludes);
		TaxonRelationshipType.CONGRUENT_TO = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidCongruentTo);
		TaxonRelationshipType.CONTRADICTION = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidContradiction);
		TaxonRelationshipType.DOES_NOT_EXCLUDE = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidDoesNotExclude);
		TaxonRelationshipType.DOES_NOT_OVERLAP = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidDoesNotOverlap);
		TaxonRelationshipType.EXCLUDES = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidExcludes);
		TaxonRelationshipType.INCLUDED_OR_INCLUDES = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidIncludedInOrIncludes);
		TaxonRelationshipType.INCLUDED_OR_INCLUDES_OR_EXCLUDES = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidIncludedInOrIncludesOrExcludes);
		TaxonRelationshipType.INCLUDED_OR_INCLUDES_OR_OVERLAPS = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidIncludedInOrIncludesOrOverlaps);
		TaxonRelationshipType.INCLUDES = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidIncludes);
		TaxonRelationshipType.INCLUDES_OR_EXCLUDES = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidIncludesOrExcludes);
		TaxonRelationshipType.INCLUDES_OR_OVERLAPS = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidIncludesOrOverlaps);
		TaxonRelationshipType.INCLUDES_OR_OVERLAPS_OR_EXCLUDES = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidIncludesOrOverlapsOrExcludes);
		TaxonRelationshipType.INVALID_DESIGNATION_FOR = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidInvalidDesignationFor);
		TaxonRelationshipType.MISAPPLIED_NAME_FOR = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidMisappliedNameFor);
		TaxonRelationshipType.NOT_CONGRUENT_TO = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidNotCongruentTo);
		TaxonRelationshipType.NOT_INCLUDED_IN = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidNotIncludedIn);	
		TaxonRelationshipType.OVERLAPS = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidOverlaps);
		TaxonRelationshipType.OVERLAPS_OR_EXCLUDES = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidOverlapsOrExcludes);
		TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN = termVocabulary.findTermByUuid(TaxonRelationshipType.uuidTaxonomicallyIncludedIn);
	}

	//TODO ohter relationshipTypes

	@Override
	public TaxonRelationshipType readCsvLine(Class<TaxonRelationshipType> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms) {
		return super.readCsvLine(termClass, csvLine, terms);
	}
}