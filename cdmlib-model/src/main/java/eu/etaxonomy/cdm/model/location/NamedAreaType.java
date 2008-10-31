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

import eu.etaxonomy.cdm.model.common.DefinedTermBase;


import org.apache.log4j.Logger;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Controlled vocabulary to differentiate categories of areas
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:37
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NamedAreaType")
@XmlRootElement(name = "NamedAreaType")
@Entity
public class NamedAreaType extends DefinedTermBase {
	public static final Logger logger = Logger.getLogger(NamedAreaType.class);

	private static final UUID uuidNaturalArea = UUID.fromString("cc33167c-d366-4030-b984-6b14e4f5fd22");
	private static final UUID uuidAdministrationArea = UUID.fromString("1799f581-f425-40d6-a4db-ec2c638c0e92");

	
	
	/**
	 * Factory method
	 * @return
	 */
	public static NamedAreaType NewInstance(String term, String label, String labelAbbrev){
		return new NamedAreaType(term, label, labelAbbrev);
	}
	
	/**
	 * Constructor
	 */
	public NamedAreaType(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}
	
	public NamedAreaType(){
		super();
	}


	public static final NamedAreaType getByUuid(UUID uuid){
		return (NamedAreaType) findByUuid(uuid);
	}	
	
	/**
	 * The boundaries are given by natural factors (mountains, valleys, climate, etc.)
	 */
	public static final NamedAreaType NATURAL_AREA(){
		return getByUuid(uuidNaturalArea);
	}

	/**
	 * The boundaries depend on administration (county, state, reserve, etc.)
	 */
	public static final NamedAreaType ADMINISTRATION_AREA(){
		return getByUuid(uuidAdministrationArea);
	}

}