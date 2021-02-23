/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.OrderedTermBase;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;


/**
 * This class represents terms describing the {@link AbsenceTerm absence}
 * (like "extinct") or the {@link PresenceTerm presence} (like "cultivated") of a
 * {@link Taxon taxon} in an {@link NamedArea area}. These terms are only
 * used for {@link Distribution distributions}.
 *
 * The current implementation includes multiple qualities of distribution status:<BR><BR>
 *
 *  1. Degree of "presence": present, presence questionable, reported in error, absent, ...<BR>
 *  2. Nativeness/naturalization/...: native, alien (introduced), naturalized, cultivated, invasive, ... <BR>
 *  <BR>
 *  There might be further qualities like adventive(unwanted)<->introduced(wanted - for cultivation)
 * <BR>
 * For an interesting discussion of these status see Pyšek & al.: Alien plants in checklists and floras;
 * Taxon 53(1), Feb. 2004: 131-143 <BR>
 *
 * Pyšek et al. distinguises<BR>
 *  native - alien<BR>
 *  alien => cultivated-outside cultivation<BR>
 *  outside cultivation=>casual - naturalized<BR>
 *  naturalized => non invasive - invasive<BR>
 *  invasive => not harmful - transformers - weeds
 *
 * @see https://dev.e-taxonomy.eu/redmine/issues/6000
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:44
 * @author a.mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PresenceAbsenceTerm")
@XmlRootElement(name = "PresenceAbsenceTerm")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.term.DefinedTermBase")
@Audited
public class PresenceAbsenceTerm extends OrderedTermBase<PresenceAbsenceTerm> {
	private static final long serialVersionUID = 1036807546935584396L;
	private static final Logger logger = Logger.getLogger(PresenceAbsenceTerm.class);


	//presence base
	public static final UUID uuidPresent = UUID.fromString("cef81d25-501c-48d8-bbea-542ec50de2c2");
	private static final UUID uuidPresentDoubfully = UUID.fromString("75a60279-a4c2-4f53-bc57-466028a4b3db");

	//presence
	public static final UUID uuidNative = UUID.fromString("ddeac4f2-d8fa-43b8-ad7e-ca13abdd32c7");
	private static final UUID uuidNativeDoubtfullyNative = UUID.fromString("310373bf-7df4-4d02-8cb3-bcc7448805fc");
	public static final UUID uuidCultivated = UUID.fromString("9eb99fe6-59e2-4445-8e6a-478365bd0fa9");
	public static final UUID uuidIntroduced = UUID.fromString("643cf9d1-a5f1-4622-9837-82ef961e880b");
	private static final UUID uuidIntroducedDoubtfullyIntroduced = UUID.fromString("0c54761e-4887-4788-9dfa-7190c88746e3");
	public static final UUID uuidIntroducedUncertainDegreeNaturalisation = UUID.fromString("da159544-b0dd-4599-a9c9-640826af8c17");
	public static final UUID uuidIntroducedAdventitious = UUID.fromString("42946bd6-9c22-45ad-a910-7427e8f60bfd");
	public static final UUID uuidNaturalised = UUID.fromString("e191e89a-a751-4b0c-b883-7f1de70915c9");
	private static final UUID uuidIntroducedCultiated = UUID.fromString("fac8c347-8262-44a1-b0a4-db4de451c021");
	private static final UUID uuidEndemic = UUID.fromString("c3ee7048-15b7-4be1-b687-9ce9c1a669d6");

	private static final UUID uuidNotEndemic = UUID.fromString("2fda5393-7423-4076-814c-1fa7678d7d33");
	private static final UUID uuidUnknownEndemism = UUID.fromString("094aa2e4-8048-4086-aca1-2d671a05a86e");

	//	private static final UUID uuidNa=UUID.fromString("4e04990a-66fe-4fdf-856c-f40772fbcf0a");
	//invasive
	private static final UUID uuidInvasive = UUID.fromString("dc536e3d-a753-4bbe-a386-dd8aff35c234");
	private static final UUID uuidNonInvasive = UUID.fromString("1b025e8b-901a-42e8-9739-119b410c6f03");

	//presents questionable
	public static final UUID uuidNativePresenceQuestionable = UUID.fromString("925662c1-bb10-459a-8c53-da5a738ac770");
	private static final UUID uuidCultivatedPresenceQuestionable = UUID.fromString("4f31bfc8-3058-4d83-aea5-3a1fe9773f9f");
	private static final UUID uuidIntroducedPresenceQuestionable = UUID.fromString("83eb0aa0-1a45-495a-a3ca-bf6958b74366");
	private static final UUID uuidEndedmicPresenceQuestionable = UUID.fromString("5f954f08-267a-4928-b073-12328f74c187");
	//intr. naturalized questionable
	private static final UUID uuidNaturalisedPresenceQuestionable = UUID.fromString("9e0b413b-5a68-4e5b-91f2-227b4f832466");
	//natur. invasive questionable
	private static final UUID uuidInvasivePresenceQuestionable = UUID.fromString("ac429d5f-e8ad-49ae-a41c-e4779b58b96a");
	//natur. non-invasive questionable
    private static final UUID uuidNonInvasivePresenceQuestionable = UUID.fromString("11f56e2f-c16c-4b3d-a870-bb5d3b20e624");

	//absence
	private static final UUID uuidAbsence=UUID.fromString("59709861-f7d9-41f9-bb21-92559cedd598");
	private static final UUID uuidReportedInError = UUID.fromString("38604788-cf05-4607-b155-86db456f7680");

	public static final UUID uuidNativeError = UUID.fromString("61cee840-801e-41d8-bead-015ad866c2f1");
	private static final UUID uuidIntroducedReportedError = UUID.fromString("aeec2947-2700-4623-8e32-9e3a430569d1");
	private static final UUID uuidCultivatedReportedError = UUID.fromString("9d4d3431-177a-4abe-8e4b-1558573169d6");
	private static final UUID uuidNativeFormerlyNative = UUID.fromString("5c397f7b-59ef-4c11-a33c-45691ceda91b");
	private static final UUID uuidNativeDoubtfullyNativeReportedError = UUID.fromString("71b72e24-c2b6-44a5-bdab-39f083bf0f06");
	private static final UUID uuidIntroducedFormerlyIntroduced = UUID.fromString("b74dc30b-ee93-496d-8c00-4d00abae1ec7");
	private static final UUID uuidEndemicReportedError = UUID.fromString("679b215d-c231-4ee2-ae12-3ffc3dd528ad");
	private static final UUID uuidNaturalisedReportedError = UUID.fromString("8d918a37-3add-4e1c-a233-c37dbee209aa");
	private static final UUID uuidCasualPresenceQuestionable = UUID.fromString("73f75493-1185-4a3e-af1e-9a1f2e8dadb7");
	private static final UUID uuidCasualReportedError = UUID.fromString("9b910b7b-43e3-4260-961c-6063b11cb7dc");


	protected static Map<UUID, PresenceAbsenceTerm> termMap = null;

    private String defaultColor = "000000";

    @XmlAttribute(name = "absenceTerm")
    private boolean absenceTerm = false;

	//********* METHODS **************************************/
	/**
	 * Creates a new empty presence term.
	 *
	 * @see #NewInstance(String, String, String)
	 */
	public static PresenceAbsenceTerm NewInstance(){
		return new PresenceAbsenceTerm();
	}

	/**
	 * Creates a new presence term with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 *
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new presence term to be created
	 * @param	label  		 the string identifying the new presence term to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new presence term to be created
	 * @see 				 #NewInstance()
	 */
	public static PresenceAbsenceTerm NewPresenceInstance(String term, String label, String labelAbbrev){
		return new PresenceAbsenceTerm(term, label, labelAbbrev, false);
	}

	/**
	 * Creates a new absence term with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 *
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new presence term to be created
	 * @param	label  		 the string identifying the new presence term to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new presence term to be created
	 * @see 				 #NewInstance()
	 */
	public static PresenceAbsenceTerm NewAbsenceInstance(String term, String label, String labelAbbrev){
		return new PresenceAbsenceTerm(term, label, labelAbbrev, true);
	}


