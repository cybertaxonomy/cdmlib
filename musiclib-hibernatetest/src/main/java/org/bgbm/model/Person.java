/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package org.bgbm.model;



import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * http://rs.tdwg.org/ontology/voc/Person.rdf
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:42
 */
@Entity
public class Person extends MetaBase{
	static Logger logger = Logger.getLogger(Person.class);

	private String firstname;
	private String lastname;
	private Set<Band> bands;
	
	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE})
	public Set<Band> getBands() {
		return bands;
	}
	public void setBands(Set<Band> bands) {
		this.bands = bands;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

}