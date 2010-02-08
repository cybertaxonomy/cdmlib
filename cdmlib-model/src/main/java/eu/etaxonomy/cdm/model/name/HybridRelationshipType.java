/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class HybridRelationshipType extends RelationshipTermBase<HybridRelationshipType> {
	private static final long serialVersionUID = 5225908742890437668L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(HybridRelationshipType.class);

	private static HybridRelationshipType FIRST_PARENT;
	private static HybridRelationshipType SECOND_PARENT;
	private static HybridRelationshipType FEMALE_PARENT;
	private static HybridRelationshipType MALE_PARENT;

	private static final UUID uuidFirstParent = UUID.fromString("83ae9e56-18f2-46b6-b211-45cdee775bf3");
	private static final UUID uuidSecondParent = UUID.fromString("0485fc3d-4755-4f53-8832-b82774484c43");
	private static final UUID uuidFemaleParent = UUID.fromString("189a3ed9-6860-4943-8be8-a1f60133be2a");
	private static final UUID uuidMaleParent = UUID.fromString("8b7324c5-cc6c-4109-b708-d49b187815c4");

	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty hybrid relationship type instance.
	 * 
	 * @see 	#HybridRelationshipType(String, String, String)
	 */
	public HybridRelationshipType() {
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
	public HybridRelationshipType(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev, false, false);
	}


	//********* METHODS **************************************/

	/**
	 * Returns the "first parent" hybrid relationship type. The elements of the
	 * {@link BotanicalName botanical taxon name} used as "first parent" affect the
	 * taxon name string of the hybrid (see Appendix I of the ICBN).
	 * 
	 * @see	#SECOND_PARENT()
	 */
	public static final HybridRelationshipType FIRST_PARENT(){
		return FIRST_PARENT;
	}

	/**
	 * Returns the "second parent" hybrid relationship type. The elements of the
	 * {@link BotanicalName botanical taxon name} used as "second parent" affect the
	 * taxon name string of the hybrid (see Appendix I of the ICBN).
	 * 
	 * @see	#FIRST_PARENT()
	 */
	public static final HybridRelationshipType SECOND_PARENT(){
		return SECOND_PARENT;
	}

	/**
	 * Returns the "female parent" hybrid relationship type. The taxon the name
	 * of which plays the female parent role is the genetic mother of the taxon
	 * which is the hybrid (and has the hybrid {@link BotanicalName botanical taxon name})
	 * For nomenclature purposes a "female parent" is also a "first parent".
	 * 
	 * @see	#MALE_PARENT()
	 * @see	#FIRST_PARENT()
	 */
	public static final HybridRelationshipType FEMALE_PARENT(){
		return FEMALE_PARENT;
	}

	/**
	 * Returns the "male parent" hybrid relationship type. The taxon the name
	 * of which plays the male parent role is the genetic father of the taxon
	 * which is the hybrid (and has the hybrid {@link BotanicalName botanical taxon name}).
	 * For nomenclature purposes a "male parent" is also a "second parent".
	 * 
	 * @see	#MALE_PARENT()
	 * @see	#SECOND_PARENT()
	 */
	public static final HybridRelationshipType MALE_PARENT(){
		return MALE_PARENT;
	}
	
	@Override
	protected void setDefaultTerms(TermVocabulary<HybridRelationshipType> termVocabulary) {
		HybridRelationshipType.FEMALE_PARENT = termVocabulary.findTermByUuid(HybridRelationshipType.uuidFemaleParent);
		HybridRelationshipType.FIRST_PARENT = termVocabulary.findTermByUuid(HybridRelationshipType.uuidFirstParent);
		HybridRelationshipType.MALE_PARENT = termVocabulary.findTermByUuid(HybridRelationshipType.uuidMaleParent);
		HybridRelationshipType.SECOND_PARENT = termVocabulary.findTermByUuid(HybridRelationshipType.uuidSecondParent);
	}
	
	@Override
	public HybridRelationshipType readCsvLine(Class<HybridRelationshipType> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms) {
		return super.readCsvLine(termClass, csvLine, terms);
	}

}