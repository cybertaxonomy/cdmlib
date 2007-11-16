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
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:23
 */
@Entity
@Table(name="EnumeratedTerm")
public abstract class EnumeratedTermBase extends DefinedTermBase {
	public EnumeratedTermBase() {
		super();
	}

	static Logger logger = Logger.getLogger(EnumeratedTermBase.class);
	private Enumeration enumeration;

	public Enumeration getEnumeration(){
		return this.enumeration;
	}

	/**
	 * Bidirectional relation with EnumeratedTermBase.
	 * 
	 */
	public void setEnumeration(Enumeration newEnumeration){
		if (this.enumeration != null) { 
			this.enumeration.terms.remove(this);
		}
		if (newEnumeration!= null) { 
			this.enumeration.terms.add(this);
		}
		this.enumeration = newEnumeration;		
	}
}