//********************************** Constructor *******************************************************************/

  	//for hibernate use only
  	@Deprecated
  	protected PresenceAbsenceTerm() {
    	super(TermType.PresenceAbsenceTerm);
    }

    /**
     * Class constructor: creates a new presence or absence term with a description
     * (in the {@link Language#DEFAULT() default language}), a label and a label abbreviation.
     *
     * @param	term  		 the string (in the default language) describing the
     * 						 new presence or absence term to be created
     * @param	label  		 the string identifying the new presence or absence term to be created
     * @param	labelAbbrev  the string identifying (in abbreviated form) the
     * 						 new presence or absence term to be created
     * @param   isAbsenceTerm  boolean value indicating if this term represents an absence or not
     * @see 				 #PresenceAbsenceTermBase()
     */
    protected PresenceAbsenceTerm(String term, String label, String labelAbbrev, boolean isAbsenceTerm) {
        super(TermType.PresenceAbsenceTerm, term, label, labelAbbrev);
        this.setAbsenceTerm(isAbsenceTerm);
    }


  //******************************* STATIC METHODS *****************************************

  	protected static PresenceAbsenceTerm getTermByUuid(UUID uuid){
        if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(PresenceAbsenceTerm.class, uuid);
        } else {
  			return termMap.get(uuid);
  		}
  	}

   	//TODO read from label(abbrevs) like in TDWGArea
  	public static PresenceAbsenceTerm getPresenceAbsenceTermByAbbreviation(String abbrev) {
  		if (abbrev == null) { throw new NullPointerException("abbrev is 'null' in getPresenceTermByAbbreviation");
  		} else if (abbrev.equalsIgnoreCase("c"))  { return PresenceAbsenceTerm.CULTIVATED();
  		} else if (abbrev.equalsIgnoreCase("e"))  { return PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA();
  		} else if (abbrev.equalsIgnoreCase("i"))  { return PresenceAbsenceTerm.INTRODUCED();
  		} else if (abbrev.equalsIgnoreCase("ia")) { return PresenceAbsenceTerm.CASUAL();
  		} else if (abbrev.equalsIgnoreCase("ic")) { return PresenceAbsenceTerm.INTRODUCED_CULTIVATED();
  		} else if (abbrev.equalsIgnoreCase("id")) { return PresenceAbsenceTerm.INTRODUCED_DOUBTFULLY_INTRODUCED();
  		} else if (abbrev.equalsIgnoreCase("in")) { return PresenceAbsenceTerm.NATURALISED();
  		} else if (abbrev.equalsIgnoreCase("ip")) { return PresenceAbsenceTerm.INTRODUCED_UNCERTAIN_DEGREE_OF_NATURALISATION();
  		} else if (abbrev.equalsIgnoreCase("iq")) { return PresenceAbsenceTerm.INTRODUCED_PRESENCE_QUESTIONABLE();
  		} else if (abbrev.equalsIgnoreCase("n"))  { return PresenceAbsenceTerm.NATIVE();
  		} else if (abbrev.equalsIgnoreCase("nd")) { return PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE();
  		} else if (abbrev.equalsIgnoreCase("nq")) { return PresenceAbsenceTerm.NATIVE_PRESENCE_QUESTIONABLE();
  		} else if (abbrev.equalsIgnoreCase("p"))  { return PresenceAbsenceTerm.PRESENT();
  		} else if (abbrev.equalsIgnoreCase("na"))  { return PresenceAbsenceTerm.NATURALISED();
  		} else if (abbrev.equalsIgnoreCase("iv"))  { return PresenceAbsenceTerm.INVASIVE();
  		//absence
		} else if (abbrev.equalsIgnoreCase("cf")) { return PresenceAbsenceTerm.CULTIVATED_REPORTED_IN_ERROR();
		} else if (abbrev.equalsIgnoreCase("if")) { return PresenceAbsenceTerm.INTRODUCED_REPORTED_IN_ERROR();
		} else if (abbrev.equalsIgnoreCase("nf")) { return PresenceAbsenceTerm.NATIVE_REPORTED_IN_ERROR();
		} else if (abbrev.equalsIgnoreCase("ne")) { return PresenceAbsenceTerm.NATIVE_FORMERLY_NATIVE();
		} else if (abbrev.equalsIgnoreCase("ie")) { return PresenceAbsenceTerm.INTRODUCED_FORMERLY_INTRODUCED();
		} else {
			logger.warn("Unknown presence or absence status term abbreviation: " + abbrev);
			return null;
		}
  	}

    /**
     * The taxon is endemic for the given area.
     * @see #PRESENT()
     * @see #NATIVE()
     * @see #ENDEMIC_DOUBTFULLY_PRESENT()
     * @see #ENDEMIC_REPORTED_IN_ERROR()
     */
    public static final PresenceAbsenceTerm ENDEMIC_FOR_THE_RELEVANT_AREA(){
        return getTermByUuid(uuidEndemic);
    }

    /**
     * The taxon is present but not endemic for the given area.
     * @see #PRESENT()
     * @see #NATIVE()
     * @see #ENDEMIC_FOR_THE_RELEVANT_AREA()
     * @see #ENDEMIC_DOUBTFULLY_PRESENT()
     * @see #ENDEMIC_REPORTED_IN_ERROR()
     */
    public static final PresenceAbsenceTerm NOT_ENDEMIC_FOR_THE_RELEVANT_AREA(){
        return getTermByUuid(uuidNotEndemic);
    }

    /**
     * The taxon is present but with unknown endemism for the given area.
     * @see #PRESENT()
     * @see #NATIVE()
     * @see #ENDEMIC_FOR_THE_RELEVANT_AREA()
     * @see #ENDEMIC_DOUBTFULLY_PRESENT()
     * @see #ENDEMIC_REPORTED_IN_ERROR()
     * @see #NOT_ENDEMIC_FOR_THE_RELEVANT_AREA()
     */
    public static final PresenceAbsenceTerm ENDEMISM_UNKNOWN(){
        return getTermByUuid(uuidUnknownEndemism);
    }

    /**
     * The taxon is endemic for the given area but doubtfully present.
     * @see #ENDEMIC_FOR_THE_RELEVANT_AREA()
     * @see #ENDEMIC_REPORTED_IN_ERROR()
     * @see #PRESENT_DOUBTFULLY()
     * @see #NATIVE_PRESENCE_QUESTIONABLE()
     */
    public static final PresenceAbsenceTerm ENDEMIC_DOUBTFULLY_PRESENT(){
        return getTermByUuid(uuidEndedmicPresenceQuestionable);
    }

    /**
     * The taxon if it exists is endemic for the given area but was only
     * erroneously reported.
     * So it either does not exist or it is unclear where it was originally found.
     * @see #ENDEMIC_FOR_THE_RELEVANT_AREA()
     * @see #ENDEMIC_DOUBTFULLY_PRESENT()
     * @see #REPORTED_IN_ERROR()
     * @see #NATIVE_REPORTED_IN_ERROR()
     */
    public static final PresenceAbsenceTerm ENDEMIC_REPORTED_IN_ERROR(){
        return getTermByUuid(uuidEndemicReportedError);
    }


	/**
	 * The taxon is present in the given area. No information given about
	 * nativeness/establishment means.
	 *
	 * @see #PRESENT_DOUBTFULLY()
	 * @see #ABSENT()
	 */
	public static final PresenceAbsenceTerm PRESENT(){
		return getTermByUuid(uuidPresent);
	}

	/**
     * Presence of the taxon in the given area is doubtful. No information given about
     * nativeness/establishment means.
     *
     * @see #PRESENT()
     * @see #ABSENT()
     */
	public static final PresenceAbsenceTerm PRESENT_DOUBTFULLY(){
		return getTermByUuid(uuidPresentDoubfully);
	}

	/**
	 * The taxon is not native but cultivated.
	 *
	 * @see #CULTIVATED_PRESENCE_QUESTIONABLE()
	 * @see #CULTIVATED_REPORTED_IN_ERROR()
	 */
	public static final PresenceAbsenceTerm CULTIVATED(){
		return getTermByUuid(uuidCultivated);
	}

	/**
     * The taxon is cultivated but presence is questionable.
     *
     * @see #CULTIVATED()
     * @see #CULTIVATED_REPORTED_IN_ERROR()
     */
    public static final PresenceAbsenceTerm CULTIVATED_PRESENCE_QUESTIONABLE(){
        return getTermByUuid(uuidCultivatedPresenceQuestionable);
    }

    /**
     * The taxon is erroneously reported as cultivated.
     *
     * @see #CULTIVATED()
     * @see #CULTIVATED_REPORTED_IN_ERROR()
     */
    public static final PresenceAbsenceTerm CULTIVATED_REPORTED_IN_ERROR(){
        return getTermByUuid(uuidCultivatedReportedError);
    }


	public static final PresenceAbsenceTerm INTRODUCED(){
		return getTermByUuid(uuidIntroduced);
	}

    /**
     * Casual alien or introduced: adventive (casual)
     *
     * @see #CASUAL_REPORTED_IN_ERROR()
     * @see #CASUAL_PRESENCE_QUESTIONABLE()
     * @see #INTRODUCED_ADVENTITIOUS()
     */
    public static final PresenceAbsenceTerm CASUAL(){
        return getTermByUuid(uuidIntroducedAdventitious);
    }
    /**
     * Casual alien, presence questionable
     *
     * @see #CASUAL()
     * @see #CASUAL_REPORTED_IN_ERROR()
     * @see #PRESENT_DOUBTFULLY()
     */
    public static final PresenceAbsenceTerm CASUAL_PRESENCE_QUESTIONABLE(){
        return getTermByUuid(uuidCasualPresenceQuestionable);
    }
    /**
     * Casual alien, reported in error
     *
     * @see #CASUAL()
     * @see #CASUAL_PRESENCE_QUESTIONABLE()
     * @see #REPORTED_IN_ERROR()
     */
    public static final PresenceAbsenceTerm CASUAL_REPORTED_IN_ERROR(){
        return getTermByUuid(uuidCasualReportedError);
    }


	/**
	 * This term is of questional value. Introduced often is handled as opposite
	 * of adventitious/adventive and describes how the alien species came
	 * - on purpose for cultivation (introduced) or not (adventive).
	 * Adventitious or better adventive is often a synonym for casual which might
	 * be the better term here.
	 *
	 * @deprecated better use {@link #CASUAL()}, which has the same result (same semantics, but more precise)
	 */
	@Deprecated
	public static final PresenceAbsenceTerm INTRODUCED_ADVENTITIOUS(){
		return getTermByUuid(uuidIntroducedAdventitious);
	}

	public static final PresenceAbsenceTerm INTRODUCED_CULTIVATED(){
		return getTermByUuid(uuidIntroducedCultiated);
	}

	public static final PresenceAbsenceTerm INTRODUCED_DOUBTFULLY_INTRODUCED(){
		return getTermByUuid(uuidIntroducedDoubtfullyIntroduced);
	}


    public static final PresenceAbsenceTerm INTRODUCED_UNCERTAIN_DEGREE_OF_NATURALISATION(){
        return getTermByUuid(uuidIntroducedUncertainDegreeNaturalisation);
    }

	public static final PresenceAbsenceTerm INTRODUCED_PRESENCE_QUESTIONABLE(){
		return getTermByUuid(uuidIntroducedPresenceQuestionable);
	}

    public static final PresenceAbsenceTerm INTRODUCED_FORMERLY_INTRODUCED(){
        return getTermByUuid(uuidIntroducedFormerlyIntroduced);
    }

    public static final PresenceAbsenceTerm INTRODUCED_REPORTED_IN_ERROR(){
        return getTermByUuid(uuidIntroducedReportedError);
    }

	/**
	 * Use native if the taxon is native in the according area. Native and indigenous
	 * have the same meaning. Native might be problematic because the abbreviation
	 * N is already used for naturalised.
	 *
	 * @see #NATIVE_DOUBTFULLY_NATIVE()
     * @see #NATIVE_PRESENCE_QUESTIONABLE()
     * @see #NATIVE_REPORTED_IN_ERROR()
	 */
	public static final PresenceAbsenceTerm NATIVE(){
		return getTermByUuid(uuidNative);
	}

    /**
     * Same as {@link #NATIVE()} but presence is questionable
     * @see #NATIVE()
     * @see #NATIVE_DOUBTFULLY_NATIVE()
     * @see #NATIVE_REPORTED_IN_ERROR()
     */
    public static final PresenceAbsenceTerm NATIVE_PRESENCE_QUESTIONABLE(){
        return getTermByUuid(uuidNativePresenceQuestionable);
    }

    /**
     * The taxon was formerly native, but is extinct now in the given area.
     *
     * @see #NATIVE()
     * @see #ABSENT()
     */
    public static final PresenceAbsenceTerm NATIVE_FORMERLY_NATIVE(){
        return getTermByUuid(uuidNativeFormerlyNative);
    }

    /**
     * Same as {@link #NATIVE()} but presence was reported in error, so
     * finally it is not native.
     * @see #NATIVE()
     * @see #NATIVE_PRESENCE_QUESTIONABLE()
     * @see #NATIVE_DOUBTFULLY_NATIVE()
     */
    public static final PresenceAbsenceTerm NATIVE_REPORTED_IN_ERROR(){
        return getTermByUuid(uuidNativeError );
    }

	/**
	 * Same as {@link #NATIVE()} but the nativeness is doubtful, while presence
	 * is NOT questionable
	 * @see #NATIVE()
	 * @see #NATIVE_PRESENCE_QUESTIONABLE()
	 * @see #NATIVE_REPORTED_IN_ERROR()
	 */
	public static final PresenceAbsenceTerm NATIVE_DOUBTFULLY_NATIVE(){
		return getTermByUuid(uuidNativeDoubtfullyNative);
	}

    /**
     * Same as {@link #NATIVE()} but the nativeness is doubtful, while presence
     * is NOT questionable
     * @see #NATIVE_DOUBTFULLY_NATIVE()
     * @see #NATIVE()
     * @see #NATIVE_PRESENCE_QUESTIONABLE()
     */
    public static final PresenceAbsenceTerm NATIVE_DOUBTFULLY_NATIVE_REPORTED_IN_ERROR(){
        return getTermByUuid(uuidNativeDoubtfullyNativeReportedError);
    }


    /**
     * Naturalized (or introduced: naturalized). Further distinction can be made by
     * distinguishing invasive / non-invasive
     * @see #NATURALISED_PRESENCE_QUESTIONABLE()
     * @see #NATURALISED_REPORTED_IN_ERROR()
     * @see #INVASIVE()
     * @see #NON_INVASIVE
     */
    public static final PresenceAbsenceTerm NATURALISED(){
        return getTermByUuid(uuidNaturalised);
    }

    /**
    *
    * @see #NATURALISED()
    */
   public static final PresenceAbsenceTerm NATURALISED_PRESENCE_QUESTIONABLE(){
       return getTermByUuid(uuidNaturalisedPresenceQuestionable);
   }

    public static final PresenceAbsenceTerm NATURALISED_REPORTED_IN_ERROR(){
        return getTermByUuid(uuidNaturalisedReportedError);
    }

	/**
	 * Naturalized invasive. <BR>
	 * The taxon is present but not native in the given area. Additionally
	 * it is spreading fast.
	 *
	 * @see #INVASIVE_PRESENCE_QUESTIONABLE()
	 * @see #NON_INVASIVE()
	 * @see #NON_INVASIVE_PRESENCE_QUESTIONABLE()
	 */
	public static final PresenceAbsenceTerm INVASIVE(){
		return getTermByUuid(uuidInvasive);
	}
    /**
     * Invasive, presence questionable.
     *
     * @see #INVASIVE()
     * @see #NON_INVASIVE()
     * @see #NON_INVASIVE_PRESENCE_QUESTIONABLE()
     */
    public static final PresenceAbsenceTerm INVASIVE_PRESENCE_QUESTIONABLE(){
        return getTermByUuid(uuidInvasivePresenceQuestionable);
    }
    /**
     * The taxon is present but not native in the given area (naturalized).
     * Additionally it is spreading not so fast.
     *
     * @see #INVASIVE()
     * @see #INVASIVE_PRESENCE_QUESTIONABLE()
     * @see #NON_INVASIVE_PRESENCE_QUESTIONABLE()
     */
    public static final PresenceAbsenceTerm NON_INVASIVE(){
        return getTermByUuid(uuidNonInvasive);
    }
    /**
     * The taxon is questionable present and not native in the given area (naturalized).
     * Additionally it is spreading not so fast
     *
     * @see #NON_INVASIVE()
     * @see #INVASIVE()
     * @see #INVASIVE_PRESENCE_QUESTIONABLE()
     */
    public static final PresenceAbsenceTerm NON_INVASIVE_PRESENCE_QUESTIONABLE(){
        return getTermByUuid(uuidNonInvasivePresenceQuestionable);
    }


	/**
	 * @see #PRESENT()
	 * @see #REPORTED_IN_ERROR()
	 */
	public static final PresenceAbsenceTerm ABSENT(){
		return getTermByUuid(uuidAbsence);
	}

	/**
	 * The taxon is {@link #ABSENT() absent} in the given area
	 * but was erroneously reported as present.
	 *
	 * @see #ABSENT()
	 * @see #NATIVE_REPORTED_IN_ERROR()
	 * @see #INTRODUCED_REPORTED_IN_ERROR()
	 */
	public static final PresenceAbsenceTerm REPORTED_IN_ERROR(){
        return getTermByUuid(uuidReportedInError);
    }


