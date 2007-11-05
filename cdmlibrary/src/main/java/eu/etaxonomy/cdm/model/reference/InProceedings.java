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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:11
 */
@Entity
public class InProceedings extends SectionBase {
	static Logger logger = Logger.getLogger(InProceedings.class);

	private Proceedings inProceedings;

	public Proceedings getInProceedings(){
		return inProceedings;
	}

	/**
	 * 
	 * @param inProceedings
	 */
	public void setInProceedings(Proceedings inProceedings){
		;
	}

	@Override
	public String generateTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}