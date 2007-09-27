/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.publication.PublicationBase;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:08
 */
@Entity
public class NameRelationship extends VersionableEntity {
	static Logger logger = Logger.getLogger(NameRelationship.class);

	private String citationMicroReference;
	private String ruleConsidered;
	private PublicationBase citation;
	private NameRelationshipType type;
	private TaxonName toName;
	private TaxonName fromName;

	@Transient
	public PublicationBase getCitation(){
		return citation;
	}

	public String getCitationMicroReference(){
		return citationMicroReference;
	}

	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
	public TaxonName getFromName(){
		return fromName;
	}

	public String getRuleConsidered(){
		return ruleConsidered;
	}

	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
	public TaxonName getToName(){
		return toName;
	}

	public NameRelationshipType getType(){
		return type;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCitation(PublicationBase newVal){
		citation = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCitationMicroReference(String newVal){
		citationMicroReference = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setFromName(TaxonName newVal){
		fromName = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setRuleConsidered(String newVal){
		ruleConsidered = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setToName(TaxonName newVal){
		toName = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setType(NameRelationshipType newVal){
		type = newVal;
	}

}