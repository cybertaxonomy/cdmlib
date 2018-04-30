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
import eu.etaxonomy.cdm.model.reference.Reference;

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
public class HybridRelationship
        extends RelationshipBase<INonViralName, INonViralName, HybridRelationshipType>
        implements Comparable<HybridRelationship>{

    private static final long serialVersionUID = -78515930138896939L;
    private static final Logger logger = Logger.getLogger(HybridRelationship.class);

	//The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in
	//the note property.
	@XmlElement(name = "RuleConsidered")
	private String ruleConsidered;

	@XmlElement(name = "RelatedFrom")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private TaxonName relatedFrom;

	@XmlElement(name = "RelatedTo")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private TaxonName relatedTo;

    @XmlElement(name = "Type")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
	private HybridRelationshipType type;

	/**
	 * @deprecated for hibernate use only, don't use
	 */
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
	 * @see						#HybridRelationship(BotanicalName, BotanicalName, HybridRelationshipType, Reference, String, String)
	 * @see						TaxonName#addHybridRelationship(HybridRelationship)
	 */
	protected HybridRelationship(INonViralName hybridName, INonViralName parentName, HybridRelationshipType type, String ruleConsidered) {
		this(hybridName, parentName, type, null, null, ruleConsidered);
	}

	/**
	 * Class constructor: creates a new hybrid relationship instance including
	 * its {@link eu.etaxonomy.cdm.model.reference.Reference reference source} and adds it to the respective
	 *{@link BotanicalName#getHybridRelationships() botanical taxon name relation sets} of both involved names.
	 *
	 * @param toName				the taxon name to be set as target for the new hybrid relationship
	 * @param fromName				the taxon name to be set as source for the new hybrid relationship
	 * @param type					the relationship type to be assigned to the new hybrid relationship
	 * @param citation				the reference source for the new hybrid relationship
	 * @param citationMicroReference	the string with the details describing the exact localisation within the reference
	 * @param ruleConsidered		the string indicating the article of the ICBN for the hybrid taxon name
	 * @see							#HybridRelationship(BotanicalName, BotanicalName, HybridRelationshipType, String)
	 * @see							TaxonName#addHybridRelationship(HybridRelationship)
	 */
	protected HybridRelationship(INonViralName  hybridName, INonViralName parentName, HybridRelationshipType type, Reference citation, String citationMicroReference, String ruleConsidered) {
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
	public TaxonName getParentName(){
		return this.getRelatedFrom();
	}
	/**
	 * @see  #getParentName()
	 */
	public void setParentName(INonViralName parentName){
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
	public TaxonName getHybridName(){
		return this.getRelatedTo();
	}
	/**
	 * @see  #getHybridName()
	 */
	public void setHybridName(INonViralName hybridName){
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

	@Override
    protected TaxonName getRelatedFrom() {
		return relatedFrom;
	}

	@Override
    protected TaxonName getRelatedTo() {
		return relatedTo;
	}

	@Override
    public HybridRelationshipType getType() {
		return type;
	}

	@Override
    protected void setRelatedFrom(INonViralName relatedFrom) {
		this.relatedFrom = TaxonName.castAndDeproxy(relatedFrom);
	}

	@Override
    protected void setRelatedTo(INonViralName relatedTo) {
		this.relatedTo = TaxonName.castAndDeproxy(relatedTo);
	}

	@Override
    public void setType(HybridRelationshipType type) {
		this.type = type;
	}

// ************************ compareTo *************************************************

	@Override
    public int compareTo(HybridRelationship rel2) {
		HybridRelationshipType type1 = this.getType();
		HybridRelationshipType type2 = rel2.getType();
		int compareType = type1.compareTo(type2);
		if (compareType != 0){
			return compareType;
		}else{
		    TaxonName related1 = this.getRelatedFrom();
		    TaxonName related2 = rel2.getRelatedFrom();
			if (related1 != related2){
				related1 = this.getRelatedTo();
				related2 = rel2.getRelatedTo();
			}
			if (related1.equals(related2)){
			    return 0;
			}

			String title1 = related1.getTitleCache();
			String title2 = related2.getTitleCache();
			return title1.compareTo(title2);
		}
	}


//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> hybrid relationship. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> hybrid relationship by
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
		HybridRelationship result;
		try {
			result = (HybridRelationship)super.clone();
			//no changes to: relatedFrom, relatedTo, type
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}

}
