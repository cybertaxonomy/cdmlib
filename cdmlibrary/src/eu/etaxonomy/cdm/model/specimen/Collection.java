/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.specimen;


import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:01
 */
@Entity
public class Collection extends VersionableEntity {
	static Logger logger = Logger.getLogger(Collection.class);

	private String code;
	private String codeStandard;
	private String name;
	private String townOrLocation;
	private Institution institute;

	public String getCode(){
		return code;
	}

	public String getCodeStandard(){
		return codeStandard;
	}

	public Institution getInstitute(){
		return institute;
	}

	public String getName(){
		return name;
	}

	public String getTownOrLocation(){
		return townOrLocation;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCode(String newVal){
		code = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCodeStandard(String newVal){
		codeStandard = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInstitute(Institution newVal){
		institute = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setName(String newVal){
		name = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTownOrLocation(String newVal){
		townOrLocation = newVal;
	}

}