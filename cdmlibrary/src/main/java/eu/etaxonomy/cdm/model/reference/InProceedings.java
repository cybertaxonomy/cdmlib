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
 * @created 08-Nov-2007 13:06:29
 */
@Entity
public class InProceedings extends SectionBase {
	static Logger logger = Logger.getLogger(InProceedings.class);
	private Proceedings inProceedings;

	@ManyToOne
	public Proceedings getInProceedings(){
		return this.inProceedings;
	}

	/**
	 * 
	 * @param inProceedings    inProceedings
	 */
	public void setInProceedings(Proceedings inProceedings){
		this.inProceedings = inProceedings;
	}

	@Override
	public String generateTitle(){
		return "";
	}

}