/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import javax.persistence.Entity;

import org.apache.log4j.Logger;

/**
 * This class represents personal communications. A personal communication is a
 * non published document originally written for information exchange between
 * private persons. 
 * <P>
 * This class corresponds to: <ul>
 * <li> the term "Communication" from PublicationTypeTerm according to the TDWG ontology
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:43
 */
@Entity
public class PersonalCommunication extends StrictReferenceBase implements Cloneable {
	private static final Logger logger = Logger.getLogger(PersonalCommunication.class);


	/**
	 * Creates a new empty personal communication instance.
	 */
	public static PersonalCommunication NewInstance(){
		PersonalCommunication result = new PersonalCommunication();
		return result;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.PublicationBase#clone()
	 */
	@Override
	public PersonalCommunication clone(){
		PersonalCommunication result = (PersonalCommunication)super.clone();
		//no changes to: -
		return result;
	}
}