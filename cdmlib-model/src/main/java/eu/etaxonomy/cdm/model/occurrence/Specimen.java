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

import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * A specimen is regarded as derived from an field observation, 
 * so locality and gathering related information is captured as a separate FieldObservation object
 * related to a specimen via a derivation event
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:52
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Specimen", propOrder = {
		"preservation"
})
@XmlRootElement(name = "Specimen")
@Entity
public class Specimen extends DerivedUnitBase implements Cloneable {
	private static final Logger logger = Logger.getLogger(Specimen.class);
	
	@XmlElement(name = "Preservation")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private PreservationMethod preservation;
	
	/**
	 * Factory method
	 * @return
	 */
	public static Specimen NewInstance(){
		return new Specimen();
	}
	
	/**
	 * Constructor
	 */
	protected Specimen() {
		super();
	}
	
	@ManyToOne
	public PreservationMethod getPreservation(){
		return this.preservation;
	}
	public void setPreservation(PreservationMethod preservation){
		this.preservation = preservation;
	}
	
	
//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> specimen. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> specimen
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link DerivedUnitBase DerivedUnitBase}.
	 * 
	 * @see DerivedUnitBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifyableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Specimen clone(){
		try{
			Specimen result = (Specimen)super.clone();
			result.setPreservation(this.preservation);
			//no changes to: -
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
	

}