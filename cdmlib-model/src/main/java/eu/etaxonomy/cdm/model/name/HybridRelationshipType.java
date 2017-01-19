/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * The class representing the categories of {@link HybridRelationship hybrid relationships}
 * between a {@link BotanicalName botanical taxon name} used as a parent of a hybrid taxon
 * name and the hybrid taxon name itself. Hybrids and their parents are always
 * plants. The relationships are to be understood as 'is .... of'.
 * <P>
 * A standard (ordered) list of hybrid relationship type instances will be
 * automatically created as the project starts. But this class allows to extend
 * this standard list by creating new instances of additional hybrid
 * relationship types if needed. Hybrid relationship types are neither symmetric
 * nor transitive.

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
@XmlType(name = "HybridRelationshipType")
@XmlRootElement(name = "HybridRelationshipType")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class HybridRelationshipType extends RelationshipTermBase<HybridRelationshipType> {
	private static final long serialVersionUID = 5225908742890437668L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(HybridRelationshipType.class);

	protected static Map<UUID, HybridRelationshipType> termMap = null;

	private static final UUID uuidFirstParent = UUID.fromString("83ae9e56-18f2-46b6-b211-45cdee775bf3");
	private static final UUID uuidSecondParent = UUID.fromString("0485fc3d-4755-4f53-8832-b82774484c43");
	private static final UUID uuidThirdParent = UUID.fromString("bfae2780-92ab-4f65-b534-e68826f59e7d");
	private static final UUID uuidFourthParent = UUID.fromString("9e92083b-cb9b-4c4d-bca5-c543bbefd3c7");
	private static final UUID uuidFemaleParent = UUID.fromString("189a3ed9-6860-4943-8be8-a1f60133be2a");
	private static final UUID uuidMaleParent = UUID.fromString("8b7324c5-cc6c-4109-b708-d49b187815c4");
	private static final UUID uuidMajorParent = UUID.fromString("da759eea-e3cb-4d3c-ae75-084c2d08f4ed");
	private static final UUID uuidMinorParent = UUID.fromString("e556b240-b03f-46b8-839b-ad89df633c5a");


	public static HybridRelationshipType NewInstance(String term, String label, String labelAbbrev) {
		return new HybridRelationshipType(term, label, labelAbbrev);
	}

//********************************** Constructor *********************************/

  	//for hibernate use only
  	@Deprecated
  	protected HybridRelationshipType() {
		super(TermType.HybridRelationshipType);
	}
	/**
	 * Class constructor: creates an additional hybrid relationship type
	 * instance with a description (in the {@link Language#DEFAULT() default language}), a label,
	 * a label abbreviation and the flags indicating whether this new hybrid
	 * relationship type is symmetric and/or transitive.
	 *
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new hybrid relationship type to be created
	 * @param	label  		 the string identifying the new hybrid relationship
	 * 						 type to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new hybrid relationship type to be created
	 * @see 				 #HybridRelationshipType()
	 */
	private HybridRelationshipType(String term, String label, String labelAbbrev) {
		super(TermType.HybridRelationshipType, term, label, labelAbbrev, false, false);
	}


//************************** METHODS ********************************

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}


	protected static HybridRelationshipType getTermByUuid(UUID uuid){
        if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(HybridRelationshipType.class, uuid);
        } else {
            return termMap.get(uuid);
        }
	}

	/**
	 * Returns the "first parent" hybrid relationship type. The elements of the
	 * {@link NonViralName non-viral taxon name} used as "first parent" affect the
	 * taxon name string of the hybrid (see Appendix I of the ICBN).
	 *
	 * @see	#SECOND_PARENT()
	 * @see #THIRD_PARENT()
	 * @see #FOURTH_PARENT()
	 * @see #FEMALE_PARENT()
	 * @see #MAJOR_PARENT()
	 */
	public static final HybridRelationshipType FIRST_PARENT(){
		return getTermByUuid(uuidFirstParent);
	}

	/**
	 * Returns the "second parent" hybrid relationship type. The elements of the
	 * {@link NonViralName non-viral taxon name} used as "second parent" affect the
	 * taxon name string of the hybrid (see Appendix I of the ICBN).
	 *
	 * @see	#FIRST_PARENT()
	 * @see #MALE_PARENT()
	 * @see #MINOR_PARENT()
	 */
	public static final HybridRelationshipType SECOND_PARENT(){
		return getTermByUuid(uuidSecondParent);
	}

	/**
	 * Returns the "third parent" hybrid relationship type. The elements of the
	 * {@link NonViralName non viral taxon name} used as "third parent" affect the
	 * taxon name string of the hybrid (see Appendix I of the ICBN).
	 *
	 * @see	#FIRST_PARENT()
	 */
	public static final HybridRelationshipType THIRD_PARENT(){
		return getTermByUuid(uuidThirdParent);
	}

	/**
	 * Returns the "fourth parent" hybrid relationship type. The elements of the
	 * {@link NonViralName non viral taxon name} used as "third parent" affect the
	 * taxon name string of the hybrid (see Appendix I of the ICBN).
	 *
	 * @see	#FIRST_PARENT()
	 */
	public static final HybridRelationshipType FOURTH_PARENT(){
		return getTermByUuid(uuidFourthParent);
	}

	/**
	 * Returns the "female parent" hybrid relationship type. The taxon the name
	 * of which plays the female parent role is the genetic mother of the taxon
	 * which is the hybrid (and has the hybrid {@link NonViralName non-viral taxon name})<BR>
	 * For nomenclature purposes a "female parent" is also a "first parent".
	 *
	 * @see	#MALE_PARENT()
	 * @see	#FIRST_PARENT()
	 */
	public static final HybridRelationshipType FEMALE_PARENT(){
		return getTermByUuid(uuidFemaleParent);
	}

	/**
	 * Returns the "male parent" hybrid relationship type. The taxon the name
	 * of which plays the male parent role is the genetic father of the taxon
	 * which is the hybrid (and has the hybrid {@link NonViralName non-viral taxon name}).<BR>
	 * For nomenclature purposes a "male parent" is also a "second parent".
	 *
	 * @see	#MALE_PARENT()
	 * @see	#SECOND_PARENT()
	 */
	public static final HybridRelationshipType MALE_PARENT(){
		return getTermByUuid(uuidMaleParent);
	}

	/**
	 * Returns the "major parent" hybrid relationship type. This relationship
	 * maybe used for hybrids which have parents that are not equally represented
	 * in the child (e.g. some fern hybrids).
	 * For nomenclature purposes a "major parent" is also a "first parent".<BR>
	 * Major and minor parent relationships are usually represented in a
	 * hybrid formula with a "greater than" symbol (>). It replaces the multiplication
	 * symbol which is generally used for hybrid fromulas.
	 *
	 * @see	#FIRST_PARENT()
	 * @see #MINOR_PARENT()
	 */
	public static final HybridRelationshipType MAJOR_PARENT(){
		return getTermByUuid(uuidMajorParent);
	}

	/**
	 * Returns the "minor parent" hybrid relationship type. This relationship
	 * maybe used for hybrids which have parents that are not equally represented
	 * in the child (e.g. some fern hybrids).<BR>
	 * For nomenclature purposes a "major parent" is also a "second parent".
	 * Major and minor parent relationships are usually represented in a
	 * hybrid formula with a "greater than" symbol (>). It replaces the multiplication
	 * symbol which is generally used for hybrid fromulas.
	 *
	 * @see	#SECOND_PARENT()
	 */
	public static final HybridRelationshipType MINOR_PARENT(){
		return getTermByUuid(uuidMinorParent);
	}


	@Override
	public int compareTo(HybridRelationshipType otherRelationshipType) {
		return super.performCompareTo(otherRelationshipType, true);
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<HybridRelationshipType> termVocabulary) {
		termMap = new HashMap<UUID, HybridRelationshipType>();
		for (HybridRelationshipType term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

//	@Override
//	public HybridRelationshipType readCsvLine(Class<HybridRelationshipType> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms) {
//		return super.readCsvLine(termClass, csvLine, terms);
//	}

}
