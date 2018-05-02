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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.molecular.Cloning;

/**
 * A material or method event handles data on materials or methods used for handling specimen or derived units
 * in general. It stores information on what material or method was used, who used it and when it was used.
 * For reusable data on materials or methods it is best practice to define these first as {@link DefinedTerm
 * defined terms} of type {@link TermType#MaterialOrMethod} TODO and then use this term as {@link #getDefinedMaterialOrMethod()
 * material or method term}. If this is not possible or if additional data needs to be added one may also
 * use {@link #getDescription() freetext} field inherited from {@link EventBase}. Actor and Date information
 * are also handled via {@link EventBase} fields.
 * This class may be extended by more specific classes which require structured handling of additional parameters.
 *
 * In general material or method data is not considered to be CDM core data. Therefore the decision was made to handle
 * all the data with a common base class which is {@link MaterialOrMethodEvent} to reduce the number of tables required
 * in the underlying databases.
 *
 * @author a.mueller
 * @since 2013-07-08
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MaterialOrMethod", propOrder = {
	"definedMaterialOrMethod"
})
@XmlRootElement(name = "MaterialOrMethod")
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Audited
public class MaterialOrMethodEvent extends EventBase implements Cloneable{
	private static final long serialVersionUID = -4799205199942053585L;
	private static final Logger logger = Logger.getLogger(MaterialOrMethodEvent.class);

    @XmlElement(name = "DefinedMaterialOrMethod")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @IndexedEmbedded // no depth for terms
	private DefinedTerm definedMaterialOrMethod;

	//TODO citation / link


// ******************** FACTORY METHOD ******************/

    public static MaterialOrMethodEvent NewInstance(){
    	return new MaterialOrMethodEvent();
    }

    public static MaterialOrMethodEvent NewInstance(DefinedTerm definedMaterialOrMethod, String methodText){
    	return new MaterialOrMethodEvent(definedMaterialOrMethod, methodText);
    }

// ********************* CONSTRUCTOR ********************/

    protected MaterialOrMethodEvent(){};

    protected MaterialOrMethodEvent(DefinedTerm definedMaterialOrMethod, String methodText){
    	this.definedMaterialOrMethod = definedMaterialOrMethod;
    	this.setDescription(methodText);
    }


// ********************* GETTER / SETTER ********************/


	/**
	 * The {@link #getDescription()} method is inherited from {@link EventBase}.
	 * In this class it is used as freetext describing the material or method used
	 * or if a {@link #getDefinedMaterialOrMethod() defined method} is given as
	 * an additional information about how this defined method was used.
	 *
	 * @see #getMaterialMethodText()
	 */
    @Override
	public String getDescription() {
		return super.getDescription();
	}


	/**
	 * @see #getDescription()
	 * @see #setMaterialMethodText(String)
	 */
    @Override
	public void setDescription(String materialMethodText) {
		super.setDescription(materialMethodText);
	}



	/**
	 * A freetext describing the material or method or if
	 * a {@link #getDefinedMaterialOrMethod() defined method} is given
	 * an additional information about how this method was used.
	 * In future this method could be removed to decrease the number
	 * of transient getters in the CDM.
	 */
	@Transient
    public String getMaterialMethodText() {
		return this.getDescription();
	}


	/**
	 * @see #getMaterialMethodText()
	 */
	public void setMaterialMethodText(String materialMethodText) {
		this.setDescription(materialMethodText);
	}


	/**
	 * A defined material or method given as a defined term in a materialOrMethod
	 * {@link TermVocabulary term vocabulary}. If such a defined material or method is used
	 * the {@link #getDescription() description} should primarily focus on describing
	 * deviation from this method rather then repeating it.
	 *
	 * @see #getDescription()
	 * @see #getMaterialMethodText()
	 * @return the material or method term
	 */
	public DefinedTerm getDefinedMaterialOrMethod() {
		return definedMaterialOrMethod;
	}

	/**
	 * @see #getDefinedMaterialOrMethod()
	 * @param materialMethodTerm
	 */
	public void setDefinedMaterialOrMethod(DefinedTerm definedMaterialOrMethod) {
		this.definedMaterialOrMethod = definedMaterialOrMethod;
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
		try{
			MaterialOrMethodEvent result = (MaterialOrMethodEvent)super.clone();

			//don't change materialMethodTerm
			return result;
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}
