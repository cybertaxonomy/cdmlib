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

/**
 * {only for typified names which have a rank above "species", in this case the
 * type has to be a "species" name}
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:31
 */
public class NameTypeDesignation extends ReferencedEntityBase implements ITypeDesignation {
	static Logger logger = Logger.getLogger(NameTypeDesignation.class);

	@Description("")
	private boolean isRejectedType;
	@Description("")
	private boolean isConservedType;
	private TaxonNameBase typeSpecies;

	public TaxonNameBase getTypeSpecies(){
		return typeSpecies;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTypeSpecies(TaxonNameBase newVal){
		typeSpecies = newVal;
	}

	public boolean isRejectedType(){
		return isRejectedType;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setRejectedType(boolean newVal){
		isRejectedType = newVal;
	}

	public boolean isConservedType(){
		return isConservedType;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setConservedType(boolean newVal){
		isConservedType = newVal;
	}

}