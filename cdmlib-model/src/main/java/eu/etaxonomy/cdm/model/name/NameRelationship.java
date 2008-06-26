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
 * @created 08-Nov-2007 13:06:37
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NameRelationship", propOrder = {
    "ruleConsidered",
    "type"
})
@Entity
public class NameRelationship extends RelationshipBase<TaxonNameBase, TaxonNameBase, NameRelationshipType> {

  static Logger logger = Logger.getLogger(NameRelationship.class);

    //The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in
	//the note property.
    @XmlElement(name = "RuleConsidered")
	private String ruleConsidered;
	
    @XmlElement(name = "NameRelationshipType")
	private NameRelationshipType type;

	//for hibernate, don't use
	@Deprecated
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
		this(toName, fromName, type, null, null, ruleConsidered);
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
	protected NameRelationship(TaxonNameBase  toName, TaxonNameBase fromName, NameRelationshipType type, ReferenceBase citation, String citationMicroReference, String ruleConsidered) {
		super(fromName, toName, type, citation, citationMicroReference);
		this.setRuleConsidered(ruleConsidered);
	}
	
	@Transient
	public TaxonNameBase getFromName(){
		return super.getRelatedFrom();
	}
	private void setFromName(TaxonNameBase fromName){
		super.setRelatedFrom(fromName);
	}

	@Transient
	public TaxonNameBase getToName(){
		return super.getRelatedTo();
	}
	private void setToName(TaxonNameBase toName){
		super.setRelatedTo(toName);
	}

	public String getRuleConsidered(){
		return this.ruleConsidered;
	}
	private void setRuleConsidered(String ruleConsidered){
		this.ruleConsidered = ruleConsidered;
	}

}