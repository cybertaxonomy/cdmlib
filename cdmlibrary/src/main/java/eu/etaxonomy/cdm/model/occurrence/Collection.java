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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:00
 */
@Entity
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
	 * @param institute
	 */
	public void setInstitute(Institution institute){
		;
	}

	public String getCode(){
		return code;
	}

	/**
	 * 
	 * @param code
	 */
	public void setCode(String code){
		;
	}

	public String getCodeStandard(){
		return codeStandard;
	}

	/**
	 * 
	 * @param codeStandard
	 */
	public void setCodeStandard(String codeStandard){
		;
	}

	public String getName(){
		return name;
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(String name){
		;
	}

	public String getTownOrLocation(){
		return townOrLocation;
	}

	/**
	 * 
	 * @param townOrLocation
	 */
	public void setTownOrLocation(String townOrLocation){
		;
	}

	public String generateTitle(){
		return "";
	}

}