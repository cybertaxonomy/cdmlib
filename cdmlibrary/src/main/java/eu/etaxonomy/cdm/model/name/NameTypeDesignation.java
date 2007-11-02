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
 * @created 02-Nov-2007 19:18:26
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
	 * @param typeSpecies
	 */
	public void setTypeSpecies(TaxonNameBase typeSpecies){
		;
	}

	public boolean isRejectedType(){
		return isRejectedType;
	}

	/**
	 * 
	 * @param isRejectedType
	 */
	public void setRejectedType(boolean isRejectedType){
		;
	}

	public boolean isConservedType(){
		return isConservedType;
	}

	/**
	 * 
	 * @param isConservedType
	 */
	public void setConservedType(boolean isConservedType){
		;
	}

}