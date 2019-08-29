/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.term;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.RelationshipTermBase;


/**
 * The class representing categories of relationships between {@link DefinedTermBase terms}
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
/**
 * @author a.mueller
 * @since 28.02.2019
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TermRelationshipType")
@XmlRootElement(name = "TermRelationshipType")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class TermRelationshipType extends RelationshipTermBase<TermRelationshipType> {
	private static final long serialVersionUID = 6575652105931691670L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermRelationshipType.class);

	protected static Map<UUID, TermRelationshipType> termMap = null;

	private static final UUID uuidSameAs = UUID.fromString("c61f8e7e-6965-4975-8238-3a1269b093d2");

	private static final UUID uuidPartOf = UUID.fromString("6ea378a9-d211-4a78-8b87-674db5c9118d");
	private static final UUID uuidKindOf = UUID.fromString("9b6d38b3-9145-4302-9a46-92c7633aa013");


	public static TermRelationshipType NewInstance(String term, String label, String labelAbbrev, boolean symmetric, boolean transitive) {
		return new TermRelationshipType(term, label, labelAbbrev, symmetric, transitive);
	}


//********************************** CONSTRUCTOR *********************************/

  	/**
  	 * @deprecated for inner (hibernate) use only
  	 */
  	@Deprecated
  	protected TermRelationshipType() {
		super(TermType.TermRelationType);
	}
	/**
	 * Class constructor: creates an additional term relationship type
	 * instance with a description (in the {@link eu.etaxonomy.cdm.model.common.Language#DEFAULT() default language}),
	 * a label, a label abbreviation and the flags indicating whether this new term
	 * relationship type is symmetric and/or transitive.
	 *
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new term relationship type to be created
	 * @param	label  		 the string identifying the new taxon relationship
	 * 						 type to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new term relationship type to be created
	 * @param	symmetric	 the boolean indicating whether the new term
	 * 						 relationship type to be created is symmetric
	 * @param	transitive	 the boolean indicating whether the new term
	 * 						 relationship type to be created is transitive
	 * @see 				 #TermRelationshipType()
	 */
	private TermRelationshipType(String term, String label, String labelAbbrev, boolean symmetric, boolean transitive) {
		super(TermType.TaxonRelationshipType, term, label, labelAbbrev, symmetric, transitive);
	}


//************************** METHODS ********************************

	@Override
	public void resetTerms(){
		termMap = null;
	}

	protected static TermRelationshipType getTermByUuid(UUID uuid){
	    if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(TermRelationshipType.class, uuid);
        } else {
            return termMap.get(uuid);
        }
	}


	/**
	 * Returns the term relationship type "is same as". This
	 * indicates that the 2 terms linked in the term relationship
	 * are considered to be equivalent/congruent.
	 * This type is both symmetric and transitive.
	 */
	@Deprecated
	public static final TermRelationshipType SAME_AS(){
		return getTermByUuid(uuidSameAs);
	}

	/**
	 * Returns the term relationship type "is part of". This
	 * indicates that the concept represented by the first ("from") term
	 * is considered to be part of the second ("to") term.<BR>
	 * E.g. a country is considered to be part of a continent.<BR>
	 * It also means that both terms must not be of the exact
	 * same type, e.g. a leaf is not considered to be a plant
	 * but is part of a plant.<BR>
	 *
	 * This type is transitive but not symmetric.
	 *
	 * @see #KIND_OF()
	 */
	public static final TermRelationshipType PART_OF(){
		return getTermByUuid(uuidPartOf);
	}

	/**
     * Returns the term relationship type "is kind of". This
     * indicates that the concept represented by the first ("from") term
     * is considered to be of a similar but more specific type as the second ("to") term.<BR>
     *
     * E.g. a tree is considered to be a plant but a specific type of plant.
     * Or for more abstract terms, a homotypic synonym relation is
     * considered to be a synonym relation but a specific synonym relation.
     * So often kind-of terms define certain properties to define sets
     * of objects having this property. The more specific term then defines
     * less objects then the more general one.<BR>
     * In this way {@link #KIND_OF()} is complementary to {@link #PART_OF()}
     * Both of them can usually not be true at the same time.
     *
     * This type is transitive but not symmetric.
     *
     * @see #PART_OF()
     */
	public static final TermRelationshipType KIND_OF(){
		return getTermByUuid(uuidKindOf);
	}



	@Override
    protected void setDefaultTerms(TermVocabulary<TermRelationshipType> termVocabulary) {
		termMap = new HashMap<>();
		for (TermRelationshipType term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}


    @Override
    public TermRelationshipType readCsvLine(Class<TermRelationshipType> termClass,
            List<String> csvLine, TermType termType, Map<UUID,DefinedTermBase> terms, boolean abbrevAsId) {

        TermRelationshipType newInstance = super.readCsvLine(termClass, csvLine, termType, terms, abbrevAsId);
        newInstance.setSymbol(newInstance.getIdInVocabulary());
        String inverseLabelAbbrev = csvLine.get(7).trim();
        newInstance.setInverseSymbol(inverseLabelAbbrev);
        return newInstance;
    }
}
