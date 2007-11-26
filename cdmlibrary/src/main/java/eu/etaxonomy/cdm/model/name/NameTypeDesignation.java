/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * {only for typified names which have a rank above "species", in this case the
 * type has to be a "species" name}
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:38
 */
@Entity
public class NameTypeDesignation extends TypeDesignationBase {
	static Logger logger = Logger.getLogger(NameTypeDesignation.class);
	private boolean isRejectedType;
	private boolean isConservedType;
	private TaxonNameBase typeSpecies;

	@ManyToOne
	public TaxonNameBase getTypeSpecies(){
		return this.typeSpecies;
	}
	public void setTypeSpecies(TaxonNameBase typeSpecies){
		this.typeSpecies = typeSpecies;
	}

	public boolean isRejectedType(){
		return this.isRejectedType;
	}

	/**
	 * 
	 * @param isRejectedType    isRejectedType
	 */
	public void setRejectedType(boolean isRejectedType){
		this.isRejectedType = isRejectedType;
	}

	public boolean isConservedType(){
		return this.isConservedType;
	}

	/**
	 * 
	 * @param isConservedType    isConservedType
	 */
	public void setConservedType(boolean isConservedType){
		this.isConservedType = isConservedType;
	}

}