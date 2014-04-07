package eu.etaxonomy.cdm.persistence.validation;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.validation.Level2;
import eu.etaxonomy.cdm.validation.Level3;

@SuppressWarnings("serial")
public class EmployeeWithLongRunningValidation extends CdmBase {

	@CheckCase(value = CaseMode.UPPER, groups = { Level2.class })
	private String firstName;
	@CheckCase(value = CaseMode.UPPER, groups = { Level3.class })
	private String lastName;
	@Min(16)
	private int age;
	private double salary;
	@NotNull
	@Valid
	private Company company;
	@NotEmpty
	@Valid
	List<Address> addresses;


	public EmployeeWithLongRunningValidation(@Valid Company company)
	{
		this.company = company;
	}


	public EmployeeWithLongRunningValidation()
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
		// Deliberately primitive equals implementation, because
		// we are using this class to stress-test the ValidationExecutor
		// and we want to be sure it will treat each submitted task as
		// a new task. See EntityValidationTask.equals()
		return false;
	}

}
