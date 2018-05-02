/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.molecular;


import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.hibernate.search.DateTimeBridge;
import eu.etaxonomy.cdm.jaxb.DateTimeAdapter;
import eu.etaxonomy.cdm.model.common.OrderedTerm;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.MaterialOrMethodEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;

/**
 * A DNA Quality describes the quality of a {@link SpecimenOrObservationType#DnaSample}
 *
 * @author a.mueller
 * @since 18-Oct-2014
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DnaQuality", propOrder = {
	"purificationMethod",
	"ratioOfAbsorbance260_230",
    "ratioOfAbsorbance260_280",
    "concentration",
    "concentrationUnit",
    "qualityTerm",
    "qualityCheckDate"
})
@XmlRootElement(name = "DnaQuality")
@Entity
//@Indexed(index = "eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase")
@Audited
public class DnaQuality extends VersionableEntity implements Cloneable {
	private static final long serialVersionUID = -8829069331010573654L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DnaQuality.class);

// ****************** FACTORY METHOD *****************/

	/**
	 * Factory method
	 * @return
	 */
	public static DnaQuality NewInstance(){
		return new DnaQuality();
	}

// ************** ATTRIBUTES ****************************/

    //TODO
//	@XmlElement(name = "PurificationMethod")
//    @XmlIDREF
//    @XmlSchemaType(name = "IDREF")
	@XmlTransient
    @ManyToOne(fetch = FetchType.LAZY)
	//FIXME preliminary as it is not yet decided if we will use a string or a MoME #4552
    private MaterialOrMethodEvent typedPurificationMethod;
	
    private String purificationMethod;


	private Double ratioOfAbsorbance260_230;

	private Double ratioOfAbsorbance260_280;

	private Double concentration;

	@XmlElement(name = "concentrationUnit")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private MeasurementUnit concentrationUnit;


	@XmlElement(name = "Type")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private OrderedTerm qualityTerm;

    @XmlElement (name = "QualityCheckDate", type= String.class)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    @Type(type="dateTimeUserType")
    @Basic(fetch = FetchType.LAZY)
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = DateTimeBridge.class)
    @Audited
    private DateTime qualityCheckDate;


// ******************* CONSTRUCTOR *************************/

	/**
	 * Constructor
	 */
	protected DnaQuality() {}

//************ GETTER / SETTER  **********************************/


	public String getPurificationMethod() {
		return purificationMethod;
	}

	public void setPurificationMethod(String purificationMethod) {
		this.purificationMethod = purificationMethod;
	}

	public Double getRatioOfAbsorbance260_230() {
		return ratioOfAbsorbance260_230;
	}

	public void setRatioOfAbsorbance260_230(Double ratioOfAbsorbance260_230) {
		this.ratioOfAbsorbance260_230 = ratioOfAbsorbance260_230;
	}

	public Double getRatioOfAbsorbance260_280() {
		return ratioOfAbsorbance260_280;
	}

	public void setRatioOfAbsorbance260_280(Double ratioOfAbsorbance260_280) {
		this.ratioOfAbsorbance260_280 = ratioOfAbsorbance260_280;
	}


    public Double getConcentration() {
		return concentration;
	}

	public void setConcentration(Double concentration) {
		this.concentration = concentration;
	}

	public MeasurementUnit getConcentrationUnit() {
		return concentrationUnit;
	}

	public void setConcentrationUnit(MeasurementUnit concentrationUnit) {
		this.concentrationUnit = concentrationUnit;
	}

	public DateTime getQualityCheckDate() {
		return qualityCheckDate;
	}

	public void setQualityCheckDate(DateTime qualityCheckDate) {
		this.qualityCheckDate = qualityCheckDate;
	}

	public OrderedTerm getQualityTerm() {
		return qualityTerm;
	}

	public void setQualityTerm(OrderedTerm qualityTerm) {
		this.qualityTerm = qualityTerm;
	}



//*********** CLONE **********************************/

	/**
	 * Clones <i>this</i> dna quality. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> dna quality
	 * by modifying only some of the attributes.<BR>
	 * @throws CloneNotSupportedException
	 *
	 * @see Specimen#clone()
	 * @see DerivedUnit#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DnaQuality clone() {
		try {
			DnaQuality result = (DnaQuality)super.clone();

			//purification method ??

			//no changes to: rationXXX, concentration, qualityCheckDate, qualityTerm,
			return result;

		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);  //may not occur as Clonable is implemented
		}
	}

}
