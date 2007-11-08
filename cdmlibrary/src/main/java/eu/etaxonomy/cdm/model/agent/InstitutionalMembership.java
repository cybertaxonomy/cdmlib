/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;


import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:30
 */
@Entity
public class InstitutionalMembership extends VersionableEntity {
	static Logger logger = Logger.getLogger(InstitutionalMembership.class);
	//Time period a person belonged to the institution
	private TimePeriod period;
	//Department of the institution this person was working in
	private String department;
	//Role this person had in the institution
	private String role;
	//current institute the person belongs to
	private Institution institute;

	public Institution getInstitute(){
		return this.institute;
	}

	/**
	 * 
	 * @param institute    institute
	 */
	public void setInstitute(Institution institute){
		this.institute = institute;
	}

	public TimePeriod getPeriod(){
		return this.period;
	}

	/**
	 * 
	 * @param period    period
	 */
	public void setPeriod(TimePeriod period){
		this.period = period;
	}

	public String getDepartment(){
		return this.department;
	}

	/**
	 * 
	 * @param department    department
	 */
	public void setDepartment(String department){
		this.department = department;
	}

	public String getRole(){
		return this.role;
	}

	/**
	 * 
	 * @param role    role
	 */
	public void setRole(String role){
		this.role = role;
	}

}