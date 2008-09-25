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

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:18
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Continent")
@XmlRootElement(name = "Continent")
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

	/**
	 * Factory method
	 * @return
	 */
	public static Continent NewInstance(){
		return new Continent();
	}

	/**
	 * Factory method
	 * @return
	 */
	public static Continent NewInstance(String term, String label, String labelAbbrev){
		return new Continent(term, label, labelAbbrev);
	}
	
	/**
	 * Constructor
	 */
	public Continent() {
		super();
	}
	public Continent(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	public static final Continent getByUuid(UUID uuid){
		return (Continent)findByUuid(uuid);
	}

	public static final Continent EUROPE(){
		return getByUuid(uuidEurope);
	}

	public static final Continent AFRICA(){
		return getByUuid(uuidAfrica);
	}

	public static final Continent ASIA(){
		return getByUuid(uuidAsia);
	}

	public static final Continent NORTH_AMERICA(){
		return getByUuid(uuidNAmerica);
	}

	public static final Continent ANTARCTICA(){
		return getByUuid(uuidAntarctica);
	}

	public static final Continent SOUTH_AMERICA(){
		return getByUuid(uuidSAmerica);
	}

	public static final Continent OCEANIA(){
		return getByUuid(uuidOceania);
	}

}