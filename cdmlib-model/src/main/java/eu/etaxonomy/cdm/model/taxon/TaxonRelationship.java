/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.validation.Level3;
import eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustBeLowerRankThanParent;
import eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustDeriveNameFromParent;
import eu.etaxonomy.cdm.validation.annotation.ChildTaxaMustNotSkipRanks;

/**
 * The class representing a relationship between two {@link Taxon ("accepted/correct") taxa}. 
 * This includes a {@link TaxonRelationshipType taxon relationship type} (for instance "congruent to" or
 * "misapplied name for").
 * <P>
 * This class corresponds in part to: <ul>
 * <li> Relationship according to the TDWG ontology
 * <li> TaxonRelationship according to the TCS
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:58
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonRelationship", propOrder = {
	"relatedFrom",
	"relatedTo",
	"type"
})
@XmlRootElement(name = "TaxonRelationship")
@Entity
@Audited
@ChildTaxaMustBeLowerRankThanParent(groups = Level3.class)
@ChildTaxaMustNotSkipRanks(groups = Level3.class)
@ChildTaxaMustDeriveNameFromParent(groups = Level3.class)
public class TaxonRelationship extends RelationshipBase<Taxon, Taxon, TaxonRelationshipType> {

	static private final Logger logger = Logger.getLogger(TaxonRelationship.class);

	@XmlElement(name = "RelatedFrom")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.EAGER)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private Taxon relatedFrom;

	@XmlElement(name = "RelatedTo")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.EAGER)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private Taxon relatedTo;
	
    @XmlElement(name = "Type")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.EAGER)
	private TaxonRelationshipType type;
	
	/**
	 * @deprecated for hibernate only, don't use
	 */
	@Deprecated
	private TaxonRelationship(){		
	}
	
	/**
	 * Class constructor: creates a new taxon relationship instance (with the
	 * given "accepted/correct" {@link Taxon taxa}, the given {@link SynonymRelationshipType synonym relationship type}
	 * and with the {@link eu.etaxonomy.cdm.model.reference.Reference reference source} on which the relationship
	 * assertion is based). Moreover the new taxon relationship will be added to
	 * the respective sets of taxon relationships assigned to both taxa.
	 * 
	 * @param from 						the taxon instance to be involved as a source in the new taxon relationship
	 * @param to						the taxon instance to be involved as a target in the new taxon relationship
	 * @param type						the taxon relationship type of the new taxon relationship
	 * @param citation					the reference source for the new taxon relationship
	 * @param citationMicroReference	the string with the details describing the exact localisation within the reference
	 * @see 							eu.etaxonomy.cdm.model.common.RelationshipBase
	 */
	protected TaxonRelationship(Taxon from, Taxon to, TaxonRelationshipType type, Reference citation, String citationMicroReference) {
		super(from, to, type, citation, citationMicroReference);
	}
	
	/** 
	 * Returns the {@link Taxon taxon} involved as a source in <i>this</i>
	 * taxon relationship.
	 *  
	 * @see    #getToTaxon()
	 * @see    Taxon#getRelationsFromThisTaxon()
	 * @see    eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedFrom()
	 * @see    eu.etaxonomy.cdm.model.common.RelationshipBase#getType()
	 */
	@Transient
	public Taxon getFromTaxon(){
		return getRelatedFrom();
	}
	/** 
	 * Sets the given {@link Taxon taxon} as a source in <i>this</i> taxon relationship.
	 * Therefore <i>this</i> taxon relationship will be added to the corresponding
	 * set of taxon relationships assigned to the given taxon. Furthermore if
	 * the given taxon replaces an "old" one <i>this</i> taxon relationship will
	 * be removed from the set of taxon relationships assigned to the "old"
	 * source taxon.
	 *  
	 * @param fromTaxon	the taxon instance to be set as a source in <i>this</i> synonym relationship
	 * @see    			#getFromTaxon()
	 */
	public void setFromTaxon(Taxon fromTaxon){
		setRelatedFrom(fromTaxon);
	}

	/** 
	 * Returns the {@link Taxon taxon} involved as a target in <i>this</i>
	 * taxon relationship.
	 *  
	 * @see    #getFromTaxon()
	 * @see    Taxon#getRelationsToThisTaxon()
	 * @see    eu.etaxonomy.cdm.model.common.RelationshipBase#getRelatedTo()
	 * @see    eu.etaxonomy.cdm.model.common.RelationshipBase#getType()
	 */
	@Transient
	public Taxon getToTaxon(){
		return getRelatedTo();
	}

	/** 
	 * Sets the given {@link Taxon taxon} as a target in <i>this</i> taxon relationship.
	 * Therefore <i>this</i> taxon relationship will be added to the corresponding
	 * set of taxon relationships assigned to the given taxon. Furthermore if
	 * the given taxon replaces an "old" one <i>this</i> taxon relationship will
	 * be removed from the set of taxon relationships assigned to the "old"
	 * target taxon.
	 *  
	 * @param toTaxon	the taxon instance to be set as a target in <i>this</i> synonym relationship
	 * @see    			#getToTaxon()
	 */
	public void setToTaxon(Taxon toTaxon){
		setRelatedTo(toTaxon);
	}

	protected Taxon getRelatedFrom() {
		return relatedFrom;
	}

	protected Taxon getRelatedTo() {
		return relatedTo;
	}

	public TaxonRelationshipType getType() {
		return type;
	}

	protected void setRelatedFrom(Taxon relatedFrom) {
		if (relatedFrom == null){
			this.deletedObjects.add(this.relatedFrom);
		}
		this.relatedFrom = relatedFrom;
	}

	protected void setRelatedTo(Taxon relatedTo) {
		if (relatedTo == null){
			this.deletedObjects.add(this.relatedTo);
		}
		this.relatedTo = relatedTo;
	}

	public void setType(TaxonRelationshipType type) {
		this.type = type;
	}
}