//******************************** METHODS ****************************/

	@Override
	public void resetTerms(){
		termMap = null;
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<PresenceAbsenceTerm> termVocabulary) {
		termMap = new HashMap<>();
		for (PresenceAbsenceTerm term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

    @Override
    public PresenceAbsenceTerm readCsvLine(Class<PresenceAbsenceTerm> termClass, List<String> csvLine, TermType termType,
            Map<UUID,DefinedTermBase> terms, boolean abbrevAsId){
    	PresenceAbsenceTerm newInstance = super.readCsvLine(termClass, csvLine, termType, terms, abbrevAsId);
        String abbreviatedLabel = csvLine.get(4);
//		String uuid = (String)csvLine.get(0);
//		map.put(abbreviatedLabel, UUID.fromString(uuid));
        String symbol = csvLine.get(5);
        newInstance.setSymbol(symbol);
        String color = csvLine.get(6);
        try {
			newInstance.setDefaultColor(color);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
        boolean isAbsence = csvLine.get(7).equals("1") ? true : false;
        newInstance.setAbsenceTerm(isAbsence);

        newInstance.getRepresentation(Language.DEFAULT()).setAbbreviatedLabel(abbreviatedLabel);
        return newInstance;
    }

    /**
     * @return the defaultColor
     */
    public String getDefaultColor() {
        return defaultColor;
    }

    /**
     * @param defaultColor the defaultColor to set
     * @throws ParseException
     */
    //TODO make format checking a hibernate validation rule
    public void setDefaultColor(String defaultColor) throws ParseException  {
    	String regEx = "[0-9a-fA-F]{6}";
    	if (StringUtils.isEmpty(defaultColor)){
    		this.defaultColor = null;
    	}else if (StringUtils.isEmpty(defaultColor) || defaultColor.matches(regEx)){
        		this.defaultColor = defaultColor;
    	}else{
    		throw new java.text.ParseException("Default color is not of correct format. Required is 'FFFFFF'", -1);
    	}
    }

    /**
     * Compares this OrderedTermBase with the specified OrderedTermBase for
     * order. Returns a -1, 0, or +1 if the orderId of this object is greater
     * than, equal to, or less than the specified object.
     * <p>
     * <b>Note:</b> The compare logic of this method is the <b>inverse logic</b>
     * of the the one implemented in
     * {@link java.lang.Comparable#compareTo(java.lang.Object)}
     *
     * @param orderedTerm
     *            the OrderedTermBase to be compared
     * @param skipVocabularyCheck
     *            whether to skip checking if both terms to compare are in the
     *            same vocabulary
     * @throws NullPointerException
     *             if the specified object is null
     */
    @Override
    protected int performCompareTo(PresenceAbsenceTerm presenceAbsenceTerm, boolean skipVocabularyCheck) {

    	PresenceAbsenceTerm presenceAbsenceTermLocal = CdmBase.deproxy(presenceAbsenceTerm, PresenceAbsenceTerm.class);
        if(! skipVocabularyCheck){
            if (this.vocabulary == null || presenceAbsenceTermLocal.vocabulary == null){
                throw new IllegalStateException("An ordered term (" + this.toString() + " or " +
                		presenceAbsenceTermLocal.toString() + ") of class " + this.getClass() + " or " +
                		presenceAbsenceTermLocal.getClass() + " does not belong to a vocabulary and therefore "
                		+ "can not be compared");
            }
            if (presenceAbsenceTermLocal.isAbsenceTerm() != this.isAbsenceTerm() ){
              if (presenceAbsenceTermLocal.isAbsenceTerm()){
            		return 1;
            	}else{
            		return -1;
            	}

            }
        }

        int orderThat;
        int orderThis;
        try {
            orderThat = presenceAbsenceTermLocal.orderIndex;//OLD: this.getVocabulary().getTerms().indexOf(orderedTerm);
            orderThis = orderIndex; //OLD: this.getVocabulary().getTerms().indexOf(this);
        } catch (RuntimeException e) {
            throw e;
        }
        if (orderThis > orderThat){
            return -1;
        }else if (orderThis < orderThat){
            return 1;
        }else {
            return 0;
        }
    }

	public boolean isAbsenceTerm() {
		return absenceTerm;
	}

	public void setAbsenceTerm(boolean isAbsenceTerm) {
		this.absenceTerm = isAbsenceTerm;
	}

	@Transient
	private Set<UUID> isAnyIntroduced;
    public boolean isAnyIntroduced() {
        if (isAnyIntroduced == null){
            isAnyIntroduced = new HashSet<>(Arrays.asList(new UUID[]{
                   uuidCasualPresenceQuestionable, uuidCasualReportedError,
                   uuidCultivated, uuidCultivatedPresenceQuestionable, uuidCultivatedReportedError,
                   uuidIntroduced, uuidIntroducedCultiated, uuidIntroducedDoubtfullyIntroduced,
                   uuidIntroducedFormerlyIntroduced, uuidIntroducedPresenceQuestionable,
                   uuidIntroducedReportedError, uuidIntroducedUncertainDegreeNaturalisation,
                   uuidIntroducedAdventitious,
                   uuidInvasive, uuidInvasivePresenceQuestionable,
                   uuidNaturalised, uuidNaturalisedPresenceQuestionable, uuidNaturalisedReportedError,
                   uuidIntroducedUncertainDegreeNaturalisation,
                   uuidInvasive, uuidInvasivePresenceQuestionable, uuidNonInvasive, uuidNonInvasivePresenceQuestionable
            }));
        }
        return isAnyIntroduced.contains(uuid);
    }
}