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
 * The class representing a hybrid relationship between one of the {@link BotanicalName parents}
 * of a hybrid taxon name and the hybrid taxon name itself. A hybrid taxon name
 * is a {@link BotanicalName botanical taxon name} assigned to a hybrid plant following
 * the {@link NomenclaturalCode#ICBN() ICBN} (Appendix I). A hybrid taxon name must have one 
 * of the hybrid flags set. The hybrid relationship includes a {@link HybridRelationshipType hybrid relationship type}
 * (for instance "first parent" or "female parent") and the article of the ICBN
 * on which the hybrid taxon name relies.
 
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

	//for hibernate use only, don't use
	@Deprecated
	private HybridRelationship(){
		super();
	}

	
	// ************* CONSTRUCTORS *************/	
	/**
	 * Class constructor: creates a new hybrid relationship instance with no
	 * reference and adds it to the respective
	 * {@link BotanicalName#getHybridRelationships() botanical taxon name relation sets} of both involved names.
	 * 
	 * @param toName			the taxon name to be set as target for the new hybrid relationship
	 * @param fromName			the taxon name to be set as source for the new hybrid relationship
	 * @param type				the relationship type to be assigned to the new hybrid relationship
	 * @param ruleConsidered	the string indicating the article of the ICBN for the hybrid taxon name
	 * @see						#HybridRelationship(BotanicalName, BotanicalName, HybridRelationshipType, ReferenceBase, String, String)
	 * @see						BotanicalName#addHybridRelationship(HybridRelationship)
	 */
	protected HybridRelationship(BotanicalName hybridName, BotanicalName parentName, HybridRelationshipType type, String ruleConsidered) {
		this(hybridName, parentName, type, null, null, ruleConsidered);
	}
	
	/**
	 * Class constructor: creates a new hybrid relationship instance including
	 * its {@link reference.ReferenceBase reference source} and adds it to the respective 
	 *{@link BotanicalName#getHybridRelationships() botanical taxon name relation sets} of both involved names.
	 * 
	 * @param toName				the taxon name to be set as target for the new hybrid relationship
	 * @param fromName				the taxon name to be set as source for the new hybrid relationship
	 * @param type					the relationship type to be assigned to the new hybrid relationship
	 * @param citation				the reference source for the new hybrid relationship
	 * @param citationMicroReference	the string with the details describing the exact localisation within the reference
	 * @param ruleConsidered		the string indicating the article of the ICBN for the hybrid taxon name
	 * @see							#HybridRelationship(BotanicalName, BotanicalName, HybridRelationshipType, String)
	 * @see							BotanicalName#addHybridRelationship(HybridRelationship)
	 */
	protected HybridRelationship(BotanicalName  hybridName, BotanicalName parentName, HybridRelationshipType type, ReferenceBase citation, String citationMicroReference, String ruleConsidered) {
		super(parentName, hybridName, type, citation, citationMicroReference);
		this.setRuleConsidered(ruleConsidered);
	}	
	
	//********* METHODS **************************************/

	/** 
	 * Returns the {@link BotanicalName botanical taxon name} that plays the parent role
	 * in <i>this</i> hybrid relationship.
	 *  
	 * @see   #getHybridName()
	 * @see   common.RelationshipBase#getRelatedFrom()
	 */
	@Transient
	public BotanicalName getParentName(){
		return super.getRelatedFrom();
	}
	/**
	 * @see  #getParentName()
	 */
	public void setParentName(BotanicalName parentName){
		super.setRelatedFrom(parentName);
	}

	/** 
	 * Returns the {@link BotanicalName botanical taxon name} that plays the child role
	 * (the child is actually the hybrid taxon name) in <i>this</i> hybrid relationship.
	 *  
	 * @see   #getParentName()
	 * @see   common.RelationshipBase#getRelatedTo()
	 */
	@Transient
	public BotanicalName getHybridName(){
		return super.getRelatedTo();
	}
	/**
	 * @see  #getHybridName()
	 */
	public void setHybridName(BotanicalName hybridName){
		super.setRelatedTo(hybridName);
	}

	/** 
	 * Returns the ICBN rule considered (that is the
	 * article/note/recommendation in the nomenclatural code) for building
	 * the string representing the hybrid taxon name within <i>this</i> hybrid
	 * relationship.
	 */
	public String getRuleConsidered(){
		return this.ruleConsidered;
	}
	/**
	 * @see  #getRuleConsidered()
	 */
	public void setRuleConsidered(String ruleConsidered){
		this.ruleConsidered = ruleConsidered;
	}

}