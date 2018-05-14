/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * The class representing categories of nomenclatural status (like "invalid",
 * "conserved" or "novum") to qualify the use of a particular taxon name string
 * depending on its {@link eu.etaxonomy.cdm.model.reference.INomenclaturalReference nomenclatural reference} (original publication),
 * on its {@link NomenclaturalCode nomenclatural code} and on possible decisions of the corresponding
 * competent authorities. Unfortunately the ICBN and the ICZN use sometimes
 * different words for the same meaning or the same word for different meanings
 * (for instance "valid" and "legitimate").
 * <P>
 * A standard (ordered) list of nomenclatural status type instances will be
 * automatically created as the project starts. But this class allows to extend
 * this standard list by creating new instances of additional nomenclatural
 * status types if needed. The present standard list follows the ICBN
 * terminology.
 * <P>
 * This class corresponds more or less to: <ul>
 * <li> NomenclaturalNoteTypeTerm according to the TDWG ontology
 * <li> NomenclaturalNoteType  according to the TCS
 * </ul>
 *
 * @author a.mueller
 * @since 10.07.2008
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NomenclaturalStatusType")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class NomenclaturalStatusType extends OrderedTermBase<NomenclaturalStatusType> {

	private static final long serialVersionUID = 1337101678484153972L;

	private static Logger logger = Logger.getLogger(NomenclaturalStatusType.class);

	//Botanical uuids
	public static final UUID uuidIcnafpNomStatusVocabulary = UUID.fromString("bb28cdca-2f8a-4f11-9c21-517e9ae87f1f");

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
	private static final UUID uuidCombinationIllegitimate = UUID.fromString("d901d455-4e01-45cb-b653-01a840b97eed");
	private static final UUID uuidProvisional = UUID.fromString("a277507e-ad93-4978-9419-077eb889c951");
	private static final UUID uuidValid = UUID.fromString("bd036217-5499-4ccd-8f4c-72e06158db93");
	private static final UUID uuidOpusUtiqueOppr = UUID.fromString("a5055d80-dbba-4660-b091-a1835d59fe7c");
	private static final UUID uuidSubnudum = UUID.fromString("92a76bd0-6ea8-493f-98e0-4be0b98c092f");
	private static final UUID uuidCombNov = UUID.fromString("ed508710-deef-44b1-96f6-1ce6d2c9c884");
	private static final UUID uuidOrthographyRejected = UUID.fromString("39a25673-f716-4ec7-ae27-2498fce43166");
	private static final UUID uuidConservedDesig = UUID.fromString("4e9c9702-a74d-4033-9d47-792ad123712c");
	private static final UUID uuidIned = UUID.fromString("51429574-c6f9-4aa1-bab9-0bbc5b160ba1");

	//zoological uuids
	public static final UUID uuidIcznNomStatusVocabulary = UUID.fromString("5e3c08e9-13a9-498e-861e-b9b5656ab6ac");

	private static final UUID uuidZooNotAvailable = UUID.fromString("6d9ed462-b761-4da3-9304-4749e883d4eb");
	private static final UUID uuidZooInvalid = UUID.fromString("2bef7039-c129-410b-815e-2a1f7249127b");
	private static final UUID uuidZooSuppressed = UUID.fromString("a61602c7-fbd4-4eb4-98a2-44919db8920b");
	private static final UUID uuidZooOblitum = UUID.fromString("6a6f7a88-991f-4f76-8ce9-4110839fae8b");


	public static NomenclaturalStatusType NewInstance(String description, String label, String labelAbbrev, Language language) {
		return new NomenclaturalStatusType(description, label, labelAbbrev, language);
	}

	public static NomenclaturalStatusType NewInstance(String description, String label, String labelAbbrev) {
		return new NomenclaturalStatusType(description, label, labelAbbrev);
	}


	protected static Map<UUID, NomenclaturalStatusType> termMap = null;
	private static Map<String, UUID> abbrevMap = null;
	private static Map<String, UUID> labelMap = null;


	protected static Map<UUID, NomenclaturalStatusType> zooTermMap = null;
	private static Map<String, UUID> zooAbbrevMap = null;
	private static Map<String, UUID> zooLabelMap = null;



	protected static NomenclaturalStatusType getTermByUuid(UUID uuid){
	    if ((termMap == null || termMap.isEmpty()) && (zooTermMap == null || zooTermMap.isEmpty())){
	        return getTermByClassAndUUID(NomenclaturalStatusType.class, uuid);

	    }
		NomenclaturalStatusType result = null;
		if (termMap != null){
			result = termMap.get(uuid);
		}
		if (result == null && zooTermMap != null){
			result = zooTermMap.get(uuid);
		}
		return result;
	}


