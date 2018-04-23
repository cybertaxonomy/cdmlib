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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * This class represents the assignment of numerical values to {@link Feature features}
 * corresponding to {@link QuantitativeData quantitative data}. A statistical measurement
 * value instance constitutes an atomized part of an information piece
 * (quantitative data) so that several statistical measurement value instances
 * may belong to one quantitative data instance.
 * <P>
 * This class corresponds to CharacterMeasureDataType according
 * to the SDD schema.
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:54
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StatisticalMeasureValue")
@XmlRootElement(name = "StatisticalMeasureValue")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.description.DescriptionElementBase")
@Audited
public class StatisticalMeasurementValue extends VersionableEntity implements IModifiable, Cloneable{
	private static final long serialVersionUID = -3576311887760351982L;
	private static final Logger logger = Logger.getLogger(StatisticalMeasurementValue.class);

	@XmlElement(name = "Value")
	private float value;

	@XmlElementWrapper(name = "Modifiers")
	@XmlElement(name = "Modifier")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
//	@NotNull // avoids creating a UNIQUE key for this field -> not needed for ManyToMany
	private Set<DefinedTerm> modifiers = new HashSet<>();

	@XmlElement(name = "StatisticalMeasureType")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	private StatisticalMeasure type;

	@XmlElement(name = "QuantitativeData")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY )
    @IndexedEmbedded(depth=1)
	private QuantitativeData quantitativeData;



    /**
	 * Class constructor: creates a new empty statistical measurement value
	 * instance.
	 */
	protected StatisticalMeasurementValue(){
		super();
	}

	/**
	 * Creates a new empty statistical measurement value instance.
	 */
	public static StatisticalMeasurementValue NewInstance(){
		return new StatisticalMeasurementValue();
	}


	/**
	 * Creates a new empty statistical measurement value instance.
	 */
	public static StatisticalMeasurementValue NewInstance(StatisticalMeasure type, float value){
		StatisticalMeasurementValue result = new StatisticalMeasurementValue();
		result.setValue(value);
		result.setType(type);
		return result;
	}

	/**
	 * Returns the type of {@link StatisticalMeasure statistical measure} used in
	 * <i>this</i> statistical measurement value.
	 */
	public StatisticalMeasure getType(){
		return this.type;
	}
	/**
	 * @see	#getType()
	 */
	public void setType(StatisticalMeasure type){
		this.type = type;
	}


	/**
	 * Returns the numerical value used to describe the {@link Feature feature}
	 * corresponding to the {@link QuantitativeData quantitative data} <i>this</i>
	 * statistical measurement value belongs to.
	 */
	public float getValue(){
		return this.value;
	}
	/**
	 * @see	#getValue()
	 */
	public void setValue(float value){
		this.value = value;
	}


    /**
     * Returns the {@link QuantitativeData quantitative data} <i>this</i>
     * statistical measurement value belongs too.
     */
    public QuantitativeData getQuantitativeData() {
        return quantitativeData;
    }

    //for bidirectional use only
    @Deprecated
    protected void setQuantitativeData(QuantitativeData quantitativeData) {
        this.quantitativeData = quantitativeData;
    }


	/**
	 * Returns the set of terms of {@link TermType type} Modifier used to qualify the validity
	 * or probability of <i>this</i> statistical measurement value.
	 * This is only metainformation.
	 */
	@Override
    public Set<DefinedTerm> getModifiers() {
		return modifiers;
	}

	/**
	 * Adds a {@link Modifier modifier} to the set of {@link #getModifiers() modifiers}
	 * used to qualify the validity of <i>this</i> statistical measurement value.
	 *
	 * @param modifier	the modifier to be added to <i>this</i> statistical measurement value
	 * @see    	   		#getModifiers()
	 */
	@Override
    public void addModifier(DefinedTerm modifier) {
		this.modifiers.add(modifier);
	}
	/**
	 * Removes one element from the set of {@link #getModifiers() modifiers}
	 * used to qualify the validity of <i>this</i> statistical measurement value.
	 *
	 * @param  modifier	the modifier which should be removed
	 * @see     		#getModifiers()
	 * @see     		#addModifier(Modifier)
	 */
	@Override
    public void removeModifier(DefinedTerm modifier) {
		this.modifiers.remove(modifier);
	}


//*********************************** CLONE *****************************************/

	/**
	 * Clones <i>this</i> statistical measurement value. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> statistical measurement value by
	 * modifying only some of the attributes.
	 *
	 * @see eu.etaxonomy.cdm.model.common.VersionableEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {

		try {
			StatisticalMeasurementValue result = (StatisticalMeasurementValue)super.clone();

			//modifiers
			result.modifiers = new HashSet<>();
			for (DefinedTerm modifier : getModifiers()){
				result.addModifier(modifier);
			}

			return result;
			//no changes to: value, type
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}

}
