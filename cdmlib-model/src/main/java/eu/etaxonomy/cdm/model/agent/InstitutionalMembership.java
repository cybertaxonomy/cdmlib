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
 * Allows to hold one {@link Institution institution} to which a {@link Person person} is affiliated.
 * It includes time period of membership and role of the person
 * in this institution. In case one person belongs to several institutions
 * the corresponding number of instances have to be created.  
 * 
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
	
	
	/** 
	 * Class constructor using an institution, a person, a time period,
	 * a department name string and a role string.
	 *
	 * @param  institute   the institution in which the person is a member
	 * @param  person      the person who is a member of the institution
	 * @param  period  	   the time period during which the person belonged
	 * 					   to the institution
	 * @param  department  the name string of the department (within the institution)
	 * 					   this person was working in
	 * @param  role  	   the string which identifies the role played by the person
	 * 					   in the institution (or in the department)
	 * @see                Person
	 * @see                Institution
	 */
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
	/** 
	 * Assigns a new person to this institutional membership.
	 * This method also updates the set of institutions
	 * the two persons (the new one and the substituted one) belong to. 
	 *
	 * @param  newPerson  the new person involved in this institutional membership
	 * @see               Person#institutionalMemberships
	 * @see               Person#removeInstitutionalMembership(InstitutionalMembership)
	 */
	public void setPerson(Person newPerson) {
		// Hibernate bidirectional cascade hack: 
		// http://opensource.atlassian.com/projects/hibernate/browse/HHH-1054
		if(this.person == newPerson) return;
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
	/** 
	 * Assigns an institution to this institutional membership.
	 *
	 * @param  newInstitute  the institution involved in this institutional membership
	 */
	public void setInstitute(Institution newInstitute){
		this.institute = newInstitute;
	}

	public TimePeriod getPeriod(){
		return this.period;
	}

	/**
	 * Assigns a time period applying for this institutional membership.
	 * 
	 * @param period  the time period during which the person belonged
	 * 				  to the institution
	 */
	public void setPeriod(TimePeriod period){
		this.period = period;
	}

	public String getDepartment(){
		return this.department;
	}

	/**
	 * Assigns a department within the institution involved
	 * in this institutional membership.
	 * 
	 * @param department  the string for the department name within the institution
	 */
	public void setDepartment(String department){
		this.department = department;
	}

	public String getRole(){
		return this.role;
	}

	/**
	 * Assigns a role to the person within the institution (or department).
	 * 
	 * @param role  the string which identifies the role played by the person
	 * 				within the institution (or within the department)
	 */
	public void setRole(String role){
		this.role = role;
	}

}