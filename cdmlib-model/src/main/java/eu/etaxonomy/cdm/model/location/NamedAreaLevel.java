/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Controlled vocabulary to differentiate levels of areas such as province, state,
 * etc.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:36
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NamedAreaLevel")
@XmlRootElement(name = "NamedAreaLevel")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class NamedAreaLevel extends OrderedTermBase<NamedAreaLevel> {
	private static final long serialVersionUID = -7977901140330659208L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NamedAreaLevel.class);

	private static final UUID uuidTdwgLevel1 = UUID.fromString("cd7771b2-7427-4a01-9057-7d7a897dddaf");
	private static final UUID uuidTdwgLevel2 = UUID.fromString("38efa5fd-d7f0-451c-9de9-e6cce41e2225");
	private static final UUID uuidTdwgLevel3 = UUID.fromString("25b563b6-6a6c-401b-b090-c9498886c50b");
	private static final UUID uuidTdwgLevel4 = UUID.fromString("160ff2c8-9bfc-49c2-9afd-049c21a91695");
	private static final UUID uuidNatureReserve = UUID.fromString("340b9050-a65d-4dd4-9523-bc10f977bc68");
	private static final UUID uuidState = UUID.fromString("08aa6127-8ebc-4120-8411-a468a7257e02");
	private static final UUID uuidProvince = UUID.fromString("401d48b4-9f09-4354-be0f-c2138444f72d");
	private static final UUID uuidTown = UUID.fromString("f127b4d2-f6bc-4019-9c87-ee3f4de1f094");
	private static final UUID uuidCountry = UUID.fromString("79db63a4-1563-461e-8e41-48f5722feca4");
	private static NamedAreaLevel COUNTRY;
	private static NamedAreaLevel TOWN;
	private static NamedAreaLevel PROVINCE;
	private static NamedAreaLevel STATE;
	private static NamedAreaLevel NATURE_RESERVE;
	private static NamedAreaLevel TDWG_LEVEL4;
	private static NamedAreaLevel TDWG_LEVEL3;
	private static NamedAreaLevel TDWG_LEVEL2;
	private static NamedAreaLevel TDWG_LEVEL1;

	
	/**
	 * Factory method
	 * @return
	 */
	public static NamedAreaLevel NewInstance(){
		return new NamedAreaLevel();
	}

	/**
	 * Factory method
	 * @return
	 */
	public static NamedAreaLevel NewInstance(String term, String label, String labelAbbrev){
		return new NamedAreaLevel(term, label, labelAbbrev);
	}
	
	/**
	 * Constructor
	 */
	public NamedAreaLevel() {
	}

	protected NamedAreaLevel(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	
//******************** METHODS ***************************************/
		
	/**
	 * continents
	 */
	public static final NamedAreaLevel TDWG_LEVEL1(){
		return TDWG_LEVEL1;
	}

	/**
	 * larger regions
	 */
	public static final NamedAreaLevel TDWG_LEVEL2(){
		return TDWG_LEVEL2;
	}

	/**
	 * mostly countries
	 */
	public static final NamedAreaLevel TDWG_LEVEL3(){
		return TDWG_LEVEL3;
	}

	public static final NamedAreaLevel TDWG_LEVEL4(){
		return TDWG_LEVEL4;
	}

	public static final NamedAreaLevel NATURE_RESERVE(){
		return NATURE_RESERVE;
	}

	public static final NamedAreaLevel STATE(){
		return STATE;
	}

	public static final NamedAreaLevel PROVINCE(){
		return PROVINCE;
	}

	public static final NamedAreaLevel TOWN(){
		return TOWN;
	}

	public static final NamedAreaLevel COUNTRY(){
		return COUNTRY;
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<NamedAreaLevel> termVocabulary) {
		NamedAreaLevel.COUNTRY = termVocabulary.findTermByUuid(NamedAreaLevel.uuidCountry);
		NamedAreaLevel.NATURE_RESERVE = termVocabulary.findTermByUuid(NamedAreaLevel.uuidNatureReserve);
		NamedAreaLevel.PROVINCE = termVocabulary.findTermByUuid(NamedAreaLevel.uuidProvince);
		NamedAreaLevel.STATE = termVocabulary.findTermByUuid(NamedAreaLevel.uuidState);
		NamedAreaLevel.TDWG_LEVEL1 = termVocabulary.findTermByUuid(NamedAreaLevel.uuidTdwgLevel1);
		NamedAreaLevel.TDWG_LEVEL2 = termVocabulary.findTermByUuid(NamedAreaLevel.uuidTdwgLevel2);
		NamedAreaLevel.TDWG_LEVEL3 = termVocabulary.findTermByUuid(NamedAreaLevel.uuidTdwgLevel3);
		NamedAreaLevel.TDWG_LEVEL4 = termVocabulary.findTermByUuid(NamedAreaLevel.uuidTdwgLevel4);
		NamedAreaLevel.TOWN = termVocabulary.findTermByUuid(NamedAreaLevel.uuidTown);
		
	}
}