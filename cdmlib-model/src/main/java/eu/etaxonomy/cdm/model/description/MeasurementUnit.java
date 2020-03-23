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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * This class represents measurement units such as "centimeter" or "degree
 * celsius".
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:34
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MeasurementUnit")
@XmlRootElement(name = "MeasurementUnit")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.term.DefinedTermBase")
@Audited
public class MeasurementUnit
        extends DefinedTermBase<MeasurementUnit> {

    private static final long serialVersionUID = 4904519152652248312L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MeasurementUnit.class);

	private static final UUID uuidMeter = UUID.fromString("8bef5055-789c-41e5-bea2-8dc2ea8ecdf6");
	private static final UUID uuidSecond = UUID.fromString("7cb20e73-d3c3-4290-bb55-98f7d1e76670");
	private static final UUID uuidMillimeter = UUID.fromString("62b0c1fd-a502-4fba-a2c7-8df004fd2b66");
	private static final UUID uuidMicrometer = UUID.fromString("128a5a38-6b92-45d2-8866-0d3c12a4915c");
	private static final UUID uuidCentimeter = UUID.fromString("950c5919-53e4-47ab-9efd-0ea86daa98ca");
	private static final UUID uuidPerSquareMillimeter = UUID.fromString("22e70b61-6474-4061-b0c9-49f86ef6b8ff");

	protected static Map<UUID, MeasurementUnit> termMap = null;

//********************************** Constructor *******************************************************************/

	//for hibernate use only
	@Deprecated
	protected MeasurementUnit(){
		super(TermType.MeasurementUnit);
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
	private MeasurementUnit(String term, String label, String labelAbbrev) {
		super(TermType.MeasurementUnit, term, label, labelAbbrev);
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
	public void resetTerms(){
		termMap = null;
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<MeasurementUnit> termVocabulary) {
		termMap = new HashMap<>();
		for (MeasurementUnit term : termVocabulary.getTerms()) {
			termMap.put(term.getUuid(), term);
		}
	}

	//******************************* STATIC METHODS *****************************************

    protected static MeasurementUnit getTermByUuid(UUID uuid){
        if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(MeasurementUnit.class, uuid);
        } else {
            return termMap.get(uuid);
        }
    }

    /**
     * Returns the "meter" unit.
     */
    public static final MeasurementUnit METER(){
        return getTermByUuid(uuidMeter);
    }
}
