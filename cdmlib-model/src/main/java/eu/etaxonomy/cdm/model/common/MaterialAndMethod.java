/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

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

import eu.etaxonomy.cdm.model.common.AnnotatableEntity;

/**
 * @author a.mueller
 * @created 2013-07-08
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MaterialOrMethod", propOrder = {
	"materialMethodTerm",
	"materialMethodText"
})
@XmlRootElement(name = "MaterialOrMethod")
@Entity
@Audited
//TODO which base class  (..., identifiable, definedTerm, ...)
public class MaterialAndMethod extends AnnotatableEntity {
	private static final long serialVersionUID = -4799205199942053585L;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MaterialAndMethod.class);
	
    @XmlElement(name = "MaterialMethodTerm")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
	private DefinedTerm materialMethodTerm;
	
    @XmlElement(name = "MaterialMethodText")
    private String materialMethodText;

	
	
	//TODO citation / link

	
// ******************** FACTORY METHOD ******************/	
	
// ********************* CONSTRUCTOR ********************/
	
// ********************* GETTER / SETTER ********************/
	
	/**
	 * A freetext describing the material or method or if
	 * a {@link #getMaterialMethodTerm() defined method} is given
	 * an additional information about how this method was used.
	 */
	public String getMaterialMethodText() {
		return materialMethodText;
	}

	/**
	 * @see #getMaterialMethodText()
	 */
	public void setMaterialMethodText(String materialMethodText) {
		this.materialMethodText = materialMethodText;
	}

	public DefinedTerm getMaterialMethodTerm() {
		return materialMethodTerm;
	}

	public void setMaterialMethodTerm(DefinedTerm materialMethodTerm) {
		this.materialMethodTerm = materialMethodTerm;
	}
	
// ********************* CLONE ********************/
	
}
