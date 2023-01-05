/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.validation;

import javax.validation.constraints.NotNull;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.validation.Level2;

/**
 * Mock class for validating entity validation tasks. DO NOT MODIFY UNLESS YOU ALSO MODIFY
 * THE UNIT TESTS MAKING USE OF THIS CLASS!
 * 
 * @author ayco_holleman
 * 
 */
@SuppressWarnings("serial")
public class Company extends CdmBase {

	@NotNull
	@CheckCase(value = CaseMode.UPPER, groups = { Level2.class })
	private String name;

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}


	public boolean equals(Object obj){
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		Company other = (Company) obj;
		return name.equals(other.name);
	}


	public int hashCode(){
		return name.hashCode();
	}

}
