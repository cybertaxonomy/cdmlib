/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.specimen.Specimen;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:17
 */
@Entity
public class TypeDesignation extends VersionableEntity {
	static Logger logger = Logger.getLogger(TypeDesignation.class);

	private Specimen typeSpecimen;
	private TypeDesignationStatus typeStatus;

	public Specimen getTypeSpecimen(){
		return typeSpecimen;
	}

	public TypeDesignationStatus getTypeStatus(){
		return typeStatus;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTypeSpecimen(Specimen newVal){
		typeSpecimen = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTypeStatus(TypeDesignationStatus newVal){
		typeStatus = newVal;
	}

}