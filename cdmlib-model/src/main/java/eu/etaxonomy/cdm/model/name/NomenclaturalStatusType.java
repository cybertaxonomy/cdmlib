/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;



import eu.etaxonomy.cdm.model.common.ILoadableTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

import org.apache.log4j.Logger;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * The class representing categories of nomenclatural status (like "invalid",
 * "conserved" or "novum") to qualify the use of a particular taxon name string
 * depending on its {@link reference.INomenclaturalReference nomenclatural reference} (original publication),
 * on its {@link NomenclaturalCode nomenclatural code} and on possible decisions of the corresponding
 * nomenclature authorities.
 * <P>
 * A standard (ordered) list of nomenclatural status type instances will be
 * automatically created as the project starts. But this class allows to extend
 * this standard list by creating new instances of additional nomenclatural
 * status types if needed. 
 * <P>
 * This class corresponds more or less to: <ul>
 * <li> NomenclaturalNoteTypeTerm according to the TDWG ontology
 * <li> NomenclaturalNoteType  according to the TCS
 * </ul>
 * 
 * @author a.mueller
 * @created 10.07.2008
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@Entity
public class NomenclaturalStatusType extends OrderedTermBase<NomenclaturalStatusType> {
	static Logger logger = Logger.getLogger(NomenclaturalStatusType.class);

	private static final UUID uuidAmbiguous = UUID.fromString("90f5012b-705b-4488-b4c6-002d2bc5198e");
	private static final UUID uuidDoubtful = UUID.fromString("0ffeb39e-872e-4c0f-85ba-a4150d9f9e7d");
	private static final UUID uuidConfusum = UUID.fromString("24955174-aa5c-4e71-a2fd-3efc79e885db");
	private static final UUID uuidIllegitimate = UUID.fromString("b7c544cf-a375-4145-9d3e-4b97f3f18108");
	private static final UUID uuidSuperfluous = UUID.fromString("6890483a-c6ba-4ae1-9ab1-9fbaa5736ce9");
	private static final UUID uuidRejected = UUID.fromString("48107cc8-7a5b-482e-b438-efbba050b851");
	private static final UUID uuidUtiqueRejected = UUID.fromString("04338fdd-c12a-402f-a1ca-68b4bf0be042");
	private static final UUID uuidConservedProp = UUID.fromString("82bab006-5aed-4301-93ec-980deb30cbb1");
	private static final UUID uuidOrthographyConservedProp = UUID.fromString("02f82bc5-1066-454b-a023-11967cba9092");
	private static final UUID uuidLegitimate = UUID.fromString("51a3613c-b53b-4561-b0cd-9163d91c15aa");
	private static final UUID uuidAlternative = UUID.fromString("3b8a8519-420f-4dfa-b050-b410cc257961");
	private static final UUID uuidNovum = UUID.fromString("05fcb68f-af60-4851-b912-892512058897");
	private static final UUID uuidUtiqueRejectedProp = UUID.fromString("643ee07f-026c-426c-b838-c778c8613383");
	private static final UUID uuidOrthographyConserved = UUID.fromString("34a7d383-988b-4117-b8c0-52b947f8c711");
	private static final UUID uuidRejectedProp = UUID.fromString("248e44c2-5436-4526-a352-f7467ecebd56");
	private static final UUID uuidConserved = UUID.fromString("6330f719-e2bc-485f-892b-9f882058a966");
	private static final UUID uuidSanctioned = UUID.fromString("1afe55c4-76aa-46c0-afce-4dc07f512733");
	private static final UUID uuidInvalid = UUID.fromString("b09d4f51-8a77-442a-bbce-e7832aaf46b7");
	private static final UUID uuidNudum = UUID.fromString("e0d733a8-7777-4b27-99a3-05ab50e9f312");
	private static final UUID uuidCombinationInvalid = UUID.fromString("f858e619-7b7f-4225-913b-880a2143ec83");
	private static final UUID uuidProvisional = UUID.fromString("a277507e-ad93-4978-9419-077eb889c951");
	private static final UUID uuidValid = UUID.fromString("bd036217-5499-4ccd-8f4c-72e06158db93");
	private static final UUID uuidOpusUtiqueOppr = UUID.fromString("a5055d80-dbba-4660-b091-a1835d59fe7c");
	private static final UUID uuidSubnudum = UUID.fromString("92a76bd0-6ea8-493f-98e0-4be0b98c092f");


	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty nomenclatural status type instance.
	 * 
	 * @see 	#NomenclaturalStatusType(String, String, String)
	 * @see 	#readCsvLine(List, Language)
	 * @see 	#readCsvLine(List)
	 */
	public NomenclaturalStatusType() {
		super();
	}

