// $Id$
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

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * 
 * @author a.babadshanjan
 * @version 1.0
 * @created 20.03.2009
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NameTypeDesignationStatus")
@Entity
@Audited
public class NameTypeDesignationStatus extends DefinedTermBase<NameTypeDesignationStatus> {
	static Logger logger = Logger.getLogger(NameTypeDesignationStatus.class);

//	private static NameTypeDesignationStatus AUTOMATIC;
//	private static NameTypeDesignationStatus FIRST_REVISOR;
//	private static NameTypeDesignationStatus MONOTYPY;
//	private static NameTypeDesignationStatus NOT_APPLICABLE;
//	private static NameTypeDesignationStatus ORIGINAL_DESIGNATION;
//	private static NameTypeDesignationStatus PRESENT_DESIGNATION;
//	private static NameTypeDesignationStatus SUBSEQUENT_MONOTYPY;
//	private static NameTypeDesignationStatus SUBSEQUENT_DESIGNATION;
//	private static NameTypeDesignationStatus TAUTONOMY;

	private static final UUID uuidAutomatic = UUID.fromString("e89d8b21-615a-4602-913f-1625bf39a69f");
	private static final UUID uuidFirstRevisor = UUID.fromString("a14ec046-c48f-4a73-939f-bd57880c7565");
	private static final UUID uuidMonotypy = UUID.fromString("3fc639b2-9a64-45f8-9a81-657a4043ad74");
	private static final UUID uuidNotApplicable = UUID.fromString("91a9d6a9-7754-41cd-9f7e-be136f599f7e");
	private static final UUID uuidOriginalDesignation = UUID.fromString("40032a44-973b-4a64-b25e-76f86c3a753c");
	private static final UUID uuidPresentDesignation = UUID.fromString("e5f38f5d-995d-4470-a036-1a9792a543fc");
	private static final UUID uuidSubsequentMonotypy = UUID.fromString("2b5806d8-31b0-406e-a32a-4adac0c89ae4");
	private static final UUID uuidSubsequentDesignation = UUID.fromString("3e449e7d-a03c-4431-a7d3-aa258406f6b2");
	private static final UUID uuidTautonymy = UUID.fromString("84521f09-3e10-43f5-aa6f-2173a55a6790");
	
	protected static Map<UUID, NameTypeDesignationStatus> termMap = null;		
	
	protected static NameTypeDesignationStatus findTermByUuid(UUID uuid){
		if (termMap == null){
			return null;
		}
		return (NameTypeDesignationStatus)termMap.get(uuid);
	}

	// ************* CONSTRUCTORS *************/	

	/** 
	 * Class constructor: creates a new empty name type designation status instance.
	 * 
	 * @see 	#NameTypeDesignationStatus(String, String, String)
	 */
	public NameTypeDesignationStatus() {
	}
	
	/** 
	 * Class constructor: creates an additional type designation status instance
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
	public NameTypeDesignationStatus(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	//********* METHODS **************************************
	

	/**
	 * Returns the "automatic" name type designation status. 
     * If a new name has to be established for a genus name this new name automatically gets 
     * the same type species as the old name.
	 */
	public static final NameTypeDesignationStatus AUTOMATIC(){
		return findTermByUuid(uuidAutomatic);
	}

	/**
	 * Returns the "first revisor" name type designation status.
	 * Used in the BDWD for incorrect original spellings only. This is only a way of dealing 
	 * with misspellings in the database, not an actual type designation.
	 */
	public static final NameTypeDesignationStatus FIRST_REVISOR(){
		return findTermByUuid(uuidFirstRevisor);
	}
	
	/**
	 * Returns the "monotypy" name type designation status.
	 * Only one species was included in original genus description. 
	 */
	public static final NameTypeDesignationStatus MONOTYPY(){
		return findTermByUuid(uuidMonotypy);
	}
	
	/**
	 * Returns the "not applicable" name type designation status. 
	 * Used in the BDWD for nomina nuda, emendations and misspellings.
	 */
	public static final NameTypeDesignationStatus NOT_APPLICABLE(){
		return findTermByUuid(uuidNotApplicable);
	}
	
	/**
	 * Returns the "original designation" name type designation status.
	 * The type species is designated in the original genus description 
	 * (this includes indication in the species name typicus).
	 */
	public static final NameTypeDesignationStatus ORIGINAL_DESIGNATION(){
		return findTermByUuid(uuidOriginalDesignation);
	}
	
	/**
	 * Returns the "present designation" name type designation status. 
	 * The type species is designated now (maybe possible in future, 
	 * after ICZN has changed).
	 */
	public static final NameTypeDesignationStatus PRESENT_DESIGNATION(){
		return findTermByUuid(uuidPresentDesignation);
	}
	
	/**
	 * Returns the "subsequent monotypy" name type designation status.
	 * If only one nominal species was first subsequently included 
	 * in a nominal genus or subgenus established without included species, 
	 * that nominal species is automatically fixed as the type species, 
	 * by subsequent monotypy.
	 */
	public static final NameTypeDesignationStatus SUBSEQUENT_MONOTYPY(){
		return findTermByUuid(uuidSubsequentMonotypy);
	}
	
	/**
	 * Returns the "subsequent designation" name type designation status. 
	 * Several species were included in the original genus description. 
	 * One of these has been designated as type species in a later publication.
	 */
	public static final NameTypeDesignationStatus SUBSEQUENT_DESIGNATION(){
		return findTermByUuid(uuidSubsequentDesignation);
	}
	
	/**
	 * Returns the "tautonomy" name type designation status. 
	 * The genus name is the same as the species name of one of the included species.
	 */
	public static final NameTypeDesignationStatus TAUTONYMY(){
		return findTermByUuid(uuidTautonymy);
	}
	
//	@Override
//	protected void setDefaultTerms(TermVocabulary<NameTypeDesignationStatus> termVocabulary) {
//		NameTypeDesignationStatus.AUTOMATIC = termVocabulary.findTermByUuid(NameTypeDesignationStatus.uuidAutomatic);
//		NameTypeDesignationStatus.FIRST_REVISOR = termVocabulary.findTermByUuid(NameTypeDesignationStatus.uuidFirstRevisor);
//		NameTypeDesignationStatus.MONOTYPY = termVocabulary.findTermByUuid(NameTypeDesignationStatus.uuidMonotypy);
//		NameTypeDesignationStatus.NOT_APPLICABLE = termVocabulary.findTermByUuid(NameTypeDesignationStatus.uuidNotApplicable);
//		NameTypeDesignationStatus.ORIGINAL_DESIGNATION = termVocabulary.findTermByUuid(NameTypeDesignationStatus.uuidOriginalDesignation);
//		NameTypeDesignationStatus.PRESENT_DESIGNATION = termVocabulary.findTermByUuid(NameTypeDesignationStatus.uuidPresentDesignation);
//		NameTypeDesignationStatus.SUBSEQUENT_MONOTYPY = termVocabulary.findTermByUuid(NameTypeDesignationStatus.uuidSubsequentMonotypy);
//		NameTypeDesignationStatus.SUBSEQUENT_DESIGNATION = termVocabulary.findTermByUuid(NameTypeDesignationStatus.uuidSubsequentDesignation);
//		NameTypeDesignationStatus.TAUTONOMY = termVocabulary.findTermByUuid(NameTypeDesignationStatus.uuidTautonymy);
//	}

	@Override
	protected void setDefaultTerms(TermVocabulary<NameTypeDesignationStatus> termVocabulary) {
		termMap = new HashMap<UUID, NameTypeDesignationStatus>();
		for (NameTypeDesignationStatus term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}
}