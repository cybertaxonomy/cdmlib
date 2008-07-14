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
import org.apache.log4j.Logger;
import javax.persistence.*;

/**
 * Controlled vocabulary to diferenciate levels of areas such as province, state,
 * etc.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:36
 */
@Entity
public class NamedAreaLevel extends OrderedTermBase<NamedAreaLevel> {
	private static final Logger logger = Logger.getLogger(NamedAreaLevel.class);

	private static final UUID uuidTdwgLevel1 = UUID.fromString("cd7771b2-7427-4a01-9057-7d7a897dddaf");
	private static final UUID uuidTdwgLevel2 = UUID.fromString("38efa5fd-d7f0-451c-9de9-e6cce41e2225");
	private static final UUID uuidTdwgLevel3 = UUID.fromString("25b563b6-6a6c-401b-b090-c9498886c50b");
	private static final UUID uuidTdwgLevel4 = UUID.fromString("160ff2c8-9bfc-49c2-9afd-049c21a91695");
	private static final UUID uuidNatureReserve = UUID.fromString("340b9050-a65d-4dd4-9523-bc10f977bc68");
	private static final UUID uuidState = UUID.fromString("08aa6127-8ebc-4120-8411-a468a7257e02");
	private static final UUID uuidProvince = UUID.fromString("401d48b4-9f09-4354-be0f-c2138444f72d");
	private static final UUID uuidTown = UUID.fromString("f127b4d2-f6bc-4019-9c87-ee3f4de1f094");

	
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
		super();
	}

	protected NamedAreaLevel(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	
//******************** METHODS ***************************************/
	

	public static final NamedAreaLevel getByUuid(UUID uuid){
		return (NamedAreaLevel) findByUuid(uuid);
	}
	
	
	/**
	 * continents
	 */
	public static final NamedAreaLevel TDWG_LEVEL1(){
		return getByUuid(uuidTdwgLevel1);
	}

	/**
	 * larger regions
	 */
	public static final NamedAreaLevel TDWG_LEVEL2(){
		return getByUuid(uuidTdwgLevel2);
	}

	/**
	 * mostly countries
	 */
	public static final NamedAreaLevel TDWG_LEVEL3(){
		return getByUuid(uuidTdwgLevel3);
	}

	public static final NamedAreaLevel TDWG_LEVEL4(){
		return getByUuid(uuidTdwgLevel4);
	}

	public static final NamedAreaLevel NATURE_RESERVE(){
		return getByUuid(uuidNatureReserve);
	}

	public static final NamedAreaLevel STATE(){
		return getByUuid(uuidState);
	}

	public static final NamedAreaLevel PROVINCE(){
		return getByUuid(uuidProvince);
	}

	public static final NamedAreaLevel TOWN(){
		return getByUuid(uuidTown);
	}

}