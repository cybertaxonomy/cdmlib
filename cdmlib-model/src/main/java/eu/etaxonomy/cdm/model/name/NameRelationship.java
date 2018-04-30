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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.validation.Level3;
import eu.etaxonomy.cdm.validation.annotation.BasionymsMustShareEpithetsAndAuthors;
import eu.etaxonomy.cdm.validation.annotation.NamesWithHomotypicRelationshipsMustBelongToSameGroup;

/**
 * The class representing a relationship between two {@link TaxonName taxon names} according
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
@NamesWithHomotypicRelationshipsMustBelongToSameGroup(groups = Level3.class)
@BasionymsMustShareEpithetsAndAuthors(groups = Level3.class)
public class NameRelationship
            extends RelationshipBase<TaxonName, TaxonName, NameRelationshipType>{
	private static final long serialVersionUID = -615987333520172043L;
	private static final Logger logger = Logger.getLogger(NameRelationship.class);

    //The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in
	//the note property.
    @XmlElement(name = "RuleConsidered")
	private String ruleConsidered;

    @XmlElement(name = "RelatedFrom")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
//    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private TaxonName relatedFrom;

	@XmlElement(name = "RelatedTo")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
//    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private TaxonName relatedTo;

    @XmlElement(name = "Type")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private NameRelationshipType type;

    /**
	 * @deprecated for hibernate use only, don't use
	 */
	@Deprecated
	private NameRelationship(){
		super();
	}


	// ************* CONSTRUCTORS *************/
	/**
	 * Class constructor: creates a new name relationship instance with no
	 * reference and adds it to the respective
	 * {@link TaxonName#getNameRelations() taxon name relation sets} of both involved names.
	 *
	 * @param toName			the taxon name to be set as target for the new name relationship
	 * @param fromName			the taxon name to be set as source for the new name relationship
	 * @param type				the relationship type to be assigned to the new name relationship
	 * @param ruleConsidered	the string indicating the article of the nomenclatural code for the new name relationship
	 * @see						#NameRelationship(TaxonName, TaxonName, NameRelationshipType, Reference, String, String)
	 * @see						TaxonName#addNameRelationship(NameRelationship)
	 * @see						TaxonName#addRelationshipFromName(TaxonName, NameRelationshipType, String)
	 * @see						TaxonName#addRelationshipToName(TaxonName, NameRelationshipType, String)
	 */
	protected NameRelationship(TaxonName toName, TaxonName fromName, NameRelationshipType type, String ruleConsidered) {
		this(toName, fromName, type, null, null, ruleConsidered);
	}

	/**
	 * Class constructor: creates a new name relationship instance including
	 * its {@link  eu.etaxonomy.cdm.model.reference.Reference reference source} and adds it to the respective
	 *{@link TaxonName#getNameRelations() taxon name relation sets} of both involved names.
	 *
	 * @param toName				the taxon name to be set as target for the new name relationship
	 * @param fromName				the taxon name to be set as source for the new name relationship
	 * @param type					the relationship type to be assigned to the new name relationship
	 * @param citation				the reference source for the new name relationship
	 * @param citationMicroReference	the string with the details describing the exact localisation within the reference
	 * @param ruleConsidered		the string indicating the article of the nomenclatural code justifying the new name relationship
	 * @see							#NameRelationship(TaxonName, TaxonName, NameRelationshipType, String)
	 * @see							TaxonName#addNameRelationship(NameRelationship)
	 * @see							TaxonName#addRelationshipFromName(TaxonName, NameRelationshipType, String)
	 * @see							TaxonName#addRelationshipToName(TaxonName, NameRelationshipType, String)
	 */
	protected NameRelationship(TaxonName  toName, TaxonName fromName, NameRelationshipType type, Reference citation, String citationMicroReference, String ruleConsidered) {
		super(fromName, toName, type, citation, citationMicroReference);
		this.setRuleConsidered(ruleConsidered);
	}

	//********* METHODS **************************************/

	/**
	 * Returns the {@link TaxonName taxon name} that plays the source role
	 * in <i>this</i> taxon name relationship.
	 *
	 * @see   #getToName()
	 * @see   eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedFrom()
	 */
	public TaxonName getFromName(){
		return this.getRelatedFrom();
	}

	/**
	 * @see  #getFromName()
	 */
	void setFromName(TaxonName fromName){
		this.setRelatedFrom(fromName);
	}

	/**
	 * Returns the {@link TaxonName taxon name} that plays the target role
	 * in <i>this</i> taxon name relationship.
	 *
	 * @see   #getFromName()
	 * @see   eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedTo()
	 */
	public TaxonName getToName(){
		return this.getRelatedTo();
	}

	/**
	 * @see  #getToName()
	 */
	void setToName(TaxonName toName){
		this.setRelatedTo(toName);
	}

	/**
	 * Returns the nomenclatural code rule considered (that is the
	 * article/note/recommendation in the nomenclatural code ruling
	 * the  taxon name(s) of this nomenclatural status).
	 * The considered rule gives the reason why the
	 * {@link NomenclaturalStatusType nomenclatural status type} has been
	 * assigned to the {@link TaxonName taxon name(s)}.
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

	// for extra-package access to relatedFrom use getFromName instead
	@Override
    protected TaxonName getRelatedFrom() {
		return relatedFrom;
	}

    // for extra-package access to relatedFrom use getToName instead
	@Override
    protected TaxonName getRelatedTo() {
		return relatedTo;
	}

	@Override
    public NameRelationshipType getType() {
		return type;
	}

	@Override
    protected void setRelatedFrom(TaxonName relatedFrom) {
		this.relatedFrom = relatedFrom;
	}

	@Override
    protected void setRelatedTo(TaxonName relatedTo) {
		this.relatedTo = relatedTo;
	}

	@Override
    public void setType(NameRelationshipType type) {
		this.type = type;
	}


//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> name relationship. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> name relationship by
	 * modifying only some of the attributes.<BR>
	 * CAUTION: Cloning a relationship will not add the relationship to the according
	 * {@link #relatedFrom} and {@link #relatedTo} objects. The method is meant to be used
	 * mainly for internal purposes (e.g. used within {@link TaxonName#clone()}
	 *
	 * @see eu.etaxonomy.cdm.model.common.RelationshipBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		NameRelationship result;
		try {
			result = (NameRelationship)super.clone();
			//no changes to: relatedFrom, relatedTo, type
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}
