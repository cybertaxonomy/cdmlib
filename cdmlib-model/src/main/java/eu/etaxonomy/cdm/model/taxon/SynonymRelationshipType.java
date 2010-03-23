/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * The class representing categories of {@link SynonymRelationship synonym relationships}
 * (like "heterotypic synonym of").
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
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class SynonymRelationshipType extends RelationshipTermBase<SynonymRelationshipType> {
	
	static Logger logger = Logger.getLogger(SynonymRelationshipType.class);

	private static SynonymRelationshipType SYNONYM_OF;
	private static SynonymRelationshipType HOMOTYPIC_SYNONYM_OF;
	private static SynonymRelationshipType HETEROTYPIC_SYNONYM_OF;
	private static SynonymRelationshipType INFERRED_SYNONYM_OF;
	
	private static SynonymRelationshipType INFERRED_GENUS_OF; 
	private static SynonymRelationshipType INFERRED_EPITHET_OF;
	private static SynonymRelationshipType POTENTIAL_COMBINATION_OF;

	private static final UUID uuidSynonymOf = UUID.fromString("1afa5429-095a-48da-8877-836fa4fe709e");
	private static final UUID uuidHomotypicSynonymOf = UUID.fromString("294313a9-5617-4ed5-ae2d-c57599907cb2");
	private static final UUID uuidHeterotypicSynonymOf = UUID.fromString("4c1e2c59-ca55-41ac-9a82-676894976084");
	private static final UUID uuidInferredSynonymOf = UUID.fromString("cb5bad12-9dbc-4b38-9977-162e45089c11");
	private static final UUID uuidInferredGenusOf = UUID.fromString("f55a574b-c1de-45cc-9ade-1aa2e098c3b5");
	private static final UUID uuidInferredEpithetOf = UUID.fromString("089c1926-eb36-47e7-a2d1-fd5f3918713d");
	private static final UUID uuidPotentialCombinationOf = UUID.fromString("7c45871f-6dc5-40e7-9f26-228318d0f63a");
	

	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty synonym relationship type instance.
	 * 
	 * @see 	#SynonymRelationshipType(String, String, String)
	 */
	public SynonymRelationshipType() {
	}

	/** 
	 * Class constructor: creates an additional synonym relationship type
	 * instance with a description (in the {@link eu.etaxonomy.cdm.model.common.Language#DEFAULT() default language}), a label and
	 * a label abbreviation. Synonym relationships types can be neither
	 * symmetric nor transitive.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new synonym relationship type to be created 
	 * @param	label  		 the string identifying the new synonym relationship
	 * 						 type to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new synonym relationship type to be created
	 * @see 				 #SynonymRelationshipType()
	 */
	public SynonymRelationshipType(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev, false, false);
	}

	//********* METHODS **************************************/
	
	/**
	 * Returns the synonym relationship type "is synonym of". This indicates
	 * that the reference asserting the {@link SynonymRelationship synonym relationship} does not know
	 * whether both {@link name.TaxonNameBase taxon names} involved are typified by the same type or
	 * not.
	 * 
	 * @see		#HOMOTYPIC_SYNONYM_OF()
	 * @see		#HETEROTYPIC_SYNONYM_OF()
	 */
	public static final SynonymRelationshipType SYNONYM_OF(){
		return SYNONYM_OF;
	}

	/**
	 * Returns the synonym relationship type "is homotypic synonym of"
	 * ("is nomenclatural synonym of" in zoology). This indicates that the
	 * the reference asserting the {@link SynonymRelationship synonym relationship} holds that
	 * the {@link name.TaxonNameBase taxon name} used as a {@link Synonym synonym} and the taxon name used as the
	 * ("accepted/correct") {@link Taxon taxon} are typified by the same type.
	 * In this case they should belong to the same {@link name.HomotypicalGroup homotypical group}.
	 * 
	 * @see		#HETEROTYPIC_SYNONYM_OF()
	 * @see		#SYNONYM_OF()
	 */
	public static final SynonymRelationshipType HOMOTYPIC_SYNONYM_OF(){
		return HOMOTYPIC_SYNONYM_OF;
	}

	/**
	 * Returns the synonym relationship type "is heterotypic synonym of"
	 * ("is taxonomic synonym of" in zoology). This indicates that the
	 * the reference asserting the {@link SynonymRelationship synonym relationship} holds that
	 * the {@link name.TaxonNameBase taxon name} used as a {@link Synonym synonym} and the taxon name used as the
	 * ("accepted/correct") {@link Taxon taxon} are not typified by the same type.
	 * In this case they should not belong to the same {@link name.HomotypicalGroup homotypical group}.
	 * 
	 * @see		#HOMOTYPIC_SYNONYM_OF()
	 * @see		#SYNONYM_OF()
	 */
	public static final SynonymRelationshipType HETEROTYPIC_SYNONYM_OF(){
		return HETEROTYPIC_SYNONYM_OF;
	}
	
	/**
	 * Returns the synonym relationship type "is inferred synonym of".
	 * This synonym relationship type is used in zoology whenever a synonymy relationship on species or infraspecific
	 * level is derived from a genus synonymy.
	 */
	public static final SynonymRelationshipType INFERRED_SYNONYM_OF(){
		return INFERRED_SYNONYM_OF;
	}
	
	/**
	 * Returns the synonym relationship type "is inferred genus of".
	 * This synonym relationship type is used in zoology whenever a synonymy relationship on species or infraspecific
	 * level is derived from a epithet synonymy.
	 */
	public static final SynonymRelationshipType INFERRED_GENUS_OF(){
		return INFERRED_GENUS_OF;
	}
	
	/**
	 * Returns the synonym relationship type "is inferred synonym of".
	 * This synonym relationship type is used in zoology whenever a synonymy relationship on species or infraspecific
	 * level is derived from a genus synonymy.
	 */
	public static final SynonymRelationshipType INFERRED_EPITHET_OF(){
		return INFERRED_EPITHET_OF;
	}
	
	public static SynonymRelationshipType POTENTIAL_COMBINATION_OF() {
		return POTENTIAL_COMBINATION_OF;
	}

	@Override
	public SynonymRelationshipType readCsvLine(Class<SynonymRelationshipType> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms) {
		return super.readCsvLine(termClass, csvLine, terms);
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<SynonymRelationshipType> termVocabulary) {
		SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF = termVocabulary.findTermByUuid(SynonymRelationshipType.uuidHeterotypicSynonymOf);
		SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF = termVocabulary.findTermByUuid(SynonymRelationshipType.uuidHomotypicSynonymOf);
		SynonymRelationshipType.SYNONYM_OF = termVocabulary.findTermByUuid(SynonymRelationshipType.uuidSynonymOf);
		SynonymRelationshipType.INFERRED_SYNONYM_OF = termVocabulary.findTermByUuid(SynonymRelationshipType.uuidInferredSynonymOf);
		SynonymRelationshipType.INFERRED_GENUS_OF = termVocabulary.findTermByUuid(SynonymRelationshipType.uuidInferredGenusOf);
		SynonymRelationshipType.INFERRED_EPITHET_OF = termVocabulary.findTermByUuid(SynonymRelationshipType.uuidInferredEpithetOf);
		SynonymRelationshipType.POTENTIAL_COMBINATION_OF = termVocabulary.findTermByUuid(SynonymRelationshipType.uuidPotentialCombinationOf);
	}

	

}