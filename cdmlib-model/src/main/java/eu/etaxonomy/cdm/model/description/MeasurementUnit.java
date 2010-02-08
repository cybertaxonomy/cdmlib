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

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
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
 * This class represents measurement units such as "centimeter" or "degree
 * Celsius".
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:34
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MeasurementUnit")
@XmlRootElement(name = "MeasurementUnit")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class MeasurementUnit extends DefinedTermBase<MeasurementUnit> {
	private static final long serialVersionUID = 4904519152652248312L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MeasurementUnit.class);
	
	protected static Map<UUID, MeasurementUnit> termMap = null;		

	/** 
	 * Class constructor: creates a new empty measurement unit instance.
	 * 
	 * @see #MeasurementUnit(String, String, String)
	 */
	public MeasurementUnit(){
	}

	/** 
	 * Creates a new measurement unit with a description
	 * (in the {@link Language#DEFAULT() default language}), a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new measurement unit to be created 
	 * @param	label  		 the string identifying the new measurement unit
	 * 						 to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new measurement unit to be created
	 * @see 				 #NewInstance()
	 */
	public MeasurementUnit(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	
	/** 
	 * Creates a new empty measurement unit instance.
	 * 
	 * @see #MeasurementUnit(String, String, String)
	 */
	public static MeasurementUnit NewInstance(){
		return new MeasurementUnit();
	}
	
	/** 
	 * Creates a new empty measurement unit instance.
	 * 
	 * @see #MeasurementUnit(String, String, String)
	 */
	public static MeasurementUnit NewInstance(String term, String label, String labelAbbrev){
		return new MeasurementUnit(term, label, labelAbbrev);
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<MeasurementUnit> termVocabulary) {
		termMap = new HashMap<UUID, MeasurementUnit>();
		for (MeasurementUnit term : termVocabulary.getTerms()) {
			termMap.put(term.getUuid(), term); 
		}
	}
		

}