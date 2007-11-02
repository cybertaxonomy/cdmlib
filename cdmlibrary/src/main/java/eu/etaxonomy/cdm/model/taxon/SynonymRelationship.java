/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.taxon;


import etaxonomy.cdm.model.common.ReferencedEntityBase;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:20
 */
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
	 * @param newVal
	 */
	public void setAcceptedTaxon(Taxon newVal){
		acceptedTaxon = newVal;
	}

	public SynonymRelationshipType getType(){
		return type;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setType(SynonymRelationshipType newVal){
		type = newVal;
	}

	public Synonym getSynoynm(){
		return synoynm;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSynoynm(Synonym newVal){
		synoynm = newVal;
	}

}