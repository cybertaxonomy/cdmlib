/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:13
 */
@Entity
public class SynonymRelationship extends VersionableEntity {
	static Logger logger = Logger.getLogger(SynonymRelationship.class);

	private AcceptedTaxon acceptedTaxon;
	private SynonymRelationshipType type;
	private SynonymTaxon synoynm;

	public AcceptedTaxon getAcceptedTaxon(){
		return acceptedTaxon;
	}

	public SynonymTaxon getSynoynm(){
		return synoynm;
	}

	public SynonymRelationshipType getType(){
		return type;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setAcceptedTaxon(AcceptedTaxon newVal){
		acceptedTaxon = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSynoynm(SynonymTaxon newVal){
		synoynm = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setType(SynonymRelationshipType newVal){
		type = newVal;
	}

}