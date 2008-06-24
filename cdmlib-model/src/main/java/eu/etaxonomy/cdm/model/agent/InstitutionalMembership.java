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

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Allows to hold one {@link Institution institution} to which a {@link Person person} is affiliated.
 * It includes {@link common.TimePeriod time period} of membership and role of the person
 * in this institution. In case one person belongs to several institutions
 * the corresponding number of instances of InstitutionalMembership
 * have to be created.  
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:30
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "period",
    "department",
    "role",
    "institute",
    "person"
})
@XmlRootElement(name = "InstitutionalMembership")
@Entity
public class InstitutionalMembership extends VersionableEntity {
	
	static Logger logger = Logger.getLogger(InstitutionalMembership.class);
	
	/*Time period a person belonged to the institution*/
    @XmlElement(name = "Period")
    //@XmlJavaTypeAdapter(IntervalAdapter.class)
	private TimePeriod period;
	
	//Department of the institution this person was working in
    @XmlElement(name = "Department")
	private String department;
	
	//Role this person had in the institution
    @XmlElement(name = "Role")
	private String role;
	
	//current institute the person belongs to
    @XmlElement(name = "Institution", required = true)
    //@XmlIDREF
    //@XmlSchemaType(name = "IDREF")
	private Institution institute;
	
    @XmlElement(name = "Person", required = true)
    //@XmlIDREF
    //@XmlSchemaType(name = "IDREF")
	private Person person;
	
	public static InstitutionalMembership NewInstance() {
		InstitutionalMembership mship = new InstitutionalMembership();
		return mship;
	}

	public InstitutionalMembership() {
		super();
	}

	/** 
	 * Class constructor using an {@link Institution institution}, a {@link Person person}, a {@link common.TimePeriod time period},
	 * a department name string and a role string.
	 *
	 * @param  institute   the institution in which the person is a member
	 * @param  person      the person who is a member of the institution
	 * @param  period  	   the time period during which the person belonged
	 * 					   to the institution
	 * @param  department  the name string of the department (within the institution)
	 * 					   this person is working in
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
	
	/** 
	 * Returns the {@link Person person} involved in this institutional membership.
	 *
	 * @see  Person#institutionalMemberships
	 * @see  Person#addInstitutionalMembership(Institution, TimePeriod, String, String)
	 */
	public Person getPerson() {
		return person;
	}
	/** 
	 * Assigns a new {@link Person person} (replacing the actual one) to this institutional membership.
	 * This method also updates both sets of institutions
	 * the two persons (the new one and the substituted one) belong to. 
	 *
	 * @param  newPerson  the new person to be included in this institutional membership
	 * @see               #getPerson()
	 * @see               Person#removeInstitutionalMembership(InstitutionalMembership)
	 */
	protected void setPerson(Person newPerson) {
		this.person = newPerson;
	}

	
	/** 
	 * Returns the {@link Institution institution} corresponding to this institutional membership.
	 */
	@Cascade({CascadeType.SAVE_UPDATE})
	public Institution getInstitute(){
		return this.institute;
	}
	/** 
	 * Assigns an new institution (replacing the actual one)
	 * to this institutional membership.
	 *
	 * @param  newInstitute  the new institution
	 * @see	   				 #getInstitute()
	 */
	public void setInstitute(Institution newInstitute){
		this.institute = newInstitute;
	}

	/** 
	 * Returns the {@link common.TimePeriod time period} during which
	 * the {@link Person person} involved in this institutional membership belonged
	 * to the {@link Institution institution} also involved in it.
	 */
	public TimePeriod getPeriod(){
		return this.period;
	}

	/**
	 * @see	#getPeriod()
	 */
	public void setPeriod(TimePeriod period){
		this.period = period;
	}

	/**
	 * Returns the string representing the name of the department (within
	 * the {@link Institution institution} involved in this institutional membership) to which
	 * the {@link Person person} belongs.
	 */
	public String getDepartment(){
		return this.department;
	}

	/**
	 * @see	#getDepartment()
	 */
	public void setDepartment(String department){
		this.department = department;
	}

	/**
	 * Returns the string representing the role played by the {@link Person person} within
	 * the {@link Institution institution} (or within the department) involved
	 * in this institutional membership.
	 */
	public String getRole(){
		return this.role;
	}

	/**
	 * @see	#getRole()
	 */
	public void setRole(String role){
		this.role = role;
	}

}