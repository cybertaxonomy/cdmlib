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
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:29
 */
@Entity
public class InProceedings extends SectionBase {
	private static final Logger logger = Logger.getLogger(InProceedings.class);
	private Proceedings inProceedings;

	public static InProceedings NewInstance(){
		InProceedings result = new InProceedings();
		return result;
	}
	
	public static InProceedings NewInstance(Proceedings inProceedings){
		InProceedings result = NewInstance();
		result.setInProceedings(inProceedings);
		return result;
	}
	
	
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
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