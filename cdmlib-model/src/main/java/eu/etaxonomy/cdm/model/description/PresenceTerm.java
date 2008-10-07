/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.Continent;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
public class PresenceTerm extends PresenceAbsenceTermBase<PresenceTerm> {
	private static final Logger logger = Logger.getLogger(PresenceTerm.class);

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
		super();
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
	private static final UUID uuidE=UUID.fromString("c3ee7048-15b7-4be1-b687-9ce9c1a669d6");

	

	public static final PresenceTerm getByUuid(UUID uuid){
		return (PresenceTerm)findByUuid(uuid);
	}

	
	//TODO Marc: Kannst du die noch ein bischen schöner formatieren, wenn du sie dokumentierst? Danke
	public static final PresenceTerm PRESENT(){return getByUuid(uuidP);}
	public static final PresenceTerm NATIVE(){return getByUuid(uuidN);}
	public static final PresenceTerm NATIVE_PRESENCE_QUESTIONABLE(){return getByUuid(uuidNQ);}
	public static final PresenceTerm NATIVE_FORMERLY_NATIVE(){return getByUuid(uuidNE);}
	public static final PresenceTerm NATIVE_DOUBTFULLY_NATIVE(){return getByUuid(uuidND);}
	public static final PresenceTerm NATIVE_REPORTED_IN_ERROR(){return getByUuid(uuidNF);}
	public static final PresenceTerm CULTIVATED(){return getByUuid(uuidC);}
	public static final PresenceTerm CULTIVATED_REPORTED_IN_ERROR(){return getByUuid(uuidCF);}
	public static final PresenceTerm INTRODUCED(){return getByUuid(uuidI);}
	public static final PresenceTerm INTRODUCED_PRESENCE_QUESTIONABLE(){return getByUuid(uuidIQ);}
	public static final PresenceTerm INTRODUCED_FORMERLY_INTRODUCED(){return getByUuid(uuidIE);}
	public static final PresenceTerm INTRODUCED_DOUBTFULLY_INTRODUCED(){return getByUuid(uuidID);}
	public static final PresenceTerm INTRODUCED_UNCERTAIN_DEGREE_OF_NATURALISATION(){return getByUuid(uuidIP);}
	public static final PresenceTerm INTRODUCED_ADVENTITIOUS(){return getByUuid(uuidIA);}
	public static final PresenceTerm INTRODUCED_NATURALIZED(){return getByUuid(uuidIN);}
	public static final PresenceTerm INTRODUCED_REPORTED_IN_ERROR(){return getByUuid(uuidIF);}
	public static final PresenceTerm ENDEMIC_FOR_THE_RELEVANT_AREA(){return getByUuid(uuidE);}
}