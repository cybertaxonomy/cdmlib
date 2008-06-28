/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:51
 */
@Entity
public abstract class SectionBase extends StrictReferenceBase {
	static Logger logger = Logger.getLogger(SectionBase.class);
	private String pages;

	public String getPages(){
		return this.pages;
	}

	/**
	 * 
	 * @param pages    pages
	 */
	public void setPages(String pages){
		this.pages = pages;
	}

	@Transient
	public PrintedUnitBase getPrintedUnit(){
		logger.warn("Not yet implemented");
		return null;
	}

}