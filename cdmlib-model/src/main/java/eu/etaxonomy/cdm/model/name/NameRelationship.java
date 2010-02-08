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
 * The class representing a relationship between two {@link TaxonNameBase taxon names} according
 * to the {@link NomenclaturalCode nomenclatural code} which governs both of them. 
 * This includes a {@link NameRelationshipType name relationship type} (for instance "later homonym" or
 * "orthographic variant") and the article of the corresponding nomenclatural
 * code on which the assignation of the relationship type is based.
 * <P>
 * This class corresponds partially to: <ul>
 * <li> Relationship according to the TDWG ontology
 * <li> TaxonRelationship according to the TCS
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:37
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NameRelationship", propOrder = {
	"relatedFrom",
	"relatedTo",
	"type",
    "ruleConsidered"
})
@Entity
@Audited
public class NameRelationship extends RelationshipBase<TaxonNameBase, TaxonNameBase, NameRelationshipType> {

  static Logger logger = Logger.getLogger(NameRelationship.class);

    //The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in
	//the note property.
    @XmlElement(name = "RuleConsidered")
	private String ruleConsidered;
    
    @XmlElement(name = "RelatedFrom")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE})
	private TaxonNameBase relatedFrom;

	@XmlElement(name = "RelatedTo")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @Cascade(CascadeType.SAVE_UPDATE)
	private TaxonNameBase relatedTo;
	
    @XmlElement(name = "Type")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private NameRelationshipType type;

	//for hibernate, don't use
	@Deprecated
	private NameRelationship(){
		super();
	}

	
	// ************* CONSTRUCTORS *************/	
	/**
	 * Class constructor: creates a new name relationship instance with no
	 * reference and adds it to the respective
	 * {@link TaxonNameBase#getNameRelations() taxon name relation sets} of both involved names.
	 * 
	 * @param toName			the taxon name to be set as target for the new name relationship
	 * @param fromName			the taxon name to be set as source for the new name relationship
	 * @param type				the relationship type to be assigned to the new name relationship
	 * @param ruleConsidered	the string indicating the article of the nomenclatural code for the new name relationship
	 * @see						#NameRelationship(TaxonNameBase, TaxonNameBase, NameRelationshipType, ReferenceBase, String, String)
	 * @see						TaxonNameBase#addNameRelationship(NameRelationship)
	 * @see						TaxonNameBase#addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
	 * @see						TaxonNameBase#addRelationshipToName(TaxonNameBase, NameRelationshipType, String)
	 */
	protected NameRelationship(TaxonNameBase toName, TaxonNameBase fromName, NameRelationshipType type, String ruleConsidered) {
		this(toName, fromName, type, null, null, ruleConsidered);
	}
	
	/**
	 * Class constructor: creates a new name relationship instance including
	 * its {@link  eu.etaxonomy.cdm.model.reference.ReferenceBase reference source} and adds it to the respective 
	 *{@link TaxonNameBase#getNameRelations() taxon name relation sets} of both involved names.
	 * 
	 * @param toName				the taxon name to be set as target for the new name relationship
	 * @param fromName				the taxon name to be set as source for the new name relationship
	 * @param type					the relationship type to be assigned to the new name relationship
	 * @param citation				the reference source for the new name relationship
	 * @param citationMicroReference	the string with the details describing the exact localisation within the reference
	 * @param ruleConsidered		the string indicating the article of the nomenclatural code justifying the new name relationship
	 * @see							#NameRelationship(TaxonNameBase, TaxonNameBase, NameRelationshipType, String)
	 * @see							TaxonNameBase#addNameRelationship(NameRelationship)
	 * @see							TaxonNameBase#addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
	 * @see							TaxonNameBase#addRelationshipToName(TaxonNameBase, NameRelationshipType, String)
	 */
	protected NameRelationship(TaxonNameBase  toName, TaxonNameBase fromName, NameRelationshipType type, ReferenceBase citation, String citationMicroReference, String ruleConsidered) {
		super(fromName, toName, type, citation, citationMicroReference);
		this.setRuleConsidered(ruleConsidered);
	}
	
	//********* METHODS **************************************/

	/** 
	 * Returns the {@link TaxonNameBase taxon name} that plays the source role
	 * in <i>this</i> taxon name relationship.
	 *  
	 * @see   #getToName()
	 * @see   eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedFrom()
	 */
	@Transient
	public TaxonNameBase getFromName(){
		return this.getRelatedFrom();
	}
	
	/**
	 * @see  #getFromName()
	 */
	void setFromName(TaxonNameBase fromName){
		this.setRelatedFrom(fromName);
	}

	/** 
	 * Returns the {@link TaxonNameBase taxon name} that plays the target role
	 * in <i>this</i> taxon name relationship.
	 *  
	 * @see   #getFromName()
	 * @see   eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedTo()
	 */
	@Transient
	public TaxonNameBase getToName(){
		return this.getRelatedTo();
	}
	
	/**
	 * @see  #getToName()
	 */
	void setToName(TaxonNameBase toName){
		this.setRelatedTo(toName);
	}

	/** 
	 * Returns the nomenclatural code rule considered (that is the
	 * article/note/recommendation in the nomenclatural code ruling
	 * the  taxon name(s) of this nomenclatural status).
	 * The considered rule gives the reason why the
	 * {@link NomenclaturalStatusType nomenclatural status type} has been
	 * assigned to the {@link TaxonNameBase taxon name(s)}.
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

	protected TaxonNameBase getRelatedFrom() {
		return relatedFrom;
	}

	protected TaxonNameBase getRelatedTo() {
		return relatedTo;
	}

	public NameRelationshipType getType() {
		return type;
	}

	protected void setRelatedFrom(TaxonNameBase relatedFrom) {
		if (relatedFrom == null){
			this.deletedObjects.add(this.relatedFrom);
		}
		this.relatedFrom = relatedFrom;
	}

	protected void setRelatedTo(TaxonNameBase relatedTo) {
		if (relatedTo == null){
			this.deletedObjects.add(this.relatedTo);
		}
		this.relatedTo = relatedTo;
	}

	protected void setType(NameRelationshipType type) {
		this.type = type;
	}
}