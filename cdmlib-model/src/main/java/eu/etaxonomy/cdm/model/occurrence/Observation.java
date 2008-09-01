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
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:32
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Observation")
@Entity
public class Observation extends DerivedUnitBase {
	static Logger logger = Logger.getLogger(Observation.class);
	
	/**
	 * Factory method
	 * @return
	 */
	public static Observation NewInstance(){
		return new Observation();
	}
	
	/**
	 * Constructor
	 */
	protected Observation() {
		super();
	}
}