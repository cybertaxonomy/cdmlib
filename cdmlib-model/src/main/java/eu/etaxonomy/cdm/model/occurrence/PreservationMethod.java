/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.occurrence;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.NumericField;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.molecular.Cloning;

/**
 * This class is a specialization of {@link MaterialOrMethodEvent} which allows to
 * specifically store temperature and XXX which are common parameters for preparation.
 *
 * {@link #getDefinedMaterialOrMethod() Defined methods} taken to describe a Preservation Method
 * should be taken from a vocabulary of type {@link TermType#PreservationMethod}
 *
 * http://rs.tdwg.org/ontology/voc/Collection.rdf#SpecimenPreservationMethodTypeTerm
 *
 * @author a.mueller
 * @since 2013-09-11
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PreservationMethod", propOrder = {
	"medium",
	"temperature"
})
@XmlRootElement(name = "PreservationMethod")
@Entity
//TODO @Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class PreservationMethod extends MaterialOrMethodEvent implements Cloneable {
	private static final long serialVersionUID = 2366116167028862401L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PreservationMethod.class);

    @XmlElement(name = "Medium")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @IndexedEmbedded // no depth for terms
    private DefinedTerm medium;

	@XmlElement(name = "Temperature")
	@Field(analyze = Analyze.NO)
	@NumericField
	private Double temperature;


	// ******************** FACTORY METHOD ******************/

	public static PreservationMethod NewInstance(){
		return new PreservationMethod();
	}

    public static PreservationMethod NewInstance(DefinedTerm definedMaterialOrMethod, String methodText){
    	return new PreservationMethod(definedMaterialOrMethod, methodText, null, null);
    }

	public static PreservationMethod NewInstance(DefinedTerm definedMaterialOrMethod, String methodText, DefinedTerm preservationMedium, Double temperature){
		return new PreservationMethod(definedMaterialOrMethod, methodText, preservationMedium, temperature);
	}


	// ********************* CONSTRUCTOR ********************/

	//for hibernate use only
	protected PreservationMethod(){};

    private PreservationMethod(DefinedTerm definedMaterialOrMethod, String methodText, DefinedTerm medium, Double temperature){
    	super(definedMaterialOrMethod, methodText);
    	this.medium = medium;
    	this.temperature = temperature;
    }

	// ********************* GETTER / SETTER ********************/

	public DefinedTerm getMedium() {
		return medium;
	}


	public void setMedium(DefinedTerm medium) {
		this.medium = medium;
	}


	public Double getTemperature() {
		return temperature;
	}


	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}

	// ********************* CLONE ********************/
	/**
	 * Clones <i>this</i> {@link Cloning}. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> cloning by
	 * modifying only some of the attributes.<BR><BR>
	 *
	 * @see EventBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone()  {
		PreservationMethod result = (PreservationMethod)super.clone();

		//don't change medium, temperature
		return result;
	}
}