//********************************** Constructor *********************************/

  	//for hibernate use only
  	@Deprecated
  	protected NomenclaturalStatusType() {
		super(TermType.NomenclaturalStatusType);
	}

	/**
	 * Class constructor: creates an additional nomenclatural status type
	 * instance with a description (in the {@link eu.etaxonomy.cdm.model.common.Language#DEFAULT() default language}), a label
	 * and a label abbreviation.
	 *
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new nomenclatural status type to be created
	 * @param	label  		 the string identifying the new nomenclatural status
	 * 						 type to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new nomenclatural status type to be created
	 * @see 				 #NomenclaturalStatusType()
	 * @see 				 #readCsvLine(List, Language)
	 * @see 				 #readCsvLine(List)
	 */
	private NomenclaturalStatusType(String term, String label, String labelAbbrev) {
		super(TermType.NomenclaturalStatusType, term, label, labelAbbrev);
	}

	private NomenclaturalStatusType(String term, String label, String labelAbbrev, Language language) {
		super(TermType.NomenclaturalStatusType);
		this.addRepresentation(new Representation(term, label, labelAbbrev, language));
	}

//********* METHODS **************************************


	@Override
	public void resetTerms(){
		termMap = null;
		zooTermMap = null;
	}

	/**
	 * Returns the boolean value indicating whether <i>this</i> nomenclatural status
	 * type is itself "invalid" or a kind of "invalid" (true) or not (false) -
	 * this corresponds to "not available" for {@link IZoologicalName zoological names} -.
	 * Returns false if <i>this</i> nomenclatural status type is null. The use
	 * of "invalid" {@link TaxonName taxon names} should be avoided.<BR>
	 * A taxon name is "invalid" if it is not "valid"; this means that
	 * the taxon name:<ul>
	 * <li>has not been effectively published or
	 * <li>has a form which does not comply with the rules of the
	 * 	   {@link NomenclaturalCode nomenclature code} or
	 * <li>is not accompanied by a description or diagnosis or by a reference to
	 * 	   such a previously published description or diagnosis
	 * </ul>
	 *
	 * @see  #VALID()
	 * @see  #isIllegitimateType()
	 * @see  eu.etaxonomy.cdm.model.common.DefinedTermBase#getKindOf()
	 */
	@Transient
	public boolean isInvalidType(){
		if (this.equals(INVALID())
			|| this.equals(NUDUM())
			|| 	this.equals(PROVISIONAL())
			||  this.equals(INED())
			|| 	this.equals(COMBINATION_INVALID())
			|| 	this.equals(OPUS_UTIQUE_OPPR())
			||  this.equals(ZOO_NOT_AVAILABLE())
			){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Returns the boolean value indicating whether <i>this</i> nomenclatural status
	 * type is itself "legitimate" or a kind of "legitimate" (true)
	 * or not (false). Corresponds to "valid" for {@link IZoologicalName zoological names}.<BR>
	 * Returns false if <i>this</i> nomenclatural status type is null.<BR>
	 * A "valid" (zool.: "available") {@link TaxonName taxon name}, unless "rejected",
	 * is "legitimate" if it was not "superfluous" when published
	 * or has been later "conserved".<BR>
	 *
	 * @see  #isInvalidType()
	 * @see  #isIllegitimateType()
	 * @see  eu.etaxonomy.cdm.model.common.DefinedTermBase#getKindOf()
	 */
	@Transient
	public boolean isLegitimateType(){
		if (this.equals(LEGITIMATE()) ||
				this.equals(NOVUM()) ||
				this.equals(ALTERNATIVE()) ||
				this.equals(CONSERVED()) ||
				this.equals(ORTHOGRAPHY_CONSERVED()) ||
				this.equals(REJECTED_PROP()) ||
				this.equals(UTIQUE_REJECTED_PROP()) ||
				this.equals(COMB_NOV())||
				this.equals(CONSERVED_DESIG())
			){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Returns the boolean value indicating whether <i>this</i> nomenclatural status
	 * type is itself "illegitimate" or a kind of "illegitimate" (true)
	 * or not (false) - this corresponds to "invalid" for {@link IZoologicalName zoological names} -.
	 * Returns false if <i>this</i> nomenclatural status type is null.<BR>
	 * A "valid" ("available") {@link TaxonName taxon name}, unless "conserved" or
	 * "sanctioned", is "illegitimate" if it was "superfluous" when published
	 * or has been later "rejected".
	 *
	 * @see  #VALID()
	 * @see  #isInvalidType()
	 * @see  #ILLEGITIMATE()
	 * @see  #CONSERVED()
	 * @see  #SANCTIONED()
	 * @see  eu.etaxonomy.cdm.model.common.DefinedTermBase#getKindOf()
	 */
	@Transient
	public boolean isIllegitimateType(){
		if (this.equals(ILLEGITIMATE()) ||
				this.equals(SUPERFLUOUS()) ||
				this.equals(REJECTED()) ||
				this.equals(UTIQUE_REJECTED()) ||
				this.equals(CONSERVED_PROP()) ||
				this.equals(ORTHOGRAPHY_CONSERVED_PROP()) ||
				this.equals(ZOO_INVALID()) ||
				this.equals(ZOO_SUPPRESSED()) ||
				this.equals(ORTHOGRAPHY_REJECTED())
			){
			return true;
		}else{
			return false;
		}
	}


	/**
	 * Returns the nomenclatural status type "ambiguous". A "valid"
	 * ("available") {@link TaxonName taxon name} is "ambiguous" if it has been used so long
	 * by different authors in different senses (other than the originally
	 * intended) that it has become a persistent cause of error and confusion.<BR>
	 * An "ambiguous" taxon name is treated as if "rejected" and is therefore
	 * also "illegitimate" ("invalid" for {@link IZoologicalName zoological names}).
	 *
	 * @see  #VALID()
	 * @see  #REJECTED()
	 * @see  #isIllegitimateType()
	 */
	public static final NomenclaturalStatusType AMBIGUOUS(){
		return getTermByUuid(uuidAmbiguous);
	}

	/**
	 * Returns the nomenclatural status type "doubtful" (dubious). A "valid"
	 * ("available") {@link TaxonName taxon name} is "doubtful" if its
	 * application is uncertain; the confusion being derived from an incomplete
	 * or confusing description.<BR>
	 * A "doubtful" taxon name is treated as if "rejected" and is therefore
	 * also "illegitimate" (("invalid" for {@link IZoologicalName zoological names}).
	 *
	 * @see  #VALID()
	 * @see  #REJECTED()
	 * @see  #isIllegitimateType()
	 */
	public static final NomenclaturalStatusType DOUBTFUL(){
		return getTermByUuid(uuidDoubtful);
	}

	/**
	 * Returns the nomenclatural status type "confusum". A "valid" ("available")
	 * {@link TaxonName taxon name} is "confusum" if it has been widely
	 * and persistently used for a taxon or taxa not including its type.<BR>
	 * A "confusum" taxon name is treated as if "rejected" and is therefore
	 * also "illegitimate" ("invalid" for {@link IZoologicalName zoological names}).
	 *
	 * @see  #VALID()
	 * @see  #REJECTED()
	 * @see  #isIllegitimateType()
	 */
	public static final NomenclaturalStatusType CONFUSUM(){
		return getTermByUuid(uuidConfusum);
	}

	/**
	 * Returns the nomenclatural status type "illegitimate" ("invalid" for
	 * {@link IZoologicalName zoological names}). A "valid" ("available")
	 * {@link TaxonName taxon name}, unless "conserved" or "sanctioned", is "illegitimate"
	 * if it was "superfluous" when published or has been later "rejected".<BR>
	 *
	 * @see  #VALID()
	 * @see  #SUPERFLUOUS()
	 * @see  #REJECTED()
	 */
	public static final NomenclaturalStatusType ILLEGITIMATE(){
		return getTermByUuid(uuidIllegitimate);
	}

	/**
	 * Returns the nomenclatural status type "superfluous". A "valid"
	 * ("available") {@link TaxonName taxon name} is "superfluous" if, when published,
	 * the taxon to which it was applied, as circumscribed by its {@link NonViralName#getCombinationAuthorship() author},
	 * definitely included the type of a name which ought to have been adopted,
	 * or of which the epithet ought to have been adopted, under the rules of
	 * the {@link NomenclaturalCode nomenclature code}, and if it has not been later declared
	 * "conserved" or "sanctioned" by the competent authorities.<BR>
	 * A "superfluous" taxon name is therefore also "illegitimate" ("invalid" for
	 * {@link IZoologicalName zoological names}).
	 *
	 * @see  #VALID()
	 * @see  #CONSERVED()
	 * @see  #SANCTIONED()
	 * @see  #isIllegitimateType()
	 */
	public static final NomenclaturalStatusType SUPERFLUOUS(){
		return getTermByUuid(uuidSuperfluous);
	}

	/**
	 * Returns the nomenclatural status type "rejected". A "valid" ("available")
	 * {@link TaxonName taxon name} is "rejected" if, even though by the strict
	 * application of the rules of the {@link NomenclaturalCode nomenclature code}, and especially
	 * of the principle of priority, it should be "legitimate" ("valid" for
	 * {@link IZoologicalName zoological names}), competent authorities decided to handle
	 * it as "illegitimate".<BR>
	 * A "rejected" taxon name is therefore also "illegitimate" ("invalid" for
	 * zoological names). A "rejected" taxon name is always rejected in favour
	 * of a "conserved" taxon name.
	 *
	 * @see  #VALID()
	 * @see  #isLegitimateType()
	 * @see  #isIllegitimateType()
	 * @see  #CONSERVED()
	 * @see  NameRelationshipType#CONSERVED_AGAINST()
	 */
	public static final NomenclaturalStatusType REJECTED(){
		return getTermByUuid(uuidRejected);
	}

	/**
	 * Returns the nomenclatural status type "utique rejected". A "valid"
	 * ("available") {@link TaxonName taxon name} is "utique rejected" if it is rejected
	 * outright (without being rejected in favour of a "conserved" taxon name).<BR>
	 * An "utique rejected" taxon name is therefore also "illegitimate"
	 * ("invalid" for zoological names).
	 *
	 * @see  #REJECTED()
	 * @see  #VALID()
	 * @see  #isIllegitimateType()
	 * @see  #CONSERVED()
	 */
	public static final NomenclaturalStatusType UTIQUE_REJECTED(){
		return getTermByUuid(uuidUtiqueRejected);
	}

	/**
	 * Returns the nomenclatural status type "proposed to be conserved". A
	 * "valid" ("available") {@link TaxonName taxon name} is "proposed to be conserved"
	 * if, even though by the strict application of the rules of
	 * the {@link NomenclaturalCode nomenclature code}, and especially of the principle of priority,
	 * it is "illegitimate" ("invalid" for {@link IZoologicalName zoological names}),
	 * it has been submitted to competent authorities in order to decide whether
	 * it should be handled as "legitimate".<BR>
	 * A "proposed to be conserved" taxon name is therefore still "illegitimate"
	 * ("invalid" for zoological names).
	 *
	 * {@link https://dev.e-taxonomy.eu/trac/ticket/5662}
	 *
	 * @see  #VALID()
	 * @see  #isIllegitimateType()
	 * @see  #isLegitimateType()
	 * @see  #CONSERVED()
	 * @see  #CONSERVED_DESIG()
	 * @see  NameRelationshipType#CONSERVED_AGAINST()
	 */
	public static final NomenclaturalStatusType CONSERVED_PROP(){
		return getTermByUuid(uuidConservedProp);
	}

    /**
     * Returns the nomenclatural status type "designated to be conserved". A
     * "valid" ("available") {@link TaxonName taxon name} is "designated to be conserved".
     * The name is considered to be legitimate as it has been decided by the General Committee
     * though not yet ratified by the Int. Bot./Zool. Congr.
     *
     * NOTE: This interpretation needs further clarification.
     *
     * {@link https://dev.e-taxonomy.eu/trac/ticket/5662}
     *
     * @see  #VALID()
     * @see  #isIllegitimateType()
     * @see  #isLegitimateType()
     * @see  #CONSERVED()
     * @see  #CONSERVED_PROP()()
     * @see  NameRelationshipType#CONSERVED_AGAINST()
     */
    public static final NomenclaturalStatusType CONSERVED_DESIG(){
        return getTermByUuid(uuidConservedDesig);
    }



	/**
	 * Returns the nomenclatural status type "proposed to be conserved
	 * (orthography)". A {@link TaxonName taxon name} is "proposed to be conserved
	 * (orthography)" if, even though originally published with another
	 * spelling, it has been submitted to competent authorities in order to
	 * decide whether the proposed alternative spelling should be "conserved".<BR>
	 * A "proposed to be conserved (orthography)" taxon name is therefore still
	 * "illegitimate" ("invalid" for {@link IZoologicalName zoological names}).
	 *
	 * @see  #isIllegitimateType()
	 * @see  #CONSERVED_PROP()
	 * @see  #CONSERVED()
	 * @see  NameRelationshipType#ORTHOGRAPHIC_VARIANT()
	 * @see  NameRelationshipType#CONSERVED_AGAINST()
	 */
	public static final NomenclaturalStatusType ORTHOGRAPHY_CONSERVED_PROP(){
		return getTermByUuid(uuidOrthographyConservedProp);
	}

	/**
	 * Returns the nomenclatural status type "legitimate" ("valid" for
	 * {@link IZoologicalName zoological names}). A "valid" ("available")
	 * {@link TaxonName taxon name}, unless "rejected", is "legitimate" if it was not
	 * "superfluous" when published or has been later "conserved".<BR>
	 *
	 * @see  #VALID()
	 * @see  #SUPERFLUOUS()
	 * @see  #CONSERVED()
	 */
	public static final NomenclaturalStatusType LEGITIMATE(){
		return getTermByUuid(uuidLegitimate);
	}

	/**
	 * Returns the nomenclatural status type "alternative". A family
	 * {@link BotanicalName botanical name} is "alternative" if it is a classical name
	 * long in use, in some cases even before 1753, and is considered as
	 * {@link NomenclaturalStatusType#VALID() "valid"} although it does not follow the rules for
	 * family names (see Article 18 of the ICBN).<BR>
	 * An "alternative" taxon name is treated as if "conserved" and is therefore
	 * also "legitimate".
	 *
	 * @see  #VALID()
	 * @see  #CONSERVED()
	 * @see  #isLegitimateType()
	 * @see  NameRelationshipType#ALTERNATIVE_NAME()
	 */
	public static final NomenclaturalStatusType ALTERNATIVE(){
		return getTermByUuid(uuidAlternative);
	}

	/**
	 * Returns the nomenclatural status type "novum". A "valid"
	 * ("available") {@link TaxonName taxon name} is "novum" if it has been created
	 * in order either to replace an earlier name that is "illegitimate" or to
	 * avoid the building of a "later homonym".<BR>
	 * A "novum" taxon name is therefore also "legitimate" ("valid" for
	 * {@link IZoologicalName zoological names}).
	 *
	 * @see  #VALID()
	 * @see  #isIllegitimateType()
	 * @see  NameRelationshipType#REPLACED_SYNONYM()
	 * @see  NameRelationshipType#BLOCKING_NAME_FOR()
	 */
	public static final NomenclaturalStatusType NOVUM(){
		return getTermByUuid(uuidNovum);
	}

	/**
	 * Returns the nomenclatural status type "proposed to be utique rejected". A
	 * "valid" ("available") {@link TaxonName taxon name} is "proposed to be utique rejected"
	 * if, even though by the strict application of the rules of
	 * the {@link NomenclaturalCode nomenclature code}, and especially of the principle of priority,
	 * it is "legitimate" ("valid" for {@link IZoologicalName zoological names}),
	 * it has been submitted to competent authorities in order to decide whether
	 * it should be handled as "illegitimate" (without to be rejected in favour
	 * of a "conserved" taxon name).<BR>
	 * A "proposed to be utique rejected" taxon name is therefore still "legitimate"
	 * ("valid" for zoological names).
	 *
	 * @see  #VALID()
	 * @see  #isLegitimateType()
	 * @see  #isIllegitimateType()
	 * @see  #REJECTED()
	 * @see  #REJECTED_PROP()
	 */
	public static final NomenclaturalStatusType UTIQUE_REJECTED_PROP(){
		return getTermByUuid(uuidUtiqueRejectedProp);
	}

	/**
	 * Returns the nomenclatural status type "conserved (orthography)". A
	 * {@link TaxonName taxon name} is "conserved (orthography)" if competent authorities
	 * decided to conserve a different spelling to the one published originally.<BR>
	 * A "conserved (orthography)" taxon name is "conserved" and hence
	 * "legitimate" ("valid" for {@link IZoologicalName zoological names}).
	 *
	 * @see  #isLegitimateType()
	 * @see  #CONSERVED()
	 * @see  #ORTHOGRAPHY_CONSERVED_PROP()
	 * @see  NameRelationshipType#ORTHOGRAPHIC_VARIANT()
	 * @see  NameRelationshipType#CONSERVED_AGAINST()
	 */
	public static final NomenclaturalStatusType ORTHOGRAPHY_CONSERVED(){
		return getTermByUuid(uuidOrthographyConserved);
	}




    /**
     * Returns the nomenclatural status type "orthography rejected". <BR>
     * TBC.
     * See also {@link http://dev.e-taxonomy.eu/trac/ticket/5649}
     *
     * @see  #ORTHOGRAPHY_CONSERVED()
     * @see  #REJECTED()
     * @see  #isIllegitimateType()
     */
    public static final NomenclaturalStatusType ORTHOGRAPHY_REJECTED(){
        return getTermByUuid(uuidOrthographyRejected);
    }

	/**
	 * Returns the nomenclatural status type "proposed to be rejected". A
	 * "valid" ("available") {@link TaxonName taxon name} is "proposed to be rejected"
	 * if, even though by the strict application of the rules of
	 * the {@link NomenclaturalCode nomenclature code}, and especially of the principle of priority,
	 * it should be "legitimate" ("valid" for {@link IZoologicalName zoological names}),
	 * it has been submitted to competent authorities in order to decide whether
	 * it should be handled as "illegitimate".<BR>
	 * A "proposed to be rejected" taxon name is therefore still "legitimate"
	 * ("valid" for zoological names). A "proposed to be rejected" taxon name is always
	 * to be rejected in favour of a "proposed to be conserved" taxon name.
	 *
	 * @see  #VALID()
	 * @see  #isLegitimateType()
	 * @see  #isIllegitimateType()
	 * @see  #REJECTED()
	 * @see  #CONSERVED_PROP()
	 * @see  NameRelationshipType#CONSERVED_AGAINST()
	 */
	public static final NomenclaturalStatusType REJECTED_PROP(){
		return getTermByUuid(uuidRejectedProp);
	}


	/**
	 * Returns the nomenclatural status type "conserved". A "valid"
	 * ("available") {@link TaxonName taxon name} is "conserved" if, even though by the strict
	 * application of the rules of the {@link NomenclaturalCode nomenclature code}, and especially of
	 * the principle of priority, it should be "illegitimate" ("invalid" for
	 * {@link IZoologicalName zoological names}), competent authorities decided to handle
	 * it as "legitimate".<BR>
	 * A "conserved" taxon name is therefore also "legitimate" ("valid" for
	 * zoological names).
	 *
	 * @see  #VALID()
	 * @see  #isIllegitimateType()
	 * @see  #isLegitimateType()
	 * @see  NameRelationshipType#CONSERVED_AGAINST()
	 */
	public static final NomenclaturalStatusType CONSERVED(){
		return getTermByUuid(uuidConserved);
	}

	/**
	 * Returns the nomenclatural status type "sanctioned". {@link BotanicalName Botanical names}
	 * for fungi are "sanctioned" if they were published in the opera mentioned
	 * in Article 13.1d of the {@link NomenclaturalCode#ICBN() ICBN}.<BR>
	 * A "sanctioned" taxon name is treated as if "conserved" and is therefore
	 * also "legitimate".
	 *
	 * @see  #VALID()
	 * @see  #CONSERVED()
	 * @see  #isLegitimateType()
	 */
	public static final NomenclaturalStatusType SANCTIONED(){
		return getTermByUuid(uuidSanctioned);
	}

	/**
	 * Returns the nomenclatural status type "invalid" (this corresponds to
	 * "not available" for {@link IZoologicalName zoological names}). The use of "invalid"
	 * {@link TaxonName taxon names} should be avoided.<BR>
	 * A taxon name is "invalid" if it is not "valid"; this means that
	 * the taxon name:<ul>
	 * <li>has not been effectively published or
	 * <li>has a form which does not comply with the rules of the
	 * 	   {@link NomenclaturalCode nomenclature code} or
	 * <li>is not accompanied by a description or diagnosis or by a reference to
	 * 	   such a previously published description or diagnosis
	 * </ul>
	 *
	 * @see  #VALID()
	 * @see  #isInvalidType()
	 * @see  #ILLEGITIMATE()
	 */
	public static final NomenclaturalStatusType INVALID(){
		return getTermByUuid(uuidInvalid);
	}

	/**
	 * Returns the nomenclatural status type "nudum". A {@link TaxonName taxon name} is "nudum"
	 * if its publication is not accompanied by a description or diagnosis or
	 * by a reference to such a previously published description or diagnosis.<BR>
	 * A "nudum" taxon name is therefore also "invalid" ("not available" for
	 * {@link IZoologicalName zoological names}).
	 *
	 * @see  #isInvalidType()
	 */
	public static final NomenclaturalStatusType NUDUM(){
		return getTermByUuid(uuidNudum);
	}

	/**
	 * Returns the nomenclatural status type "invalid combination". A
	 * {@link TaxonName bi- or trinomial} is an "invalid combination" if its
	 * {@link NonViralName#getCombinationAuthorship() author} did not definitely associate the final
	 * epithet with the name of the genus or species, or with its abbreviation.<BR>
	 * An "invalid combination" taxon name is therefore also "invalid"
	 * ("not available" for {@link IZoologicalName zoological names}).
	 *
	 * @see  #isInvalidType()
	 */
	public static final NomenclaturalStatusType COMBINATION_INVALID(){
		return getTermByUuid(uuidCombinationInvalid);
	}

	/**
	 * Returns the nomenclatural status type "illegitimate combination".
	 * TODO explanation
	 *
	 * @see  #isInvalidType()
	 */
	public static final NomenclaturalStatusType COMBINATION_ILLEGITIMATE(){
		return getTermByUuid(uuidCombinationIllegitimate);
	}

	/**
	 * Returns the nomenclatural status type "provisional". A {@link TaxonName taxon name} is
	 * "provisional" if it is not validly published, because not finally accepted by the author<BR>
	 * Some people use it in the same way as {@link #INED() ined.}
	 *
	 * @see #INED()
	 * @see  #isInvalidType()
	 */
	public static final NomenclaturalStatusType PROVISIONAL(){
		return getTermByUuid(uuidProvisional);
	}

    /**
     * Returns the nomenclatural status type "ined.". A {@link TaxonName taxon name} is
     * "inedited" if it it has not yet been published.<BR>
     * An inedited taxon name is therefore also "invalid" (bot.) / "not available (zool.)
     *<BR>
     * see also http://dev.e-taxonomy.eu/trac/ticket/5896
     *
     * @see #PROVISIONAL()
     * @see  #isInvalidType()
     * @return
     */
    public static final NomenclaturalStatusType INED(){
        return getTermByUuid(uuidIned);
    }

	/**
	 * Returns the nomenclatural status type "valid" (this corresponds to
	 * "available" for {@link IZoologicalName zoological names}).<BR>
	 * A {@link TaxonName taxon name} is "valid" if it:<ul>
	 * <li>has been effectively published and
	 * <li>has a form which complies with the rules of the
	 * 	   {@link NomenclaturalCode nomenclature code} and
	 * <li>is accompanied by a description or diagnosis or by a reference to
	 * 	   such a previously published description or diagnosis
	 * </ul>
	 *
	 * @see  #INVALID()
	 * @see  #LEGITIMATE()
	 */
	public static final NomenclaturalStatusType VALID(){
		return getTermByUuid(uuidValid);
	}

	/**
	 * Returns the nomenclatural status type "subnudum". This type is not
	 * covered by {@link NomenclaturalCode nomenclature codes}. It appears sometimes in literature and
	 * represents the opinion of the author who considers the {@link TaxonName taxon name} to be
	 * unusable for an unambiguous taxonomic use.
	 *
	 * @see  #AMBIGUOUS()
	 * @see  #CONFUSUM()
	 *
	 */
	public static final NomenclaturalStatusType SUBNUDUM(){
		return getTermByUuid(uuidSubnudum);
	}

	/**
	 * Returns the nomenclatural status type "comb. nov.". No further information available for now.
	 * @return
	 */
	//TODO javadoc. this term was added for Flore du Gabon
	public static final NomenclaturalStatusType COMB_NOV(){
		return getTermByUuid(uuidCombNov);
	}

	/**
	 * Returns the nomenclatural status type "opus utique oppressum". This type
	 * relates to article 32.7 (old ICBN) and article 32.9 as well as App. 6
	 * (new {@link NomenclaturalCode#ICBN() ICBN}). This is a reference list of botanical opera, in which all
	 * {@link BotanicalName taxon names} (or names of a certain rank) are oppressed. Such a name has the
	 * status "invalid" but in contrary to "rejected" not a single name
	 * is rejected by the commission but an opus with regard to the validity of
	 * all taxon names occurring in it.<BR>
	 * An "opus utique oppressum" taxon name is therefore also "invalid"
	 * ("not available" for {@link IZoologicalName zoological names}).
	 *
	 * @see  #isInvalidType()
	 */
	public static final NomenclaturalStatusType OPUS_UTIQUE_OPPR(){
		return getTermByUuid(uuidOpusUtiqueOppr);
	}

	/**
	 * TODO
	 * @return
	 */
	public static final  NomenclaturalStatusType ZOO_NOT_AVAILABLE (){
		return getTermByUuid(uuidZooNotAvailable);
	}

	/**
	 * TODO
	 * @return
	 */
	public static final  NomenclaturalStatusType ZOO_INVALID (){
		return getTermByUuid(uuidZooInvalid);
	}

	/**
	 * TODO
	 * @return
	 */
	public static final  NomenclaturalStatusType ZOO_SUPPRESSED (){
		return getTermByUuid(uuidZooSuppressed);
	}

	public static final  NomenclaturalStatusType ZOO_OBLITUM (){
        return getTermByUuid(uuidZooOblitum);
    }



	//TODO further Zoological status


	//TODO Soraya
	//	orth. var.: orthographic variant
	//	pro syn.: pro synonymo

	// TODO
	// Preliminary implementation for BotanicalNameParser.
	// not yet complete
	/**
	 * Returns the nomenclatural status type identified through its label
	 * abbreviation. Preliminary implementation for BotanicalNameParser.
	 *
	 * @param statusAbbreviation	the label abbreviation
	 * @param name                  the taxon name
	 * @return  					the nomenclatural status type
	 *
	 */
	public static NomenclaturalStatusType getNomenclaturalStatusTypeByAbbreviation(String statusAbbreviation, ITaxonNameBase name) throws UnknownCdmTypeException{
		if (statusAbbreviation == null){
			throw new NullPointerException("Abbreviation is NULL in getNomenclaturalStatusTypeByAbbreviation");
		}
		NomenclaturalStatusType result = null;
		statusAbbreviation = normalizeStatusAbbrev(statusAbbreviation);

		//TODO handle undefined names correctly
		boolean isZooname = name.getNameType().equals(NomenclaturalCode.ICZN);

		Map<String, UUID> map = isZooname ? zooAbbrevMap : abbrevMap;
		if (map == null ){
			return null;
		}
		//non unique abbrev
		if (! isZooname && statusAbbreviation.equalsIgnoreCase("nom. alternativ.")){
			return NomenclaturalStatusType.ALTERNATIVE();
		}
		UUID uuid = map.get(statusAbbreviation);
		if (uuid != null ){
			result = getTermByUuid(uuid);
		}
		if (result != null){
			return result;
		}else {
			throw new UnknownCdmTypeException("Unknown nom. status abbreviation: " + statusAbbreviation);
		}
	}

	/**
     * @param statusAbbreviation
     * @return
     */
    private static String normalizeStatusAbbrev(String statusAbbreviation) {
        //#7109 should not happen anymore
        if (statusAbbreviation.equalsIgnoreCase("nom. valid")){
            statusAbbreviation = "nom. val.";
        }
        return statusAbbreviation;
    }

    /**
	 * Returns the nomenclatural status type identified through its label.
	 *
	 * @param	statusLabel	the nomenclatural status label
	 * @return  the nomenclatural status type
	 *
	 */
	public static NomenclaturalStatusType getNomenclaturalStatusTypeByLabel(String statusLabel) throws UnknownCdmTypeException{
		if (statusLabel == null){
			throw new NullPointerException("Status label is NULL in getNomenclaturalStatusTypeBylabel");
		}
		NomenclaturalStatusType result = null;
		if (labelMap == null){
			return null;
		}
		statusLabel = statusLabel.toLowerCase();
		UUID uuid = labelMap.get(statusLabel);
		if (uuid != null ){
			result = getTermByUuid(uuid);
		}
		if (result != null){
			return result;
		}else {
			if (statusLabel == null){
				statusLabel = "(null)";
			}
			throw new UnknownCdmTypeException("Unknown nom. status label: " + statusLabel);
		}
	}


	/**
	 * Fills <i>this</i> nomenclatural status type with contents (uuid, uri,
	 * description text, label and label abbreviation) coming from a csv line.
	 * The implicit language for the description text is "latin".
	 * This method overrides the method of {@link eu.etaxonomy.cdm.model.common.DefinedTermBase DefinedTermBase}.
	 *
	 * @param	csvLine 	the (ordered) list of substrings from a csv string
	 * 						to be used to fill <i>this</i> nomenclatural status type
	 * @see					#NomenclaturalStatusType(String, String, String)
	 * @see					#readCsvLine(List, Language)
	 * @see					eu.etaxonomy.cdm.model.common.DefinedTermBase#readCsvLine(List)
	 */

	@Override
    public NomenclaturalStatusType readCsvLine(Class<NomenclaturalStatusType> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms, boolean abbrevAsId) {   //TODO should be List<String> but makes error for some strange reason
		try {
			NomenclaturalStatusType newInstance = termClass.newInstance();
			DefinedTermBase.readCsvLine(newInstance, csvLine, Language.LATIN(), abbrevAsId);
			return newInstance;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<NomenclaturalStatusType> termVocabulary) {
		if (termVocabulary.getUuid().equals(uuidIcnafpNomStatusVocabulary)){
			termMap = new HashMap<UUID, NomenclaturalStatusType>();
			abbrevMap = new HashMap<String, UUID>();
			labelMap = new HashMap<String, UUID>();
			for (NomenclaturalStatusType term : termVocabulary.getTerms()){
				termMap.put(term.getUuid(), term);
				addStatusType(term, abbrevMap, labelMap);
			}
		}else if (termVocabulary.getUuid().equals(uuidIcznNomStatusVocabulary)){
			zooTermMap = new HashMap<UUID, NomenclaturalStatusType>();
			zooAbbrevMap = new HashMap<String, UUID>();
			zooLabelMap = new HashMap<String, UUID>();
			for (NomenclaturalStatusType term : termVocabulary.getTerms()){
				zooTermMap.put(term.getUuid(), term);
				addStatusType(term, zooAbbrevMap, zooLabelMap);
			}
		}else{
			throw new IllegalArgumentException("Unknown Nom.Status Vocabulary");
		}
	}

	public static void initDefaultTerms() {
        TermVocabulary<NomenclaturalStatusType> vocabulary = getTermByUuid(uuidDoubtful).getVocabulary();

        (new NomenclaturalStatusType()).setDefaultTerms(vocabulary);
    }

	/**
	 * Adds the status type to the (abbreviated) label maps
	 * @param term
	 */
	private void addStatusType(NomenclaturalStatusType statusType, Map<String, UUID> abbrevMap, Map<String, UUID> labelMap ) {
		if (statusType == null){
			logger.warn("statusType is NULL");
			return;
		}
		List<Language> list = new ArrayList<Language>();
		list.add(Language.LATIN());
		list.add(Language.ENGLISH());
		list.add(Language.DEFAULT());

		Representation representation = statusType.getPreferredRepresentation(list);
		if (representation != null){

			String abbrevLabel = representation.getAbbreviatedLabel();
			String label = representation.getLabel();
			if (abbrevLabel == null){
				logger.warn("label is NULL");
				return;
			}

			//add to map
			abbrevMap.put(abbrevLabel, statusType.getUuid());
			labelMap.put(label.toLowerCase(), statusType.getUuid());
		}

	}



	/**
	 * NomenclaturalStatusType should always be shown in latin, therefore the only existing representation
	 * is the latin one. In case we pass in another Language to this method it will return a <code>null</code> representation.
	 *
	 * In case the representation becomes null, we fall back to the latin representation.
	 *
	 */
	@Override
	public Representation getRepresentation(Language lang) {
		Representation representation = super.getRepresentation(lang);

		if(representation == null){
			representation = super.getRepresentation(Language.LATIN());
		}

		return representation;
	}


}
