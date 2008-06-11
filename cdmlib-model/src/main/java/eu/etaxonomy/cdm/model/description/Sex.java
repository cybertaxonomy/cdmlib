/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import org.apache.log4j.Logger;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:52
 */
@Entity
public class Sex extends Scope {
	static Logger logger = Logger.getLogger(Sex.class);


	/**
	 * Factory method
	 * @return
	 */
	public static Sex NewInstance(){
		return new Sex();
	}
	
	/**
	 * Constructor
	 */
	public Sex() {
		super();
	}

	public Sex(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

}