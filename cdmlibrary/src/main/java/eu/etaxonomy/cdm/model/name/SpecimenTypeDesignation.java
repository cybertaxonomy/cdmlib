/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.occurrence.ObservationalUnit;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import org.apache.log4j.Logger;

/**
 * {only for typified names which have the "species" rank or below}
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:39
 */
public class SpecimenTypeDesignation extends ReferencedEntityBase implements ITypeDesignation {
	static Logger logger = Logger.getLogger(SpecimenTypeDesignation.class);

	private ObservationalUnit typeSpecimen;
	private TypeDesignationStatus typeStatus;

	public ObservationalUnit getTypeSpecimen(){
		return typeSpecimen;
	}

	/**
	 * 
	 * @param typeSpecimen
	 */
	public void setTypeSpecimen(ObservationalUnit typeSpecimen){
		;
	}

	public TypeDesignationStatus getTypeStatus(){
		return typeStatus;
	}

	/**
	 * 
	 * @param typeStatus
	 */
	public void setTypeStatus(TypeDesignationStatus typeStatus){
		;
	}

}