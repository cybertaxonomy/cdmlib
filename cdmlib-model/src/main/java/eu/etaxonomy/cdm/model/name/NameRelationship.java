/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;
import javax.persistence.*;

/**
 * http://rs.tdwg.org/ontology/voc/TaxonName.rdf#NomenclaturalNote
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:37
 */
@Entity
public class NameRelationship extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(NameRelationship.class);
	//The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in
	//the note property.
	private String ruleConsidered;
	private TaxonNameBase fromName;
	private NameRelationshipType type;
	private TaxonNameBase toName;

	private NameRelationship(){
		super();
	}
	
	
	/**
	 * creates a relationship between 2 names and adds this relationship object to the respective name relation sets
	 * @param toName
	 * @param fromName
	 * @param type
	 * @param ruleConsidered
	 */
	protected NameRelationship(TaxonNameBase toName, TaxonNameBase fromName, NameRelationshipType type, String ruleConsidered) {
		super();
		setFromName(fromName);
		setToName(toName);
		setType(type);
		setRuleConsidered(ruleConsidered);
		fromName.addNameRelationship(this);
		toName.addNameRelationship(this);
	}
	
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public TaxonNameBase getFromName(){
		return this.fromName;
	}
	private void setFromName(TaxonNameBase fromName){
		this.fromName = fromName;
	}

	@ManyToOne
	public NameRelationshipType getType(){
		return this.type;
	}
	private void setType(NameRelationshipType type){
		this.type = type;
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public TaxonNameBase getToName(){
		return this.toName;
	}
	private void setToName(TaxonNameBase toName){
		this.toName = toName;
	}

	public String getRuleConsidered(){
		return this.ruleConsidered;
	}
	private void setRuleConsidered(String ruleConsidered){
		this.ruleConsidered = ruleConsidered;
	}

}