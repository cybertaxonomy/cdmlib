package eu.etaxonomy.cdm.persistence.validation;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.validation.Level2;
import eu.etaxonomy.cdm.validation.Level3;

@SuppressWarnings("serial")
public class TestEntity03 extends CdmBase {

	@CheckCase(value = CaseMode.UPPER, groups = { Level2.class })
	private String firstName;
	@CheckCase(value = CaseMode.UPPER, groups = { Level3.class })
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
