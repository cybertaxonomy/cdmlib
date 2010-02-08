/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
/**
 * This class represents terms describing different statistical measures (such
 * as "sample size", "minimum" or "average") for {@link Feature features} that can be
 * described with numerical values (like for instance weights or temperature).
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:54
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StatisticalMeasure")
@XmlRootElement(name = "StatisticalMeasure")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class StatisticalMeasure extends DefinedTermBase<StatisticalMeasure> {
	private static final long serialVersionUID = 9168097283660941430L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(StatisticalMeasure.class);
	
	private static final UUID uuidMin = UUID.fromString("2c8b42e5-154c-42bd-a301-03b483275dd6");
	private static final UUID uuidMax = UUID.fromString("8955815b-7d21-4149-b1b7-d37af3c2046c");
	private static final UUID uuidAverage = UUID.fromString("264c3979-d551-4795-9e25-24c6b533fbb1");
	private static final UUID uuidSampleSize = UUID.fromString("571f86ca-a44c-4484-9981-11fd82138a7a");
	private static final UUID uuidVariance = UUID.fromString("4d22cf5e-89ff-4de3-a9ae-12dbeda3faba");
	private static final UUID uuidTypicalLowerBoundary = UUID.fromString("8372a89a-35ad-4755-a881-7edae6c37c8f");
	private static final UUID uuidTypicalUpperBoundary = UUID.fromString("9eff88ba-b8e7-4631-9e55-a50bd16ba79d");
	private static final UUID uuidStandardDeviation = UUID.fromString("9ee4397e-3496-4fe1-9114-afc7d7bdc652");
	private static StatisticalMeasure STANDARD_DEVIATION;
	private static StatisticalMeasure TYPICAL_UPPER_BOUNDARY;
	private static StatisticalMeasure TYPICAL_LOWER_BOUNDARY;
	private static StatisticalMeasure VARIANCE;
	private static StatisticalMeasure SAMPLE_SIZE;
	private static StatisticalMeasure AVERAGE;
	private static StatisticalMeasure MAX;
	private static StatisticalMeasure MIN;

	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty statistical measure instance.
	 * 
	 * @see #StatisticalMeasure(String, String, String)
	 */
	public StatisticalMeasure() {
	}
	public StatisticalMeasure(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	//********* METHODS **************************************/
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

	public static final StatisticalMeasure MIN(){
		return MIN;
	}

	public static final StatisticalMeasure MAX(){
		return MAX;
	}

	public static final StatisticalMeasure AVERAGE(){
		return AVERAGE;
	}

	public static final StatisticalMeasure SAMPLE_SIZE(){
		return SAMPLE_SIZE;
	}

	public static final StatisticalMeasure VARIANCE(){
		return VARIANCE;
	}

	public static final StatisticalMeasure TYPICAL_LOWER_BOUNDARY(){
		return TYPICAL_LOWER_BOUNDARY;
	}

	public static final StatisticalMeasure TYPICAL_UPPER_BOUNDARY(){
		return TYPICAL_UPPER_BOUNDARY;
	}

	public static final StatisticalMeasure STANDARD_DEVIATION(){
		return STANDARD_DEVIATION;
	}
	@Override
	protected void setDefaultTerms(TermVocabulary<StatisticalMeasure> termVocabulary) {
		StatisticalMeasure.AVERAGE = termVocabulary.findTermByUuid(StatisticalMeasure.uuidAverage);
		StatisticalMeasure.MAX = termVocabulary.findTermByUuid(StatisticalMeasure.uuidMax);
		StatisticalMeasure.MIN = termVocabulary.findTermByUuid(StatisticalMeasure.uuidMin);
		StatisticalMeasure.SAMPLE_SIZE = termVocabulary.findTermByUuid(StatisticalMeasure.uuidSampleSize);
		StatisticalMeasure.STANDARD_DEVIATION = termVocabulary.findTermByUuid(StatisticalMeasure.uuidStandardDeviation);
		StatisticalMeasure.TYPICAL_LOWER_BOUNDARY = termVocabulary.findTermByUuid(StatisticalMeasure.uuidTypicalLowerBoundary);
		StatisticalMeasure.TYPICAL_UPPER_BOUNDARY = termVocabulary.findTermByUuid(StatisticalMeasure.uuidTypicalUpperBoundary);
		StatisticalMeasure.VARIANCE = termVocabulary.findTermByUuid(StatisticalMeasure.uuidVariance);
	}
	
}