/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.constraints.NotEmpty;

import eu.etaxonomy.cdm.validation.Level2;

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
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuantitativeData", propOrder = {
    "unit",
    "statisticalValues"
})
@XmlRootElement(name = "QuantitativeData")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.description.DescriptionElementBase")
@Audited
public class QuantitativeData extends DescriptionElementBase implements Cloneable {
	private static final long serialVersionUID = -2755806455420051488L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(QuantitativeData.class);
	
	@XmlElement(name = "MeasurementUnit")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	private MeasurementUnit unit;
	
	@XmlElementWrapper(name = "StatisticalValues")
	@XmlElement(name = "StatisticalValue")
	@OneToMany(fetch = FetchType.LAZY)
	@Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE,CascadeType.DELETE_ORPHAN })
	@NotEmpty(groups = Level2.class)
	private Set<StatisticalMeasurementValue> statisticalValues = new HashSet<StatisticalMeasurementValue>();

// ******************************** FACTORY METHOD *******************************/
	
	/** 
	 * Creates a new empty quantitative data instance.
	 */
	public static QuantitativeData NewInstance(){
		return new QuantitativeData();
	}

// ******************************** FACTORY METHOD *******************************/
	
	/** 
	 * Creates a new empty quantitative data instance.
	 */
	public static QuantitativeData NewInstance(Feature feature){
		return new QuantitativeData(feature);
	}

	
// ******************************** CONSTRUCTOR *******************************/

	/** 
	 * Class constructor: creates a new empty quantitative data instance.
	 */
	protected QuantitativeData(){
		super(null);
	}

	/** 
	 * Class constructor: creates a new empty quantitative data instance.
	 */
	protected QuantitativeData(Feature feature){
		super(feature);
	}

	
// ******************************** GETTER /SETTER *******************************/
	
	
	/** 
	 * Returns the set of {@link StatisticalMeasurementValue statistical measurement values} describing
	 * the {@link Feature feature} corresponding to <i>this</i> quantitative data.
	 */
	public Set<StatisticalMeasurementValue> getStatisticalValues() {
		return statisticalValues;
	}

	protected void setStatisticalValues(Set<StatisticalMeasurementValue> statisticalValues) {
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
	public void addStatisticalValue(StatisticalMeasurementValue statisticalValue) {
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
	public void removeStatisticalValue(StatisticalMeasurementValue statisticalValue) {
		this.statisticalValues.remove(statisticalValue);
	}

	
	/** 
	 * Returns the {@link MeasurementUnit measurement unit} used in <i>this</i>
	 * quantitative data.
	 */
	public MeasurementUnit getUnit(){
		return this.unit;
	}
	/**
	 * @see	#getUnit() 
	 */
	public void setUnit(MeasurementUnit unit){
		this.unit = unit;
	}

// ******************************** TRANSIENT METHODS *******************************/
	
	
	/** 
	 * Returns the numerical value of the one {@link StatisticalMeasurementValue statistical measurement value}
	 * with the corresponding {@link StatisticalMeasure statistical measure} "minimum" and
	 * belonging to <i>this</i> quantitative data. Returns <code>null</code> if no such
	 * statistical measurement value instance exists. 
	 */
	@Transient
	public Float getMin(){
		return getSpecificStatisticalValue(StatisticalMeasure.MIN());
	}

	/** 
	 * Returns the numerical value of the one {@link StatisticalMeasurementValue statistical measurement value}
	 * with the corresponding {@link StatisticalMeasure statistical measure} "maximum" and
	 * belonging to <i>this</i> quantitative data. Returns <code>null</code> if no such
	 * statistical measurement value instance exists. 
	 */
	@Transient
	public Float getMax(){
		return getSpecificStatisticalValue(StatisticalMeasure.MAX());
	}

	/** 
	 * Returns the numerical value of the one {@link StatisticalMeasurementValue statistical measurement value}
	 * with the corresponding {@link StatisticalMeasure statistical measure}
	 * "typical lower boundary" and belonging to <i>this</i> quantitative data.
	 * Returns <code>null</code> if no such statistical measurement value instance exists. 
	 */
	@Transient
	public Float getTypicalLowerBoundary(){
		return getSpecificStatisticalValue(StatisticalMeasure.TYPICAL_LOWER_BOUNDARY());
	}
	
	/** 
	 * Returns the numerical value of the one {@link StatisticalMeasurementValue statistical measurement value}
	 * with the corresponding {@link StatisticalMeasure statistical measure}
	 * "average" and belonging to <i>this</i> quantitative data.
	 * Returns <code>null</code> if no such statistical measurement value instance exists. 
	 */
	@Transient
	public Float getAverage(){
		return getSpecificStatisticalValue(StatisticalMeasure.AVERAGE());
	}

	/** 
	 * Returns the numerical value of the one {@link StatisticalMeasurementValue statistical measurement value}
	 * with the corresponding {@link StatisticalMeasure statistical measure}
	 * "standard deviation" and belonging to <i>this</i> quantitative data.
	 * Returns <code>null</code> if no such statistical measurement value instance exists. 
	 */
	@Transient
	public Float getStandardDeviation(){
		return getSpecificStatisticalValue(StatisticalMeasure.STANDARD_DEVIATION());
	}

	/** 
	 * Returns the numerical value of the one {@link StatisticalMeasurementValue statistical measurement value}
	 * with the corresponding {@link StatisticalMeasure statistical measure}
	 * "sample size" and belonging to <i>this</i> quantitative data.
	 * Returns <code>null</code> if no such statistical measurement value instance exists. 
	 */
	@Transient
	public Float getSampleSize(){
		return getSpecificStatisticalValue(StatisticalMeasure.SAMPLE_SIZE());
	}
	
	
	

	/** 
	 * Returns the numerical value of the one {@link StatisticalMeasurementValue statistical measurement value}
	 * with the corresponding {@link StatisticalMeasure statistical measure}
	 * "typical upper boundary" and belonging to <i>this</i> quantitative data.
	 * Returns <code>null</code> if no such statistical measurement value instance exists. 
	 */
	@Transient
	public Float getTypicalUpperBoundary(){
		return getSpecificStatisticalValue(StatisticalMeasure.TYPICAL_UPPER_BOUNDARY());
	}

	/**
	 * Returns the statistical value of type <code>type</code>.
	 * If no such value exists <code>null</code> is returned. If multiple such
	 * values exist an arbitrary one is returned.
	 * @param type
	 * @return the value
	 */
	public Float getSpecificStatisticalValue(StatisticalMeasure type){
		Float result = null;
		for (StatisticalMeasurementValue value : statisticalValues){
			if (type.equals(value.getType())){
				result = value.getValue();
				break;
			}
		}
		return result;
	}
	

//*********************************** CLONE *****************************************/

	/** 
	 * Clones <i>this</i> quantitative data. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> quantitative data by
	 * modifying only some of the attributes.
	 * 
	 * @see eu.etaxonomy.cdm.model.description.DescriptionElementBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {

		try {
			QuantitativeData result = (QuantitativeData)super.clone();
			
			//states
			result.statisticalValues = new HashSet<StatisticalMeasurementValue>();
			for (StatisticalMeasurementValue data : getStatisticalValues()){
				//TODO do we need to clone here?
				StatisticalMeasurementValue newData = (StatisticalMeasurementValue)data.clone();
				result.statisticalValues.add(newData);
			}
			
			return result;
			//no changes to: unit
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}	

}