/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;


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
 * @since 08-Nov-2007 13:06:17
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonRelationshipType")
@XmlRootElement(name = "TaxonRelationshipType")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.term.DefinedTermBase")
@Audited
public class TaxonRelationshipType
        extends RelationshipTermBase<TaxonRelationshipType> {

	private static final long serialVersionUID = 6575652105931691670L;
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	protected static Map<UUID, TaxonRelationshipType> termMap = null;

	private static final UUID uuidTaxonomicallyIncludedIn = UUID.fromString("d13fecdf-eb44-4dd7-9244-26679c05df1c");

	public static final UUID uuidMisappliedNameFor = UUID.fromString("1ed87175-59dd-437e-959e-0d71583d8417");
	public static final UUID uuidProParteMisappliedNameFor = UUID.fromString("b59b4bd2-11ff-45d1-bae2-146efdeee206");
	public static final UUID uuidPartialMisappliedNameFor = UUID.fromString("859fb615-b0e8-440b-866e-8a19f493cd36");
	public static final UUID uuidProParteSynonymFor = UUID.fromString("8a896603-0fa3-44c6-9cd7-df2d8792e577");
	public static final UUID uuidPartialSynonymFor = UUID.fromString("9d7a5e56-973c-474c-b6c3-a1cb00833a3c");

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

	private static final UUID uuidAllRelationships = UUID.fromString("831fcd88-e5c9-49e0-b06e-bbb67d1c05c9");

	private static final UUID uuidUnclear = UUID.fromString("4c48ba25-c1d0-4bdd-9260-c1fa2e42a5d3");
    private static final UUID uuidNotYetWorkedOn = UUID.fromString("8d47e59a-790d-428f-8060-01d443519166");

    //currently only used for ERMS import for synonym relationship where the synonym needs to be of class
    //Taxon for some reason.
    public static final UUID uuidHeterotypicSynonymTaxonRelationship = UUID.fromString("8f1be54b-c693-4d58-ab36-6f389fc9bd1f");
    public static final UUID uuidHomotypicSynonymTaxonRelationship = UUID.fromString("bfe114b9-1a25-4199-b8b0-6599eb53ae8a");
    public static final UUID uuidSynonymOfTaxonRelationship = UUID.fromString("cc648276-0823-47b1-9deb-fa7c046e4afd");



	public static TaxonRelationshipType NewInstance(String term, String label, String labelAbbrev, boolean symmetric, boolean transitive) {
		return new TaxonRelationshipType(term, label, labelAbbrev, symmetric, transitive);
	}


//********************************** CONSTRUCTOR *********************************/

    //for hibernate use only, *packet* private required by bytebuddy
  	@Deprecated
  	TaxonRelationshipType() {
		super(TermType.TaxonRelationshipType);
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
	private TaxonRelationshipType(String term, String label, String labelAbbrev, boolean symmetric, boolean transitive) {
		super(TermType.TaxonRelationshipType, term, label, labelAbbrev, symmetric, transitive);
	}


//************************** METHODS ********************************

	@Override
	public void resetTerms(){
		termMap = null;
	}


	protected static TaxonRelationshipType getTermByUuid(UUID uuid){
	    if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(TaxonRelationshipType.class, uuid);
        } else {
            return termMap.get(uuid);
        }
	}

    /**
     * <code>true</code> if this relationship type is {@link #isAnyMisappliedName()
     * any of the misapplied name relationship types} or an
     * {@link #INVALID_DESIGNATION_FOR() invalid designation}
     *
     * @see #isAnyMisappliedName()()
     */
	public boolean isMisappliedName(){
        if (this.isAnyMisappliedName()){
            return true;
        }
        return false;
    }

    /**
     * <code>true</code> if this relationship type is any
     * of the misapplied name relationships such as
     * {@link #MISAPPLIED_NAME_FOR()} or {@link #PRO_PARTE_MISAPPLIED_NAME_FOR()}
     *
     * @see #isMisappliedNameOrInvalidDesignation()
     */
    public boolean isAnyMisappliedName(){
        return (allMisappliedNameTypes().contains(this));
    }

    /**
     * <code>true</code> if this relationship type is any
     * of the {@link #isAnyMisappliedName() misapplied name relationships} or
     * any of the {@link #isAnySynonym() (pro parte) synonym relationships}
     */
    public boolean isAnySynonymOrMisappliedName(){
        return (allMisappliedNameTypes().contains(this) || allSynonymTypes().contains(this));
    }


    /**
     * <code>true</code> if this relationship type is any
     * of the pro parte or partial synonym relationship types
     * {@link #PRO_PARTE_SYNONYM_FOR()} or {@link #PARTIAL_SYNONYM_FOR()}
     *
     * @see #isAnyMisappliedName()
     */
    public boolean isAnySynonym(){
        return (allSynonymTypes().contains(this));
    }

    /**
     * <code>true</code> if this relationship type is either
     * a pro parte synonym or a pro parte misapplied name relationship type
     * {@link #PRO_PARTE_SYNONYM_FOR()} or {@link #PRO_PARTE_MISAPPLIED_NAME_FOR}
     */
    public boolean isProParte(){
        return (allProParteTypes().contains(this));
    }

    /**
     * <code>true</code> if this relationship type is either
     * a partial synonym or a partial misapplied name relationship type
     * {@link #PARTIAL_SYNONYM_FOR()} or {@link #PARTIAL_MISAPPLIED_NAME_FOR}
     *
     * @see #isProParte()
     */
    public boolean isPartial(){
        return (allPartialTypes().contains(this));
    }

    /**
     * Returns a list of all misapplied name relationship
     * types such as "misapplied name for" and
     * "pro parte misapplied name for".
     *
     * @see #MISAPPLIED_NAME_FOR()
     * @see #PRO_PARTE_MISAPPLIED_NAME_FOR()
     */
    public static Set<TaxonRelationshipType> allMisappliedNameTypes(){
        Set<TaxonRelationshipType> result = new HashSet<>();
        result.add(MISAPPLIED_NAME_FOR());
        result.add(PRO_PARTE_MISAPPLIED_NAME_FOR());
        result.add(PARTIAL_MISAPPLIED_NAME_FOR());
        return result;
    }

    public static Set<TaxonRelationshipType> allSynonymTypes(){
        Set<TaxonRelationshipType> result = new HashSet<>();
        result.add(PRO_PARTE_SYNONYM_FOR());
        result.add(PARTIAL_SYNONYM_FOR());
        return result;
    }

    public static Set<TaxonRelationshipType> allProParteTypes(){
        Set<TaxonRelationshipType> result = new HashSet<>();
        result.add(PRO_PARTE_SYNONYM_FOR());
        result.add(PRO_PARTE_MISAPPLIED_NAME_FOR());
        return result;
    }

    public static Set<TaxonRelationshipType> allPartialTypes(){
        Set<TaxonRelationshipType> result = new HashSet<>();
        result.add(PARTIAL_SYNONYM_FOR());
        result.add(PARTIAL_MISAPPLIED_NAME_FOR());
        return result;
    }


	/**
	 * Returns <code>true</code>, if this relationship type is not a <i>misapplied name for<i>
	 * and also no <i>taxonomically included in</i> relationship.<BR>
	 * It assumes that all other relationships are concept relationships.
	 * @return
	 */
	public boolean isConceptRelationship(){
		if (this.isMisappliedName()){
			return false;
        }else if (this.equals(TAXONOMICALLY_INCLUDED_IN())){
			return false;
		}
		return true;
	}

	/**
	 * Returns the taxon relationship type "is taxonomically included in". This
	 * indicates that the {@link TaxonRelationship#getFromTaxon() source taxon}
	 * in such a {@link TaxonRelationship taxon relationship} has the target {@link Taxon taxon}
	 * as immediate next higher parent within the classification. Generally
	 * the {@link Taxon#getSec() concept reference} of both taxa are the same
	 * except if the concept reference follows the taxonomical opinion of
	 * another reference.<BR>
	 * This type is neither symmetric nor transitive.
	 * @deprecated will be removed in version 3.0
	 */
	@Deprecated
	public static final TaxonRelationshipType TAXONOMICALLY_INCLUDED_IN(){
		return getTermByUuid(uuidTaxonomicallyIncludedIn);
	}
	/**
	 * Returns the taxon relationship type "is misapplied name for". This
	 * indicates that the {@link eu.etaxonomy.cdm.model.name.TaxonName taxon name}
	 * of the {@link TaxonRelationship#getFromTaxon() source taxon}
	 * in such a {@link TaxonRelationship taxon relationship} has been erroneously used by
	 * the {@link TaxonBase#getSec() concept reference} to denominate the same real taxon
	 * as the one meant by the target {@link Taxon taxon}.<BR>
	 * This type is neither symmetric nor transitive.
	 */
	public static final TaxonRelationshipType MISAPPLIED_NAME_FOR(){
		return getTermByUuid(uuidMisappliedNameFor);
	}
    /**
     * Returns the taxon relationship type "is pro parte misapplied name for". This
     * indicates that the {@link eu.etaxonomy.cdm.model.name.TaxonName taxon name} of the
     * {@link TaxonRelationship#getFromTaxon() source taxon}
     * in such a {@link TaxonRelationship taxon relationship} has been erroneously used by
     * the {@link TaxonBase#getSec() concept reference} to (partly) denominate the same real taxon
     * as the one meant by the target {@link Taxon taxon}. Additionaly another real taxon
     * is (partly) denominated by the given name in the concept reference. Therefore it is called
     * pro parte. <BR>
     * This type is neither symmetric nor transitive.
     *
     * @see #MISAPPLIED_NAME_FOR()
     * @see #PRO_PARTE_SYNONYM_FOR()
     */
    public static final TaxonRelationshipType PRO_PARTE_MISAPPLIED_NAME_FOR(){
        return getTermByUuid(uuidProParteMisappliedNameFor);
    }
    /**
     * Returns the taxon relationship type "is partial misapplied name for". This
     * indicates that the {@link eu.etaxonomy.cdm.model.name.TaxonName taxon name} of the
     * {@link TaxonRelationship#getFromTaxon() source taxon}
     * in such a {@link TaxonRelationship taxon relationship} has been erroneously used by
     * the {@link TaxonBase#getSec() concept reference} to (partly) denominate the same real taxon
     * as the one meant by the target {@link Taxon taxon}. In contrary to a
     * {@link #PRO_PARTE_MISAPPLIED_NAME_FOR() pro parte misapplied name} no other real taxon
     * is (partly) demoninated by the given name in the concept reference. Therefore it is called
     * partial.<BR>
     * This type is neither symmetric nor transitive.
     *
     * @see #MISAPPLIED_NAME_FOR()
     * @see #PRO_PARTE_MISAPPLIED_NAME_FOR()
     * @see #PARTIAL_SYNONYM_FOR()
     * @see #INCLUDES()
     */
    public static final TaxonRelationshipType PARTIAL_MISAPPLIED_NAME_FOR(){
        return getTermByUuid(uuidPartialMisappliedNameFor);
    }
    /**
     * Returns the taxon relationship type "is pro parte synonym for". This
     * indicates that the {@link eu.etaxonomy.cdm.model.name.TaxonName taxon name} of the
     * {@link TaxonRelationship#getFromTaxon() source taxon}
     * in such a {@link TaxonRelationship taxon relationship} has been used as a
     * pro parte synonym.<BR>
     * This type is neither symmetric nor transitive.
     *
     * @see #PRO_PARTE_MISAPPLIED_NAME_FOR()
     */
    public static final TaxonRelationshipType PRO_PARTE_SYNONYM_FOR(){
        return getTermByUuid(uuidProParteSynonymFor);
    }
    /**
     * Returns the taxon relationship type "is partial synonym for". This
     * indicates that the {@link eu.etaxonomy.cdm.model.name.TaxonName taxon name} of the
     * {@link TaxonRelationship#getFromTaxon() source taxon}
     * in such a {@link TaxonRelationship taxon relationship} has been used as a
     * partial synonym.<BR>
     * This type is neither symmetric nor transitive.
     *
     * @see #PRO_PARTE_SYNONYM_FOR()
     * @see #PARTIAL_MISAPPLIED_NAME_FOR()
     */
    public static final TaxonRelationshipType PARTIAL_SYNONYM_FOR(){
        return getTermByUuid(uuidPartialSynonymFor);
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
		return getTermByUuid(uuidContradiction);
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
		return getTermByUuid(uuidCongruentTo);
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
		return getTermByUuid(uuidIncludes);
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
		return getTermByUuid(uuidOverlaps);
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
		return getTermByUuid(uuidExcludes);
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
		return getTermByUuid(uuidDoesNotExclude);
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
		return getTermByUuid(uuidDoesNotOverlap);
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
		return getTermByUuid(uuidNotIncludedIn);
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
		return getTermByUuid(uuidNotCongruentTo);
	}

	public static final TaxonRelationshipType CONGRUENT_OR_INCLUDES(){
		return getTermByUuid(uuidCongruentToOrIncludes);
	}
	public static final TaxonRelationshipType INCLUDED_OR_INCLUDES(){
		return getTermByUuid(uuidIncludedInOrIncludes);
	}
	public static final TaxonRelationshipType CONGRUENT_OR_INCLUDED_OR_INCLUDES(){
		return getTermByUuid(uuidCongruentIncludedInOrIncludes);
	}
	public static final TaxonRelationshipType CONGRUENT_OR_OVERLAPS(){
		return getTermByUuid(uuidCongruentToOrOverlaps);
	}

	public static final TaxonRelationshipType INCLUDES_OR_OVERLAPS(){
		return getTermByUuid(uuidIncludesOrOverlaps);
	}

	public static final TaxonRelationshipType CONGRUENT_OR_INCLUDES_OR_EXCLUDES(){
		return getTermByUuid(uuidCongruentToOrIncludesOrExcludes);
	}

	public static final TaxonRelationshipType INCLUDES_OR_EXCLUDES(){
		return getTermByUuid(uuidIncludesOrExcludes);
	}

	public static final TaxonRelationshipType CONGRUENT_OR_EXCLUDES(){
		return getTermByUuid(uuidCongruentToOrExcludes);
	}

	public static final TaxonRelationshipType INCLUDED_OR_INCLUDES_OR_OVERLAPS(){
		return getTermByUuid(uuidIncludedInOrIncludesOrOverlaps);
	}

	public static final TaxonRelationshipType CONGRUENT_OR_INCLUDES_OR_OVERLAPS(){
		return getTermByUuid(uuidCongruentToOrIncludesOrOverlaps);
	}

	public static final TaxonRelationshipType INCLUDES_OR_OVERLAPS_OR_EXCLUDES(){
		return getTermByUuid(uuidIncludesOrOverlapsOrExcludes);
	}

	public static final TaxonRelationshipType CONGRUENT_OR_OVERLAPS_OR_EXCLUDES(){
		return getTermByUuid(uuidCongruentToOrOverlapsOrExcludes);
	}

	public static final TaxonRelationshipType OVERLAPS_OR_EXCLUDES(){
		return getTermByUuid(uuidOverlapsOrExcludes);
	}

	public static final TaxonRelationshipType INCLUDED_OR_INCLUDES_OR_EXCLUDES(){
		return getTermByUuid(uuidIncludedInOrIncludesOrExcludes);
	}

	public static final TaxonRelationshipType UNCLEAR(){
	    return getTermByUuid(uuidUnclear);
	}

    public static final TaxonRelationshipType NOT_YET_WORKED_ON(){
        return getTermByUuid(uuidNotYetWorkedOn);
    }


	@Override
    protected void setDefaultTerms(TermVocabulary<TaxonRelationshipType> termVocabulary) {
		termMap = new HashMap<>();
		for (TaxonRelationshipType term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

	//TODO other relationshipTypes

	public static final TaxonRelationshipType ALL_RELATIONSHIPS(){
		return getTermByUuid(uuidAllRelationships);
	}

    @Override
    public TaxonRelationshipType readCsvLine(Class<TaxonRelationshipType> termClass, List<String> csvLine, TermType termType,
            Map<UUID,DefinedTermBase> terms, boolean abbrevAsId) {
        TaxonRelationshipType newInstance = super.readCsvLine(termClass, csvLine, termType, terms, abbrevAsId);

        newInstance.setSymbol(newInstance.getIdInVocabulary());
        String inverseLabelAbbrev = csvLine.get(7).trim();
        newInstance.setInverseSymbol(inverseLabelAbbrev);
        return newInstance;
    }

    /**
     * @return a set containing all UUIDs of relationship types representing
     * a pro parte or partial synonym relationship (e.g.
     * {@link #PRO_PARTE_SYNONYM_FOR()} or {@link #PARTIAL_SYNONYM_FOR()}
     */
    public static Set<UUID> proParteOrPartialSynonymUuids() {
        Set<UUID> result = new HashSet<>();
        result.add(uuidProParteSynonymFor);
        result.add(uuidPartialSynonymFor);
        return result;
    }

    /**
     * @return a set containing all UUIDs of relationship types representing
     * a misapplied name relationship (e.g. {@link #MISAPPLIED_NAME_FOR()},
     * {@link #PRO_PARTE_MISAPPLIED_NAME_FOR()} and {@link #PARTIAL_MISAPPLIED_NAME_FOR()}
     */
    public static Set<UUID> misappliedNameUuids() {
        Set<UUID> result = new HashSet<>();
        result.add(uuidMisappliedNameFor);
        result.add(uuidProParteMisappliedNameFor);
        result.add(uuidPartialMisappliedNameFor);
        return result;
    }


    /**
     * @return a set containing all UUIDs of relationship types representing
     * a partial relationship (e.g. {@link #PARTIAL_SYNONYM_FOR()} and
     * {@link #PARTIAL_MISAPPLIED_NAME_FOR()})
     */
    public static Set<UUID> partialUuids() {
        Set<UUID> result = new HashSet<>();
        result.add(uuidPartialSynonymFor);
        result.add(uuidPartialSynonymFor);
        return result;
    }

    /**
     * @return a set containing all UUIDs of relationship types representing
     * a pro parte relationship (e.g. {@link #PRO_PARTE_SYNONYM_FOR()} and
     * {@link #PRO_PARTE_MISAPPLIED_NAME_FOR()})
     */
    public static Set<UUID> proParteUuids() {
        Set<UUID> result = new HashSet<>();
        result.add(uuidProParteSynonymFor);
        result.add(uuidProParteMisappliedNameFor);
        return result;
    }

    /**
     * @return a set containing all UUIDs of relationship types representing
     * a pseudo taxon relationship (a synonym relationship expressed as
     * taxon relationship as the synonym side for some reason must be
     * handled as pseudo accepted taxon, e.g. because factual data
     * is attached from an import)
     */
    public static Set<UUID> pseudoTaxonUuids() {
        Set<UUID> result = new HashSet<>();
        result.add(uuidHomotypicSynonymTaxonRelationship);
        result.add(uuidHeterotypicSynonymTaxonRelationship);
        result.add(uuidSynonymOfTaxonRelationship);
        return result;
    }

}
