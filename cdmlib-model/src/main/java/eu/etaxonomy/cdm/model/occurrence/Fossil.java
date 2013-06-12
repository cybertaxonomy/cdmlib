/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.springframework.beans.factory.annotation.Configurable;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:25
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Fossil", propOrder = {
})
@XmlRootElement(name = "Fossil")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase")
@Audited
@Configurable
public class Fossil extends Specimen implements Cloneable{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Fossil.class);
	
	/**
	 * Factory method
	 * @return
	 */
	public static Fossil NewInstance(){
		return new Fossil();
	}
	
	/**
	 * Constructor
	 */
	protected Fossil() {
		super();
	}
	

//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> fossil. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> fossil
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link Specimen Specimen}.
	 * 
	 * @see Specimen#clone()
	 * @see DerivedUnitBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Specimen clone(){
		Specimen result = (Specimen)super.clone();
		//no changes to: -
		return result;
	}

}