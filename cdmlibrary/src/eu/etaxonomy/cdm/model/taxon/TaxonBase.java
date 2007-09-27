/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.publication.PublicationBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:14
 */
@Entity
public abstract class TaxonBase extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(TaxonBase.class);

	private boolean isDoubtful;
	private PublicationBase sec;
	private TaxonName name;

	public TaxonName getName(){
		return name;
	}

	public PublicationBase getSec(){
		return sec;
	}

	public boolean isDoubtful(){
		return isDoubtful;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setDoubtful(boolean newVal){
		isDoubtful = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setName(TaxonName newVal){
		name = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSec(PublicationBase newVal){
		sec = newVal;
	}

}