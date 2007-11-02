/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:04
 */
@Entity
public class Determination extends AnnotatableEntity {
	static Logger logger = Logger.getLogger(Determination.class);

	@Description("")
	private Calendar identificationDate;
	private Team identifierTeam;
	private Taxon taxon;

	public Taxon getTaxon(){
		return taxon;
	}

	/**
	 * 
	 * @param taxon
	 */
	public void setTaxon(Taxon taxon){
		;
	}

	public Team getIdentifierTeam(){
		return identifierTeam;
	}

	/**
	 * 
	 * @param identifierTeam
	 */
	public void setIdentifierTeam(Team identifierTeam){
		;
	}

	public Calendar getIdentificationDate(){
		return identificationDate;
	}

	/**
	 * 
	 * @param identificationDate
	 */
	public void setIdentificationDate(Calendar identificationDate){
		;
	}

}