package eu.etaxonomy.cdm.persistence.validation;

import java.util.List;

import javax.validation.Valid;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.validation.Level2;
import eu.etaxonomy.cdm.validation.Level3;

/**
 * A Mock class for testing entity validation tasks.
 * 
 * @author ayco_holleman
 * 
 */
@SuppressWarnings("serial")
public class Employee extends CdmBase {

	@CheckCase(value = CaseMode.UPPER, groups = { Level2.class })
	private String firstName;
	@CheckCase(value = CaseMode.UPPER, groups = { Level3.class })
	private String lastName;
	@Valid
	private Company company;
	@Valid
	List<Address> addresses;


	public Employee()
	{
	}


	public String getFirstName()
	{
		return firstName;
	}


	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}


	public String getLastName()
	{
		return lastName;
	}


	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}


	public Company getCompany()
	{
		return company;
	}


	public void setCompany(Company company)
	{
		this.company = company;
	}


	public List<Address> getAddresses()
	{
		return addresses;
	}


	public void setAddresses(List<Address> addresses)
	{
		this.addresses = addresses;
	}


	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		Employee emp = (Employee) obj;
		return firstName.equals(emp.firstName) && lastName.equals(emp.lastName);
	}


	public int hashCode()
	{
		int hash = 17;
		hash = (hash * 31) + firstName.hashCode();
		hash = (hash * 31) + lastName.hashCode();
		return hash;
	}

}
