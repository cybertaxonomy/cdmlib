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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:23
 */
@MappedSuperclass
public abstract class OrderedTermBase extends DefinedTermBase {
	static Logger logger = Logger.getLogger(OrderedTermBase.class);
	private TermVocabulary enumeration;


	public OrderedTermBase(String term, String label, TermVocabulary enumeration) {
		super(term, label);
		setEnumeration(enumeration);
	}

	
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public TermVocabulary getEnumeration(){
		return this.enumeration;
	}
	public void setEnumeration(TermVocabulary newEnumeration){
		if (this.enumeration != null) { 
			this.enumeration.terms.remove(this);
		}
		if (newEnumeration!= null) { 
			newEnumeration.terms.add(this);
		}
		this.enumeration = newEnumeration;		
	}
}