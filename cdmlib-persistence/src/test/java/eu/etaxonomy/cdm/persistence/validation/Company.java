package eu.etaxonomy.cdm.persistence.validation;

import javax.validation.constraints.NotNull;

import eu.etaxonomy.cdm.validation.Level2;

public class Company {

	@NotNull
	@CheckCase(value = CaseMode.UPPER, groups = { Level2.class })
	private String name;


	public String getName()
	{
		return name;
	}


	public void setName(String name)
	{
		this.name = name;
	}

}
