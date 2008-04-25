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
import org.apache.log4j.Logger;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:54
 */
@Entity
public class StatisticalMeasure extends DefinedTermBase {
	static Logger logger = Logger.getLogger(StatisticalMeasure.class);

	/**
	 * Factory method
	 * @return
	 */
	public static StatisticalMeasure NewInstance(String term, String label){
		return new StatisticalMeasure(term, label);
	}
	/**
	 * Factory method
	 * @return
	 */
	public static StatisticalMeasure NewInstance(){
		return new StatisticalMeasure();
	}
	
	/**
	 * Constructor
	 */
	public StatisticalMeasure() {
		super();
	}
	public StatisticalMeasure(String term, String label) {
		super(term, label);
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