/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.validation;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.validation.Level2;
/**
 * A Mock class for testing entity validation tasks. DO NOT MODIFY UNLESS YOU ALSO MODIFY
 * THE UNIT TESTS MAKING USE OF THIS CLASS!
 *
 * @author ayco_holleman
 *
 */
@SuppressWarnings("serial")
public class Address extends CdmBase {

	@CheckCase(value = CaseMode.UPPER, groups = { Level2.class })
	String street;


	public String getStreet(){
		return street;
	}


	public void setStreet(String street){
		this.street = street;
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		return street.equals(((Address) obj).street);
	}


	@Override
    public int hashCode(){
		return street.hashCode();
	}

}