	/** 
	 * Class constructor: creates an additional nomenclatural status type
	 * instance with a description, a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new nomenclatural status type to be created 
	 * @param	label  		 the string identifying the new nomenclatural status
	 * 						 type to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new nomenclatural status type to be created
	 * @see 	#NomenclaturalStatusType()
	 * @see 	#readCsvLine(List, Language)
	 * @see 	#readCsvLine(List)
	 */
	public NomenclaturalStatusType(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	//********* METHODS **************************************/

	/**
	 * Returns the nomenclatural status type identified through its immutable
	 * universally unique identifier (UUID).
	 * 
	 * @param	uuid	the universally unique identifier
	 * @return  		the nomenclatural status type corresponding to the given
	 * 					universally unique identifier
	 */
	public static final NomenclaturalStatusType getByUuid(UUID uuid){
		return (NomenclaturalStatusType) findByUuid(uuid);
	}


	/**
	 * Returns the boolean value indicating whether this nomenclatural status
	 * type is itself "invalid" or a kind of "invalid" (true) or not (false).
	 * Returns false if this nomenclatural status type is null.
	 *
	 * @see  #isLegitimateType()
	 * @see  #isIllegitimateType()
	 * @see  common.DefinedTermBase#getKindOf()
	 */
	@Transient
	public boolean isInvalidType(){
		if (this.equals(INVALID())
			|| this.equals(NUDUM())
			|| 	this.equals(PROVISIONAL())
			|| 	this.equals(COMBINATION_INVALID())
			|| 	this.equals(OPUS_UTIQUE_OPPR())
			){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Returns the boolean value indicating whether this nomenclatural status
	 * type is itself "legitimate" or a kind of "legitimate" (true)
	 * or not (false). Returns false if this nomenclatural status type is null.
	 *
	 * @see  #isInvalidType()
	 * @see  #isIllegitimateType()
	 * @see  common.DefinedTermBase#getKindOf()
	 */
	@Transient
	public boolean isLegitimateType(){
		if (this.equals(LEGITIMATE()) ||
				this.equals(NOVUM()) ||
				this.equals(ALTERNATIVE()) ||
				this.equals(CONSERVED()) ||
				this.equals(ORTHOGRAPHY_CONSERVED()) ||
				this.equals(REJECTED_PROP()) ||
				this.equals(UTIQUE_REJECTED_PROP())
			){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Returns the boolean value indicating whether this nomenclatural status
	 * type is itself "illegitimate" or a kind of "illegitimate" (true)
	 * or not (false). Returns false if this nomenclatural status type is null.
	 *
	 * @see  #isInvalidType()
	 * @see  #isLegitimateType()
	 * @see  common.DefinedTermBase#getKindOf()
	 */
	@Transient
	public boolean isIllegitimateType(){
		if (this.equals(ILLEGITIMATE()) ||
				this.equals(SUPERFLUOUS()) ||
				this.equals(REJECTED()) ||
				this.equals(UTIQUE_REJECTED()) ||
				this.equals(CONSERVED_PROP()) ||
				this.equals(ORTHOGRAPHY_CONSERVED_PROP())
			){
			return true;
		}else{
			return false;
		}
	}

	public static final NomenclaturalStatusType AMBIGUOUS(){
		return getByUuid(uuidAmbiguous);
	}

	public static final NomenclaturalStatusType DOUBTFUL(){
		return getByUuid(uuidDoubtful);
	}

	public static final NomenclaturalStatusType CONFUSUM(){
		return getByUuid(uuidConfusum);
	}

	public static final NomenclaturalStatusType ILLEGITIMATE(){
		return getByUuid(uuidIllegitimate);
	}

	public static final NomenclaturalStatusType SUPERFLUOUS(){
		return getByUuid(uuidSuperfluous);
	}

	public static final NomenclaturalStatusType REJECTED(){
		return getByUuid(uuidRejected);
	}

	public static final NomenclaturalStatusType UTIQUE_REJECTED(){
		return getByUuid(uuidUtiqueRejected);
	}

	public static final NomenclaturalStatusType CONSERVED_PROP(){
		return getByUuid(uuidConservedProp);
	}

	public static final NomenclaturalStatusType ORTHOGRAPHY_CONSERVED_PROP(){
		return getByUuid(uuidOrthographyConservedProp);
	}

	public static final NomenclaturalStatusType LEGITIMATE(){
		return getByUuid(uuidLegitimate);
	}

	public static final NomenclaturalStatusType ALTERNATIVE(){
		return getByUuid(uuidAlternative);
	}

	public static final NomenclaturalStatusType NOVUM(){
		return getByUuid(uuidNovum);
	}

	public static final NomenclaturalStatusType UTIQUE_REJECTED_PROP(){
		return getByUuid(uuidUtiqueRejectedProp);
	}

	public static final NomenclaturalStatusType ORTHOGRAPHY_CONSERVED(){
		return getByUuid(uuidOrthographyConserved);
	}

	public static final NomenclaturalStatusType REJECTED_PROP(){
		return getByUuid(uuidRejectedProp);
	}

	public static final NomenclaturalStatusType CONSERVED(){
		return getByUuid(uuidConserved);
	}

	public static final NomenclaturalStatusType SANCTIONED(){
		return getByUuid(uuidSanctioned);
	}

	public static final NomenclaturalStatusType INVALID(){
		return getByUuid(uuidInvalid);
	}

	public static final NomenclaturalStatusType NUDUM(){
		return getByUuid(uuidNudum);
	}

	public static final NomenclaturalStatusType COMBINATION_INVALID(){
		return getByUuid(uuidCombinationInvalid);
	}

	public static final NomenclaturalStatusType PROVISIONAL(){
		return getByUuid(uuidProvisional);
	}

	public static final NomenclaturalStatusType VALID(){
		return getByUuid(uuidValid);
	}

	/**
	 * Returns the nomenclatural status type "subnudum". This type is not
	 * covered by nomenclature codes. It appears sometimes in literature and
	 * represents the opinion of the author who considers the name to be
	 * unusable for an unambiguous taxonomic use.
	 */
	public static final NomenclaturalStatusType SUBNUDUM(){
		return getByUuid(uuidSubnudum);
	}

	/**
	 * Returns the nomenclatural status type "opus utique oppressum". This type
	 * relates to article 32.7 (old ICBN) and article 32.9 as well as App. 6
	 * (new ICBN). This is a reference list of botanical opera, in which all
	 * names (or names of a certain rank) are oppressed. Such a name has the
	 * status "invalid" but in contrary to "nomen rejicendum" not a single name
	 * is rejected by the commission but an opus with regard to the validity of
	 * the names occurring in it.
	 */
	public static final NomenclaturalStatusType OPUS_UTIQUE_OPPR(){
		return getByUuid(uuidOpusUtiqueOppr);
	}


//TODO Soraya
//	orth. var.: orthographic variant
//	pro syn.: pro synonymo

	/** TODO
	 * preliminary implementation for BotanicalNameParser
	 *  * not yet complete
	 */
	@Transient
	public static NomenclaturalStatusType getNomenclaturalStatusTypeByAbbreviation(String statusAbbreviation) throws UnknownCdmTypeException{
		if (statusAbbreviation == null){ throw new NullPointerException("statusAbbreviation is 'null' in getNomenclaturalStatusTypeByAbbreviation");
		}else if (statusAbbreviation.equalsIgnoreCase("nom. superfl.")){ return NomenclaturalStatusType.SUPERFLUOUS();
		}else if (statusAbbreviation.equalsIgnoreCase("nom. nud.")){ return NomenclaturalStatusType.NUDUM();
		}else if (statusAbbreviation.equalsIgnoreCase("nom. illeg.")){return NomenclaturalStatusType.ILLEGITIMATE();
		}else if (statusAbbreviation.equalsIgnoreCase("nom. inval.")) { return NomenclaturalStatusType.INVALID();
		}else if (statusAbbreviation.equalsIgnoreCase("nom. cons.")) { return NomenclaturalStatusType.CONSERVED();
		}else if (statusAbbreviation.equalsIgnoreCase("nom. alternativ.")) { return NomenclaturalStatusType.ALTERNATIVE();
		}else if (statusAbbreviation.equalsIgnoreCase("nom. altern.")) { return NomenclaturalStatusType.ALTERNATIVE();
		}else if (statusAbbreviation.equalsIgnoreCase("nom. rej.")) { return NomenclaturalStatusType.REJECTED();
		}else if (statusAbbreviation.equalsIgnoreCase("nom. rej. prop.")) { return NomenclaturalStatusType.REJECTED_PROP();
		}else if (statusAbbreviation.equalsIgnoreCase("nom. provis.")) { return NomenclaturalStatusType.PROVISIONAL();
		}else if (statusAbbreviation.equalsIgnoreCase("nom. subnud.")) { return NomenclaturalStatusType.SUBNUDUM();
		}else if (statusAbbreviation.equalsIgnoreCase("opus utique oppr.")) { return NomenclaturalStatusType.OPUS_UTIQUE_OPPR();

		//TODO
		}else {
			if (statusAbbreviation == null){
				statusAbbreviation = "(null)";
			}
			throw new eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException("Unknown NomenclaturalStatusType abbreviation: " + statusAbbreviation);
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#readCsvLine(java.util.List, eu.etaxonomy.cdm.model.common.Language)
	 */
	/** 
	 * Fills this nomenclatural status type with contents (uuid, uri, 
	 * description text, label and label abbreviation) coming from a csv line.
	 * This method overrides the method of {@link common.DefinedTermBase DefinedTermBase}.
	 * 
	 * @param	csvLine_ 	the (ordered) list of substrings from a csv string
	 * 						to be used to fill this nomenclatural status type
	 * @param	lang		the language in which the description text for this
	 * 						nomenclatural status type is written
	 * @see					#NomenclaturalStatusType(String, String, String)
	 * @see					#readCsvLine(List)
	 * @see					common.DefinedTermBase#readCsvLine(List, Language)
	 */
	@Override
	public ILoadableTerm readCsvLine(List csvLine_, Language lang) {
		List<String> csvLine = (List<String>) csvLine_;
		this.setUuid(UUID.fromString(csvLine.get(0)));
		this.setUri(csvLine.get(1));
		String label = csvLine.get(2).trim();
		String abbreviatedLabel = csvLine.get(3);
		String text = label;
		this.addRepresentation(Representation.NewInstance(text, label, abbreviatedLabel, lang) );
		return this;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#readCsvLine(java.util.List)
	 */
	/** 
	 * Fills this nomenclatural status type with contents (uuid, uri, 
	 * description text, label and label abbreviation) coming from a csv line.
	 * The implicit language for the description text is "latin".
	 * This method overrides the method of {@link common.DefinedTermBase DefinedTermBase}.
	 * 
	 * @param	csvLine_ 	the (ordered) list of substrings from a csv string
	 * 						to be used to fill this nomenclatural status type
	 * @see					#NomenclaturalStatusType(String, String, String)
	 * @see					#readCsvLine(List, Language)
	 * @see					common.DefinedTermBase#readCsvLine(List)
	 */
	@Override
	public ILoadableTerm readCsvLine(List csvLine) {   //TODO should be List<String> but makes error for some strange reason
		return readCsvLine(csvLine, Language.LATIN());
	}


//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#readCsvLine(java.util.List)
//	 */
//	@Override
//	public ILoadableTerm readCsvLine(List<String> csvLine) {
//		return readCsvLine(csvLine, Language.ENGLISH());
//	}
//
//
//	public ILoadableTerm readCsvLine(List<String> csvLine, Language lang) {
//		this.setUuid(UUID.fromString(csvLine.get(0)));
//		this.setUri(csvLine.get(1));
//		String text = csvLine.get(3);
//		String label = csvLine.get(2).trim();
//		String abbreviatedLabel = null;
//		this.addRepresentation(Representation.NewInstance(text, label, abbreviatedLabel, lang) );
//		return this;
//	}

}
