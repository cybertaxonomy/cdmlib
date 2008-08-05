/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;



import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import org.apache.log4j.Logger;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * http://rs.tdwg.org/ontology/voc/TaxonName#NomencalturalTypeTypeTerm
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:00
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TypeDesignationStatus")
@Entity
public class TypeDesignationStatus extends OrderedTermBase<TypeDesignationStatus> {
	static Logger logger = Logger.getLogger(TypeDesignationStatus.class);

	private static final UUID uuidHolotype = UUID.fromString("a407dbc7-e60c-46ff-be11-eddf4c5a970d");
	private static final UUID uuidLectotype = UUID.fromString("05002d46-083e-4b27-8731-2e7c28a8825c");
	private static final UUID uuidNeotype = UUID.fromString("26e13359-8f77-4e40-a85a-56c01782fce0");
	private static final UUID uuidEpitype = UUID.fromString("989a2715-71d5-4fbe-aa9a-db9168353744");
	private static final UUID uuidIsolectotype = UUID.fromString("7a1a8a53-78f4-4fc0-89f7-782e94992d08");
	private static final UUID uuidIsoneotype = UUID.fromString("7afc2f4f-f70a-4aa5-80a5-87764f746bde");
	private static final UUID uuidIsotype = UUID.fromString("93ef8257-0a08-47bb-9b36-542417ae7560");
	private static final UUID uuidParaneotype = UUID.fromString("0c39e2a5-2fe0-4d4f-819a-f609b5340339");
	private static final UUID uuidParatype = UUID.fromString("eb7df2e5-d9a7-479d-970c-c6f2b0a761d7");
	private static final UUID uuidSecondStepLectotype = UUID.fromString("01d91053-7004-4984-aa0d-9f4de59d6205");
	private static final UUID uuidSecondStepNeotype = UUID.fromString("8d2fed1f-242e-4bcf-bbd7-e85133e479dc");
	private static final UUID uuidSyntype = UUID.fromString("f3b60bdb-4638-4ca9-a0c7-36e77d8459bb");
	private static final UUID uuidParalectotype = UUID.fromString("7244bc51-14d8-41a6-9524-7dc5303bba29");
	private static final UUID uuidIsoepitype = UUID.fromString("95b90696-e103-4bc0-b60b-c594983fb566");
	private static final UUID uuidIconotype = UUID.fromString("643513d0-32f5-46ba-840b-d9b9caf8160f");
	private static final UUID uuidPhototype = UUID.fromString("b7807acc-f559-474e-ad4a-e7a41e085e34");

	
	public TypeDesignationStatus() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TypeDesignationStatus(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	
	public static final TypeDesignationStatus getByUuid(UUID uuid){
		return (TypeDesignationStatus) findByUuid(uuid);
	}
	
	public static final TypeDesignationStatus HOLOTYPE(){
		return getByUuid(uuidHolotype);
	}

	public static final TypeDesignationStatus LECTOTYPE(){
		return getByUuid(uuidLectotype);
	}

	public static final TypeDesignationStatus NEOTYPE(){
		return getByUuid(uuidNeotype);
	}

	public static final TypeDesignationStatus EPITYPE(){
		return getByUuid(uuidEpitype);
	}

	public static final TypeDesignationStatus ISOLECTOTYPE(){
		return getByUuid(uuidIsolectotype);
	}

	public static final TypeDesignationStatus ISONEOTYPE(){
		return getByUuid(uuidIsoneotype);
	}

	public static final TypeDesignationStatus ISOTYPE(){
		return getByUuid(uuidIsotype);
	}

	public static final TypeDesignationStatus PARANEOTYPE(){
		return getByUuid(uuidParaneotype);
	}

	public static final TypeDesignationStatus PARATYPE(){
		return getByUuid(uuidParatype);
	}

	public static final TypeDesignationStatus SECOND_STEP_LECTOTYPE(){
		return getByUuid(uuidSecondStepLectotype);
	}

	public static final TypeDesignationStatus SECOND_STEP_NEOTYPE(){
		return getByUuid(uuidSecondStepNeotype);
	}

	public static final TypeDesignationStatus SYNTYPE(){
		return getByUuid(uuidSyntype);
	}

	public static final TypeDesignationStatus PARALECTOTYPE(){
		return getByUuid(uuidParalectotype);
	}

	public static final TypeDesignationStatus ISOEPITYPE(){
		return getByUuid(uuidIsoepitype);
	}

	public static final TypeDesignationStatus ICONOTYPE(){
		return getByUuid(uuidIconotype);
	}

	public static final TypeDesignationStatus PHOTOTYPE(){
		return getByUuid(uuidPhototype);
	}

}