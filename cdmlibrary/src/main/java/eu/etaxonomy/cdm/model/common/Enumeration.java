/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * A single enumeration must only contain DefinedTerm instances of one kind
 * (=class)
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:06
 */
@Entity
public class Enumeration extends DefinedTermBase {
	static Logger logger = Logger.getLogger(Enumeration.class);

	//The order of the enumeration list is a linear order that can be used for statistical purposes. Measurement scale =
	//ordinal
	@Description("The order of the enumeration list is a linear order that can be used for statistical purposes. Measurement scale = ordinal")
	private boolean isOrdinal;

	public boolean isOrdinal(){
		return isOrdinal;
	}

	/**
	 * 
	 * @param isOrdinal
	 */
	public void setOrdinal(boolean isOrdinal){
		;
	}

}