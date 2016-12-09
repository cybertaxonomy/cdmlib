/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * Controlled vocabulary to differentiate levels of areas such as province, state,
 * etc.
 * @author m.doering
 * @created 08-Nov-2007 13:06:36
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NamedAreaLevel")
@XmlRootElement(name = "NamedAreaLevel")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class NamedAreaLevel extends OrderedTermBase<NamedAreaLevel> {
	private static final long serialVersionUID = -7977901140330659208L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NamedAreaLevel.class);

	protected static Map<UUID, NamedAreaLevel> termMap = null;

	private static final UUID uuidTdwgLevel1 = UUID.fromString("cd7771b2-7427-4a01-9057-7d7a897dddaf");
	private static final UUID uuidTdwgLevel2 = UUID.fromString("38efa5fd-d7f0-451c-9de9-e6cce41e2225");
	private static final UUID uuidTdwgLevel3 = UUID.fromString("25b563b6-6a6c-401b-b090-c9498886c50b");
	private static final UUID uuidTdwgLevel4 = UUID.fromString("160ff2c8-9bfc-49c2-9afd-049c21a91695");
	private static final UUID uuidNatureReserve = UUID.fromString("340b9050-a65d-4dd4-9523-bc10f977bc68");
	private static final UUID uuidState = UUID.fromString("08aa6127-8ebc-4120-8411-a468a7257e02");
	private static final UUID uuidProvince = UUID.fromString("401d48b4-9f09-4354-be0f-c2138444f72d");
	private static final UUID uuidTown = UUID.fromString("f127b4d2-f6bc-4019-9c87-ee3f4de1f094");
	private static final UUID uuidCountry = UUID.fromString("79db63a4-1563-461e-8e41-48f5722feca4");
    private static final UUID uuidDepartment = UUID.fromString("a31fe600-d142-4722-b82e-4df2f5ec3cb3");

//************************** FACTORY METHODS ********************************

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

//************************** CONSTRUCTOR ********************************

	//for hibernate use only
	@Deprecated
	protected NamedAreaLevel() {
		super(TermType.NamedAreaLevel);
	}

	private NamedAreaLevel(String term, String label, String labelAbbrev) {
		super(TermType.NamedAreaLevel, term, label, labelAbbrev);
	}



//************************** METHODS ********************************

	protected static NamedAreaLevel getTermByUuid(UUID uuid){
	    if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(NamedAreaLevel.class, uuid);
        } else {
            return termMap.get(uuid);
        }
	}

	/**
	 * Continents
	 */
	public static final NamedAreaLevel TDWG_LEVEL1(){
		return getTermByUuid(uuidTdwgLevel1);
	}

	/**
	 * larger regions
	 */
	public static final NamedAreaLevel TDWG_LEVEL2(){
		return getTermByUuid(uuidTdwgLevel2);
	}

	/**
	 * mostly countries
	 */
	public static final NamedAreaLevel TDWG_LEVEL3(){
		return getTermByUuid(uuidTdwgLevel3);
	}

	public static final NamedAreaLevel TDWG_LEVEL4(){
		return getTermByUuid(uuidTdwgLevel4);
	}

	public static final NamedAreaLevel NATURE_RESERVE(){
		return getTermByUuid(uuidNatureReserve);
	}

	public static final NamedAreaLevel STATE(){
		return getTermByUuid(uuidState);
	}

	public static final NamedAreaLevel PROVINCE(){
		return getTermByUuid(uuidProvince);
	}

    public static NamedAreaLevel DEPARTMENT() {
        return getTermByUuid(uuidDepartment);
    }

	public static final NamedAreaLevel TOWN(){
		return getTermByUuid(uuidTown);
	}

	public static final NamedAreaLevel COUNTRY(){
		return getTermByUuid(uuidCountry);
	}

	public static final boolean isTDWG_LEVEL1(String str){
		boolean result = false;
		if (uuidTdwgLevel1.compareTo(UUID.fromString(str)) == 0){
			result = true;
		}
		return result;
	}

	public static final boolean isTDWG_LEVEL2(String str){
		boolean result = false;
		if (uuidTdwgLevel2.compareTo(UUID.fromString(str)) == 0){
			result = true;
		}
		return result;
	}

	public static final boolean isTDWG_LEVEL3(String str){
		boolean result = false;
		if (uuidTdwgLevel3.compareTo(UUID.fromString(str)) == 0){
			result = true;
		}
		return result;
	}

	public static final boolean isTDWG_LEVEL4(String str){
		boolean result = false;
		if (uuidTdwgLevel4.compareTo(UUID.fromString(str)) == 0){
			result = true;
		}
		return result;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}



	@Override
	protected void setDefaultTerms(TermVocabulary<NamedAreaLevel> termVocabulary) {
		termMap = new HashMap<UUID, NamedAreaLevel>();
		for (NamedAreaLevel term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

}