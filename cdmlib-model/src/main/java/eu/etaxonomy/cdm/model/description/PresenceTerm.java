/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * This class represents terms describing different types of presence
 * (like "native" or "introduced") of a {@link Taxon taxon} in a {@link NamedArea particular area}.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:44
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PresenceTerm")
@XmlRootElement(name = "PresenceTerm")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class PresenceTerm extends PresenceAbsenceTermBase<PresenceTerm> {
	private static final long serialVersionUID = -2876981902335193596L;
	private static final Logger logger = Logger.getLogger(PresenceTerm.class);
	
	protected static Map<UUID, PresenceTerm> termMap = null;	
//	private static PresenceTerm ENDEMIC_FOR_THE_RELEVANT_AREA;
//	private static PresenceTerm INTRODUCED_REPORTED_IN_ERROR;
//	private static PresenceTerm INTRODUCED_NATURALIZED;
//	private static PresenceTerm INTRODUCED_CULTIVATED;
//	private static PresenceTerm INTRODUCED_ADVENTITIOUS;
//	private static PresenceTerm INTRODUCED_UNCERTAIN_DEGREE_OF_NATURALISATION;
//	private static PresenceTerm INTRODUCED_DOUBTFULLY_INTRODUCED;
//	private static PresenceTerm INTRODUCED_FORMERLY_INTRODUCED;
//	private static PresenceTerm INTRODUCED_PRESENCE_QUESTIONABLE;
//	private static PresenceTerm INTRODUCED;
//	private static PresenceTerm CULTIVATED_REPORTED_IN_ERROR;
//	private static PresenceTerm CULTIVATED;
//	private static PresenceTerm NATIVE_REPORTED_IN_ERROR;
//	private static PresenceTerm NATIVE_DOUBTFULLY_NATIVE;
//	private static PresenceTerm NATIVE_FORMERLY_NATIVE;
//	private static PresenceTerm NATIVE_PRESENCE_QUESTIONABLE;
//	private static PresenceTerm NATIVE;
//	private static PresenceTerm PRESENT;
//	private static PresenceTerm NATURALISED;
//	private static PresenceTerm INVASIVE;

	//********* METHODS **************************************/
	/** 
	 * Creates a new empty presence term.
	 * 
	 * @see #NewInstance(String, String, String)
	 */
	public static PresenceTerm NewInstance(){
		return new PresenceTerm();
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
	public static PresenceTerm NewInstance(String term, String label, String labelAbbrev){
		return new PresenceTerm(term, label, labelAbbrev);
	}
	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty presence term.
	 * 
	 * @see #PresenceTerm(String, String, String)
	 */
	public PresenceTerm() {
	}

	/** 
	 * Class constructor: creates a new presence term with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new presence term to be created 
	 * @param	label  		 the string identifying the new presence term to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new presence term to be created
	 * @see 				 #PresenceTerm()
	 */
	public PresenceTerm(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	private static final UUID uuidP=UUID.fromString("cef81d25-501c-48d8-bbea-542ec50de2c2");
	private static final UUID uuidN=UUID.fromString("ddeac4f2-d8fa-43b8-ad7e-ca13abdd32c7");
	private static final UUID uuidNQ=UUID.fromString("925662c1-bb10-459a-8c53-da5a738ac770");
	private static final UUID uuidNE=UUID.fromString("8ad9e9df-49cd-4b6a-880b-51ec4de4ce32");
	private static final UUID uuidND=UUID.fromString("310373bf-7df4-4d02-8cb3-bcc7448805fc");
	private static final UUID uuidNF=UUID.fromString("4ba212ef-041e-418d-9d43-2ebb191b61d8");
	private static final UUID uuidC=UUID.fromString("9eb99fe6-59e2-4445-8e6a-478365bd0fa9");
	private static final UUID uuidCF=UUID.fromString("b47f1679-0d0c-4ea7-a2e4-80709ea791c6");
	private static final UUID uuidI=UUID.fromString("643cf9d1-a5f1-4622-9837-82ef961e880b");
	private static final UUID uuidIQ=UUID.fromString("83eb0aa0-1a45-495a-a3ca-bf6958b74366");
	private static final UUID uuidIE=UUID.fromString("2522c527-e488-45d4-87df-a5a5ef0fdbbd");
	private static final UUID uuidID=UUID.fromString("0c54761e-4887-4788-9dfa-7190c88746e3");
	private static final UUID uuidIP=UUID.fromString("da159544-b0dd-4599-a9c9-640826af8c17");
	private static final UUID uuidIA=UUID.fromString("42946bd6-9c22-45ad-a910-7427e8f60bfd");
	private static final UUID uuidIN=UUID.fromString("e191e89a-a751-4b0c-b883-7f1de70915c9");
	private static final UUID uuidIF=UUID.fromString("826239f7-45b7-42b5-857c-c1f852cfad6b");
	private static final UUID uuidIC=UUID.fromString("fac8c347-8262-44a1-b0a4-db4de451c021");
	private static final UUID uuidE=UUID.fromString("c3ee7048-15b7-4be1-b687-9ce9c1a669d6");
	private static final UUID uuidNA=UUID.fromString("4e04990a-66fe-4fdf-856c-f40772fbcf0a");
	private static final UUID uuidIV=UUID.fromString("dc536e3d-a753-4bbe-a386-dd8aff35c234");

	
	
//******************************* STATIC METHODS *****************************************
	
	protected static PresenceTerm getTermByUuid(UUID uuid){
		if (termMap == null){
			DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
			vocabularyStore.initialize();
		}
		return (PresenceTerm)termMap.get(uuid);
	}

	
	//TODO Marc: Kannst du die noch ein bischen sch�ner formatieren, wenn du sie dokumentierst? Danke
	public static final PresenceTerm PRESENT(){return getTermByUuid(uuidP); } 
	public static final PresenceTerm NATIVE(){return getTermByUuid(uuidIN); } 
	public static final PresenceTerm NATIVE_PRESENCE_QUESTIONABLE(){return getTermByUuid(uuidNQ); }
	public static final PresenceTerm NATIVE_FORMERLY_NATIVE(){return getTermByUuid(uuidNE); }
	public static final PresenceTerm NATIVE_DOUBTFULLY_NATIVE(){return getTermByUuid(uuidND); }
	public static final PresenceTerm NATIVE_REPORTED_IN_ERROR(){return getTermByUuid(uuidNF); }
	public static final PresenceTerm CULTIVATED(){return getTermByUuid(uuidC); }
	public static final PresenceTerm CULTIVATED_REPORTED_IN_ERROR(){return getTermByUuid(uuidCF); }
	public static final PresenceTerm INTRODUCED(){return getTermByUuid(uuidI); }
	public static final PresenceTerm INTRODUCED_PRESENCE_QUESTIONABLE(){return getTermByUuid(uuidIQ); }
	public static final PresenceTerm INTRODUCED_FORMERLY_INTRODUCED(){return getTermByUuid(uuidIE); }
	public static final PresenceTerm INTRODUCED_DOUBTFULLY_INTRODUCED(){return getTermByUuid(uuidID); } 
	public static final PresenceTerm INTRODUCED_UNCERTAIN_DEGREE_OF_NATURALISATION(){return getTermByUuid(uuidIP); }
	public static final PresenceTerm INTRODUCED_ADVENTITIOUS(){return getTermByUuid(uuidIA); } 
	public static final PresenceTerm INTRODUCED_CULTIVATED(){return getTermByUuid(uuidIC); } 
	public static final PresenceTerm INTRODUCED_NATURALIZED(){return getTermByUuid(uuidIN); }
	public static final PresenceTerm INTRODUCED_REPORTED_IN_ERROR(){return getTermByUuid(uuidIF); } 
	public static final PresenceTerm ENDEMIC_FOR_THE_RELEVANT_AREA(){return getTermByUuid(uuidE); } 
	public static final PresenceTerm INVASIVE(){return getTermByUuid(uuidIV); }
	public static final PresenceTerm NATURALISED(){return getTermByUuid(uuidNA); } 

	public static PresenceTerm getPresenceTermByAbbreviation(String abbrev) { 

		if (abbrev == null) { throw new NullPointerException("abbrev is 'null' in getPresenceTermByAbbreviation");
		} else if (abbrev.equalsIgnoreCase("c"))  { return PresenceTerm.CULTIVATED();
		} else if (abbrev.equalsIgnoreCase("cf")) { return PresenceTerm.CULTIVATED_REPORTED_IN_ERROR();
		} else if (abbrev.equalsIgnoreCase("e"))  { return PresenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA();
		} else if (abbrev.equalsIgnoreCase("i"))  { return PresenceTerm.INTRODUCED();
		} else if (abbrev.equalsIgnoreCase("ia")) { return PresenceTerm.INTRODUCED_ADVENTITIOUS();
		} else if (abbrev.equalsIgnoreCase("ic")) { return PresenceTerm.INTRODUCED_CULTIVATED();
		} else if (abbrev.equalsIgnoreCase("id")) { return PresenceTerm.INTRODUCED_DOUBTFULLY_INTRODUCED();
		} else if (abbrev.equalsIgnoreCase("ie")) { return PresenceTerm.INTRODUCED_FORMERLY_INTRODUCED();
		} else if (abbrev.equalsIgnoreCase("if")) { return PresenceTerm.INTRODUCED_REPORTED_IN_ERROR();
		} else if (abbrev.equalsIgnoreCase("in")) { return PresenceTerm.INTRODUCED_NATURALIZED();
		} else if (abbrev.equalsIgnoreCase("ip")) { return PresenceTerm.INTRODUCED_UNCERTAIN_DEGREE_OF_NATURALISATION();
		} else if (abbrev.equalsIgnoreCase("iq")) { return PresenceTerm.INTRODUCED_PRESENCE_QUESTIONABLE();
		} else if (abbrev.equalsIgnoreCase("n"))  { return PresenceTerm.NATIVE();
		} else if (abbrev.equalsIgnoreCase("nd")) { return PresenceTerm.NATIVE_DOUBTFULLY_NATIVE();
		} else if (abbrev.equalsIgnoreCase("ne")) { return PresenceTerm.NATIVE_FORMERLY_NATIVE();
		} else if (abbrev.equalsIgnoreCase("nf")) { return PresenceTerm.NATIVE_REPORTED_IN_ERROR();
		} else if (abbrev.equalsIgnoreCase("nq")) { return PresenceTerm.NATIVE_PRESENCE_QUESTIONABLE();
		} else if (abbrev.equalsIgnoreCase("p"))  { return PresenceTerm.PRESENT();
		} else if (abbrev.equalsIgnoreCase("na"))  { return PresenceTerm.NATURALISED();
		} else if (abbrev.equalsIgnoreCase("iv"))  { return PresenceTerm.INVASIVE();
		} else {
			logger.warn("Unknown presence status term: " + abbrev);
			return null;
		}
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<PresenceTerm> termVocabulary) {
		if (termMap == null){
			termMap = new HashMap<UUID, PresenceTerm>();
		}
		for (PresenceTerm term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), (PresenceTerm)term);
		}
	}
}