/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;

import org.apache.log4j.Logger;
import javax.persistence.*;

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
@Entity
public class StatisticalMeasure extends DefinedTermBase {
	private static final Logger logger = Logger.getLogger(StatisticalMeasure.class);

	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty statistical measure instance.
	 * 
	 * @see #StatisticalMeasure(String, String, String)
	 */
	public StatisticalMeasure() {
		super();
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
		return null;
	}

	public static final StatisticalMeasure MAX(){
		return null;
	}

	public static final StatisticalMeasure AVERAGE(){
		return null;
	}

	public static final StatisticalMeasure SAMPLE_SIZE(){
		return null;
	}

	public static final StatisticalMeasure VARIANCE(){
		return null;
	}

	public static final StatisticalMeasure TYPICAL_LOWER_BOUNDARY(){
		return null;
	}

	public static final StatisticalMeasure TYPICAL_UPPER_BOUNDARY(){
		return null;
	}

}