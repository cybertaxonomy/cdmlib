/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.occurrence;


import etaxonomy.cdm.model.taxon.Taxon;
import etaxonomy.cdm.model.agent.Team;
import etaxonomy.cdm.model.common.AnnotatableEntity;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:14:45
 */
public class Determination extends AnnotatableEntity {
	static Logger logger = Logger.getLogger(Determination.class);

	@Description("")
	private Calendar identificationDate;
	private Taxon taxon;
	private Team identifierTeam;

	public Taxon getTaxon(){
		return taxon;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTaxon(Taxon newVal){
		taxon = newVal;
	}

	public Team getIdentifierTeam(){
		return identifierTeam;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setIdentifierTeam(Team newVal){
		identifierTeam = newVal;
	}

	public Calendar getIdentificationDate(){
		return identificationDate;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setIdentificationDate(Calendar newVal){
		identificationDate = newVal;
	}

}