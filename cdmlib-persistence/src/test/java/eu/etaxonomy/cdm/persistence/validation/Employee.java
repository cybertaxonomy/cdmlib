package eu.etaxonomy.cdm.persistence.validation;

import java.util.List;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.validation.Level2;

@SuppressWarnings("serial")
public class Employee extends CdmBase {

	@LongRunningCheckCase(value = CaseMode.UPPER, groups = { Level2.class })
	private String firstName;
	private String lastName;
	private int age;
	private double salary;
	private Company company;
	List<Address> addresses;


	public Employee(Company company)
	{
		this.company = company;
	}


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


	public int getAge()
	{
		return age;
	}


	public void setAge(int age)
	{
		this.age = age;
	}


	public double getSalary()
	{
		return salary;
	}


	public void setSalary(double salary)
	{
		this.salary = salary;
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

}
