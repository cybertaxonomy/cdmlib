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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

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
	private Person person;
	
	
	public InstitutionalMembership(Institution institute, Person person, TimePeriod period, String department,
			String role) {
		super();
		this.period = period;
		this.department = department;
		this.role = role;
		this.institute = institute;
		this.person = person;
	}
	
	public Person getPerson() {
		return person;
	}
	public void setPerson(Person newPerson) {
		if (person != null) { 
			person.institutionalMemberships.remove(this);
		}
		if (newPerson!= null) { 
			newPerson.institutionalMemberships.add(this);
		}
		this.person = newPerson;
	}

	
	@Cascade({CascadeType.SAVE_UPDATE})
	public Institution getInstitute(){
		return this.institute;
	}
	public void setInstitute(Institution newInstitute){
		this.institute = newInstitute;
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