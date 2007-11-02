/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:26
 */
@Entity
public class QuantitativeData extends FeatureBase {
	static Logger logger = Logger.getLogger(QuantitativeData.class);

	private MeasurementUnit unit;
	private ArrayList statisticalValues;

	public ArrayList getStatisticalValues(){
		return statisticalValues;
	}

	/**
	 * 
	 * @param statisticalValues
	 */
	public void setStatisticalValues(ArrayList statisticalValues){
		;
	}

	public MeasurementUnit getUnit(){
		return unit;
	}

	/**
	 * 
	 * @param unit
	 */
	public void setUnit(MeasurementUnit unit){
		;
	}

	@Transient
	public float getMin(){
		return null;
	}

	@Transient
	public float getMax(){
		return null;
	}

	@Transient
	public float getTypicalLowerBoundary(){
		return null;
	}

	@Transient
	public float getTypicalUpperBoundary(){
		return null;
	}

}