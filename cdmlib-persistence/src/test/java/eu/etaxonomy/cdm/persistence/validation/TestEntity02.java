package eu.etaxonomy.cdm.persistence.validation;

import eu.etaxonomy.cdm.validation.Level2;

@SuppressWarnings("serial")
public class TestEntity02 {

	@CheckCase(value = CaseMode.UPPER, groups = { Level2.class })
	private String firstName;
	private String lastName;
	private int age;
	private double salary;


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

}
