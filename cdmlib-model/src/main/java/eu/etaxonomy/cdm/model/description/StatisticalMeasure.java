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

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
/**
 * This class represents terms describing different statistical measures (such
 * as "sample size", "minimum" or "average") for {@link Feature features} that can be
 * described with numerical values (like for instance weights or temperature).
 *
 * @author m.doering
 * @created 08-Nov-2007 13:06:54
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StatisticalMeasure")
@XmlRootElement(name = "StatisticalMeasure")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class StatisticalMeasure extends DefinedTermBase<StatisticalMeasure> {
	private static final long serialVersionUID = 9168097283660941430L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(StatisticalMeasure.class);

	protected static Map<UUID, StatisticalMeasure> termMap = null;


	private static final UUID uuidMin = UUID.fromString("2c8b42e5-154c-42bd-a301-03b483275dd6");
	private static final UUID uuidMax = UUID.fromString("8955815b-7d21-4149-b1b7-d37af3c2046c");
	private static final UUID uuidAverage = UUID.fromString("264c3979-d551-4795-9e25-24c6b533fbb1");
	private static final UUID uuidSampleSize = UUID.fromString("571f86ca-a44c-4484-9981-11fd82138a7a");
	private static final UUID uuidVariance = UUID.fromString("4d22cf5e-89ff-4de3-a9ae-12dbeda3faba");
	private static final UUID uuidTypicalLowerBoundary = UUID.fromString("8372a89a-35ad-4755-a881-7edae6c37c8f");
	private static final UUID uuidTypicalUpperBoundary = UUID.fromString("9eff88ba-b8e7-4631-9e55-a50bd16ba79d");
	private static final UUID uuidStandardDeviation = UUID.fromString("9ee4397e-3496-4fe1-9114-afc7d7bdc652");
	//needed for Xper (later maybe integrated into model)
	public  static final UUID uuidStatisticalMeasureUnknownData = UUID.fromString("4bbd6e78-6d4e-4ec8-ac14-12f53aae049e");

	//********* FACTORY METHODS **************************************/
	/**
	 * Creates a new empty statistical measure instance.
	 *
	 * @see #NewInstance(String, String, String)
	 */
	public static StatisticalMeasure NewInstance(){
		return new StatisticalMeasure();
	}
	/**
	 * Creates a new statistical measure instance with a description
	 * (in the {@link Language#DEFAULT() default language}), a label and a label abbreviation.
	 *
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new statistical measure to be created
	 * @param	label  		 the string identifying the new statistical measure
	 * 						 to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new statistical measure to be created
	 * @see 				 #NewInstance()
	 */
	public static StatisticalMeasure NewInstance(String term, String label, String labelAbbrev){
		return new StatisticalMeasure(term, label, labelAbbrev);
	}


//********************************** Constructor *******************************************************************/

	// ************* CONSTRUCTORS *************/
	/**
	 * Class constructor: creates a new empty statistical measure instance.
	 *
	 * @see #StatisticalMeasure(String, String, String)
	 */
	//for hibernate use only
	@Deprecated
	protected StatisticalMeasure() {
		super(TermType.StatisticalMeasure);
	}
	private StatisticalMeasure(String term, String label, String labelAbbrev) {
		super(TermType.StatisticalMeasure, term, label, labelAbbrev);
	}


//************************** METHODS ********************************

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}



	protected static StatisticalMeasure getTermByUuid(UUID uuid){
        if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(StatisticalMeasure.class, uuid);
        } else {
            return termMap.get(uuid);
        }
	}

	public static final StatisticalMeasure MIN(){
		return getTermByUuid(uuidMin);
	}

	public static final StatisticalMeasure MAX(){
		return getTermByUuid(uuidMax);
	}

	public static final StatisticalMeasure AVERAGE(){
		return getTermByUuid(uuidAverage);
	}

	public static final StatisticalMeasure SAMPLE_SIZE(){
		return getTermByUuid(uuidSampleSize);
	}

	public static final StatisticalMeasure VARIANCE(){
		return getTermByUuid(uuidVariance);
	}

	public static final StatisticalMeasure TYPICAL_LOWER_BOUNDARY(){
		return getTermByUuid(uuidTypicalLowerBoundary);
	}

	public static final StatisticalMeasure TYPICAL_UPPER_BOUNDARY(){
		return getTermByUuid(uuidTypicalUpperBoundary);
	}

	public static final StatisticalMeasure STANDARD_DEVIATION(){
		return getTermByUuid(uuidStandardDeviation);
	}


	@Override
	protected void setDefaultTerms(TermVocabulary<StatisticalMeasure> termVocabulary) {
		termMap = new HashMap<UUID, StatisticalMeasure>();
		for (StatisticalMeasure term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

}