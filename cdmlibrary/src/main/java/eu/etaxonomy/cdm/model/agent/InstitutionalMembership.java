/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;


import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:23
 */
public class InstitutionalMembership extends VersionableEntity {
	static Logger logger = Logger.getLogger(InstitutionalMembership.class);

	//Time period a person belonged to the institution
	@Description("Time period a person belonged to the institution")
	private TimePeriod period;
	//Department of the institution this person was working in
	@Description("Department of the institution this person was working in")
	private String department;
	//Role this person had in the institution
	@Description("Role this person had in the institution")
	private String role;
	/**
	 * current institute the person belongs to
	 */
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

	public TimePeriod getPeriod(){
		return period;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPeriod(TimePeriod newVal){
		period = newVal;
	}

	public String getDepartment(){
		return department;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setDepartment(String newVal){
		department = newVal;
	}

	public String getRole(){
		return role;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setRole(String newVal){
		role = newVal;
	}

}