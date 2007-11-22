/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.taxon.Taxon;

import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:21
 */
@Entity
public class Determination extends AnnotatableEntity {
	static Logger logger = Logger.getLogger(Determination.class);
	private Calendar identificationDate;
	private Agent determiner;
	private IdentifiableEntity taxon;
	private DeterminationModifier modifier;

	public DeterminationModifier getModifier() {
		return modifier;
	}

	public void setModifier(DeterminationModifier modifier) {
		this.modifier = modifier;
	}

	public IdentifiableEntity getTaxon(){
		return this.taxon;
	}

	/**
	 * 
	 * @param taxon    taxon
	 */
	public void setTaxon(IdentifiableEntity taxon){
		this.taxon = taxon;
	}


	@Temporal(TemporalType.DATE)
	public Calendar getIdentificationDate(){
		return this.identificationDate;
	}

	/**
	 * 
	 * @param identificationDate    identificationDate
	 */
	public void setIdentificationDate(Calendar identificationDate){
		this.identificationDate = identificationDate;
	}

	protected Agent getDeterminer() {
		return determiner;
	}

	protected void setDeterminer(Agent determiner) {
		this.determiner = determiner;
	}

}