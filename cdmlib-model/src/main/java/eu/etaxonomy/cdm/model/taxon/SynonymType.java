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
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * The class representing categories of {@link Synonym synonyms}
 * (like "heterotypic synonym of").
 * <P>
 * A standard (ordered) list of synonym type instances will be
 * automatically created as the project starts. But this class allows to extend
 * this standard list by creating new instances of additional synonym
 * types if needed.
 * <P>
 * This class corresponds in part to: <ul>
 * <li> TaxonRelationshipTerm according to the TDWG ontology
 * <li> RelationshipType according to the TCS
 * </ul>
 *
 * @author m.doering
 * @created 08-Nov-2007 13:06:55
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SynonymType")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class SynonymType extends RelationshipTermBase<SynonymType> {
	private static final long serialVersionUID = -3775216614202923889L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SynonymType.class);

	protected static Map<UUID, SynonymType> termMap = null;

	public static final UUID uuidSynonymOf = UUID.fromString("1afa5429-095a-48da-8877-836fa4fe709e");
	public static final UUID uuidHomotypicSynonymOf = UUID.fromString("294313a9-5617-4ed5-ae2d-c57599907cb2");
	public static final UUID uuidHeterotypicSynonymOf = UUID.fromString("4c1e2c59-ca55-41ac-9a82-676894976084");
	public static final UUID uuidInferredSynonymOf = UUID.fromString("cb5bad12-9dbc-4b38-9977-162e45089c11");
	public static final UUID uuidInferredGenusOf = UUID.fromString("f55a574b-c1de-45cc-9ade-1aa2e098c3b5");
	public static final UUID uuidInferredEpithetOf = UUID.fromString("089c1926-eb36-47e7-a2d1-fd5f3918713d");
	public static final UUID uuidPotentialCombinationOf = UUID.fromString("7c45871f-6dc5-40e7-9f26-228318d0f63a");


//********************************** CONSTRUCTOR *********************************/

  	//for hibernate use only
  	@Deprecated
  	protected SynonymType() {
		super(TermType.SynonymType);
	}

	/**
	 * Class constructor: creates an additional synonym type
	 * instance with a description (in the {@link Language#DEFAULT() default language}), a label and
	 * a label abbreviation. Synonym types can be neither
	 * symmetric nor transitive.
	 *
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new synonym type to be created
	 * @param	label  		 the string identifying the new synonym
	 * 						 type to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new synonym type to be created
	 * @see 				 #SynonymType()
	 */
	private SynonymType(String term, String label, String labelAbbrev) {
		super(TermType.SynonymType, term, label, labelAbbrev, false, false);
	}


//************************** METHODS ********************************

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}


	protected static SynonymType getTermByUuid(UUID uuid){
        if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(SynonymType.class, uuid);
        } else {
            return termMap.get(uuid);
        }
	}

	/**
	 * Returns the synonym type "is synonym of". This indicates
	 * that the reference asserting the synonym relationship does not know
	 * whether both {@link name.TaxonName taxon names} involved are typified by the same type or
	 * not.
	 *
	 * @see		#HOMOTYPIC_SYNONYM_OF()
	 * @see		#HETEROTYPIC_SYNONYM_OF()
	 */
	public static final SynonymType SYNONYM_OF(){
		return getTermByUuid(uuidSynonymOf);
	}

	/**
	 * Returns the synonym relationship type "is homotypic synonym of"
	 * ("is nomenclatural synonym of" in zoology). This indicates that the
	 * the reference asserting the synonym relationship holds that
	 * the {@link name.TaxonName taxon name} used as a {@link Synonym synonym} and the taxon name used as the
	 * ("accepted/correct") {@link Taxon taxon} are typified by the same type.
	 * In this case they should belong to the same {@link name.HomotypicalGroup homotypical group}.
	 *
	 * @see		#HETEROTYPIC_SYNONYM_OF()
	 * @see		#SYNONYM_OF()
	 */
	public static final SynonymType HOMOTYPIC_SYNONYM_OF(){
		return getTermByUuid(uuidHomotypicSynonymOf);
	}

	/**
	 * Returns the synonym relationship type "is heterotypic synonym of"
	 * ("is taxonomic synonym of" in zoology). This indicates that the
	 * the reference asserting the synonym relationship holds that
	 * the {@link name.TaxonName taxon name} used as a {@link Synonym synonym} and the taxon name used as the
	 * ("accepted/correct") {@link Taxon taxon} are not typified by the same type.
	 * In this case they should not belong to the same {@link name.HomotypicalGroup homotypical group}.
	 *
	 * @see		#HOMOTYPIC_SYNONYM_OF()
	 * @see		#SYNONYM_OF()
	 */
	public static final SynonymType HETEROTYPIC_SYNONYM_OF(){
		return getTermByUuid(uuidHeterotypicSynonymOf);
	}

	/**
	 * Returns the synonym relationship type "is inferred synonym of".
	 * This synonym relationship type is used in zoology whenever a synonymy relationship on species or infraspecific
	 * level is derived from a genus synonymy.
	 */
	public static final SynonymType INFERRED_SYNONYM_OF(){
		return getTermByUuid(uuidInferredSynonymOf);
	}

	/**
	 * Returns the synonym relationship type "is inferred genus of".
	 * This synonym relationship type is used in zoology whenever a synonymy relationship on species or infraspecific
	 * level is derived from a epithet synonymy.
	 */
	public static final SynonymType INFERRED_GENUS_OF(){
		return getTermByUuid(uuidInferredGenusOf);
	}

	/**
	 * Returns the synonym relationship type "is inferred synonym of".
	 * This synonym relationship type is used in zoology whenever a synonymy relationship on species or infraspecific
	 * level is derived from a genus synonymy.
	 */
	public static final SynonymType INFERRED_EPITHET_OF(){
		return getTermByUuid(uuidInferredEpithetOf);
	}

	public static SynonymType POTENTIAL_COMBINATION_OF() {
		return getTermByUuid(uuidPotentialCombinationOf);
	}


//	@Override
//	public SynonymType readCsvLine(Class<SynonymType> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms) {
//		return super.readCsvLine(termClass, csvLine, terms);
//	}

	@Override
	protected void setDefaultTerms(TermVocabulary<SynonymType> termVocabulary) {
		termMap = new HashMap<UUID, SynonymType>();
		for (SynonymType term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}



}
