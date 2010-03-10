/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * The class representing a hybrid relationship between one of the {@link BotanicalName parents}
 * of a hybrid taxon name and the hybrid taxon name itself. A hybrid taxon name
 * is a {@link BotanicalName botanical taxon name} assigned to a hybrid plant following
 * the {@link NomenclaturalCode#ICBN() ICBN} (Appendix I). A hybrid taxon name must have one 
 * of the hybrid flags set. The hybrid relationship includes a {@link HybridRelationshipType hybrid relationship type}
 * (for instance "first parent" or "female parent") and the article of the ICBN
 * on which the hybrid taxon name relies.
 * <P>
 * This class corresponds partially to: <ul>
 * <li> Relationship according to the TDWG ontology
 * <li> TaxonRelationship according to the TCS
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:26
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HybridRelationship", propOrder = {
	"relatedFrom",
	"relatedTo",
	"type",
    "ruleConsidered"
})
@Entity
@Audited
public class HybridRelationship extends RelationshipBase<NonViralName, NonViralName, HybridRelationshipType> {
  
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(HybridRelationship.class);
	
	//The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in
	//the note property.
	@XmlElement(name = "RuleConsidered")
	private String ruleConsidered;
	
	@XmlElement(name = "RelatedFrom")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @Cascade(CascadeType.SAVE_UPDATE)
	private NonViralName relatedFrom;

	@XmlElement(name = "RelatedTo")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @Cascade(CascadeType.SAVE_UPDATE)
	private NonViralName relatedTo;
	
    @XmlElement(name = "Type")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
	private HybridRelationshipType type;

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
	protected HybridRelationship(NonViralName hybridName, NonViralName parentName, HybridRelationshipType type, String ruleConsidered) {
		this(hybridName, parentName, type, null, null, ruleConsidered);
	}
	
	/**
	 * Class constructor: creates a new hybrid relationship instance including
	 * its {@link eu.etaxonomy.cdm.model.reference.ReferenceBase reference source} and adds it to the respective 
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
	protected HybridRelationship(NonViralName  hybridName, NonViralName parentName, HybridRelationshipType type, ReferenceBase citation, String citationMicroReference, String ruleConsidered) {
		super(parentName, hybridName, type, citation, citationMicroReference);
		this.setRuleConsidered(ruleConsidered);
	}	
	
	//********* METHODS **************************************/

	/** 
	 * Returns the {@link BotanicalName botanical taxon name} that plays the parent role
	 * in <i>this</i> hybrid relationship.
	 *  
	 * @see   #getHybridName()
	 * @see   eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedFrom()
	 */
	@Transient
	public NonViralName getParentName(){
		return this.getRelatedFrom();
	}
	/**
	 * @see  #getParentName()
	 */
	public void setParentName(NonViralName parentName){
		this.setRelatedFrom(parentName);
	}

	/** 
	 * Returns the {@link BotanicalName botanical taxon name} that plays the child role
	 * (the child is actually the hybrid taxon name) in <i>this</i> hybrid relationship.
	 *  
	 * @see   #getParentName()
	 * @see   eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedTo()
	 */
	@Transient
	public NonViralName getHybridName(){
		return this.getRelatedTo();
	}
	/**
	 * @see  #getHybridName()
	 */
	public void setHybridName(NonViralName hybridName){
		this.setRelatedTo(hybridName);
	}

	/** 
	 * Returns the ICBN rule considered (that is the
	 * article/note/recommendation in the nomenclatural code) for building
	 * the string representing the (child) hybrid {@link BotanicalName taxon name}
	 * within <i>this</i> hybrid relationship.
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

	protected NonViralName getRelatedFrom() {
		return relatedFrom;
	}

	protected NonViralName getRelatedTo() {
		return relatedTo;
	}

	public HybridRelationshipType getType() {
		return type;
	}

	protected void setRelatedFrom(NonViralName relatedFrom) {
		if (relatedFrom == null){
			this.deletedObjects.add(this.relatedFrom);
		}
		this.relatedFrom = relatedFrom;
	}

	protected void setRelatedTo(NonViralName relatedTo) {
		if (relatedTo == null){
			this.deletedObjects.add(this.relatedTo);
		}
		this.relatedTo = relatedTo;
	}

	public void setType(HybridRelationshipType type) {
		this.type = type;
	}
}