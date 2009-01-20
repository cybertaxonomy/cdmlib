/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * This class represents descriptions for {@link SpecimenOrObservationBase specimens or observations}.
 * <P>
 * This class corresponds to DescriptionsBaseType with an "Object" element
 * according to the SDD schema.
 *  
 * @author a.mueller
 * @version 1.0
 * @created 08-Jul-2008
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpecimenDescription")
@XmlRootElement(name = "SpecimenDescription")
@Entity
//@Audited
public class SpecimenDescription extends DescriptionBase {
	private static final long serialVersionUID = -8506790426682192703L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SpecimenDescription.class);
	

	/**
	 * Class constructor: creates a new empty specimen description instance.
	 */
	public SpecimenDescription() {
		super();
	}
	

	/**
	 * Creates a new empty specimen description instance.
	 */
	public static SpecimenDescription NewInstance(){
		return new SpecimenDescription();
	}
	

}
