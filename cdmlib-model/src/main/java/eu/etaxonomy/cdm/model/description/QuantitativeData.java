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

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;

import java.util.*;

import javax.persistence.*;

/**
 * This class represents information pieces expressed in numerical data
 * (in opposition to {@link CategoricalData categorical data} on one side and to literal data on
 * the other side). Only {@link TaxonDescription taxon descriptions} and
 * {@link SpecimenDescription specimen descriptions} may contain quantitative data.<BR>
 * The "length of leaves" {@link Feature feature} for instance can be measured in inches.
 * If the length of leaves of a particular tree is described as
 * "typically between 3 and 5 inches" and "at the utmost 8 inches" then three
 * {@link StatisticalMeasurementValue statistical measurement value} instances
 * must be assigned to an instance of the present class
 * (with the {@link MeasurementUnit measurement unit} set to "inch"):<ul>
 * <li> the first one with the value "3" and the {@link StatisticalMeasure statistical measure}
 * "typical lower boundary",
 * <li> the second one with the value "5" and the statistical measure
 * "typical upper boundary"
 * <li> the third one with the value "8" and the statistical measure "maximum"
 * </ul> 
 * <P>
 * This class corresponds partially to CodedDescriptionType according to
 * the SDD schema.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:46
 */
@Entity
public class QuantitativeData extends DescriptionElementBase {
	static Logger logger = Logger.getLogger(QuantitativeData.class);
	private MeasurementUnit unit;
	private Set<StatisticalMeasurementValue> statisticalValues = new HashSet();
	
	/** 
	 * Class constructor: creates a new empty quantitative data instance.
	 */
	protected QuantitativeData(){
		super(null);
	}
	
	/** 
	 * Creates a new empty quantitative data instance.
	 */
	public static QuantitativeData NewInstance(){
		return new QuantitativeData();
	}
	
	/** 
	 * Returns the set of {@link StatisticalMeasurementValue statistical measurement values} describing
	 * the {@link Feature feature} corresponding to <i>this</i> quantitative data.
	 */
	@OneToMany
	public Set<StatisticalMeasurementValue> getStatisticalValues() {
		return statisticalValues;
	}
	/**
	 * @see	#getStatisticalValues() 
	 */
	protected void setStatisticalValues(
			Set<StatisticalMeasurementValue> statisticalValues) {
		this.statisticalValues = statisticalValues;
	}
	/**
	 * Adds a {@link StatisticalMeasurementValue statistical measurement value} to the set of
	 * {@link #getStatisticalValues() statistical measurement values} describing
	 * the {@link Feature feature} corresponding to <i>this</i> quantitative data.
	 * 
	 * @param statisticalValue	the statistical measurement value to be added to
	 * 							<i>this</i> quantitative data
	 * @see    	   				#getStatisticalValues()
	 */
	public void addStatisticalValue(
			StatisticalMeasurementValue statisticalValue) {
		this.statisticalValues.add(statisticalValue);
	}
	/** 
	 * Removes one element from the set of {@link #getStatisticalValues() statistical measurement values}
	 * describing the {@link Feature feature} corresponding to <i>this</i> quantitative data.
	 *
	 * @param  statisticalValue	the statistical measurement value which should be removed
	 * @see     				#getStatisticalValues()
	 * @see     				#addStatisticalValue(StatisticalMeasurementValue)
	 */
	public void removeStatisticalValue(
			StatisticalMeasurementValue statisticalValue) {
		this.statisticalValues.remove(statisticalValue);
	}

	
	/** 
	 * Returns the {@link MeasurementUnit measurement unit} used in <i>this</i>
	 * quantitative data.
	 */
	@ManyToOne
	public MeasurementUnit getUnit(){
		return this.unit;
	}
	/**
	 * @see	#getUnit() 
	 */
	public void setUnit(MeasurementUnit unit){
		this.unit = unit;
	}

	/** 
	 * Returns the numerical value of the one {@link StatisticalMeasurementValue statistical measurement value}
	 * with the corresponding {@link StatisticalMeasure statistical measure} "minimum" and
	 * belonging to <i>this</i> quantitative data. Returns "0" if no such
	 * statistical measurement value instance exists. 
	 */
	@Transient
	public float getMin(){
		return 0;
	}

	/** 
	 * Returns the numerical value of the one {@link StatisticalMeasurementValue statistical measurement value}
	 * with the corresponding {@link StatisticalMeasure statistical measure} "maximum" and
	 * belonging to <i>this</i> quantitative data. Returns "0" if no such
	 * statistical measurement value instance exists. 
	 */
	@Transient
	public float getMax(){
		return 0;
	}

	/** 
	 * Returns the numerical value of the one {@link StatisticalMeasurementValue statistical measurement value}
	 * with the corresponding {@link StatisticalMeasure statistical measure}
	 * "typical lower boundary" and belonging to <i>this</i> quantitative data.
	 * Returns "0" if no such statistical measurement value instance exists. 
	 */
	@Transient
	public float getTypicalLowerBoundary(){
		return 0;
	}

	/** 
	 * Returns the numerical value of the one {@link StatisticalMeasurementValue statistical measurement value}
	 * with the corresponding {@link StatisticalMeasure statistical measure}
	 * "typical upper boundary" and belonging to <i>this</i> quantitative data.
	 * Returns "0" if no such statistical measurement value instance exists. 
	 */
	@Transient
	public float getTypicalUpperBoundary(){
		return 0;
	}

}