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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * The terms in this class define the status of a {@link NameTypeDesignation name type designation}.
 *
 * @author a.babadshanjan
 * @created 20.03.2009
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NameTypeDesignationStatus")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class NameTypeDesignationStatus extends TypeDesignationStatusBase<NameTypeDesignationStatus> {
	private static final long serialVersionUID = -8801837496688711907L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NameTypeDesignationStatus.class);

	private static final UUID uuidAutomatic = UUID.fromString("e89d8b21-615a-4602-913f-1625bf39a69f");
	private static final UUID uuidMonotypy = UUID.fromString("3fc639b2-9a64-45f8-9a81-657a4043ad74");
	private static final UUID uuidNotApplicable = UUID.fromString("91a9d6a9-7754-41cd-9f7e-be136f599f7e");
	private static final UUID uuidOriginalDesignation = UUID.fromString("40032a44-973b-4a64-b25e-76f86c3a753c");
	private static final UUID uuidPresentDesignation = UUID.fromString("e5f38f5d-995d-4470-a036-1a9792a543fc");
	private static final UUID uuidSubsequentMonotypy = UUID.fromString("2b5806d8-31b0-406e-a32a-4adac0c89ae4");
	private static final UUID uuidSubsequentDesignation = UUID.fromString("3e449e7d-a03c-4431-a7d3-aa258406f6b2");
	private static final UUID uuidTautonymy = UUID.fromString("84521f09-3e10-43f5-aa6f-2173a55a6790");
	private static final UUID uuidLectotype = UUID.fromString("4177c938-b741-40e1-95e5-4c53bd1ed87d");

	/**
	 * Factory method: creates an additional type designation status instance
	 * with a description (in the {@link eu.etaxonomy.cdm.model.common.Language#DEFAULT() default language}), a label
	 * and a label abbreviation.
	 *
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new name type designation status to be created
	 * @param	label  		 the string identifying the new name type designation
	 * 						 status to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new name type designation status to be created
	 */
	public static NameTypeDesignationStatus NewInstance(String term, String label, String labelAbbrev){
		return new NameTypeDesignationStatus(term, label, labelAbbrev);
	}

	protected static Map<UUID, NameTypeDesignationStatus> termMap = null;

	protected static NameTypeDesignationStatus findTermByUuid(UUID uuid){
        if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(NameTypeDesignationStatus.class, uuid);
        } else {
            return termMap.get(uuid);
        }
	}

	//********************************** Constructor *********************************/

  	//for hibernate use only
  	@Deprecated
  	protected NameTypeDesignationStatus() {
		super (TermType.NameTypeDesignationStatus);
	}

	protected NameTypeDesignationStatus(String term, String label, String labelAbbrev) {
		super(TermType.NameTypeDesignationStatus, term, label, labelAbbrev);
	}

//************************** METHODS ********************************

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}


	/**
	 * Returns the "automatic" name type designation status.</BR>
     * If a new name has to be established for a genus name this new name automatically gets
     * the same type species as the old name.
     * ICZN 67.8
	 */
	public static final NameTypeDesignationStatus AUTOMATIC(){
		return findTermByUuid(uuidAutomatic);
	}

	/**
	 * Returns the "monotypy" name type designation status.</BR>
	 * Only one species was included in original genus description.
	 * ICZN 68.3.
	 * No {@linkplain TypeDesignationBase#getCitation() citation} is needed
	 * for a monotypy as the type species is decided within the original paper.
	 */
	public static final NameTypeDesignationStatus MONOTYPY(){
		return findTermByUuid(uuidMonotypy);
	}

	/**
	 * Returns the "not applicable" name type designation status.</BR>
	 * Used in the BDWD (BioSystematic Database of World Diptera) for
	 * nomina nuda, emendations and misspellings.
	 */
	public static final NameTypeDesignationStatus NOT_APPLICABLE(){
		return findTermByUuid(uuidNotApplicable);
	}

	/**
	 * Returns the "original designation" name type designation status.</BR>
	 * The type species is designated in the original genus description
	 * (this includes indication in the species name typicus).
	 * ICZN 68.2<Br>
	 * No {@linkplain TypeDesignationBase#getCitation() citation} is needed
	 * for an original designation as the type species is decided within the original paper.
	 */
	public static final NameTypeDesignationStatus ORIGINAL_DESIGNATION(){
		return findTermByUuid(uuidOriginalDesignation);
	}

	/**
	 * Returns the "present designation" name type designation status.</BR>
	 * The type species is designated now (maybe possible in future,
	 * after ICZN has changed and online publications are available).
	 */
	public static final NameTypeDesignationStatus PRESENT_DESIGNATION(){
		return findTermByUuid(uuidPresentDesignation);
	}

	/**
	 * Returns the "subsequent monotypy" name type designation status.</BR>
	 * If only one nominal species was first subsequently included
	 * in a nominal genus or subgenus established without included species,
	 * that nominal species is automatically fixed as the type species,
	 * by subsequent monotypy.
	 * ICZN 69.3
	 */
	public static final NameTypeDesignationStatus SUBSEQUENT_MONOTYPY(){
		return findTermByUuid(uuidSubsequentMonotypy);
	}

	/**
	 * Returns the "subsequent designation" name type designation status.</BR>
	 * Several species were included in the original genus description.
	 * One of these has been designated as type species in a later publication.
	 *
	 * ICZN 69.1
	 */
	public static final NameTypeDesignationStatus SUBSEQUENT_DESIGNATION(){
		return findTermByUuid(uuidSubsequentDesignation);
	}


	/**
	 * Returns the lectotype name type designation status.</BR>
	 * This may be the same as a {@link SUBSEQUENT_DESIGNATION()} but used in botany.
	 * Maybe these 2 status will be merged in future.
	 */
	public static final NameTypeDesignationStatus LECTOTYPE(){
		return findTermByUuid(uuidLectotype);
	}

	/**
	 * Returns the "tautonomy" name type designation status.</BR>
	 * The genus name is the same as the species name of one of the included species.
	 * ICZN 68.4, 68.5
	 */
	public static final NameTypeDesignationStatus TAUTONYMY(){
		return findTermByUuid(uuidTautonymy);
	}


	@Override
	protected void setDefaultTerms(TermVocabulary<NameTypeDesignationStatus> termVocabulary) {
		termMap = new HashMap<UUID, NameTypeDesignationStatus>();
		for (NameTypeDesignationStatus term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

	/**
	 * Returns the boolean value indicating whether <i>this</i> type designation
	 * status is itself "lectotype" or a kind of "lectotype" (true) or not
	 * (false). Returns false if <i>this</i> type designation status is null.<BR>
	 * A lectotype is a {@link eu.etaxonomy.cdm.model.occurrence.DerivedUnit specimen or illustration} designated as the
	 * nomenclatural type, when no holotype was indicated at the time of
	 * publication of the "type-bringing" {@link TaxonName taxon name}, when the
	 * holotype is found to be assigned to taxon names belonging to more than
	 * one {@link HomotypicalGroup homotypical group}, or as long as it is missing.
	 *
	 * @see  #LECTOTYPE()
	 * @see  #HOLOTYPE()
	 * @see  eu.etaxonomy.cdm.model.common.DefinedTermBase#getKindOf()
	 */
	@Transient
	public boolean isLectotype(){
		if (
				this.equals(LECTOTYPE()) ||
				this.equals(SUBSEQUENT_DESIGNATION()) ||
				this.equals(PRESENT_DESIGNATION() )
				){
			return true;
		}else{
			return false;
		}
	}
}
