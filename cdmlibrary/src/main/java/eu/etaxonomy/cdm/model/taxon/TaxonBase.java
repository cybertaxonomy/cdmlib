/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * {unique name within view/treatment}
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:56
 */
public abstract class TaxonBase extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(TaxonBase.class);
	//The assignement to the Taxon or to the Synonym class is not definitive
	private boolean isDoubtful;
	private TaxonNameBase name;
	/**
	 * The concept reference
	 */
	private ReferenceBase sec;

	public TaxonNameBase getName(){
		return this.name;
	}

	/**
	 * 
	 * @param name    name
	 */
	public void setName(TaxonNameBase name){
		this.name = name;
	}

	public boolean isDoubtful(){
		return this.isDoubtful;
	}

	/**
	 * 
	 * @param isDoubtful    isDoubtful
	 */
	public void setDoubtful(boolean isDoubtful){
		this.isDoubtful = isDoubtful;
	}

	public ReferenceBase getSec() {
		return sec;
	}

	public void setSec(ReferenceBase sec) {
		this.sec = sec;
	}

}