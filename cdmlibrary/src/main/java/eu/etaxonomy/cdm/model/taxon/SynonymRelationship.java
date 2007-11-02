/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:35
 */
@Entity
public class SynonymRelationship extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(SynonymRelationship.class);

	private Synonym synoynm;
	private Taxon acceptedTaxon;
	private SynonymRelationshipType type;

	public Taxon getAcceptedTaxon(){
		return acceptedTaxon;
	}

	/**
	 * 
	 * @param acceptedTaxon
	 */
	public void setAcceptedTaxon(Taxon acceptedTaxon){
		;
	}

	public SynonymRelationshipType getType(){
		return type;
	}

	/**
	 * 
	 * @param type
	 */
	public void setType(SynonymRelationshipType type){
		;
	}

	public Synonym getSynoynm(){
		return synoynm;
	}

	/**
	 * 
	 * @param synoynm
	 */
	public void setSynoynm(Synonym synoynm){
		;
	}

}