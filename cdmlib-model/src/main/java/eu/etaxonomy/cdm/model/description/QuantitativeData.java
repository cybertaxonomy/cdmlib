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

import java.util.*;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:46
 */
@Entity
public class QuantitativeData extends FeatureBase {
	static Logger logger = Logger.getLogger(QuantitativeData.class);
	private MeasurementUnit unit;
	private Set<StatisticalMeasurementValue> statisticalValues = new HashSet();
	

	@OneToMany
	public Set<StatisticalMeasurementValue> getStatisticalValues() {
		return statisticalValues;
	}
	protected void setStatisticalValues(
			Set<StatisticalMeasurementValue> statisticalValues) {
		this.statisticalValues = statisticalValues;
	}
	public void addStatisticalValue(
			StatisticalMeasurementValue statisticalValue) {
		this.statisticalValues.add(statisticalValue);
	}
	public void removeStatisticalValue(
			StatisticalMeasurementValue statisticalValue) {
		this.statisticalValues.remove(statisticalValue);
	}

	
	@ManyToOne
	public MeasurementUnit getUnit(){
		return this.unit;
	}
	public void setUnit(MeasurementUnit unit){
		this.unit = unit;
	}

	@Transient
	public float getMin(){
		return 0;
	}

	@Transient
	public float getMax(){
		return 0;
	}

	@Transient
	public float getTypicalLowerBoundary(){
		return 0;
	}

	@Transient
	public float getTypicalUpperBoundary(){
		return 0;
	}

}