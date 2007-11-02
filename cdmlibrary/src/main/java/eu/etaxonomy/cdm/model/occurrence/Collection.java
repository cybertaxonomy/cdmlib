/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:11
 */
public class Collection extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(Collection.class);

	@Description("")
	private String code;
	@Description("")
	private String codeStandard;
	@Description("")
	private String name;
	@Description("")
	private String townOrLocation;
	private Institution institute;

	public Institution getInstitute(){
		return institute;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInstitute(Institution newVal){
		institute = newVal;
	}

	public String getCode(){
		return code;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCode(String newVal){
		code = newVal;
	}

	public String getCodeStandard(){
		return codeStandard;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCodeStandard(String newVal){
		codeStandard = newVal;
	}

	public String getName(){
		return name;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setName(String newVal){
		name = newVal;
	}

	public String getTownOrLocation(){
		return townOrLocation;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTownOrLocation(String newVal){
		townOrLocation = newVal;
	}

	public String generateTitle(){
		return "";
	}

}