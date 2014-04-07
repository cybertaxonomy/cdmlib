package eu.etaxonomy.cdm.persistence.validation;

import javax.validation.constraints.NotNull;

import eu.etaxonomy.cdm.validation.Level2;

public class Address {

	@NotNull
	@CheckCase(value = CaseMode.UPPER, groups = { Level2.class })
	String street;
	@NotNull
	String streetNo;
	String zip;
	@NotNull
	String city;


	public String getStreet()
	{
		return street;
	}


	public void setStreet(String street)
	{
		this.street = street;
	}


	public String getStreetNo()
	{
		return streetNo;
	}


	public void setStreetNo(String streetNo)
	{
		this.streetNo = streetNo;
	}


	public String getZip()
	{
		return zip;
	}


	public void setZip(String zip)
	{
		this.zip = zip;
	}


	public String getCity()
	{
		return city;
	}


	public void setCity(String city)
	{
		this.city = city;
	}

}
