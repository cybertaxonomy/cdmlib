package eu.etaxonomy.cdm.persistence.validation;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.validation.Level2;

@SuppressWarnings("serial")
public class Address extends CdmBase {

	@CheckCase(value = CaseMode.UPPER, groups = { Level2.class })
	String street;


	public String getStreet()
	{
		return street;
	}


	public void setStreet(String street)
	{
		this.street = street;
	}


	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		return street.equals(((Address) obj).street);
	}


	public int hashCode()
	{
		return street.hashCode();
	}

}
