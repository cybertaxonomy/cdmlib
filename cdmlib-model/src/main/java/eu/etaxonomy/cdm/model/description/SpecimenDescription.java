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
import javax.persistence.ManyToMany;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

@Entity
public class SpecimenDescription extends DescriptionBase {
	static Logger logger = Logger.getLogger(SpecimenDescription.class);
	

	/**
	 * Factory method
	 * @return
	 */
	public static SpecimenDescription NewInstance(){
		return new SpecimenDescription();
	}
	
	/**
	 * Constructor
	 */
	public SpecimenDescription() {
		super();
	}
	


}
