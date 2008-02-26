/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;


import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;

import org.apache.log4j.Logger;

import java.util.*;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:18
 */
@Entity
public class Continent extends DefinedTermBase {
	static Logger logger = Logger.getLogger(Continent.class);

	private static final UUID uuidEurope = UUID.fromString("3b69f979-408c-4080-b573-0ad78a315610");
	private static final UUID uuidAfrica = UUID.fromString("c204c529-d8d2-458f-b939-96f0ebd2cbe8");
	private static final UUID uuidAsia = UUID.fromString("7f4f4f89-3b4c-475d-929f-144109bd8457");
	private static final UUID uuidNAmerica = UUID.fromString("81d8aca3-ddd7-4537-9f2b-5327c95b6e28");
	private static final UUID uuidSAmerica = UUID.fromString("12b861c9-c922-498c-8b1a-62afc26d19e3");
	private static final UUID uuidOceania = UUID.fromString("c57adcff-5213-45f0-a5f0-97a9f5c0f1fe");
	private static final UUID uuidAntarctica = UUID.fromString("71fd9ab7-9b07-4eb6-8e54-c519aff56728");


	public Continent() {
		super();
	}
	public Continent(String term, String label) {
		super(term, label);
	}

	public static final Continent getUUID(UUID uuid){
		return (Continent)findByUuid(uuid);
	}

	public static final Continent EUROPE(){
		return getUUID(uuidEurope);
	}

	public static final Continent AFRICA(){
		return getUUID(uuidAfrica);
	}

	public static final Continent ASIA(){
		return getUUID(uuidAsia);
	}

	public static final Continent NORTH_AMERICA(){
		return getUUID(uuidNAmerica);
	}

	public static final Continent ANTARCTICA(){
		return getUUID(uuidAntarctica);
	}

	public static final Continent SOUTH_AMERICA(){
		return getUUID(uuidSAmerica);
	}

	public static final Continent OCEANIA(){
		return getUUID(uuidOceania);
	}

}