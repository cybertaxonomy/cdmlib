/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import org.apache.log4j.Logger;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * http://rs.tdwg.org/ontology/voc/TaxonName.rdf#NomenclaturalNote
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:26
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HybridRelationship", propOrder = {
    "ruleConsidered"
})
@Entity
public class HybridRelationship extends RelationshipBase<BotanicalName, BotanicalName, HybridRelationshipType> {
  
	private static final Logger logger = Logger.getLogger(HybridRelationship.class);
	
	//The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in
	//the note property.
	@XmlElement(name = "RuleConsidered")
	private String ruleConsidered;

	//for hibernate, don't use
	@Deprecated
	private HybridRelationship(){
		super();
	}

	
	/**
	 * creates a relationship between 2 names and adds this relationship object to the respective name relation sets
	 * @param toName
	 * @param fromName
	 * @param type
	 * @param ruleConsidered
	 */
	protected HybridRelationship(BotanicalName hybridName, BotanicalName parentName, HybridRelationshipType type, String ruleConsidered) {
		this(parentName, hybridName, type, null, null, ruleConsidered);
	}
	
	/**
	 * Constructor that adds immediately a relationship instance to both 
	 * Creates a relationship between 2 names and adds this relationship object to the respective name relation sets
	 * @param toName
	 * @param fromName
	 * @param type
	 * @param citation
	 * @param citationMicroReference
	 * @param ruleConsidered
	 */
	protected HybridRelationship(BotanicalName  hybridName, BotanicalName parentName, HybridRelationshipType type, ReferenceBase citation, String citationMicroReference, String ruleConsidered) {
		super(parentName, hybridName, type, citation, citationMicroReference);
		this.setRuleConsidered(ruleConsidered);
	}	
	
	public BotanicalName getParentName(){
		return super.getRelatedFrom();
	}
	public void setParentName(BotanicalName parentName){
		super.setRelatedFrom(parentName);
	}

	public BotanicalName getHybridName(){
		return super.getRelatedTo();
	}
	public void setHybridName(BotanicalName hybridName){
		super.setRelatedTo(hybridName);
	}

	public String getRuleConsidered(){
		return this.ruleConsidered;
	}
	public void setRuleConsidered(String ruleConsidered){
		this.ruleConsidered = ruleConsidered;
	}

}